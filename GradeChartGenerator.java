import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;  // Explicitly import util.List to avoid ambiguity

/**
 * Advanced Chart Generator for GradeRise Analytics
 * Creates beautiful, interactive charts for grade visualization and analysis
 */
public class GradeChartGenerator {
    
    // Chart Types
    public enum ChartType {
        LINE_CHART,
        BAR_CHART,
        PIE_CHART,
        SCATTER_PLOT,
        HISTOGRAM,
        BOX_PLOT
    }
    
    // GradeRise Color Palette
    private static final Color PRIMARY_DARK = new Color(0x8B0000); // Dark Red
    private static final Color ACCENT_LIGHT = new Color(0xDC143C); // Crimson
    private static final Color BACKGROUND_DARK = new Color(0x1A1A1A); // Dark Gray
    private static final Color SURFACE_DARK = new Color(0x2D2D2D); // Medium Gray
    private static final Color TEXT_PRIMARY = new Color(0xFFFFFF); // White
    private static final Color TEXT_SECONDARY = new Color(0xB0B0B0); // Light Gray
    private static final Color SUCCESS_GREEN = new Color(0x10B981); // Emerald
    private static final Color WARNING_ORANGE = new Color(0xF59E0B); // Amber
    private static final Color ERROR_RED = new Color(0xEF4444); // Red
    
    /**
     * Custom Chart Panel with Modern Styling
     */
    public static class ModernChartPanel extends JPanel {
        private final String title;
        private final String subtitle;
        private final ChartType chartType;
        private final List<DataPoint> data;
        private final Map<String, Object> options;
        
        public ModernChartPanel(String title, String subtitle, ChartType type, 
                               List<DataPoint> data, Map<String, Object> options) {
            this.title = title;
            this.subtitle = subtitle;
            this.chartType = type;
            this.data = new ArrayList<>(data);
            this.options = new HashMap<>(options);
            
            setBackground(SURFACE_DARK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK.darker(), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            setPreferredSize(new Dimension(600, 400));
        }
        
        // Getter for chart options (makes the options field useful)
        public Map<String, Object> getOptions() {
            return new HashMap<>(options);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            drawHeader(g2);
            drawChart(g2);
            
            g2.dispose();
        }
        
        private void drawHeader(Graphics2D g2) {
            int y = 30;
            
            // Title
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString(title, 10, y);
            
            // Subtitle
            if (subtitle != null && !subtitle.isEmpty()) {
                y += 25;
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(subtitle, 10, y);
            }
        }
        
        private void drawChart(Graphics2D g2) {
            if (data.isEmpty()) {
                drawNoDataMessage(g2);
                return;
            }
            
            Rectangle chartArea = new Rectangle(50, 80, getWidth() - 100, getHeight() - 120);
            
            switch (chartType) {
                case LINE_CHART:
                    drawLineChart(g2, chartArea);
                    break;
                case BAR_CHART:
                    drawBarChart(g2, chartArea);
                    break;
                case PIE_CHART:
                    drawPieChart(g2, chartArea);
                    break;
                case SCATTER_PLOT:
                    drawScatterPlot(g2, chartArea);
                    break;
                case HISTOGRAM:
                    drawHistogram(g2, chartArea);
                    break;
                case BOX_PLOT:
                    drawBoxPlot(g2, chartArea);
                    break;
            }
            
            drawLegend(g2);
        }
        
        private void drawLineChart(Graphics2D g2, Rectangle area) {
            drawAxes(g2, area);
            
            // Calculate scale
            double minY = data.stream().mapToDouble(DataPoint::getY).min().orElse(0);
            double maxY = data.stream().mapToDouble(DataPoint::getY).max().orElse(100);
            double rangeY = maxY - minY;
            if (rangeY == 0) rangeY = 1;
            
            // Draw trend line
            g2.setColor(PRIMARY_DARK);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            Path2D path = new Path2D.Double();
            boolean first = true;
            
            for (int i = 0; i < data.size(); i++) {
                DataPoint point = data.get(i);
                double x = area.x + (double) i / (data.size() - 1) * area.width;
                double y = area.y + area.height - ((point.getY() - minY) / rangeY) * area.height;
                
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
                
                // Draw data points
                g2.setColor(ACCENT_LIGHT);
                g2.fillOval((int) x - 4, (int) y - 4, 8, 8);
                g2.setColor(TEXT_PRIMARY);
                g2.drawOval((int) x - 4, (int) y - 4, 8, 8);
            }
            
            g2.setColor(PRIMARY_DARK);
            g2.draw(path);
            
            // Add value labels
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i < data.size(); i++) {
                DataPoint point = data.get(i);
                double x = area.x + (double) i / (data.size() - 1) * area.width;
                double y = area.y + area.height - ((point.getY() - minY) / rangeY) * area.height;
                
                g2.setColor(TEXT_SECONDARY);
                String label = String.format("%.1f", point.getY());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (int) x - fm.stringWidth(label) / 2, (int) y - 10);
            }
        }
        
