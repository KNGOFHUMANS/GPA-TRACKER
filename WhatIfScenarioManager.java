import java.util.*;
import java.util.stream.Collectors;

/**
 * What-If Scenario Manager for GradeRise
 * Enables students to plan and predict grade outcomes under different scenarios
 */
public class WhatIfScenarioManager {
    
    /**
     * Scenario Types for different planning purposes
     */
    public enum ScenarioType {
        GRADE_GOAL,          // "What grade do I need to achieve X?"
        FINAL_IMPACT,        // "How will my final exam affect my grade?"
        ASSIGNMENT_SKIP,     // "What if I skip this assignment?"
        IMPROVEMENT_PLAN,    // "How to improve my grade by X points?"
        SEMESTER_PROJECTION  // "What will my semester GPA be?"
    }
    
    /**
     * Scenario Result containing all relevant information
     */
    public static class ScenarioResult {
        private final ScenarioType type;
        private final String scenarioName;
        private final boolean achievable;
        private final double requiredGrade;
        private final double currentGrade;
        private final double projectedGrade;
        private final List<String> recommendations;
        private final List<ActionPlan> actionPlans;
        private final Map<String, Double> impactAnalysis;
        private final double difficultyScore; // 0-10 scale
        
        public ScenarioResult(ScenarioType type, String name, boolean achievable, 
                            double required, double current, double projected,
                            List<String> recommendations, List<ActionPlan> plans,
                            Map<String, Double> impact, double difficulty) {
            this.type = type;
            this.scenarioName = name;
            this.achievable = achievable;
            this.requiredGrade = required;
            this.currentGrade = current;
            this.projectedGrade = projected;
            this.recommendations = new ArrayList<>(recommendations);
            this.actionPlans = new ArrayList<>(plans);
            this.impactAnalysis = new HashMap<>(impact);
            this.difficultyScore = difficulty;
        }
        
        // Getters
        public ScenarioType getType() { return type; }
        public String getScenarioName() { return scenarioName; }
        public boolean isAchievable() { return achievable; }
        public double getRequiredGrade() { return requiredGrade; }
        public double getCurrentGrade() { return currentGrade; }
        public double getProjectedGrade() { return projectedGrade; }
        public List<String> getRecommendations() { return recommendations; }
        public List<ActionPlan> getActionPlans() { return actionPlans; }
        public Map<String, Double> getImpactAnalysis() { return impactAnalysis; }
        public double getDifficultyScore() { return difficultyScore; }
        
        public String getDifficultyLevel() {
            if (difficultyScore <= 2) return "Very Easy 😊";
            if (difficultyScore <= 4) return "Easy 🙂";
            if (difficultyScore <= 6) return "Moderate 😐";
            if (difficultyScore <= 8) return "Challenging 😰";
            return "Very Difficult 😱";
        }
    }
    
    /**
     * Action Plan for achieving specific goals
     */
    public static class ActionPlan {
        private final String title;
        private final String description;
        private final int priority; // 1-5, 1 being highest
        private final double expectedImpact; // Grade points improvement
        private final int estimatedHours;
        private final List<String> steps;
        
        public ActionPlan(String title, String description, int priority, 
                         double impact, int hours, List<String> steps) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.expectedImpact = impact;
            this.estimatedHours = hours;
            this.steps = new ArrayList<>(steps);
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getPriority() { return priority; }
        public double getExpectedImpact() { return expectedImpact; }
        public int getEstimatedHours() { return estimatedHours; }
        public List<String> getSteps() { return steps; }
        
        public String getPriorityLabel() {
            switch (priority) {
                case 1: return "🔴 Critical";
                case 2: return "🟠 High";
                case 3: return "🟡 Medium";
                case 4: return "🔵 Low";
                case 5: return "⚪ Optional";
                default: return "Unknown";
            }
        }
    }
    
