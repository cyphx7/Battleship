package ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FrameSplashscreen extends JFrame {

    private static final long serialVersionUID = 1L;

    public FrameSplashscreen() {
        super("Battleship - Pirate Edition");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setSize(600, 350);

        // 1. Safe Load App Icon
        try {
            URL iconUrl = getClass().getResource("/res/images/icon.png");
            if (iconUrl != null) {
                this.setIconImage(ImageIO.read(iconUrl));
            }
        } catch (IOException e) {
            System.err.println("Could not load app icon.");
        }

        // Center the frame
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        JPanel container = new JPanel(null);

        // 2. Safe Load Background Image
        BufferedImage bgImage = null;
        try {
            // Note: Ensure your file is named exactly 'splashimage.png' or 'splashimage.jpg' inside res/images
            URL bgUrl = getClass().getResource("/res/images/splashimage.png");
            if (bgUrl != null) {
                bgImage = ImageIO.read(bgUrl);
            } else {
                // Try fallback to jpg if png missing
                bgUrl = getClass().getResource("/res/images/splashimage.jpg");
                if (bgUrl != null) bgImage = ImageIO.read(bgUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        UIJPanelBG splashPanel = new UIJPanelBG(bgImage);
        splashPanel.setBounds(0, 0, 600, 350);
        container.add(splashPanel);

        URL loadingUrl = getClass().getResource("/res/images/loading.gif");
        if (loadingUrl != null) {
            ImageIcon loadingIMG = new ImageIcon(loadingUrl);
            JLabel loadingLabel = new JLabel(loadingIMG);
            loadingLabel.setBounds(560, 310, 24, 24);
            container.add(loadingLabel, 0);
        } else {
            System.err.println("Warning: loading.gif not found.");
        }

        this.add(container);
        this.setVisible(true);
    }
}