GPAManagerApp — Release README

How to run
1. Unzip GPAManagerApp-ready.zip into a folder.
2. Ensure the folder contains:
   - GPAManagerApp.exe
   - client_secret.json (at the same level as the EXE)
   - tokens/ (writable directory)
   - data/ (optional, stores user data)
3. Double-click GPAManagerApp.exe to run.

Email (password reset) setup
- The app can send password-reset emails via Gmail.
- Provide credentials via environment variables or a properties file:
  - Environment variables (PowerShell):
    $env:GMAIL_FROM_EMAIL="you@gmail.com"
    $env:GMAIL_APP_PASSWORD="abcd-efgh-ijkl-mnop"
  - Or file: data/mail_config.properties
    from_email=you@gmail.com
    app_password=abcd-efgh-ijkl-mnop
- To create a Gmail App Password: enable 2-Step Verification in your Google Account, then go to Security → App passwords.

Google Sign-In
- For Google Sign-In to work the folder must contain client_secret.json next to the EXE and a writable tokens/ directory.

Troubleshooting
- If GPAManagerApp.exe does not open: run it from PowerShell to collect logs:
  .\GPAManagerApp.exe > run-out.log 2> run-err.log
  (Open run-err.log if it exists.)
- If OAuth fails: check client_secret.json permissions and tokens/ write access.

Contact
- Include the console error or a screenshot when asking for help.