    /**
     * Grade Goal Scenario: "What grade do I need on upcoming assignments to achieve X?"
     */
    public static ScenarioResult calculateGradeGoalScenario(Course course, double targetGrade) {
        List<Assignment> assignments = course.getAllAssignments();
        double currentTotal = assignments.stream().mapToDouble(Assignment::getPointsEarned).sum();
        double maxCurrentPoints = assignments.stream().mapToDouble(Assignment::getMaxPoints).sum();
        double currentGrade = maxCurrentPoints > 0 ? (currentTotal / maxCurrentPoints) * 100 : 0;
        
        // Estimate remaining points (assume course has ~20 assignments total)
        int remainingAssignments = Math.max(1, 20 - assignments.size());
        double avgPointsPerAssignment = maxCurrentPoints > 0 ? maxCurrentPoints / assignments.size() : 100;
        double remainingMaxPoints = remainingAssignments * avgPointsPerAssignment;
        
        // Calculate required grade on remaining assignments
        double totalMaxPoints = maxCurrentPoints + remainingMaxPoints;
        double targetTotal = (targetGrade / 100.0) * totalMaxPoints;
        double requiredTotal = targetTotal - currentTotal;
        double requiredGrade = remainingMaxPoints > 0 ? (requiredTotal / remainingMaxPoints) * 100 : 0;
        
        boolean achievable = requiredGrade <= 100 && requiredGrade >= 0;
        double difficulty = calculateDifficultyScore(requiredGrade, currentGrade, assignments);
        
        List<String> recommendations = generateGradeGoalRecommendations(requiredGrade, currentGrade, achievable);
        List<ActionPlan> actionPlans = generateGradeGoalActionPlans(requiredGrade, currentGrade, course);
        Map<String, Double> impact = calculateAssignmentImpacts(course, remainingMaxPoints);
        
        return new ScenarioResult(
            ScenarioType.GRADE_GOAL,
            String.format("Achieve %.1f%% Grade", targetGrade),
            achievable,
            requiredGrade,
            currentGrade,
            targetGrade,
            recommendations,
            actionPlans,
            impact,
            difficulty
        );
    }
    
    /**
     * Final Exam Impact Scenario: "How will different final exam scores affect my grade?"
     */
    public static ScenarioResult calculateFinalExamImpactScenario(Course course, double finalExamWeight, 
                                                                double... possibleScores) {
        double currentGrade = course.calculateCurrentGrade();
        double currentWeight = 1.0 - finalExamWeight;
        
        Map<String, Double> scenarios = new HashMap<>();
        double likelyCase = 0;
        
        for (double score : possibleScores) {
            double finalGrade = currentGrade * currentWeight + score * finalExamWeight;
            scenarios.put(String.format("Score: %.0f%%", score), finalGrade);
            
            if (score == 85) likelyCase = finalGrade; // Assume likely score of 85%
        }
        
        List<String> recommendations = generateFinalExamRecommendations(currentGrade, finalExamWeight, scenarios);
        List<ActionPlan> actionPlans = generateFinalExamActionPlans(course, finalExamWeight);
        double difficulty = calculateFinalExamDifficulty(currentGrade, finalExamWeight);
        
        return new ScenarioResult(
            ScenarioType.FINAL_IMPACT,
            "Final Exam Impact Analysis",
            true,
            0, // No specific required grade
            currentGrade,
            likelyCase,
            recommendations,
            actionPlans,
            scenarios,
            difficulty
        );
    }
    
    /**
     * Assignment Skip Scenario: "What happens if I skip this assignment?"
     */
    public static ScenarioResult calculateAssignmentSkipScenario(Course course, double assignmentWeight) {
        double currentGrade = course.calculateCurrentGrade();
        double gradeWithSkip = currentGrade * (1.0 - assignmentWeight); // Assume 0 points for skipped assignment
        
        double impactPoints = currentGrade - gradeWithSkip;
        boolean advisable = impactPoints < 2.0; // Less than 2 point drop might be acceptable
        
        List<String> recommendations = generateSkipRecommendations(impactPoints, currentGrade, advisable);
        List<ActionPlan> actionPlans = generateSkipActionPlans(course, assignmentWeight, impactPoints);
        Map<String, Double> impact = Map.of(
            "Current Grade", currentGrade,
            "Grade After Skip", gradeWithSkip,
            "Point Reduction", impactPoints
        );
        
        double difficulty = impactPoints * 2; // Higher impact = higher difficulty to recover
        
        return new ScenarioResult(
            ScenarioType.ASSIGNMENT_SKIP,
            "Skip Assignment Impact",
            advisable,
            0,
            currentGrade,
            gradeWithSkip,
            recommendations,
            actionPlans,
            impact,
            difficulty
        );
    }
    
