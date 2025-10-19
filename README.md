# GPAManagerApp

A desktop Java application to track college classes, assignments and GPA with a modern, responsive Swing UI, Google Sign-In support, and password-reset via email. This repository contains the full source, packaged app image, and release artifacts.

## Project snapshot

- **Name:** GPAManagerApp
- **Platform:** Java (Swing desktop application)
- **Purpose:** Let students track classes, assignments, credits and automatically calculate per-class and overall GPA across semesters. Includes account management, Google OAuth sign-in, password-reset by emailed verification code, and a small analytics/dashboard (pie/trend/badges).

## Highlights & features

- Full-featured GPA dashboard
  - Add / remove classes and assignments
  - Custom weights (Homework / Exam / Project) and credit hours per class
  - Class percent → GPA conversion and semester/overall GPA aggregation
  - Visualizations: pie chart breakdown, performance trend sparkline, achievement badges
- User accounts
  - Local JSON-based user store (`data/users.json`)
  - Sign-up, login, change password, change username (15-day cooldown)
  - Google Sign-In (OAuth desktop flow) with persisted tokens
- Password reset flow (one-time code)
  - Generate a one-time reset code and email it to users
  - Persist reset tokens only after successful email delivery
  - Retry logic and helpful user prompts for failed delivery
- Packaging & distribution
  - Packaged with `jpackage` into a Windows app image; `GPAManagerApp-ready.zip` is provided for distribution
  - `client_secret.json` placed next to the EXE so Google Sign-In works out of the box for recipients
- UX polish
  - Custom rounded card UI, placeholder inputs, tasteful color palette
  - Animated pill buttons with hover/press states

## Technology stack

- Language: Java (tested with JDK 17+ / 21+; packaged builds here used a newer JDK)
- UI: Swing/AWT with custom painting
- JSON persistence: Gson
- Email: Jakarta Mail (JavaMail) via SMTP
- OAuth: Google OAuth2 (desktop flow)
- Packaging: jpackage (app-image produced; installer via WiX possible)

## Important files

- `CollegeGPATracker.java` — Main application, UI, data handling, and business logic. Contains login/signup, forgot-password flow, dashboard UI, and custom components.
- `MailSender.java` — Centralized email sending utility with environment/file credential fallback and STARTTLS/SMTPS logic.
- `GoogleSignIn.java` — Helper for Google OAuth desktop flow; stores tokens in `tokens/`.
- `data/` — runtime data: `users.json`, `user_data.json`, `reset_tokens.json`, etc.
- `release/GPAManagerApp-ready.zip` — packaged app image ready to upload to GitHub Releases or distribute.

## How it works (high level)

1. On startup the app ensures `data/` exists and loads users and persisted app data.
2. The Login screen provides: username/email login, Google Sign-In, Create account, Forgot password.
3. Forgot password flow:
   - User provides their account email.
   - App generates a short reset token and attempts to email it.
   - The token is persisted only after email delivery succeeds.
   - User is prompted to enter the code and set a new password.
4. Google Sign-In uses a desktop OAuth flow and persists tokens to `tokens/` so subsequent launches can refresh credentials silently.
5. The dashboard aggregates class GPAs using configurable weights and credits.

## Running locally (developer instructions)

### Prerequisites

- Java JDK 17, 21, or later (only required for building/running from source). Packaged EXE includes a runtime.
- Google client libraries placed in `libs/` (this repo includes the ones used during development).

### Compile & run (development)

```powershell
# from repository root (Windows PowerShell)
# compile (places classes in out/)
javac -cp "libs/*;." -d out *.java

# run (RunWithTrace prints uncaught exceptions and is useful during dev)
java -cp "libs/*;out" RunWithTrace
```

### Run the packaged app (recommended for end users)

- Unzip `release/GPAManagerApp-ready.zip` and run `GPAManagerApp.exe`.
- Ensure `client_secret.json` is at the same level as the EXE and that the `tokens/` directory is writable.

## Configuration — email (password reset)

The app supports multiple ways to provide SMTP credentials (Gmail App Passwords recommended):

