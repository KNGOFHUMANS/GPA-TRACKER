import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Grade Analytics Engine for GradeRise
 * Provides grade prediction algorithms, trend analysis, and statistical calculations
 */
public class GradeAnalyticsEngine {
    
    // Prediction Models
    public enum PredictionModel {
        LINEAR_REGRESSION,
        WEIGHTED_AVERAGE,
        MOMENTUM_BASED,
        DIFFICULTY_ADJUSTED
    }
    
    // Grade Trend Types
    public enum TrendType {
        IMPROVING, DECLINING, STABLE, VOLATILE
    }
    
    /**
     * Grade Prediction Result containing various scenarios
     */
    public static class GradePrediction {
        private final double conservativeGrade;
        private final double likelyGrade;
        private final double optimisticGrade;
        private final double confidenceScore;
        private final TrendType currentTrend;
        private final String trendDescription;
        
        public GradePrediction(double conservative, double likely, double optimistic, 
                             double confidence, TrendType trend, String description) {
            this.conservativeGrade = conservative;
            this.likelyGrade = likely;
            this.optimisticGrade = optimistic;
            this.confidenceScore = confidence;
            this.currentTrend = trend;
            this.trendDescription = description;
        }
        
        // Getters
        public double getConservativeGrade() { return conservativeGrade; }
        public double getLikelyGrade() { return likelyGrade; }
        public double getOptimisticGrade() { return optimisticGrade; }
        public double getConfidenceScore() { return confidenceScore; }
        public TrendType getCurrentTrend() { return currentTrend; }
        public String getTrendDescription() { return trendDescription; }
    }
    
    /**
     * Grade Statistics for comprehensive analysis
     */
    public static class GradeStatistics {
        private final double mean;
        private final double median;
        private final double mode;
        private final double standardDeviation;
        private final double variance;
        private final double range;
        private final double quartile1;
        private final double quartile3;
        private final int totalAssignments;
        
        public GradeStatistics(double mean, double median, double mode, double stdDev,
                             double variance, double range, double q1, double q3, int total) {
            this.mean = mean;
            this.median = median;
            this.mode = mode;
            this.standardDeviation = stdDev;
            this.variance = variance;
            this.range = range;
            this.quartile1 = q1;
            this.quartile3 = q3;
            this.totalAssignments = total;
        }
        
        // Getters
        public double getMean() { return mean; }
        public double getMedian() { return median; }
        public double getMode() { return mode; }
        public double getStandardDeviation() { return standardDeviation; }
        public double getVariance() { return variance; }
        public double getRange() { return range; }
        public double getQuartile1() { return quartile1; }
        public double getQuartile3() { return quartile3; }
        public int getTotalAssignments() { return totalAssignments; }
    }
    
    /**
     * Predicts final grade using multiple algorithms
     */
    public static GradePrediction predictFinalGrade(Course course, PredictionModel model) {
        List<Assignment> assignments = course.getAllAssignments();
        if (assignments.isEmpty()) {
            return new GradePrediction(0.0, 0.0, 0.0, 0.0, TrendType.STABLE, "No data available");
        }
        
        // Sort assignments by date
        assignments.sort((a, b) -> a.getDateAdded().compareTo(b.getDateAdded()));
        
        double prediction = 0.0;
        double confidence = 0.0;
        TrendType trend = analyzeTrend(assignments);
        String description = generateTrendDescription(assignments, trend);
        
        switch (model) {
            case LINEAR_REGRESSION:
                prediction = calculateLinearRegressionPrediction(assignments);
                confidence = calculateRegressionConfidence(assignments);
                break;
            case WEIGHTED_AVERAGE:
                prediction = calculateWeightedAveragePrediction(assignments);
                confidence = 0.75; // High confidence for weighted average
                break;
            case MOMENTUM_BASED:
                prediction = calculateMomentumPrediction(assignments);
                confidence = calculateMomentumConfidence(assignments);
                break;
            case DIFFICULTY_ADJUSTED:
                prediction = calculateDifficultyAdjustedPrediction(assignments);
                confidence = 0.70;
                break;
        }
        
        // Generate conservative and optimistic scenarios
        double stdDev = calculateStandardDeviation(assignments);
        double conservative = Math.max(0, prediction - stdDev * 0.5);
        double optimistic = Math.min(100, prediction + stdDev * 0.3);
        
        return new GradePrediction(conservative, prediction, optimistic, confidence, trend, description);
    }
    
