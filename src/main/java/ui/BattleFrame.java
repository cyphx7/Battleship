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

    private UIMapPanel pnlPlayer;
    private UIMapPanel pnlComputer;
    private JTextArea logArea;

    // Right Panel Components
    private JPanel rightPanel;
    private JPanel turnContainer; // The container for the stacking effect

    private JPanel pnlYourTurn;
    private JPanel pnlEnemyTurn;
    private JLabel lblYourTurn;
    private JLabel lblEnemyTurn;

    private boolean playerTurn = true;
    private boolean gameOver = false;

    // --- COLORS ---
    private final Color COLOR_PLAYER = new Color(34, 139, 34); // Bright Green
    private final Color COLOR_PLAYER_DIM = new Color(20, 80, 20); // Dark Green

    private final Color COLOR_ENEMY = new Color(178, 34, 34);  // Bright Red
    private final Color COLOR_ENEMY_DIM = new Color(80, 20, 20);  // Dark Red

    // --- DIMENSIONS (This fixes the "Not Seen" issue) ---
    private final Dimension DIM_ACTIVE = new Dimension(300, 100); // Big
    private final Dimension DIM_INACTIVE = new Dimension(300, 50); // Small but VISIBLE (50px)

    public BattleFrame(Map playerMap) {
        super("Battleship - Combat Mode");
        this.playerMap = playerMap;

        // 1. Initialize Enemy Data
        computerMap = new Map();
        computerMap.fillRandomly();
        computerAI = new Computer(playerMap); // AI targets you

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
                executePlayerTurn(pos);
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

        // -- 1. TURN CONTAINER (Holds the 2 stacking banners) --
        turnContainer = new JPanel();
        turnContainer.setLayout(new BoxLayout(turnContainer, BoxLayout.Y_AXIS));
        turnContainer.setOpaque(false);
        // Allow enough height for both (100 + 50)
        turnContainer.setMaximumSize(new Dimension(300, 160));

        // Initialize the panels
        initTurnPanels();

        // Set initial state (Player Active)
        updateTurnIndicators(true);

        rightPanel.add(turnContainer);
        rightPanel.add(Box.createVerticalStrut(20)); // Spacer

        // -- 2. Special Abilities Banner --
        JLabel lblAbilities = new JLabel("SPECIAL ABILITIES", SwingConstants.CENTER);
        lblAbilities.setFont(new Font("Arial", Font.BOLD, 22));
        lblAbilities.setForeground(Color.ORANGE);
        lblAbilities.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(lblAbilities);
        rightPanel.add(Box.createVerticalStrut(15));

        // -- 3. Ability Buttons --
        JPanel abilityGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        abilityGrid.setOpaque(false);
        abilityGrid.setMaximumSize(new Dimension(300, 80));
        abilityGrid.setPreferredSize(new Dimension(300, 80));

        JButton btnAbility1 = new JButton("RADAR");
        JButton btnAbility2 = new JButton("AIRSTRIKE");
        styleButton(btnAbility1);
        styleButton(btnAbility2);

        abilityGrid.add(btnAbility1);
        abilityGrid.add(btnAbility2);

        rightPanel.add(abilityGrid);
        rightPanel.add(Box.createVerticalStrut(30));

        // -- 4. Fleet Status Banner --
        JPanel fleetPanel = new JPanel(new BorderLayout());
        fleetPanel.setBackground(Color.LIGHT_GRAY);
        fleetPanel.setBorder(BorderFactory.createTitledBorder("FLEET STATUS"));

        fleetPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        fleetPanel.setPreferredSize(new Dimension(300, 200));

        JLabel lblFleetPlaceholder = new JLabel("<html><center>User Ships Icons<br>vs<br>Enemy Ships Icons</center></html>", SwingConstants.CENTER);
        fleetPanel.add(lblFleetPlaceholder, BorderLayout.CENTER);

        rightPanel.add(fleetPanel);

        add(rightPanel, BorderLayout.EAST);

        log("Battle stations ready. Select a target on the Enemy Sector.");
    }

    private void initTurnPanels() {
        // Init Player Panel
        pnlYourTurn = new JPanel(new BorderLayout());
        lblYourTurn = new JLabel("YOUR TURN", SwingConstants.CENTER);
        lblYourTurn.setForeground(Color.WHITE);
        pnlYourTurn.add(lblYourTurn, BorderLayout.CENTER);

        // Init Enemy Panel
        pnlEnemyTurn = new JPanel(new BorderLayout());
        lblEnemyTurn = new JLabel("ENEMY TURN", SwingConstants.CENTER);
        lblEnemyTurn.setForeground(Color.WHITE);
        pnlEnemyTurn.add(lblEnemyTurn, BorderLayout.CENTER);
    }

    // --- Game Logic Methods ---

    private void executePlayerTurn(Position target) {
        if (computerMap.isHit(target) || computerMap.isWater(target)) {
            return;
        }

        boolean hit = computerMap.fireAt(target);
        log("Firing at " + (char)('A' + target.getY()) + (target.getX() + 1) + "...");

        if (hit) {
            log(">> SPLASH! DIRECT HIT!");
            if (!computerMap.hasShips()) {
                gameOver = true;
                log("ENEMY FLEET ELIMINATED. VICTORY!");
                JOptionPane.showMessageDialog(this, "VICTORY!");
            }
        } else {
            log(">> Miss.");
        }

        pnlComputer.repaint();

        if (!gameOver) {
            playerTurn = false;
            updateTurnIndicators(false); // Switch to Enemy

            // Delay for AI
            Timer t = new Timer(800, e -> executeComputerTurn());
            t.setRepeats(false);
            t.start();
        }
    }

    private void executeComputerTurn() {
        Report report = computerAI.takeTurn();
        Position p = report.getP();
        log("Enemy attacking " + (char)('A' + p.getY()) + (p.getX() + 1));

        if (report.isHit()) {
            log(">> ALERT! WE HAVE BEEN HIT!");
            if (report.isSunk()) {
                log(">> SHIP LOST!");
            }
            if (!playerMap.hasShips()) {
                gameOver = true;
                log("FLEET DESTROYED. GAME OVER.");
                JOptionPane.showMessageDialog(this, "DEFEAT!");
            }
        } else {
            log(">> Enemy shot missed.");
        }

        pnlPlayer.repaint();
        playerTurn = true;
        updateTurnIndicators(true); // Switch to Player
    }

    // --- Helper UI Methods ---

    /**
     * Rebuilds the turnContainer to show the "Active" panel BIG on top,
     * and the "Inactive" panel SMALL on bottom.
     */
    private void updateTurnIndicators(boolean isPlayer) {
        turnContainer.removeAll(); // Clear current layout

        if (isPlayer) {
            // --- STATE: PLAYER TURN ---

            // 1. Top Panel (Player - Active)
            stylePanel(pnlYourTurn, lblYourTurn, COLOR_PLAYER, DIM_ACTIVE, true);
            turnContainer.add(pnlYourTurn);

            // 2. Bottom Panel (Enemy - Inactive/Peeking)
            stylePanel(pnlEnemyTurn, lblEnemyTurn, COLOR_ENEMY_DIM, DIM_INACTIVE, false);
            turnContainer.add(pnlEnemyTurn);

        } else {
            // --- STATE: ENEMY TURN ---

            // 1. Top Panel (Enemy - Active)
            stylePanel(pnlEnemyTurn, lblEnemyTurn, COLOR_ENEMY, DIM_ACTIVE, true);
            turnContainer.add(pnlEnemyTurn);

            // 2. Bottom Panel (Player - Inactive/Peeking)
            stylePanel(pnlYourTurn, lblYourTurn, COLOR_PLAYER_DIM, DIM_INACTIVE, false);
            turnContainer.add(pnlYourTurn);
        }

        // Force UI Refresh
        turnContainer.revalidate();
        turnContainer.repaint();
    }

    // Helper to apply size/color styles dynamically
    private void stylePanel(JPanel pnl, JLabel lbl, Color bg, Dimension dim, boolean isActive) {
        pnl.setBackground(bg);
        pnl.setMaximumSize(dim);
        pnl.setPreferredSize(dim);
        pnl.setBorder(isActive ?
                BorderFactory.createLineBorder(Color.WHITE, 2) :
                BorderFactory.createLineBorder(Color.GRAY, 1));

        // Make text smaller if inactive so it fits in the small bar
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