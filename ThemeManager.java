import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import java.awt.*;
import java.util.*;

import java.io.*;
import java.nio.file.*;

/**
 * Advanced Theme Manager for GradeRise
 * Provides comprehensive theming system with multiple color schemes
 */
public class ThemeManager {
    
    // Theme Types
    public enum ThemeType {
        LIGHT("Light Theme", "☀️"),
        DARK("Dark Theme", "🌙"),
        AUTO("Auto (System)", "🔄");
        
        private final String displayName;
        private final String icon;
        
        ThemeType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        
        @Override
        public String toString() { return icon + " " + displayName; }
    }
    
    // Color Schemes
    public enum ColorScheme {
        GRADERISE_RED("GradeRise Red", new Color(139, 0, 0), new Color(220, 53, 69)),
        OCEAN_BLUE("Ocean Blue", new Color(13, 110, 253), new Color(108, 117, 125)),
        FOREST_GREEN("Forest Green", new Color(25, 135, 84), new Color(108, 117, 125)),
        PURPLE_ROYAL("Royal Purple", new Color(102, 16, 242), new Color(108, 117, 125)),
        SUNSET_ORANGE("Sunset Orange", new Color(253, 126, 20), new Color(108, 117, 125)),
        ROSE_GOLD("Rose Gold", new Color(225, 29, 72), new Color(161, 98, 7)),
        CYBER_NEON("Cyber Neon", new Color(20, 184, 166), new Color(147, 51, 234)),
        MIDNIGHT_BLUE("Midnight Blue", new Color(30, 58, 138), new Color(67, 56, 202));
        
        private final String displayName;
        private final Color primaryColor;
        private final Color accentColor;
        
        ColorScheme(String displayName, Color primaryColor, Color accentColor) {
            this.displayName = displayName;
            this.primaryColor = primaryColor;
            this.accentColor = accentColor;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getPrimaryColor() { return primaryColor; }
        public Color getAccentColor() { return accentColor; }
        
        @Override
        public String toString() { return displayName; }
    }
    
    // Theme Configuration
    public static class ThemeConfig {
        private ThemeType themeType;
        private ColorScheme colorScheme;
        private boolean useSystemColors;
        private boolean highContrast;
        private float fontSize;
        private String fontFamily;
        
        public ThemeConfig() {
            this.themeType = ThemeType.LIGHT;
            this.colorScheme = ColorScheme.GRADERISE_RED;
            this.useSystemColors = false;
            this.highContrast = false;
            this.fontSize = 12.0f;
            this.fontFamily = "Segoe UI";
        }
        
        // Getters and Setters
        public ThemeType getThemeType() { return themeType; }
        public void setThemeType(ThemeType themeType) { this.themeType = themeType; }
        
        public ColorScheme getColorScheme() { return colorScheme; }
        public void setColorScheme(ColorScheme colorScheme) { this.colorScheme = colorScheme; }
        
        public boolean isUseSystemColors() { return useSystemColors; }
        public void setUseSystemColors(boolean useSystemColors) { this.useSystemColors = useSystemColors; }
        
        public boolean isHighContrast() { return highContrast; }
        public void setHighContrast(boolean highContrast) { this.highContrast = highContrast; }
        
        public float getFontSize() { return fontSize; }
        public void setFontSize(float fontSize) { this.fontSize = fontSize; }
        
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    }
    
    // Theme Colors Class
    public static class ThemeColors {
        // Background Colors
        public Color primaryBackground;
        public Color secondaryBackground;
        public Color tertiaryBackground;
        
        // Text Colors
        public Color primaryText;
        public Color secondaryText;
        public Color accentText;
        
        // UI Element Colors
        public Color primaryButton;
        public Color secondaryButton;
        public Color borderColor;
        public Color focusColor;
        public Color hoverColor;
        
        // Status Colors
        public Color successColor;
        public Color warningColor;
        public Color errorColor;
        public Color infoColor;
        
        // Chart Colors
        public Color[] chartPalette;
        
        public ThemeColors(ThemeType themeType, ColorScheme colorScheme) {
            initializeColors(themeType, colorScheme);
        }
        
        private void initializeColors(ThemeType themeType, ColorScheme colorScheme) {
            Color primary = colorScheme.getPrimaryColor();
            Color accent = colorScheme.getAccentColor();
            
            if (themeType == ThemeType.DARK) {
                initializeDarkTheme(primary, accent);
            } else {
                initializeLightTheme(primary, accent);
            }
        }
        
