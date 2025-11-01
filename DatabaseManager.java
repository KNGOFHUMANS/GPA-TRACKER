import java.sql.*;
import java.util.*;
import java.io.File;

/**
 * DatabaseManager - Handles SQLite database operations for GradeRise
 * Replaces JSON file storage with proper database operations
 */
public class DatabaseManager {
    private static final String DATABASE_FILE = "data" + File.separator + "graderise.db";
    private static Connection connection;
    
    /**
     * Get the database connection for external use
     * @return database connection, or null if not initialized
     */
    public static Connection getConnection() {
        return connection;
    }
    
    // Initialize database connection and create tables
    public static void initialize() {
        try {
            // Create data directory if it doesn't exist
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to database (creates file if it doesn't exist)
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
            
            // Enable foreign keys
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            
            // Create tables
            createTables();
            
            System.out.println("Database initialized successfully at: " + DATABASE_FILE);
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTables() throws SQLException {
        // Users table
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                email TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_username_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        // Semesters table
        String createSemesters = """
            CREATE TABLE IF NOT EXISTS semesters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                display_order INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE(user_id, name)
            )
        """;
        
        // Courses table
        String createCourses = """
            CREATE TABLE IF NOT EXISTS courses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                semester_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                credits INTEGER DEFAULT 3,
                is_active BOOLEAN DEFAULT TRUE,
                final_grade REAL DEFAULT -1.0,
                letter_grade TEXT DEFAULT '',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE CASCADE
            )
        """;
        
        // Assignment categories table
        String createCategories = """
            CREATE TABLE IF NOT EXISTS assignment_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                course_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                weight INTEGER NOT NULL,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                UNIQUE(course_id, name)
            )
        """;
        
        // Assignments table
        String createAssignments = """
            CREATE TABLE IF NOT EXISTS assignments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                score REAL NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES assignment_categories(id) ON DELETE CASCADE
            )
        """;
        
        // Grade history table for trend tracking
        String createGradeHistory = """
            CREATE TABLE IF NOT EXISTS grade_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                course_id INTEGER NOT NULL,
                percentage REAL NOT NULL,
                recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
            )
        """;
        
        // Password reset tokens table
        String createResetTokens = """
            CREATE TABLE IF NOT EXISTS password_reset_tokens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                token TEXT NOT NULL UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        // User sessions table
        String createSessions = """
            CREATE TABLE IF NOT EXISTS user_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                session_token TEXT NOT NULL UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        // Execute all CREATE TABLE statements
        Statement stmt = connection.createStatement();
        stmt.execute(createUsers);
        stmt.execute(createSemesters);
        stmt.execute(createCourses);
        stmt.execute(createCategories);
        stmt.execute(createAssignments);
        stmt.execute(createGradeHistory);
        stmt.execute(createResetTokens);
        stmt.execute(createSessions);
        stmt.close();
    }
    
    // User management methods
    
    /**
     * Create a new user with bcrypt password hashing and input validation
     * @param username Username (will be validated)
     * @param plainPassword Plain text password (will be hashed with bcrypt)
     * @param email Email address (will be validated)
     * @return true if user created successfully, false otherwise
     */
    public static boolean createUserSecure(String username, String plainPassword, String email) {
        try {
            // Validate inputs using SecurityManager
            String validUsername = SecurityManager.validateUsername(username);
            String validEmail = SecurityManager.validateEmail(email);
            SecurityManager.validatePassword(plainPassword);
            
            // Hash password with bcrypt
            String passwordHash = SecurityManager.hashPassword(plainPassword);
            
            // Create user in database
            String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, validUsername);
                stmt.setString(2, passwordHash);
                stmt.setString(3, validEmail);
                stmt.executeUpdate();
                
                SecurityManager.logSecurityEvent("User created", validUsername, true);
                return true;
            }
        } catch (SecurityException e) {
            System.err.println("Security validation failed: " + e.getMessage());
            SecurityManager.logSecurityEvent("User creation failed - validation", username, false);
        } catch (SQLException e) {
            System.err.println("Database error creating user: " + e.getMessage());
            SecurityManager.logSecurityEvent("User creation failed - database", username, false);
        }
        return false;
    }
    
    /**
     * Legacy method for backward compatibility - still accepts pre-hashed passwords
     * @deprecated Use createUserSecure instead
     */
    @Deprecated
    public static boolean createUser(String username, String passwordHash, String email) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, email);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticate user with bcrypt password verification and rate limiting
     * @param usernameOrEmail Username or email to authenticate
     * @param plainPassword Plain text password
     * @param clientIdentifier Client identifier for rate limiting (IP address, etc.)
     * @return username if authentication successful, null otherwise
     */
    public static String authenticateUser(String usernameOrEmail, String plainPassword, String clientIdentifier) {
        // Check rate limiting first
        if (SecurityManager.isRateLimited(clientIdentifier)) {
            long remaining = SecurityManager.getRemainingLockoutSeconds(clientIdentifier);
            System.err.println("Authentication rate limited. Try again in " + remaining + " seconds.");
            SecurityManager.logSecurityEvent("Authentication blocked - rate limited", usernameOrEmail, false);
            return null;
        }
        
        try {
            // Find user by username or email
            String username = usernameOrEmail;
            if (usernameOrEmail.contains("@")) {
                username = findUserByEmail(usernameOrEmail);
                if (username == null) {
                    SecurityManager.recordFailedAttempt(clientIdentifier);
                    SecurityManager.logSecurityEvent("Authentication failed - user not found", usernameOrEmail, false);
                    return null;
                }
            }
            
            // Get user data
            String[] userData = getUser(username);
            if (userData == null) {
                SecurityManager.recordFailedAttempt(clientIdentifier);
                SecurityManager.logSecurityEvent("Authentication failed - user not found", username, false);
                return null;
            }
            
            String storedPasswordHash = userData[0];
            
            // Handle empty passwords (Google sign-in accounts)
            if (storedPasswordHash.isEmpty()) {
                SecurityManager.recordFailedAttempt(clientIdentifier);
                SecurityManager.logSecurityEvent("Authentication failed - Google account", username, false);
                return null; // Google accounts can't use password auth
            }
            
            // Verify password
            if (SecurityManager.verifyPassword(plainPassword, storedPasswordHash)) {
                // Check if password needs upgrade to bcrypt
                if (SecurityManager.needsPasswordUpgrade(storedPasswordHash)) {
                    // Upgrade password to bcrypt
                    upgradeUserPassword(username, plainPassword);
                    SecurityManager.logSecurityEvent("Password upgraded to bcrypt", username, true);
                }
                
                SecurityManager.clearFailedAttempts(clientIdentifier);
                SecurityManager.logSecurityEvent("Authentication successful", username, true);
                return username;
            } else {
                SecurityManager.recordFailedAttempt(clientIdentifier);
                SecurityManager.logSecurityEvent("Authentication failed - wrong password", username, false);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            SecurityManager.recordFailedAttempt(clientIdentifier);
            SecurityManager.logSecurityEvent("Authentication failed - error", usernameOrEmail, false);
            return null;
        }
    }
    
    /**
     * Upgrade a user's password to bcrypt hashing
     * @param username Username to upgrade
     * @param plainPassword Plain text password to hash and store
     */
    private static void upgradeUserPassword(String username, String plainPassword) {
        try {
            String newHash = SecurityManager.hashPassword(plainPassword);
            String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error upgrading password: " + e.getMessage());
        }
    }
    
    /**
     * Change user password with validation and bcrypt hashing
     * @param username Username
     * @param newPlainPassword New plain text password
     * @return true if password changed successfully
     */
    public static boolean changePassword(String username, String newPlainPassword) {
        try {
            SecurityManager.validatePassword(newPlainPassword);
            String newHash = SecurityManager.hashPassword(newPlainPassword);
            
            String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, username);
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    // Invalidate all sessions for security
                    SecurityManager.invalidateAllUserSessions(username);
                    SecurityManager.logSecurityEvent("Password changed", username, true);
                    return true;
                }
            }
        } catch (SecurityException e) {
            System.err.println("Password validation failed: " + e.getMessage());
            SecurityManager.logSecurityEvent("Password change failed - validation", username, false);
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            SecurityManager.logSecurityEvent("Password change failed - database", username, false);
        }
        return false;
    }
    
    /**
     * Find username by email address
     * @param email Email to search for
     * @return username if found, null otherwise
     */
    public static String findUserByEmail(String email) {
        String sql = "SELECT username FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }
    
    public static String[] getUser(String username) {
        String sql = "SELECT password_hash, email FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{rs.getString("password_hash"), rs.getString("email")};
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }
    
    public static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        return -1;
    }
    
    public static Map<String, String[]> getAllUsers() {
        Map<String, String[]> users = new HashMap<>();
        String sql = "SELECT username, password_hash, email FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.put(rs.getString("username"), 
                         new String[]{rs.getString("password_hash"), rs.getString("email")});
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }
    
    // Semester management
    public static boolean createSemester(int userId, String semesterName, int displayOrder) {
        String sql = "INSERT INTO semesters (user_id, name, display_order) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, semesterName);
            stmt.setInt(3, displayOrder);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating semester: " + e.getMessage());
            return false;
        }
    }
    
    public static int getSemesterId(int userId, String semesterName) {
        String sql = "SELECT id FROM semesters WHERE user_id = ? AND name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, semesterName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting semester ID: " + e.getMessage());
        }
        return -1;
    }
    
    // Course management
    public static boolean createCourse(int semesterId, String courseName, int credits) {
        String sql = "INSERT INTO courses (semester_id, name, credits) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, semesterId);
            stmt.setString(2, courseName);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
            
            // Create default assignment categories
            createDefaultCategories(getLastInsertId());
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating course: " + e.getMessage());
            return false;
        }
    }
    
    private static void createDefaultCategories(int courseId) {
        String sql = "INSERT INTO assignment_categories (course_id, name, weight) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Homework category (40%)
            stmt.setInt(1, courseId);
            stmt.setString(2, "Homework");
            stmt.setInt(3, 40);
            stmt.executeUpdate();
            
            // Exam category (40%)
            stmt.setInt(1, courseId);
            stmt.setString(2, "Exam");
            stmt.setInt(3, 40);
            stmt.executeUpdate();
            
            // Project category (20%)
            stmt.setInt(1, courseId);
            stmt.setString(2, "Project");
            stmt.setInt(3, 20);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating default categories: " + e.getMessage());
        }
    }
    
    private static int getLastInsertId() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting last insert ID: " + e.getMessage());
        }
        return -1;
    }
    
    // Assignment management
    public static boolean createAssignment(String courseName, String semesterName, String username,
                                         String assignmentName, double score, String category) {
        try {
            int userId = getUserId(username);
            if (userId == -1) return false;
            
            int semesterId = getSemesterId(userId, semesterName);
            if (semesterId == -1) return false;
            
            int courseId = getCourseId(semesterId, courseName);
            if (courseId == -1) return false;
            
            int categoryId = getCategoryId(courseId, category);
            if (categoryId == -1) return false;
            
            String sql = "INSERT INTO assignments (category_id, name, score) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                stmt.setString(2, assignmentName);
                stmt.setDouble(3, score);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating assignment: " + e.getMessage());
            return false;
        }
    }
    
    private static int getCourseId(int semesterId, String courseName) {
        String sql = "SELECT id FROM courses WHERE semester_id = ? AND name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, semesterId);
            stmt.setString(2, courseName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting course ID: " + e.getMessage());
        }
        return -1;
    }
    
    private static int getCategoryId(int courseId, String categoryName) {
        String sql = "SELECT id FROM assignment_categories WHERE course_id = ? AND name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setString(2, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting category ID: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Load user data from database - simplified version to avoid circular dependencies
     * Full implementation would require restructuring to avoid circular references
     * between DatabaseManager and CollegeGPATracker inner classes
     */
    public static Map<String, Object> loadUserDataRaw(String username) {
        // Returns empty map for now - database operations work through other methods
        // This approach avoids circular dependency issues while maintaining compatibility
        return new HashMap<>();
    }
    
    // Close database connection
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}