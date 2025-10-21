GPAManagerApp — Compact README

What
A lightweight Java desktop app for tracking college classes, assignments and GPA with a polished Swing UI, Google Sign-In, and email-based password reset.

Run (end users)
1. Unzip `release/GPAManagerApp-ready.zip`.
2. Run `GPAManagerApp.exe` (no Java install required).
3. Ensure `client_secret.json` sits next to the EXE for Google Sign-In; keep `tokens/` writable.

Run (dev)
```powershell
javac -cp "libs/*;." -d out *.java
java -cp "libs/*;out" RunWithTrace
```

Config (email)
- Set environment vars (preferred):
  - `GMAIL_FROM_EMAIL` and `GMAIL_APP_PASSWORD` (Gmail App Password)
- Or create `data/mail_config.properties` with `from_email` and `app_password`.

Notes
- Data persisted to `data/` (users.json, user_data.json).
- Release asset: `release/GPAManagerApp-ready.zip` (EXE + runtime + client_secret.json + tokens/).

Resume one-liner
- GPAManagerApp — Java/Swing app for GPA tracking with Google OAuth and email-based account recovery.

Contact
- See repository for full README and release notes.