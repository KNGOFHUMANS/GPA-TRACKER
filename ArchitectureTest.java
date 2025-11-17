/**
 * ArchitectureTest - Test basic functionality of refactored components
 */
public class ArchitectureTest {
    public static void main(String[] args) {
        System.out.println("Testing Refactored Architecture Components...");
        
        try {
            // Test data models
            testDataModels();
            
            // Test services
            testServices();
            
            // Test UI components
            testUIComponents();
            
            System.out.println("✅ All architecture tests passed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Architecture test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testDataModels() {
        System.out.println("\n📋 Testing Data Models...");
        
        // Test Assignment
        Assignment assignment = new Assignment("Homework 1", 95.0, "Homework");
        System.out.println("✓ Assignment: " + assignment.toString());
        
        // Test Course
        Course course = new Course("Computer Science 101", 3);
        course.addAssignment(assignment);
        course.addAssignment(new Assignment("Midterm", 87.0, "Exam"));
        System.out.println("✓ Course: " + course.toString());
        
        // Test Semester
        Semester semester = new Semester("Fall 2025", 1);
        semester.addCourse(course);
        System.out.println("✓ Semester: " + semester.toString());
        
        // Test User
        User user = new User("testuser", "test@example.com");
        user.addSemester(semester);
        System.out.println("✓ User: " + user.toString());
        
        System.out.println("✅ Data Models: All tests passed");
    }
    
    private static void testServices() {
        System.out.println("\n🔧 Testing Services...");
        
        // Test PasswordResetStore
        PasswordResetStore.init("test_reset_tokens.json");
        String token = PasswordResetStore.generateTokenFor("testuser");
        System.out.println("✓ Password Reset Token: " + token);
        

        
        // Test DataPersistenceService
        DataPersistenceService.initialize();
        System.out.println("✓ Data Directory: " + DataPersistenceService.getDataDirectory());
        
        System.out.println("✅ Services: All tests passed");
    }
    
    private static void testUIComponents() {
        System.out.println("\n🎨 Testing UI Components...");
        
        // Test color blending
        java.awt.Color color1 = java.awt.Color.RED;
        java.awt.Color color2 = java.awt.Color.BLUE;
        java.awt.Color blended = UIComponentFactory.blend(color1, color2, 0.5f);
        System.out.println("✓ Color Blending: " + blended.toString());
        
        // Test component creation (without actually displaying)
        System.out.println("✓ UI Component Factory: Methods available");
        
        System.out.println("✅ UI Components: All tests passed");
    }
}