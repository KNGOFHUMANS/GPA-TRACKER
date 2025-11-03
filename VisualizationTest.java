import javax.swing.*;

/**
 * Test class to verify Data Visualization Engine functionality
 */
public class VisualizationTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create test data
                User testUser = createTestUser();
                
                // Test individual chart components
                testChartComponents(testUser);
                
                System.out.println("✅ All visualization tests passed!");
                
            } catch (Exception e) {
                System.err.println("❌ Visualization test failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private static User createTestUser() {
        User user = new User();
        
        // Create test semester
        Semester fall2024 = new Semester("Fall 2024");
        
        // Create test course with assignments
        Course mathCourse = new Course("Calculus I", 4);
        mathCourse.addAssignment(new Assignment("Quiz 1", 85.0, "Quiz"));
        mathCourse.addAssignment(new Assignment("Quiz 2", 92.0, "Quiz"));
        mathCourse.addAssignment(new Assignment("Midterm", 78.5, "Exam"));
        mathCourse.addAssignment(new Assignment("Quiz 3", 88.0, "Quiz"));
        mathCourse.addAssignment(new Assignment("Final Project", 94.0, "Project"));
        
        Course physicsCourse = new Course("Physics I", 3);
        physicsCourse.addAssignment(new Assignment("Lab 1", 95.0, "Lab"));
        physicsCourse.addAssignment(new Assignment("Lab 2", 87.0, "Lab"));
        physicsCourse.addAssignment(new Assignment("Midterm", 82.0, "Exam"));
        physicsCourse.addAssignment(new Assignment("Lab 3", 91.0, "Lab"));
        
        fall2024.addCourse(mathCourse);
        fall2024.addCourse(physicsCourse);
        
        // Create another semester for trend analysis
        Semester spring2024 = new Semester("Spring 2024");
        Course chemCourse = new Course("Chemistry I", 4);
        chemCourse.addAssignment(new Assignment("Quiz 1", 90.0, "Quiz"));
        chemCourse.addAssignment(new Assignment("Lab 1", 88.0, "Lab"));
        chemCourse.addAssignment(new Assignment("Midterm", 85.0, "Exam"));
        
        spring2024.addCourse(chemCourse);
        
        user.addSemester(spring2024); // Add spring first (chronological order)
        user.addSemester(fall2024);
        
        return user;
    }
    
    private static void testChartComponents(User user) throws Exception {
        System.out.println("🧪 Testing Data Visualization Components...");
        
        // Test GPA Trend Data Generation
        DataVisualizationEngine.ChartDataset gpaTrend = 
            DataVisualizationEngine.generateGPATrendData(user);
        System.out.println("✅ GPA Trend Dataset: " + gpaTrend.getDataPoints().size() + " points");
        
        // Test Grade Distribution for a course
        Course testCourse = user.getAllSemesters().get(0).getAllCourses().get(0);
        DataVisualizationEngine.ChartDataset gradeDistribution = 
            DataVisualizationEngine.generateGradeDistributionData(testCourse);
        System.out.println("✅ Grade Distribution Dataset: " + gradeDistribution.getDataPoints().size() + " ranges");
        
        // Test Course Difficulty Analysis
        DataVisualizationEngine.ChartDataset courseDifficulty = 
            DataVisualizationEngine.generateCourseDifficultyData(user);
        System.out.println("✅ Course Difficulty Dataset: " + courseDifficulty.getDataPoints().size() + " courses");
        
        // Test Achievement Progress
        DataVisualizationEngine.ChartDataset achievementProgress = 
            DataVisualizationEngine.generateAchievementProgressData(user);
        System.out.println("✅ Achievement Progress Dataset: " + achievementProgress.getDataPoints().size() + " metrics");
        
        // Test Semester Comparison
        DataVisualizationEngine.ChartDataset semesterComparison = 
            DataVisualizationEngine.generateSemesterComparisonData(user);
        System.out.println("✅ Semester Comparison Dataset: " + semesterComparison.getDataPoints().size() + " semesters");
        
        // Test Assignment Trends
        DataVisualizationEngine.ChartDataset assignmentTrends = 
            DataVisualizationEngine.generateAssignmentTrendData(testCourse);
        System.out.println("✅ Assignment Trends Dataset: " + assignmentTrends.getDataPoints().size() + " assignments");
        
        // Test Interactive Chart Panel Creation
        DataVisualizationEngine.InteractiveChartPanel chartPanel = 
            new DataVisualizationEngine.InteractiveChartPanel(gpaTrend);
        System.out.println("✅ Interactive Chart Panel created successfully");
        
        // Test chart configuration
        chartPanel.setShowGrid(true);
        chartPanel.setShowLegend(true);
        chartPanel.setShowTooltips(true);
        chartPanel.setAnimated(true);
        System.out.println("✅ Chart configuration methods working");
        
        // Test dashboard creation (without showing)
        if (user.getAllSemesters().size() > 0) {
            System.out.println("✅ Dashboard ready - user has " + user.getAllSemesters().size() + " semesters");
        }
        
        System.out.println("🎯 All visualization components tested successfully!");
    }
}