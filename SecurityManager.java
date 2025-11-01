import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * SecurityManager - Comprehensive security utilities for GradeRise application
 * 
 * Features:
 * - bcrypt password hashing with salt
 * - Input validation and sanitization
 * - Rate limiting for login attempts
 * - Session timeout management
 * - Secure random token generation
 * - SQL injection prevention
 */
public class SecurityManager {
    
    // ===== BCRYPT PASSWORD HASHING =====
    
    /** bcrypt work factor (higher = more secure but slower) */
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash a password using bcrypt with salt
     * @param plainTextPassword The plain text password to hash
     * @return bcrypt hashed password with salt
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verify a password against its bcrypt hash
     * @param plainTextPassword The plain text password to verify
     * @param hashedPassword The bcrypt hash to verify against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        
        // Handle legacy plain text passwords (migration support)
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$") && !hashedPassword.startsWith("$2y$")) {
            // This is a plain text password - check direct match and upgrade it
            if (plainTextPassword.equals(hashedPassword)) {
                // Password matches - should be upgraded to bcrypt
                System.out.println("WARNING: Plain text password detected - upgrade recommended");
                return true;
            }
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a password needs to be upgraded to bcrypt
     * @param storedPassword The stored password to check
     * @return true if upgrade is needed (plain text), false if already bcrypt
     */
    public static boolean needsPasswordUpgrade(String storedPassword) {
        return storedPassword != null && 
               !storedPassword.startsWith("$2a$") && 
               !storedPassword.startsWith("$2b$") && 
               !storedPassword.startsWith("$2y$");
    }
    
    // ===== INPUT VALIDATION AND SANITIZATION =====
    
