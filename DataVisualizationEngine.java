import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.time.LocalDate;

/**
 * Advanced Data Visualization Engine for GradeRise
 * Interactive charts without external dependencies using custom Java2D graphics
 */
public class DataVisualizationEngine {
    
    // Chart Types
    public enum ChartType {
        GPA_TREND_LINE("GPA Trend Over Time", "Time", "GPA"),
        GRADE_DISTRIBUTION("Grade Distribution", "Grade Range", "Count"),
        COURSE_DIFFICULTY("Course Difficulty Analysis", "Course", "Difficulty Score"),
        ACHIEVEMENT_PROGRESS("Achievement Progress", "Category", "Progress %"),
        SEMESTER_COMPARISON("Semester Comparison", "Semester", "GPA"),
        ASSIGNMENT_TRENDS("Assignment Performance", "Assignment", "Score %");
        
        private final String title;
        private final String xLabel;
        private final String yLabel;
        
        ChartType(String title, String xLabel, String yLabel) {
            this.title = title;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
        }
        
        public String getTitle() { return title; }
        public String getXLabel() { return xLabel; }
        public String getYLabel() { return yLabel; }
    }
    
    // Chart Data Models
    public static class ChartDataPoint {
        private final String label;
        private final double value;
        private final LocalDate date;
        private final Color color;
        private final Map<String, Object> metadata;
        
        public ChartDataPoint(String label, double value) {
            this(label, value, LocalDate.now(), null, new HashMap<>());
        }
        
        public ChartDataPoint(String label, double value, LocalDate date, Color color, Map<String, Object> metadata) {
            this.label = label;
            this.value = value;
            this.date = date;
            this.color = color != null ? color : generateColorForValue(value);
            this.metadata = metadata != null ? metadata : new HashMap<>();
        }
        
        private static Color generateColorForValue(double value) {
            // Generate color based on value (red for low, green for high)
            if (value >= 90) return new Color(34, 197, 94);  // Green
            if (value >= 80) return new Color(59, 130, 246); // Blue
            if (value >= 70) return new Color(245, 158, 11); // Orange
            return new Color(239, 68, 68); // Red
        }
        
