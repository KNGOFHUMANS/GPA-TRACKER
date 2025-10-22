# GPA Tracker Setup Guide

## Prerequisites
- Java JDK 25 or later
- Google Developer Console account

## Quick Start

### 1. Download and Extract
Download `GPATracker-PORT-FIXED.zip` for the ready-to-use application with all fixes applied.

### 2. Google OAuth Setup (Required)
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable Google+ API and Gmail API
4. Create OAuth 2.0 credentials:
   - Application type: Desktop application
   - Name: GPA Tracker
5. Download the credentials JSON file
6. Rename it to `client_secret.json` and place in the application directory

### 3. Run the Application
Extract `GPATracker-PORT-FIXED.zip` and run the executable.

## Features Fixed in This Version

✅ **Login UI Issues Fixed**
- Professional dark theme layout
- Removed duplicate elements
- Clean, centered design

✅ **Google OAuth Completely Working**
- Multi-port fallback (8888, 8080, 9999, random)
- Modern OAuth parameters with account selection
- Robust error handling and retry mechanisms

✅ **Account Switching Fixed**
- Users can now sign out and sign into different Google accounts
- Proper credential clearing between sessions
- Force account selection on each login

✅ **Technical Improvements**
- Added jdk.httpserver module for OAuth LocalServerReceiver
- Comprehensive error handling with user-friendly messages
- Progress dialogs for better user experience
- Port conflict resolution with automatic fallback

## Troubleshooting

### OAuth Issues
- Ensure `client_secret.json` is in the same directory as the executable
- Check that ports 8888, 8080, or 9999 are available
- Application will try multiple ports automatically

### Account Switching
- Use the "Sign Out" option in the application
- Application will force Google account selection on next login

### Port Conflicts
- Application automatically tries multiple ports
- If all fail, it will use a random available port

## Package Contents
- `GPATracker-PORT-FIXED.zip` - Complete working application (38.21 MB)
- All OAuth and UI issues resolved
- Ready for distribution

## Development
For development setup, see `CODE_DOCUMENTATION.md` for technical details.