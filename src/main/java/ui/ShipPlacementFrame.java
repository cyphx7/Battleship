package ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;

import game.Map; // Changed from Mappa

public class ShipPlacementFrame extends JFrame implements ActionListener, KeyListener {
    private static final long serialVersionUID = 2923975805665801740L;
    private static final int NUM_SHIPS = 10;
    LinkedList<int[]> playerShips; // contains the placed ships, needed for BattleFrame
    boolean finished = false;
    int shipsPlaced = 0;
    int[] shipCounter = { 1, 2, 3, 4 };
    Map map; // Changed from Mappa
    UIManagePanel choosePan;
    UIMapPanel mapPanel;

    public ShipPlacementFrame() {
        super("Battleship - Pirate Edition");
        map = new Map(); // Changed from Mappa
        playerShips = new LinkedList<int[]>();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(900, 672);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/images/icon.png")));
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        UIJPanelBG container = new UIJPanelBG(
                Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/images/wood.jpg")));
        mapPanel = new UIMapPanel("manage");
        choosePan = new UIManagePanel();
        container.add(mapPanel);
        container.add(choosePan);
        mapPanel.setBounds(25, 25, 600, 620);
        choosePan.setBounds(580, 25, 280, 800);
        // Internal panel containing ships to place.
        this.add(container);
        for (int i = 0; i < mapPanel.buttons.length; i++) {
            for (int j = 0; j < mapPanel.buttons[i].length; j++) {
                mapPanel.buttons[i][j].addActionListener(this);
                mapPanel.buttons[i][j].setActionCommand("" + i + " " + j);
            }
        }
        choosePan.random.addActionListener(this);
        choosePan.reset.addActionListener(this);
        choosePan.playButton.addActionListener(this); // Renamed from gioca
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        String text = source.getText();
        // RESET
        if (text.equals("reset")) {
            reset();
        }
        // RANDOM
        else if (text.equals("random")) {
            random();
        }
        // PLAY
        else if (text.equals("play")) { // Renamed from gioca
            play();

        } else {
            if (finished) {
                return;
            }
            StringTokenizer st = new StringTokenizer(source.getActionCommand(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int shipType = -1;
            int size = 0;
            int dir;
            for (int i = 0; i < choosePan.ship.length; i++) {
                if (choosePan.ship[i].isSelected())
                    shipType = i;
            }
            switch (shipType) {
                case 0: size = 4; break;
                case 1: size = 3; break;
                case 2: size = 2; break;
                case 3: size = 1; break;
            }
            if (choosePan.direction[0].isSelected())// 0=horizontal 1=vertical
                dir = 0;
            else
                dir = 1;
            boolean placed = map.placeShip(x, y, size, dir); // Used placeShip
            if (placed) {
                // increment the number of ships placed
                shipsPlaced++;
                // decrement the counter for the placed ship
                shipCounter[shipType]--;
                choosePan.counterLabel[shipType].setText("" + shipCounter[shipType]);
                // disable the ship if all have been placed and
                // automatically select another ship to place
                if (choosePan.counterLabel[shipType].getText().equals("0")) {
                    choosePan.ship[shipType].setEnabled(false);
                    for (int i = 0; i < choosePan.ship.length; i++) {
                        if (choosePan.ship[i].isEnabled() && !choosePan.ship[i].isSelected()) {
                            choosePan.ship[i].setSelected(true);
                            break;
                        }
                    }
                }
                // check if we have placed all ships (10)
                if (shipsPlaced == NUM_SHIPS) {
                    finished = true;
                    choosePan.direction[0].setEnabled(false);
                    choosePan.direction[1].setEnabled(false);
                    choosePan.playButton.setEnabled(true); // Renamed from gioca
                }
                int[] data = { x, y, size, dir };
                playerShips.add(data);
                mapPanel.drawShip(data); // Renamed from disegnaNave
            }
        }
        this.requestFocusInWindow();
    }

    private void random() {
        if (shipsPlaced == NUM_SHIPS) {
            reset();
        }
        Random r = new Random();
        int[] data = new int[4];
        for (int i = 0; i < shipCounter.length; i++) {
            for (int j = 0; j < shipCounter[i]; j++) {
                data = map.placeShipRandomly(r, shipCounter.length - i); // Renamed
                playerShips.add(data);
                mapPanel.drawShip(data); // Renamed
            }
        }
        shipsPlaced = NUM_SHIPS;
        finished = true;
        choosePan.playButton.setEnabled(true); // Renamed
        for (int i = 0; i < choosePan.ship.length; i++) {
            choosePan.ship[i].setEnabled(false);
        }
        choosePan.direction[0].setEnabled(false);
        choosePan.direction[1].setEnabled(false);
        for (int i = 0; i < shipCounter.length; i++) {
            shipCounter[i] = 0;
            choosePan.counterLabel[i].setText("0");
        }
        choosePan.ship[0].setSelected(true);

    }

    private void reset() {
        map = new Map(); // Renamed
        playerShips = new LinkedList<int[]>();
        for (int i = 0; i < Map.MAP_SIZE; i++) { // Renamed
            for (int j = 0; j < Map.MAP_SIZE; j++) { // Renamed
                mapPanel.buttons[i][j].setEnabled(true);
            }
        }
        finished = false;
        choosePan.playButton.setEnabled(false); // Renamed
        for (int i = 0; i < choosePan.ship.length; i++) {
            choosePan.ship[i].setEnabled(true);
        }
        choosePan.direction[0].setEnabled(true);
        choosePan.direction[1].setEnabled(true);
        for (int i = 0; i < shipCounter.length; i++) {
            shipCounter[i] = i + 1;
            choosePan.counterLabel[i].setText("" + (i + 1));
        }
        choosePan.ship[0].setSelected(true);
        shipsPlaced = 0;
    }

    private void play() {
        BattleFrame battle = new BattleFrame(playerShips, map); // Renamed
        battle.frame.setVisible(true);
        this.setVisible(false);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        char s = Character.toLowerCase(arg0.getKeyChar());
        int key = arg0.getKeyCode();
        if (s == 'g') {
            random();
            play();
        } else {
            if (s == 'r') {
                random();
            } else {
                if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE) {
                    reset();
                } else {
                    if (key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
                if (key == KeyEvent.VK_ENTER) {
                    if (finished) {
                        play();
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}

}