        // Getters
        public String getLabel() { return label; }
        public double getValue() { return value; }
        public LocalDate getDate() { return date; }
        public Color getColor() { return color; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
    
    public static class ChartDataset {
        private final String name;
        private final List<ChartDataPoint> dataPoints;
        private final ChartType chartType;
        private final Color primaryColor;
        
        public ChartDataset(String name, ChartType type) {
            this.name = name;
            this.chartType = type;
            this.dataPoints = new ArrayList<>();
            this.primaryColor = new Color(139, 0, 0); // GradeRise red
        }
        
        public void addDataPoint(ChartDataPoint point) {
            dataPoints.add(point);
        }
        
        public void addDataPoint(String label, double value) {
            addDataPoint(new ChartDataPoint(label, value));
        }
        
        // Getters
        public String getName() { return name; }
        public List<ChartDataPoint> getDataPoints() { return dataPoints; }
        public ChartType getChartType() { return chartType; }
        public Color getPrimaryColor() { return primaryColor; }
    }
    
    /**
     * Interactive Chart Panel with custom graphics
     */
    public static class InteractiveChartPanel extends JPanel {
        private ChartDataset dataset;
        private boolean showGrid = true;
        private boolean showLegend = true;
        private boolean showTooltips = true;
        private boolean isAnimated = true;
        private double animationProgress = 1.0;
        
        // Mouse interaction
        private Point mousePosition;
        private int hoveredPointIndex = -1;
        
        public InteractiveChartPanel(ChartDataset dataset) {
            this.dataset = dataset;
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
            
            // Add mouse listeners for interactivity
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePosition = e.getPoint();
                    updateHoveredPoint();
                    repaint();
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleChartClick(e);
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Enable anti-aliasing for smooth graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            try {
                drawChart(g2d);
            } finally {
                g2d.dispose();
            }
        }
        
        private void drawChart(Graphics2D g2d) {
            if (dataset == null || dataset.getDataPoints().isEmpty()) {
                drawEmptyChart(g2d);
                return;
            }
            
            // Calculate chart area
            int margin = 80;
            int width = getWidth() - 2 * margin;
            int height = getHeight() - 2 * margin;
            Rectangle2D chartArea = new Rectangle2D.Double(margin, margin, width, height);
            
            // Draw background
            g2d.setColor(new Color(248, 249, 250));
            g2d.fill(chartArea);
            g2d.setColor(new Color(229, 231, 235));
            g2d.draw(chartArea);
            
            // Draw grid if enabled
            if (showGrid) {
                drawGrid(g2d, chartArea);
            }
            
            // Draw chart based on type
            switch (dataset.getChartType()) {
                case GPA_TREND_LINE:
                case ASSIGNMENT_TRENDS:
                    drawLineChart(g2d, chartArea);
                    break;
                case GRADE_DISTRIBUTION:
                case SEMESTER_COMPARISON:
                    drawBarChart(g2d, chartArea);
                    break;
                case COURSE_DIFFICULTY:
                    drawScatterChart(g2d, chartArea);
                    break;
                case ACHIEVEMENT_PROGRESS:
                    drawProgressChart(g2d, chartArea);
                    break;
            }
            
            // Draw axes and labels
            drawAxes(g2d, chartArea);
            
            // Draw legend if enabled
            if (showLegend) {
                drawLegend(g2d);
            }
            
            // Draw tooltip if hovering
            if (showTooltips && hoveredPointIndex >= 0) {
                drawTooltip(g2d);
            }
        }
        
        private void drawLineChart(Graphics2D g2d, Rectangle2D chartArea) {
            List<ChartDataPoint> points = dataset.getDataPoints();
            if (points.size() < 2) return;
            
            // Calculate scales
            double maxValue = points.stream().mapToDouble(ChartDataPoint::getValue).max().orElse(100);
            double minValue = points.stream().mapToDouble(ChartDataPoint::getValue).min().orElse(0);
            double valueRange = maxValue - minValue;
            if (valueRange == 0) valueRange = 1;
            
            // Create path for line
            GeneralPath linePath = new GeneralPath();
            boolean firstPoint = true;
            
            for (int i = 0; i < points.size(); i++) {
                ChartDataPoint point = points.get(i);
                double x = chartArea.getX() + (i * chartArea.getWidth() / (points.size() - 1));
                double y = chartArea.getY() + chartArea.getHeight() - 
                          ((point.getValue() - minValue) / valueRange * chartArea.getHeight() * animationProgress);
                
                if (firstPoint) {
                    linePath.moveTo(x, y);
                    firstPoint = false;
                } else {
                    linePath.lineTo(x, y);
                }
                
                // Draw data points
                g2d.setColor(point.getColor());
                Ellipse2D dataPoint = new Ellipse2D.Double(x - 4, y - 4, 8, 8);
                g2d.fill(dataPoint);
                
                // Highlight hovered point
                if (i == hoveredPointIndex) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(dataPoint);
                }
            }
            
            // Draw line
            g2d.setColor(dataset.getPrimaryColor());
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(linePath);
        }
        
        private void drawBarChart(Graphics2D g2d, Rectangle2D chartArea) {
            List<ChartDataPoint> points = dataset.getDataPoints();
            if (points.isEmpty()) return;
            
            double maxValue = points.stream().mapToDouble(ChartDataPoint::getValue).max().orElse(100);
            double barWidth = chartArea.getWidth() / points.size() * 0.8;
            double spacing = chartArea.getWidth() / points.size() * 0.2;
            
            for (int i = 0; i < points.size(); i++) {
                ChartDataPoint point = points.get(i);
                double barHeight = (point.getValue() / maxValue) * chartArea.getHeight() * animationProgress;
                
                double x = chartArea.getX() + (i * (barWidth + spacing)) + spacing / 2;
                double y = chartArea.getY() + chartArea.getHeight() - barHeight;
                
                Rectangle2D bar = new Rectangle2D.Double(x, y, barWidth, barHeight);
                
                // Create gradient for bars
                GradientPaint gradient = new GradientPaint(
                    (float)x, (float)y, point.getColor().brighter(),
                    (float)x, (float)(y + barHeight), point.getColor().darker()
                );
                g2d.setPaint(gradient);
                g2d.fill(bar);
                
                // Outline
                g2d.setColor(point.getColor().darker());
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(bar);
                
                // Highlight hovered bar
                if (i == hoveredPointIndex) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.draw(bar);
                }
            }
        }
        
