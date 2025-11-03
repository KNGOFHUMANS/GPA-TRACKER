import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Modern UI Framework - Enhanced Swing components with contemporary design
 * Provides dark/light theme support, responsive design, and modern aesthetics
 */
public class ModernUIFramework {
    
    // Theme Management
    private static boolean isDarkTheme = true;
    private static final Map<String, ThemeColors> themes = new HashMap<>();
    
    // Animation System
    private static final javax.swing.Timer animationTimer = new javax.swing.Timer(16, null); // 60 FPS
    private static final Map<Component, Animation> activeAnimations = new HashMap<>();
    
    // Responsive Design
    // Screen size is calculated dynamically when needed
    
    static {
        initializeThemes();
        setupGlobalLookAndFeel();
    }
    
    // Theme Color Definitions
    public static class ThemeColors {
        public final Color primary;
        public final Color secondary;
        public final Color background;
        public final Color surface;
        public final Color surfaceVariant;
        public final Color onPrimary;
        public final Color onSecondary;
        public final Color onBackground;
        public final Color onSurface;
        public final Color accent;
        public final Color error;
        public final Color warning;
        public final Color success;
        public final Color border;
        public final Color shadow;
        public final Color cardBackground;
        public final Color inputBackground;
        public final Color buttonHover;
        
        public ThemeColors(Color primary, Color secondary, Color background, Color surface,
                          Color surfaceVariant, Color onPrimary, Color onSecondary,
                          Color onBackground, Color onSurface, Color accent,
                          Color error, Color warning, Color success, Color border,
                          Color shadow, Color cardBackground, Color inputBackground,
                          Color buttonHover) {
            this.primary = primary;
            this.secondary = secondary;
            this.background = background;
            this.surface = surface;
            this.surfaceVariant = surfaceVariant;
            this.onPrimary = onPrimary;
            this.onSecondary = onSecondary;
            this.onBackground = onBackground;
            this.onSurface = onSurface;
            this.accent = accent;
            this.error = error;
            this.warning = warning;
            this.success = success;
            this.border = border;
            this.shadow = shadow;
            this.cardBackground = cardBackground;
            this.inputBackground = inputBackground;
            this.buttonHover = buttonHover;
        }
    }
    
    // Initialize theme definitions
    private static void initializeThemes() {
        // Dark Theme (GradeRise brand colors - dark gray and dark red)
        themes.put("dark", new ThemeColors(
            new Color(0x8B0000),    // primary - dark red (GradeRise brand)
            new Color(0x4A4A4A),    // secondary - medium gray
            new Color(0x1A1A1A),    // background - very dark gray
            new Color(0x2D2D2D),    // surface - dark gray elevation 1
            new Color(0x3A3A3A),    // surface variant - medium dark gray
            new Color(0xFFFFFF),    // on primary - white
            new Color(0xE0E0E0),    // on secondary - light gray
            new Color(0xF0F0F0),    // on background - very light gray
            new Color(0xF0F0F0),    // on surface - very light gray
            new Color(0xDC143C),    // accent - crimson red
            new Color(0xFF6B6B),    // error - light red
            new Color(0xFFB347),    // warning - orange
            new Color(0x4ECDC4),    // success - teal
            new Color(0x555555),    // border - medium dark gray
            new Color(0x00000060),  // shadow - transparent black
            new Color(0x353535),    // card background - elevated dark gray
            new Color(0x404040),    // input background - darker gray
            new Color(0xA50000)     // button hover - deeper dark red
        ));
        
        // Light Theme (GradeRise brand colors - light version)
        themes.put("light", new ThemeColors(
            new Color(0x8B0000),    // primary - dark red (GradeRise brand)
            new Color(0x666666),    // secondary - medium gray
            new Color(0xF8F8F8),    // background - very light gray
            new Color(0xFFFFFF),    // surface - pure white
            new Color(0xF0F0F0),    // surface variant - light gray
            new Color(0xFFFFFF),    // on primary - white
            new Color(0x2D2D2D),    // on secondary - dark gray
            new Color(0x1A1A1A),    // on background - very dark
            new Color(0x1A1A1A),    // on surface - very dark
            new Color(0xDC143C),    // accent - crimson red
            new Color(0xD32F2F),    // error - red
            new Color(0xF57C00),    // warning - orange
            new Color(0x388E3C),    // success - green
            new Color(0xCCCCCC),    // border - light gray
            new Color(0x00000015),  // shadow - very light transparent black
            new Color(0xFFFFFF),    // card background - pure white
            new Color(0xF5F5F5),    // input background - very light gray
            new Color(0xA50000)     // button hover - deeper red
        ));
    }
    
