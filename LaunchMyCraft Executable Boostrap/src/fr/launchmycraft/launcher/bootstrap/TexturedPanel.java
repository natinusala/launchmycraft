package fr.launchmycraft.launcher.bootstrap;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.geom.Point2D;

import javax.swing.JPanel;


public class TexturedPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Image image;
    private Image bgImage;

    public TexturedPanel(Image bg) {
        setOpaque(true);
        setLayout(new GridBagLayout());
        bgImage = bg;
        bgImage = bgImage.getScaledInstance(32, 32, 16);
    }

    protected void copyImage(final int width, final int height) {
        final Graphics imageGraphics = image.getGraphics();

        for(int x = 0; x <= width / 32; x++)
            for(int y = 0; y <= height / 32; y++)
                imageGraphics.drawImage(bgImage, x * 32, y * 32, null);

        if(imageGraphics instanceof Graphics2D)
            overlayGradient(width, height, (Graphics2D) imageGraphics);

        imageGraphics.dispose();
    }

    protected void overlayGradient(final int width, final int height, final Graphics2D graphics) {
        int gh = 1;
        graphics.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(553648127, true), new Point2D.Float(0.0F, gh), new Color(0, true)));
        graphics.fillRect(0, 0, width, gh);

        gh = height;
        graphics.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(0, true), new Point2D.Float(0.0F, gh), new Color(1610612736, true)));
        graphics.fillRect(0, 0, width, gh);
    }

    @Override
    public void paintComponent(final Graphics graphics) {
        final int width = getWidth() / 2 + 1;
        final int height = getHeight() / 2 + 1;

        if(image == null || image.getWidth(null) != width || image.getHeight(null) != height) {
            image = createImage(width, height);
            copyImage(width, height);
        }

        graphics.drawImage(image, 0, 0, width * 2, height * 2, null);
    }

    @Override
    public void update(final Graphics g) {
        paint(g);
    }
}