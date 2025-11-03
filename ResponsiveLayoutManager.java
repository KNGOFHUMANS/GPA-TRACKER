import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * Responsive Layout Manager - Provides adaptive layouts for different screen sizes
 * Supports breakpoints and responsive component sizing
 */
public class ResponsiveLayoutManager implements LayoutManager2 {
    
    // Breakpoint definitions (in pixels)
    public enum Breakpoint {
        MOBILE(0, 768),
        TABLET(768, 1024), 
        DESKTOP(1024, 1440),
        LARGE_DESKTOP(1440, Integer.MAX_VALUE);
        
        public final int minWidth;
        public final int maxWidth;
        
        Breakpoint(int minWidth, int maxWidth) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
        }
        
        public static Breakpoint fromWidth(int width) {
            for (Breakpoint bp : values()) {
                if (width >= bp.minWidth && width < bp.maxWidth) {
                    return bp;
                }
            }
            return DESKTOP; // fallback
        }
    }
    
    // Responsive configuration for components
    public static class ResponsiveConfig {
        private final Map<Breakpoint, ComponentConfig> configs = new HashMap<>();
        
        public ResponsiveConfig mobile(ComponentConfig config) {
            configs.put(Breakpoint.MOBILE, config);
            return this;
        }
        
        public ResponsiveConfig tablet(ComponentConfig config) {
            configs.put(Breakpoint.TABLET, config);
            return this;
        }
        
        public ResponsiveConfig desktop(ComponentConfig config) {
            configs.put(Breakpoint.DESKTOP, config);
            return this;
        }
        
        public ResponsiveConfig largeDesktop(ComponentConfig config) {
            configs.put(Breakpoint.LARGE_DESKTOP, config);
            return this;
        }
        
        public ComponentConfig getConfig(Breakpoint breakpoint) {
            // Try exact match first
            ComponentConfig config = configs.get(breakpoint);
            if (config != null) return config;
            
            // Fallback to closest smaller breakpoint
            switch (breakpoint) {
                case LARGE_DESKTOP:
                    config = configs.get(Breakpoint.DESKTOP);
                    if (config != null) return config;
                case DESKTOP:
                    config = configs.get(Breakpoint.TABLET);
                    if (config != null) return config;
                case TABLET:
                    config = configs.get(Breakpoint.MOBILE);
                    if (config != null) return config;
                case MOBILE:
                    // Return a default config if nothing is found
                    return new ComponentConfig(1, 1, 8, true);
            }
            return new ComponentConfig(1, 1, 8, true);
        }
    }
    
    // Configuration for individual components at specific breakpoints
    public static class ComponentConfig {
        public final int columns;      // How many columns this component spans
        public final int rows;         // How many rows this component spans
        public final int padding;      // Padding around the component
        public final boolean visible;  // Whether component is visible at this breakpoint
        
        public ComponentConfig(int columns, int rows, int padding, boolean visible) {
            this.columns = Math.max(1, columns);
            this.rows = Math.max(1, rows);
            this.padding = Math.max(0, padding);
            this.visible = visible;
        }
    }
    
    // Grid layout configuration
    private int maxColumns = 12; // 12-column grid system like Bootstrap
    private int baseGutterSize = 16;
    private final Map<Component, ResponsiveConfig> componentConfigs = new HashMap<>();
    private Breakpoint currentBreakpoint = Breakpoint.DESKTOP;
    
    public ResponsiveLayoutManager() {
        this(12, 16);
    }
    
    public ResponsiveLayoutManager(int maxColumns, int gutterSize) {
        this.maxColumns = maxColumns;
        this.baseGutterSize = gutterSize;
    }
    
    // Add component with responsive configuration
    public void addComponent(Component component, ResponsiveConfig config) {
        componentConfigs.put(component, config);
    }
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof ResponsiveConfig) {
            componentConfigs.put(comp, (ResponsiveConfig) constraints);
        } else {
            // Default configuration - full width on mobile, auto on larger screens
            ResponsiveConfig defaultConfig = new ResponsiveConfig()
                .mobile(new ComponentConfig(12, 1, 8, true))
                .tablet(new ComponentConfig(6, 1, 12, true))
                .desktop(new ComponentConfig(4, 1, 16, true))
                .largeDesktop(new ComponentConfig(3, 1, 20, true));
            componentConfigs.put(comp, defaultConfig);
        }
    }
    
    @Override
    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp, (Object) null);
    }
    
    @Override
    public void removeLayoutComponent(Component comp) {
        componentConfigs.remove(comp);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return calculateLayoutSize(parent, false);
    }
    
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return calculateLayoutSize(parent, true);
    }
    
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            // Note: availableHeight may be used in future versions for vertical layout constraints
            
            // Determine current breakpoint
            currentBreakpoint = Breakpoint.fromWidth(availableWidth);
            
            // Calculate column width
            int gutterSize = getResponsiveGutterSize();
            int totalGutters = (maxColumns - 1) * gutterSize;
            int columnWidth = (availableWidth - totalGutters) / maxColumns;
            
            // Layout components in grid
            int currentX = insets.left;
            int currentY = insets.top;
            int rowHeight = 0;
            int columnsUsed = 0;
            
            Component[] components = getVisibleComponents(parent);
            
            for (Component component : components) {
                ResponsiveConfig config = componentConfigs.get(component);
                if (config == null) continue;
                
                ComponentConfig compConfig = config.getConfig(currentBreakpoint);
                if (!compConfig.visible) {
                    component.setVisible(false);
                    continue;
                }
                
                component.setVisible(true);
                
                // Check if we need to wrap to next row
                if (columnsUsed > 0 && columnsUsed + compConfig.columns > maxColumns) {
                    currentX = insets.left;
                    currentY += rowHeight + gutterSize;
                    rowHeight = 0;
                    columnsUsed = 0;
                }
                
                // Calculate component dimensions
                int compWidth = columnWidth * compConfig.columns + 
                              gutterSize * (compConfig.columns - 1);
                
                Dimension preferredSize = component.getPreferredSize();
                int compHeight = Math.max(preferredSize.height, 
                                        calculateResponsiveHeight(compConfig, columnWidth));
                
                // Set component bounds with padding
                component.setBounds(
                    currentX + compConfig.padding,
                    currentY + compConfig.padding,
                    compWidth - (compConfig.padding * 2),
                    compHeight - (compConfig.padding * 2)
                );
                
                // Update position tracking
                currentX += compWidth + gutterSize;
                columnsUsed += compConfig.columns;
                rowHeight = Math.max(rowHeight, compHeight);
            }
        }
    }
    
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
    
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }
    
    @Override
    public void invalidateLayout(Container target) {
        // Layout will be recalculated on next layout pass
    }
    
    private Dimension calculateLayoutSize(Container parent, boolean minimum) {
        Insets insets = parent.getInsets();
        int width = insets.left + insets.right;
        int height = insets.top + insets.bottom;
        
        // Calculate based on current breakpoint or assume desktop for sizing
        Breakpoint breakpoint = currentBreakpoint != null ? currentBreakpoint : Breakpoint.DESKTOP;
        
        Component[] components = getVisibleComponents(parent);
        
        if (components.length == 0) {
            return new Dimension(width + 300, height + 200); // Minimum container size
        }
        
        // Simulate layout to calculate required dimensions
        int maxWidth = 0;
        int totalHeight = 0;
        int currentRowWidth = 0;
        int currentRowHeight = 0;
        int columnsUsed = 0;
        
        int gutterSize = getResponsiveGutterSize();
        int columnWidth = 200; // Estimated column width for calculation
        
        for (Component component : components) {
            ResponsiveConfig config = componentConfigs.get(component);
            if (config == null) continue;
            
            ComponentConfig compConfig = config.getConfig(breakpoint);
            if (!compConfig.visible) continue;
            
            // Check if we need to wrap to next row
            if (columnsUsed > 0 && columnsUsed + compConfig.columns > maxColumns) {
                maxWidth = Math.max(maxWidth, currentRowWidth);
                totalHeight += currentRowHeight + gutterSize;
                currentRowWidth = 0;
                currentRowHeight = 0;
                columnsUsed = 0;
            }
            
            // Calculate component size
            int compWidth = columnWidth * compConfig.columns + gutterSize * (compConfig.columns - 1);
            Dimension compSize = minimum ? component.getMinimumSize() : component.getPreferredSize();
            int compHeight = Math.max(compSize.height, calculateResponsiveHeight(compConfig, columnWidth));
            
            currentRowWidth += compWidth + (columnsUsed > 0 ? gutterSize : 0);
            currentRowHeight = Math.max(currentRowHeight, compHeight);
            columnsUsed += compConfig.columns;
        }
        
        // Add final row
        maxWidth = Math.max(maxWidth, currentRowWidth);
        totalHeight += currentRowHeight;
        
        return new Dimension(width + maxWidth, height + totalHeight);
    }
    
    private Component[] getVisibleComponents(Container parent) {
        List<Component> visibleComponents = new ArrayList<>();
        
        for (Component component : parent.getComponents()) {
            ResponsiveConfig config = componentConfigs.get(component);
            if (config != null) {
                ComponentConfig compConfig = config.getConfig(currentBreakpoint);
                if (compConfig.visible) {
                    visibleComponents.add(component);
                }
            } else if (component.isVisible()) {
                visibleComponents.add(component);
            }
        }
        
        return visibleComponents.toArray(new Component[0]);
    }
    
    private int getResponsiveGutterSize() {
        switch (currentBreakpoint) {
            case MOBILE: return baseGutterSize / 2;
            case TABLET: return baseGutterSize;
            case DESKTOP: return (int) (baseGutterSize * 1.25);
            case LARGE_DESKTOP: return (int) (baseGutterSize * 1.5);
            default: return baseGutterSize;
        }
    }
    
    private int calculateResponsiveHeight(ComponentConfig config, int columnWidth) {
        // Calculate responsive height based on aspect ratio or minimum height
        int minHeight = Math.max(40, columnWidth / 4); // Minimum reasonable height
        return minHeight * config.rows;
    }
}