    // Get current theme colors
    public static ThemeColors getTheme() {
        // Always map to ModernThemeSystem to ensure selected color scheme is respected
        try {
            ModernThemeSystem.ThemeColors mc = ModernThemeSystem.getCurrentColors();
            boolean dark = ModernThemeSystem.getCurrentTheme() == ModernThemeSystem.Theme.DARK;
            isDarkTheme = dark;
            Color primary = mc.primary;
            Color secondary = mc.secondary != null ? mc.secondary : mc.primary;
            Color background = mc.background;
            Color surface = mc.surface;
            Color surfaceVariant = mc.surfaceVariant;
            Color onSurface = mc.text;
            Color onBackground = mc.text;
            Color onPrimary = getReadableOnPrimary(primary);
            Color accent = mc.accent != null ? mc.accent : mc.primary;
            Color border = mc.border;
            Color shadow = dark ? new Color(0x00000060, true) : new Color(0x00000015, true);
            Color cardBackground = surface;
            Color inputBackground = surfaceVariant;
            Color buttonHover = dark ? darken(primary, 0.15f) : lighten(primary, 0.15f);
            // Keep semantic defaults
            Color error = new Color(0xD32F2F);
            Color warning = new Color(0xF59E0B);
            Color success = new Color(0x10B981);
            return new ThemeColors(
                primary, secondary, background, surface, surfaceVariant,
                onPrimary, onSurface, onBackground, onSurface, accent,
                error, warning, success, border, shadow, cardBackground,
                inputBackground, buttonHover
            );
        } catch (Throwable t) {
            // Fallback to local theme map if ModernThemeSystem is unavailable early
            return themes.get(isDarkTheme ? "dark" : "light");
        }
    }
    
    // Theme switching
    public static void setDarkTheme(boolean dark) {
        isDarkTheme = dark;
        try {
            ModernThemeSystem.setTheme(dark ? ModernThemeSystem.Theme.DARK : ModernThemeSystem.Theme.LIGHT);
        } catch (Throwable ignored) {}
        notifyThemeChange();
    }
    
    public static boolean isDarkTheme() {
        try {
            return ModernThemeSystem.getCurrentTheme() == ModernThemeSystem.Theme.DARK;
        } catch (Throwable t) {
            return isDarkTheme;
        }
    }
    
