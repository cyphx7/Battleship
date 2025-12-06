package ui;

import game.Computer;
import game.Map;
import game.Position;
import game.Report;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BattleFrame extends JFrame {
    private Map playerMap;
    private Map computerMap;
    private Computer computerAI;
    private Computer solverAI;
    private Timer solverTimer;

    private UIMapPanel pnlPlayer;
    private UIMapPanel pnlComputer;
    private JTextArea logArea;

    private JPanel rightPanel;
    private JPanel turnContainer;

    private JLabel lblTurnIndicator;
    private ImageIcon imgYourTurn;
    private ImageIcon imgEnemyTurn;

    private JButton btnScout;
    private JButton btnTsunami;
    private int activeAbility = 0;

    private boolean playerTurn = true;
    private boolean gameOver = false;

    // Fixed dimension for the turn indicator to prevent layout shifting
    private final Dimension DIM_TURN_CONTAINER = new Dimension(300, 150);

    public BattleFrame(Map playerMap) {
        super("Battleship - Combat Mode");
        this.playerMap = playerMap;

        computerMap = new Map();
        computerMap.fillRandomly();
        computerAI = new Computer(playerMap);
        solverAI = new Computer(computerMap);

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // MAXIMIZED FOR BETTER VISIBILITY
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));

        setLayout(new BorderLayout());

        // CENTER PANEL (Main Game Area)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(50, 50, 50));

        // WRAPPER: Holds both Headers and Grids
        JPanel gameBoardContainer = new JPanel(new BorderLayout());
        gameBoardContainer.setOpaque(false);
        gameBoardContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADERS ROW (Top) - Separated from grids so resizing doesn't break layout
        JPanel headerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Gap between header and grid

        JLabel lblPlayerHeader = createHeaderLabel("/res/images/yourfleet.png");
        JLabel lblEnemyHeader = createHeaderLabel("/res/images/enemysector.png");

        headerPanel.add(lblPlayerHeader);
        headerPanel.add(lblEnemyHeader);

        // 2. MAPS ROW (Center)
        JPanel mapPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mapPanel.setOpaque(false);

        pnlPlayer = new UIMapPanel(playerMap, true);
        pnlComputer = new UIMapPanel(computerMap, false);

        pnlComputer.setOnCellClicked(pos -> {
            if (playerTurn && !gameOver) {
                if (activeAbility == 0) executePlayerTurn(pos);
                else if (activeAbility == 1) executeScout(pos);
                else if (activeAbility == 2) executeTsunami(pos);
            }
        });

        mapPanel.add(pnlPlayer);
        mapPanel.add(pnlComputer);

        // Assemble the Game Board
        gameBoardContainer.add(headerPanel, BorderLayout.NORTH);
        gameBoardContainer.add(mapPanel, BorderLayout.CENTER);

        centerPanel.add(gameBoardContainer, BorderLayout.CENTER);

        // LOG AREA (Bottom)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setForeground(Color.GREEN);
        logArea.setBackground(Color.BLACK);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(0, 150));
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "MISSION LOG", 0, 0, null, Color.WHITE));

        centerPanel.add(scroll, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // RIGHT PANEL
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setBackground(new Color(40, 40, 40));
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        turnContainer = new JPanel();
        turnContainer.setLayout(new BorderLayout());
        turnContainer.setOpaque(false);

        turnContainer.setPreferredSize(DIM_TURN_CONTAINER);
        turnContainer.setMaximumSize(DIM_TURN_CONTAINER);
        turnContainer.setMinimumSize(DIM_TURN_CONTAINER);

        initTurnPanels();
        updateTurnIndicators(true);
        rightPanel.add(turnContainer);
        rightPanel.add(Box.createVerticalStrut(20));

        JLabel lblAbilities = new JLabel("SPECIAL ABILITIES", SwingConstants.CENTER);
        lblAbilities.setFont(new Font("Arial", Font.BOLD, 22));
        lblAbilities.setForeground(Color.ORANGE);
        lblAbilities.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblAbilities);
        rightPanel.add(Box.createVerticalStrut(10));

        JPanel abilityGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        abilityGrid.setOpaque(false);
        abilityGrid.setPreferredSize(new Dimension(280, 45));
        abilityGrid.setMaximumSize(new Dimension(280, 45));
        abilityGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnScout = new JButton("SCOUT");
        btnTsunami = new JButton("TSUNAMI");
        styleButton(btnScout);
        styleButton(btnTsunami);

        btnScout.addActionListener(e -> {
            activeAbility = 1;
            log(">> SCOUT PLANE READY. Select a target zone.");
            btnScout.setBackground(Color.ORANGE);
            btnTsunami.setBackground(Color.DARK_GRAY);
        });

        btnTsunami.addActionListener(e -> {
            activeAbility = 2;
            log(">> TSUNAMI STRIKE READY. Select a target zone.");
            btnTsunami.setBackground(Color.RED);
            btnScout.setBackground(Color.DARK_GRAY);
        });

        abilityGrid.add(btnScout);
        abilityGrid.add(btnTsunami);
        rightPanel.add(abilityGrid);
        rightPanel.add(Box.createVerticalStrut(30));

        JButton btnSolve = new JButton("AUTO-PILOT");
        styleButton(btnSolve);
        btnSolve.setBackground(new Color(0, 102, 204));
        btnSolve.setForeground(Color.WHITE);
        btnSolve.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSolve.setMaximumSize(new Dimension(280, 45));
        btnSolve.setPreferredSize(new Dimension(280, 45));
        btnSolve.addActionListener(e -> runSolver());
        rightPanel.add(btnSolve);

        rightPanel.add(Box.createVerticalStrut(20));
        UIStatPanel statPanel = new UIStatPanel();
        statPanel.setPreferredSize(new Dimension(300, 120));
        statPanel.setMaximumSize(new Dimension(500, 120));
        statPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(statPanel);

        add(rightPanel, BorderLayout.EAST);
        checkAbilityAvailability();
        log("Battle stations ready. Select a target on the Enemy Sector.");
    }

    private void initTurnPanels() {
        lblTurnIndicator = new JLabel();
        lblTurnIndicator.setHorizontalAlignment(SwingConstants.CENTER);

        int targetWidth = DIM_TURN_CONTAINER.width - 20;
        imgYourTurn = loadScaledImage("/res/images/yourturn.png", targetWidth, -1);
        imgEnemyTurn = loadScaledImage("/res/images/enemyturn.png", targetWidth, -1);

        turnContainer.add(lblTurnIndicator, BorderLayout.CENTER);
    }

    private ImageIcon loadScaledImage(String path, int targetWidth, int targetHeight) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage();
                if (img.getWidth(null) > 0) {
                    int w = targetWidth;
                    int h = targetHeight;

                    if (w > 0 && h == -1) {
                        double ratio = (double) img.getHeight(null) / img.getWidth(null);
                        h = (int) (w * ratio);
                    } else if (h > 0 && w == -1) {
                        double ratio = (double) img.getWidth(null) / img.getHeight(null);
                        w = (int) (h * ratio);
                    }

                    // Safety check for turn container bounds
                    if (targetHeight == -1 && targetWidth == (DIM_TURN_CONTAINER.width - 20)) {
                        if (h > DIM_TURN_CONTAINER.height) {
                            h = DIM_TURN_CONTAINER.height;
                            w = (int) (h * ((double)img.getWidth(null)/img.getHeight(null)));
                        }
                    }

                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return icon;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to create the fleet headers separately
    private JLabel createHeaderLabel(String imagePath) {
        JLabel l = new JLabel();
        l.setHorizontalAlignment(SwingConstants.CENTER);

        // --- EDIT SIZE HERE ---
        // Change '60' to make the image bigger or smaller (height in pixels).
        // Since we separated the rows, this won't break the grid layout!
        ImageIcon icon = loadScaledImage(imagePath, 300, 200);

        if (icon != null) {
            l.setIcon(icon);
        } else {
            l.setText(imagePath);
            l.setForeground(Color.WHITE);
        }
        return l;
    }

    private void updateTurnIndicators(boolean isPlayer) {
        if (isPlayer) {
            if (imgYourTurn != null) lblTurnIndicator.setIcon(imgYourTurn);
            else lblTurnIndicator.setText("YOUR TURN");
        } else {
            if (imgEnemyTurn != null) lblTurnIndicator.setIcon(imgEnemyTurn);
            else lblTurnIndicator.setText("ENEMY TURN");
        }
        turnContainer.revalidate();
        turnContainer.repaint();
    }

    private void checkAbilityAvailability() {
        if (playerMap.isShipAlive(2)) {
            btnScout.setEnabled(true);
            if(activeAbility != 1) btnScout.setBackground(Color.DARK_GRAY);
        } else {
            btnScout.setEnabled(false);
            btnScout.setText("SCOUT (LOST)");
            btnScout.setBackground(Color.BLACK);
        }
        if (playerMap.isShipAlive(5)) {
            btnTsunami.setEnabled(true);
            if(activeAbility != 2) btnTsunami.setBackground(Color.DARK_GRAY);
        } else {
            btnTsunami.setEnabled(false);
            btnTsunami.setText("TSUNAMI (LOST)");
            btnTsunami.setBackground(Color.BLACK);
        }
    }

    private void executeScout(Position center) {
        log(">> SCOUTING SECTOR " + (char)('A' + center.getY()) + (center.getX() + 1) + "...");
        boolean found = false;
        for (int r = center.getX() - 1; r <= center.getX() + 1; r++) {
            for (int c = center.getY() - 1; c <= center.getY() + 1; c++) {
                if (r >= 0 && r < Map.MAP_SIZE && c >= 0 && c < Map.MAP_SIZE) {
                    if (computerMap.getGridAt(r, c) == Map.SHIP) found = true;
                }
            }
        }
        if (found) JOptionPane.showMessageDialog(this, "Enemy Detected in the scanned area!", "Scout Report", JOptionPane.WARNING_MESSAGE);
        else log(">> REPORT: Sector appears clear.");
        activeAbility = 0;
        styleButton(btnScout);
        styleButton(btnTsunami);
        endPlayerTurn();
    }

    private void executeTsunami(Position center) {
        log(">> CALLING TSUNAMI STRIKE...");
        boolean anyHit = false;
        for (int r = center.getX() - 1; r <= center.getX() + 1; r++) {
            for (int c = center.getY() - 1; c <= center.getY() + 1; c++) {
                if (r >= 0 && r < Map.MAP_SIZE && c >= 0 && c < Map.MAP_SIZE) {
                    if (computerMap.fireAt(new Position(r, c))) anyHit = true;
                }
            }
        }
        pnlComputer.repaint();
        if (anyHit) {
            log(">> TSUNAMI CONFIRMED HITS!");
            if (!computerMap.hasShips()) {
                gameOver = true;
                JOptionPane.showMessageDialog(this, "VICTORY!");
                return;
            }
        } else log(">> Tsunami hit nothing.");
        activeAbility = 0;
        styleButton(btnScout);
        styleButton(btnTsunami);
        endPlayerTurn();
    }

    private void executePlayerTurn(Position target) {
        if (computerMap.isHit(target) || computerMap.isWater(target)) return;
        boolean hit = computerMap.fireAt(target);
        pnlComputer.repaint();
        log("Firing at " + (char)('A' + target.getY()) + (target.getX() + 1) + "...");
        if (hit) {
            log(">> DIRECT HIT!");
            if (!computerMap.hasShips()) {
                gameOver = true;
                JOptionPane.showMessageDialog(this, "VICTORY!");
                return;
            }
        } else log(">> Miss.");
        endPlayerTurn();
    }

    private void endPlayerTurn() {
        if (!gameOver) {
            playerTurn = false;
            updateTurnIndicators(false);
            Timer t = new Timer(800, e -> executeComputerTurn());
            t.setRepeats(false);
            t.start();
        }
    }

    private void executeComputerTurn() {
        Report report = computerAI.takeTurn();
        if(report == null) return;
        Position p = report.getP();
        log("Enemy attacking " + (char)('A' + p.getY()) + (p.getX() + 1));
        pnlPlayer.repaint();
        if (report.isHit()) {
            log(">> WE HAVE BEEN HIT!");
            if (!playerMap.hasShips()) {
                gameOver = true;
                JOptionPane.showMessageDialog(this, "DEFEAT!");
            }
        } else log(">> Enemy shot missed.");
        checkAbilityAvailability();
        playerTurn = true;
        updateTurnIndicators(true);
    }

    private void runSolver() {
        if (gameOver) return;
        log(">> AUTO-PILOT ENGAGED...");
        playerTurn = false;
        updateTurnIndicators(false);
        solverTimer = new Timer(150, e -> {
            if (gameOver) { ((Timer)e.getSource()).stop(); return; }
            Report report = solverAI.takeTurn();
            if (report == null) return;
            pnlComputer.repaint();
            if (report.isHit() && !computerMap.hasShips()) {
                gameOver = true;
                JOptionPane.showMessageDialog(this, "PUZZLE SOLVED!");
                ((Timer)e.getSource()).stop();
            }
        });
        solverTimer.start();
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}