        private void initializeLightTheme(Color primary, Color accent) {
            // Background Colors
            primaryBackground = Color.WHITE;
            secondaryBackground = new Color(248, 249, 250);
            tertiaryBackground = new Color(233, 236, 239);
            
            // Text Colors
            primaryText = new Color(33, 37, 41);
            secondaryText = new Color(108, 117, 125);
            accentText = primary;
            
            // UI Element Colors
            primaryButton = primary;
            secondaryButton = new Color(108, 117, 125);
            borderColor = new Color(222, 226, 230);
            focusColor = primary.brighter();
            hoverColor = darkenColor(primary, 0.1f);
            
            // Status Colors
            successColor = new Color(25, 135, 84);
            warningColor = new Color(255, 193, 7);
            errorColor = new Color(220, 53, 69);
            infoColor = new Color(13, 202, 240);
            
            // Chart Colors
            chartPalette = generateChartPalette(primary, accent, false);
        }
        
        private void initializeDarkTheme(Color primary, Color accent) {
            // Background Colors - Dark gray theme
            primaryBackground = new Color(45, 45, 45);        // Main dark gray background
            secondaryBackground = new Color(35, 35, 35);      // Darker gray for panels
            tertiaryBackground = new Color(55, 55, 55);       // Lighter gray for cards
            
            // Text Colors
            primaryText = new Color(245, 245, 245);           // Light gray text
            secondaryText = new Color(180, 180, 180);         // Medium gray text
            accentText = lightenColor(primary, 0.3f);
            
            // UI Element Colors
            primaryButton = primary;
            secondaryButton = new Color(85, 85, 85);          // Gray buttons
            borderColor = new Color(70, 70, 70);              // Dark gray borders
            focusColor = lightenColor(primary, 0.2f);
            hoverColor = lightenColor(primary, 0.1f);
            
            // Status Colors
            successColor = new Color(40, 167, 69);            // Success green
            warningColor = new Color(255, 193, 7);            // Warning yellow
            errorColor = new Color(220, 53, 69);              // Error red
            infoColor = new Color(23, 162, 184);              // Info blue
            
            // Chart Colors
            chartPalette = generateChartPalette(primary, accent, true);
        }
        
        private Color[] generateChartPalette(Color primary, Color accent, boolean isDark) {
            Color[] palette = new Color[8];
            palette[0] = primary;
            palette[1] = accent;
            
            // Generate complementary colors
            float[] hsbPrimary = Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), null);

            
            for (int i = 2; i < 8; i++) {
                float hue = (hsbPrimary[0] + (i * 0.15f)) % 1.0f;
                float saturation = isDark ? 0.7f : 0.6f;
                float brightness = isDark ? 0.8f : 0.7f;
                palette[i] = Color.getHSBColor(hue, saturation, brightness);
            }
            
            return palette;
        }
        
        private Color lightenColor(Color color, float factor) {
            int r = (int) Math.min(255, color.getRed() + (255 - color.getRed()) * factor);
            int g = (int) Math.min(255, color.getGreen() + (255 - color.getGreen()) * factor);
            int b = (int) Math.min(255, color.getBlue() + (255 - color.getBlue()) * factor);
            return new Color(r, g, b);
        }
        
