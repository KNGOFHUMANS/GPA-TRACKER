 Developer Setup Notes

Clone repo →
git clone https://github.com/KNGOFHUMANS/GPA-TRACKER.git
cd GPA-TRACKER

Copy OAuth template →
copy client_secret.json.template client_secret.json

Go to Google Cloud Console

Create new project

Enable Google+ API

Create OAuth 2.0 (Desktop App)

Download JSON

Paste contents into client_secret.json

client_secret.json = your real credentials (ignored by git)

client_secret.json.template = safe placeholder (shared)

⚙️ Workflow Notes

Work locally with real credentials

Push only the template

Other devs add their own JSON

Keeps data secure + easy to set up

 App Status

Dark login theme 

Google OAuth (multi-port) 

Account switching 

Error handling + credential checks 

Production ready