        private void drawBarChart(Graphics2D g2, Rectangle area) {
            drawAxes(g2, area);
            
            double maxY = data.stream().mapToDouble(DataPoint::getY).max().orElse(100);
            int barWidth = area.width / Math.max(1, data.size()) - 10;
            
            for (int i = 0; i < data.size(); i++) {
                DataPoint point = data.get(i);
                int x = area.x + i * (area.width / data.size()) + 5;
                int height = (int) ((point.getY() / maxY) * area.height);
                int y = area.y + area.height - height;
                
                // Color based on grade level
                Color barColor = getGradeColor(point.getY());
                
                // Draw bar with gradient effect
                GradientPaint gradient = new GradientPaint(x, y, barColor, x, y + height, barColor.darker());
                g2.setPaint(gradient);
                g2.fillRect(x, y, barWidth, height);
                
                // Draw border
                g2.setColor(barColor.darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(x, y, barWidth, height);
                
                // Draw value label
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String label = String.format("%.1f", point.getY());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x + barWidth / 2 - fm.stringWidth(label) / 2, y - 5);
                
                // Draw category label
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String category = point.getLabel();
                if (category.length() > 8) category = category.substring(0, 8) + "...";
                g2.drawString(category, x + barWidth / 2 - fm.stringWidth(category) / 2, area.y + area.height + 15);
            }
        }
        
