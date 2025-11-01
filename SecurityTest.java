/**
 * SecurityTest - Comprehensive test suite for all security features
 * Tests bcrypt hashing, input validation, rate limiting, and session management
 */
public class SecurityTest {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== GradeRise Security Test Suite ===\n");
        
        try {
            // Initialize database for testing
            DatabaseManager.initialize();
            
            // Run all security tests
            testPasswordHashing();
            testInputValidation();
            testRateLimiting();
            testSessionManagement();
            Thread.sleep(100); // Small delay for timestamp uniqueness
            testUserAuthentication();
            testSecureTokenGeneration();
            testPasswordUpgrade();
            
            // Print results
            System.out.println("\n=== Test Results ===");
            System.out.println("✓ Tests Passed: " + testsPassed);
            System.out.println("✗ Tests Failed: " + testsFailed);
            System.out.println("Total Tests: " + (testsPassed + testsFailed));
            
            if (testsFailed == 0) {
                System.out.println("\n🎉 All security tests passed! The application is secure.");
            } else {
                System.out.println("\n⚠️  Some security tests failed. Please review the implementation.");
            }
            
        } catch (Exception e) {
            System.err.println("Test suite error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===== PASSWORD HASHING TESTS =====
    
    private static void testPasswordHashing() {
        System.out.println("--- Testing Password Hashing ---");
        
        // Test bcrypt hashing
        testCase("bcrypt password hashing", () -> {
            String password = "mySecurePassword123!";
            String hash = SecurityManager.hashPassword(password);
            
            // Check hash format
            if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
                throw new AssertionError("Hash doesn't have bcrypt format");
            }
            
            // Verify password
            if (!SecurityManager.verifyPassword(password, hash)) {
                throw new AssertionError("Password verification failed");
            }
            
            // Test wrong password
            if (SecurityManager.verifyPassword("wrongPassword", hash)) {
                throw new AssertionError("Wrong password was accepted");
            }
        });
        
        // Test password upgrade detection
        testCase("password upgrade detection", () -> {
            String plainTextPassword = "oldPlainTextPassword";
            String bcryptHash = SecurityManager.hashPassword("newBcryptPassword");
            
            if (!SecurityManager.needsPasswordUpgrade(plainTextPassword)) {
                throw new AssertionError("Plain text password should need upgrade");
            }
            
            if (SecurityManager.needsPasswordUpgrade(bcryptHash)) {
                throw new AssertionError("bcrypt hash should not need upgrade");
            }
        });
        
        // Test null/empty password handling
        testCase("null/empty password handling", () -> {
            try {
                SecurityManager.hashPassword(null);
                throw new AssertionError("Should throw exception for null password");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            try {
                SecurityManager.hashPassword("");
                throw new AssertionError("Should throw exception for empty password");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Verify should handle nulls gracefully
            if (SecurityManager.verifyPassword(null, "hash") || SecurityManager.verifyPassword("pass", null)) {
                throw new AssertionError("Verify should return false for null inputs");
            }
        });
    }
    
    // ===== INPUT VALIDATION TESTS =====
    
    private static void testInputValidation() {
        System.out.println("--- Testing Input Validation ---");
        
        // Test username validation
        testCase("username validation", () -> {
            // Valid usernames
            SecurityManager.validateUsername("validuser123");
            SecurityManager.validateUsername("user.name");
            SecurityManager.validateUsername("user-name");
            SecurityManager.validateUsername("user_name");
            
            // Invalid usernames
            assertThrows(() -> SecurityManager.validateUsername("ab")); // Too short
            assertThrows(() -> SecurityManager.validateUsername("ab#$%")); // Invalid chars
            assertThrows(() -> SecurityManager.validateUsername("select * from users")); // SQL injection
            assertThrows(() -> SecurityManager.validateUsername("")); // Empty
            assertThrows(() -> SecurityManager.validateUsername(null)); // Null
        });
        
        // Test email validation
        testCase("email validation", () -> {
            // Valid emails
            SecurityManager.validateEmail("user@example.com");
            SecurityManager.validateEmail("test.email+tag@domain.co.uk");
            SecurityManager.validateEmail("user123@test-domain.org");
            
            // Invalid emails
            assertThrows(() -> SecurityManager.validateEmail("notanemail"));
            assertThrows(() -> SecurityManager.validateEmail("@domain.com"));
            assertThrows(() -> SecurityManager.validateEmail("user@"));
            assertThrows(() -> SecurityManager.validateEmail(""));
            assertThrows(() -> SecurityManager.validateEmail(null));
        });
        
        // Test password validation
        testCase("password validation", () -> {
            // Valid passwords
            SecurityManager.validatePassword("StrongPass123!");
            SecurityManager.validatePassword("MyPass1");
            SecurityManager.validatePassword("simplepass");
            
            // Invalid passwords
            assertThrows(() -> SecurityManager.validatePassword("short")); // Too short
            assertThrows(() -> SecurityManager.validatePassword("password")); // Common weak
            assertThrows(() -> SecurityManager.validatePassword("123456")); // Common weak
            assertThrows(() -> SecurityManager.validatePassword("")); // Empty
            assertThrows(() -> SecurityManager.validatePassword(null)); // Null
        });
        
        // Test course name validation
        testCase("course name validation", () -> {
            SecurityManager.validateCourseName("Computer Science 101");
            SecurityManager.validateCourseName("Math & Statistics");
            SecurityManager.validateCourseName("English (Advanced)");
            
            assertThrows(() -> SecurityManager.validateCourseName("<script>alert('xss')</script>"));
            assertThrows(() -> SecurityManager.validateCourseName(""));
            assertThrows(() -> SecurityManager.validateCourseName(null));
        });
    }
    
    // ===== RATE LIMITING TESTS =====
    
    private static void testRateLimiting() {
        System.out.println("--- Testing Rate Limiting ---");
        
        testCase("rate limiting basic functionality", () -> {
            String testId = "test-client-1";
            
            // Should not be rate limited initially
            if (SecurityManager.isRateLimited(testId)) {
                throw new AssertionError("Should not be rate limited initially");
            }
            
            // Record failed attempts
            for (int i = 0; i < 5; i++) {
                SecurityManager.recordFailedAttempt(testId);
            }
            
            // Should be rate limited after max attempts
            if (!SecurityManager.isRateLimited(testId)) {
                throw new AssertionError("Should be rate limited after max attempts");
            }
            
            // Check remaining lockout time
            long remaining = SecurityManager.getRemainingLockoutSeconds(testId);
            if (remaining <= 0) {
                throw new AssertionError("Should have remaining lockout time");
            }
            
            // Clear attempts should remove rate limiting
            SecurityManager.clearFailedAttempts(testId);
            if (SecurityManager.isRateLimited(testId)) {
                throw new AssertionError("Should not be rate limited after clearing attempts");
            }
        });
        
        testCase("rate limiting with null identifier", () -> {
            // Should handle null gracefully
            if (!SecurityManager.isRateLimited(null)) {
                throw new AssertionError("Should be rate limited for null identifier");
            }
            
            SecurityManager.recordFailedAttempt(null); // Should not crash
            SecurityManager.clearFailedAttempts(null); // Should not crash
        });
    }
    
    // ===== SESSION MANAGEMENT TESTS =====
    
    private static void testSessionManagement() {
        System.out.println("--- Testing Session Management ---");
        
        testCase("session creation and validation", () -> {
            long timestamp = System.currentTimeMillis();
            String testUser = "sessionTestUser" + timestamp;
            String testEmail = "session" + timestamp + "@test.com";
            
            // Create test user first
            if (!DatabaseManager.createUserSecure(testUser, "TestPass123!", testEmail)) {
                throw new AssertionError("Failed to create test user");
            }
            
            // Create session
            String sessionToken = SecurityManager.createSession(testUser);
            if (sessionToken == null) {
                throw new AssertionError("Failed to create session");
            }
            
            // Validate session
            String validatedUser = SecurityManager.validateSession(sessionToken);
            if (!testUser.equals(validatedUser)) {
                throw new AssertionError("Session validation failed - got: " + validatedUser + ", expected: " + testUser);
            }
            
            // Invalidate session
            SecurityManager.invalidateSession(sessionToken);
            
            // Should no longer be valid
            if (SecurityManager.validateSession(sessionToken) != null) {
                throw new AssertionError("Session should be invalid after invalidation");
            }
        });
        
        testCase("session null handling", () -> {
            // Should handle nulls gracefully
            if (SecurityManager.createSession(null) != null) {
                throw new AssertionError("Should not create session for null user");
            }
            
            if (SecurityManager.validateSession(null) != null) {
                throw new AssertionError("Should not validate null session");
            }
            
            SecurityManager.invalidateSession(null); // Should not crash
        });
    }
    
    // ===== USER AUTHENTICATION TESTS =====
    
    private static void testUserAuthentication() {
        System.out.println("--- Testing User Authentication ---");
        
        testCase("secure user creation and authentication", () -> {
            long timestamp = System.currentTimeMillis();
            String testUser = "authTestUser" + timestamp;
            String testPassword = "AuthPass123!";
            String testEmail = "auth" + timestamp + "@test.com";
            String clientId = "test-client-auth";
            
            // Create user with secure method
            if (!DatabaseManager.createUserSecure(testUser, testPassword, testEmail)) {
                throw new AssertionError("Failed to create user securely");
            }
            
            // Authenticate with correct password
            String authUser = DatabaseManager.authenticateUser(testUser, testPassword, clientId);
            if (!testUser.equals(authUser)) {
                throw new AssertionError("Authentication failed with correct password");
            }
            
            // Authenticate with wrong password
            String failAuth = DatabaseManager.authenticateUser(testUser, "wrongPassword", clientId);
            if (failAuth != null) {
                throw new AssertionError("Authentication should fail with wrong password");
            }
            
            // Authenticate by email
            String emailAuth = DatabaseManager.authenticateUser(testEmail, testPassword, clientId);
            if (!testUser.equals(emailAuth)) {
                throw new AssertionError("Authentication by email failed");
            }
        });
        
        testCase("password change functionality", () -> {
            long timestamp = System.currentTimeMillis();
            String testUser = "passChangeUser" + timestamp;
            String oldPassword = "OldPass123!";
            String newPassword = "NewPass456!";
            String testEmail = "passchange" + timestamp + "@test.com";
            
            // Create user
            if (!DatabaseManager.createUserSecure(testUser, oldPassword, testEmail)) {
                throw new AssertionError("Failed to create user for password change test");
            }
            
            // Change password
            if (!DatabaseManager.changePassword(testUser, newPassword)) {
                throw new AssertionError("Failed to change password");
            }
            
            // Old password should not work
            if (DatabaseManager.authenticateUser(testUser, oldPassword, "test-client") != null) {
                throw new AssertionError("Old password should not work after change");
            }
            
            // New password should work
            if (DatabaseManager.authenticateUser(testUser, newPassword, "test-client") == null) {
                throw new AssertionError("New password should work after change");
            }
        });
    }
    
    // ===== SECURE TOKEN GENERATION TESTS =====
    
    private static void testSecureTokenGeneration() {
        System.out.println("--- Testing Secure Token Generation ---");
        
        testCase("secure token generation", () -> {
            // Test session token generation
            String token1 = SecurityManager.generateSecureToken();
            String token2 = SecurityManager.generateSecureToken();
            
            if (token1.equals(token2)) {
                throw new AssertionError("Tokens should be unique");
            }
            
            if (token1.length() != 64) { // 32 bytes = 64 hex chars
                throw new AssertionError("Token should be 64 characters long");
            }
            
            // Test reset token generation
            String resetToken1 = SecurityManager.generateResetToken();
            String resetToken2 = SecurityManager.generateResetToken();
            
            if (resetToken1.equals(resetToken2)) {
                throw new AssertionError("Reset tokens should be unique");
            }
            
            if (resetToken1.length() != 6) {
                throw new AssertionError("Reset token should be 6 digits");
            }
            
            // Test secure random string
            String randomStr = SecurityManager.generateSecureRandomString(16);
            if (randomStr.length() != 16) {
                throw new AssertionError("Random string should be requested length");
            }
        });
    }
    
    // ===== PASSWORD UPGRADE TESTS =====
    
    private static void testPasswordUpgrade() {
        System.out.println("--- Testing Password Upgrade ---");
        
        testCase("automatic password upgrade", () -> {
            long timestamp = System.currentTimeMillis();
            String testUser = "upgradeTestUser" + timestamp;
            String plainPassword = "PlainPassword123!";
            String testEmail = "upgrade" + timestamp + "@test.com";
            
            // Create user with legacy method (plain text password)
            @SuppressWarnings("deprecation")
            boolean created = DatabaseManager.createUser(testUser, plainPassword, testEmail);
            if (!created) {
                throw new AssertionError("Failed to create legacy user");
            }
            
            // Authenticate - this should trigger upgrade
            String authResult = DatabaseManager.authenticateUser(testUser, plainPassword, "upgrade-client");
            if (!testUser.equals(authResult)) {
                throw new AssertionError("Authentication failed during upgrade test");
            }
            
            // Get user data to check if password was upgraded
            String[] userData = DatabaseManager.getUser(testUser);
            if (userData == null) {
                throw new AssertionError("Failed to get user data after upgrade");
            }
            
            String storedHash = userData[0];
            if (SecurityManager.needsPasswordUpgrade(storedHash)) {
                throw new AssertionError("Password should have been upgraded to bcrypt");
            }
            
            // Verify the upgraded password still works
            if (!SecurityManager.verifyPassword(plainPassword, storedHash)) {
                throw new AssertionError("Upgraded password should still work");
            }
        });
    }
    
    // ===== UTILITY METHODS =====
    
    private static void testCase(String testName, Runnable test) {
        try {
            test.run();
            System.out.println("✓ " + testName);
            testsPassed++;
        } catch (AssertionError e) {
            System.out.println("✗ " + testName + " - " + e.getMessage());
            e.printStackTrace(); // Print stack trace for assertion errors
            testsFailed++;
        } catch (Exception e) {
            System.out.println("✗ " + testName + " - " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void assertThrows(Runnable code) {
        try {
            code.run();
            throw new AssertionError("Expected exception was not thrown");
        } catch (SecurityException e) {
            // Expected - security validation should throw SecurityException
        } catch (AssertionError e) {
            throw e; // Re-throw assertion errors
        } catch (Exception e) {
            // Other exceptions are also acceptable for validation failures
        }
    }
}