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

    private int currentDirection = 0; // 0 = Horizontal, 1 = Vertical
    private int currentShipIndex = 0;

    private final String[] SHIP_NAMES = {"Kobaya", "Supply", "Ashigaru", "Sekibune", "Atakebune"};
    private final int[] SHIP_SIZES = {2, 3, 3, 4, 5};
    private final String[] SHIP_IMAGES = {"ship1.png", "ship2.png", "ship3.png", "ship4.png", "ship4.png"};

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

        // --- CENTER PANEL (Grid) ---
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG);

        // We pass 'true' to indicate this is the placement phase (grid handles clicks)
        panelPlayer = new UIMapPanel(mapPlayer, true);
        panelPlayer.setOnCellClicked(this::handleGridClick);
        centerPanel.add(panelPlayer);

        add(centerPanel, BorderLayout.CENTER);

        // --- RIGHT SIDEBAR ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout()); // Locked layout structure
        rightPanel.setPreferredSize(new Dimension(500, 0));
        rightPanel.setBackground(COLOR_SIDEBAR);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        // 1. HEADER (Top)
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

        // 2. SHIP LIST
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

        // 3. STATUS & ROTATE
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

        // 4. RANDOM BUTTON
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        btnRandom = createGraphicButton("RANDOMIZE", "/res/images/random.png", new Color(70, 130, 180));
        btnRandom.addActionListener(e -> randomizeShips());
        rightPanel.add(btnRandom, gbc);

        // 5. FILLER (Pushes bottom buttons down)
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(Box.createGlue(), gbc);

        // 6. BOTTOM BUTTONS (Reset & Start)
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(10, 0, 0, 0);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(500, 110)); // Slightly taller for better fit

        // Reset
        btnReset = createGraphicButton("RESET", "/res/images/reset.png", new Color(178, 34, 34));
        btnReset.addActionListener(e -> resetPlacement());
        bottomPanel.add(btnReset);

        // Start
        btnStart = createGraphicButton("START", "/res/images/play.png", new Color(50, 205, 50));
        btnStart.setEnabled(false);

        // --- FIX: Add Action Listener for Start Button ---
        btnStart.addActionListener(e -> startGame());

        bottomPanel.add(btnStart);

        rightPanel.add(bottomPanel, gbc);

        add(rightPanel, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    private JButton createGraphicButton(String text, String imagePath, Color fallbackColor) {
        JButton btn = new JButton();
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int w = 250;
        int h = 150;
        btn.setPreferredSize(new Dimension(w, h));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon icon = loadIcon(imagePath, w, h);

        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("");
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            // Visible Ghost Icon for Disabled State
            btn.setDisabledIcon(createGhostIcon(icon));
        } else {
            btn.setText(text);
            btn.setBackground(fallbackColor);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 22));
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

        // 2. Logic to place all ships
        for (int i = 0; i < SHIP_SIZES.length; i++) {
            int size = SHIP_SIZES[i];
            mapPlayer.placeShipRandomly(r, size);
            // Ensure images are assigned
            for (Ship s : mapPlayer.getShipList()) {
                if (s.getImageName() == null) s.setImageName(SHIP_IMAGES[i]);
            }
        }

        // 3. Mark all as done
        currentShipIndex = SHIP_NAMES.length;

        // 4. Update UI
        panelPlayer.updateMap(mapPlayer); // Critical: Update the panel's reference!
        updateShipListVisuals();
        checkCompletion();
    }

    private void resetPlacement() {
        // 1. Clear map
        mapPlayer = new Map();
        currentShipIndex = 0;
        currentDirection = 0;

        // 2. Reset Buttons
        btnStart.setEnabled(false);
        btnRotate.setEnabled(true); // FIX: Re-enable Rotate button!

        lblStatus.setText("Orientation: HORIZONTAL");

        // 3. Update UI
        panelPlayer.updateMap(mapPlayer);
        updateShipListVisuals();
    }

    private void startGame() {
        // Close this window and open BattleFrame
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
            btnRotate.setEnabled(false); // Disable rotate when done
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
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        int w = 220;
        int h = 60;
        btn.setPreferredSize(new Dimension(w, h));
    }
}