1. Environment variables (preferred):
   - `GMAIL_FROM_EMAIL` — sending Gmail address
   - `GMAIL_APP_PASSWORD` — Gmail App Password (16-character)

   Example (PowerShell):
   ```powershell
   $env:GMAIL_FROM_EMAIL = "you@gmail.com"
   $env:GMAIL_APP_PASSWORD = "abcd-efgh-ijkl-mnop"
   ```

2. File fallback: create `data/mail_config.properties` with:

   ```text
   from_email=you@gmail.com
   app_password=abcd-efgh-ijkl-mnop
   ```

Notes:
- To create a Gmail App Password you must enable 2-Step Verification on the Google account and generate the app password at Google Account → Security → App passwords.
- Some corporate networks block outbound SMTP; if emails fail, try from a different network or check firewall rules.

## Configuration — Google OAuth

- **IMPORTANT**: Copy `client_secret.json.template` to `client_secret.json` and fill in your Google OAuth credentials
- Create a project in Google Cloud Console, enable Gmail API, and create OAuth 2.0 desktop credentials
- Place your `client_secret.json` (OAuth client credentials) next to the EXE or in a path discoverable by the app
- The app writes tokens to `tokens/` next to the EXE; ensure that folder exists and is writable by the user running the app

## Packaging & distribution notes

- A ready-to-share ZIP (`release/GPAManagerApp-ready.zip`) is included. It contains `GPAManagerApp.exe`, a bundled runtime, `client_secret.json` (at the EXE root), and `tokens/`.
- To create an installer (.msi/.exe) use `jpackage --type exe` on a machine with WiX installed and on PATH. Consider code signing the EXE to reduce SmartScreen warnings for broad distribution.

## Troubleshooting

- App doesn't open:
  - Run from PowerShell to capture logs:
    ```powershell
    .\GPAManagerApp.exe > run-out.log 2> run-err.log
    notepad run-err.log
    ```
  - If running from source, run `java -cp "libs/*;out" RunWithTrace` and paste any exceptions for debugging.
- Google Sign-In fails:
  - Confirm `client_secret.json` is present next to the EXE and `tokens/` exists and is writable.
- Emails not delivered:
  - Ensure `GMAIL_FROM_EMAIL` and `GMAIL_APP_PASSWORD` are correct and that SMTP ports 587/465 are allowed by your network.
  - Check `MailSender` logs or stack traces for details.

## Security notes

- `client_secret.json` and Gmail App Passwords are sensitive. Only distribute them to trusted recipients. In production, prefer a secure secret store instead of plaintext files.

## Testing & quality

- Use `RunWithTrace` during development to capture uncaught exceptions from the Swing EDT and other threads.
- Add unit tests for GPA calculations and for the MailSender behavior to improve reliability.

## Suggested resume bullets

Use one or more of these on your resume (adjust wording to match your role and contribution):

- Implemented `GPAManagerApp`, a desktop Java application that enables students to track classes, assignments, and compute GPA with a polished Swing UI and local JSON persistence.
- Integrated Google OAuth desktop flow and token persistence for seamless "Sign in with Google" onboarding.
- Designed a secure password-reset workflow using emailed one-time verification codes; added retry logic and only persist tokens after confirmed delivery.
- Built a robust email subsystem with Jakarta Mail supporting STARTTLS and SMTPS fallback, environment-variable and file-based credential resolution, and runtime diagnostics.
- Packaged the application with `jpackage` into a Windows app-image and prepared GitHub Release artifacts for distribution.
- Modernized the Swing UX with custom-drawn components, rounded-card layout, placeholder inputs, and animated buttons.

## Example one-liner for resume header

- GPAManagerApp — Java/Swing desktop app for academic GPA tracking, Google OAuth sign-in, email-based account recovery, and packaged Windows distribution.

## Contributing

- Open an issue describing changes you plan to make.
- Create a feature branch, implement changes, and submit a pull request.

## License

- (Add your preferred license here; MIT is a common choice.)

## Contact

- Add a contact email or your GitHub profile link if you'd like others to reach out.

---

If you'd like, I can also:

- Create a shorter `README-short.md` for the GitHub front page.
- Add a `RESUME.md` with only the resume-ready bullets and a 2–3 line elevator pitch.
- Insert a screenshot into the release assets and update the release notes to include it.

Which of those would you like next?

