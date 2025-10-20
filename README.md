GPAManagerApp

A desktop Java app that helps students track their classes, assignments, and GPA.
Built with a clean Swing interface, Google Sign-In, and email password reset.

Overview

GPAManagerApp makes it easy for college students to organize their coursework and see their GPA for each semester and overall.
It includes simple account management, Google login, and a modern dashboard that shows progress and performance trends.

Key Features

Track classes, assignments, and credit hours

Automatic GPA calculation and semester summary

Google Sign-In (OAuth2 desktop login)

Password reset with one-time code sent by email

Visual dashboard with pie charts and badges

JSON-based data storage (no database needed)

Packaged Windows version ready to install

Tech Stack

Java (Swing / AWT)

Gson for JSON data

Jakarta Mail for email sending

Google OAuth 2 desktop flow

jpackage and WiX Toolset for Windows packaging

How to Run

Download and unzip GPAManagerApp-ready.zip.

Run GPAManagerApp.exe.

Make sure client_secret.json is in the same folder so Google login works.

Project Files

CollegeGPATracker.java – Main app logic and UI

MailSender.java – Handles password reset emails

GoogleSignIn.java – Manages Google login tokens

data/ – Stores users and GPA data in JSON files

Resume Highlights

Built a Java desktop GPA tracker with Google Sign-In and email password reset

Designed GPA dashboard with charts and progress badges

Used JSON storage for fast, offline use

Packaged as a Windows EXE for easy sharing

Contact

Malik G. Jones
📧 malikgjones0415@gmail.com

🌐 github.com/KNGOFHUMANS
