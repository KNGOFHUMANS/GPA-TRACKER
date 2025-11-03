import java.util.*;

/**
 * User - Represents a user account with academic data and preferences
 * Part of the refactored architecture for CollegeGPATracker
 */
public class User {
    private String username;
    private String email;
    private Map<String, Semester> semesters;
    private Map<String, Integer> semesterOrder;
    private long lastUsernameChange;
    private boolean darkModeEnabled;
    
    // Constructors
    public User() {
        this("", "");
    }
    
    public User(String username, String email) {
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.semesters = new HashMap<>();
        this.semesterOrder = new HashMap<>();
        this.lastUsernameChange = System.currentTimeMillis();
        this.darkModeEnabled = false;
        
        // Initialize default semesters
        initializeDefaultSemesters();
    }
    
    private void initializeDefaultSemesters() {
        for (int i = 1; i <= 4; i++) {
            String semesterName = "Semester " + i;
            Semester semester = new Semester(semesterName, i);
            semesters.put(semesterName, semester);
            semesterOrder.put(semesterName, i);
        }
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }
    
    public Map<String, Semester> getSemesters() {
        return new HashMap<>(semesters);
    }
    
    public Map<String, Integer> getSemesterOrder() {
        return new HashMap<>(semesterOrder);
    }
    
    public long getLastUsernameChange() {
        return lastUsernameChange;
    }
    
    public void setLastUsernameChange(long timestamp) {
        this.lastUsernameChange = timestamp;
    }
    
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
    
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }
    
    // Semester management
    public void addSemester(Semester semester) {
        if (semester != null && !semester.getName().trim().isEmpty()) {
            semesters.put(semester.getName(), semester);
            semesterOrder.put(semester.getName(), semester.getOrder());
        }
    }
    
    public void removeSemester(String semesterName) {
        semesters.remove(semesterName);
        semesterOrder.remove(semesterName);
    }
    
    public Semester getSemester(String semesterName) {
        return semesters.get(semesterName);
    }
    
    public boolean hasSemester(String semesterName) {
        return semesters.containsKey(semesterName);
    }
    
    public List<String> getSemesterNames() {
        return semesters.keySet().stream()
            .sorted((a, b) -> Integer.compare(
                semesterOrder.getOrDefault(a, 0),
                semesterOrder.getOrDefault(b, 0)
            ))
            .toList();
    }
    
    public List<Semester> getAllSemesters() {
        return getSemesterNames().stream()
            .map(semesters::get)
            .toList();
    }
    
    public String getCurrentSemester() {
        return semesters.entrySet().stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .max(Comparator.comparing(entry -> semesterOrder.getOrDefault(entry.getKey(), 0)))
            .map(Map.Entry::getKey)
            .orElse("Semester 1");
    }
    
    // Academic calculations
    public double calculateOverallGPA() {
        double totalPoints = 0.0;
        int totalCredits = 0;
        
        for (Semester semester : semesters.values()) {
            for (Course course : semester.getAllCourses()) {
                double courseGPA = course.calculateGPA();
                int courseCredits = course.getCredits();
                totalPoints += courseGPA * courseCredits;
                totalCredits += courseCredits;
            }
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    
    public double calculateOverallPercentage() {
        double totalWeightedPercentage = 0.0;
        int totalCredits = 0;
        
        for (Semester semester : semesters.values()) {
            for (Course course : semester.getAllCourses()) {
                double coursePercentage = course.calculateOverallPercentage();
                int courseCredits = course.getCredits();
                totalWeightedPercentage += coursePercentage * courseCredits;
                totalCredits += courseCredits;
            }
        }
        
        return totalCredits > 0 ? totalWeightedPercentage / totalCredits : 0.0;
    }
    
    public int getTotalCredits() {
        return semesters.values().stream()
            .mapToInt(Semester::getTotalCredits)
            .sum();
    }
    
    public int getTotalCourses() {
        return semesters.values().stream()
            .mapToInt(Semester::getCourseCount)
            .sum();
    }
    
    // Analytics and achievements
    public boolean isAcademicStanding() {
        return calculateOverallGPA() >= 2.0;
    }
    
    public boolean isDeansListEligible() {
        return calculateOverallGPA() >= 3.5 && getTotalCredits() >= 12;
    }
    
    public boolean isHonorsEligible() {
        return calculateOverallGPA() >= 3.8;
    }
    
    public int getCompletedSemesters() {
        return (int) semesters.values().stream()
            .filter(semester -> !semester.isEmpty())
            .count();
    }
    
    public Map<Character, Integer> getOverallGradeDistribution() {
        Map<Character, Integer> distribution = new HashMap<>();
        distribution.put('A', 0);
        distribution.put('B', 0);
        distribution.put('C', 0);
        distribution.put('D', 0);
        distribution.put('F', 0);
        
        for (Semester semester : semesters.values()) {
            Map<Character, Integer> semesterDist = semester.getGradeDistribution();
            for (Map.Entry<Character, Integer> entry : semesterDist.entrySet()) {
                distribution.put(entry.getKey(), 
                    distribution.get(entry.getKey()) + entry.getValue());
            }
        }
        
        return distribution;
    }
    
    // Username change validation
    public boolean canChangeUsername() {
        long daysSinceChange = (System.currentTimeMillis() - lastUsernameChange) / (1000L * 60 * 60 * 24);
        return daysSinceChange >= 15;
    }
    
    public long getDaysUntilUsernameChange() {
        long daysSinceChange = (System.currentTimeMillis() - lastUsernameChange) / (1000L * 60 * 60 * 24);
        return Math.max(0, 15 - daysSinceChange);
    }
    
    public void updateUsernameChangeTimestamp() {
        this.lastUsernameChange = System.currentTimeMillis();
    }
    
    // Utility methods
    public boolean hasAcademicData() {
        return semesters.values().stream()
            .anyMatch(semester -> !semester.isEmpty());
    }
    
    public void clearAllAcademicData() {
        for (Semester semester : semesters.values()) {
            semester.clearAllCourses();
        }
    }
    
    @Override
    public String toString() {
        return String.format("User{username='%s', email='%s', GPA=%.2f, courses=%d}", 
            username, email, calculateOverallGPA(), getTotalCourses());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}