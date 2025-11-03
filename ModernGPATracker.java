import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern GPA Tracker Application - Enhanced with Modern UI Framework
 * Features: Dark/Light themes, responsive design, smooth animations, contemporary aesthetics
 */
public class ModernGPATracker extends JFrame {
    
    // Application state
    private static String currentUser;
    private static boolean isLoggedIn = false;
    
    // UI Components
    private JPanel mainContentPanel;
    private ModernUIFramework.LoadingSpinner loadingSpinner;
    private JLabel statusLabel;
    
    // Theme toggle
    private JToggleButton themeToggle;
    
    public ModernGPATracker() {
        initializeApplication();
    }
    
    private void initializeApplication() {
        setTitle("Modern GPA Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Make application responsive
        Dimension appSize = ModernUIFramework.getResponsiveSize(new Dimension(1200, 800));
        setSize(appSize);
        setLocationRelativeTo(null);
        
        // Setup modern look and feel
        setupModernLookAndFeel();
        
        // Initialize UI
        initializeUI();
        
        // Add theme listener for automatic updates
        ModernUIFramework.addThemeListener(this);
        
        // Start with login screen
        showLoginScreen();
    }
    
    private void setupModernLookAndFeel() {
        try {
            // Use system look and feel as base - commenting out problematic line
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            
            // Enhance with modern styling
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 4);
            
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create modern menu bar
        createModernMenuBar();
        
        // Main content panel with responsive layout
        mainContentPanel = new ResponsiveContainer();
        mainContentPanel.setBackground(ModernUIFramework.getTheme().background);
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Status bar at bottom
        createStatusBar();
        
        // Loading overlay (initially hidden)
        loadingSpinner = new ModernUIFramework.LoadingSpinner();
        loadingSpinner.setVisible(false);
    }
    
    private void createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(ModernUIFramework.getTheme().surface);
        menuBar.setBorder(new EmptyBorder(8, 16, 8, 16));
        
        // App title/logo
        JLabel appTitle = new JLabel("GPA Tracker");
        appTitle.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 18)));
        appTitle.setForeground(ModernUIFramework.getTheme().onSurface);
        appTitle.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        // Theme toggle button
        themeToggle = new JToggleButton();
        themeToggle.setSelected(ModernUIFramework.isDarkTheme());
        themeToggle.setText(ModernUIFramework.isDarkTheme() ? "🌙" : "☀️");
        themeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        themeToggle.setBorder(new EmptyBorder(8, 12, 8, 12));
        themeToggle.setContentAreaFilled(false);
        themeToggle.setFocusPainted(false);
        themeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggle.addActionListener(e -> toggleTheme());
        
        // User menu (when logged in)
        JMenu userMenu = new JMenu();
        userMenu.setText(isLoggedIn ? currentUser : "Not logged in");
        userMenu.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 14)));
        
        if (isLoggedIn) {
            JMenuItem profileItem = new JMenuItem("Profile");
            JMenuItem settingsItem = new JMenuItem("Settings");
            JMenuItem logoutItem = new JMenuItem("Logout");
            
            logoutItem.addActionListener(e -> logout());
            
            userMenu.add(profileItem);
            userMenu.add(settingsItem);
            userMenu.addSeparator();
            userMenu.add(logoutItem);
        }
        
        // Layout menu bar
        menuBar.setLayout(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(appTitle);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(themeToggle);
        if (isLoggedIn) {
            rightPanel.add(userMenu);
        }
        
        menuBar.add(leftPanel, BorderLayout.WEST);
        menuBar.add(rightPanel, BorderLayout.EAST);
        
        setJMenuBar(menuBar);
    }
    
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(ModernUIFramework.getTheme().surface);
        statusBar.setBorder(new EmptyBorder(8, 16, 8, 16));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 12)));
        statusLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void toggleTheme() {
        ModernUIFramework.setDarkTheme(!ModernUIFramework.isDarkTheme());
        themeToggle.setText(ModernUIFramework.isDarkTheme() ? "🌙" : "☀️");
        
        // Show loading animation during theme switch
        showLoading("Switching theme...");
        
        // Delay to show animation, then update UI
        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            updateTheme();
            hideLoading();
            setStatus("Theme updated");
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void updateTheme() {
        SwingUtilities.updateComponentTreeUI(this);
        
        // Update custom components
        mainContentPanel.setBackground(ModernUIFramework.getTheme().background);
        getJMenuBar().setBackground(ModernUIFramework.getTheme().surface);
        
        repaint();
    }
    
    private void showLoginScreen() {
        mainContentPanel.removeAll();
        
        // Create login panel with modern design
        ModernUIFramework.ModernCard loginCard = new ModernUIFramework.ModernCard(6f);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setPreferredSize(ModernUIFramework.getResponsiveSize(new Dimension(400, 500)));
        
        // Header with GradeRise branding
        JLabel brandLabel = new JLabel("GradeRise", SwingConstants.CENTER);
        brandLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 32)));
        brandLabel.setForeground(ModernUIFramework.getTheme().primary);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel headerLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        headerLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 24)));
        headerLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        
        JLabel subtitleLabel = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitleLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 16)));
        subtitleLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        // Input fields
        ModernUIFramework.ModernTextField usernameField = new ModernUIFramework.ModernTextField("Username or Email");
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(300, 56));
        
        ModernUIFramework.ModernTextField passwordField = new ModernUIFramework.ModernTextField("Password");
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(300, 56));
        
        // Buttons
        ModernUIFramework.ModernButton loginButton = new ModernUIFramework.ModernButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(300, 48));
        loginButton.addActionListener(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        
        ModernUIFramework.ModernButton signupButton = new ModernUIFramework.ModernButton("Create Account", 
                                                         ModernUIFramework.ModernButton.ButtonStyle.OUTLINED);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(300, 48));
        signupButton.addActionListener(e -> showSignupScreen());
        
        // Google Sign-In button with proper styling
        ModernUIFramework.ModernButton googleButton = new ModernUIFramework.ModernButton("Sign in with Google", 
                                                         ModernUIFramework.ModernButton.ButtonStyle.OUTLINED);
        googleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        googleButton.setMaximumSize(new Dimension(300, 48));
        googleButton.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 14)));
        
        // Google brand colors - white background with dark text for light theme, dark for dark theme
        if (ModernUIFramework.isDarkTheme()) {
            googleButton.setCustomColor(new Color(0x4285f4)); // Google blue
        } else {
            googleButton.setCustomColor(new Color(0xFFFFFF)); // White background
        }
        googleButton.addActionListener(e -> handleGoogleLogin());
        
        // Layout components
        loginCard.add(brandLabel);
        loginCard.add(headerLabel);
        loginCard.add(subtitleLabel);
        loginCard.add(usernameField);
        loginCard.add(Box.createVerticalStrut(16));
        loginCard.add(passwordField);
        loginCard.add(Box.createVerticalStrut(24));
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(12));
        loginCard.add(signupButton);
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(new JSeparator());
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(googleButton);
        
        // Add to main panel with responsive layout
        ResponsiveContainer container = (ResponsiveContainer) mainContentPanel;
        container.removeAll();
        
        // Center the login card
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(loginCard);
        
        container.addFullWidth(centerPanel);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        setStatus("Please sign in to continue");
    }
    
    private void showSignupScreen() {
        setStatus("Loading signup form...");
        
        // Show loading briefly for smooth transition
        showLoading("Loading...");
        
        javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
            createSignupForm();
            hideLoading();
            setStatus("Create your account");
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void createSignupForm() {
        mainContentPanel.removeAll();
        
        ModernUIFramework.ModernCard signupCard = new ModernUIFramework.ModernCard(6f);
        signupCard.setLayout(new BoxLayout(signupCard, BoxLayout.Y_AXIS));
        signupCard.setPreferredSize(ModernUIFramework.getResponsiveSize(new Dimension(400, 600)));
        
        // Header with GradeRise branding
        JLabel brandLabel = new JLabel("GradeRise", SwingConstants.CENTER);
        brandLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 28)));
        brandLabel.setForeground(ModernUIFramework.getTheme().primary);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel headerLabel = new JLabel("Create Account", SwingConstants.CENTER);
        headerLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 24)));
        headerLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        // Input fields
        ModernUIFramework.ModernTextField usernameField = new ModernUIFramework.ModernTextField("Username");
        ModernUIFramework.ModernTextField emailField = new ModernUIFramework.ModernTextField("Email Address");
        ModernUIFramework.ModernTextField passwordField = new ModernUIFramework.ModernTextField("Password");
        ModernUIFramework.ModernTextField confirmPasswordField = new ModernUIFramework.ModernTextField("Confirm Password");
        
        // Set consistent sizing and alignment
        JComponent[] fields = {usernameField, emailField, passwordField, confirmPasswordField};
        for (JComponent field : fields) {
            field.setAlignmentX(Component.CENTER_ALIGNMENT);
            field.setMaximumSize(new Dimension(300, 56));
        }
        
        // Buttons
        ModernUIFramework.ModernButton createButton = new ModernUIFramework.ModernButton("Create Account");
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.setMaximumSize(new Dimension(300, 48));
        createButton.addActionListener(e -> handleSignup(
            usernameField.getText(), 
            emailField.getText(), 
            passwordField.getText(), 
            confirmPasswordField.getText()
        ));
        
        ModernUIFramework.ModernButton backButton = new ModernUIFramework.ModernButton("Back to Login", 
                                                      ModernUIFramework.ModernButton.ButtonStyle.TEXT);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> showLoginScreen());
        
        // Layout
        signupCard.add(brandLabel);
        signupCard.add(headerLabel);
        signupCard.add(usernameField);
        signupCard.add(Box.createVerticalStrut(16));
        signupCard.add(emailField);
        signupCard.add(Box.createVerticalStrut(16));
        signupCard.add(passwordField);
        signupCard.add(Box.createVerticalStrut(16));
        signupCard.add(confirmPasswordField);
        signupCard.add(Box.createVerticalStrut(24));
        signupCard.add(createButton);
        signupCard.add(Box.createVerticalStrut(16));
        signupCard.add(backButton);
        
        // Center and add to main panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(signupCard);
        
        ResponsiveContainer container = (ResponsiveContainer) mainContentPanel;
        container.removeAll();
        container.addFullWidth(centerPanel);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void showDashboard() {
        isLoggedIn = true;
        createModernMenuBar(); // Refresh menu bar to show user menu
        
        mainContentPanel.removeAll();
        
        // Dashboard header
        createDashboardHeader();
        
        // Main dashboard content with responsive grid
        createDashboardContent();
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        setStatus("Dashboard loaded successfully");
    }
    
    private void createDashboardHeader() {
        ModernUIFramework.ModernCard headerCard = new ModernUIFramework.ModernCard(2f);
        headerCard.setLayout(new BorderLayout());
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome back, " + currentUser + "!");
        welcomeLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 24)));
        welcomeLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        
        // GPA display (mock data)
        JLabel gpaLabel = new JLabel("Current GPA: 3.75");
        gpaLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 18)));
        gpaLabel.setForeground(ModernUIFramework.getTheme().primary);
        
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.add(welcomeLabel, BorderLayout.WEST);
        headerContent.add(gpaLabel, BorderLayout.EAST);
        
        headerCard.add(headerContent);
        
        ResponsiveContainer container = (ResponsiveContainer) mainContentPanel;
        container.addFullWidth(headerCard);
    }
    
    private void createDashboardContent() {
        ResponsiveContainer container = (ResponsiveContainer) mainContentPanel;
        
        // Quick stats cards
        createQuickStatsCards(container);
        
        // Recent activity
        createRecentActivityCard(container);
        
        // Semester overview
        createSemesterOverviewCard(container);
    }
    
    private void createQuickStatsCards(ResponsiveContainer container) {
        // GPA Trend Card
        ModernUIFramework.ModernCard gpaCard = createStatCard("GPA Trend", "3.75", "↑ 0.1 this semester", 
                                                             ModernUIFramework.getTheme().success);
        
        // Credits Card
        ModernUIFramework.ModernCard creditsCard = createStatCard("Total Credits", "84", "12 this semester", 
                                                                 ModernUIFramework.getTheme().primary);
        
        // Courses Card
        ModernUIFramework.ModernCard coursesCard = createStatCard("Active Courses", "5", "Spring 2025", 
                                                                 ModernUIFramework.getTheme().accent);
        
        // Assignments Card
        ModernUIFramework.ModernCard assignmentsCard = createStatCard("Pending Assignments", "3", "Due this week", 
                                                                     ModernUIFramework.getTheme().warning);
        
        container.addQuarterWidth(gpaCard);
        container.addQuarterWidth(creditsCard);
        container.addQuarterWidth(coursesCard);
        container.addQuarterWidth(assignmentsCard);
    }
    
    private ModernUIFramework.ModernCard createStatCard(String title, String value, String subtitle, Color accentColor) {
        ModernUIFramework.ModernCard card = new ModernUIFramework.ModernCard(3f);
        card.setLayout(new BorderLayout());
        card.setClickable(true);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 14)));
        titleLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 32)));
        valueLabel.setForeground(accentColor);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 12)));
        subtitleLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void createRecentActivityCard(ResponsiveContainer container) {
        ModernUIFramework.ModernCard activityCard = new ModernUIFramework.ModernCard(4f);
        activityCard.setLayout(new BorderLayout());
        
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 18)));
        activityTitle.setForeground(ModernUIFramework.getTheme().onSurface);
        activityTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Activity list (mock data)
        JPanel activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setOpaque(false);
        
        String[] activities = {
            "Submitted Math 301 Assignment #4",
            "Updated grade for CS 250 Midterm Exam",
            "Added new course: Physics 201",
            "Calculated semester GPA: 3.75"
        };
        
        for (String activity : activities) {
            JLabel activityItem = new JLabel("• " + activity);
            activityItem.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 14)));
            activityItem.setForeground(ModernUIFramework.getTheme().onSurface);
            activityItem.setBorder(new EmptyBorder(4, 0, 4, 0));
            activityList.add(activityItem);
        }
        
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(activityTitle, BorderLayout.NORTH);
        content.add(activityList, BorderLayout.CENTER);
        
        activityCard.add(content);
        
        container.addHalfWidth(activityCard);
    }
    
    private void createSemesterOverviewCard(ResponsiveContainer container) {
        ModernUIFramework.ModernCard semesterCard = new ModernUIFramework.ModernCard(4f);
        semesterCard.setLayout(new BorderLayout());
        
        JLabel semesterTitle = new JLabel("Current Semester");
        semesterTitle.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.BOLD, 18)));
        semesterTitle.setForeground(ModernUIFramework.getTheme().onSurface);
        semesterTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Progress bar for semester completion
        ModernUIFramework.ModernProgressBar progressBar = new ModernUIFramework.ModernProgressBar(0, 100);
        progressBar.setValue(65); // 65% through semester
        progressBar.setPreferredSize(new Dimension(200, 8));
        
        JLabel progressLabel = new JLabel("65% Complete - 5 weeks remaining");
        progressLabel.setFont(ModernUIFramework.getResponsiveFont(new Font("Segoe UI", Font.PLAIN, 14)));
        progressLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(semesterTitle);
        content.add(progressBar);
        content.add(Box.createVerticalStrut(8));
        content.add(progressLabel);
        
        semesterCard.add(content);
        
        container.addHalfWidth(semesterCard);
    }
    
    // Event handlers
    private void handleLogin(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showErrorMessage("Please enter both username and password");
            return;
        }
        
        showLoading("Signing in...");
        setStatus("Authenticating user...");
        
        // Simulate authentication delay
        javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
            // Mock successful login
            currentUser = username;
            hideLoading();
            showDashboard();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void handleSignup(String username, String email, String password, String confirmPassword) {
        // Validation
        if (username.trim().isEmpty() || email.trim().isEmpty() || 
            password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            showErrorMessage("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showErrorMessage("Passwords do not match");
            return;
        }
        
        if (!email.contains("@")) {
            showErrorMessage("Please enter a valid email address");
            return;
        }
        
        showLoading("Creating account...");
        setStatus("Setting up your account...");
        
        // Simulate account creation delay
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
            currentUser = username;
            hideLoading();
            showDashboard();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void handleGoogleLogin() {
        showLoading("Connecting to Google...");
        setStatus("Authenticating with Google...");
        
        // Simulate Google OAuth flow
        javax.swing.Timer timer = new javax.swing.Timer(2500, e -> {
            currentUser = "Google User";
            hideLoading();
            showDashboard();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void logout() {
        showLoading("Signing out...");
        setStatus("Logging out...");
        
        javax.swing.Timer timer = new javax.swing.Timer(800, e -> {
            currentUser = null;
            isLoggedIn = false;
            hideLoading();
            showLoginScreen();
            createModernMenuBar(); // Refresh menu bar
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    // Utility methods
    private void showLoading(String message) {
        if (loadingSpinner != null) {
            statusLabel.setText(message);
            
            // Add loading overlay
            JPanel overlay = new JPanel(new GridBagLayout());
            overlay.setBackground(new Color(0, 0, 0, 100)); // Semi-transparent
            overlay.add(loadingSpinner);
            
            setGlassPane(overlay);
            overlay.setVisible(true);
            loadingSpinner.start();
        }
    }
    
    private void hideLoading() {
        if (loadingSpinner != null) {
            loadingSpinner.stop();
            getGlassPane().setVisible(false);
        }
    }
    
    private void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + message);
    }
    
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system properties for better rendering
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
                System.setProperty("sun.java2d.opengl", "true");
                
                new ModernGPATracker().setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}