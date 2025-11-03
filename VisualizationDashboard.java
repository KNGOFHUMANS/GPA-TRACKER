import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Interactive Data Visualization Dashboard for GradeRise
 * Provides advanced charts and analytics visualization
 */
public class VisualizationDashboard extends JFrame {
    
    private User currentUser;
    private JTabbedPane tabbedPane;
    private Map<String, DataVisualizationEngine.InteractiveChartPanel> chartPanels;
    
    // Dashboard Components
    private JPanel controlPanel;
    private JComboBox<ChartOption> chartSelector;
    private JCheckBox showGridBox;
    private JCheckBox showLegendBox;
    private JCheckBox showTooltipsBox;
    private JCheckBox animateBox;
    private JButton refreshButton;
    private JButton exportButton;
    private JButton settingsButton;
    
    public VisualizationDashboard(User user) {
        this.currentUser = user;
        this.chartPanels = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        loadInitialCharts();
        
        setTitle("GradeRise - Data Visualization Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Apply GradeRise styling
        applyGradeRiseTheme();
    }
    
    private void initializeComponents() {
        // Create tabbed pane for different chart categories
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        // Control panel
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Chart selector
        ChartOption[] chartOptions = {
            new ChartOption("GPA Trend Over Time", DataVisualizationEngine.ChartType.GPA_TREND_LINE),
            new ChartOption("Grade Distribution", DataVisualizationEngine.ChartType.GRADE_DISTRIBUTION),
            new ChartOption("Course Difficulty Analysis", DataVisualizationEngine.ChartType.COURSE_DIFFICULTY),
            new ChartOption("Achievement Progress", DataVisualizationEngine.ChartType.ACHIEVEMENT_PROGRESS),
            new ChartOption("Semester Comparison", DataVisualizationEngine.ChartType.SEMESTER_COMPARISON),
            new ChartOption("Assignment Trends", DataVisualizationEngine.ChartType.ASSIGNMENT_TRENDS)
        };
        
        chartSelector = new JComboBox<>(chartOptions);
        chartSelector.addActionListener(e -> refreshCurrentChart());
        
        // Control checkboxes
        showGridBox = new JCheckBox("Show Grid", true);
        showGridBox.addActionListener(e -> updateChartSettings());
        
        showLegendBox = new JCheckBox("Show Legend", true);
        showLegendBox.addActionListener(e -> updateChartSettings());
        
        showTooltipsBox = new JCheckBox("Show Tooltips", true);
        showTooltipsBox.addActionListener(e -> updateChartSettings());
        
        animateBox = new JCheckBox("Animate", true);
        animateBox.addActionListener(e -> updateChartSettings());
        
        // Action buttons
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> refreshAllCharts());
        
        exportButton = new JButton("💾 Export");
        exportButton.addActionListener(e -> exportCurrentChart());
        
        settingsButton = new JButton("⚙️ Settings");
        settingsButton.addActionListener(e -> showSettings());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add controls to control panel
        controlPanel.add(new JLabel("Chart Type:"));
        controlPanel.add(chartSelector);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(showGridBox);
        controlPanel.add(showLegendBox);
        controlPanel.add(showTooltipsBox);
        controlPanel.add(animateBox);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(refreshButton);
        controlPanel.add(exportButton);
        controlPanel.add(settingsButton);
        
        // Add components to frame
        add(controlPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(new JLabel("Ready • Charts: Interactive • Data: Live"));
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void loadInitialCharts() {
        // Create all chart tabs
        createGPATrendTab();
        createGradeDistributionTab();
        createCourseDifficultyTab();
        createAchievementProgressTab();
        createSemesterComparisonTab();
        createAssignmentTrendsTab();
    }
    
    private void createGPATrendTab() {
        try {
            DataVisualizationEngine.ChartDataset dataset = 
                DataVisualizationEngine.generateGPATrendData(currentUser);
            DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                new DataVisualizationEngine.InteractiveChartPanel(dataset);
            
            JPanel containerPanel = createChartContainer(chartPanel, "GPA Trend Analysis");
            tabbedPane.addTab("📈 GPA Trend", containerPanel);
            chartPanels.put("GPA_TREND", chartPanel);
            
            // Animate after adding
            SwingUtilities.invokeLater(() -> chartPanel.animateIn());
            
        } catch (Exception e) {
            addErrorTab("GPA Trend", "Error loading GPA trend data: " + e.getMessage());
        }
    }
    
    private void createGradeDistributionTab() {
        try {
            // Create a panel with multiple course distributions
            JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            boolean hasData = false;
            for (Semester semester : currentUser.getAllSemesters()) {
                for (Course course : semester.getAllCourses()) {
                    if (!course.getAllAssignments().isEmpty()) {
                        DataVisualizationEngine.ChartDataset dataset = 
                            DataVisualizationEngine.generateGradeDistributionData(course);
                        DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                            new DataVisualizationEngine.InteractiveChartPanel(dataset);
                        chartPanel.setPreferredSize(new Dimension(400, 300));
                        
                        JPanel coursePanel = createMiniChartContainer(chartPanel, course.getName());
                        mainPanel.add(coursePanel);
                        hasData = true;
                        
                        SwingUtilities.invokeLater(() -> chartPanel.animateIn());
                    }
                }
            }
            
            if (!hasData) {
                mainPanel.add(new JLabel("No assignment data available", SwingConstants.CENTER));
            }
            
            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            tabbedPane.addTab("📊 Grade Distribution", scrollPane);
            
        } catch (Exception e) {
            addErrorTab("Grade Distribution", "Error loading grade distribution: " + e.getMessage());
        }
    }
    
    private void createCourseDifficultyTab() {
        try {
            DataVisualizationEngine.ChartDataset dataset = 
                DataVisualizationEngine.generateCourseDifficultyData(currentUser);
            DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                new DataVisualizationEngine.InteractiveChartPanel(dataset);
            
            JPanel containerPanel = createChartContainer(chartPanel, "Course Difficulty Analysis");
            tabbedPane.addTab("🎯 Difficulty", containerPanel);
            chartPanels.put("COURSE_DIFFICULTY", chartPanel);
            
            SwingUtilities.invokeLater(() -> chartPanel.animateIn());
            
        } catch (Exception e) {
            addErrorTab("Course Difficulty", "Error loading difficulty analysis: " + e.getMessage());
        }
    }
    
    private void createAchievementProgressTab() {
        try {
            DataVisualizationEngine.ChartDataset dataset = 
                DataVisualizationEngine.generateAchievementProgressData(currentUser);
            DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                new DataVisualizationEngine.InteractiveChartPanel(dataset);
            
            JPanel containerPanel = createChartContainer(chartPanel, "Achievement Progress Tracking");
            tabbedPane.addTab("🏆 Achievements", containerPanel);
            chartPanels.put("ACHIEVEMENT_PROGRESS", chartPanel);
            
            SwingUtilities.invokeLater(() -> chartPanel.animateIn());
            
        } catch (Exception e) {
            addErrorTab("Achievement Progress", "Error loading achievement data: " + e.getMessage());
        }
    }
    
    private void createSemesterComparisonTab() {
        try {
            DataVisualizationEngine.ChartDataset dataset = 
                DataVisualizationEngine.generateSemesterComparisonData(currentUser);
            DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                new DataVisualizationEngine.InteractiveChartPanel(dataset);
            
            JPanel containerPanel = createChartContainer(chartPanel, "Semester Performance Comparison");
            tabbedPane.addTab("📅 Semesters", containerPanel);
            chartPanels.put("SEMESTER_COMPARISON", chartPanel);
            
            SwingUtilities.invokeLater(() -> chartPanel.animateIn());
            
        } catch (Exception e) {
            addErrorTab("Semester Comparison", "Error loading semester data: " + e.getMessage());
        }
    }
    
    private void createAssignmentTrendsTab() {
        try {
            // Create a panel with assignment trends for each course
            JPanel mainPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            boolean hasData = false;
            for (Semester semester : currentUser.getAllSemesters()) {
                for (Course course : semester.getAllCourses()) {
                    if (course.getAllAssignments().size() > 1) {
                        DataVisualizationEngine.ChartDataset dataset = 
                            DataVisualizationEngine.generateAssignmentTrendData(course);
                        DataVisualizationEngine.InteractiveChartPanel chartPanel = 
                            new DataVisualizationEngine.InteractiveChartPanel(dataset);
                        chartPanel.setPreferredSize(new Dimension(800, 200));
                        
                        JPanel coursePanel = createMiniChartContainer(chartPanel, 
                            course.getName() + " Assignment Trends");
                        mainPanel.add(coursePanel);
                        hasData = true;
                        
                        SwingUtilities.invokeLater(() -> chartPanel.animateIn());
                    }
                }
            }
            
            if (!hasData) {
                mainPanel.add(new JLabel("No assignment trend data available", SwingConstants.CENTER));
            }
            
            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            tabbedPane.addTab("📋 Assignment Trends", scrollPane);
            
        } catch (Exception e) {
            addErrorTab("Assignment Trends", "Error loading assignment trends: " + e.getMessage());
        }
    }
    
    private JPanel createChartContainer(DataVisualizationEngine.InteractiveChartPanel chartPanel, String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title));
        
        container.add(chartPanel, BorderLayout.CENTER);
        
        // Add chart-specific controls
        JPanel chartControls = new JPanel(new FlowLayout());
        JButton fullscreenBtn = new JButton("⛶ Fullscreen");
        fullscreenBtn.addActionListener(e -> showFullscreenChart(chartPanel, title));
        
        JButton dataBtn = new JButton("📄 Data");
        dataBtn.addActionListener(e -> showChartData(chartPanel));
        
        chartControls.add(fullscreenBtn);
        chartControls.add(dataBtn);
        container.add(chartControls, BorderLayout.SOUTH);
        
        return container;
    }
    
