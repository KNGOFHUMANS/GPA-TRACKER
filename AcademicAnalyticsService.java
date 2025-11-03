import java.util.*;
import java.util.stream.Collectors;

/**
 * AcademicAnalyticsService - Provides GPA calculations and academic analytics
 * Part of the refactored architecture for CollegeGPATracker
 */
public class AcademicAnalyticsService {
    
    // ===== GPA CALCULATIONS =====
    
    /**
     * Calculate GPA from percentage grade
     */
    public static double percentageToGPA(double percentage) {
        if (percentage >= 90) return 4.0;
        else if (percentage >= 80) return 3.0;
        else if (percentage >= 70) return 2.0;
        else if (percentage >= 60) return 1.0;
        else return 0.0;
    }
    
    /**
     * Calculate percentage from GPA (reverse calculation)
     */
    public static double gpaToPercentage(double gpa) {
        if (gpa >= 4.0) return 95.0;  // A grade
        else if (gpa >= 3.0) return 85.0;  // B grade
        else if (gpa >= 2.0) return 75.0;  // C grade
        else if (gpa >= 1.0) return 65.0;  // D grade
        else return 50.0;  // F grade
    }
    
    /**
     * Get letter grade from percentage
     */
    public static char percentageToLetterGrade(double percentage) {
        if (percentage >= 90) return 'A';
        else if (percentage >= 80) return 'B';
        else if (percentage >= 70) return 'C';
        else if (percentage >= 60) return 'D';
        else return 'F';
    }
    
    /**
     * Get letter grade from GPA
     */
    public static char gpaToLetterGrade(double gpa) {
        if (gpa >= 4.0) return 'A';
        else if (gpa >= 3.0) return 'B';
        else if (gpa >= 2.0) return 'C';
        else if (gpa >= 1.0) return 'D';
        else return 'F';
    }
    
    // ===== COURSE ANALYTICS =====
    
    /**
     * Calculate average grade for a specific assignment category
     */
    public static double calculateCategoryAverage(Course course, String category) {
        return course.calculateCategoryAverage(category);
    }
    
    /**
     * Get course performance status
     */
    public static CoursePerformanceStatus getCoursePerformanceStatus(Course course) {
        double gpa = course.calculateGPA();
        double percentage = course.calculateOverallPercentage();
        boolean improving = course.isImproving();
        
        if (gpa >= 3.8) {
            return improving ? CoursePerformanceStatus.EXCELLENT_IMPROVING : CoursePerformanceStatus.EXCELLENT;
        } else if (gpa >= 3.0) {
            return improving ? CoursePerformanceStatus.GOOD_IMPROVING : CoursePerformanceStatus.GOOD;
        } else if (gpa >= 2.0) {
            return improving ? CoursePerformanceStatus.AVERAGE_IMPROVING : CoursePerformanceStatus.AVERAGE;
        } else if (percentage >= 60) {
            return improving ? CoursePerformanceStatus.PASSING_IMPROVING : CoursePerformanceStatus.PASSING;
        } else {
            return CoursePerformanceStatus.FAILING;
        }
    }
    
    /**
     * Calculate what grade is needed on next assignment to reach target GPA
     */
    public static double calculateRequiredGrade(Course course, String category, double targetGPA) {
        double targetPercentage = gpaToPercentage(targetGPA);
        
        Map<String, Integer> weights = course.getCategoryWeights();
        int categoryWeight = weights.getOrDefault(category, 0);
        
        if (categoryWeight == 0) {
            return -1; // Category doesn't exist or has no weight
        }
        
        double categoryContribution = (double) categoryWeight / 100.0;
        double currentCategoryAvg = course.calculateCategoryAverage(category);
        List<Assignment> assignments = course.getAssignmentsByCategory(category);
        int currentAssignments = assignments.size();
        
        // Calculate required category average to reach target
        double otherCategoriesContribution = 0.0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            if (!entry.getKey().equals(category)) {
                double catAvg = course.calculateCategoryAverage(entry.getKey());
                otherCategoriesContribution += catAvg * (entry.getValue() / 100.0);
            }
        }
        
        double requiredCategoryAverage = (targetPercentage - otherCategoriesContribution) / categoryContribution;
        
