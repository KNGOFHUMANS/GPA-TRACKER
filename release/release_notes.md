GPAManagerApp Release

What's included
- GPAManagerApp-ready.zip — Ready-to-run Windows app image containing the native launcher, runtime, client_secret.json, and tokens/ directory.

Changelog (v1.0.0)
- Login UI refreshed and made more robust on different screen sizes.
- Inline Forgot Password flow with emailed reset codes.
- Robust MailSender with environment variable and properties file fallbacks.
- Google Sign-In preserved in the packaged app; client_secret.json placed at the EXE root and tokens/ directory created.

How to install
- Upload the GPAManagerApp-ready.zip to GitHub Releases and include this release notes file.

Notes for release page
- Add a short description and the following assets:
  - GPAManagerApp-ready.zip
  - Optional: source code link (repository) and commit hash used to build.

Commands to create release using GitHub CLI
- (Requires gh CLI and authentication)
  gh release create v1.0.0 .\GPAManagerApp-ready.zip -t "GPAManagerApp v1.0.0" -n "See release notes in release_notes.md"
