package ui;

import game.Map;
import game.Position;
import game.Ship;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class UIMapPanel extends JPanel {
    private Map map;
    private final boolean isPlayer;

    // --- GRID SETTINGS ---
    private final int CELL_SIZE = 55;
    private final int GRID_SIZE = Map.MAP_SIZE * CELL_SIZE;
    private final int OFFSET = 45;

    // --- OVERLAP SETTINGS ---
    private final int IMAGE_OVERLAP = 20;

    // --- CACHES ---
    private java.util.Map<String, BufferedImage> coordImages = new HashMap<>();
    private java.util.Map<String, BufferedImage> shipSlices = new HashMap<>();
    private BufferedImage seaImage;

    // --- NEW IMAGES ---
    private BufferedImage imgHit;    // wreck w fire.png
    private BufferedImage imgSunk;   // wreck_.png
    private BufferedImage imgTarget; // target.png

    private CellClickListener listener;
    private Point hoverCell = null; // Track mouse for target.png

    public interface CellClickListener {
        void onCellClicked(Position p);
    }

    public UIMapPanel(Map map, boolean isPlayer) {
        this.map = map;
        this.isPlayer = isPlayer;
        setOpaque(false);
        setPreferredSize(new Dimension(GRID_SIZE + OFFSET + 20, GRID_SIZE + OFFSET + 20));

        loadImages();

        // Click Listener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverCell = null;
                repaint();
            }
        });

        // --- NEW: Motion Listener for Target.png ---
        if (!isPlayer) { // Only show target on enemy map
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int col = (e.getX() - OFFSET) / CELL_SIZE;
                    int row = (e.getY() - OFFSET) / CELL_SIZE;
                    if (row >= 0 && row < Map.MAP_SIZE && col >= 0 && col < Map.MAP_SIZE) {
                        hoverCell = new Point(col, row);
                    } else {
                        hoverCell = null;
                    }
                    repaint();
                }
            });
        }
    }

    public void updateMap(Map newMap) {
        this.map = newMap;
        this.repaint();
    }

    public void setOnCellClicked(CellClickListener listener) {
        this.listener = listener;
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        if (listener == null) return;
        int col = (mouseX - OFFSET) / CELL_SIZE;
        int row = (mouseY - OFFSET) / CELL_SIZE;
        if (row >= 0 && row < Map.MAP_SIZE && col >= 0 && col < Map.MAP_SIZE) {
            listener.onCellClicked(new Position(row, col));
        }
    }

    private void loadImages() {
        try {
            // 1. Backgrounds & Debris
            URL seaUrl = getClass().getResource("/res/images/sea.png");
            if (seaUrl != null) seaImage = ImageIO.read(seaUrl);

            // NEW: Load Hit, Sunk, Target images
            URL hitUrl = getClass().getResource("/res/images/wreck w fire.png");
            if (hitUrl != null) imgHit = ImageIO.read(hitUrl);

            URL sunkUrl = getClass().getResource("/res/images/wreck_.png");
            if (sunkUrl != null) imgSunk = ImageIO.read(sunkUrl);

            URL targetUrl = getClass().getResource("/res/images/target.png");
            if (targetUrl != null) imgTarget = ImageIO.read(targetUrl);

            // 2. Coords
            loadCoordinates();

            // 3. Sliced Ships
            String[] prefixes = {"ship1", "ship2", "ship3", "ship4", "ship5"};
            for (String prefix : prefixes) {
                for (int i = 1; i <= 12; i++) {
                    String filename = prefix + "." + i + ".png";
                    URL url = getClass().getResource("/res/images/" + filename);
                    if (url != null) {
                        try {
                            shipSlices.put(prefix + "_" + i, ImageIO.read(url));
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCoordinates() {
        for (int i = 1; i <= 10; i++) {
            try {
                URL url = getClass().getResource("/res/images/coord/" + i + ".png");
                if (url != null) coordImages.put(String.valueOf(i), ImageIO.read(url));
            } catch (Exception e) {}
        }
        char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        for (int i = 0; i < letters.length; i++) {
            String letter = String.valueOf(letters[i]);
            try {
                URL url = getClass().getResource("/res/images/coord/" + letter + ".png");
                if (url == null) url = getClass().getResource("/res/images/coord/" + (11 + i) + ".png");
                if (url != null) coordImages.put(letter, ImageIO.read(url));
            } catch (Exception e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (seaImage != null) {
            g.drawImage(seaImage, OFFSET, OFFSET, GRID_SIZE, GRID_SIZE, null);
        } else {
            g.setColor(new Color(0, 100, 200));
            g.fillRect(OFFSET, OFFSET, GRID_SIZE, GRID_SIZE);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        drawCoordinates(g2d);
        drawGridLines(g2d);
        drawMapContent(g2d);

        // --- NEW: Draw Target Crosshair ---
        if (!isPlayer && hoverCell != null) {
            int x = OFFSET + hoverCell.x * CELL_SIZE;
            int y = OFFSET + hoverCell.y * CELL_SIZE;
            if (imgTarget != null) {
                g2d.drawImage(imgTarget, x, y, CELL_SIZE, CELL_SIZE, null);
            } else {
                g2d.setColor(new Color(255, 0, 0, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawCoordinates(Graphics2D g) {
        int imgSize = 35;
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);

        char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        for (int col = 0; col < Map.MAP_SIZE; col++) {
            String text = String.valueOf(letters[col]);
            BufferedImage img = coordImages.get(text);
            int x = OFFSET + col * CELL_SIZE + (CELL_SIZE - imgSize) / 2;
            int y = (OFFSET - imgSize) / 2;
            if (img != null) g.drawImage(img, x, y, imgSize, imgSize, null);
            else g.drawString(text, x + 10, y + 25);
        }

        for (int row = 0; row < Map.MAP_SIZE; row++) {
            String text = String.valueOf(row + 1);
            BufferedImage img = coordImages.get(text);
            int x = (OFFSET - imgSize) / 2;
            int y = OFFSET + row * CELL_SIZE + (CELL_SIZE - imgSize) / 2;
            if (img != null) g.drawImage(img, x, y, imgSize, imgSize, null);
            else g.drawString(text, x + 10, y + 25);
        }
    }

    private void drawGridLines(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= Map.MAP_SIZE; i++) {
            g.drawLine(OFFSET, OFFSET + i * CELL_SIZE, OFFSET + GRID_SIZE, OFFSET + i * CELL_SIZE);
            g.drawLine(OFFSET + i * CELL_SIZE, OFFSET, OFFSET + i * CELL_SIZE, OFFSET + GRID_SIZE);
        }
    }

    private void drawMapContent(Graphics2D g) {
        // Draw Ships (Active)
        if (isPlayer) {
            for (Ship s : map.getShipList()) {
                drawShip(g, s);
            }
        }

        // Draw Hits / Misses / Sunk Debris
        for (int row = 0; row < Map.MAP_SIZE; row++) {
            for (int col = 0; col < Map.MAP_SIZE; col++) {
                int x = OFFSET + col * CELL_SIZE;
                int y = OFFSET + row * CELL_SIZE;
                char cell = map.getGridAt(row, col);

                if (cell == Map.HIT) {
                    // Check if this hit is part of a sunk ship
                    if (map.isSunkAt(row, col)) {
                        drawSunk(g, x, y);
                    } else {
                        drawHit(g, x, y);
                    }
                } else if (cell == Map.WATER || cell == Map.MISS) {
                    drawMiss(g, x, y);
                }
            }
        }
    }

    private void drawShip(Graphics2D g, Ship ship) {
        boolean isHorizontal = (ship.getStartX() == ship.getEndX());
        int size = Math.max(Math.abs(ship.getEndX() - ship.getStartX()), Math.abs(ship.getEndY() - ship.getStartY())) + 1;

        String rawName = ship.getImageName();
        if (rawName == null) rawName = "ship1.png";

        String prefix = rawName.toLowerCase().replace(".png", "").replace(" ", "");

        if (size == 5) prefix = "ship5";

        boolean invertVertical = prefix.equals("ship2") || prefix.equals("ship3") || prefix.equals("ship4") || prefix.equals("ship5");

        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? ship.getStartX() : ship.getStartX() + i;
            int col = isHorizontal ? ship.getStartY() + i : ship.getStartY();

            int x = OFFSET + col * CELL_SIZE;
            int y = OFFSET + row * CELL_SIZE;

            int sliceIndex;
            if (isHorizontal) {
                sliceIndex = i + 1;
            } else {
                if (invertVertical) sliceIndex = (size * 2) - i;
                else sliceIndex = i + 1 + size;
            }

            BufferedImage img = shipSlices.get(prefix + "_" + sliceIndex);

            if (img != null) {
                g.drawImage(img,
                        x - IMAGE_OVERLAP,
                        y - IMAGE_OVERLAP,
                        CELL_SIZE + (IMAGE_OVERLAP * 2),
                        CELL_SIZE + (IMAGE_OVERLAP * 2),
                        null);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            }
        }
    }

    // --- NEW: Draw "wreck w fire.png" ---
    private void drawHit(Graphics2D g, int x, int y) {
        if (imgHit != null) {
            g.drawImage(imgHit, x, y, CELL_SIZE, CELL_SIZE, null);
        } else {
            g.setColor(new Color(255, 50, 50, 200));
            g.setStroke(new BasicStroke(4f));
            g.drawLine(x + 10, y + 10, x + CELL_SIZE - 10, y + CELL_SIZE - 10);
            g.drawLine(x + CELL_SIZE - 10, y + 10, x + 10, y + CELL_SIZE - 10);
        }
    }

    // --- NEW: Draw "wreck_.png" (Sunk) ---
    private void drawSunk(Graphics2D g, int x, int y) {
        if (imgSunk != null) {
            g.drawImage(imgSunk, x, y, CELL_SIZE, CELL_SIZE, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(x + 10, y + 10, CELL_SIZE - 20, CELL_SIZE - 20);
        }
    }

    private void drawMiss(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(x + CELL_SIZE / 2 - 8, y + CELL_SIZE / 2 - 8, 16, 16);
    }
}