        // Calculate what single assignment score would achieve this average
        double currentTotal = currentCategoryAvg * currentAssignments;
        double requiredScore = requiredCategoryAverage * (currentAssignments + 1) - currentTotal;
        
        return Math.max(0, Math.min(100, requiredScore)); // Clamp to 0-100
    }
    
    // ===== SEMESTER ANALYTICS =====
    
    /**
     * Get semester performance summary
     */
    public static SemesterSummary getSemesterSummary(Semester semester) {
        double gpa = semester.calculateSemesterGPA();
        int totalCredits = semester.getTotalCredits();
        int courseCount = semester.getCourseCount();
        int passingCourses = semester.getPassingCourseCount();
        int highPerformingCourses = semester.getHighPerformingCourseCount();
        
        Course bestCourse = semester.getHighestGradeCourse();
        Course worstCourse = semester.getLowestGradeCourse();
        
        return new SemesterSummary(
            semester.getName(),
            gpa,
            totalCredits,
            courseCount,
            passingCourses,
            highPerformingCourses,
            bestCourse != null ? bestCourse.getName() : "N/A",
            worstCourse != null ? worstCourse.getName() : "N/A"
        );
    }
    
    /**
     * Calculate semester workload (total assignments)
     */
    public static int calculateSemesterWorkload(Semester semester) {
        return semester.getAllCourses().stream()
            .mapToInt(Course::getTotalAssignments)
            .sum();
    }
    
    // ===== USER ANALYTICS =====
    
    /**
     * Get comprehensive academic report for user
     */
    public static AcademicReport generateAcademicReport(User user) {
        double overallGPA = user.calculateOverallGPA();
        int totalCredits = user.getTotalCredits();
        int totalCourses = user.getTotalCourses();
        
        List<SemesterSummary> semesterSummaries = user.getAllSemesters().stream()
            .filter(semester -> !semester.isEmpty())
            .map(AcademicAnalyticsService::getSemesterSummary)
            .collect(Collectors.toList());
        
        Map<Character, Integer> gradeDistribution = user.getOverallGradeDistribution();
        
        // Calculate trends
        List<Double> semesterGPAs = user.getAllSemesters().stream()
            .filter(semester -> !semester.isEmpty())
            .map(Semester::calculateSemesterGPA)
            .collect(Collectors.toList());
        
        TrendAnalysis trendAnalysis = analyzeTrend(semesterGPAs);
        
        return new AcademicReport(
            user.getUsername(),
            overallGPA,
            totalCredits,
            totalCourses,
            semesterSummaries,
            gradeDistribution,
            trendAnalysis,
            generateRecommendations(user)
        );
    }
    
    /**
     * Analyze GPA trend over semesters
     */
    public static TrendAnalysis analyzeTrend(List<Double> values) {
        if (values.size() < 2) {
            return new TrendAnalysis(TrendDirection.STABLE, 0.0, 0);
        }
        
        // Simple linear trend analysis
        double sum = 0.0;
        for (int i = 1; i < values.size(); i++) {
            sum += values.get(i) - values.get(i - 1);
        }
        
        double averageChange = sum / (values.size() - 1);
        int consecutiveTrend = calculateConsecutiveTrend(values);
        
        TrendDirection direction;
        if (averageChange > 0.1) {
            direction = TrendDirection.IMPROVING;
        } else if (averageChange < -0.1) {
            direction = TrendDirection.DECLINING;
        } else {
            direction = TrendDirection.STABLE;
        }
        
        return new TrendAnalysis(direction, averageChange, consecutiveTrend);
    }
    
    private static int calculateConsecutiveTrend(List<Double> values) {
        if (values.size() < 2) return 0;
        
        int consecutive = 0;
        boolean lastWasUp = values.get(1) > values.get(0);
        int currentStreak = 1;
        
        for (int i = 2; i < values.size(); i++) {
            boolean isUp = values.get(i) > values.get(i - 1);
            if (isUp == lastWasUp) {
                currentStreak++;
            } else {
                consecutive = Math.max(consecutive, currentStreak);
                currentStreak = 1;
                lastWasUp = isUp;
            }
        }
        
        return Math.max(consecutive, currentStreak);
    }
    
    /**
     * Generate personalized recommendations
     */
    public static List<String> generateRecommendations(User user) {
        List<String> recommendations = new ArrayList<>();
        
        double overallGPA = user.calculateOverallGPA();
        
        // GPA-based recommendations
        if (overallGPA < 2.0) {
            recommendations.add("Focus on bringing failing grades up to passing (60%+)");
            recommendations.add("Consider reducing course load to focus on current classes");
            recommendations.add("Seek academic support or tutoring for struggling subjects");
        } else if (overallGPA < 3.0) {
            recommendations.add("Aim to improve assignment scores in lower-performing categories");
            recommendations.add("Consider forming study groups for challenging subjects");
        } else if (overallGPA >= 3.5) {
            recommendations.add("Excellent work! Consider taking more challenging courses");
            recommendations.add("You're eligible for Dean's List recognition");
        }
        
        // Course-specific recommendations
        for (Semester semester : user.getAllSemesters()) {
            for (Course course : semester.getAllCourses()) {
                if (course.calculateGPA() < 2.0) {
                    recommendations.add("Focus extra attention on " + course.getName());
                }
                
                if (course.isImproving()) {
                    recommendations.add("Great improvement in " + course.getName() + " - keep it up!");
                }
            }
        }
        
        // Credit recommendations
        int totalCredits = user.getTotalCredits();
        if (totalCredits > 0) {
            int completedSemesters = user.getCompletedSemesters();
            if (completedSemesters > 0) {
                double avgCreditsPerSemester = (double) totalCredits / completedSemesters;
                if (avgCreditsPerSemester < 12) {
                    recommendations.add("Consider taking more credits per semester to maintain full-time status");
                }
            }
        }
        
        return recommendations;
    }
    
    // ===== COMPARISON AND BENCHMARKING =====
    
    /**
     * Compare course performance across semesters
     */
    public static Map<String, Double> compareCoursesAcrossSemesters(User user, String courseName) {
        Map<String, Double> semesterPerformance = new HashMap<>();
        
        for (Map.Entry<String, Semester> entry : user.getSemesters().entrySet()) {
            String semesterName = entry.getKey();
            Semester semester = entry.getValue();
            Course course = semester.getCourse(courseName);
            
            if (course != null) {
                semesterPerformance.put(semesterName, course.calculateOverallPercentage());
            }
        }
        
        return semesterPerformance;
    }
    
    /**
     * Get academic achievement badges
     */
    public static List<AchievementBadge> getAchievementBadges(User user) {
        List<AchievementBadge> badges = new ArrayList<>();
        
        double overallGPA = user.calculateOverallGPA();
        
        if (overallGPA >= 4.0) {
            badges.add(new AchievementBadge("Perfect Scholar", "Achieved 4.0 GPA", "🎓"));
        } else if (overallGPA >= 3.8) {
            badges.add(new AchievementBadge("Summa Cum Laude", "GPA 3.8+", "🌟"));
        } else if (overallGPA >= 3.5) {
            badges.add(new AchievementBadge("Magna Cum Laude", "GPA 3.5+", "⭐"));
        } else if (overallGPA >= 3.2) {
            badges.add(new AchievementBadge("Cum Laude", "GPA 3.2+", "✨"));
        }
        
        if (user.isDeansListEligible()) {
            badges.add(new AchievementBadge("Dean's List", "GPA 3.5+ with 12+ credits", "🏆"));
        }
        
        // Improvement badges
        List<Double> semesterGPAs = user.getAllSemesters().stream()
            .filter(semester -> !semester.isEmpty())
            .map(Semester::calculateSemesterGPA)
            .collect(Collectors.toList());
        
        TrendAnalysis trend = analyzeTrend(semesterGPAs);
        if (trend.getDirection() == TrendDirection.IMPROVING && trend.getConsecutiveTrend() >= 3) {
            badges.add(new AchievementBadge("Comeback Kid", "3+ semesters of improvement", "📈"));
        }
        
        // Credit achievements
        int totalCredits = user.getTotalCredits();
        if (totalCredits >= 120) {
            badges.add(new AchievementBadge("Graduation Ready", "120+ credits completed", "🎉"));
        } else if (totalCredits >= 90) {
            badges.add(new AchievementBadge("Senior Status", "90+ credits completed", "🎯"));
        } else if (totalCredits >= 60) {
            badges.add(new AchievementBadge("Junior Status", "60+ credits completed", "📚"));
        } else if (totalCredits >= 30) {
            badges.add(new AchievementBadge("Sophomore Status", "30+ credits completed", "📖"));
        }
        
        return badges;
    }
    
    // ===== DATA CLASSES =====
    
    public enum CoursePerformanceStatus {
        EXCELLENT, EXCELLENT_IMPROVING,
        GOOD, GOOD_IMPROVING,
        AVERAGE, AVERAGE_IMPROVING,
        PASSING, PASSING_IMPROVING,
        FAILING
    }
    
    public enum TrendDirection {
        IMPROVING, DECLINING, STABLE
    }
    
    public static class SemesterSummary {
        private final String name;
        private final double gpa;
        private final int totalCredits;
        private final int courseCount;
        private final int passingCourses;
        private final int highPerformingCourses;
        private final String bestCourse;
        private final String worstCourse;
        
        public SemesterSummary(String name, double gpa, int totalCredits, int courseCount,
                             int passingCourses, int highPerformingCourses, String bestCourse, String worstCourse) {
            this.name = name;
            this.gpa = gpa;
            this.totalCredits = totalCredits;
            this.courseCount = courseCount;
            this.passingCourses = passingCourses;
            this.highPerformingCourses = highPerformingCourses;
            this.bestCourse = bestCourse;
            this.worstCourse = worstCourse;
        }
        
        // Getters
        public String getName() { return name; }
        public double getGpa() { return gpa; }
        public int getTotalCredits() { return totalCredits; }
        public int getCourseCount() { return courseCount; }
        public int getPassingCourses() { return passingCourses; }
        public int getHighPerformingCourses() { return highPerformingCourses; }
        public String getBestCourse() { return bestCourse; }
        public String getWorstCourse() { return worstCourse; }
    }
    
    public static class TrendAnalysis {
        private final TrendDirection direction;
        private final double averageChange;
        private final int consecutiveTrend;
        
        public TrendAnalysis(TrendDirection direction, double averageChange, int consecutiveTrend) {
            this.direction = direction;
            this.averageChange = averageChange;
            this.consecutiveTrend = consecutiveTrend;
        }
        
        // Getters
        public TrendDirection getDirection() { return direction; }
        public double getAverageChange() { return averageChange; }
        public int getConsecutiveTrend() { return consecutiveTrend; }
    }
    
    public static class AcademicReport {
        private final String username;
        private final double overallGPA;
        private final int totalCredits;
        private final int totalCourses;
        private final List<SemesterSummary> semesterSummaries;
        private final Map<Character, Integer> gradeDistribution;
        private final TrendAnalysis trendAnalysis;
        private final List<String> recommendations;
        
        public AcademicReport(String username, double overallGPA, int totalCredits, int totalCourses,
                            List<SemesterSummary> semesterSummaries, Map<Character, Integer> gradeDistribution,
                            TrendAnalysis trendAnalysis, List<String> recommendations) {
            this.username = username;
            this.overallGPA = overallGPA;
            this.totalCredits = totalCredits;
            this.totalCourses = totalCourses;
            this.semesterSummaries = semesterSummaries;
            this.gradeDistribution = gradeDistribution;
            this.trendAnalysis = trendAnalysis;
            this.recommendations = recommendations;
        }
        
        // Getters
        public String getUsername() { return username; }
        public double getOverallGPA() { return overallGPA; }
        public int getTotalCredits() { return totalCredits; }
        public int getTotalCourses() { return totalCourses; }
        public List<SemesterSummary> getSemesterSummaries() { return semesterSummaries; }
        public Map<Character, Integer> getGradeDistribution() { return gradeDistribution; }
        public TrendAnalysis getTrendAnalysis() { return trendAnalysis; }
        public List<String> getRecommendations() { return recommendations; }
    }
    
    public static class AchievementBadge {
        private final String name;
        private final String description;
        private final String icon;
        
        public AchievementBadge(String name, String description, String icon) {
            this.name = name;
            this.description = description;
            this.icon = icon;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        
        @Override
        public String toString() {
            return String.format("%s %s - %s", icon, name, description);
        }
    }
}