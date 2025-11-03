import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Modern Theme System for GradeRise
 * Provides sleek, contemporary UI theming with proper dark and light modes
 */
public class ModernThemeSystem {
    
    // Theme Types
    public enum Theme {
        LIGHT("Light", "☀️");
        
        private final String displayName;
        private final String icon;
        
        Theme(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        @Override
        public String toString() {
            return icon + " " + displayName;
        }
    }
    
    // Modern Color Schemes
    public enum ColorScheme {
        LAVENDER_RED("Lavender & Red", new Color(0xD4446E)),
        CREAM_AMBER("Cream & Amber", new Color(0xD18529)),
        TEAL_PINK("Teal & Pink", new Color(0x007C7C)),
        GRADERISE("GradeRise Classic", new Color(139, 21, 56)),
        OCEAN("Ocean Blue", new Color(34, 139, 204));
        
        private final String name;
        private final Color primary;
        
        ColorScheme(String name, Color primary) {
            this.name = name;
            this.primary = primary;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public Color getPrimary() { return primary; }
    }
    
    // Theme-specific Color Palettes
    public static class ThemeColors {
        public Color background;
        public Color surface;
        public Color surfaceVariant;
        public Color primary;
        public Color secondary; 
        public Color accent;
        public Color text;
        public Color textSecondary;
        public Color border;
        
        public ThemeColors(ColorScheme scheme, Theme themeType) {
            initializeColors(scheme, themeType);
        }
        
        private void initializeColors(ColorScheme scheme, Theme themeType) {
            switch (scheme) {
                case LAVENDER_RED:
                    initializeLavenderRed();
                    break;
                case CREAM_AMBER:
                    initializeCreamAmber();
                    break;
                case TEAL_PINK:
                    initializeTealPink();
                    break;
                case GRADERISE:
                    initializeGradeRise(themeType);
                    break;
                case OCEAN:
                    initializeOcean(themeType);
                    break;
            }
        }
        
        private void initializeLavenderRed() {
            background = new Color(0xE8CFF5);     // Light Lavender background
            surface = new Color(0xF1D9F8);        // Pale Lilac for cards/panels
            surfaceVariant = new Color(0xF1D9F8); // Pale Lilac
            primary = new Color(0xC1507A);        // Beautiful Pink/Rose header (matching target)
            secondary = new Color(0xD4446E);      // Rose Pink buttons
            accent = new Color(0xD4446E);         // Rose Pink accent
            text = new Color(0x333333);           // Dark gray text
            textSecondary = new Color(0x5C4860);  // Muted Plum
            border = new Color(0xD4D4D4);         // Light border
        }
        
        private void initializeCreamAmber() {
            background = new Color(0xFFF5D0);     // Soft Cream background
            surface = new Color(0xFFE9C8);        // Pale Sand for cards/panels
            surfaceVariant = new Color(0xFFE9C8); // Pale Sand
            primary = new Color(0xCC7A00);        // Bright Orange/Amber header (matching target)
            secondary = new Color(0xD18529);      // Burnt Amber buttons
            accent = new Color(0xE29A3C);         // Honey Gold accent
            text = new Color(0x3A2E17);           // Deep Brown text
            textSecondary = new Color(0x6C5531);  // Cocoa Brown
            border = new Color(0xE0C896);         // Light cream border
        }
        

        
        private void initializeTealPink() {
            background = new Color(0xF0FFFE);     // Soft Mint background
            surface = new Color(0xE8FDFC);        // Aqua Mist for cards/panels
            surfaceVariant = new Color(0xE8FDFC); // Aqua Mist
            primary = new Color(0x007C7C);        // Bright Teal header (matching target)
            secondary = new Color(0x26A69A);      // Teal buttons
            accent = new Color(0xD4446E);         // Rose Pink accent
            text = new Color(0x1A3D3A);           // Deep Teal text
            textSecondary = new Color(0x4A6B69);  // Muted Teal
            border = new Color(0xB8E6E1);         // Light Teal border
        }
        
