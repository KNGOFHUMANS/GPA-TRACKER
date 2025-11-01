/**
 * DatabaseTest - Simple test to verify SQLite database functionality
 */
public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing SQLite database integration...");
        
        try {
            // Initialize database
            DatabaseManager.initialize();
            System.out.println("✓ Database initialized successfully");
            
            // Test user creation (using secure method)
            String testUser = "testuser123";
            String testPassword = "SecureTestPass123!";
            String testEmail = "test@example.com";
            
            boolean created = DatabaseManager.createUserSecure(testUser, testPassword, testEmail);
            if (created) {
                System.out.println("✓ User created successfully with secure hashing");
            } else {
                System.out.println("User might already exist or creation failed");
            }
            
            // Test user retrieval
            String[] userData = DatabaseManager.getUser(testUser);
            if (userData != null) {
                System.out.println("✓ User retrieved: " + testUser + " with email: " + userData[1]);
            } else {
                System.out.println("✗ Failed to retrieve user");
            }
            
            // Test semester creation
            int userId = DatabaseManager.getUserId(testUser);
            if (userId != -1) {
                boolean semesterCreated = DatabaseManager.createSemester(userId, "Fall 2025", 1);
                if (semesterCreated) {
                    System.out.println("✓ Semester created successfully");
                } else {
                    System.out.println("✗ Failed to create semester");
                }
            }
            
            // Test course creation
            int semesterId = DatabaseManager.getSemesterId(userId, "Fall 2025");
            if (semesterId != -1) {
                boolean courseCreated = DatabaseManager.createCourse(semesterId, "Computer Science 101", 3);
                if (courseCreated) {
                    System.out.println("✓ Course created successfully");
                } else {
                    System.out.println("✗ Failed to create course");
                }
            }
            
            // Test assignment creation
            boolean assignmentCreated = DatabaseManager.createAssignment(
                "Computer Science 101", "Fall 2025", testUser, 
                "Homework 1", 95.5, "Homework"
            );
            if (assignmentCreated) {
                System.out.println("✓ Assignment created successfully");
            } else {
                System.out.println("✗ Failed to create assignment");
            }
            
            System.out.println("Database test completed!");
            
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}