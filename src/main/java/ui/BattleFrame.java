package ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import game.Computer;
import game.Map; // Renamed
import game.Ship; // Renamed
import game.Position; // Renamed
import game.Report;

public class BattleFrame implements ActionListener, KeyListener {
    UIMapPanel playerPanel = new UIMapPanel("player");
    UIMapPanel cpuPanel = new UIMapPanel("cpu");
    JFrame frame = new JFrame("Battleship");
    Cursor cursorDefault = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    UIJPanelBG panel = new UIJPanelBG(
            Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/images/battleImg.jpg")));
    Report rep;
    Computer cpu;
    Map cpuMap; // Renamed
    Map playerMap; // Renamed
    int numPlayerShips = 10;
    int numCPUShips = 10;
    StringBuilder sb = new StringBuilder();
    boolean b = true;
    UIStatPanel statPlayer;
    UIStatPanel statCPU;
    JPanel targetPanel = new JPanel(null);
    UIJPanelBG target = new UIJPanelBG(
            Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/images/target.png")));
    ImageIcon wreck = new ImageIcon(getClass().getResource("/res/images/wreck.gif"));
    Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    Timer timer;
    boolean cpuTurn;

    public BattleFrame(LinkedList<int[]> playerShips, Map map) { // Renamed
        playerMap = map;
        cpu = new Computer(map);
        cpuMap = new Map(); // Renamed
        cpuMap.fillRandomly(); // Renamed
        frame.setSize(1080, 700);
        frame.setTitle("Battleship - Pirate Edition");
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(this);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/images/icon.png")));
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Panel containing the ships to eliminate
        statPlayer = new UIStatPanel();
        statCPU = new UIStatPanel();
        statPlayer.setBounds(30, 595, 500, 80);
        statCPU.setBounds(570, 595, 500, 80);
        frame.add(statPlayer);
        frame.add(statCPU);
        // Target Panel
        targetPanel.setBounds(0, 0, 500, 500);
        targetPanel.setOpaque(false);
        playerPanel.sea.add(targetPanel);
        panel.add(playerPanel);
        playerPanel.setBounds(0, 0, UIMapPanel.X, UIMapPanel.Y);
        playerPanel.setOpaque(false);
        panel.add(cpuPanel);
        cpuPanel.setBounds(540, 0, UIMapPanel.X, UIMapPanel.Y);
        frame.add(panel);
        frame.setResizable(false);
        timer = new Timer(2000, new TimerHandler());
        cpuTurn = false;

        for (int i = 0; i < cpuPanel.buttons.length; i++) {
            for (int j = 0; j < cpuPanel.buttons[i].length; j++) {
                cpuPanel.buttons[i][j].addActionListener(this);
                cpuPanel.buttons[i][j].setActionCommand("" + i + " " + j);
            }
        }
        for (int[] v : playerShips) {
            playerPanel.drawShip(v); // Renamed
        }

    }

    void setCell(Report rep, boolean player) { // Renamed from setCasella
        int x = rep.getP().getX(); // Renamed
        int y = rep.getP().getY(); // Renamed
        ImageIcon fire = new ImageIcon(getClass().getResource("/res/images/fireButton.gif"));
        ImageIcon water = new ImageIcon(getClass().getResource("/res/images/grayButton.gif"));
        String what;
        if (rep.isHit()) // Renamed
            what = "X";
        else
            what = "A";
        UIMapPanel mappanel;
        if (!player) {
            mappanel = playerPanel;
        } else {
            mappanel = cpuPanel;
        }
        if (what == "X") {
            mappanel.buttons[x][y].setIcon(fire);
            mappanel.buttons[x][y].setEnabled(false);
            mappanel.buttons[x][y].setDisabledIcon(fire);
            mappanel.buttons[x][y].setCursor(cursorDefault);
        } else {
            mappanel.buttons[x][y].setIcon(water);
            mappanel.buttons[x][y].setEnabled(false);
            mappanel.buttons[x][y].setDisabledIcon(water);
            mappanel.buttons[x][y].setCursor(cursorDefault);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cpuTurn)
            return;
        JButton source = (JButton) e.getSource();
        StringTokenizer st = new StringTokenizer(source.getActionCommand(), " ");
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        Position newP = new Position(x, y); // Renamed
        boolean hit = cpuMap.fireAt(newP); // Renamed
        Report rep = new Report(newP, hit, false);
        this.setCell(rep, true); // Renamed
        if (hit) { // player's turn continues
            Ship sunkShip = cpuMap.checkSunk(newP); // Renamed
            if (sunkShip != null) {
                numCPUShips--;
                setSunk(sunkShip); // Renamed
                if (numCPUShips == 0) {
                    Object[] options = { "New Game", "Exit" };
                    int n = JOptionPane.showOptionDialog(frame, (new JLabel("You Win!", JLabel.CENTER)),
                            "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                            options[1]);
                    if (n == 0) {
                        ShipPlacementFrame restart = new ShipPlacementFrame(); // Renamed
                        restart.setVisible(true);
                        this.frame.setVisible(false);
                    } else {
                        System.exit(0);
                    }
                }
            }
        } else { // CPU's turn
            if (b) {
                timer.start();
                cpuTurn = true;
            }
        }
        frame.requestFocusInWindow();
    }

    private void setSunk(Position p) { // Renamed from setAffondato
        LinkedList<String> possibilities = new LinkedList<String>();
        if (p.getX() != 0) {
            possibilities.add("N");
        }
        if (p.getX() != Map.MAP_SIZE - 1) { // Renamed
            possibilities.add("S");
        }
        if (p.getY() != 0) {
            possibilities.add("O");
        }
        if (p.getY() != Map.MAP_SIZE - 1) { // Renamed
            possibilities.add("E");
        }
        String direction;
        boolean found = false;
        Position currentPos;
        do {
            currentPos = new Position(p);
            if (possibilities.isEmpty()) {
                deleteShip(1, statPlayer);
                playerPanel.buttons[currentPos.getX()][currentPos.getY()].setIcon(wreck);
                playerPanel.buttons[currentPos.getX()][currentPos.getY()].setEnabled(false);
                playerPanel.buttons[currentPos.getX()][currentPos.getY()].setDisabledIcon(wreck);
                playerPanel.buttons[currentPos.getX()][currentPos.getY()].setCursor(cursorDefault);
                return;
            }
            direction = possibilities.removeFirst();
            currentPos.move(direction.charAt(0)); // Renamed
            if (playerMap.isHit(currentPos)) { // Renamed
                found = true;
            }
        } while (!found);
        int size = 0;
        currentPos = new Position(p);
        do {

            playerPanel.buttons[currentPos.getX()][currentPos.getY()].setIcon(wreck);
            playerPanel.buttons[currentPos.getX()][currentPos.getY()].setEnabled(false);
            playerPanel.buttons[currentPos.getX()][currentPos.getY()].setDisabledIcon(wreck);
            playerPanel.buttons[currentPos.getX()][currentPos.getY()].setCursor(cursorDefault);
            currentPos.move(direction.charAt(0)); // Renamed

            size++;
        } while (currentPos.getX() >= 0 && currentPos.getX() <= 9 && currentPos.getY() >= 0
                && currentPos.getY() <= 9 && !playerMap.isWater(currentPos)); // Renamed

        deleteShip(size, statPlayer);
    }

    private void setSunk(Ship sunkShip) { // Renamed from setAffondato
        int size = 0;
        for (int i = sunkShip.getStartX(); i <= sunkShip.getEndX(); i++) {
            for (int j = sunkShip.getStartY(); j <= sunkShip.getEndY(); j++) {
                cpuPanel.buttons[i][j].setIcon(wreck);
                cpuPanel.buttons[i][j].setEnabled(false);
                cpuPanel.buttons[i][j].setDisabledIcon(wreck);
                cpuPanel.buttons[i][j].setCursor(cursorDefault);
                size++;
            }
        }
        deleteShip(size, statCPU);
    }

    private void deleteShip(int size, UIStatPanel panel) {
        switch (size) {
            case 4:
                panel.ships[0].setEnabled(false);
                break;
            case 3:
                if (!panel.ships[1].isEnabled())
                    panel.ships[2].setEnabled(false);
                else
                    panel.ships[1].setEnabled(false);
                break;
            case 2:
                if (!panel.ships[3].isEnabled())
                    if (!panel.ships[4].isEnabled())
                        panel.ships[5].setEnabled(false);
                    else
                        panel.ships[4].setEnabled(false);
                else
                    panel.ships[3].setEnabled(false);
                break;
            case 1:
                if (!panel.ships[6].isEnabled())
                    if (!panel.ships[7].isEnabled())
                        if (!panel.ships[8].isEnabled())
                            panel.ships[9].setEnabled(false);
                        else
                            panel.ships[8].setEnabled(false);
                    else
                        panel.ships[7].setEnabled(false);
                else
                    panel.ships[6].setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        int key = arg0.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            ShipPlacementFrame manage = new ShipPlacementFrame(); // Renamed
            manage.setVisible(true);
            frame.setVisible(false);
        }

        sb.append(arg0.getKeyChar());
        if (sb.length() == 4) {
            int z = sb.toString().hashCode();
            if (z == 3194657) { // "fiki"?? Easter egg
                sb = new StringBuilder();
                b = !b;
            } else {
                String s = sb.substring(1, 4);
                sb = new StringBuilder(s);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}

    public class TimerHandler implements ActionListener { // Renamed from GestoreTimer

        @Override
        public void actionPerformed(ActionEvent arg0) {
            timer.stop();
            boolean flag;

            Report report = cpu.takeTurn(); // Renamed
            drawTarget(report.getP().getX() * 50, report.getP().getY() * 50); // Renamed
            flag = report.isHit();
            setCell(report, false); // Renamed
            if (report.isSunk()) { // Renamed
                numPlayerShips--;
                setSunk(report.getP()); // Renamed
                if (numPlayerShips == 0) {
                    Object[] options = { "New Game", "Exit" };
                    int n = JOptionPane.showOptionDialog(frame, (new JLabel("You Lost!", JLabel.CENTER)),
                            "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                            options[1]);
                    if (n == 0) {
                        ShipPlacementFrame restart = new ShipPlacementFrame(); // Renamed
                        restart.setVisible(true);
                        frame.setVisible(false);
                    } else {
                        System.exit(0);
                    }
                }
            }

            cpuTurn = false;
            if (flag) {
                timer.start();
                cpuTurn = true;
            }
            frame.requestFocusInWindow();
        }

    }

    public void drawTarget(int i, int j) { // Renamed from disegnaTarget
        target.setBounds(j, i, 50, 50);
        target.setVisible(true);
        targetPanel.add(target);
        targetPanel.repaint();
    }
}