        private void drawScatterChart(Graphics2D g2d, Rectangle2D chartArea) {
            List<ChartDataPoint> points = dataset.getDataPoints();
            if (points.isEmpty()) return;
            
            double maxValue = points.stream().mapToDouble(ChartDataPoint::getValue).max().orElse(100);
            double minValue = points.stream().mapToDouble(ChartDataPoint::getValue).min().orElse(0);
            double valueRange = maxValue - minValue;
            if (valueRange == 0) valueRange = 1;
            
            for (int i = 0; i < points.size(); i++) {
                ChartDataPoint point = points.get(i);
                double x = chartArea.getX() + (i * chartArea.getWidth() / (points.size() - 1));
                double y = chartArea.getY() + chartArea.getHeight() - 
                          ((point.getValue() - minValue) / valueRange * chartArea.getHeight());
                
                // Draw scatter point with size based on value
                int pointSize = (int) (8 + (point.getValue() / maxValue) * 12);
                Ellipse2D scatter = new Ellipse2D.Double(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
                
                g2d.setColor(point.getColor());
                g2d.fill(scatter);
                
                // Outline
                g2d.setColor(point.getColor().darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(scatter);
                
                // Highlight hovered point
                if (i == hoveredPointIndex) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.draw(scatter);
                }
            }
        }
        
        private void drawProgressChart(Graphics2D g2d, Rectangle2D chartArea) {
            List<ChartDataPoint> points = dataset.getDataPoints();
            if (points.isEmpty()) return;
            
            double barHeight = chartArea.getHeight() / points.size() * 0.6;
            double spacing = chartArea.getHeight() / points.size() * 0.4;
            
            for (int i = 0; i < points.size(); i++) {
                ChartDataPoint point = points.get(i);
                double progressWidth = (point.getValue() / 100.0) * chartArea.getWidth() * animationProgress;
                
                double x = chartArea.getX();
                double y = chartArea.getY() + (i * (barHeight + spacing));
                
                // Background bar
                Rectangle2D backgroundBar = new Rectangle2D.Double(x, y, chartArea.getWidth(), barHeight);
                g2d.setColor(new Color(229, 231, 235));
                g2d.fill(backgroundBar);
                
                // Progress bar
                Rectangle2D progressBar = new Rectangle2D.Double(x, y, progressWidth, barHeight);
                g2d.setColor(point.getColor());
                g2d.fill(progressBar);
                
                // Progress text
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String progressText = String.format("%.1f%%", point.getValue());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (int)(x + progressWidth + 5);
                int textY = (int)(y + barHeight/2 + fm.getAscent()/2);
                g2d.drawString(progressText, textX, textY);
                
                // Highlight hovered bar
                if (i == hoveredPointIndex) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.draw(backgroundBar);
                }
            }
        }
        
        private void drawGrid(Graphics2D g2d, Rectangle2D chartArea) {
            g2d.setColor(new Color(229, 231, 235, 128));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                          10.0f, new float[]{5.0f}, 0.0f));
            
            // Vertical grid lines
            for (int i = 0; i <= 10; i++) {
                double x = chartArea.getX() + (i * chartArea.getWidth() / 10);
                g2d.draw(new Line2D.Double(x, chartArea.getY(), x, chartArea.getY() + chartArea.getHeight()));
            }
            
