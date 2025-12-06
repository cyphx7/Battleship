package ui;

import game.Computer;
import game.Map;
import game.MyLinkedList;
import game.Position;
import game.Report;
import game.Ship;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

class BackgroundPanel extends JPanel {
    private BufferedImage background;
    
    public BackgroundPanel() {
        super();
        try {
            URL bgUrl = getClass().getResource("/res/images/battleImg.jpg");
            if (bgUrl != null) {
                background = ImageIO.read(bgUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            // Scale the image to fit the panel
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback color if image loading fails
            g.setColor(new Color(30, 30, 60));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

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

    private UIStatPanel statPanel;

    private boolean playerTurn = true;
    private boolean gameOver = false;

    private final Dimension DIM_TURN_CONTAINER = new Dimension(300, 150);
    private final int ABILITY_BTN_W = 135;
    private final int ABILITY_BTN_H = 45;

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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        
        // Set content pane to our custom background panel
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout());
        
        // Apply the look and feel settings
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Game board container
        JPanel gameBoardContainer = new JPanel(new BorderLayout());
        gameBoardContainer.setOpaque(false);
        gameBoardContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblPlayerHeader = createHeaderLabel("/res/images/yourfleet.png", 300, 50);
        JLabel lblEnemyHeader = createHeaderLabel("/res/images/enemysector.png", 300, 50);
        headerPanel.add(lblPlayerHeader);
        headerPanel.add(lblEnemyHeader);

        // Map panel
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

        // Add components to game board container
        gameBoardContainer.add(headerPanel, BorderLayout.NORTH);
        gameBoardContainer.add(mapPanel, BorderLayout.CENTER);

        // Create a wrapper panel for game board and log
        JPanel gameBoardWrapper = new JPanel(new BorderLayout());
        gameBoardWrapper.setOpaque(false);
        gameBoardWrapper.add(gameBoardContainer, BorderLayout.CENTER);

        // Create log area
        logArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                // Semi-transparent background for the text area
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.BOLD, 14));
        logArea.setForeground(Color.BLACK);
        logArea.setOpaque(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        // Create scroll pane for log
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200), 2, true));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 150)); // Fixed height

        // Add log to wrapper
        gameBoardWrapper.add(scroll, BorderLayout.SOUTH);

        // Add wrapper to center panel
        centerPanel.add(gameBoardWrapper, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Right panel setup
        rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(40, 40, 40, 200)); // Semi-transparent dark background
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setOpaque(false);
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

        JLabel lblAbilities = createHeaderLabel("/res/images/specialabilities.png", 300, 50);
        lblAbilities.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblAbilities);
        rightPanel.add(Box.createVerticalStrut(10));

        JPanel abilityGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        abilityGrid.setOpaque(false);
        abilityGrid.setPreferredSize(new Dimension(280, ABILITY_BTN_H));
        abilityGrid.setMaximumSize(new Dimension(280, ABILITY_BTN_H));
        abilityGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnScout = createGraphicAbilityButton("/res/images/scout.png", ABILITY_BTN_W, ABILITY_BTN_H);
        btnTsunami = createGraphicAbilityButton("/res/images/tsunami.png", ABILITY_BTN_W, ABILITY_BTN_H);

        btnScout.addActionListener(e -> {
            activeAbility = 1;
            log(">> SCOUT PLANE READY. Select a target zone.");
            btnScout.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            btnTsunami.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        });

        btnTsunami.addActionListener(e -> {
            activeAbility = 2;
            log(">> TSUNAMI STRIKE READY. Select a target zone.");
            btnTsunami.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            btnScout.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        });

        abilityGrid.add(btnScout);
        abilityGrid.add(btnTsunami);
        rightPanel.add(abilityGrid);

        rightPanel.add(Box.createVerticalStrut(15));

        JButton btnSolve = new JButton("AUTO-PILOT");
        styleButton(btnSolve);
        btnSolve.setBackground(new Color(205, 178, 161)); // Darker brown
        btnSolve.setForeground(Color.WHITE);
        btnSolve.setOpaque(true); // Make sure the button is opaque
        btnSolve.setBorderPainted(false); // Remove the default border
        btnSolve.setFocusPainted(false); // Remove the focus border
        btnSolve.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSolve.setMaximumSize(new Dimension(280, 45));
        btnSolve.setPreferredSize(new Dimension(280, 45));
        btnSolve.addActionListener(e -> runSolver());
        rightPanel.add(btnSolve);

        // --- UI STAT PANEL (Size parameters already set to use full remaining space) ---
        rightPanel.add(Box.createVerticalStrut(15));
        statPanel = new UIStatPanel();

        statPanel.setPreferredSize(new Dimension(300, 150));
        statPanel.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        statPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(statPanel);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(statPanel);
        rightPanel.add(Box.createVerticalGlue());

        add(rightPanel, BorderLayout.EAST);
        checkAbilityAvailability();
        log("Battle stations ready. Select a target on the Enemy Sector.");
        updateUIStatPanel();
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

                    if (path.contains("turn.png") && targetWidth == (DIM_TURN_CONTAINER.width - 20)) {
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

    private JLabel createHeaderLabel(String imagePath, int width, int height) {
        JLabel l = new JLabel();
        l.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = loadScaledImage(imagePath, width, height);

        if (icon != null) {
            l.setIcon(icon);
        } else {
            l.setText(imagePath.substring(imagePath.lastIndexOf('/') + 1).replace(".png", "").toUpperCase());
            l.setFont(new Font("Arial", Font.BOLD, 22));
            l.setForeground(Color.ORANGE);
        }
        return l;
    }

    private JButton createGraphicAbilityButton(String imagePath, int width, int height) {
        JButton btn = new JButton();
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));

        ImageIcon icon = loadScaledImage(imagePath, width, height);

        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("");
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        } else {
            btn.setText(imagePath.substring(imagePath.lastIndexOf('/') + 1).replace(".png", "").toUpperCase());
            styleButton(btn);
        }
        return btn;
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

    private int[] getShipSizes(MyLinkedList<Ship> shipList) {
        int[] sizes = new int[shipList.size()];
        for (int i = 0; i < shipList.size(); i++) {
            Ship s = shipList.get(i);
            sizes[i] = Math.max(Math.abs(s.getEndX() - s.getStartX()), Math.abs(s.getEndY() - s.getStartY())) + 1;
        }
        return sizes;
    }

    // In BattleFrame.java
    private void updateUIStatPanel() {
        // Get all player and computer ships
        MyLinkedList<Ship> playerShips = playerMap.getShipList();
        MyLinkedList<Ship> computerShips = computerMap.getShipList();
        
        // Get all sunk ships
        MyLinkedList<Ship> playerSunkShips = playerMap.getSunkList();
        MyLinkedList<Ship> computerSunkShips = computerMap.getSunkList();
        
        // Initialize arrays for ship states and sizes
        boolean[] playerShipStates = new boolean[5];
        boolean[] computerShipStates = new boolean[5];
        int[] playerSizes = new int[5];
        int[] computerSizes = new int[5];
        
        // Default ship sizes (2, 3, 3, 4, 5)
        int[] defaultSizes = {2, 3, 3, 4, 5};
        
        // Initialize with default sizes and all ships alive
        for (int i = 0; i < 5; i++) {
            playerSizes[i] = defaultSizes[i];
            computerSizes[i] = defaultSizes[i];
            playerShipStates[i] = true;
            computerShipStates[i] = true;
        }
        
        // Update player ship states based on sunk ships
        for (int i = 0; i < playerSunkShips.size(); i++) {
            Ship s = playerSunkShips.get(i);
            int size = Math.max(Math.abs(s.getEndX() - s.getStartX()), 
                              Math.abs(s.getEndY() - s.getStartY())) + 1;
            // Find the index of this ship size and mark it as sunk
            for (int j = 0; j < defaultSizes.length; j++) {
                if (defaultSizes[j] == size && playerShipStates[j]) {
                    playerShipStates[j] = false;
                    break;
                }
            }
        }
        
        // Update computer ship states based on sunk ships
        for (int i = 0; i < computerSunkShips.size(); i++) {
            Ship s = computerSunkShips.get(i);
            int size = Math.max(Math.abs(s.getEndX() - s.getStartX()), 
                              Math.abs(s.getEndY() - s.getStartY())) + 1;
            // Find the index of this ship size and mark it as sunk
            for (int j = 0; j < defaultSizes.length; j++) {
                if (defaultSizes[j] == size && computerShipStates[j]) {
                    computerShipStates[j] = false;
                    break;
                }
            }
        }

        // Update the stat panel with the current states
        if (statPanel != null) {
            statPanel.updateStatus(playerSizes, computerSizes, playerShipStates, computerShipStates);
        }
    }

    private void checkAbilityAvailability() {
        if (playerMap.isShipAlive(2)) {
            btnScout.setEnabled(true);
            if(activeAbility != 1) btnScout.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        } else {
            btnScout.setEnabled(false);
            btnScout.setText("SCOUT (LOST)");
            btnScout.setBackground(Color.BLACK);
            btnScout.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        if (playerMap.isShipAlive(5)) {
            btnTsunami.setEnabled(true);
            if(activeAbility != 2) btnTsunami.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        } else {
            btnTsunami.setEnabled(false);
            btnTsunami.setText("TSUNAMI (LOST)");
            btnTsunami.setBackground(Color.BLACK);
            btnTsunami.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
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
        btnScout.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        btnTsunami.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        endPlayerTurn();
        updateUIStatPanel();
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
        btnScout.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        btnTsunami.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        endPlayerTurn();
        updateUIStatPanel();
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

        updateUIStatPanel();
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
        updateUIStatPanel();
        playerTurn = true;
        updateTurnIndicators(true);
    }

    private void runSolver() {
        if (gameOver) return;
        log(">> AUTO-PILOT ENGAGED...");
        playerTurn = false;
        updateTurnIndicators(false);

        // Stop any existing timer
        if (solverTimer != null && solverTimer.isRunning()) {
            solverTimer.stop();
        }

        solverTimer = new Timer(150, e -> {
            if (gameOver) {
                ((Timer)e.getSource()).stop();
                return;
            }

            // Let the solver take a turn
            Report report = solverAI.takeTurn();
            if (report == null) return;

            pnlComputer.repaint();
            updateUIStatPanel();

            if (report.isHit() && !computerMap.hasShips()) {
                gameOver = true;
                JOptionPane.showMessageDialog(this, "PUZZLE SOLVED!");
                ((Timer)e.getSource()).stop();
                return;
            }

            // After solver's turn, let the computer take its turn
            Timer computerTimer = new Timer(800, evt -> {
                executeComputerTurn();
                // After computer's turn, continue with solver's next move
                if (!gameOver) {
                    ((Timer)e.getSource()).restart();
                }
            });
            computerTimer.setRepeats(false);
            computerTimer.start();

            // Pause the solver timer while computer is moving
            ((Timer)e.getSource()).stop();

        });

        // Set initial delay to 0 for immediate first move
        solverTimer.setInitialDelay(0);
        solverTimer.start();
    }


    private void styleButton(JButton btn) {
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}