package ui;

import game.Map;
import game.Position;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class ShipPlacementFrame extends JFrame {
    private Map mapPlayer;
    private UIMapPanel panelPlayer;
    private JPanel centerPanel; // Wrapper for the grid

    // UI Components
    private JLabel lblStatus;
    private JButton btnRotate, btnReset, btnRandom, btnStart;

    // Ship List Labels
    private JLabel[] shipLabels;

    // Logic Variables
    private int currentDirection = 0; // 0 = Horizontal, 1 = Vertical
    private int currentShipIndex = 0;

    // Data (Standard Battleship Ships)
    private final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    private final int[] SHIP_SIZES = {5, 4, 3, 3, 2};

    // Colors
    private final Color COLOR_BG = new Color(50, 50, 50);
    private final Color COLOR_SIDEBAR = new Color(40, 40, 40);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_ACCENT = new Color(255, 165, 0); // Orange
    private final Color COLOR_SUCCESS = new Color(50, 205, 50); // Green

    public ShipPlacementFrame() {
        super("Fleet Deployment");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700); // Increased window width slightly
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        // --- 1. Init Logic ---
        mapPlayer = new Map();

        // --- 2. Center Panel (The Grid) ---
        // We use a GridBagLayout to center the large grid in the available space
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG);

        initGridPanel(); // Helper to setup the grid

        add(centerPanel, BorderLayout.CENTER);

        // --- 3. Right Sidebar (Controls) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(COLOR_SIDEBAR);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -- Header --
        JLabel lblHeader = new JLabel("DEPLOY FLEET");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 24));
        lblHeader.setForeground(COLOR_TEXT);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(lblHeader);
        rightPanel.add(Box.createVerticalStrut(30));

        // -- Ship List Indicators --
        shipLabels = new JLabel[SHIP_NAMES.length];
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 0; i < SHIP_NAMES.length; i++) {
            JLabel lbl = new JLabel(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
            lbl.setFont(new Font("Arial", Font.PLAIN, 18));
            lbl.setForeground(Color.GRAY);
            lbl.setBorder(new EmptyBorder(5, 0, 5, 0));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            shipLabels[i] = lbl;
            listPanel.add(lbl);
        }
        updateShipListVisuals();

        rightPanel.add(listPanel);
        rightPanel.add(Box.createVerticalStrut(40));

        // -- Controls --
        lblStatus = new JLabel("Orientation: HORIZONTAL");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblStatus);
        rightPanel.add(Box.createVerticalStrut(10));

        // ROTATE
        btnRotate = new JButton("ROTATE SHIP");
        styleButton(btnRotate);
        btnRotate.addActionListener(e -> toggleRotation());
        rightPanel.add(btnRotate);
        rightPanel.add(Box.createVerticalStrut(15));

        // RANDOM (New Button)
        btnRandom = new JButton("RANDOMIZE");
        styleButton(btnRandom);
        btnRandom.setBackground(new Color(70, 130, 180)); // Steel Blue
        btnRandom.addActionListener(e -> randomizeShips());
        rightPanel.add(btnRandom);
        rightPanel.add(Box.createVerticalStrut(15));

        // RESET
        btnReset = new JButton("RESET BOARD");
        styleButton(btnReset);
        btnReset.setBackground(new Color(178, 34, 34)); // Red
        btnReset.addActionListener(e -> resetPlacement());
        rightPanel.add(btnReset);

        // -- Start Button (Bottom) --
        rightPanel.add(Box.createVerticalGlue());

        btnStart = new JButton("START GAME");
        styleButton(btnStart);
        btnStart.setBackground(Color.GRAY);
        btnStart.setEnabled(false);
        btnStart.setPreferredSize(new Dimension(260, 60));
        btnStart.setFont(new Font("Arial", Font.BOLD, 20));

        btnStart.addActionListener(e -> {
            new BattleFrame(mapPlayer).setVisible(true);
            this.dispose();
        });

        rightPanel.add(btnStart);

        add(rightPanel, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    private void initGridPanel() {
        centerPanel.removeAll();

        panelPlayer = new UIMapPanel(mapPlayer, true);

        // IMPORTANT: Set a large preferred size to fill the left side
        // The frame is 1000px wide, sidebar is 300px.
        // 600x600 will fill the remaining space nicely with the layout margins.
        panelPlayer.setPreferredSize(new Dimension(600, 600));

        panelPlayer.setOnCellClicked(this::handleGridClick);

        centerPanel.add(panelPlayer);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // --- LOGIC METHODS ---

    private void handleGridClick(Position p) {
        if (currentShipIndex >= SHIP_NAMES.length) return;

        int size = SHIP_SIZES[currentShipIndex];
        boolean success = mapPlayer.placeShip(p.getX(), p.getY(), size, currentDirection);

        if (success) {
            currentShipIndex++;
            panelPlayer.repaint();
            updateShipListVisuals();
            checkCompletion();
        } else {
            // --- UI WARNING ADDED HERE ---
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this,
                    "Invalid Placement!\nShips cannot overlap or go off the map.",
                    "Placement Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void randomizeShips() {
        // 1. Create a clean map
        mapPlayer = new Map();

        // 2. Randomly place all ships defined in SHIP_SIZES
        Random r = new Random();
        for (int size : SHIP_SIZES) {
            // placeShipRandomly is a helper in Map.java
            mapPlayer.placeShipRandomly(r, size);
        }

        // 3. Update State
        currentShipIndex = SHIP_NAMES.length; // Mark all as done

        // 4. Refresh UI
        initGridPanel(); // Rebind the panel to the new map
        updateShipListVisuals();
        checkCompletion();
    }

    private void toggleRotation() {
        currentDirection = (currentDirection == 0) ? 1 : 0;
        lblStatus.setText("Orientation: " + (currentDirection == 0 ? "HORIZONTAL" : "VERTICAL"));
    }

    private void resetPlacement() {
        mapPlayer = new Map();
        currentShipIndex = 0;
        currentDirection = 0;

        btnStart.setEnabled(false);
        btnStart.setBackground(Color.GRAY);
        btnRotate.setEnabled(true);
        btnRandom.setEnabled(true);
        lblStatus.setText("Orientation: HORIZONTAL");

        initGridPanel(); // Refresh grid
        updateShipListVisuals();
    }

    private void checkCompletion() {
        if (currentShipIndex >= SHIP_NAMES.length) {
            btnStart.setEnabled(true);
            btnStart.setBackground(COLOR_SUCCESS);
            btnRotate.setEnabled(false);
            btnRandom.setEnabled(true); // Allow re-rolling if they want
            lblStatus.setText("FLEET READY!");
        }
    }

    private void updateShipListVisuals() {
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            if (i < currentShipIndex) {
                shipLabels[i].setForeground(COLOR_SUCCESS);
                shipLabels[i].setFont(new Font("Arial", Font.PLAIN, 18));
                shipLabels[i].setText("✔ " + SHIP_NAMES[i]);
            } else if (i == currentShipIndex) {
                shipLabels[i].setForeground(COLOR_ACCENT);
                shipLabels[i].setFont(new Font("Arial", Font.BOLD, 20));
                shipLabels[i].setText("➤ " + SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
            } else {
                shipLabels[i].setForeground(Color.GRAY);
                shipLabels[i].setFont(new Font("Arial", Font.PLAIN, 18));
                shipLabels[i].setText(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
            }
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(260, 45));
    }
}