        private void initializeGradeRise(Theme themeType) {
            // Always use light theme colors
            background = new Color(248, 249, 250);
            surface = Color.WHITE;
            surfaceVariant = new Color(241, 243, 244);
            text = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            border = new Color(222, 226, 230);
            primary = new Color(139, 21, 56);
            secondary = new Color(108, 117, 125);
            accent = new Color(139, 21, 56);
        }
        
        private void initializeOcean(Theme themeType) {
            // Always use light theme colors
            background = new Color(248, 249, 250);
            surface = Color.WHITE;
            surfaceVariant = new Color(241, 243, 244);
            text = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            border = new Color(222, 226, 230);
            primary = new Color(34, 139, 204);
            secondary = new Color(108, 117, 125);
            accent = new Color(34, 139, 204);
        }
    }
    
    // Current theme state
    private static Theme currentTheme = Theme.LIGHT;
    private static ColorScheme currentScheme = ColorScheme.LAVENDER_RED;
    private static ThemeColors currentColors = new ThemeColors(ColorScheme.LAVENDER_RED, Theme.LIGHT);
    private static final String CONFIG_FILE = "data/modern_theme.properties";
    
    /**
     * Initialize the modern theme system
     */
    public static void initialize() {
        loadThemeConfig();
        applyTheme();
    }
    
    /**
     * Apply the current theme to the entire application
     */
    public static void applyTheme() {
        try {
            // Set modern look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Apply modern UI defaults
            setModernUIDefaults();
            
            // Update all existing windows immediately and recursively
            SwingUtilities.invokeLater(() -> {
                for (Window window : Window.getWindows()) {
                    // Update component tree first
                    SwingUtilities.updateComponentTreeUI(window);
                    
                    // Force apply our custom styling
                    if (window instanceof JFrame) {
                        JFrame frame = (JFrame) window;
                        applyModernStyling(frame);
                        
                        // Apply to content pane specifically
                        if (frame.getContentPane() instanceof JComponent) {
                            applyModernStyling((JComponent) frame.getContentPane());
                        }
                    }
                    
                    // Force repaint
                    window.repaint();
                }
            });
            
            // Save theme configuration
            saveThemeConfig();
            
        } catch (Exception e) {
            System.err.println("Error applying modern theme: " + e.getMessage());
        }
    }
    
