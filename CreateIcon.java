import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Simple utility to create a GradeRise icon file for Launch4j
 */
public class CreateIcon {
    public static void main(String[] args) {
        try {
            // Create the icon directly using the same drawing logic
            BufferedImage iconImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = iconImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // GradeRise icon drawing logic
            int width = 128, height = 128;
            
            // Background
            g2.setColor(new Color(240, 82, 82)); // Coral red #F05252
            g2.fillOval(0, 0, width, height);
            
            // Graduation cap base
            g2.setColor(new Color(139, 34, 52)); // Dark red #8B2234
            int capWidth = (int)(width * 0.6);
            int capHeight = (int)(height * 0.15);
            int capX = (width - capWidth) / 2;
            int capY = (int)(height * 0.35);
            g2.fillRect(capX, capY, capWidth, capHeight);
            
            // Graduation cap top
            int topWidth = (int)(width * 0.7);
            int topHeight = (int)(height * 0.08);
            int topX = (width - topWidth) / 2;
            int topY = capY - topHeight;
            g2.fillRect(topX, topY, topWidth, topHeight);
            
            // Tassel
            g2.setColor(new Color(220, 150, 50)); // Gold #DC9632
            int tasselX = capX + capWidth - 5;
            int tasselY = capY - 5;
            g2.fillRect(tasselX, tasselY, 3, 20);
            
            // Tassel end
            g2.fillOval(tasselX - 2, tasselY + 18, 7, 7);
            
            g2.dispose();
            
            // Save as PNG (Launch4j can convert PNG to ICO)
            File outputFile = new File("graderise-icon.png");
            ImageIO.write(iconImage, "PNG", outputFile);
            
            System.out.println("✅ Icon created successfully: " + outputFile.getAbsolutePath());
            System.out.println("📁 File size: " + outputFile.length() + " bytes");
            
        } catch (Exception e) {
            System.err.println("❌ Error creating icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
}