        private void drawPieChart(Graphics2D g2, Rectangle area) {
            double total = data.stream().mapToDouble(DataPoint::getY).sum();
            if (total == 0) return;
            
            int centerX = area.x + area.width / 2;
            int centerY = area.y + area.height / 2;
            int radius = Math.min(area.width, area.height) / 2 - 20;
            
            double startAngle = 0;
            Color[] colors = {PRIMARY_DARK, ACCENT_LIGHT, SUCCESS_GREEN, WARNING_ORANGE, ERROR_RED};
            
            for (int i = 0; i < data.size(); i++) {
                DataPoint point = data.get(i);
                double percentage = point.getY() / total;
                double angle = percentage * 360;
                
                Color sliceColor = colors[i % colors.length];
                g2.setColor(sliceColor);
                g2.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                          (int) startAngle, (int) angle);
                
                // Draw border
                g2.setColor(BACKGROUND_DARK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                          (int) startAngle, (int) angle);
                
                // Draw labels
                if (percentage > 0.05) { // Only show labels for slices > 5%
                    double labelAngle = Math.toRadians(startAngle + angle / 2);
                    int labelX = centerX + (int) (Math.cos(labelAngle) * radius * 0.7);
                    int labelY = centerY + (int) (Math.sin(labelAngle) * radius * 0.7);
                    
                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    String label = String.format("%.1f%%", percentage * 100);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(label, labelX - fm.stringWidth(label) / 2, labelY);
                }
                
                startAngle += angle;
            }
        }
        
        private void drawScatterPlot(Graphics2D g2, Rectangle area) {
            drawAxes(g2, area);
            
            double minX = data.stream().mapToDouble(DataPoint::getX).min().orElse(0);
            double maxX = data.stream().mapToDouble(DataPoint::getX).max().orElse(1);
            double minY = data.stream().mapToDouble(DataPoint::getY).min().orElse(0);
            double maxY = data.stream().mapToDouble(DataPoint::getY).max().orElse(100);
            
            double rangeX = maxX - minX;
            double rangeY = maxY - minY;
            if (rangeX == 0) rangeX = 1;
            if (rangeY == 0) rangeY = 1;
            
            for (DataPoint point : data) {
                double x = area.x + ((point.getX() - minX) / rangeX) * area.width;
                double y = area.y + area.height - ((point.getY() - minY) / rangeY) * area.height;
                
                Color pointColor = getGradeColor(point.getY());
                g2.setColor(pointColor);
                g2.fillOval((int) x - 5, (int) y - 5, 10, 10);
                g2.setColor(pointColor.darker());
                g2.drawOval((int) x - 5, (int) y - 5, 10, 10);
            }
        }
        
        private void drawHistogram(Graphics2D g2, Rectangle area) {
            // Create bins for grade ranges
            Map<String, Integer> bins = new HashMap<>();
            bins.put("90-100", 0);
            bins.put("80-89", 0);
            bins.put("70-79", 0);
            bins.put("60-69", 0);
            bins.put("0-59", 0);
            
            for (DataPoint point : data) {
                double grade = point.getY();
                if (grade >= 90) bins.put("90-100", bins.get("90-100") + 1);
                else if (grade >= 80) bins.put("80-89", bins.get("80-89") + 1);
                else if (grade >= 70) bins.put("70-79", bins.get("70-79") + 1);
                else if (grade >= 60) bins.put("60-69", bins.get("60-69") + 1);
                else bins.put("0-59", bins.get("0-59") + 1);
            }
            
            int maxCount = bins.values().stream().max(Integer::compare).orElse(1);
            String[] binLabels = {"90-100", "80-89", "70-79", "60-69", "0-59"};
            Color[] binColors = {SUCCESS_GREEN, PRIMARY_DARK, WARNING_ORANGE, ACCENT_LIGHT, ERROR_RED};
            
            int binWidth = area.width / binLabels.length - 10;
            
            for (int i = 0; i < binLabels.length; i++) {
                String bin = binLabels[i];
                int count = bins.get(bin);
                int x = area.x + i * (area.width / binLabels.length) + 5;
                int height = (int) ((double) count / maxCount * area.height);
                int y = area.y + area.height - height;
                
                g2.setColor(binColors[i]);
                g2.fillRect(x, y, binWidth, height);
                g2.setColor(binColors[i].darker());
                g2.drawRect(x, y, binWidth, height);
                
                // Draw count label
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String countLabel = String.valueOf(count);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(countLabel, x + binWidth / 2 - fm.stringWidth(countLabel) / 2, y - 5);
                
                // Draw bin label
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(bin, x + binWidth / 2 - fm.stringWidth(bin) / 2, area.y + area.height + 15);
            }
            
            drawAxes(g2, area);
        }
        
        private void drawBoxPlot(Graphics2D g2, Rectangle area) {
            List<Double> values = data.stream().mapToDouble(DataPoint::getY).sorted().boxed().toList();
            if (values.isEmpty()) return;
            
            // Calculate quartiles
            double q1 = calculateQuartile(values, 0.25);
            double median = calculateQuartile(values, 0.5);
            double q3 = calculateQuartile(values, 0.75);
            double min = values.get(0);
            double max = values.get(values.size() - 1);
            
            double range = max - min;
            if (range == 0) range = 1;
            
            int centerY = area.y + area.height / 2;
            int boxHeight = 60;
            
            // Scale values to area
            int minX = area.x + (int) ((min - min) / range * area.width);
            int q1X = area.x + (int) ((q1 - min) / range * area.width);
            int medianX = area.x + (int) ((median - min) / range * area.width);
            int q3X = area.x + (int) ((q3 - min) / range * area.width);
            int maxX = area.x + (int) ((max - min) / range * area.width);
            
            // Draw whiskers
            g2.setColor(TEXT_PRIMARY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(minX, centerY, q1X, centerY); // Left whisker
            g2.drawLine(q3X, centerY, maxX, centerY); // Right whisker
            g2.drawLine(minX, centerY - 10, minX, centerY + 10); // Left cap
            g2.drawLine(maxX, centerY - 10, maxX, centerY + 10); // Right cap
            
            // Draw box
            g2.setColor(SURFACE_DARK);
            g2.fillRect(q1X, centerY - boxHeight/2, q3X - q1X, boxHeight);
            g2.setColor(PRIMARY_DARK);
            g2.drawRect(q1X, centerY - boxHeight/2, q3X - q1X, boxHeight);
            
            // Draw median line
            g2.setColor(ACCENT_LIGHT);
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(medianX, centerY - boxHeight/2, medianX, centerY + boxHeight/2);
            
            // Draw value labels
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(String.format("%.1f", min), minX - 10, centerY + 35);
            g2.drawString(String.format("%.1f", q1), q1X - 10, centerY + 35);
            g2.drawString(String.format("%.1f", median), medianX - 10, centerY - 35);
            g2.drawString(String.format("%.1f", q3), q3X - 10, centerY + 35);
            g2.drawString(String.format("%.1f", max), maxX - 10, centerY + 35);
        }
        
        private void drawAxes(Graphics2D g2, Rectangle area) {
            g2.setColor(TEXT_SECONDARY);
            g2.setStroke(new BasicStroke(1f));
            
            // X-axis
            g2.drawLine(area.x, area.y + area.height, area.x + area.width, area.y + area.height);
            
            // Y-axis
            g2.drawLine(area.x, area.y, area.x, area.y + area.height);
            
            // Grid lines
            g2.setColor(new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), 
                                TEXT_SECONDARY.getBlue(), 50));
            
            for (int i = 1; i < 5; i++) {
                int y = area.y + (area.height * i / 5);
                g2.drawLine(area.x, y, area.x + area.width, y);
            }
        }
        
        private void drawLegend(Graphics2D g2) {
            if (chartType == ChartType.PIE_CHART && data.size() > 1) {
                int legendX = getWidth() - 150;
                int legendY = 100;
                
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                Color[] colors = {PRIMARY_DARK, ACCENT_LIGHT, SUCCESS_GREEN, WARNING_ORANGE, ERROR_RED};
                
                for (int i = 0; i < Math.min(data.size(), colors.length); i++) {
                    DataPoint point = data.get(i);
                    Color color = colors[i];
                    
                    g2.setColor(color);
                    g2.fillRect(legendX, legendY + i * 20, 15, 15);
                    g2.setColor(TEXT_PRIMARY);
                    g2.drawString(point.getLabel(), legendX + 20, legendY + i * 20 + 12);
                }
            }
        }
        
        private void drawNoDataMessage(Graphics2D g2) {
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String message = "No data available to display";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
        }
        
        private Color getGradeColor(double grade) {
            if (grade >= 90) return SUCCESS_GREEN;
            else if (grade >= 80) return PRIMARY_DARK;
            else if (grade >= 70) return WARNING_ORANGE;
            else if (grade >= 60) return ACCENT_LIGHT;
            else return ERROR_RED;
        }
        
        private double calculateQuartile(List<Double> sortedValues, double percentile) {
            int n = sortedValues.size();
            double index = percentile * (n - 1);
            int lowerIndex = (int) Math.floor(index);
            int upperIndex = (int) Math.ceil(index);
            
            if (lowerIndex == upperIndex) {
                return sortedValues.get(lowerIndex);
            } else {
                double weight = index - lowerIndex;
                return sortedValues.get(lowerIndex) * (1 - weight) + sortedValues.get(upperIndex) * weight;
            }
        }
    }
    
    /**
     * Data Point for Charts
     */
    public static class DataPoint {
        private final double x;
        private final double y;
        private final String label;
        private final Object metadata;
        
        public DataPoint(double x, double y, String label) {
            this(x, y, label, null);
        }
        
        public DataPoint(double x, double y, String label, Object metadata) {
            this.x = x;
            this.y = y;
            this.label = label;
            this.metadata = metadata;
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public String getLabel() { return label; }
        public Object getMetadata() { return metadata; }
    }
    
    /**
     * Factory Methods for Creating Charts
     */
    public static ModernChartPanel createGradeTrendChart(Course course) {
        List<Assignment> assignments = course.getAllAssignments();
        assignments.sort((a, b) -> a.getDateAdded().compareTo(b.getDateAdded()));
        
        List<DataPoint> data = new ArrayList<>();
        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);
            data.add(new DataPoint(i + 1, assignment.getGradePercent(), assignment.getName()));
        }
        
        Map<String, Object> options = new HashMap<>();
        options.put("showTrendLine", true);
        options.put("showDataLabels", true);
        
        return new ModernChartPanel(
            course.getName() + " - Grade Trend",
            "Performance over time",
            ChartType.LINE_CHART,
            data,
            options
        );
    }
    
    public static ModernChartPanel createGradeDistributionChart(Course course) {
        List<Assignment> assignments = course.getAllAssignments();
        List<DataPoint> data = new ArrayList<>();
        
        for (Assignment assignment : assignments) {
            data.add(new DataPoint(0, assignment.getGradePercent(), assignment.getName()));
        }
        
        return new ModernChartPanel(
            course.getName() + " - Grade Distribution",
            "Histogram of all grades",
            ChartType.HISTOGRAM,
            data,
            new HashMap<>()
        );
    }
    
    public static ModernChartPanel createCategoryPerformanceChart(Course course) {
        List<Assignment> allAssignments = course.getAllAssignments();
        Map<String, List<Assignment>> byCategory = allAssignments.stream()
            .collect(java.util.stream.Collectors.groupingBy(Assignment::getCategory));
        
        List<DataPoint> data = new ArrayList<>();
        for (Map.Entry<String, List<Assignment>> entry : byCategory.entrySet()) {
            double average = entry.getValue().stream()
                .mapToDouble(Assignment::getGradePercent)
                .average()
                .orElse(0.0);
            data.add(new DataPoint(0, average, entry.getKey()));
        }
        
        return new ModernChartPanel(
            course.getName() + " - Category Performance",
            "Average grades by assignment type",
            ChartType.BAR_CHART,
            data,
            new HashMap<>()
        );
    }
    
    public static ModernChartPanel createSemesterComparisonChart(List<Semester> semesters) {
        List<DataPoint> data = new ArrayList<>();
        
        for (Semester semester : semesters) {
            double gpa = semester.calculateGPA();
            data.add(new DataPoint(0, gpa * 25, semester.getName())); // Scale GPA to percentage
        }
        
        return new ModernChartPanel(
            "Semester GPA Comparison",
            "Academic performance across semesters",
            ChartType.BAR_CHART,
            data,
            new HashMap<>()
        );
    }
    
    public static ModernChartPanel createGradeBoxPlot(Course course) {
        List<Assignment> assignments = course.getAllAssignments();
        List<DataPoint> data = new ArrayList<>();
        
        for (Assignment assignment : assignments) {
            data.add(new DataPoint(0, assignment.getGradePercent(), assignment.getName()));
        }
        
        return new ModernChartPanel(
            course.getName() + " - Grade Statistics",
            "Box plot showing quartiles and outliers",
            ChartType.BOX_PLOT,
            data,
            new HashMap<>()
        );
    }
}