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
        System.out.println("DEBUG: GoogleSignIn.authenticate() called");
        
        // Clear any existing tokens to force account selection
        clearStoredCredentials();
        System.out.println("DEBUG: Cleared existing credentials to force account selection");
        
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        System.out.println("DEBUG: HTTP transport created");
        
        // Load client secrets. Try multiple locations so packaging (jar/exe) works.
        InputStream in = null;
        // 1) try working directory
        try {
                File f = new File(CLIENT_SECRET_FILE);
                System.out.println("DEBUG: Checking for client secret at: " + f.getAbsolutePath());
                if (f.exists()) {
                    in = new FileInputStream(f);
                    System.out.println("DEBUG: Found client secret in working directory");
                }
        } catch (Exception e) {
            System.out.println("DEBUG: Failed to load from working directory: " + e.getMessage());
        }

        // 2) try the folder next to the running jar/exe
        if (in == null) {
                try {
                        URI codeUri = GoogleSignIn.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                        Path codePath = Paths.get(codeUri).getParent();
                        System.out.println("DEBUG: Code path: " + codePath);
                        if (codePath != null) {
                                File alt = codePath.resolve(CLIENT_SECRET_FILE).toFile();
                                System.out.println("DEBUG: Checking alternative path: " + alt.getAbsolutePath());
                                if (alt.exists()) {
                                    in = new FileInputStream(alt);
                                    System.out.println("DEBUG: Found client secret next to jar/exe");
                                }
                        }
                } catch (Exception e) {
                    System.out.println("DEBUG: Failed to load from jar/exe directory: " + e.getMessage());
                }
        }

        // 3) try classpath resource (if bundled inside jar)
        if (in == null) {
                in = GoogleSignIn.class.getResourceAsStream("/" + CLIENT_SECRET_FILE);
                if (in != null) {
                    System.out.println("DEBUG: Found client secret as classpath resource");
                } else {
                    System.out.println("DEBUG: Client secret not found as classpath resource");
                }
        }

        if (in == null) {
                throw new RuntimeException("Missing " + CLIENT_SECRET_FILE + ". Place it next to the jar/exe or in the working directory.");
        }

        System.out.println("DEBUG: Loading client secrets from stream");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                        JSON_FACTORY, new InputStreamReader(in, StandardCharsets.UTF_8));
        System.out.println("DEBUG: Client secrets loaded successfully");

        // Determine tokens directory near the application so installed apps store tokens next to app
        File tokenStoreDir = new File(TOKENS_DIR);
        try {
            URI codeUri = GoogleSignIn.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codePath = Paths.get(codeUri).getParent();
            if (codePath != null) tokenStoreDir = codePath.resolve(TOKENS_DIR).toFile();
        } catch (Exception e) {
            System.out.println("DEBUG: Failed to determine tokens directory: " + e.getMessage());
        }
        System.out.println("DEBUG: Using tokens directory: " + tokenStoreDir.getAbsolutePath());
        if (!tokenStoreDir.exists()) {
            boolean created = tokenStoreDir.mkdirs();
            System.out.println("DEBUG: Created tokens directory: " + created);
        }

        // Flow - create OAuth flow (account selection handled in authorization URL)
        System.out.println("DEBUG: Creating OAuth flow");
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Objects.requireNonNull(tokenStoreDir)))
                .setAccessType("offline")
                .build();

        // Desktop receiver - try multiple ports for better compatibility
        System.out.println("DEBUG: Creating local server receiver with port fallback");
        LocalServerReceiver receiver = null;
        Exception lastException = null;
        
        // Try common OAuth ports - use a more robust approach
        int[] ports = {8888, 8080, 9999, 0}; // 0 means random available port
        
        for (int port : ports) {
            try {
                System.out.println("DEBUG: Attempting to create receiver for port: " + (port == 0 ? "random" : port));
                
                // Create the receiver
                LocalServerReceiver.Builder builder = new LocalServerReceiver.Builder()
                        .setHost("localhost");
                        
                if (port != 0) {
                    builder.setPort(port);
                }
                
                receiver = builder.build();
                
                // The actual port binding test will happen during the OAuth flow
                // For now, just confirm the receiver was created
                System.out.println("DEBUG: Receiver created for port: " + (port == 0 ? "random" : port));
                break;
                
            } catch (Exception e) {
                System.out.println("DEBUG: Failed to create receiver for port " + port + ": " + e.getMessage());
                lastException = e;
                if (receiver != null) {
                    try {
                        receiver.stop();
                    } catch (Exception ignored) {}
                    receiver = null;
                }
            }
        }
        
        if (receiver == null) {
            throw new RuntimeException("Failed to create local server receiver on any port. Tried ports: 8888, 8080, 9999, and random. Last error: " + (lastException != null ? lastException.getMessage() : "Unknown"), lastException);
        }

        System.out.println("DEBUG: Starting authorization flow with port fallback");
        Exception authException = null;
        
        // Try the OAuth flow with automatic port fallback
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                System.out.println("DEBUG: Authorization attempt " + (attempt + 1));
                
                // If this is a retry, create a new receiver with a different port strategy
                if (attempt > 0) {
                    System.out.println("DEBUG: Retrying with different port configuration");
                    try {
                        receiver.stop();
                    } catch (Exception ignored) {}
                    
                    // For retries, use random port (0) to avoid conflicts
                    receiver = new LocalServerReceiver.Builder()
                            .setHost("localhost")
                            .setPort(0)  // Use random available port
                            .build();
                    System.out.println("DEBUG: Created new receiver with random port for retry");
                }
                
                // Force account selection by creating custom authorization request
                AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver) {
                    @Override
                    protected void onAuthorization(com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl authorizationUrl) throws java.io.IOException {
                        // Add parameters to force account selection
                        authorizationUrl.set("prompt", "select_account");
                        authorizationUrl.set("include_granted_scopes", "true");
                        super.onAuthorization(authorizationUrl);
                    }
                };
                
                Credential credential = app.authorize("user");
                System.out.println("DEBUG: Authorization successful on attempt " + (attempt + 1));
                
                Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                        .setApplicationName("College GPA Tracker")
                        .build();

                System.out.println("DEBUG: Getting user info");
                Userinfo userInfo = oauth2.userinfo().get().execute();

                // Return email + suggested username
                String email = userInfo.getEmail();
                String suggested = (email != null && email.contains("@")) ? email.split("@")[0] : "user";
                System.out.println("DEBUG: Successfully got user email: " + email);
                return new String[]{email, suggested};
                
            } catch (java.net.BindException bindEx) {
                System.err.println("DEBUG: Port binding failed on attempt " + (attempt + 1) + ": " + bindEx.getMessage());
                authException = bindEx;
                
                if (attempt < 2) { // Don't sleep on the last attempt
                    try {
                        Thread.sleep(1000); // Wait 1 second before retry
                    } catch (InterruptedException ignored) {}
                }
                continue; // Try next attempt
                
            } catch (Exception e) {
                System.err.println("DEBUG: Authorization failed on attempt " + (attempt + 1) + ": " + e.getMessage());
                authException = e;
                
                // For non-binding exceptions, don't retry
                if (!e.getMessage().contains("bind") && !e.getMessage().contains("Address already in use")) {
                    break;
                }
                
                if (attempt < 2) { // Don't sleep on the last attempt
                    try {
                        Thread.sleep(1000); // Wait 1 second before retry
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        
        // If we get here, all attempts failed
        System.err.println("DEBUG: All authorization attempts failed");
        
        // Provide helpful error message based on the last exception
        if (authException != null) {
            if (authException.getMessage() != null) {
                if (authException.getMessage().contains("400") || authException.getMessage().contains("redirect_uri_mismatch")) {
                    throw new RuntimeException("Google OAuth configuration issue. The redirect URI in your Google Cloud Console may not match. Expected: http://localhost with ports 8888, 8080, or 9999", authException);
                } else if (authException.getMessage().contains("Connection refused") || authException.getMessage().contains("ConnectException")) {
                    throw new RuntimeException("Network connection failed. Please check your internet connection and firewall settings.", authException);
                } else if (authException.getMessage().contains("403") || authException.getMessage().contains("access_denied")) {
                    throw new RuntimeException("Access denied. Please ensure the OAuth client is properly configured in Google Cloud Console.", authException);
                } else if (authException.getMessage().contains("bind") || authException.getMessage().contains("Address already in use")) {
                    throw new RuntimeException("All local ports (8888, 8080, 9999, and random) are in use. Please close other applications using these ports or restart your computer.", authException);
                }
            }
            throw authException;
        }
        
        throw new RuntimeException("Authorization failed for unknown reasons");
    }

        /**
         * Delete stored tokens so next authenticate() call prompts the user to choose an account.
         * This clears both local tokens and ensures the browser prompts for account selection.
         */
        public static void clearStoredCredentials() {
                System.out.println("DEBUG: Clearing stored Google credentials...");
                
                // Clear tokens directory
                File dir = new File(TOKENS_DIR);
                if (dir.exists()) {
                    deleteRecursive(dir);
                    System.out.println("DEBUG: Deleted tokens directory");
                }
                
                // Also check the alternative tokens location
                try {
                    URI codeUri = GoogleSignIn.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                    Path codePath = Paths.get(codeUri).getParent();
                    if (codePath != null) {
                        File altDir = codePath.resolve(TOKENS_DIR).toFile();
                        if (altDir.exists() && !altDir.equals(dir)) {
                            deleteRecursive(altDir);
                            System.out.println("DEBUG: Deleted alternative tokens directory");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Could not clear alternative tokens directory: " + e.getMessage());
                }
                
                System.out.println("DEBUG: Google credentials cleared successfully");
        }

        private static void deleteRecursive(File f) {
                if (f.isDirectory()) {
                        File[] children = f.listFiles();
                        if (children != null) {
                                for (File c : children) deleteRecursive(c);
                        }
                }
                try {
                        boolean deleted = f.delete();
                        System.out.println("DEBUG: Deleted " + f.getAbsolutePath() + ": " + deleted);
                } catch (Exception e) {
                        System.out.println("DEBUG: Failed to delete " + f.getAbsolutePath() + ": " + e.getMessage());
                }
        }
}