    /**
     * Linear Regression Prediction Algorithm
     */
    private static double calculateLinearRegressionPrediction(List<Assignment> assignments) {
        if (assignments.size() < 2) return getCurrentAverage(assignments);
        
        List<Double> grades = assignments.stream()
            .mapToDouble(Assignment::getGradePercent)
            .boxed()
            .collect(Collectors.toList());
        
        int n = grades.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1; // Time index
            double y = grades.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Project to end of semester (assume 20 assignments total)
        double projectedFinal = slope * 20 + intercept;
        return Math.max(0, Math.min(100, projectedFinal));
    }
    
    /**
     * Weighted Average Prediction (recent grades weighted more heavily)
     */
    private static double calculateWeightedAveragePrediction(List<Assignment> assignments) {
        if (assignments.isEmpty()) return 0.0;
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        int n = assignments.size();
        
        for (int i = 0; i < n; i++) {
            // More recent assignments get higher weights
            double weight = Math.pow(1.2, i); // Exponential weighting
            weightedSum += assignments.get(i).getGradePercent() * weight;
            totalWeight += weight;
        }
        
        return weightedSum / totalWeight;
    }
    
    /**
     * Momentum-Based Prediction (considers grade velocity)
     */
    private static double calculateMomentumPrediction(List<Assignment> assignments) {
        if (assignments.size() < 3) return getCurrentAverage(assignments);
        
        // Calculate recent momentum (last 5 assignments)
        int recentCount = Math.min(5, assignments.size());
        List<Assignment> recent = assignments.subList(assignments.size() - recentCount, assignments.size());
        
        double momentum = 0.0;
        for (int i = 1; i < recent.size(); i++) {
            momentum += recent.get(i).getGradePercent() - recent.get(i-1).getGradePercent();
        }
        momentum /= (recent.size() - 1);
        
        double currentAvg = getCurrentAverage(assignments);
        // Project momentum forward (damped by 0.7 to avoid extreme predictions)
        return Math.max(0, Math.min(100, currentAvg + momentum * 5 * 0.7));
    }
    
    /**
     * Difficulty-Adjusted Prediction (considers assignment types and weights)
     */
    private static double calculateDifficultyAdjustedPrediction(List<Assignment> assignments) {
        if (assignments.isEmpty()) return 0.0;
        
        Map<String, List<Assignment>> byType = assignments.stream()
            .collect(Collectors.groupingBy(Assignment::getCategory));
        
        double totalWeightedGrade = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, List<Assignment>> entry : byType.entrySet()) {
            String category = entry.getKey();
            List<Assignment> categoryAssignments = entry.getValue();
            
            // Assign difficulty multipliers based on category
            double difficultyMultiplier = getDifficultyMultiplier(category);
            double categoryAvg = categoryAssignments.stream()
                .mapToDouble(Assignment::getGradePercent)
                .average()
                .orElse(0.0);
            
            double weight = categoryAssignments.size() * difficultyMultiplier;
            totalWeightedGrade += categoryAvg * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalWeightedGrade / totalWeight : 0.0;
    }
    
    /**
     * Get difficulty multiplier based on assignment category
     */
    private static double getDifficultyMultiplier(String category) {
        switch (category.toLowerCase()) {
            case "exam": case "final": return 2.5;
            case "project": case "paper": return 2.0;
            case "quiz": return 1.5;
            case "homework": case "assignment": return 1.0;
            case "participation": case "attendance": return 0.8;
            default: return 1.0;
        }
    }
    
    /**
     * Analyze grade trend over time
     */
    public static TrendType analyzeTrend(List<Assignment> assignments) {
        if (assignments.size() < 3) return TrendType.STABLE;
        
        // Calculate recent vs older performance
        int mid = assignments.size() / 2;
        List<Assignment> older = assignments.subList(0, mid);
        List<Assignment> recent = assignments.subList(mid, assignments.size());
        
        double olderAvg = getCurrentAverage(older);
        double recentAvg = getCurrentAverage(recent);
        double difference = recentAvg - olderAvg;
        
        // Calculate volatility
        double volatility = calculateStandardDeviation(assignments);
        
        if (volatility > 15.0) return TrendType.VOLATILE;
        if (difference > 5.0) return TrendType.IMPROVING;
        if (difference < -5.0) return TrendType.DECLINING;
        return TrendType.STABLE;
    }
    
