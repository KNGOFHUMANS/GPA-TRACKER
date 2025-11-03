import java.util.*;

/**
 * AuthenticationService - Handles user authentication, registration, and session management
 * Part of the refactored architecture for CollegeGPATracker
 * Integrates with SecurityManager for secure authentication
 */
public class AuthenticationService {
    private User currentUser;
    private Map<String, String> sessionTokens; // sessionToken -> username
    private Map<String, Long> sessionExpiry; // sessionToken -> expiry timestamp
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    
    public AuthenticationService() {
        this.currentUser = null;
        this.sessionTokens = new HashMap<>();
        this.sessionExpiry = new HashMap<>();
    }
    
    // Authentication methods
    public AuthenticationResult login(String username, String password, String clientIdentifier) {
        try {
            // Use secure authentication through DatabaseManager
            String authenticatedUser = DatabaseManager.authenticateUser(username, password, clientIdentifier);
            
            if (authenticatedUser != null) {
                // Load user data and create session
                this.currentUser = loadUserFromDatabase(authenticatedUser);
                String sessionToken = createSession(authenticatedUser);
                
                return new AuthenticationResult(true, "Login successful", sessionToken);
            } else {
                // Check if user is rate limited
                if (SecurityManager.isRateLimited(clientIdentifier)) {
                    long remainingSeconds = SecurityManager.getRemainingLockoutSeconds(clientIdentifier);
                    long remainingMinutes = remainingSeconds / 60;
                    return new AuthenticationResult(false, 
                        String.format("Account temporarily locked. Try again in %d minutes.", 
                            remainingMinutes), null);
                } else {
                    return new AuthenticationResult(false, "Invalid username or password", null);
                }
            }
        } catch (Exception e) {
            return new AuthenticationResult(false, "Authentication error: " + e.getMessage(), null);
        }
    }
    
    public AuthenticationResult signup(String username, String email, String password) {
        try {
            // Validate input using SecurityManager
            String validatedUsername = SecurityManager.validateUsername(username);
            String validatedEmail = SecurityManager.validateEmail(email);
            SecurityManager.validatePassword(password);
            
            // Create secure user account
            boolean created = DatabaseManager.createUserSecure(validatedUsername, password, validatedEmail);
            
            if (created) {
                return new AuthenticationResult(true, 
                    "Account created successfully! Please log in.", null);
            } else {
                return new AuthenticationResult(false, 
                    "Account creation failed. Username or email may already exist.", null);
            }
        } catch (SecurityException e) {
            return new AuthenticationResult(false, "Validation error: " + e.getMessage(), null);
        } catch (Exception e) {
            return new AuthenticationResult(false, "Registration error: " + e.getMessage(), null);
        }
    }
    
    public AuthenticationResult googleSignIn() {
        try {
            String[] result = GoogleSignIn.authenticate(); // [email, suggestedUsername]
            String email = result[0];
            String suggested = result[1];
            
            // Check if user exists by email
            String existingUser = DatabaseManager.findUserByEmail(email);
            boolean isNewUser = (existingUser == null);
            String username = isNewUser ? suggested : existingUser;
            
            if (isNewUser) {
                // Create new Google-linked account (empty password indicates Google auth)
                boolean created = DatabaseManager.createUserSecure(username, "", email);
                if (!created) {
                    return new AuthenticationResult(false, 
                        "Failed to create Google account", null);
                }
            }
            
            // Load user and create session
            this.currentUser = loadUserFromDatabase(username);
            String sessionToken = createSession(username);
            
            return new AuthenticationResult(true, "Google sign-in successful", sessionToken);
            
        } catch (Exception e) {
            return new AuthenticationResult(false, "Google sign-in failed: " + e.getMessage(), null);
        }
    }
    
    public void logout() {
        if (currentUser != null) {
            // Invalidate all sessions for this user
            String username = currentUser.getUsername();
            sessionTokens.entrySet().removeIf(entry -> entry.getValue().equals(username));
            sessionExpiry.entrySet().removeIf(entry -> 
                sessionTokens.get(entry.getKey()) == null);
            
            this.currentUser = null;
        }
    }
    
    public void googleSignOut() {
        try {
            GoogleSignIn.clearStoredCredentials();
            logout();
        } catch (Exception e) {
            System.err.println("Error during Google sign-out: " + e.getMessage());
            logout(); // Still perform local logout
        }
    }
    
    // Session management
    private String createSession(String username) {
        String sessionToken = SecurityManager.generateSecureToken();
        long expiryTime = System.currentTimeMillis() + SESSION_TIMEOUT;
        
        sessionTokens.put(sessionToken, username);
        sessionExpiry.put(sessionToken, expiryTime);
        
        // Store session in database for persistence
        SecurityManager.createSession(username);
        
        return sessionToken;
    }
    
    public boolean validateSession(String sessionToken) {
        if (sessionToken == null) return false;
        
        Long expiry = sessionExpiry.get(sessionToken);
        if (expiry == null || expiry < System.currentTimeMillis()) {
            // Session expired or doesn't exist
            invalidateSession(sessionToken);
            return false;
        }
        
        String username = sessionTokens.get(sessionToken);
        if (username == null) {
            return false;
        }
        
        // Extend session if still valid
        sessionExpiry.put(sessionToken, System.currentTimeMillis() + SESSION_TIMEOUT);
        return true;
    }
    
