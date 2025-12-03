package ui;

import game.Map;
import game.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class UIMapPanel extends JPanel {
    private Map map;
    private boolean isSelf; // true = your fleet, false = enemy fleet
    private final int GRID_SIZE = 10;

    // Margin size in pixels for the Labels (A-J, 1-10)
    private final int LABEL_OFFSET = 30;

    private Consumer<Position> onCellClicked;

    public UIMapPanel(Map map, boolean isSelf) {
        this.map = map;
        this.isSelf = isSelf;
        // Increased size to accommodate the labels
        this.setPreferredSize(new Dimension(350, 350));
        this.setBackground(new Color(245, 245, 245)); // Light gray background for the label area
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onCellClicked != null) {
                    // Adjust coordinate calculation to account for the margin (OFFSET)
                    int mouseX = e.getX() - LABEL_OFFSET;
                    int mouseY = e.getY() - LABEL_OFFSET;

                    // If user clicks in the margin/label area, ignore it
                    if (mouseX < 0 || mouseY < 0) return;

                    int width = getWidth() - LABEL_OFFSET;
                    int height = getHeight() - LABEL_OFFSET;

                    int cellW = width / GRID_SIZE;
                    int cellH = height / GRID_SIZE;

                    // Safety check to prevent crash on resize
                    if (cellW <= 0 || cellH <= 0) return;

                    int col = mouseX / cellW;
                    int row = mouseY / cellH;

                    if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                        onCellClicked.accept(new Position(row, col));
                    }
                }
            }
        });
    }

    public void setOnCellClicked(Consumer<Position> listener) {
        this.onCellClicked = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Convert Graphics to Graphics2D for better rendering and features like setStroke
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - LABEL_OFFSET;
        int height = getHeight() - LABEL_OFFSET;

        int cellW = width / GRID_SIZE;
        int cellH = height / GRID_SIZE;

        // --- DRAW LABELS ---
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();

        // 1. Draw Column Numbers (1-10) along the Top
        for (int col = 0; col < GRID_SIZE; col++) {
            String num = String.valueOf(col + 1);
            int x = LABEL_OFFSET + (col * cellW) + (cellW - fm.stringWidth(num)) / 2;
            int y = (LABEL_OFFSET + fm.getAscent()) / 2 - 3; // Center vertically in top margin
            g2.drawString(num, x, y);
        }

        // 2. Draw Row Letters (A-J) along the Left
        for (int row = 0; row < GRID_SIZE; row++) {
            String letter = String.valueOf((char)('A' + row));
            int x = (LABEL_OFFSET - fm.stringWidth(letter)) / 2;
            int y = LABEL_OFFSET + (row * cellH) + (cellH + fm.getAscent()) / 2 - 5; // Center vertically in cell
            g2.drawString(letter, x, y);
        }

        // --- DRAW GRID AREA ---
        // Shift drawing origin to skip the margins
        g2.translate(LABEL_OFFSET, LABEL_OFFSET);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int x = col * cellW;
                int y = row * cellH;

                char status = map.getGridAt(row, col);

                // A. Draw Cell Background
                if (status == Map.WATER) {
                    g2.setColor(new Color(135, 206, 250)); // SkyBlue (Miss)
                }
                else if (status == Map.HIT) {
                    g2.setColor(new Color(220, 20, 60)); // Crimson (Hit)
                }
                else if (isSelf && status == Map.SHIP) {
                    g2.setColor(Color.GRAY); // Your Ship
                }
                else {
                    // Default Water
                    if (isSelf) g2.setColor(new Color(240, 248, 255)); // AliceBlue
                    else g2.setColor(new Color(255, 240, 240)); // LavenderBlush (Enemy Fog)
                }

                g2.fillRect(x, y, cellW, cellH);

                // B. Draw Grid Lines
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1)); // Reset stroke for grid lines
                g2.drawRect(x, y, cellW, cellH);

                // C. Draw Marks (X for Hit, O for Miss)
                if (status == Map.HIT) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2)); // <--- FIXED: Using g2 here
                    g2.drawLine(x + 5, y + 5, x + cellW - 5, y + cellH - 5);
                    g2.drawLine(x + cellW - 5, y + 5, x + 5, y + cellH - 5);
                }
                else if (status == Map.WATER) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + cellW/2 - 3, y + cellH/2 - 3, 6, 6);
                }
            }
        }

        // Draw Main Border around the grid
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2)); // <--- FIXED: Using g2 here
        g2.drawRect(0, 0, width, height);

        // Reset translation
        g2.translate(-LABEL_OFFSET, -LABEL_OFFSET);
    }
}