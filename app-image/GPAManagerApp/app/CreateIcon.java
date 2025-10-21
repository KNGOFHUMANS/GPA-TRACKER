import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class CreateIcon {
    public static void main(String[] args) {
        try {
            // Create the red graduation cap icon
            BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Clear background (transparent)
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, 256, 256);
            
            // Colors for the graduation cap
            Color redCap = new Color(220, 38, 38);    // Nice red for the cap
            Color darkRed = new Color(153, 27, 27);   // Darker red for depth
            Color black = new Color(30, 30, 30);      // Black for tassel
            Color gold = new Color(251, 191, 36);     // Gold for tassel button
            
            // Draw graduation cap base (the part that sits on head)
            g2d.setColor(darkRed);
            g2d.fillOval(64, 140, 128, 60); // Base of cap (ellipse)
            
            // Draw graduation cap top (mortarboard/square top)
            g2d.setColor(redCap);
            // Create a perspective square (slightly rotated)
            int[] capX = {40, 216, 200, 56};
            int[] capY = {100, 80, 120, 140};
            g2d.fillPolygon(capX, capY, 4);
            
            // Add some depth with darker edge
            g2d.setColor(darkRed);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolygon(capX, capY, 4);
            
            // Draw tassel cord
            g2d.setColor(black);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(200, 100, 220, 140);
            
            // Draw tassel
            g2d.setColor(black);
            for (int i = 0; i < 8; i++) {
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(220 + (i * 2), 140, 215 + (i * 3), 170 + (i * 2));
            }
            
            // Draw tassel button
            g2d.setColor(gold);
            g2d.fillOval(216, 96, 8, 8);
            
            // Add highlight to the cap for 3D effect
            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.fillPolygon(new int[]{50, 180, 170, 60}, new int[]{110, 95, 105, 125}, 4);
            
            g2d.dispose();
            
            // Save as PNG
            ImageIO.write(image, "PNG", new File("app-icon.png"));
            System.out.println("Icon saved as app-icon.png");
            
            // Create smaller versions for different contexts
            BufferedImage icon64 = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g64 = icon64.createGraphics();
            g64.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g64.drawImage(image, 0, 0, 64, 64, null);
            g64.dispose();
            ImageIO.write(icon64, "PNG", new File("app-icon-64.png"));
            
            BufferedImage icon32 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g32 = icon32.createGraphics();
            g32.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g32.drawImage(image, 0, 0, 32, 32, null);
            g32.dispose();
            ImageIO.write(icon32, "PNG", new File("app-icon-32.png"));
            
            System.out.println("Created icon files: app-icon.png, app-icon-64.png, app-icon-32.png");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}