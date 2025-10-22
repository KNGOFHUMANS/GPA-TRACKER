import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

public class GoogleSignIn {
    // Put your OAuth desktop client file in the project root with this exact name:
    private static final String CLIENT_SECRET_FILE = "client_secret.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIR = "tokens";

    private static final java.util.List<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/userinfo.email");

    public static String[] authenticate() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                // Load client secrets. Try multiple locations so packaging (jar/exe) works.
                InputStream in = null;
                // 1) try working directory
                try {
                        File f = new File(CLIENT_SECRET_FILE);
                        if (f.exists()) in = new FileInputStream(f);
                } catch (Exception ignored) {}

                // 2) try the folder next to the running jar/exe
                if (in == null) {
                        try {
                                URI codeUri = GoogleSignIn.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                                Path codePath = Paths.get(codeUri).getParent();
                                if (codePath != null) {
                                        File alt = codePath.resolve(CLIENT_SECRET_FILE).toFile();
                                        if (alt.exists()) in = new FileInputStream(alt);
                                }
                        } catch (Exception ignored) {}
                }

                // 3) try classpath resource (if bundled inside jar)
                if (in == null) {
                        in = GoogleSignIn.class.getResourceAsStream("/" + CLIENT_SECRET_FILE);
                }

                if (in == null) {
                        throw new RuntimeException("Missing " + CLIENT_SECRET_FILE + ". Place it next to the jar/exe or in the working directory.");
                }

                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                                JSON_FACTORY, new InputStreamReader(in, StandardCharsets.UTF_8));

        // Determine tokens directory near the application so installed apps store tokens next to app
        File tokenStoreDir = new File(TOKENS_DIR);
        try {
            URI codeUri = GoogleSignIn.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codePath = Paths.get(codeUri).getParent();
            if (codePath != null) tokenStoreDir = codePath.resolve(TOKENS_DIR).toFile();
        } catch (Exception ignored) {}
        if (!tokenStoreDir.exists()) tokenStoreDir.mkdirs();

        // Flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Objects.requireNonNull(tokenStoreDir)))
                .setAccessType("offline")
                .build();

        // Desktop receiver http://localhost:8888
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(8888)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("College GPA Tracker")
                .build();

        Userinfo userInfo = oauth2.userinfo().get().execute();

        // Return email + suggested username
        String email = userInfo.getEmail();
        String suggested = (email != null && email.contains("@")) ? email.split("@")[0] : "user";
        return new String[]{email, suggested};
    }

        /**
         * Delete stored tokens so next authenticate() call prompts the user to choose an account.
         */
        public static void clearStoredCredentials() {
                File dir = new File(TOKENS_DIR);
                if (!dir.exists()) return;
                deleteRecursive(dir);
        }

        private static void deleteRecursive(File f) {
                if (f.isDirectory()) {
                        File[] children = f.listFiles();
                        if (children != null) {
                                for (File c : children) deleteRecursive(c);
                        }
                }
                try {
                        f.delete();
                } catch (Exception ignored) {}
        }
}
