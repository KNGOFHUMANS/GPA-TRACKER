/**
 * Assignment - Represents a single assignment/grade in a course
 * Part of the refactored architecture for CollegeGPATracker
 */
public class Assignment {
    private String name;
    private double score;
    private String category;
    
    // Constructors
    public Assignment() {
        this("", 0.0, "");
    }
    
    public Assignment(String name, double score, String category) {
        this.name = name != null ? name : "";
        this.score = Math.max(0, Math.min(100, score)); // Clamp between 0-100
        this.category = category != null ? category : "";
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = Math.max(0, Math.min(100, score));
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category != null ? category : "";
    }
    
    // Enhanced methods for grade management compatibility
    
    /**
     * Get points earned (same as score for percentage-based assignments)
     */
    public double getPointsEarned() {
        return score;
    }
    
    /**
     * Get maximum points (100 for percentage-based assignments)
     */
    public double getMaxPoints() {
        return 100.0;
    }
    
    /**
     * Get grade as percentage (same as score)
     */
    public double getGradePercent() {
        return score;
    }
    
    /**
     * Get date added (using current date for compatibility)
     */
    public java.util.Date getDateAdded() {
        return new java.util.Date(); // Default to current date for existing assignments
    }
    
    // Utility methods
    public boolean isPassingGrade() {
        return score >= 60.0;
    }
    
    public char getLetterGrade() {
        if (score >= 90) return 'A';
        else if (score >= 80) return 'B';
        else if (score >= 70) return 'C';
        else if (score >= 60) return 'D';
        else return 'F';
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.1f%% (%s)", name, score, category);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Assignment that = (Assignment) obj;
        return Double.compare(that.score, score) == 0 &&
               name.equals(that.name) &&
               category.equals(that.category);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() + category.hashCode() + Double.hashCode(score);
    }
}