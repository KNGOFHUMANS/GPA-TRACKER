# GradeRise - Comprehensive GPA Management Application

## Application Overview

GradeRise is a sophisticated Java-based desktop application designed for college students to track, manage, and analyze their academic performance. The application provides a complete suite of tools for monitoring grades, calculating GPAs, and maintaining academic records across multiple semesters.

## Core Features

### Academic Performance Tracking
- **Multi-Semester Management**: Track academic performance across unlimited semesters with organized data storage
- **Course Management**: Add, edit, and manage individual courses with credit hour tracking
- **Grade Categories**: Support for multiple assignment types including homework, exams, projects, and participation
- **Weighted Calculations**: Configurable weighting systems for different assignment categories
- **Real-Time GPA Calculation**: Automatic computation of semester and cumulative GPAs using standard 4.0 scale

### User Authentication System
- **Google OAuth Integration**: Secure authentication using Google Sign-In with OAuth 2.0 protocol
- **Account Management**: User profile creation and management with Google account integration
- **Session Persistence**: Automatic login state management with secure token storage
- **Multi-User Support**: Individual user accounts with separate data isolation

### Data Management and Persistence
- **Local Data Storage**: All user data stored locally in JSON format for privacy and offline access
- **Automatic Backup**: Continuous data saving with corruption recovery mechanisms
- **Import/Export Functionality**: Data portability for backup and transfer purposes
- **User Preferences**: Customizable application settings and display preferences

### Email Integration
- **Password Reset System**: Automated password reset functionality via email
- **Notification System**: Grade alerts and academic milestone notifications
- **SMTP Integration**: Full email capability using Jakarta Mail libraries
- **Security Compliance**: Encrypted email communications with app-specific passwords

## Technical Architecture

### Application Framework
- **Java Swing GUI**: Professional desktop interface with responsive design
- **Event-Driven Architecture**: Efficient user interaction handling with asynchronous processing
- **MVC Design Pattern**: Clean separation of data, presentation, and business logic
- **Cross-Platform Compatibility**: Runs on Windows, macOS, and Linux systems

### Security Implementation
- **OAuth 2.0 Authentication**: Industry-standard Google authentication protocol
- **Local Credential Storage**: Encrypted token storage with automatic refresh mechanisms
- **Data Encryption**: Sensitive information protected using standard encryption algorithms
- **Input Validation**: Comprehensive data validation to prevent security vulnerabilities

### External API Integration
- **Google APIs**: Complete integration with Google authentication and user information services
- **HTTP Client Libraries**: Robust network communication with retry mechanisms and error handling
- **JSON Processing**: Efficient data serialization and deserialization using Jackson libraries
- **gRPC Communication**: High-performance communication protocols for Google services

## Distribution and Deployment

### Portable Application Design
- **Single JAR Distribution**: Complete application packaged in one 38.5MB executable JAR file
- **Embedded Dependencies**: All required libraries included to eliminate dependency conflicts
- **No Installation Required**: Direct execution without system installation or administrator privileges
- **Self-Contained Runtime**: Optional bundled Java runtime for systems without Java installed

### Executable Generation
- **Launch4j Integration**: Windows executable creation with professional application appearance
- **Icon Customization**: Custom application icons and branding elements
- **System Integration**: Proper Windows application behavior with taskbar and system tray support
- **Resource Management**: Efficient memory and CPU usage optimization

## Data Structure and Organization

### Academic Data Model
- **Hierarchical Organization**: Semesters contain courses, courses contain assignments
- **Flexible Grading Systems**: Support for letter grades, percentage grades, and point systems
- **Credit Hour Tracking**: Accurate GPA calculations based on course credit weighting
- **Historical Data**: Complete academic history with trend analysis capabilities

### File System Architecture
```
Application Directory/
├── GradeRise-Complete-Distribution.jar    # Main application
├── data/                                  # User data storage
│   ├── user_data.json                    # Academic records
│   ├── user_prefs.json                   # Application settings
│   ├── semester_order.json               # Academic calendar
│   └── username_changes.json             # Account modifications
├── tokens/                               # Authentication data
│   └── StoredCredential                  # Encrypted OAuth tokens
├── client_secret.json                    # Google API configuration
└── custom-jre/                          # Optional Java runtime
```

## Performance and Reliability

### Error Handling and Recovery
- **Comprehensive Exception Management**: Robust error handling with user-friendly error messages
- **Data Integrity Checks**: Automatic validation of stored data with corruption detection
- **Graceful Degradation**: Continued functionality when network services are unavailable
- **Debug Logging System**: Detailed logging for troubleshooting and support

### Resource Optimization
- **Memory Management**: Efficient memory usage with garbage collection optimization
- **Startup Performance**: Fast application initialization with lazy loading of non-essential components
- **Network Efficiency**: Optimized API calls with caching and batch operations
- **Storage Optimization**: Compressed data storage with minimal disk space requirements

## Compatibility and Requirements

### System Requirements
- **Java Runtime**: Java 8 or higher (included in distribution package)
- **Operating System**: Windows 10/11, macOS 10.12+, Linux (most distributions)
- **Memory**: Minimum 512MB RAM, recommended 1GB
- **Storage**: 100MB free disk space for application and data
- **Network**: Internet connection required for Google authentication and email features

### Browser Compatibility
- **OAuth Authentication**: Compatible with all modern web browsers for Google Sign-In
- **Automatic Browser Detection**: Seamless integration with default system browser
- **Cross-Browser Support**: Works with Chrome, Firefox, Safari, Edge, and other standards-compliant browsers

## Privacy and Data Security

### Data Protection
- **Local Storage Only**: All academic data stored exclusively on user's device
- **No Data Transmission**: Grade information never transmitted to external servers
- **Encrypted Authentication**: OAuth tokens encrypted using industry-standard protocols
- **Privacy Compliance**: No user tracking, analytics, or data collection beyond authentication

### Security Measures
- **Secure Communication**: All network communications use HTTPS/TLS encryption
- **Token Management**: Automatic token refresh and secure storage mechanisms
- **Input Sanitization**: Protection against injection attacks and malicious input
- **Access Control**: User-specific data access with proper authentication verification

## Development and Maintenance

### Code Quality
- **Modular Design**: Well-organized codebase with clear separation of concerns
- **Documentation**: Comprehensive inline documentation and API references
- **Error Reporting**: Detailed error messages for debugging and user support
- **Version Control**: Complete development history with Git version tracking

### Extensibility
- **Plugin Architecture**: Designed for future feature additions and modifications
- **API Integration Points**: Easy integration with additional academic services
- **Customizable Components**: Flexible UI and functionality customization options
- **Scalable Data Model**: Support for additional academic tracking features

This application represents a complete solution for academic performance management, combining modern software engineering practices with practical student needs in a secure, reliable, and user-friendly package.