    /**
     * Improvement Plan Scenario: "How can I improve my grade by X points?"
     */
    public static ScenarioResult calculateImprovementPlanScenario(Course course, double targetImprovement) {
        double currentGrade = course.calculateCurrentGrade();
        double targetGrade = currentGrade + targetImprovement;
        
        List<Assignment> assignments = course.getAllAssignments();
        List<ActionPlan> actionPlans = generateImprovementActionPlans(course, targetImprovement);
        
        // Calculate what's needed for improvement
        boolean achievable = targetGrade <= 100;
        double difficulty = calculateImprovementDifficulty(targetImprovement, currentGrade, assignments);
        
        List<String> recommendations = generateImprovementRecommendations(targetImprovement, currentGrade, achievable);
        Map<String, Double> impact = calculateImprovementStrategies(course, targetImprovement);
        
        return new ScenarioResult(
            ScenarioType.IMPROVEMENT_PLAN,
            String.format("Improve Grade by %.1f Points", targetImprovement),
            achievable,
            targetGrade,
            currentGrade,
            targetGrade,
            recommendations,
            actionPlans,
            impact,
            difficulty
        );
    }
    
    /**
     * Semester Projection Scenario: "What will my overall semester GPA be?"
     */
    public static ScenarioResult calculateSemesterProjectionScenario(Semester semester) {
        List<Course> courses = semester.getAllCourses();
        double currentGPA = semester.calculateGPA();
        
        // Project final GPA based on current performance and trends
        double projectedGPA = 0.0;
        double totalCredits = 0.0;
        
        for (Course course : courses) {
            double courseGrade = course.calculateCurrentGrade();
            
            // Apply trend analysis for projection
            GradeAnalyticsEngine.TrendType trend = GradeAnalyticsEngine.analyzeTrend(course.getAllAssignments());
            double adjustment = getTrendAdjustment(trend);
            double projectedCourseGrade = Math.max(0, Math.min(100, courseGrade + adjustment));
            
            double gpa = convertToGPA(projectedCourseGrade);
            projectedGPA += gpa * course.getCredits();
            totalCredits += course.getCredits();
        }
        
        projectedGPA = totalCredits > 0 ? projectedGPA / totalCredits : 0.0;
        
        List<String> recommendations = generateSemesterRecommendations(currentGPA, projectedGPA, courses);
        List<ActionPlan> actionPlans = generateSemesterActionPlans(semester);
        Map<String, Double> impact = calculateCourseImpacts(semester);
        double difficulty = calculateSemesterDifficulty(currentGPA, projectedGPA);
        
        return new ScenarioResult(
            ScenarioType.SEMESTER_PROJECTION,
            "Semester GPA Projection",
            true,
            projectedGPA,
            currentGPA,
            projectedGPA,
            recommendations,
            actionPlans,
            impact,
            difficulty
        );
    }
    
    // Helper Methods for Recommendations and Action Plans
    
    private static List<String> generateGradeGoalRecommendations(double required, double current, boolean achievable) {
        List<String> recommendations = new ArrayList<>();
        
        if (!achievable) {
            recommendations.add("🚫 Target grade is not achievable with remaining assignments");
            recommendations.add("💡 Consider adjusting your target to a more realistic goal");
            recommendations.add("📚 Focus on maximizing learning rather than just grades");
        } else if (required > 95) {
            recommendations.add("⚡ Extremely high performance required on all remaining work");
            recommendations.add("📖 Consider getting tutoring or additional help");
            recommendations.add("👥 Form study groups with high-performing classmates");
        } else if (required > 85) {
            recommendations.add("💪 Strong performance needed - very achievable with effort");
            recommendations.add("📝 Focus on understanding concepts thoroughly");
            recommendations.add("⏰ Create a detailed study schedule");
        } else {
            recommendations.add("✅ Your target is very achievable!");
            recommendations.add("📈 Maintain consistent effort on assignments");
            recommendations.add("🎯 Consider aiming even higher if comfortable");
        }
        
        return recommendations;
    }
    