    // Setup global look and feel improvements
    private static void setupGlobalLookAndFeel() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("TextField.selectionBackground", getTheme().accent);
        UIManager.put("TextArea.selectionBackground", getTheme().accent);
    }
    
    // Animation System
    private static class Animation {
        private final Component component;
        private final long duration;
        private final long startTime;
        private final AnimationCallback callback;
        private boolean completed = false;
        
        public Animation(Component component, long duration, AnimationCallback callback) {
            this.component = component;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.callback = callback;
        }
        
        public boolean update() {
            if (completed) return false;
            
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / duration);
            
            // Easing function - cubic bezier for smooth animations
            float easedProgress = easeInOutCubic(progress);
            
            callback.onFrame(component, easedProgress);
            
            if (progress >= 1.0f) {
                callback.onComplete(component);
                completed = true;
                return false;
            }
            return true;
        }
        
        private float easeInOutCubic(float t) {
            return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
        }
    }
    
    private interface AnimationCallback {
        void onFrame(Component component, float progress);
        default void onComplete(Component component) {}
    }
    
    // Start animation
    public static void animate(Component component, long duration, AnimationCallback callback) {
        activeAnimations.put(component, new Animation(component, duration, callback));
        
        if (!animationTimer.isRunning()) {
            animationTimer.addActionListener(e -> {
                activeAnimations.entrySet().removeIf(entry -> !entry.getValue().update());
                if (activeAnimations.isEmpty()) {
                    animationTimer.stop();
                }
            });
            animationTimer.start();
        }
    }
    
    // Modern Button Component
    public static class ModernButton extends JButton {
        private boolean isHovered = false;
        private boolean isPressed = false;
        private ButtonStyle style = ButtonStyle.FILLED;
        private float elevation = 0f;
        private Color customColor = null;
        
        public enum ButtonStyle {
            FILLED, OUTLINED, TEXT, FAB
        }
        
        public ModernButton(String text) {
            super(text);
            init();
        }
        
        public ModernButton(String text, ButtonStyle style) {
            super(text);
            this.style = style;
            init();
        }
        
        public ModernButton(String text, Icon icon) {
            super(text, icon);
            init();
        }
        
        private void init() {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Set appropriate size based on style
            if (style == ButtonStyle.FAB) {
                setPreferredSize(new Dimension(56, 56));
            } else {
                setPreferredSize(new Dimension(120, 40));
            }
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    animateElevation(style == ButtonStyle.FAB ? 8f : 4f);
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    animateElevation(style == ButtonStyle.FAB ? 6f : 1f);
                    repaint();
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    animateElevation(style == ButtonStyle.FAB ? 12f : 8f);
                    repaint();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    animateElevation(isHovered ? (style == ButtonStyle.FAB ? 8f : 4f) : 
                                   (style == ButtonStyle.FAB ? 6f : 1f));
                    repaint();
                }
            });
        }
        
        public void setButtonStyle(ButtonStyle style) {
            this.style = style;
            repaint();
        }
        
        public void setCustomColor(Color color) {
            this.customColor = color;
            repaint();
        }
        
        private void animateElevation(float targetElevation) {
            float startElevation = elevation;
            animate(this, 150, new AnimationCallback() {
                @Override
                public void onFrame(Component component, float progress) {
                    elevation = startElevation + (targetElevation - startElevation) * progress;
                    component.repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            ThemeColors theme = getTheme();
            int width = getWidth();
            int height = getHeight();
            
            // Draw shadow if elevated
            if (elevation > 0 && style != ButtonStyle.TEXT) {
                drawElevationShadow(g2, width, height, elevation);
            }
            
            // Draw button background
            RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                0, 0, width, height, 
                style == ButtonStyle.FAB ? height : 8, 
                style == ButtonStyle.FAB ? height : 8
            );
            
            Color backgroundColor = getButtonBackgroundColor(theme);
            Color foregroundColor = getButtonForegroundColor(theme);
            
            switch (style) {
                case FILLED:
                case FAB:
                    g2.setColor(backgroundColor);
                    g2.fill(rect);
                    break;
                case OUTLINED:
                    g2.setColor(theme.surface);
                    g2.fill(rect);
                    g2.setColor(backgroundColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(rect);
                    break;
                case TEXT:
                    if (isHovered || isPressed) {
                        g2.setColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), 
                                            backgroundColor.getBlue(), 20));
                        g2.fill(rect);
                    }
                    break;
            }
            
            // Draw text and icon
            g2.setColor(foregroundColor);
            FontMetrics fm = g2.getFontMetrics(getFont());
            
            if (getIcon() != null && style == ButtonStyle.FAB) {
                // Center icon for FAB
                int iconX = (width - getIcon().getIconWidth()) / 2;
                int iconY = (height - getIcon().getIconHeight()) / 2;
                getIcon().paintIcon(this, g2, iconX, iconY);
            } else {
                // Draw text (and icon if present)
                String text = getText();
                if (text != null && !text.isEmpty()) {
                    int textX = (width - fm.stringWidth(text)) / 2;
                    int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, textX, textY);
                }
            }
            
            g2.dispose();
        }
        
        private Color getButtonBackgroundColor(ThemeColors theme) {
            if (customColor != null) return customColor;
            
            switch (style) {
                case FILLED:
                case FAB:
                    return isPressed ? darken(theme.primary, 0.2f) : 
                           isHovered ? theme.buttonHover : theme.primary;
                case OUTLINED:
                case TEXT:
                    return theme.primary;
                default:
                    return theme.primary;
            }
        }
        
        private Color getButtonForegroundColor(ThemeColors theme) {
            switch (style) {
                case FILLED:
                case FAB:
                    return theme.onPrimary;
                case OUTLINED:
                case TEXT:
                    return theme.primary;
                default:
                    return theme.onPrimary;
            }
        }
    }
    
    // Modern TextField Component
    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        private boolean isFocused = false;
        private boolean hasError = false;
        private JLabel helperLabel;
        private float animationProgress = 0f;
        
        public ModernTextField() {
            init();
        }
        
        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            init();
        }
        
        private void init() {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(200, 56));
            setBorder(new EmptyBorder(16, 16, 8, 16));
            setOpaque(false);
            
            // Set text colors using ModernThemeSystem for better integration
            try {
                setForeground(ModernThemeSystem.getCurrentColors().text);
                setCaretColor(ModernThemeSystem.getCurrentColors().text);
            } catch (Exception e) {
                // Fallback colors
                setForeground(getTheme().onSurface);
                setCaretColor(getTheme().onSurface);
            }
            
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    animateFocus(true);
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    animateFocus(false);
                }
            });
        }
        
        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }
        
        public void setError(boolean hasError, String message) {
            this.hasError = hasError;
            if (helperLabel != null) {
                helperLabel.setText(hasError ? message : "");
                helperLabel.setForeground(hasError ? getTheme().error : getTheme().onSurface);
            }
            repaint();
        }
        
        public JLabel getHelperLabel() {
            if (helperLabel == null) {
                helperLabel = new JLabel();
                helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                helperLabel.setForeground(getTheme().onSurface);
            }
            return helperLabel;
        }
        
        private void animateFocus(boolean focused) {
            float target = focused ? 1f : 0f;
            animate(this, 200, new AnimationCallback() {
                @Override
                public void onFrame(Component component, float progress) {
                    animationProgress = animationProgress + (target - animationProgress) * progress;
                    component.repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            ThemeColors theme = getTheme();
            int width = getWidth();
            int height = getHeight();
            
            // Draw background using ModernThemeSystem colors for better integration
            RoundRectangle2D.Float background = new RoundRectangle2D.Float(0, 0, width, height, 12, 12);
            try {
                g2.setColor(ModernThemeSystem.getCurrentColors().surfaceVariant);
            } catch (Exception e) {
                g2.setColor(theme.inputBackground);
            }
            g2.fill(background);
            
            // Draw border
            Color borderColor = hasError ? theme.error : 
                              isFocused ? theme.primary : theme.border;
            float borderWidth = isFocused ? 2f : 1f;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.draw(background);
            
            // Draw label/placeholder
            String text = getText();
            boolean isEmpty = text == null || text.isEmpty();
            
            if (!placeholder.isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                
                if (isEmpty && !isFocused) {
                    // Show placeholder using ModernThemeSystem colors for better visibility
                    try {
                        g2.setColor(ModernThemeSystem.getPlaceholderTextColor());
                    } catch (Exception e) {
                        // Fallback to a more visible color for dark themes
                        g2.setColor(isDarkTheme() ? new Color(200, 200, 200) : new Color(100, 100, 100));
                    }
                    g2.drawString(placeholder, 16, (height + fm.getAscent() - fm.getDescent()) / 2);
                } else {
                    // Show floating label
                    float labelSize = 12f - animationProgress * 2f;
                    Font labelFont = getFont().deriveFont(labelSize);
                    g2.setFont(labelFont);
                    FontMetrics labelFm = g2.getFontMetrics();
                    
                    int labelY = (int) (8 + labelFm.getAscent() + animationProgress * 5);
                    g2.setColor(isFocused ? theme.primary : theme.onSurface);
                    g2.drawString(placeholder, 16, labelY);
                }
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // Modern Card Component
    public static class ModernCard extends JPanel {
        private float elevation = 1f;
        private boolean isClickable = false;
        
        public ModernCard() {
            init();
        }
        
        public ModernCard(float elevation) {
            this.elevation = elevation;
            init();
        }
        
        private void init() {
            setOpaque(false);
            setBorder(new EmptyBorder(16, 16, 16, 16));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isClickable) {
                        animateElevation(elevation + 4f);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (isClickable) {
                        animateElevation(elevation);
                    }
                }
            });
        }
        
        public void setClickable(boolean clickable) {
            this.isClickable = clickable;
            setCursor(clickable ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        }
        
        public void setElevation(float elevation) {
            this.elevation = elevation;
            repaint();
        }
        
        private void animateElevation(float targetElevation) {
            float startElevation = elevation;
            animate(this, 150, new AnimationCallback() {
                @Override
                public void onFrame(Component component, float progress) {
                    elevation = startElevation + (targetElevation - startElevation) * progress;
                    component.repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            ThemeColors theme = getTheme();
            int width = getWidth();
            int height = getHeight();
            
            // Draw shadow
            if (elevation > 0) {
                drawElevationShadow(g2, width, height, elevation);
            }
            
            // Draw card background
            RoundRectangle2D.Float card = new RoundRectangle2D.Float(0, 0, width, height, 16, 16);
            g2.setColor(theme.cardBackground);
            g2.fill(card);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // Modern Progress Bar
    public static class ModernProgressBar extends JProgressBar {
        private boolean isIndeterminate = false;
        private float animationOffset = 0f;
        
        public ModernProgressBar() {
            init();
        }
        
        public ModernProgressBar(int min, int max) {
            super(min, max);
            init();
        }
        
        private void init() {
            setStringPainted(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(200, 8));
            setUI(new ModernProgressBarUI());
        }
        
        @Override
        public void setIndeterminate(boolean newValue) {
            super.setIndeterminate(newValue);
            isIndeterminate = newValue;
            
            if (isIndeterminate) {
                startIndeterminateAnimation();
            }
        }
        
        private void startIndeterminateAnimation() {
            javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
                animationOffset += 0.02f;
                if (animationOffset > 1f) animationOffset = 0f;
                repaint();
            });
            timer.start();
            
            // Stop animation when component is no longer indeterminate
            addPropertyChangeListener("indeterminate", evt -> {
                if (!isIndeterminate()) {
                    timer.stop();
                }
            });
        }
        
        private class ModernProgressBarUI extends BasicProgressBarUI {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                ThemeColors theme = getTheme();
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Background track
                RoundRectangle2D.Float track = new RoundRectangle2D.Float(0, 0, width, height, height, height);
                g2.setColor(theme.surfaceVariant);
                g2.fill(track);
                
                // Progress fill
                float progress = (float) progressBar.getValue() / progressBar.getMaximum();
                int progressWidth = (int) (width * progress);
                
                if (progressWidth > 0) {
                    RoundRectangle2D.Float fill = new RoundRectangle2D.Float(0, 0, progressWidth, height, height, height);
                    g2.setColor(theme.primary);
                    g2.fill(fill);
                }
                
                g2.dispose();
            }
            
            @Override
            protected void paintIndeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                ThemeColors theme = getTheme();
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Background track
                RoundRectangle2D.Float track = new RoundRectangle2D.Float(0, 0, width, height, height, height);
                g2.setColor(theme.surfaceVariant);
                g2.fill(track);
                
                // Animated progress indicator
                float segmentWidth = width * 0.3f;
                float x = (width + segmentWidth) * animationOffset - segmentWidth;
                
                if (x + segmentWidth > 0 && x < width) {
                    RoundRectangle2D.Float fill = new RoundRectangle2D.Float(
                        Math.max(0, x), 0, 
                        Math.min(segmentWidth, width - Math.max(0, x)), height, 
                        height, height
                    );
                    g2.setColor(theme.primary);
                    g2.fill(fill);
                }
                
                g2.dispose();
            }
        }
    }
    
    // Utility Methods
    private static void drawElevationShadow(Graphics2D g2, int width, int height, float elevation) {
        float shadowOffset = elevation * 0.5f;
        float shadowBlur = elevation * 2f;
        
        // Create gradient for shadow blur effect
        int steps = Math.max(1, (int) shadowBlur);
        float alpha = 0.1f / steps;
        
        for (int i = 0; i < steps; i++) {
            float offset = i * 0.5f;
            Color shadowColor = new Color(0, 0, 0, alpha);
            g2.setColor(shadowColor);
            g2.setStroke(new BasicStroke(1f));
            
            RoundRectangle2D.Float blurShape = new RoundRectangle2D.Float(
                shadowOffset + offset, shadowOffset + offset, 
                width - offset * 2, height - offset * 2, 16, 16
            );
            g2.draw(blurShape);
        }
    }
    
    private static Color darken(Color color, float factor) {
        int r = Math.max(0, (int) (color.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - factor)));
        return new Color(r, g, b, color.getAlpha());
    }
    
    private static Color lighten(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    private static Color getReadableOnPrimary(Color primary) {
        // Calculate perceived luminance and choose black or white for contrast
        double luminance = (0.299 * primary.getRed() + 0.587 * primary.getGreen() + 0.114 * primary.getBlue()) / 255.0;
        return luminance > 0.6 ? Color.BLACK : Color.WHITE;
    }
    

    
    // Theme change notification
    private static final List<Component> themeListeners = new ArrayList<>();
    
    public static void addThemeListener(Component component) {
        themeListeners.add(component);
    }
    
    public static void removeThemeListener(Component component) {
        themeListeners.remove(component);
    }
    
    private static void notifyThemeChange() {
        SwingUtilities.invokeLater(() -> {
            for (Component component : themeListeners) {
                if (component != null) {
                    updateComponentTheme(component);
                }
            }
        });
    }
    
    private static void updateComponentTheme(Component component) {
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                updateComponentTheme(child);
            }
        }
        component.repaint();
    }
    
    // Responsive Design Utilities
    public static Dimension getResponsiveSize(Dimension baseSize) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        float scale = Math.min(screenSize.width / 1920f, screenSize.height / 1080f);
        scale = Math.max(0.8f, Math.min(2.0f, scale)); // Clamp between 0.8x and 2.0x
        
        return new Dimension(
            (int) (baseSize.width * scale),
            (int) (baseSize.height * scale)
        );
    }
    
    public static Font getResponsiveFont(Font baseFont) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        float scale = Math.min(screenSize.width / 1920f, screenSize.height / 1080f);
        scale = Math.max(0.8f, Math.min(1.5f, scale));
        
        return baseFont.deriveFont(baseFont.getSize() * scale);
    }
    
    // Loading Animation Component
    public static class LoadingSpinner extends JComponent {
        private float rotation = 0f;
        private final javax.swing.Timer animationTimer;
        
        public LoadingSpinner() {
            setPreferredSize(new Dimension(32, 32));
            animationTimer = new javax.swing.Timer(16, e -> {
                rotation += 8f;
                if (rotation >= 360f) rotation = 0f;
                repaint();
            });
        }
        
        public void start() {
            animationTimer.start();
        }
        
        public void stop() {
            animationTimer.stop();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            ThemeColors theme = getTheme();
            int size = Math.min(getWidth(), getHeight());
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            g2.translate(centerX, centerY);
            g2.rotate(Math.toRadians(rotation));
            
            // Draw spinning arc
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(theme.primary);
            g2.drawArc(-size/4, -size/4, size/2, size/2, 0, 270);
            
            g2.dispose();
        }
    }
}