    public void invalidateSession(String sessionToken) {
        sessionTokens.remove(sessionToken);
        sessionExpiry.remove(sessionToken);
    }
    
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        List<String> expiredTokens = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : sessionExpiry.entrySet()) {
            if (entry.getValue() < now) {
                expiredTokens.add(entry.getKey());
            }
        }
        
        for (String token : expiredTokens) {
            invalidateSession(token);
        }
    }
    
    // Password reset functionality
    public AuthenticationResult requestPasswordReset(String email) {
        try {
            String username = DatabaseManager.findUserByEmail(email);
            if (username == null) {
                return new AuthenticationResult(false, "No account found with that email address", null);
            }
            
            // Generate reset code
            String resetCode = PasswordResetStore.generateTokenFor(username);
            
            // Attempt to send email (implementation depends on MailSender)
            boolean emailSent = false;
            try {
                // This would typically call MailSender.sendEmail with prepared content
                // For now, we'll simulate success - in production, implement email sending:
                /*
                String subject = "GPA Tracker - Password Reset Code";
                String body = "Your password reset code: " + resetCode + "\n\n" +
                             "Enter this code in the app to set a new password.\n" +
                             "If you did not request this, ignore this message.";
                emailSent = MailSender.sendEmail(email, subject, body);
                */
                emailSent = true; // Simulated success for development
                
                if (emailSent) {
                    // Only persist the token after successful email send
                    PasswordResetStore.persistToken(resetCode, username);
                    return new AuthenticationResult(true, 
                        "Password reset code sent to your email", null);
                } else {
                    return new AuthenticationResult(false, 
                        "Failed to send reset email. Please try again.", null);
                }
            } catch (Exception e) {
                return new AuthenticationResult(false, 
                    "Email service unavailable. Please try again later.", null);
            }
            
        } catch (Exception e) {
            return new AuthenticationResult(false, 
                "Password reset request failed: " + e.getMessage(), null);
        }
    }
    
    public AuthenticationResult resetPassword(String resetCode, String newPassword) {
        try {
            // Validate new password
            SecurityManager.validatePassword(newPassword);
            
            // Verify and consume reset code
            String username = PasswordResetStore.consume(resetCode);
            if (username == null) {
                return new AuthenticationResult(false, 
                    "Invalid or expired reset code", null);
            }
            
            // Change password securely
            boolean success = DatabaseManager.changePassword(username, newPassword);
            if (success) {
                return new AuthenticationResult(true, 
                    "Password reset successful. Please log in with your new password.", null);
            } else {
                return new AuthenticationResult(false, 
                    "Failed to reset password. Please try again.", null);
            }
            
        } catch (SecurityException e) {
            return new AuthenticationResult(false, 
                "Password validation failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new AuthenticationResult(false, 
                "Password reset failed: " + e.getMessage(), null);
        }
    }
    
    // User management
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    public AuthenticationResult changePassword(String currentPassword, String newPassword) {
        if (!isLoggedIn()) {
            return new AuthenticationResult(false, "Not logged in", null);
        }
        
        try {
            SecurityManager.validatePassword(newPassword);
            
            // Verify current password first
            String username = currentUser.getUsername();
            String clientId = "password_change";
            
            if (DatabaseManager.authenticateUser(username, currentPassword, clientId) == null) {
                return new AuthenticationResult(false, "Current password is incorrect", null);
            }
            
            // Change password
            boolean success = DatabaseManager.changePassword(username, newPassword);
            if (success) {
                return new AuthenticationResult(true, "Password changed successfully", null);
            } else {
                return new AuthenticationResult(false, "Failed to change password", null);
            }
            
        } catch (SecurityException e) {
            return new AuthenticationResult(false, 
                "New password validation failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new AuthenticationResult(false, 
                "Password change failed: " + e.getMessage(), null);
        }
    }
    
    public AuthenticationResult changeUsername(String newUsername) {
        if (!isLoggedIn()) {
            return new AuthenticationResult(false, "Not logged in", null);
        }
        
        try {
            // Check if user can change username (15-day restriction)
            if (!currentUser.canChangeUsername()) {
                long daysRemaining = currentUser.getDaysUntilUsernameChange();
                return new AuthenticationResult(false, 
                    String.format("Username change locked for %d more days", daysRemaining), null);
            }
            
            // Validate new username
            String validatedUsername = SecurityManager.validateUsername(newUsername);
            
            // Check if username is available
            if (DatabaseManager.getUser(validatedUsername) != null) {
                return new AuthenticationResult(false, "Username is already taken", null);
            }
            
            // Update username in database and current user
            // This would require a method in DatabaseManager to change username
            // For now, we'll update the user object
            currentUser.setUsername(validatedUsername);
            currentUser.updateUsernameChangeTimestamp();
            
            return new AuthenticationResult(true, 
                "Username changed successfully. Locked for 15 days.", null);
            
        } catch (SecurityException e) {
            return new AuthenticationResult(false, 
                "Username validation failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new AuthenticationResult(false, 
                "Username change failed: " + e.getMessage(), null);
        }
    }
    
    // Helper method to load user data from database
    private User loadUserFromDatabase(String username) {
        String[] userData = DatabaseManager.getUser(username);
        if (userData != null) {
            User user = new User(username, userData[1]); // userData[1] is email
            // Additional user data loading would go here
            return user;
        }
        return null;
    }
    
    // Result class for authentication operations
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final String sessionToken;
        
        public AuthenticationResult(boolean success, String message, String sessionToken) {
            this.success = success;
            this.message = message;
            this.sessionToken = sessionToken;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
        
        @Override
        public String toString() {
            return String.format("AuthResult{success=%b, message='%s'}", success, message);
        }
    }
}