    private static List<ActionPlan> generateGradeGoalActionPlans(double required, double current, Course course) {
        List<ActionPlan> plans = new ArrayList<>();
        
        if (required > 90) {
            plans.add(new ActionPlan(
                "Intensive Study Plan",
                "Maximize performance on all remaining assignments",
                1,
                required - current,
                15,
                Arrays.asList(
                    "Create detailed study schedule for each upcoming assignment",
                    "Seek help from professor during office hours",
                    "Form study group with top students",
                    "Complete all practice problems and review materials",
                    "Start assignments early to allow for revisions"
                )
            ));
        }
        
        plans.add(new ActionPlan(
            "Assignment Optimization",
            "Focus on high-impact assignments and categories",
            2,
            (required - current) * 0.6,
            8,
            Arrays.asList(
                "Identify which assignment types are worth the most points",
                "Allocate more time to high-weight assignments",
                "Use rubrics to understand grading criteria",
                "Submit drafts for feedback when possible"
            )
        ));
        
        return plans;
    }
    
    private static List<String> generateFinalExamRecommendations(double current, double weight, 
                                                               Map<String, Double> scenarios) {
        List<String> recommendations = new ArrayList<>();
        
        double finalImpact = weight * 100; // Convert to percentage points
        
        if (finalImpact > 30) {
            recommendations.add("🎯 Final exam has MAJOR impact on your grade");
            recommendations.add("📚 Dedicate significant time to final exam preparation");
            recommendations.add("📝 Review all course materials systematically");
        } else if (finalImpact > 15) {
            recommendations.add("⚖️ Final exam has moderate impact on your grade");
            recommendations.add("📖 Solid preparation will help secure your desired grade");
        } else {
            recommendations.add("✅ Final exam has limited impact - you're in good shape!");
            recommendations.add("📋 Focus on understanding key concepts for the exam");
        }
        
        // Add specific score recommendations
        double safeScore = calculateSafeScore(current, weight);
        recommendations.add(String.format("🎯 Aim for at least %.0f%% to maintain current performance", safeScore));
        
        return recommendations;
    }
    
    private static List<ActionPlan> generateFinalExamActionPlans(Course course, double weight) {
        List<ActionPlan> plans = new ArrayList<>();
        
        plans.add(new ActionPlan(
            "Final Exam Study Plan",
            "Comprehensive preparation for the final exam",
            1,
            weight * 20, // Potential improvement
            20,
            Arrays.asList(
                "Create a study timeline working backwards from exam date",
                "Review all lecture notes and highlight key concepts",
                "Complete all practice exams and past finals",
                "Form study group to discuss difficult topics",
                "Schedule professor office hours for clarification",
                "Create summary sheets for each major topic"
            )
        ));
        
        return plans;
    }
    
    private static List<String> generateSkipRecommendations(double impact, double current, boolean advisable) {
        List<String> recommendations = new ArrayList<>();
        
        if (advisable) {
            recommendations.add("✅ Minimal impact on overall grade");
            recommendations.add("⏰ Could be acceptable if you have competing priorities");
            recommendations.add("📚 Focus extra effort on higher-weight assignments instead");
        } else {
            recommendations.add("⚠️ Significant negative impact on your grade");
            recommendations.add("💪 Strongly recommend completing this assignment");
            recommendations.add("🆘 If struggling, seek help rather than skipping");
        }
        
        recommendations.add(String.format("📉 Skipping would reduce your grade by %.1f points", impact));
        
        return recommendations;
    }
    
