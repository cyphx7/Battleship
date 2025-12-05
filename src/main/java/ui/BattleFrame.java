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

    // --- SOLVER ADDITIONS ---
    private Computer solverAI;
    private Timer solverTimer;
    // ------------------------

    private UIMapPanel pnlPlayer;
    private UIMapPanel pnlComputer;
    private JTextArea logArea;

    // Right Panel Components
    private JPanel rightPanel;
    private JPanel turnContainer;

    // Ability Buttons
    private JButton btnScout;
    private JButton btnTsunami;

    // Ability Logic
    private int activeAbility = 0; // 0 = None, 1 = Scout, 2 = Tsunami

    // Dual Turn Panels
    private JPanel pnlYourTurn;
    private JPanel pnlEnemyTurn;
    private JLabel lblYourTurn;
    private JLabel lblEnemyTurn;

    private boolean playerTurn = true;
    private boolean gameOver = false;

    // --- COLORS ---
    private final Color COLOR_PLAYER = new Color(34, 139, 34);
    private final Color COLOR_PLAYER_DIM = new Color(20, 80, 20);

    private final Color COLOR_ENEMY = new Color(178, 34, 34);
    private final Color COLOR_ENEMY_DIM = new Color(80, 20, 20);

    private final Dimension DIM_ACTIVE = new Dimension(300, 100);
    private final Dimension DIM_INACTIVE = new Dimension(300, 50);

    public BattleFrame(Map playerMap) {
        super("Battleship - Combat Mode");
        this.playerMap = playerMap;

        // 1. Initialize Enemy Data
        computerMap = new Map();
        computerMap.fillRandomly();
        computerAI = new Computer(playerMap);

        // --- SOLVER INIT ---
        solverAI = new Computer(computerMap);
        // -------------------

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 850);
        setLayout(new BorderLayout());

        // ==========================================
        // CENTER PANEL: Grids + Console Log
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(50, 50, 50));

        // -- 1. Grids Area --
        JPanel gridContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        gridContainer.setOpaque(false);
        gridContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        pnlPlayer = new UIMapPanel(playerMap, true);
        pnlComputer = new UIMapPanel(computerMap, false);

        // CLICK EVENT
        pnlComputer.setOnCellClicked(pos -> {
            if (playerTurn && !gameOver) {
                if (activeAbility == 0) {
                    executePlayerTurn(pos);
                } else if (activeAbility == 1) {
                    executeScout(pos);
                } else if (activeAbility == 2) {
                    executeTsunami(pos);
                }
            }
        });

        gridContainer.add(createLabeledPanel("YOUR FLEET", pnlPlayer));
        gridContainer.add(createLabeledPanel("ENEMY SECTOR", pnlComputer));

        centerPanel.add(gridContainer, BorderLayout.CENTER);

        // -- 2. Console Log Area --
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


        // ==========================================
        // RIGHT PANEL: Controls
        // ==========================================
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(new Color(40, 40, 40));
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // -- 1. TURN CONTAINER --
        turnContainer = new JPanel();
        turnContainer.setLayout(new BoxLayout(turnContainer, BoxLayout.Y_AXIS));
        turnContainer.setOpaque(false);
        turnContainer.setMaximumSize(new Dimension(300, 160));

        initTurnPanels();
        updateTurnIndicators(true);

        rightPanel.add(turnContainer);
        rightPanel.add(Box.createVerticalStrut(20)); // Spacer

        // -- 2. Special Abilities --
        JLabel lblAbilities = new JLabel("SPECIAL ABILITIES", SwingConstants.CENTER);
        lblAbilities.setFont(new Font("Arial", Font.BOLD, 22));
        lblAbilities.setForeground(Color.ORANGE);
        lblAbilities.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(lblAbilities);
        rightPanel.add(Box.createVerticalStrut(10));

        // -- 3. Ability Buttons --
        JPanel abilityGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        abilityGrid.setOpaque(false);
        abilityGrid.setPreferredSize(new Dimension(280, 60));
        abilityGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnScout = new JButton("SCOUT");
        btnTsunami = new JButton("TSUNAMI");

        styleButton(btnScout);
        styleButton(btnTsunami);

        // Setup Actions
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

        // -- 4. AUTO PILOT BUTTON --
        JButton btnSolve = new JButton("AUTO-PILOT");
        styleButton(btnSolve);
        btnSolve.setBackground(new Color(0, 102, 204)); // Tactical Blue
        btnSolve.setForeground(Color.WHITE);
        btnSolve.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSolve.setMaximumSize(new Dimension(280, 50));

        btnSolve.addActionListener(e -> runSolver());

        rightPanel.add(btnSolve);
        rightPanel.add(Box.createVerticalStrut(30));

        // -- 5. Fleet Status Banner --
        JPanel fleetPanel = new JPanel(new BorderLayout());
        fleetPanel.setBackground(Color.LIGHT_GRAY);
        fleetPanel.setBorder(BorderFactory.createTitledBorder("FLEET STATUS"));

        fleetPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        fleetPanel.setPreferredSize(new Dimension(300, 200));

        JLabel lblFleetPlaceholder = new JLabel("<html><center>User Ships Icons<br>vs<br>Enemy Ships Icons</center></html>", SwingConstants.CENTER);
        fleetPanel.add(lblFleetPlaceholder, BorderLayout.CENTER);

        rightPanel.add(fleetPanel);

        add(rightPanel, BorderLayout.EAST);

        checkAbilityAvailability(); // Initial check
        log("Battle stations ready. Select a target on the Enemy Sector.");
    }

    private void initTurnPanels() {
        pnlYourTurn = new JPanel(new BorderLayout());
        lblYourTurn = new JLabel("YOUR TURN", SwingConstants.CENTER);
        lblYourTurn.setForeground(Color.WHITE);
        pnlYourTurn.add(lblYourTurn, BorderLayout.CENTER);

        pnlEnemyTurn = new JPanel(new BorderLayout());
        lblEnemyTurn = new JLabel("ENEMY TURN", SwingConstants.CENTER);
        lblEnemyTurn.setForeground(Color.WHITE);
        pnlEnemyTurn.add(lblEnemyTurn, BorderLayout.CENTER);
    }

    // --- SPECIAL ABILITY LOGIC ---

    private void checkAbilityAvailability() {
        // Scout requires Kobaya (Size 2)
        if (playerMap.isShipAlive(2)) {
            btnScout.setEnabled(true);
        } else {
            btnScout.setEnabled(false);
            btnScout.setText("SCOUT (LOST)");
            btnScout.setBackground(Color.DARK_GRAY);
        }

        // Tsunami requires Atakebune (Size 5)
        if (playerMap.isShipAlive(5)) {
            btnTsunami.setEnabled(true);
        } else {
            btnTsunami.setEnabled(false);
            btnTsunami.setText("TSUNAMI (LOST)");
            btnTsunami.setBackground(Color.DARK_GRAY);
        }
    }

    private void executeScout(Position center) {
        log(">> SCOUTING SECTOR " + (char)('A' + center.getY()) + (center.getX() + 1) + "...");

        boolean found = false;
        // Check 3x3 Area
        for (int r = center.getX() - 1; r <= center.getX() + 1; r++) {
            for (int c = center.getY() - 1; c <= center.getY() + 1; c++) {
                if (r >= 0 && r < Map.MAP_SIZE && c >= 0 && c < Map.MAP_SIZE) {
                    if (computerMap.getGridAt(r, c) == Map.SHIP) {
                        found = true;
                    }
                }
            }
        }

        if (found) {
            log(">> REPORT: ENEMY ACTIVITY DETECTED IN THIS SECTOR!");
            JOptionPane.showMessageDialog(this, "Enemy Detected in the scanned area!", "Scout Report", JOptionPane.WARNING_MESSAGE);
        } else {
            log(">> REPORT: Sector appears clear.");
        }

        activeAbility = 0; // Reset
        styleButton(btnScout); // Reset colors
        styleButton(btnTsunami);

        endPlayerTurn();
    }

    private void executeTsunami(Position center) {
        log(">> CALLING TSUNAMI STRIKE ON " + (char)('A' + center.getY()) + (center.getX() + 1) + "...");

        boolean anyHit = false;
        // Hit 3x3 Area
        for (int r = center.getX() - 1; r <= center.getX() + 1; r++) {
            for (int c = center.getY() - 1; c <= center.getY() + 1; c++) {
                if (r >= 0 && r < Map.MAP_SIZE && c >= 0 && c < Map.MAP_SIZE) {
                    // Fire at this specific spot
                    boolean hit = computerMap.fireAt(new Position(r, c));
                    if (hit) anyHit = true;
                }
            }
        }

        pnlComputer.repaint();

        if (anyHit) {
            log(">> TSUNAMI CONFIRMED HITS! DEVASTATING!");
            if (!computerMap.hasShips()) {
                gameOver = true;
                log("ENEMY FLEET ELIMINATED. VICTORY!");
                JOptionPane.showMessageDialog(this, "VICTORY!");
                return;
            }
        } else {
            log(">> Tsunami hit nothing but water.");
        }

        activeAbility = 0; // Reset
        styleButton(btnScout);
        styleButton(btnTsunami);

        endPlayerTurn();
    }

    // --- Game Logic Methods ---

    private void executePlayerTurn(Position target) {
        if (computerMap.isHit(target) || computerMap.isWater(target)) {
            return;
        }

        boolean hit = computerMap.fireAt(target);
        pnlComputer.repaint();

        log("Firing at " + (char)('A' + target.getY()) + (target.getX() + 1) + "...");

        if (hit) {
            log(">> SPLASH! DIRECT HIT!");
            if (!computerMap.hasShips()) {
                gameOver = true;
                log("ENEMY FLEET ELIMINATED. VICTORY!");

                Timer t = new Timer(100, e -> {
                    JOptionPane.showMessageDialog(this, "VICTORY!");
                });
                t.setRepeats(false);
                t.start();
                return;
            }
        } else {
            log(">> Miss.");
        }

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
        Position p = report.getP();
        log("Enemy attacking " + (char)('A' + p.getY()) + (p.getX() + 1));

        pnlPlayer.repaint();

        if (report.isHit()) {
            log(">> ALERT! WE HAVE BEEN HIT!");
            if (report.isSunk()) {
                log(">> SHIP LOST!");
            }
            if (!playerMap.hasShips()) {
                gameOver = true;
                log("FLEET DESTROYED. GAME OVER.");

                Timer t = new Timer(100, e -> {
                    JOptionPane.showMessageDialog(this, "DEFEAT!");
                });
                t.setRepeats(false);
                t.start();
            }
        } else {
            log(">> Enemy shot missed.");
        }

        // Check if abilities should still be available after taking damage
        checkAbilityAvailability();

        playerTurn = true;
        updateTurnIndicators(true);
    }

    private void runSolver() {
        if (gameOver) return;

        log(">> AUTO-PILOT ENGAGED. CALCULATING FIRING SOLUTIONS...");
        playerTurn = false;
        updateTurnIndicators(false);

        solverTimer = new Timer(250, e -> {
            if (gameOver) {
                ((Timer)e.getSource()).stop();
                return;
            }

            Report report = solverAI.takeTurn();
            if (report == null) return;

            Position p = report.getP();
            log("Auto-Pilot fired at " + (char)('A' + p.getY()) + (p.getX() + 1));
            pnlComputer.repaint();

            if (report.isHit()) {
                if (report.isSunk()) log(">> TARGET DESTROYED!");
                if (!computerMap.hasShips()) {
                    gameOver = true;
                    log("MISSION ACCOMPLISHED. VICTORY!");
                    JOptionPane.showMessageDialog(this, "PUZZLE SOLVED!");
                    ((Timer)e.getSource()).stop();
                    return;
                }
            } else {
                executeComputerTurn();
            }
        });
        solverTimer.start();
    }

    // --- Helper UI Methods ---

    private void updateTurnIndicators(boolean isPlayer) {
        turnContainer.removeAll();

        if (isPlayer) {
            stylePanel(pnlYourTurn, lblYourTurn, COLOR_PLAYER, DIM_ACTIVE, true);
            turnContainer.add(pnlYourTurn);
            stylePanel(pnlEnemyTurn, lblEnemyTurn, COLOR_ENEMY_DIM, DIM_INACTIVE, false);
            turnContainer.add(pnlEnemyTurn);
        } else {
            stylePanel(pnlEnemyTurn, lblEnemyTurn, COLOR_ENEMY, DIM_ACTIVE, true);
            turnContainer.add(pnlEnemyTurn);
            stylePanel(pnlYourTurn, lblYourTurn, COLOR_PLAYER_DIM, DIM_INACTIVE, false);
            turnContainer.add(pnlYourTurn);
        }

        turnContainer.revalidate();
        turnContainer.repaint();
    }

    private void stylePanel(JPanel pnl, JLabel lbl, Color bg, Dimension dim, boolean isActive) {
        pnl.setBackground(bg);
        pnl.setMaximumSize(dim);
        pnl.setPreferredSize(dim);
        pnl.setBorder(isActive ?
                BorderFactory.createLineBorder(Color.WHITE, 2) :
                BorderFactory.createLineBorder(Color.GRAY, 1));

        lbl.setFont(new Font("Arial", Font.BOLD, isActive ? 28 : 16));
        lbl.setForeground(isActive ? Color.WHITE : Color.LIGHT_GRAY);
    }

    private JPanel createLabeledPanel(String title, JPanel panel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        p.add(l, BorderLayout.NORTH);
        p.add(panel, BorderLayout.CENTER);
        return p;
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