            // Horizontal grid lines
            for (int i = 0; i <= 10; i++) {
                double y = chartArea.getY() + (i * chartArea.getHeight() / 10);
                g2d.draw(new Line2D.Double(chartArea.getX(), y, chartArea.getX() + chartArea.getWidth(), y));
            }
        }
        
        private void drawAxes(Graphics2D g2d, Rectangle2D chartArea) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            
            // X-axis
            g2d.draw(new Line2D.Double(chartArea.getX(), chartArea.getY() + chartArea.getHeight(),
                                       chartArea.getX() + chartArea.getWidth(), chartArea.getY() + chartArea.getHeight()));
            
            // Y-axis
            g2d.draw(new Line2D.Double(chartArea.getX(), chartArea.getY(),
                                       chartArea.getX(), chartArea.getY() + chartArea.getHeight()));
            
            // Axis labels
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            
            // X-axis label
            String xLabel = dataset.getChartType().getXLabel();
            int xLabelX = (int)(chartArea.getX() + chartArea.getWidth()/2 - fm.stringWidth(xLabel)/2);
            int xLabelY = getHeight() - 20;
            g2d.drawString(xLabel, xLabelX, xLabelY);
            
            // Y-axis label (rotated)
            String yLabel = dataset.getChartType().getYLabel();
            AffineTransform originalTransform = g2d.getTransform();
            g2d.rotate(-Math.PI/2);
            int yLabelX = -(int)(chartArea.getY() + chartArea.getHeight()/2 + fm.stringWidth(yLabel)/2);
            int yLabelY = 30;
            g2d.drawString(yLabel, yLabelX, yLabelY);
            g2d.setTransform(originalTransform);
            
            // Title
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.setColor(new Color(139, 0, 0));
            String title = dataset.getChartType().getTitle();
            fm = g2d.getFontMetrics();
            int titleX = (int)(chartArea.getX() + chartArea.getWidth()/2 - fm.stringWidth(title)/2);
            g2d.drawString(title, titleX, 30);
        }
        
        private void drawLegend(Graphics2D g2d) {
            // Simple legend for dataset name
            g2d.setColor(Color.WHITE);
            g2d.fillRect(getWidth() - 200, 50, 180, 80);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(getWidth() - 200, 50, 180, 80);
            
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Dataset:", getWidth() - 190, 70);
            g2d.setColor(dataset.getPrimaryColor());
            g2d.fillRect(getWidth() - 190, 80, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString(dataset.getName(), getWidth() - 170, 92);
        }
        
        private void drawTooltip(Graphics2D g2d) {
            if (hoveredPointIndex < 0 || hoveredPointIndex >= dataset.getDataPoints().size()) return;
            
            ChartDataPoint point = dataset.getDataPoints().get(hoveredPointIndex);
            String tooltipText = String.format("%s: %.2f", point.getLabel(), point.getValue());
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            
            int tooltipWidth = fm.stringWidth(tooltipText) + 10;
            int tooltipHeight = fm.getHeight() + 5;
            
            int tooltipX = mousePosition.x + 10;
            int tooltipY = mousePosition.y - tooltipHeight - 5;
            
            // Ensure tooltip stays within bounds
            if (tooltipX + tooltipWidth > getWidth()) {
                tooltipX = mousePosition.x - tooltipWidth - 10;
            }
            if (tooltipY < 0) {
                tooltipY = mousePosition.y + 15;
            }
            
            // Draw tooltip background
            g2d.setColor(new Color(255, 255, 255, 240));
            g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
            
            // Draw tooltip text
            g2d.drawString(tooltipText, tooltipX + 5, tooltipY + fm.getAscent() + 2);
        }
        
        private void drawEmptyChart(Graphics2D g2d) {
            g2d.setColor(new Color(156, 163, 175));
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            FontMetrics fm = g2d.getFontMetrics();
            
            String message = "No data available";
            int x = getWidth()/2 - fm.stringWidth(message)/2;
            int y = getHeight()/2;
            
            g2d.drawString(message, x, y);
        }
        
        private void updateHoveredPoint() {
            if (mousePosition == null || dataset.getDataPoints().isEmpty()) {
                hoveredPointIndex = -1;
                return;
            }
            
            // Simple hit detection - find closest point
            int margin = 80;
            Rectangle2D chartArea = new Rectangle2D.Double(margin, margin, 
                                                          getWidth() - 2 * margin, getHeight() - 2 * margin);
            
            double minDistance = Double.MAX_VALUE;
            int closestIndex = -1;
            
            for (int i = 0; i < dataset.getDataPoints().size(); i++) {
                double x = chartArea.getX() + (i * chartArea.getWidth() / Math.max(1, dataset.getDataPoints().size() - 1));
                double distance = Math.abs(mousePosition.x - x);
                
                if (distance < minDistance && distance < 20) { // 20px threshold
                    minDistance = distance;
                    closestIndex = i;
                }
            }
            
            hoveredPointIndex = closestIndex;
        }
        
        private void handleChartClick(MouseEvent e) {
            if (hoveredPointIndex >= 0) {
                ChartDataPoint point = dataset.getDataPoints().get(hoveredPointIndex);
                String details = String.format("Point Details:\nLabel: %s\nValue: %.2f\nDate: %s",
                    point.getLabel(), point.getValue(), point.getDate().toString());
                
                JOptionPane.showMessageDialog(this, details, "Data Point Details", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        // Animation methods
        public void animateIn() {
            if (!isAnimated) return;
            
            javax.swing.Timer timer = new javax.swing.Timer(16, null); // ~60 FPS
            timer.addActionListener(new ActionListener() {
                double progress = 0.0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    progress += 0.05;
                    if (progress >= 1.0) {
                        progress = 1.0;
                        timer.stop();
                    }
                    
                    animationProgress = easeOutQuart(progress);
                    repaint();
                }
            });
            
            animationProgress = 0.0;
            timer.start();
        }
        
        private double easeOutQuart(double t) {
            return 1 - Math.pow(1 - t, 4);
        }
        
        // Setters for configuration
        public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; repaint(); }
        public void setShowLegend(boolean showLegend) { this.showLegend = showLegend; repaint(); }
        public void setShowTooltips(boolean showTooltips) { this.showTooltips = showTooltips; }
        public void setAnimated(boolean animated) { this.isAnimated = animated; }
        
        // Getters
        public ChartDataset getDataset() { return dataset; }
        public boolean isShowGrid() { return showGrid; }
        public boolean isShowLegend() { return showLegend; }
        public boolean isShowTooltips() { return showTooltips; }
        public boolean isAnimated() { return isAnimated; }
    }
    
    // Factory methods for generating common chart types
    
    public static ChartDataset generateGPATrendData(User user) {
        ChartDataset dataset = new ChartDataset("GPA Trend", ChartType.GPA_TREND_LINE);
        
        // Generate GPA trend data from user's semester history
        List<Semester> semesters = user.getAllSemesters();
        semesters.sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
        
        for (Semester semester : semesters) {
            double gpa = semester.calculateGPA();
            dataset.addDataPoint(semester.getName(), gpa);
        }
        
        return dataset;
    }
    
    public static ChartDataset generateGradeDistributionData(Course course) {
        ChartDataset dataset = new ChartDataset("Grade Distribution", ChartType.GRADE_DISTRIBUTION);
        
        // Count assignments by grade ranges
        Map<String, Integer> gradeRanges = new LinkedHashMap<>();
        gradeRanges.put("A (90-100%)", 0);
        gradeRanges.put("B (80-89%)", 0);
        gradeRanges.put("C (70-79%)", 0);
        gradeRanges.put("D (60-69%)", 0);
        gradeRanges.put("F (0-59%)", 0);
        
        for (Assignment assignment : course.getAllAssignments()) {
            double grade = assignment.getGradePercent();
            if (grade >= 90) gradeRanges.put("A (90-100%)", gradeRanges.get("A (90-100%)") + 1);
            else if (grade >= 80) gradeRanges.put("B (80-89%)", gradeRanges.get("B (80-89%)") + 1);
            else if (grade >= 70) gradeRanges.put("C (70-79%)", gradeRanges.get("C (70-79%)") + 1);
            else if (grade >= 60) gradeRanges.put("D (60-69%)", gradeRanges.get("D (60-69%)") + 1);
            else gradeRanges.put("F (0-59%)", gradeRanges.get("F (0-59%)") + 1);
        }
        
        for (Map.Entry<String, Integer> entry : gradeRanges.entrySet()) {
            dataset.addDataPoint(entry.getKey(), entry.getValue());
        }
        
        return dataset;
    }
    
    public static ChartDataset generateCourseDifficultyData(User user) {
        ChartDataset dataset = new ChartDataset("Course Difficulty", ChartType.COURSE_DIFFICULTY);
        
        for (Semester semester : user.getAllSemesters()) {
            for (Course course : semester.getAllCourses()) {
                double difficultyScore = calculateCourseDifficulty(course);
                ChartDataPoint point = new ChartDataPoint(course.getName(), difficultyScore);
                dataset.addDataPoint(point);
            }
        }
        
        return dataset;
    }
    
    public static ChartDataset generateAchievementProgressData(User user) {
        ChartDataset dataset = new ChartDataset("Achievement Progress", ChartType.ACHIEVEMENT_PROGRESS);
        
        // Calculate various achievement metrics
        double gpaGoalProgress = calculateGPAGoalProgress(user);
        double assignmentCompletionRate = calculateAssignmentCompletionRate(user);
        double improvementTrend = calculateImprovementTrend(user);
        double consistencyScore = calculateConsistencyScore(user);
        
        dataset.addDataPoint("GPA Goal", gpaGoalProgress);
        dataset.addDataPoint("Completion Rate", assignmentCompletionRate);
        dataset.addDataPoint("Improvement", improvementTrend);
        dataset.addDataPoint("Consistency", consistencyScore);
        
        return dataset;
    }
    
    public static ChartDataset generateSemesterComparisonData(User user) {
        ChartDataset dataset = new ChartDataset("Semester Comparison", ChartType.SEMESTER_COMPARISON);
        
        for (Semester semester : user.getAllSemesters()) {
            double gpa = semester.calculateGPA();
            dataset.addDataPoint(semester.getName(), gpa);
        }
        
        return dataset;
    }
    
    public static ChartDataset generateAssignmentTrendData(Course course) {
        ChartDataset dataset = new ChartDataset("Assignment Trends", ChartType.ASSIGNMENT_TRENDS);
        
        List<Assignment> assignments = new ArrayList<>(course.getAllAssignments());
        assignments.sort((a1, a2) -> a1.getDateAdded().compareTo(a2.getDateAdded()));
        
        for (Assignment assignment : assignments) {
            dataset.addDataPoint(assignment.getName(), assignment.getGradePercent());
        }
        
        return dataset;
    }
    
    // Utility calculation methods
    
    private static double calculateCourseDifficulty(Course course) {
        List<Assignment> assignments = course.getAllAssignments();
        if (assignments.isEmpty()) return 50.0;
        
        double averageGrade = assignments.stream()
            .mapToDouble(Assignment::getGradePercent)
            .average().orElse(0.0);
        
        // Calculate standard deviation
        double variance = assignments.stream()
            .mapToDouble(a -> Math.pow(a.getGradePercent() - averageGrade, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // Difficulty score: lower average + higher variance = more difficult
        double difficultyScore = (100 - averageGrade) + (stdDev * 0.5);
        return Math.min(100, Math.max(0, difficultyScore));
    }
    
    private static double calculateGPAGoalProgress(User user) {
        // Assume target GPA of 3.5 for calculation
        double targetGPA = 3.5;
        double currentGPA = user.calculateOverallGPA();
        return Math.min(100, (currentGPA / targetGPA) * 100);
    }
    
    private static double calculateAssignmentCompletionRate(User user) {
        int totalAssignments = 0;
        int completedAssignments = 0;
        
        for (Semester semester : user.getAllSemesters()) {
            for (Course course : semester.getAllCourses()) {
                totalAssignments += course.getAllAssignments().size();
                completedAssignments += course.getAllAssignments().size(); // Assume all recorded assignments are completed
            }
        }
        
        return totalAssignments > 0 ? (completedAssignments * 100.0 / totalAssignments) : 0.0;
    }
    
    private static double calculateImprovementTrend(User user) {
        List<Semester> semesters = user.getAllSemesters();
        if (semesters.size() < 2) return 50.0;
        
        semesters.sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
        double firstGPA = semesters.get(0).calculateGPA();
        double lastGPA = semesters.get(semesters.size() - 1).calculateGPA();
        
        double improvement = ((lastGPA - firstGPA) / 4.0) * 100; // Normalize to percentage
        return Math.min(100, Math.max(0, 50 + improvement * 10));
    }
    
    private static double calculateConsistencyScore(User user) {
        List<Double> semesterGPAs = user.getAllSemesters().stream()
            .mapToDouble(Semester::calculateGPA)
            .boxed()
            .collect(java.util.stream.Collectors.toList());
        
        if (semesterGPAs.size() < 2) return 100.0;
        
        double mean = semesterGPAs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = semesterGPAs.stream()
            .mapToDouble(gpa -> Math.pow(gpa - mean, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // Lower standard deviation = higher consistency
        return Math.max(0, 100 - (stdDev * 25));
    }
}