    private static List<ActionPlan> generateSkipActionPlans(Course course, double weight, double impact) {
        List<ActionPlan> plans = new ArrayList<>();
        
        if (impact > 3) {
            plans.add(new ActionPlan(
                "Complete the Assignment",
                "Avoid the significant grade penalty",
                1,
                impact,
                5,
                Arrays.asList(
                    "Break the assignment into smaller, manageable parts",
                    "Seek help from classmates or teaching assistants",
                    "Visit professor's office hours for guidance",
                    "Focus on meeting minimum requirements rather than perfection",
                    "Submit partial work rather than nothing"
                )
            ));
        }
        
        return plans;
    }
    
    private static List<String> generateImprovementRecommendations(double improvement, double current, boolean achievable) {
        List<String> recommendations = new ArrayList<>();
        
        if (!achievable) {
            recommendations.add("🚫 Improvement target exceeds maximum possible grade");
            recommendations.add("🎯 Consider a more realistic improvement goal");
        } else if (improvement > 15) {
            recommendations.add("🚀 Ambitious goal - will require significant effort and strategy changes");
            recommendations.add("📈 Focus on identifying and addressing weak areas");
        } else if (improvement > 5) {
            recommendations.add("💪 Achievable with focused effort and good study habits");
            recommendations.add("📊 Analyze your past performance to identify improvement opportunities");
        } else {
            recommendations.add("✅ Very achievable improvement goal!");
            recommendations.add("🎯 Small consistent improvements will get you there");
        }
        
        return recommendations;
    }
    
    private static List<ActionPlan> generateImprovementActionPlans(Course course, double improvement) {
        List<ActionPlan> plans = new ArrayList<>();
        
        plans.add(new ActionPlan(
            "Study Strategy Optimization",
            "Improve study effectiveness and efficiency",
            1,
            improvement * 0.4,
            10,
            Arrays.asList(
                "Analyze past assignments to identify common mistakes",
                "Adjust study methods based on what works best",
                "Create a consistent study schedule",
                "Use active learning techniques (teaching, practice problems)",
                "Track progress on each assignment type"
            )
        ));
        
        plans.add(new ActionPlan(
            "Seek Additional Support",
            "Leverage available resources for better understanding",
            2,
            improvement * 0.3,
            6,
            Arrays.asList(
                "Attend all available review sessions",
                "Visit professor during office hours regularly",
                "Join or form a study group",
                "Consider getting a tutor for difficult topics",
                "Use online resources and supplementary materials"
            )
        ));
        
        return plans;
    }
    
    private static List<String> generateSemesterRecommendations(double current, double projected, List<Course> courses) {
        List<String> recommendations = new ArrayList<>();
        
        double difference = projected - current;
        
        if (difference > 0.2) {
            recommendations.add("📈 Your semester is trending upward - great progress!");
            recommendations.add("💪 Maintain current momentum through finals");
        } else if (difference < -0.2) {
            recommendations.add("📉 Grades trending downward - time to refocus");
            recommendations.add("🆘 Consider seeking academic support services");
        } else {
            recommendations.add("📊 Stable performance - consistent effort paying off");
            recommendations.add("🎯 Focus on strong finish to achieve goals");
        }
        
        if (projected >= 3.5) {
            recommendations.add("🌟 On track for excellent semester GPA!");
        } else if (projected >= 3.0) {
            recommendations.add("👍 Solid semester performance");
        } else if (projected >= 2.0) {
            recommendations.add("⚠️ Consider strategies to improve final grades");
        } else {
            recommendations.add("🚨 Urgent: Seek academic advising support");
        }
        
        return recommendations;
    }
    
    private static List<ActionPlan> generateSemesterActionPlans(Semester semester) {
        List<ActionPlan> plans = new ArrayList<>();
        
        plans.add(new ActionPlan(
            "Finals Preparation Strategy",
            "Optimize performance across all courses for semester end",
            1,
            0.5, // Potential GPA improvement
            25,
            Arrays.asList(
                "Create master calendar of all finals and major assignments",
                "Prioritize study time based on course weights and current grades",
                "Schedule review sessions for each course",
                "Prepare comprehensive study materials for each final",
                "Plan adequate rest and stress management"
            )
        ));
        
        return plans;
    }
    
    // Utility Methods
    
