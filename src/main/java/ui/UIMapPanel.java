package ui;

import game.Map;
import game.Position;
import game.Ship;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class UIMapPanel extends JPanel {
    // Removed 'final' so we can update the map without recreating the panel
    private Map map;
    private final boolean isPlayer;

    // --- GRID SETTINGS ---
    private final int CELL_SIZE = 65;
    private final int GRID_SIZE = Map.MAP_SIZE * CELL_SIZE;
    private final int OFFSET = 50;

    // --- CACHES ---
    private java.util.Map<String, BufferedImage> coordImages = new HashMap<>();
    private java.util.Map<String, BufferedImage> shipImagesHorizontal = new HashMap<>();
    private java.util.Map<String, BufferedImage> shipImagesVertical = new HashMap<>();
    private BufferedImage seaImage;

    private CellClickListener listener;

    public interface CellClickListener {
        void onCellClicked(Position p);
    }

    public UIMapPanel(Map map, boolean isPlayer) {
        this.map = map;
        this.isPlayer = isPlayer;
        setOpaque(false);
        // Force the panel to stay big enough
        setPreferredSize(new Dimension(GRID_SIZE + OFFSET + 20, GRID_SIZE + OFFSET + 20));

        loadImages(); // Load images ONLY ONCE

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    // --- NEW: Method to update map without reloading images (Fixes LAG) ---
    public void updateMap(Map newMap) {
        this.map = newMap;
        this.repaint();
    }

    private void loadImages() {
        try {
            URL seaUrl = getClass().getResource("/res/images/sea.png");
            if (seaUrl != null) seaImage = ImageIO.read(seaUrl);

            loadCoordinates();

            String[] ships = {"ship1.png", "ship2.png", "ship3.png", "ship 3.png", "ship4.png"};
            for (String s : ships) {
                String path = "/res/images/" + s;
                URL url = getClass().getResource(path);
                if (url != null) {
                    BufferedImage original = ImageIO.read(url);
                    shipImagesHorizontal.put(s, original);
                    shipImagesVertical.put(s, createRotatedImage(original, 90));
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

    private BufferedImage createRotatedImage(BufferedImage src, double degrees) {
        double radians = Math.toRadians(degrees);
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int newWidth = (int) Math.abs(srcWidth * Math.cos(radians) + srcHeight * Math.sin(radians));
        int newHeight = (int) Math.abs(srcHeight * Math.cos(radians) + srcWidth * Math.sin(radians));

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, src.getType());
        Graphics2D g2d = rotated.createGraphics();
        g2d.translate((newWidth - srcWidth) / 2, (newHeight - srcHeight) / 2);
        g2d.rotate(radians, srcWidth / 2, srcHeight / 2);
        g2d.drawRenderedImage(src, null);
        g2d.dispose();
        return rotated;
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

        drawCoordinates(g2d);
        drawGridLines(g2d);
        drawMapContent(g2d);
    }

    private void drawCoordinates(Graphics2D g) {
        int imgSize = 40;
        g.setFont(new Font("Arial", Font.BOLD, 20));
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
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i <= Map.MAP_SIZE; i++) {
            g.drawLine(OFFSET, OFFSET + i * CELL_SIZE, OFFSET + GRID_SIZE, OFFSET + i * CELL_SIZE);
            g.drawLine(OFFSET + i * CELL_SIZE, OFFSET, OFFSET + i * CELL_SIZE, OFFSET + GRID_SIZE);
        }
    }

    private void drawMapContent(Graphics2D g) {
        if (isPlayer) {
            for (Ship s : map.getShipList()) {
                drawShip(g, s);
            }
        }
        for (int row = 0; row < Map.MAP_SIZE; row++) {
            for (int col = 0; col < Map.MAP_SIZE; col++) {
                int x = OFFSET + col * CELL_SIZE;
                int y = OFFSET + row * CELL_SIZE;
                char cell = map.getGridAt(row, col);
                if (cell == Map.HIT) drawHit(g, x, y);
                else if (cell == Map.WATER) drawMiss(g, x, y);
            }
        }
    }

    private void drawShip(Graphics2D g, Ship ship) {
        int x = OFFSET + ship.getStartY() * CELL_SIZE;
        int y = OFFSET + ship.getStartX() * CELL_SIZE;
        boolean isHorizontal = ship.getStartX() == ship.getEndX();
        BufferedImage shipImg = isHorizontal ? shipImagesHorizontal.get(ship.getImageName()) : shipImagesVertical.get(ship.getImageName());

        int width = isHorizontal ? (ship.getEndY() - ship.getStartY() + 1) * CELL_SIZE : CELL_SIZE;
        int height = isHorizontal ? CELL_SIZE : (ship.getEndX() - ship.getStartX() + 1) * CELL_SIZE;

        if (shipImg != null) {
            g.drawImage(shipImg, x, y, width, height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(x+5, y+5, width-10, height-10);
        }
    }

    private void drawHit(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 50, 50, 200));
        g.setStroke(new BasicStroke(4f));
        g.drawLine(x + 10, y + 10, x + CELL_SIZE - 10, y + CELL_SIZE - 10);
        g.drawLine(x + CELL_SIZE - 10, y + 10, x + 10, y + CELL_SIZE - 10);
    }

    private void drawMiss(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(x + CELL_SIZE / 2 - 10, y + CELL_SIZE / 2 - 10, 20, 20);
    }
}