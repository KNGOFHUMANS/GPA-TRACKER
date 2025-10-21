GPAManagerApp — Release folder

Included files:
- GPAManagerApp-ready.zip
- README-release.txt
- release_notes.md
- sha256.txt

How to publish to GitHub Releases (recommended)
1. Install GitHub CLI (gh) and authenticate: gh auth login
2. Create a tag and push it:
   git tag -a v1.0.0 -m "v1.0.0"; git push origin v1.0.0
3. Create the release and upload the zip:
   gh release create v1.0.0 .\GPAManagerApp-ready.zip -t "GPAManagerApp v1.0.0" -n "See release_notes.md"

If you prefer the web UI:
- Go to your GitHub repository → Releases → Draft a new release → Upload the GPAManagerApp-ready.zip and paste the release notes.

Checksum verification
- Provide sha256.txt alongside the zip so users can verify the download integrity.

Optional: code signing
- If you expect many Windows users, code-sign the EXE to reduce SmartScreen warnings. This requires a code signing certificate and signtool.