    private static double calculateDifficultyScore(double required, double current, List<Assignment> assignments) {
        if (required > 100) return 10;
        if (required < 0) return 0;
        
        double baseScore = Math.abs(required - current) / 10.0;
        
        // Adjust based on consistency
        double consistency = calculateConsistency(assignments);
        double consistencyAdjustment = (1.0 - consistency) * 2.0;
        
        return Math.min(10, Math.max(0, baseScore + consistencyAdjustment));
    }
    
    private static double calculateConsistency(List<Assignment> assignments) {
        if (assignments.size() < 2) return 1.0;
        
        double avg = assignments.stream().mapToDouble(Assignment::getGradePercent).average().orElse(0);
        double variance = assignments.stream()
            .mapToDouble(a -> Math.pow(a.getGradePercent() - avg, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        return Math.max(0, 1.0 - (stdDev / 50.0)); // Scale to 0-1
    }
    
    private static Map<String, Double> calculateAssignmentImpacts(Course course, double remainingPoints) {
        Map<String, Double> impacts = new HashMap<>();
        
        List<Assignment> allAssignments = course.getAllAssignments();
        Map<String, List<Assignment>> byCategory = allAssignments.stream()
            .collect(Collectors.groupingBy(Assignment::getCategory));
        
        for (Map.Entry<String, List<Assignment>> entry : byCategory.entrySet()) {
            double categoryWeight = entry.getValue().size() / (double) course.getAllAssignments().size();
            double impact = categoryWeight * remainingPoints;
            impacts.put(entry.getKey(), impact);
        }
        
        return impacts;
    }
    
    private static double calculateSafeScore(double current, double weight) {
        // Calculate score needed to maintain current grade
        return current / weight;
    }
    
    private static double getTrendAdjustment(GradeAnalyticsEngine.TrendType trend) {
        switch (trend) {
            case IMPROVING: return 3.0;
            case DECLINING: return -2.0;
            case VOLATILE: return 0.0;
            case STABLE: default: return 1.0;
        }
    }
    
    private static double convertToGPA(double percentage) {
        if (percentage >= 97) return 4.0;
        if (percentage >= 93) return 3.7;
        if (percentage >= 90) return 3.3;
        if (percentage >= 87) return 3.0;
        if (percentage >= 83) return 2.7;
        if (percentage >= 80) return 2.3;
        if (percentage >= 77) return 2.0;
        if (percentage >= 73) return 1.7;
        if (percentage >= 70) return 1.3;
        if (percentage >= 67) return 1.0;
        if (percentage >= 65) return 0.7;
        return 0.0;
    }
    
    private static double calculateFinalExamDifficulty(double current, double weight) {
        return weight * 5; // Higher weight = higher difficulty to manage
    }
    
    private static double calculateImprovementDifficulty(double improvement, double current, List<Assignment> assignments) {
        double baseScore = improvement / 5.0; // Each 5 points adds 1 difficulty
        double currentPerformance = current / 100.0; // Factor in current level
        
        return Math.min(10, Math.max(1, baseScore + (1 - currentPerformance) * 3));
    }
    
    private static Map<String, Double> calculateImprovementStrategies(Course course, double improvement) {
        Map<String, Double> strategies = new HashMap<>();
        
        strategies.put("Better Study Habits", improvement * 0.4);
        strategies.put("Additional Help/Tutoring", improvement * 0.3);
        strategies.put("More Time Investment", improvement * 0.2);
        strategies.put("Test-Taking Strategies", improvement * 0.1);
        
        return strategies;
    }
    
    private static Map<String, Double> calculateCourseImpacts(Semester semester) {
        Map<String, Double> impacts = new HashMap<>();
        
        for (Course course : semester.getAllCourses()) {
            double gpa = convertToGPA(course.calculateCurrentGrade());
            impacts.put(course.getName(), gpa * course.getCredits());
        }
        
        return impacts;
    }
    
    private static double calculateSemesterDifficulty(double current, double projected) {
        double difference = Math.abs(projected - current);
        return Math.min(10, difference * 3); // Scale difficulty based on GPA change
    }
}