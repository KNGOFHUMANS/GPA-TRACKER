import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UIComponentFactory - Creates styled UI components with consistent theming
 * Part of the refactored architecture for CollegeGPATracker
 */
public class UIComponentFactory {
    
    // ===== THEME COLORS =====
    
    // GradeRise Brand Colors
    public static final Color BRAND_PRIMARY = new Color(0x8B1538);   // Dark red from logo
    public static final Color GRADIENT_START = new Color(0x6D1028);  // Darker red start
    public static final Color GRADIENT_END = BRAND_PRIMARY;          // Main red end
    
    // Light Theme
    public static final Color BG_LIGHT = new Color(0xE5E7EB);       // Light grey background
    public static final Color CARD_LIGHT = new Color(0xF9FAFB);     // Light grey cards
    public static final Color TEXT_DARK = new Color(0x1F2937);      // Dark text on light
    public static final Color TEXT_MUTED = new Color(0x6B7280);     // Muted grey text
    
    // Status Colors
    public static final Color SUCCESS_EMERALD = new Color(0x10B981); // Success green
    public static final Color WARNING_AMBER = new Color(0xF59E0B);   // Warning amber
    public static final Color ERROR_RED = new Color(0xEF4444);       // Error red
    public static final Color INFO_BRAND = new Color(0x3B82F6);      // Blue for info
    
    // UI Colors
    public static final Color BORDER_SUBTLE = new Color(0xD1D5DB);    // Subtle grey borders
    public static final Color HOVER_LIGHT = new Color(0xF3F4F6);     // Light hover state
    
    // Login Page Colors
    public static final Color LEFT_TOP = new Color(0x14B8A6);        // Teal gradient start
    public static final Color LEFT_BOTTOM = new Color(0x6440FF);     // Purple gradient end
    public static final Color RIGHT_BG = new Color(0xF6F7F9);        // Soft off-white
    public static final Color CARD_BG = new Color(0xFAFAFC);         // Card background
    
    // Dark Theme Colors
    public static final Color RIGHT_BG_DARK = new Color(0x4B5563);
    public static final Color CARD_BG_DARK = new Color(0x6B7280);
    public static final Color INPUT_BG_DARK = new Color(0x9CA3AF);
    public static final Color INPUT_BORDER_DARK = new Color(0xD1D5DB);
    
    // ===== BUTTON FACTORY =====
    
    /**
     * Create a modern pill-style button
     */
    public static JButton createPillButton(String text) {
        return createPillButton(text, null, BRAND_PRIMARY, Color.WHITE);
    }
    
    /**
     * Create a modern pill-style button with icon
     */
    public static JButton createPillButton(String text, Icon icon) {
        return createPillButton(text, icon, BRAND_PRIMARY, Color.WHITE);
    }
    
