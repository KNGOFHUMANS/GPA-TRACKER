import java.util.*;

/**
 * Course - Represents a single course/class with assignments and grading
 * Part of the refactored architecture for CollegeGPATracker
 */
public class Course {
    private String name;
    private int credits;
    private Map<String, List<Assignment>> assignments;
    private Map<String, Integer> categoryWeights;
    private List<Double> gradeHistory;
    
    // Default category weights
    private static final Map<String, Integer> DEFAULT_WEIGHTS = Map.of(
        "Homework", 40,
        "Exam", 40,
        "Project", 20
    );
    
    // Constructors
    public Course() {
        this("", 3);
    }
    
    public Course(String name) {
        this(name, 3);
    }
    
    public Course(String name, int credits) {
        this.name = name != null ? name : "";
        this.credits = Math.max(0, credits);
        this.assignments = new HashMap<>();
        this.categoryWeights = new HashMap<>(DEFAULT_WEIGHTS);
        this.gradeHistory = new ArrayList<>();
        
        // Initialize assignment categories
        for (String category : DEFAULT_WEIGHTS.keySet()) {
            this.assignments.put(category, new ArrayList<>());
        }
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        this.credits = Math.max(0, credits);
    }
    
    public Map<String, List<Assignment>> getAssignments() {
        return new HashMap<>(assignments);
    }
    
    public Map<String, Integer> getCategoryWeights() {
        return new HashMap<>(categoryWeights);
    }
    
    public void setCategoryWeights(Map<String, Integer> weights) {
        if (weights != null) {
            this.categoryWeights = new HashMap<>(weights);
        }
    }
    
    public List<Double> getGradeHistory() {
        return new ArrayList<>(gradeHistory);
    }
    
    // Assignment management
    public void addAssignment(Assignment assignment) {
        if (assignment != null) {
            String category = assignment.getCategory();
            assignments.computeIfAbsent(category, k -> new ArrayList<>()).add(assignment);
        }
    }
    
    public void removeAssignment(String category, String assignmentName) {
        List<Assignment> categoryAssignments = assignments.get(category);
        if (categoryAssignments != null) {
            categoryAssignments.removeIf(a -> a.getName().equals(assignmentName));
        }
    }
    
    public List<Assignment> getAssignmentsByCategory(String category) {
        return assignments.getOrDefault(category, new ArrayList<>());
    }
    
    public void addCategory(String category, int weight) {
        if (category != null && !category.trim().isEmpty()) {
            assignments.putIfAbsent(category, new ArrayList<>());
            categoryWeights.put(category, Math.max(0, weight));
        }
    }
    
    public void removeCategory(String category) {
        assignments.remove(category);
        categoryWeights.remove(category);
    }
    
    // Grade calculations
    public double calculateCategoryAverage(String category) {
        List<Assignment> categoryAssignments = assignments.get(category);
        if (categoryAssignments == null || categoryAssignments.isEmpty()) {
            return 0.0;
        }
        
        double sum = categoryAssignments.stream()
            .mapToDouble(Assignment::getScore)
            .sum();
        return sum / categoryAssignments.size();
    }
    
    public double calculateOverallPercentage() {
        double totalWeight = categoryWeights.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        if (totalWeight == 0) return 0.0;
        
        double weightedSum = 0.0;
        for (Map.Entry<String, Integer> entry : categoryWeights.entrySet()) {
            String category = entry.getKey();
            int weight = entry.getValue();
            double categoryAvg = calculateCategoryAverage(category);
            weightedSum += (categoryAvg * weight / 100.0);
        }
        
        return (weightedSum * 100.0) / (totalWeight / 100.0);
    }
    
    public double calculateGPA() {
        double percentage = calculateOverallPercentage();
        return percentageToGPA(percentage);
    }
    
    private double percentageToGPA(double percentage) {
        if (percentage >= 90) return 4.0;
        else if (percentage >= 80) return 3.0;
        else if (percentage >= 70) return 2.0;
        else if (percentage >= 60) return 1.0;
        else return 0.0;
    }
    
    public char getLetterGrade() {
        double percentage = calculateOverallPercentage();
        if (percentage >= 90) return 'A';
        else if (percentage >= 80) return 'B';
        else if (percentage >= 70) return 'C';
        else if (percentage >= 60) return 'D';
        else return 'F';
    }
    
    // Grade history management
    public void addToHistory() {
        double currentPercentage = calculateOverallPercentage();
        gradeHistory.add(currentPercentage);
    }
    
    public void clearHistory() {
        gradeHistory.clear();
    }
    
    // Analytics
    public boolean isImproving() {
        if (gradeHistory.size() < 2) return false;
        double recent = gradeHistory.get(gradeHistory.size() - 1);
        double previous = gradeHistory.get(gradeHistory.size() - 2);
        return recent > previous + 5.0; // 5% improvement threshold
    }
    
    public boolean isPassing() {
        return calculateOverallPercentage() >= 60.0;
    }
    
    public boolean isHighPerforming() {
        return calculateGPA() >= 3.8;
    }
    
    // Utility methods
    public int getTotalAssignments() {
        return assignments.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d credits) - %.1f%% (%.2f GPA)", 
            name, credits, calculateOverallPercentage(), calculateGPA());
    }
    
    // Enhanced methods for grade management compatibility
    
    /**
     * Get all assignments as a flat list (for analytics compatibility)
     */
    public java.util.List<Assignment> getAllAssignments() {
        java.util.List<Assignment> allAssignments = new java.util.ArrayList<>();
        for (java.util.List<Assignment> categoryAssignments : assignments.values()) {
            allAssignments.addAll(categoryAssignments);
        }
        return allAssignments;
    }
    
    /**
     * Calculate current grade percentage
     */
    public double calculateCurrentGrade() {
        return calculateOverallPercentage();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return credits == course.credits && name.equals(course.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() + Integer.hashCode(credits);
    }
}