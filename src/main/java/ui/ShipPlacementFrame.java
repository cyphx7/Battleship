package ui;

import game.Map;
import game.Position;
import game.Ship;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import game.Position;
import java.awt.event.MouseEvent;

public class ShipPlacementFrame extends JFrame {
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        
        public BackgroundPanel() {
            try {
                URL imageUrl = getClass().getResource("/res/images/battleImg.jpg");
                if (imageUrl != null) {
                    backgroundImage = ImageIO.read(imageUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                double scale = Math.max(
                    getWidth() / (double)backgroundImage.getWidth(null),
                    getHeight() / (double)backgroundImage.getHeight(null)
                );
                int width = (int)(backgroundImage.getWidth(null) * scale);
                int height = (int)(backgroundImage.getHeight(null) * scale);
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;
                g.drawImage(backgroundImage, x, y, width, height, this);
            } else {
                g.setColor(new Color(30, 30, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private class RoundedPanel extends JPanel {
        private int cornerRadius = 20;
        private Color bgColor = new Color(30, 30, 30, 200);
        
        public RoundedPanel() {
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(bgColor);
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2d.dispose();
        }
    }
    private Map mapPlayer;
    private UIMapPanel panelPlayer;
    private JPanel centerPanel;

    private JLabel lblStatus;
    private JButton btnRotate, btnRandom, btnReset, btnStart;
    private JLabel[] shipLabels;

    private int currentDirection = 0;
    private int currentShipIndex = 0;

    private final String[] SHIP_NAMES = {"Kobaya", "Supply", "Ashigaru", "Sekibune", "Atakebune"};
    private final int[] SHIP_SIZES = {2, 3, 3, 4, 5};
    private final String[] SHIP_IMAGES = {"ship1.png", "ship2.png", "ship3.png", "ship4.png", "ship4.png"};

    private final Color COLOR_BG = new Color(0, 0, 0, 0);
    private final Color COLOR_SIDEBAR = new Color(30, 30, 30, 200);
    private final Color COLOR_TEXT = new Color(255, 255, 255, 220);
    private final Color COLOR_ACCENT = new Color(210, 180, 140, 220);
    private final Color COLOR_SUCCESS = new Color(50, 205, 50, 220);
    private final Color COLOR_BUTTON = new Color(139, 69, 19, 200);
    private final Color COLOR_BUTTON_HOVER = new Color(160, 82, 45, 220);

    private void handleGridClick(Position position, MouseEvent e) {
        if (currentShipIndex < SHIP_SIZES.length) {
            boolean placed = mapPlayer.placeShip(
                position.getX(),
                position.getY(),
                SHIP_SIZES[currentShipIndex],
                currentDirection
            );
            if (placed) {
                panelPlayer.repaint();
                currentShipIndex++;
                updateUI();
            }
        }
    }

    private void updateUI() {
        if (currentShipIndex < SHIP_NAMES.length) {
            lblStatus.setText("Place your " + SHIP_NAMES[currentShipIndex] + " (" + SHIP_SIZES[currentShipIndex] + " cells)");
            shipLabels[currentShipIndex].setForeground(COLOR_ACCENT);
        } else {
            lblStatus.setText("All ships placed! Click Start Battle!");
            btnStart.setEnabled(true);
        }
    }

    public ShipPlacementFrame() {
        super("Fleet Deployment");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 950);
        setLocationRelativeTo(null);

        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout());

        UIManager.put("Panel.background", new Color(0, 0, 0, 0));
        UIManager.put("TextArea.background", new Color(255, 255, 255, 20));
        UIManager.put("TextArea.foreground", COLOR_TEXT);
        UIManager.put("TextArea.border", BorderFactory.createLineBorder(new Color(139, 69, 19, 100), 1));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollPane.background", new Color(0, 0, 0, 0));
        UIManager.put("Viewport.background", new Color(0, 0, 0, 0));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapPlayer = new Map();
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        panelPlayer = new UIMapPanel(mapPlayer, true);
        panelPlayer.setOnCellClicked(this::handleGridClick);
        centerPanel.add(panelPlayer);

        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new RoundedPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(500, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel lblHeader = new JLabel();
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon headerIcon = loadIcon("/res/images/deployfleet.png", 460, 250);
        if (headerIcon != null) {
            lblHeader.setIcon(headerIcon);
        } else {
            lblHeader.setText("DEPLOY FLEET");
            lblHeader.setFont(new Font("Arial", Font.BOLD, 40));
            lblHeader.setForeground(COLOR_TEXT);
        }
        rightPanel.add(lblHeader, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 20, 0);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        shipLabels = new JLabel[SHIP_NAMES.length];
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(450, 60));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblText = new JLabel(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
            lblText.setFont(new Font("Arial", Font.PLAIN, 18));
            lblText.setForeground(Color.GRAY);

            JLabel lblIcon = new JLabel();
            ImageIcon icon = loadIcon("/res/images/" + SHIP_IMAGES[i], 120, 35);
            if (icon != null) lblIcon.setIcon(icon);

            shipLabels[i] = lblText;
            row.add(lblIcon);
            row.add(lblText);
            listPanel.add(row);
        }
        updateShipListVisuals();
        rightPanel.add(listPanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);

        lblStatus = new JLabel("Orientation: HORIZONTAL");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(lblStatus, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        btnRotate = new JButton("ROTATE SHIP");
        styleButton(btnRotate);
        btnRotate.addActionListener(e -> toggleRotation());
        rightPanel.add(btnRotate, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        btnRandom = createGraphicButton("RANDOMIZE", "/res/images/random.png", new Color(70, 130, 180));
        btnRandom.addActionListener(e -> randomizeShips());
        rightPanel.add(btnRandom, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(Box.createGlue(), gbc);

        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(10, 0, 0, 0);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(500, 110)); // Slightly taller for better fit

        btnReset = createGraphicButton("RESET", "/res/images/reset.png", new Color(178, 34, 34));
        btnReset.addActionListener(e -> resetPlacement());
        bottomPanel.add(btnReset);

        btnStart = createGraphicButton("START", "/res/images/play.png", new Color(50, 205, 50));
        btnStart.setEnabled(false);
        btnStart.addActionListener(e -> startGame());

        bottomPanel.add(btnStart);

        rightPanel.add(bottomPanel, gbc);

        add(rightPanel, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    private JButton createGraphicButton(String text, String imagePath, Color fallbackColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };

        // Basic button styling
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        int w = 250;
        int h = 100;
        btn.setPreferredSize(new Dimension(w, h));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setForeground(COLOR_TEXT);
        btn.setFont(new Font("Arial", Font.BOLD, 20));

        ImageIcon icon = loadIcon(imagePath, w - 40, h - 20);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("");
            btn.setDisabledIcon(createGhostIcon(icon));
            btn.setToolTipText(text);
        } else {
            btn.setText(text);
        }
        return btn;
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        try {
            BufferedImage img = ImageIO.read(url);
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon createGhostIcon(ImageIcon original) {
        BufferedImage img = new BufferedImage(
                original.getIconWidth(), original.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        original.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void handleGridClick(Position p) {
        if (currentShipIndex >= SHIP_NAMES.length) return;
        int size = SHIP_SIZES[currentShipIndex];
        boolean success = mapPlayer.placeShip(p.getX(), p.getY(), size, currentDirection);

        if (success) {
            Ship newlyPlacedShip = mapPlayer.getShipAt(p);
            if (newlyPlacedShip != null) {
                newlyPlacedShip.setImageName(SHIP_IMAGES[currentShipIndex]);
            }
            currentShipIndex++;
            panelPlayer.updateMap(mapPlayer);
            updateShipListVisuals();
            checkCompletion();
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Invalid Placement!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void randomizeShips() {
        // 1. Create a fresh map
        mapPlayer = new Map();
        Random r = new Random();

        for (int i = 0; i < SHIP_SIZES.length; i++) {
            int size = SHIP_SIZES[i];
            mapPlayer.placeShipRandomly(r, size);
            for (Ship s : mapPlayer.getShipList()) {
                if (s.getImageName() == null) s.setImageName(SHIP_IMAGES[i]);
            }
        }

        currentShipIndex = SHIP_NAMES.length;

        panelPlayer.updateMap(mapPlayer);
        updateShipListVisuals();
        checkCompletion();
    }

    private void resetPlacement() {
        mapPlayer = new Map();
        currentShipIndex = 0;
        currentDirection = 0;
        btnStart.setEnabled(false);
        btnRotate.setEnabled(true); // FIX: Re-enable Rotate button!
        lblStatus.setText("Orientation: HORIZONTAL");
        panelPlayer.updateMap(mapPlayer);
        updateShipListVisuals();
    }

    private void startGame() {
        new BattleFrame(mapPlayer).setVisible(true);
        this.dispose();
    }

    private void toggleRotation() {
        currentDirection = (currentDirection == 0) ? 1 : 0;
        lblStatus.setText("Orientation: " + (currentDirection == 0 ? "HORIZONTAL" : "VERTICAL"));
    }

    private void checkCompletion() {
        if (currentShipIndex >= SHIP_NAMES.length) {
            btnStart.setEnabled(true);
            btnRotate.setEnabled(false);
            lblStatus.setText("FLEET READY!");
        }
    }

    private void updateShipListVisuals() {
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            if (i < currentShipIndex) {
                shipLabels[i].setForeground(COLOR_SUCCESS);
                shipLabels[i].setText("✔ " + SHIP_NAMES[i]);
            } else if (i == currentShipIndex) {
                shipLabels[i].setForeground(COLOR_ACCENT);
                shipLabels[i].setFont(new Font("Arial", Font.BOLD, 18));
            } else {
                shipLabels[i].setForeground(Color.GRAY);
                shipLabels[i].setFont(new Font("Arial", Font.PLAIN, 16));
            }
        }
    }

    private void styleButton(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(COLOR_TEXT);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(COLOR_ACCENT);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(COLOR_TEXT);
            }
        });

        int w = 220;
        int h = 50;
        btn.setPreferredSize(new Dimension(w, h));
    }
}