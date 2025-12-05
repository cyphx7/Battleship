package ui;

import game.Map;
import game.Position;
import game.Ship;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Random;

public class ShipPlacementFrame extends JFrame {
    private Map mapPlayer;
    private UIMapPanel panelPlayer;
    private JPanel centerPanel;

    private JLabel lblStatus;
    private JButton btnRotate, btnRandom, btnReset, btnStart;
    private JLabel[] shipLabels;

    private int currentDirection = 0;
    private int currentShipIndex = 0;

    private final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    private final int[] SHIP_SIZES = {5, 4, 3, 3, 2};
    private final String[] SHIP_IMAGES = {"ship1.png", "ship2.png", "ship3.png", "ship 3.png", "ship4.png"};

    private final Color COLOR_BG = new Color(50, 50, 50);
    private final Color COLOR_SIDEBAR = new Color(40, 40, 40);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_ACCENT = new Color(255, 165, 0);
    private final Color COLOR_SUCCESS = new Color(50, 205, 50);

    public ShipPlacementFrame() {
        super("Fleet Deployment");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 950);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        mapPlayer = new Map();

        // --- CENTER PANEL ---
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG);

        // Initialize Grid ONCE
        panelPlayer = new UIMapPanel(mapPlayer, true);
        panelPlayer.setOnCellClicked(this::handleGridClick);
        centerPanel.add(panelPlayer);

        add(centerPanel, BorderLayout.CENTER);

        // --- RIGHT SIDEBAR ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        // Wide sidebar to accommodate massive buttons
        rightPanel.setPreferredSize(new Dimension(550, 0));
        rightPanel.setBackground(COLOR_SIDEBAR);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel lblHeader = new JLabel("DEPLOY FLEET");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 30));
        lblHeader.setForeground(COLOR_TEXT);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblHeader);
        rightPanel.add(Box.createVerticalStrut(30));

        // Ship List
        shipLabels = new JLabel[SHIP_NAMES.length];
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 0; i < SHIP_NAMES.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(500, 60));

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
        rightPanel.add(listPanel);
        rightPanel.add(Box.createVerticalStrut(40));

        // Status
        lblStatus = new JLabel("Orientation: HORIZONTAL");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblStatus);
        rightPanel.add(Box.createVerticalStrut(20));

        // Controls
        btnRotate = new JButton("ROTATE SHIP");
        styleButton(btnRotate);
        btnRotate.addActionListener(e -> toggleRotation());
        rightPanel.add(btnRotate);
        rightPanel.add(Box.createVerticalStrut(20));

        btnRandom = new JButton("RANDOMIZE");
        styleButton(btnRandom);
        btnRandom.setBackground(new Color(70, 130, 180));
        btnRandom.addActionListener(e -> randomizeShips());
        rightPanel.add(btnRandom);

        // Push everything to bottom
        rightPanel.add(Box.createVerticalGlue());

        // --- BOTTOM BUTTONS (Reset & Start) ---
        // GridLayout(1, 2) forces them to be equal size and fill the width
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setMaximumSize(new Dimension(550, 120)); // Restrict max height
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 1. Reset Button
        btnReset = createMassiveButton("RESET", "/res/images/reset.png", new Color(178, 34, 34));
        btnReset.addActionListener(e -> resetPlacement());
        bottomPanel.add(btnReset);

        // 2. Start Button
        btnStart = createMassiveButton("START", "/res/images/play.png", new Color(50, 205, 50));
        btnStart.setEnabled(false); // Initially disabled
        bottomPanel.add(btnStart);

        rightPanel.add(bottomPanel);
        add(rightPanel, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    /**
     * Creates a MASSIVE button (250x100) with robust image handling
     */
    private JButton createMassiveButton(String text, String imagePath, Color fallbackColor) {
        JButton btn = new JButton();

        // --- BUTTON SIZE ---
        // These dimensions are huge and will fill the GridLayout cells
        int w = 250;
        int h = 200;
        btn.setPreferredSize(new Dimension(w, h));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icon = loadIcon(imagePath, w, h);

        if (icon != null) {
            // IMAGE MODE
            btn.setIcon(icon);
            btn.setText("");
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);

            // --- CRITICAL FIX FOR MISSING PLAY BUTTON ---
            // Create a semi-transparent version of the icon for the "Disabled" state.
            // This ensures it is VISIBLE (ghosted) instead of disappearing.
            btn.setDisabledIcon(createTransparentIcon(icon, 0.5f));
        } else {
            // FALLBACK TEXT MODE
            btn.setText(text);
            btn.setBackground(fallbackColor);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 24));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        }
        return btn;
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        try {
            BufferedImage img = ImageIO.read(url);
            // High quality scaling
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Manually creates a transparent version of an image.
     * This is much more reliable than GrayFilter for disabled buttons.
     */
    private ImageIcon createTransparentIcon(ImageIcon original, float alpha) {
        BufferedImage img = new BufferedImage(
                original.getIconWidth(), original.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
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
        btnStart.setEnabled(false); // Will now show the transparent icon
        lblStatus.setText("Orientation: HORIZONTAL");
        panelPlayer.updateMap(mapPlayer);
        updateShipListVisuals();
    }

    private void toggleRotation() {
        currentDirection = (currentDirection == 0) ? 1 : 0;
        lblStatus.setText("Orientation: " + (currentDirection == 0 ? "HORIZONTAL" : "VERTICAL"));
    }

    private void checkCompletion() {
        if (currentShipIndex >= SHIP_NAMES.length) {
            btnStart.setEnabled(true); // Icon becomes fully opaque
            btnRotate.setEnabled(false);
            btnRandom.setEnabled(true);
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
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 60));
    }
}