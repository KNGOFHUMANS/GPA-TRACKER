# College GPA Tracker — Setup
This small desktop app (Swing) supports:
- Local username/password accounts
- Password reset via emailed 6-digit code
- Google Sign-In (OAuth) — optional

Important setup steps

1) Java and dependencies
- Install JDK 11+ and ensure `javac` and `java` are on your PATH.
- The `libs/` directory contains required jars (Jakarta Mail, Google API clients, Gson, etc.).

2) Email (Gmail SMTP)
- Create a Gmail App Password (recommended) for the account you want to send from.
  - In Google Account > Security > App passwords. Create a "Mail" app password for "Other (Custom name)".
  - Copy the 16-character password.
- Set the following environment variables so the app can send mail:
  - `GMAIL_FROM_EMAIL` — the sender address (e.g. your@gmail.com)
  - `GMAIL_APP_PASSWORD` — the 16-character app password you created
- PowerShell example (temporary for session):
```powershell
$env:GMAIL_FROM_EMAIL = 'your@gmail.com'
$env:GMAIL_APP_PASSWORD = 'your-16-char-app-password'
```
- To set them permanently on Windows, use System Properties → Environment Variables.
- If email sending fails (network, wrong credentials), the app will fall back to showing the reset code in a dialog so you can still reset.

3) Google Sign-In (optional)
- To enable the Google Sign-In button you need an OAuth 2.0 Desktop client credentials file named `client_secret.json` in the project root.
- Create credentials in Google Cloud Console: OAuth consent screen → Credentials → Create OAuth client ID → Desktop.
- Download the JSON and save it as `client_secret.json` in the app folder.
- The first time you sign in, a browser window will open and you'll approve the request.
- Tokens are stored in the `tokens/` folder.

4) Build and run (PowerShell)
```powershell
# ensure env vars are set in the same session (see above)
javac -cp libs/* CollegeGPATracker.java MailSender.java GoogleSignIn.java
java -cp .;libs/* CollegeGPATracker
```

Notes and options
- The app no longer contains an embedded sender address or app password in source; set `GMAIL_FROM_EMAIL` and `GMAIL_APP_PASSWORD` instead.
- If you'd prefer a UI for SMTP settings, I can add a small Settings dialog and persist the values in `data/` (encrypted or obfuscated on request).

If you'd like me to proceed with any of these follow-ups, tell me which one:
- Read `GMAIL_FROM_EMAIL` from a settings dialog instead of env var.
- Remove the fallback dialog (codes delivered only by email).
- Add logging of email send attempts to `data/email.log`.

Important setup steps

1) Java and dependencies
- Install JDK 11+ and ensure `javac` and `java` are on your PATH.
- The `libs/` directory contains required jars (Jakarta Mail, Google API clients, Gson, etc.).

2) Email (Gmail SMTP)
- Create a Gmail App Password (recommended) for the account you want to send from.
  - In Google Account > Security > App passwords. Create a "Mail" app password for "Other (Custom name)".
  - Copy the 16-character password.
- Set an environment variable named `GMAIL_APP_PASSWORD` with that app password.
  - Windows PowerShell (temporary for session):
    ```powershell
    $env:GMAIL_APP_PASSWORD = 'your-16-char-app-password'
    ```
  - To set it permanently on Windows, use System Properties → Environment Variables.
- The app sends mail from the address set in `MailSender.java` (`FROM_EMAIL`). Change it if you need to send from another account.
- If email sending fails, the app will fall back to showing the reset code in a dialog so you can still reset.

3) Google Sign-In (optional)
- To enable the Google Sign-In button you need a OAuth 2.0 Desktop client credentials file named `client_secret.json` in the project root.
- Create credentials in Google Cloud Console: OAuth consent screen + Credentials → Create OAuth client ID → Desktop.
- Download the JSON and save it as `client_secret.json` in the app folder.
- The first time you sign in, a browser window will open.
- Tokens are stored in `tokens/` folder.

4) Build and run
- Compile:
  ```powershell
  javac -cp libs/* CollegeGPATracker.java MailSender.java GoogleSignIn.java
  ```
- Run:
  ```powershell
  java -cp .;libs/* CollegeGPATracker
  ```

If you want, I can:
- Remove `FROM_EMAIL` from source and read it from an env var too.
- Add a small settings dialog for email address, SMTP server, etc.
- Hide the fallback dialog so codes are only delivered by email (not recommended for development).