    /**
     * Set modern UI defaults based on current theme
     */
    private static void setModernUIDefaults() {
        UIDefaults defaults = UIManager.getDefaults();
        
        // Update current colors based on theme and scheme
        currentColors = new ThemeColors(currentScheme, currentTheme);
        
        Color bg = currentColors.background;
        Color surface = currentColors.surface;
        Color surfaceVariant = currentColors.surfaceVariant;
        Color text = currentColors.text;
        Color border = currentColors.border;
        
        // Background colors
        defaults.put("Panel.background", bg);
        defaults.put("Button.background", currentColors.primary);
        defaults.put("TextField.background", surface);
        defaults.put("TextArea.background", surface);
        defaults.put("List.background", surface);
        defaults.put("Table.background", surface);
        defaults.put("ScrollPane.background", bg);
        defaults.put("TabbedPane.background", bg);
        defaults.put("MenuBar.background", surfaceVariant);
        defaults.put("Menu.background", surfaceVariant);
        defaults.put("MenuItem.background", surfaceVariant);
        defaults.put("PopupMenu.background", surface);
        defaults.put("ComboBox.background", surface);
        defaults.put("Spinner.background", surface);
        defaults.put("ToolTip.background", surfaceVariant);
        
        // Text colors
        defaults.put("Label.foreground", text);
        defaults.put("Button.foreground", Color.WHITE);
        defaults.put("TextField.foreground", text);
        defaults.put("PasswordField.foreground", text);
        defaults.put("TextArea.foreground", text);
        defaults.put("List.foreground", text);
        defaults.put("Table.foreground", text);
        defaults.put("Menu.foreground", text);
        defaults.put("MenuItem.foreground", text);
        defaults.put("ComboBox.foreground", text);
        defaults.put("Spinner.foreground", text);
        defaults.put("TabbedPane.foreground", text);
        defaults.put("ToolTip.foreground", text);
        
        // Special handling for password fields and input carets
        defaults.put("PasswordField.background", surfaceVariant);
        defaults.put("TextField.caretForeground", text);
        defaults.put("PasswordField.caretForeground", text);
        defaults.put("TextArea.caretForeground", text);
        
        // Enhanced placeholder and inactive text colors
        defaults.put("TextField.inactiveForeground", currentColors.textSecondary);
        defaults.put("PasswordField.inactiveForeground", currentColors.textSecondary);
        defaults.put("Label.disabledForeground", currentColors.textSecondary);
        
        // Selection colors
        Color selectionColor = lightenColor(currentColors.primary, 0.8f);
        defaults.put("List.selectionBackground", selectionColor);
        defaults.put("Table.selectionBackground", selectionColor);
        defaults.put("TextField.selectionBackground", selectionColor);
        defaults.put("TextArea.selectionBackground", selectionColor);
        defaults.put("Menu.selectionBackground", selectionColor);
        defaults.put("MenuItem.selectionBackground", selectionColor);
        
        // Borders
        defaults.put("TextField.border", createModernBorder(border));
        defaults.put("ComboBox.border", createModernBorder(border));
        defaults.put("ScrollPane.border", createModernBorder(border));
        defaults.put("Table.gridColor", border);
        
        // Fonts
        Font modernFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font boldFont = modernFont.deriveFont(Font.BOLD);
        
        defaults.put("Label.font", modernFont);
        defaults.put("Button.font", boldFont);
        defaults.put("TextField.font", modernFont);
        defaults.put("TextArea.font", modernFont);
        defaults.put("List.font", modernFont);
        defaults.put("Table.font", modernFont);
        defaults.put("Menu.font", modernFont);
        defaults.put("MenuItem.font", modernFont);
        defaults.put("ComboBox.font", modernFont);
        defaults.put("TabbedPane.font", modernFont);
    }
    
    /**
     * Create modern border with subtle styling
     */
    private static Border createModernBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }
    
    /**
     * Apply modern styling to a JFrame
     */
    public static void applyModernStyling(JFrame frame) {
        if (frame.getContentPane() instanceof JComponent) {
            applyModernStyling((JComponent) frame.getContentPane());
        }
    }
    