    private JPanel createMiniChartContainer(DataVisualizationEngine.InteractiveChartPanel chartPanel, String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title));
        
        container.add(chartPanel, BorderLayout.CENTER);
        
        return container;
    }
    
    private void addErrorTab(String tabName, String errorMessage) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        JLabel errorLabel = new JLabel("<html><center>" + errorMessage + "</center></html>", 
                                      SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        
        JButton retryButton = new JButton("🔄 Retry");
        retryButton.addActionListener(e -> {
            // Remove error tab and retry loading
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTitleAt(i).contains(tabName)) {
                    tabbedPane.removeTabAt(i);
                    break;
                }
            }
            loadInitialCharts();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(retryButton);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("❌ " + tabName, errorPanel);
    }
    
    private void refreshCurrentChart() {
        // Refresh the currently selected chart based on chart selector
        ChartOption selected = (ChartOption) chartSelector.getSelectedItem();
        if (selected != null) {
            // Use the chartType from selected option for specific refresh logic
            DataVisualizationEngine.ChartType chartType = selected.getChartType();
            System.out.println("Refreshing chart type: " + chartType);
            refreshAllCharts();
        }
    }
    
    private void refreshAllCharts() {
        // Clear existing charts and reload
        tabbedPane.removeAll();
        chartPanels.clear();
        loadInitialCharts();
        
        JOptionPane.showMessageDialog(this, 
            "Charts refreshed successfully!", 
            "Refresh Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateChartSettings() {
        // Update settings for all chart panels
        for (DataVisualizationEngine.InteractiveChartPanel panel : chartPanels.values()) {
            panel.setShowGrid(showGridBox.isSelected());
            panel.setShowLegend(showLegendBox.isSelected());
            panel.setShowTooltips(showTooltipsBox.isSelected());
            panel.setAnimated(animateBox.isSelected());
        }
    }
    
    private void exportCurrentChart() {
        // Get currently selected tab
        Component selectedTab = tabbedPane.getSelectedComponent();
        if (selectedTab == null) return;
        
        // Show export options
        String[] exportOptions = {"PNG Image", "PDF Report", "CSV Data"};
        String choice = (String) JOptionPane.showInputDialog(this,
            "Select export format:",
            "Export Chart",
            JOptionPane.QUESTION_MESSAGE,
            null,
            exportOptions,
            exportOptions[0]);
        
        if (choice != null) {
            JOptionPane.showMessageDialog(this,
                "Export functionality would save the current chart as: " + choice,
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showSettings() {
        // Create settings dialog
        JDialog settingsDialog = new JDialog(this, "Visualization Settings", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLocationRelativeTo(this);
        
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Color scheme selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(new JLabel("Color Scheme:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> colorScheme = new JComboBox<>(new String[]{"GradeRise Red", "Professional Blue", "Academic Green"});
        settingsPanel.add(colorScheme, gbc);
        
        // Chart resolution
        gbc.gridx = 0; gbc.gridy = 1;
        settingsPanel.add(new JLabel("Chart Quality:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> quality = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        settingsPanel.add(quality, gbc);
        
        // Animation speed
        gbc.gridx = 0; gbc.gridy = 2;
        settingsPanel.add(new JLabel("Animation Speed:"), gbc);
        gbc.gridx = 1;
        JSlider speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        settingsPanel.add(speedSlider, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(settingsDialog, "Settings saved!");
            settingsDialog.dispose();
        });
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        settingsDialog.add(settingsPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        settingsDialog.setVisible(true);
    }
    
    private void showFullscreenChart(DataVisualizationEngine.InteractiveChartPanel chartPanel, String title) {
        JDialog fullscreenDialog = new JDialog(this, title + " - Fullscreen", true);
        fullscreenDialog.setSize(1000, 700);
        fullscreenDialog.setLocationRelativeTo(this);
        
        // Create a copy of the chart panel for fullscreen
        DataVisualizationEngine.InteractiveChartPanel fullscreenChart = 
            new DataVisualizationEngine.InteractiveChartPanel(chartPanel.getDataset());
        fullscreenChart.setShowGrid(chartPanel.isShowGrid());
        fullscreenChart.setShowLegend(chartPanel.isShowLegend());
        fullscreenChart.setShowTooltips(chartPanel.isShowTooltips());
        
        fullscreenDialog.add(fullscreenChart);
        fullscreenDialog.setVisible(true);
        
        SwingUtilities.invokeLater(() -> fullscreenChart.animateIn());
    }
    
    private void showChartData(DataVisualizationEngine.InteractiveChartPanel chartPanel) {
        // Create data table showing raw chart data
        DataVisualizationEngine.ChartDataset dataset = chartPanel.getDataset();
        
        String[] columnNames = {"Label", "Value", "Date"};
        Object[][] data = new Object[dataset.getDataPoints().size()][3];
        
        for (int i = 0; i < dataset.getDataPoints().size(); i++) {
            DataVisualizationEngine.ChartDataPoint point = dataset.getDataPoints().get(i);
            data[i][0] = point.getLabel();
            data[i][1] = String.format("%.2f", point.getValue());
            data[i][2] = point.getDate().toString();
        }
        
        JTable dataTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        
        JDialog dataDialog = new JDialog(this, "Chart Data - " + dataset.getName(), true);
        dataDialog.setSize(500, 400);
        dataDialog.setLocationRelativeTo(this);
        dataDialog.add(scrollPane);
        dataDialog.setVisible(true);
    }
    
    private void applyGradeRiseTheme() {
        // Apply consistent GradeRise styling
        Color gradeRiseRed = new Color(139, 0, 0);
        Color lightGray = new Color(248, 249, 250);
        
        controlPanel.setBackground(lightGray);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Style buttons
        JButton[] buttons = {refreshButton, exportButton, settingsButton};
        for (JButton button : buttons) {
            button.setBackground(gradeRiseRed);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
        }
        
        // Style checkboxes
        JCheckBox[] checkboxes = {showGridBox, showLegendBox, showTooltipsBox, animateBox};
        for (JCheckBox checkbox : checkboxes) {
            checkbox.setBackground(lightGray);
            checkbox.setForeground(gradeRiseRed);
            checkbox.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        
        // Style chart selector
        chartSelector.setBackground(Color.WHITE);
        chartSelector.setForeground(gradeRiseRed);
    }
    
    // Helper class for chart selector options
    private static class ChartOption {
        private final String displayName;
        private final DataVisualizationEngine.ChartType chartType;
        
        public ChartOption(String displayName, DataVisualizationEngine.ChartType chartType) {
            this.displayName = displayName;
            this.chartType = chartType;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        public DataVisualizationEngine.ChartType getChartType() {
            return chartType;
        }
    }
    
    // Static method to launch dashboard
    public static void showVisualizationDashboard(User user, JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            try {
                VisualizationDashboard dashboard = new VisualizationDashboard(user);
                dashboard.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                    "Error opening visualization dashboard: " + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}