    /**
     * Create a modern pill-style button with custom colors
     */
    public static JButton createPillButton(String text, Icon icon, Color bgColor, Color textColor) {
        JButton button = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(blend(bgColor, Color.WHITE, 0.1f));
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2d.dispose();
                
                // Text and icon
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // No border for pill buttons
            }
        };
        
        button.setForeground(textColor);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        
        // Add hover animation
        addHoverAnimation(button, bgColor);
        
        return button;
    }
    
    /**
     * Create a GradeRise branded button with gradient
     */
    public static JButton createGradeRiseButton(String text, boolean isGradient) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isGradient) {
                    // Gradient background
                    GradientPaint gradient = new GradientPaint(
                        0, 0, GRADIENT_START,
                        getWidth(), getHeight(), GRADIENT_END
                    );
                    g2d.setPaint(gradient);
                } else {
                    // Solid background
                    if (getModel().isPressed()) {
                        g2d.setColor(BRAND_PRIMARY.darker());
                    } else if (getModel().isRollover()) {
                        g2d.setColor(blend(BRAND_PRIMARY, Color.WHITE, 0.1f));
                    } else {
                        g2d.setColor(BRAND_PRIMARY);
                    }
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BRAND_PRIMARY.darker());
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2d.dispose();
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        
        addHoverAnimation(button, BRAND_PRIMARY);
        
        return button;
    }
    
    /**
     * Create a secondary button (outlined style)
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                if (getModel().isPressed()) {
                    g2d.setColor(HOVER_LIGHT);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2d.setColor(HOVER_LIGHT);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                
                g2d.dispose();
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BORDER_SUBTLE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                g2d.dispose();
            }
        };
        
        button.setForeground(TEXT_DARK);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        
        return button;
    }
    
    // ===== PANEL FACTORY =====
    
    /**
     * Create a rounded card panel
     */
    public static JPanel createCard() {
        return createCard(CARD_LIGHT, 12);
    }
    
    /**
     * Create a rounded card panel with custom color
     */
    public static JPanel createCard(Color backgroundColor, int cornerRadius) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with shadow effect
                g2d.setColor(new Color(0, 0, 0, 10)); // Subtle shadow
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);
                
                g2d.setColor(backgroundColor);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);
                
                g2d.dispose();
            }
        };
    }
    
    /**
     * Create a gradient panel
     */
    public static JPanel createGradientPanel(Color startColor, Color endColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
    }
    
    /**
     * Create analytics card for dashboard
     */
    public static JPanel createAnalyticsCard(String title, JComponent content) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_DARK);
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Content
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    // ===== TEXT FIELD FACTORY =====
    
    /**
     * Create a styled text field with placeholder
     */
    public static PlaceholderTextField createTextField(String placeholder) {
        PlaceholderTextField field = new PlaceholderTextField(placeholder);
        styleTextField(field);
        return field;
    }
    
    /**
     * Create a styled password field with placeholder
     */
    public static PlaceholderPasswordField createPasswordField(String placeholder) {
        PlaceholderPasswordField field = new PlaceholderPasswordField(placeholder);
        styleTextField(field);
        return field;
    }
    
    /**
     * Apply consistent styling to text components
     */
    private static void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(250, 35));
    }
    
    // ===== LABEL FACTORY =====
    
    /**
     * Create a title label
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(TEXT_DARK);
        return label;
    }
    
    /**
     * Create a subtitle label
     */
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(TEXT_MUTED);
        return label;
    }
    
    /**
     * Create a section header label
     */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(TEXT_DARK);
        label.setBorder(new EmptyBorder(10, 0, 5, 0));
        return label;
    }
    
    /**
     * Create a GPA display label
     */
    public static JLabel createGPALabel(double gpa) {
        JLabel label = new JLabel(String.format("%.2f", gpa));
        label.setFont(new Font("SansSerif", Font.BOLD, 32));
        
        // Color code based on GPA
        if (gpa >= 3.8) {
            label.setForeground(SUCCESS_EMERALD);
        } else if (gpa >= 3.0) {
            label.setForeground(INFO_BRAND);
        } else if (gpa >= 2.0) {
            label.setForeground(WARNING_AMBER);
        } else {
            label.setForeground(ERROR_RED);
        }
        
        return label;
    }
    
    // ===== ICON FACTORY =====
    
    /**
     * Create a resized icon
     */
    public static Icon createResizedIcon(Icon originalIcon, int size) {
        if (originalIcon == null) return null;
        
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Scale and center the icon
        int iconWidth = originalIcon.getIconWidth();
        int iconHeight = originalIcon.getIconHeight();
        double scale = Math.min((double) size / iconWidth, (double) size / iconHeight);
        
        int scaledWidth = (int) (iconWidth * scale);
        int scaledHeight = (int) (iconHeight * scale);
        int x = (size - scaledWidth) / 2;
        int y = (size - scaledHeight) / 2;
        
        originalIcon.paintIcon(null, g2d, x, y);
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Blend two colors
     */
    public static Color blend(Color color1, Color color2, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio)); // Clamp ratio
        float inverseRatio = 1f - ratio;
        
        int r = (int) (color1.getRed() * inverseRatio + color2.getRed() * ratio);
        int g = (int) (color1.getGreen() * inverseRatio + color2.getGreen() * ratio);
        int b = (int) (color1.getBlue() * inverseRatio + color2.getBlue() * ratio);
        int a = (int) (color1.getAlpha() * inverseRatio + color2.getAlpha() * ratio);
        
        return new Color(r, g, b, a);
    }
    
    /**
     * Add hover animation to button
     */
    private static void addHoverAnimation(JButton button, Color originalColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(Cursor.getDefaultCursor());
                button.repaint();
            }
        });
    }
    
    /**
     * Apply theme to container and all its children
     */
    public static void applyTheme(Container container, boolean darkMode) {
        if (darkMode) {
            container.setBackground(new Color(35, 35, 35));
            applyDarkTheme(container);
        } else {
            container.setBackground(Color.WHITE);
            applyLightTheme(container);
        }
    }
    
    private static void applyLightTheme(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel) {
                component.setForeground(TEXT_DARK);
            } else if (component instanceof Container) {
                applyLightTheme((Container) component);
            }
        }
    }
    
    private static void applyDarkTheme(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel) {
                component.setForeground(Color.WHITE);
            } else if (component instanceof Container) {
                applyDarkTheme((Container) component);
            }
        }
    }
    
    // ===== CUSTOM COMPONENTS =====
    
    /**
     * Placeholder text field implementation
     */
    public static class PlaceholderTextField extends JTextField {
        private String placeholder;
        
        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_MUTED);
                g2d.setFont(getFont().deriveFont(Font.ITALIC));
                
                FontMetrics metrics = g2d.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                
                g2d.drawString(placeholder, x, y);
                g2d.dispose();
            }
        }
        
        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }
    }
    
    /**
     * Placeholder password field implementation
     */
    public static class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;
        
        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (getPassword().length == 0 && placeholder != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_MUTED);
                g2d.setFont(getFont().deriveFont(Font.ITALIC));
                
                FontMetrics metrics = g2d.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                
                g2d.drawString(placeholder, x, y);
                g2d.dispose();
            }
        }
        
        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }
    }
}