        private Color darkenColor(Color color, float factor) {
            int r = (int) Math.max(0, color.getRed() - color.getRed() * factor);
            int g = (int) Math.max(0, color.getGreen() - color.getGreen() * factor);
            int b = (int) Math.max(0, color.getBlue() - color.getBlue() * factor);
            return new Color(r, g, b);
        }
    }
    
    // Static Theme Management
    private static ThemeConfig currentConfig = new ThemeConfig();
    private static ThemeColors currentColors = new ThemeColors(ThemeType.LIGHT, ColorScheme.GRADERISE_RED);

    
    /**
     * Apply theme to the entire application
     */
    public static void applyTheme(ThemeConfig config) {
        currentConfig = config;
        currentColors = new ThemeColors(config.getThemeType(), config.getColorScheme());
        
        // Set Look and Feel
        setLookAndFeel(config);
        
        // Update UI Manager defaults
        updateUIDefaults();
        
        // Update all existing windows
        updateAllWindows();
        
        // Save theme configuration
        saveThemeConfig();
    }
    
    /**
     * Set appropriate Look and Feel
     */
    private static void setLookAndFeel(ThemeConfig config) {
        try {
            if (config.getThemeType() == ThemeType.AUTO) {
                // Try to detect system theme preference
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                // Use custom Metal theme
                MetalLookAndFeel.setCurrentTheme(new GradeRiseTheme(config));
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            }
        } catch (Exception e) {
            System.err.println("Error setting Look and Feel: " + e.getMessage());
        }
    }
    
    /**
     * Update UI Manager defaults with theme colors
     */
    private static void updateUIDefaults() {
        UIDefaults defaults = UIManager.getDefaults();
        
        // Background colors - Apply to all components
        defaults.put("Panel.background", currentColors.primaryBackground);
        defaults.put("Button.background", currentColors.primaryButton);
        defaults.put("TextField.background", currentColors.tertiaryBackground);
        defaults.put("TextArea.background", currentColors.tertiaryBackground);
        defaults.put("List.background", currentColors.primaryBackground);
        defaults.put("Table.background", currentColors.primaryBackground);
        defaults.put("TabbedPane.background", currentColors.secondaryBackground);
        defaults.put("MenuBar.background", currentColors.secondaryBackground);
        defaults.put("Menu.background", currentColors.secondaryBackground);
        defaults.put("MenuItem.background", currentColors.secondaryBackground);
        defaults.put("PopupMenu.background", currentColors.secondaryBackground);
        defaults.put("ScrollPane.background", currentColors.primaryBackground);
        defaults.put("Viewport.background", currentColors.primaryBackground);
        defaults.put("ComboBox.background", currentColors.tertiaryBackground);
        defaults.put("CheckBox.background", currentColors.primaryBackground);
        defaults.put("RadioButton.background", currentColors.primaryBackground);
        defaults.put("Slider.background", currentColors.primaryBackground);
        defaults.put("ProgressBar.background", currentColors.secondaryBackground);
        defaults.put("Spinner.background", currentColors.tertiaryBackground);
        defaults.put("ToolTip.background", currentColors.tertiaryBackground);
        defaults.put("Tree.background", currentColors.primaryBackground);
        defaults.put("SplitPane.background", currentColors.primaryBackground);
        
        // Text colors
        defaults.put("Label.foreground", currentColors.primaryText);
        defaults.put("Button.foreground", Color.WHITE);
        defaults.put("TextField.foreground", currentColors.primaryText);
        defaults.put("TextArea.foreground", currentColors.primaryText);
        defaults.put("List.foreground", currentColors.primaryText);
        defaults.put("Table.foreground", currentColors.primaryText);
        defaults.put("Menu.foreground", currentColors.primaryText);
        defaults.put("MenuItem.foreground", currentColors.primaryText);
        defaults.put("ComboBox.foreground", currentColors.primaryText);
        defaults.put("CheckBox.foreground", currentColors.primaryText);
        defaults.put("RadioButton.foreground", currentColors.primaryText);
        defaults.put("Slider.foreground", currentColors.primaryText);
        defaults.put("ProgressBar.foreground", currentColors.primaryText);
        defaults.put("Spinner.foreground", currentColors.primaryText);
        defaults.put("ToolTip.foreground", currentColors.primaryText);
        defaults.put("Tree.foreground", currentColors.primaryText);
        defaults.put("TabbedPane.foreground", currentColors.primaryText);
        
        // Border colors
        defaults.put("TextField.border", BorderFactory.createLineBorder(currentColors.borderColor));
        defaults.put("Button.border", BorderFactory.createLineBorder(currentColors.borderColor));
        defaults.put("ComboBox.border", BorderFactory.createLineBorder(currentColors.borderColor));
        defaults.put("ScrollPane.border", BorderFactory.createLineBorder(currentColors.borderColor));
        defaults.put("Table.gridColor", currentColors.borderColor);
        
        // Selection colors
        defaults.put("List.selectionBackground", currentColors.focusColor);
        defaults.put("Table.selectionBackground", currentColors.focusColor);
        defaults.put("TextField.selectionBackground", currentColors.focusColor);
        defaults.put("TextArea.selectionBackground", currentColors.focusColor);
        defaults.put("ComboBox.selectionBackground", currentColors.focusColor);
        defaults.put("Menu.selectionBackground", currentColors.focusColor);
        defaults.put("MenuItem.selectionBackground", currentColors.focusColor);
        defaults.put("Tree.selectionBackground", currentColors.focusColor);
        
        // Focus colors
        defaults.put("Button.focus", currentColors.focusColor);
        
        // Font settings
        Font baseFont = new Font(currentConfig.getFontFamily(), Font.PLAIN, (int) currentConfig.getFontSize());
        Font boldFont = baseFont.deriveFont(Font.BOLD);
        
        defaults.put("Label.font", baseFont);
        defaults.put("Button.font", boldFont);
        defaults.put("TextField.font", baseFont);
        defaults.put("TextArea.font", baseFont);
        defaults.put("List.font", baseFont);
        defaults.put("Table.font", baseFont);
        defaults.put("Menu.font", baseFont);
        defaults.put("MenuItem.font", baseFont);
        defaults.put("ComboBox.font", baseFont);
        defaults.put("CheckBox.font", baseFont);
        defaults.put("RadioButton.font", baseFont);
        defaults.put("TabbedPane.font", baseFont);
    }
    
    /**
     * Update all existing windows with new theme
     */
    private static void updateAllWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }
    
    /**
     * Get current theme colors
     */
    public static ThemeColors getCurrentColors() {
        return currentColors;
    }
    
    /**
     * Get current theme configuration
     */
    public static ThemeConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * Force apply current theme to all windows and components
     */
    public static void forceApplyCurrentTheme() {
        updateUIDefaults();
        updateAllWindows();
        
        // Also update all existing components
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                if (frame.getContentPane() instanceof JComponent) {
                    applyThemeToComponent((JComponent) frame.getContentPane());
                }
            }
        }
    }
    
    /**
     * Apply theme to specific component recursively
     */
    public static void applyThemeToComponent(JComponent component) {
        if (component == null) return;
        
        // Apply to different component types
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(currentColors.primaryButton);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
        } else if (component instanceof JPanel) {
            component.setBackground(currentColors.primaryBackground);
            component.setForeground(currentColors.primaryText);
        } else if (component instanceof JLabel) {
            component.setForeground(currentColors.primaryText);
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(currentColors.tertiaryBackground);
            component.setForeground(currentColors.primaryText);
        } else if (component instanceof JTable) {
            component.setBackground(currentColors.primaryBackground);
            component.setForeground(currentColors.primaryText);
            ((JTable) component).setGridColor(currentColors.borderColor);
        } else if (component instanceof JList) {
            component.setBackground(currentColors.primaryBackground);
            component.setForeground(currentColors.primaryText);
        } else if (component instanceof JComboBox) {
            component.setBackground(currentColors.tertiaryBackground);
            component.setForeground(currentColors.primaryText);
        } else if (component instanceof JScrollPane) {
            component.setBackground(currentColors.primaryBackground);
            JScrollPane scrollPane = (JScrollPane) component;
            if (scrollPane.getViewport() != null) {
                scrollPane.getViewport().setBackground(currentColors.primaryBackground);
            }
        } else {
            // Default for other components
            component.setBackground(currentColors.primaryBackground);
            component.setForeground(currentColors.primaryText);
        }
        
        // Recursively apply to children
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                applyThemeToComponent((JComponent) child);
            }
        }
    }
    
    /**
     * Create themed button
     */
    public static JButton createThemedButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        
        if (isPrimary) {
            button.setBackground(currentColors.primaryButton);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(currentColors.secondaryButton);
            button.setForeground(Color.WHITE);
        }
        
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font(currentConfig.getFontFamily(), Font.BOLD, (int) currentConfig.getFontSize()));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(currentColors.hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(isPrimary ? currentColors.primaryButton : currentColors.secondaryButton);
            }
        });
        
        return button;
    }
    
    /**
     * Save theme configuration to file
     */
    private static void saveThemeConfig() {
        try {
            // Ensure data directory exists
            Files.createDirectories(Paths.get("data"));
            
            // Create simple properties format
            Properties props = new Properties();
            props.setProperty("themeType", currentConfig.getThemeType().name());
            props.setProperty("colorScheme", currentConfig.getColorScheme().name());
            props.setProperty("useSystemColors", String.valueOf(currentConfig.isUseSystemColors()));
            props.setProperty("highContrast", String.valueOf(currentConfig.isHighContrast()));
            props.setProperty("fontSize", String.valueOf(currentConfig.getFontSize()));
            props.setProperty("fontFamily", currentConfig.getFontFamily());
            
            try (FileOutputStream fos = new FileOutputStream("data/theme_config.properties")) {
                props.store(fos, "GradeRise Theme Configuration");
            }
            
        } catch (Exception e) {
            System.err.println("Error saving theme configuration: " + e.getMessage());
        }
    }
    
    /**
     * Load theme configuration from file
     */
    public static void loadThemeConfig() {
        try {
            File configFile = new File("data/theme_config.properties");
            if (!configFile.exists()) {
                return; // Use defaults
            }
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            }
            
            ThemeConfig config = new ThemeConfig();
            
            // Load properties
            String themeTypeStr = props.getProperty("themeType", "LIGHT");
            try {
                config.setThemeType(ThemeType.valueOf(themeTypeStr));
            } catch (IllegalArgumentException e) {
                config.setThemeType(ThemeType.LIGHT);
            }
            
            String colorSchemeStr = props.getProperty("colorScheme", "GRADERISE_RED");
            try {
                config.setColorScheme(ColorScheme.valueOf(colorSchemeStr));
            } catch (IllegalArgumentException e) {
                config.setColorScheme(ColorScheme.GRADERISE_RED);
            }
            
            config.setUseSystemColors(Boolean.parseBoolean(props.getProperty("useSystemColors", "false")));
            config.setHighContrast(Boolean.parseBoolean(props.getProperty("highContrast", "false")));
            config.setFontSize(Float.parseFloat(props.getProperty("fontSize", "12.0")));
            config.setFontFamily(props.getProperty("fontFamily", "Segoe UI"));
            
            // Apply loaded configuration
            applyTheme(config);
            
        } catch (Exception e) {
            System.err.println("Error loading theme configuration: " + e.getMessage());
        }
    }
    
    /**
     * Show theme selection dialog
     */
    public static void showThemeDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "🎨 Theme Settings", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🎨 Customize GradeRise Appearance");
        titleLabel.setFont(new Font(currentConfig.getFontFamily(), Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme Type
        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        JComboBox<ThemeType> themeCombo = new JComboBox<>(ThemeType.values());
        themeCombo.setSelectedItem(currentConfig.getThemeType());
        settingsPanel.add(themeCombo, gbc);
        
        // Color Scheme
        gbc.gridx = 0; gbc.gridy = 1;
        settingsPanel.add(new JLabel("Color Scheme:"), gbc);
        gbc.gridx = 1;
        JComboBox<ColorScheme> colorCombo = new JComboBox<>(ColorScheme.values());
        colorCombo.setSelectedItem(currentConfig.getColorScheme());
        settingsPanel.add(colorCombo, gbc);
        
        // Font Size
        gbc.gridx = 0; gbc.gridy = 2;
        settingsPanel.add(new JLabel("Font Size:"), gbc);
        gbc.gridx = 1;
        JSlider fontSlider = new JSlider(8, 24, (int) currentConfig.getFontSize());
        fontSlider.setMajorTickSpacing(4);
        fontSlider.setPaintTicks(true);
        fontSlider.setPaintLabels(true);
        settingsPanel.add(fontSlider, gbc);
        
        // High Contrast
        gbc.gridx = 0; gbc.gridy = 3;
        settingsPanel.add(new JLabel("High Contrast:"), gbc);
        gbc.gridx = 1;
        JCheckBox contrastCheck = new JCheckBox("Enable high contrast mode");
        contrastCheck.setSelected(currentConfig.isHighContrast());
        settingsPanel.add(contrastCheck, gbc);
        
        // Preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        
        JPanel samplePanel = createSamplePreview();
        previewPanel.add(samplePanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyButton = createThemedButton("Apply", true);
        JButton cancelButton = createThemedButton("Cancel", false);
        JButton resetButton = createThemedButton("Reset to Default", false);
        
        // Live preview
        Runnable updatePreview = () -> {
            ThemeConfig tempConfig = new ThemeConfig();
            tempConfig.setThemeType((ThemeType) themeCombo.getSelectedItem());
            tempConfig.setColorScheme((ColorScheme) colorCombo.getSelectedItem());
            tempConfig.setFontSize(fontSlider.getValue());
            tempConfig.setHighContrast(contrastCheck.isSelected());
            
            ThemeColors previewColors = new ThemeColors(tempConfig.getThemeType(), tempConfig.getColorScheme());
            updateSamplePreview(samplePanel, previewColors, tempConfig);
        };
        
        themeCombo.addActionListener(e -> updatePreview.run());
        colorCombo.addActionListener(e -> updatePreview.run());
        fontSlider.addChangeListener(e -> updatePreview.run());
        contrastCheck.addActionListener(e -> updatePreview.run());
        
        // Button actions
        applyButton.addActionListener(e -> {
            ThemeConfig newConfig = new ThemeConfig();
            newConfig.setThemeType((ThemeType) themeCombo.getSelectedItem());
            newConfig.setColorScheme((ColorScheme) colorCombo.getSelectedItem());
            newConfig.setFontSize(fontSlider.getValue());
            newConfig.setHighContrast(contrastCheck.isSelected());
            newConfig.setFontFamily(currentConfig.getFontFamily());
            
            applyTheme(newConfig);
            dialog.dispose();
            
            JOptionPane.showMessageDialog(parent, "Theme applied successfully!", "Theme Updated", JOptionPane.INFORMATION_MESSAGE);
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        resetButton.addActionListener(e -> {
            ThemeConfig defaultConfig = new ThemeConfig();
            applyTheme(defaultConfig);
            dialog.dispose();
            
            JOptionPane.showMessageDialog(parent, "Theme reset to default!", "Theme Reset", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(settingsPanel, BorderLayout.NORTH);
        centerPanel.add(previewPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        applyThemeToComponent(mainPanel);
        
        dialog.setVisible(true);
    }
    
    /**
     * Create sample preview panel
     */
    private static JPanel createSamplePreview() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Sample components
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("📚 Sample Course"), gbc);
        
        gbc.gridy = 1;
        JButton sampleButton = new JButton("Sample Button");
        panel.add(sampleButton, gbc);
        
        gbc.gridy = 2;
        JTextField sampleField = new JTextField("Sample Text Field", 15);
        panel.add(sampleField, gbc);
        
        gbc.gridy = 3;
        JCheckBox sampleCheck = new JCheckBox("Sample Checkbox");
        panel.add(sampleCheck, gbc);
        
        return panel;
    }
    
    /**
     * Update sample preview with theme colors
     */
    private static void updateSamplePreview(JPanel panel, ThemeColors colors, ThemeConfig config) {
        panel.setBackground(colors.primaryBackground);
        
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                comp.setForeground(colors.primaryText);
                comp.setFont(new Font(config.getFontFamily(), Font.PLAIN, (int) config.getFontSize()));
            } else if (comp instanceof JButton) {
                comp.setBackground(colors.primaryButton);
                comp.setForeground(Color.WHITE);
                comp.setFont(new Font(config.getFontFamily(), Font.BOLD, (int) config.getFontSize()));
            } else if (comp instanceof JTextField) {
                comp.setBackground(colors.primaryBackground);
                comp.setForeground(colors.primaryText);
                comp.setFont(new Font(config.getFontFamily(), Font.PLAIN, (int) config.getFontSize()));
                ((JTextField) comp).setBorder(BorderFactory.createLineBorder(colors.borderColor));
            } else if (comp instanceof JCheckBox) {
                comp.setBackground(colors.primaryBackground);
                comp.setForeground(colors.primaryText);
                comp.setFont(new Font(config.getFontFamily(), Font.PLAIN, (int) config.getFontSize()));
            }
        }
        
        panel.repaint();
    }
    
    /**
     * Custom Metal Theme for GradeRise
     */
    private static class GradeRiseTheme extends DefaultMetalTheme {
        
        public GradeRiseTheme(ThemeConfig config) {
            // Config passed but not used - theme uses currentColors directly
        }
        
        @Override
        public ColorUIResource getPrimary1() {
            return new ColorUIResource(currentColors.primaryButton);
        }
        
        @Override
        public ColorUIResource getPrimary2() {
            return new ColorUIResource(currentColors.hoverColor);
        }
        
        @Override
        public ColorUIResource getPrimary3() {
            return new ColorUIResource(currentColors.focusColor);
        }
        
        @Override
        public ColorUIResource getSecondary1() {
            return new ColorUIResource(currentColors.tertiaryBackground);
        }
        
        @Override
        public ColorUIResource getSecondary2() {
            return new ColorUIResource(currentColors.secondaryBackground);
        }
        
        @Override
        public ColorUIResource getSecondary3() {
            return new ColorUIResource(currentColors.primaryBackground);
        }
        
        @Override
        public ColorUIResource getPrimaryControlHighlight() {
            return new ColorUIResource(currentColors.focusColor);
        }
    }
}