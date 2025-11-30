package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class UIJPanelBG extends JPanel {

    private static final long serialVersionUID = 1L;
    Image image; // Renamed from immagine
    public UIJPanelBG(String imagePath) { // Renamed from immagine
        this(UIJPanelBG.createImageIcon(imagePath).getImage());
    }

    public UIJPanelBG(Image img) {
        this.image = img; // Renamed from immagine
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null); // Renamed from immagine
    }

    public static ImageIcon createImageIcon(final String path) {
        InputStream is = UIJPanelBG.class.getResourceAsStream(path);
        if (is == null) {
            System.err.println("Resource not found: " + path);
            return null;
        }
        try {
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            is.close();
            ImageIcon ii = new ImageIcon(data);
            return ii;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}