    /**
     * Apply modern styling to any JComponent recursively with smart text colors
     */
    public static void applyModernStyling(JComponent component) {
        if (component == null) return;
        
        // Use current theme colors
        Color bg = currentColors.background;
        Color surface = currentColors.surface;
        Color text = currentColors.text;
        Color border = currentColors.border;
        
        // Apply styling based on component type - AGGRESSIVELY with proper surface colors
        if (component instanceof JButton) {
            styleModernButton((JButton) component);
        } else if (component instanceof JPanel) {
            // Different panels get different background colors based on their role
            String name = component.getName();
            if (name != null && (name.contains("card") || name.contains("panel") || name.contains("content"))) {
                component.setBackground(surface);  // Cards get surface color
            } else {
                component.setBackground(bg);       // Main panels get background color
            }
            component.setForeground(text);
            component.setOpaque(true);
        } else if (component instanceof JLabel) {
            component.setForeground(text);
            component.setOpaque(false);
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            // Special handling for input fields to ensure visibility
            component.setBackground(currentColors.surfaceVariant);
            component.setForeground(text);
            component.setBorder(createModernBorder(border));
            component.setOpaque(true);
            
            // Ensure caret visibility for dark themes
            if (component instanceof JPasswordField) {
                JPasswordField field = (JPasswordField) component;
                field.setCaretColor(text);
                field.setSelectionColor(lightenColor(currentColors.primary, 0.3f));
                field.setSelectedTextColor(Color.WHITE);
                field.setEchoChar('•'); // Modern bullet character for password masking
            } else if (component instanceof JTextField) {
                JTextField field = (JTextField) component;
                field.setCaretColor(text);
                field.setSelectionColor(lightenColor(currentColors.primary, 0.3f));
                field.setSelectedTextColor(Color.WHITE);
            } else if (component instanceof JTextArea) {
                JTextArea area = (JTextArea) component;
                area.setCaretColor(text);
                area.setSelectionColor(lightenColor(currentColors.primary, 0.3f));
                area.setSelectedTextColor(Color.WHITE);
            }
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setBackground(surface);
            table.setForeground(text);
            table.setGridColor(border);
            table.setSelectionBackground(lightenColor(currentColors.primary, 0.8f));
            table.setSelectionForeground(text);
            table.setOpaque(true);
        } else if (component instanceof JScrollPane) {
            component.setBackground(surface);
            JScrollPane scrollPane = (JScrollPane) component;
            if (scrollPane.getViewport() != null) {
                scrollPane.getViewport().setBackground(surface);
            }
            component.setOpaque(true);
        } else if (component instanceof JTabbedPane) {
            component.setBackground(bg);
            component.setForeground(text);
            component.setOpaque(true);
        } else if (component instanceof JMenuBar || component instanceof JMenu || component instanceof JMenuItem) {
            component.setBackground(currentColors.surfaceVariant);
            component.setForeground(text);
            component.setOpaque(true);
        } else {
            // Default styling for any other component
            component.setBackground(surface);
            component.setForeground(text);
            component.setOpaque(true);
        }
        
        // Apply to children recursively
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                applyModernStyling((JComponent) child);
            }
        }
    }
    
    /**
     * Style a button with modern design
     */
    public static void styleModernButton(JButton button) {
        Color primary = currentColors.primary;
        
        button.setBackground(primary);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add modern hover effect
        button.addMouseListener(new MouseAdapter() {
            private final Color originalColor = button.getBackground();
            private final Color hoverColor = lightenColor(primary, 0.9f);
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
    }
    
    /**
     * Create a modern styled button
     */
    public static JButton createModernButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        
        if (isPrimary) {
            styleModernButton(button);
        } else {
            button.setBackground(currentColors.surfaceVariant);
            button.setForeground(currentColors.text);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        return button;
    }
    
    /**
     * Switch to specified theme
     */
    public static void setTheme(Theme theme) {
        currentTheme = theme;
        applyTheme();
        forceRefreshTheme();  // Ensure immediate update
    }
    
    /**
     * Switch to specified color scheme
     */
    public static void setColorScheme(ColorScheme scheme) {
        currentScheme = scheme;
        applyTheme();
        forceRefreshTheme();  // Ensure immediate update
    }
    
    /**
     * Get current theme
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Get current color scheme
     */
    public static ColorScheme getCurrentColorScheme() {
        return currentScheme;
    }
    
    /**
     * Get current theme colors for external use
     */
    public static ThemeColors getCurrentColors() {
        return currentColors;
    }
    
    /**
     * Get placeholder text color for form fields
     */
    public static Color getPlaceholderTextColor() {
        return currentColors.textSecondary;
    }
    
    /**
     * Force refresh all components with current theme
     */
    public static void forceRefreshTheme() {
        setModernUIDefaults();
        
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    applyModernStyling(frame);
                    if (frame.getContentPane() instanceof JComponent) {
                        applyModernStyling((JComponent) frame.getContentPane());
                    }
                }
                window.repaint();
            }
        });
    }
    
    /**
     * Apply enhanced styling for form elements to ensure visibility
     */
    public static void enhanceFormVisibility(JComponent component) {
        if (component == null) return;
        
        // Enhanced form field styling for better visibility in dark themes
        if (component instanceof JTextField || component instanceof JPasswordField) {
            component.setBackground(currentColors.surfaceVariant);
            component.setForeground(currentColors.text);
            component.setBorder(createEnhancedBorder(currentColors.border));
            component.setOpaque(true);
            
            if (component instanceof JPasswordField) {
                JPasswordField field = (JPasswordField) component;
                field.setCaretColor(currentColors.text);
                field.setEchoChar('•');
                field.setDisabledTextColor(currentColors.textSecondary);
            } else if (component instanceof JTextField) {
                JTextField field = (JTextField) component;
                field.setCaretColor(currentColors.text);
                field.setDisabledTextColor(currentColors.textSecondary);
            }
        }
        
        // Handle labels that might be used for placeholders
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            // If it's a placeholder-style label, use secondary text color
            String text = label.getText();
            if (text != null && (text.toLowerCase().contains("username") || 
                                text.toLowerCase().contains("password") || 
                                text.toLowerCase().contains("email") ||
                                text.toLowerCase().contains("enter"))) {
                label.setForeground(currentColors.textSecondary);
            } else {
                label.setForeground(currentColors.text);
            }
        }
        
        // Apply to children recursively
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                enhanceFormVisibility((JComponent) child);
            }
        }
    }
    
    /**
     * Create enhanced border with better visibility for form fields
     */
    private static Border createEnhancedBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2), // Thicker border for better visibility
            BorderFactory.createEmptyBorder(10, 15, 10, 15) // More padding
        );
    }
    
    /**
     * Show theme selection dialog
     */
    public static void showThemeDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Theme Settings", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🎨 Customize Appearance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme selection
        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        JComboBox<Theme> themeCombo = new JComboBox<>(Theme.values());
        themeCombo.setSelectedItem(currentTheme);
        settingsPanel.add(themeCombo, gbc);
        
        // Color scheme selection
        gbc.gridx = 0; gbc.gridy = 1;
        settingsPanel.add(new JLabel("Color Scheme:"), gbc);
        gbc.gridx = 1;
        JComboBox<ColorScheme> schemeCombo = new JComboBox<>(ColorScheme.values());
        schemeCombo.setSelectedItem(currentScheme);
        settingsPanel.add(schemeCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyButton = createModernButton("Apply", true);
        JButton cancelButton = createModernButton("Cancel", false);
        
        applyButton.addActionListener(e -> {
            setTheme((Theme) themeCombo.getSelectedItem());
            setColorScheme((ColorScheme) schemeCombo.getSelectedItem());
            dialog.dispose();
            JOptionPane.showMessageDialog(parent, "Theme applied successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(settingsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        applyModernStyling(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Lighten a color by the given factor
     */
    private static Color lightenColor(Color color, float factor) {
        int r = (int) Math.min(255, color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) Math.min(255, color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) Math.min(255, color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(r, g, b);
    }
    
    /**
     * Save theme configuration
     */
    private static void saveThemeConfig() {
        try {
            Files.createDirectories(Paths.get("data"));
            
            Properties props = new Properties();
            props.setProperty("theme", currentTheme.name());
            props.setProperty("colorScheme", currentScheme.name());
            
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Modern Theme Configuration");
            }
        } catch (Exception e) {
            System.err.println("Error saving theme config: " + e.getMessage());
        }
    }
    
    /**
     * Load theme configuration
     */
    private static void loadThemeConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (!configFile.exists()) {
                return; // Use defaults
            }
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            }
            
            String themeName = props.getProperty("theme", "LIGHT");
            String schemeName = props.getProperty("colorScheme", "GRADERISE");
            
            try {
                currentTheme = Theme.valueOf(themeName);
            } catch (IllegalArgumentException e) {
                currentTheme = Theme.LIGHT;
            }
            
            try {
                currentScheme = ColorScheme.valueOf(schemeName);
            } catch (IllegalArgumentException e) {
                currentScheme = ColorScheme.GRADERISE;
            }
            
        } catch (Exception e) {
            System.err.println("Error loading theme config: " + e.getMessage());
        }
    }
}