/**
 * Responsive Container - A container that automatically handles responsive layout
 */
class ResponsiveContainer extends JPanel {
    private final ResponsiveLayoutManager layoutManager;
    
    public ResponsiveContainer() {
        this.layoutManager = new ResponsiveLayoutManager();
        setLayout(layoutManager);
        setupResponsiveListener();
    }
    
    public ResponsiveContainer(int maxColumns, int gutterSize) {
        this.layoutManager = new ResponsiveLayoutManager(maxColumns, gutterSize);
        setLayout(layoutManager);
        setupResponsiveListener();
    }
    
    private void setupResponsiveListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Trigger re-layout when container is resized
                revalidate();
                repaint();
            }
        });
    }
    
    public void addComponent(Component component, ResponsiveLayoutManager.ResponsiveConfig config) {
        add(component, config);
    }
    
    // Convenience methods for common responsive patterns
    public void addFullWidth(Component component) {
        ResponsiveLayoutManager.ResponsiveConfig config = new ResponsiveLayoutManager.ResponsiveConfig()
            .mobile(new ResponsiveLayoutManager.ComponentConfig(12, 1, 8, true))
            .tablet(new ResponsiveLayoutManager.ComponentConfig(12, 1, 12, true))
            .desktop(new ResponsiveLayoutManager.ComponentConfig(12, 1, 16, true))
            .largeDesktop(new ResponsiveLayoutManager.ComponentConfig(12, 1, 20, true));
        addComponent(component, config);
    }
    
    public void addHalfWidth(Component component) {
        ResponsiveLayoutManager.ResponsiveConfig config = new ResponsiveLayoutManager.ResponsiveConfig()
            .mobile(new ResponsiveLayoutManager.ComponentConfig(12, 1, 8, true))
            .tablet(new ResponsiveLayoutManager.ComponentConfig(6, 1, 12, true))
            .desktop(new ResponsiveLayoutManager.ComponentConfig(6, 1, 16, true))
            .largeDesktop(new ResponsiveLayoutManager.ComponentConfig(6, 1, 20, true));
        addComponent(component, config);
    }
    
    public void addThirdWidth(Component component) {
        ResponsiveLayoutManager.ResponsiveConfig config = new ResponsiveLayoutManager.ResponsiveConfig()
            .mobile(new ResponsiveLayoutManager.ComponentConfig(12, 1, 8, true))
            .tablet(new ResponsiveLayoutManager.ComponentConfig(6, 1, 12, true))
            .desktop(new ResponsiveLayoutManager.ComponentConfig(4, 1, 16, true))
            .largeDesktop(new ResponsiveLayoutManager.ComponentConfig(4, 1, 20, true));
        addComponent(component, config);
    }
    
    public void addQuarterWidth(Component component) {
        ResponsiveLayoutManager.ResponsiveConfig config = new ResponsiveLayoutManager.ResponsiveConfig()
            .mobile(new ResponsiveLayoutManager.ComponentConfig(12, 1, 8, true))
            .tablet(new ResponsiveLayoutManager.ComponentConfig(6, 1, 12, true))
            .desktop(new ResponsiveLayoutManager.ComponentConfig(3, 1, 16, true))
            .largeDesktop(new ResponsiveLayoutManager.ComponentConfig(3, 1, 20, true));
        addComponent(component, config);
    }
}