    /**
     * Generate comprehensive grade statistics
     */
    public static GradeStatistics calculateStatistics(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            return new GradeStatistics(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        List<Double> grades = assignments.stream()
            .mapToDouble(Assignment::getGradePercent)
            .sorted()
            .boxed()
            .collect(Collectors.toList());
        
        double mean = grades.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = calculateMedian(grades);
        double mode = calculateMode(grades);
        double variance = calculateVariance(grades, mean);
        double stdDev = Math.sqrt(variance);
        double range = grades.get(grades.size() - 1) - grades.get(0);
        double q1 = calculateQuartile(grades, 0.25);
        double q3 = calculateQuartile(grades, 0.75);
        
        return new GradeStatistics(mean, median, mode, stdDev, variance, range, q1, q3, assignments.size());
    }
    
    /**
     * Helper Methods
     */
    private static double getCurrentAverage(List<Assignment> assignments) {
        return assignments.stream()
            .mapToDouble(Assignment::getGradePercent)
            .average()
            .orElse(0.0);
    }
    
    private static double calculateStandardDeviation(List<Assignment> assignments) {
        if (assignments.size() < 2) return 0.0;
        
        double mean = getCurrentAverage(assignments);
        double variance = assignments.stream()
            .mapToDouble(a -> Math.pow(a.getGradePercent() - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private static double calculateRegressionConfidence(List<Assignment> assignments) {
        if (assignments.size() < 3) return 0.5;
        
        // Calculate R-squared for confidence
        double correlation = calculateCorrelation(assignments);
        double rSquared = correlation * correlation;
        
        // Adjust for sample size
        double sizeAdjustment = Math.min(1.0, assignments.size() / 10.0);
        
        return Math.max(0.3, Math.min(0.95, rSquared * sizeAdjustment));
    }
    
    private static double calculateMomentumConfidence(List<Assignment> assignments) {
        if (assignments.size() < 5) return 0.4;
        
        // Higher confidence if trend is consistent
        TrendType trend = analyzeTrend(assignments);
        double volatility = calculateStandardDeviation(assignments);
        
        double baseConfidence = trend == TrendType.VOLATILE ? 0.3 : 0.7;
        double volatilityPenalty = Math.min(0.3, volatility / 50.0);
        
        return Math.max(0.2, baseConfidence - volatilityPenalty);
    }
    
    private static double calculateCorrelation(List<Assignment> assignments) {
        int n = assignments.size();
        if (n < 2) return 0.0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0, sumYY = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1; // Time index
            double y = assignments.get(i).getGradePercent();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
            sumYY += y * y;
        }
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumXX - sumX * sumX) * (n * sumYY - sumY * sumY));
        
        return denominator != 0 ? numerator / denominator : 0.0;
    }
    
    private static String generateTrendDescription(List<Assignment> assignments, TrendType trend) {
        double currentAvg = getCurrentAverage(assignments);
        
        switch (trend) {
            case IMPROVING:
                return String.format("📈 Grades are improving! Current average: %.1f%%", currentAvg);
            case DECLINING:
                return String.format("📉 Grades are declining. Current average: %.1f%% - Consider seeking help", currentAvg);
            case VOLATILE:
                return String.format("📊 Inconsistent performance. Current average: %.1f%% - Focus on consistency", currentAvg);
            case STABLE:
            default:
                return String.format("📊 Stable performance. Current average: %.1f%%", currentAvg);
        }
    }
    
    // Statistical helper methods
    private static double calculateMedian(List<Double> sortedGrades) {
        int n = sortedGrades.size();
        if (n % 2 == 0) {
            return (sortedGrades.get(n/2 - 1) + sortedGrades.get(n/2)) / 2.0;
        } else {
            return sortedGrades.get(n/2);
        }
    }
    
    private static double calculateMode(List<Double> grades) {
        Map<Double, Integer> frequency = new HashMap<>();
        for (Double grade : grades) {
            frequency.put(grade, frequency.getOrDefault(grade, 0) + 1);
        }
        
        return frequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(0.0);
    }
    
    private static double calculateVariance(List<Double> grades, double mean) {
        return grades.stream()
            .mapToDouble(grade -> Math.pow(grade - mean, 2))
            .average()
            .orElse(0.0);
    }
    
    private static double calculateQuartile(List<Double> sortedGrades, double percentile) {
        int n = sortedGrades.size();
        double index = percentile * (n - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedGrades.get(lowerIndex);
        } else {
            double weight = index - lowerIndex;
            return sortedGrades.get(lowerIndex) * (1 - weight) + sortedGrades.get(upperIndex) * weight;
        }
    }
}