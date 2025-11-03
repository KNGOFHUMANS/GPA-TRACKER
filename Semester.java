import java.util.*;

/**
 * Semester - Represents a semester containing multiple courses
 * Part of the refactored architecture for CollegeGPATracker
 */
public class Semester {
    private String name;
    private int order; // Display order (1, 2, 3, 4, etc.)
    private Map<String, Course> courses;
    
    // Constructors
    public Semester() {
        this("", 1);
    }
    
    public Semester(String name) {
        this(name, 1);
    }
    
    public Semester(String name, int order) {
        this.name = name != null ? name : "";
        this.order = Math.max(1, order);
        this.courses = new HashMap<>();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = Math.max(1, order);
    }
    
    public Map<String, Course> getCourses() {
        return new HashMap<>(courses);
    }
    
    // Course management
    public void addCourse(Course course) {
        if (course != null && !course.getName().trim().isEmpty()) {
            courses.put(course.getName(), course);
        }
    }
    
    public void removeCourse(String courseName) {
        courses.remove(courseName);
    }
    
    public Course getCourse(String courseName) {
        return courses.get(courseName);
    }
    
    public boolean hasCourse(String courseName) {
        return courses.containsKey(courseName);
    }
    
    public List<String> getCourseNames() {
        return new ArrayList<>(courses.keySet());
    }
    
    public List<Course> getAllCourses() {
        return new ArrayList<>(courses.values());
    }
    
    // GPA calculations
    public double calculateSemesterGPA() {
        if (courses.isEmpty()) return 0.0;
        
        double totalPoints = 0.0;
        int totalCredits = 0;
        
        for (Course course : courses.values()) {
            double courseGPA = course.calculateGPA();
            int courseCredits = course.getCredits();
            totalPoints += courseGPA * courseCredits;
            totalCredits += courseCredits;
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    
    public double calculateSemesterPercentage() {
        if (courses.isEmpty()) return 0.0;
        
        double totalWeightedPercentage = 0.0;
        int totalCredits = 0;
        
        for (Course course : courses.values()) {
            double coursePercentage = course.calculateOverallPercentage();
            int courseCredits = course.getCredits();
            totalWeightedPercentage += coursePercentage * courseCredits;
            totalCredits += courseCredits;
        }
        
        return totalCredits > 0 ? totalWeightedPercentage / totalCredits : 0.0;
    }
    
    public int getTotalCredits() {
        return courses.values().stream()
            .mapToInt(Course::getCredits)
            .sum();
    }
    
    // Analytics
    public int getPassingCourseCount() {
        return (int) courses.values().stream()
            .filter(Course::isPassing)
            .count();
    }
    
    public int getHighPerformingCourseCount() {
        return (int) courses.values().stream()
            .filter(Course::isHighPerforming)
            .count();
    }
    
    public int getImprovingCourseCount() {
        return (int) courses.values().stream()
            .filter(Course::isImproving)
            .count();
    }
    
    public boolean isFullTime() {
        return getTotalCredits() >= 12;
    }
    
    public boolean isSuccessful() {
        return calculateSemesterGPA() >= 2.0 && getPassingCourseCount() == courses.size();
    }
    
    // Statistics
    public Course getHighestGradeCourse() {
        return courses.values().stream()
            .max(Comparator.comparing(Course::calculateGPA))
            .orElse(null);
    }
    
    public Course getLowestGradeCourse() {
        return courses.values().stream()
            .min(Comparator.comparing(Course::calculateGPA))
            .orElse(null);
    }
    
    public Map<Character, Integer> getGradeDistribution() {
        Map<Character, Integer> distribution = new HashMap<>();
        distribution.put('A', 0);
        distribution.put('B', 0);
        distribution.put('C', 0);
        distribution.put('D', 0);
        distribution.put('F', 0);
        
        for (Course course : courses.values()) {
            char grade = course.getLetterGrade();
            distribution.put(grade, distribution.get(grade) + 1);
        }
        
        return distribution;
    }
    
    // Utility methods
    public boolean isEmpty() {
        return courses.isEmpty();
    }
    
    public int getCourseCount() {
        return courses.size();
    }
    
    public void clearAllCourses() {
        courses.clear();
    }
    
    @Override
    public String toString() {
        return String.format("%s - %d courses, %.2f GPA (%d credits)", 
            name, courses.size(), calculateSemesterGPA(), getTotalCredits());
    }
    
    // Enhanced methods for compatibility with analytics
    
    /**
     * Alias for calculateSemesterGPA for analytics compatibility
     */
    public double calculateGPA() {
        return calculateSemesterGPA();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Semester semester = (Semester) obj;
        return order == semester.order && name.equals(semester.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() + Integer.hashCode(order);
    }
}