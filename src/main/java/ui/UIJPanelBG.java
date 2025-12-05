package ui;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

public class UIJPanelBG extends JPanel {
    private Image img;

    public UIJPanelBG(Image img) {
        this.img = img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            // FIX: Scale the image to fill the entire panel
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    // Optional: Helper to update image if needed dynamically
    public void setImage(Image img) {
        this.img = img;
        this.repaint();
    }
}