    /** Regex patterns for input validation */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,128}$"); // At least 6 chars, max 128
    private static final Pattern COURSE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s&().,'-]{1,100}$");
    private static final Pattern SEMESTER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]{1,50}$");
    
    /**
     * Validate and sanitize username input
     * @param username Username to validate
     * @return sanitized username
     * @throws SecurityException if username is invalid
     */
    public static String validateUsername(String username) throws SecurityException {
        if (username == null) {
            throw new SecurityException("Username cannot be null");
        }
        
        String sanitized = sanitizeInput(username.trim());
        
        if (sanitized.isEmpty()) {
            throw new SecurityException("Username cannot be empty");
        }
        
        if (!USERNAME_PATTERN.matcher(sanitized).matches()) {
            throw new SecurityException("Username must be 3-30 characters and contain only letters, numbers, dots, hyphens, and underscores");
        }
        
        // Check for common injection patterns
        if (containsSqlKeywords(sanitized)) {
            throw new SecurityException("Username contains invalid characters");
        }
        
        return sanitized;
    }
    
    /**
     * Validate email address format
     * @param email Email to validate
     * @return sanitized email
     * @throws SecurityException if email is invalid
     */
    public static String validateEmail(String email) throws SecurityException {
        if (email == null) {
            throw new SecurityException("Email cannot be null");
        }
        
        String sanitized = sanitizeInput(email.trim().toLowerCase());
        
        if (sanitized.isEmpty()) {
            throw new SecurityException("Email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(sanitized).matches()) {
            throw new SecurityException("Invalid email format");
        }
        
        if (sanitized.length() > 254) { // RFC 5321 limit
            throw new SecurityException("Email address too long");
        }
        
        return sanitized;
    }
    
    /**
     * Validate password strength and format
     * @param password Password to validate
     * @throws SecurityException if password is invalid
     */
    public static void validatePassword(String password) throws SecurityException {
        if (password == null) {
            throw new SecurityException("Password cannot be null");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new SecurityException("Password must be 6-128 characters long");
        }
        
        // Check password strength
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        int strengthScore = 0;
        if (hasLower) strengthScore++;
        if (hasUpper) strengthScore++;
        if (hasDigit) strengthScore++;
        if (hasSpecial) strengthScore++;
        
        if (strengthScore < 2) {
            throw new SecurityException("Password must contain at least 2 of: lowercase, uppercase, numbers, special characters");
        }
        
        // Check for common weak passwords
        String lowerPass = password.toLowerCase();
        String[] commonPasswords = {"password", "123456", "qwerty", "abc123", "letmein", "admin"};
        for (String common : commonPasswords) {
            if (lowerPass.contains(common)) {
                throw new SecurityException("Password contains common weak patterns");
            }
        }
    }
    
    /**
     * Validate and sanitize course name
     * @param courseName Course name to validate
     * @return sanitized course name
     * @throws SecurityException if invalid
     */
    public static String validateCourseName(String courseName) throws SecurityException {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new SecurityException("Course name cannot be empty");
        }
        
        String sanitized = sanitizeInput(courseName.trim());
        
        if (!COURSE_NAME_PATTERN.matcher(sanitized).matches()) {
            throw new SecurityException("Course name contains invalid characters");
        }
        
        return sanitized;
    }
    
    /**
     * Validate and sanitize semester name
     * @param semesterName Semester name to validate
     * @return sanitized semester name
     * @throws SecurityException if invalid
     */
    public static String validateSemesterName(String semesterName) throws SecurityException {
        if (semesterName == null || semesterName.trim().isEmpty()) {
            throw new SecurityException("Semester name cannot be empty");
        }
        
        String sanitized = sanitizeInput(semesterName.trim());
        
        if (!SEMESTER_NAME_PATTERN.matcher(sanitized).matches()) {
            throw new SecurityException("Semester name contains invalid characters");
        }
        
        return sanitized;
    }
    
    /**
     * Basic input sanitization - removes dangerous characters
     * @param input Input to sanitize
     * @return sanitized input
     */
    private static String sanitizeInput(String input) {
        if (input == null) return "";
        
        // Remove null bytes and control characters
        String sanitized = input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // Remove HTML/script injection patterns
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Check if input contains SQL keywords (basic injection detection)
     * @param input Input to check
     * @return true if contains SQL keywords
     */
    private static boolean containsSqlKeywords(String input) {
        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {
            "select", "insert", "update", "delete", "drop", "create", "alter",
            "union", "join", "where", "order", "group", "having", "exec",
            "execute", "script", "xp_", "sp_", "--", "/*", "*/"
        };
        
        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    // ===== RATE LIMITING =====
    
    /** Rate limiting data structures */
    private static final Map<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 15 * 60 * 1000; // 15 minutes
    private static final long LOCKOUT_DURATION_MS = 30 * 60 * 1000; // 30 minutes
    
    /**
     * Check if login attempts are rate limited for given identifier
     * @param identifier IP address, username, or other identifier
     * @return true if rate limited, false if allowed
     */
    public static boolean isRateLimited(String identifier) {
        if (identifier == null) return true;
        
        long currentTime = System.currentTimeMillis();
        List<Long> attempts = loginAttempts.computeIfAbsent(identifier, k -> new ArrayList<>());
        
        synchronized (attempts) {
            // Remove old attempts outside the window
            attempts.removeIf(time -> currentTime - time > RATE_LIMIT_WINDOW_MS);
            
            // Check if too many attempts
            if (attempts.size() >= MAX_LOGIN_ATTEMPTS) {
                // Check if still in lockout period
                Long lastAttempt = attempts.get(attempts.size() - 1);
                return currentTime - lastAttempt < LOCKOUT_DURATION_MS;
            }
        }
        
        return false;
    }
    
    /**
     * Record a failed login attempt for rate limiting
     * @param identifier IP address, username, or other identifier
     */
    public static void recordFailedAttempt(String identifier) {
        if (identifier == null) return;
        
        long currentTime = System.currentTimeMillis();
        List<Long> attempts = loginAttempts.computeIfAbsent(identifier, k -> new ArrayList<>());
        
        synchronized (attempts) {
            attempts.add(currentTime);
            // Keep only recent attempts
            attempts.removeIf(time -> currentTime - time > RATE_LIMIT_WINDOW_MS);
        }
    }
    
    /**
     * Clear failed attempts for successful login
     * @param identifier IP address, username, or other identifier
     */
    public static void clearFailedAttempts(String identifier) {
        if (identifier != null) {
            loginAttempts.remove(identifier);
        }
    }
    
    /**
     * Get remaining lockout time in seconds
     * @param identifier IP address, username, or other identifier
     * @return seconds remaining in lockout, 0 if not locked out
     */
    public static long getRemainingLockoutSeconds(String identifier) {
        if (identifier == null) return 0;
        
        List<Long> attempts = loginAttempts.get(identifier);
        if (attempts == null || attempts.isEmpty()) return 0;
        
        synchronized (attempts) {
            if (attempts.size() >= MAX_LOGIN_ATTEMPTS) {
                long lastAttempt = attempts.get(attempts.size() - 1);
                long timeSince = System.currentTimeMillis() - lastAttempt;
                long remaining = LOCKOUT_DURATION_MS - timeSince;
                return remaining > 0 ? remaining / 1000 : 0;
            }
        }
        
        return 0;
    }
    
    // ===== SESSION MANAGEMENT =====
    
    /** Session timeout in milliseconds (30 minutes) */
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;
    
    /**
     * Generate a secure random session token
     * @return secure random token string
     */
    public static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        random.nextBytes(tokenBytes);
        
        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : tokenBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Create a new user session in the database
     * @param username Username for the session
     * @return session token, or null if failed
     */
    public static String createSession(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            String sessionToken = generateSecureToken();
            long expiresAt = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
            
            // Store session in database - first get user ID
            int userId = DatabaseManager.getUserId(username);
            if (userId == -1) {
                return null; // User not found
            }
            
            String sql = "INSERT INTO user_sessions (user_id, session_token, expires_at) VALUES (?, ?, datetime(?, 'unixepoch'))";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, sessionToken);
                    stmt.setLong(3, expiresAt / 1000); // SQLite uses seconds
                    stmt.executeUpdate();
                    return sessionToken;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Validate a session token and return username if valid
     * @param sessionToken Session token to validate
     * @return username if session is valid, null otherwise
     */
    public static String validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return null;
        }
        
        try {
            String sql = "SELECT u.username FROM user_sessions s " +
                        "JOIN users u ON s.user_id = u.id " +
                        "WHERE s.session_token = ? AND s.expires_at > datetime('now')";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, sessionToken);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        // Session is valid - extend it
                        extendSession(sessionToken);
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating session: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extend session expiration time
     * @param sessionToken Session token to extend
     */
    public static void extendSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return;
        }
        
        try {
            long newExpiresAt = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
            String sql = "UPDATE user_sessions SET expires_at = datetime(?, 'unixepoch') " +
                        "WHERE session_token = ?";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, newExpiresAt / 1000);
                    stmt.setString(2, sessionToken);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error extending session: " + e.getMessage());
        }
    }
    
    /**
     * Invalidate a session (logout)
     * @param sessionToken Session token to invalidate
     */
    public static void invalidateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return;
        }
        
        try {
            String sql = "DELETE FROM user_sessions WHERE session_token = ?";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, sessionToken);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error invalidating session: " + e.getMessage());
        }
    }
    
    /**
     * Clean up expired sessions from database
     */
    public static void cleanupExpiredSessions() {
        try {
            String sql = "DELETE FROM user_sessions WHERE expires_at < datetime('now')";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate(sql);
                    if (deleted > 0) {
                        System.out.println("Cleaned up " + deleted + " expired sessions");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cleaning up sessions: " + e.getMessage());
        }
    }
    
    /**
     * Invalidate all sessions for a user (force logout everywhere)
     * @param username Username whose sessions to invalidate
     */
    public static void invalidateAllUserSessions(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        try {
            String sql = "DELETE FROM user_sessions WHERE user_id = (SELECT id FROM users WHERE username = ?)";
            
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error invalidating user sessions: " + e.getMessage());
        }
    }
    
    // ===== SECURE RANDOM TOKEN GENERATION =====
    
    /**
     * Generate a secure random password reset token
     * @return 6-digit numeric token
     */
    public static String generateResetToken() {
        SecureRandom random = new SecureRandom();
        int token = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(token);
    }
    
    /**
     * Generate a secure random API key
     * @return 32-character hex API key
     */
    public static String generateApiKey() {
        return generateSecureToken().substring(0, 32);
    }
    
    /**
     * Generate a cryptographically secure random string
     * @param length Length of the string to generate
     * @return secure random string using alphanumeric characters
     */
    public static String generateSecureRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }
    
    // ===== SECURITY UTILITY METHODS =====
    
    /**
     * Get security strength assessment for a password
     * @param password Password to assess
     * @return Security strength (WEAK, FAIR, GOOD, STRONG)
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) return "WEAK";
        
        int score = 0;
        
        // Length bonus
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        
        // No repeating characters
        if (!password.matches(".*(.)\\1{2,}.*")) score++;
        
        if (score <= 2) return "WEAK";
        if (score <= 4) return "FAIR";
        if (score <= 6) return "GOOD";
        return "STRONG";
    }
    
    /**
     * Log security event for audit trail
     * @param event Security event description
     * @param username Username involved (can be null)
     * @param success Whether the event was successful
     */
    public static void logSecurityEvent(String event, String username, boolean success) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = String.format("[%s] %s - User: %s - Success: %s", 
                                      timestamp, event, username != null ? username : "unknown", success);
        System.out.println("SECURITY LOG: " + logEntry);
        
        // In production, this should write to a secure audit log file
        // For now, we'll use console output
    }
}