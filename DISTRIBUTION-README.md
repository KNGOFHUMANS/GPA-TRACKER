# 🎓 GPA Tracker - Windows Executable Distribution

## 📥 Download Options

### 🏆 **RECOMMENDED: GPATracker-Optimized.zip (37.9 MB)**
- **Smallest size** with full functionality
- **No Java required** - includes minimal runtime
- **Best performance** - optimized for Windows
- **Just extract and run** GPATracker.exe

### 💿 **Alternative: GPATracker-Standalone.zip (50.9 MB)**
- **Standard build** with full Java runtime
- **Maximum compatibility** - works on all Windows systems
- **All features included**
- **Extract and run** GPATracker.exe

## 🚀 Quick Installation

1. **Download** your preferred zip file above
2. **Extract** to any folder (e.g., Desktop, Program Files)
3. **Double-click** `GPATracker.exe` to launch
4. **Create account** or sign in with Google
5. **Start tracking** your GPA!

*No installation wizard needed - just extract and run!*

## ✨ Features

- 📊 **Multi-Semester Tracking** - Track up to 4 semesters
- 📈 **Visual Analytics** - Pie charts, trends, achievement badges  
- 🔐 **Google Sign-In** - Secure OAuth authentication
- 📧 **Email Password Reset** - Automated reset codes
- 🎨 **Modern Interface** - Professional UI with animations
- 💾 **Auto-Save** - Your data is preserved automatically
- 🏆 **Grade Categories** - Homework, Exams, Projects with custom weights

## 🔧 System Requirements

- **OS**: Windows 10/11 (64-bit)
- **RAM**: 512MB minimum (1GB recommended)
- **Disk**: 100MB free space
- **Network**: Internet connection for Google auth and email features

## 🛠️ Optional Configuration

### Email Password Reset Setup
To enable email password reset functionality:

1. **Set environment variables** (one-time setup):
   ```powershell
   $env:GMAIL_FROM_EMAIL = "your-email@gmail.com"
   $env:GMAIL_APP_PASSWORD = "your-app-password"
   ```

2. **Or create config file** `data/mail_config.properties`:
   ```
   from_email=your-email@gmail.com
   app_password=your-app-password
   ```

*Note: Use Gmail App Password if you have 2FA enabled*

### Google Sign-In
- **Pre-configured** - Works automatically
- **OAuth secure** - No passwords stored locally
- **Account switching** - Can sign out and switch Google accounts

## 📂 File Structure

```
GPATracker/
├── GPATracker.exe          # Main executable
├── runtime/                # Embedded Java runtime
├── app/                    # Application files
│   ├── GPATracker.jar     # Main application
│   ├── *.jar              # Dependencies
│   ├── client_secret.json # Google OAuth config
│   └── data/              # User data storage
└── tokens/                # OAuth tokens (created at runtime)
```

## 🔒 Security & Privacy

- **Local storage only** - All data stays on your computer
- **No telemetry** - No data sent to external servers
- **Secure OAuth** - Google authentication uses industry standards
- **Encrypted storage** - Passwords never stored in plain text

## 🆘 Troubleshooting

### App Won't Start
- **Run as Administrator** if Windows blocks it
- **Check Windows Defender** - may need to allow the executable
- **Verify extraction** - make sure all files are present

### Google Sign-In Issues
- **Check internet connection**
- **Verify client_secret.json** is present in the app folder
- **Try running from command line** to see error messages

### Email Reset Not Working
- **Verify Gmail App Password** - regular passwords won't work with 2FA
- **Check environment variables** or config file
- **Test with simple Gmail account** first

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/KNGOFHUMANS/GPA-TRACKER/issues)
- **Email**: Contact via GitHub profile
- **Documentation**: See CODE_DOCUMENTATION.md for technical details

## 🎯 Quick Start Guide

1. **First Run**: Create account or sign in with Google
2. **Add Semester**: Click "Semester 1" tab
3. **Add Class**: Click "+ Add Class", enter name and credits
4. **Add Assignment**: Select class, click "+ Add Assignment"
5. **View Analytics**: See pie charts, trends, and GPA calculations
6. **Manage Account**: Use User menu for profile settings

## 🔄 Updates

**Current Version**: v1.2.0

- Optimized package size (37.9 MB vs previous 4.3 GB)
- Improved startup performance
- Enhanced Google OAuth integration
- Better error handling and user feedback

**Check for updates**: Visit the GitHub releases page periodically

---

**Built with ❤️ for students** | **Free & Open Source** | **Windows 10/11 Compatible**