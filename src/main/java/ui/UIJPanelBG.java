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
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public void setImage(Image img) {
        this.img = img;
        this.repaint();
    }
}