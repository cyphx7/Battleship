package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class UIStatPanel extends UIJPanelBG {
    private static final long serialVersionUID = 1L;

    private final int[] SHIP_SIZES = {1, 2, 3, 4, 5};
    private final String[] SHIP_NAMES = {"Patrol Boat", "Submarine", "Destroyer", "Battleship", "Aircraft Carrier"};
    private final String[] SHIP_IMAGE_PATHS = {
            "ship1.png",
            "ship2.png",
            "ship3.png",
            "ship4.png",
            "ship4.png"
    };

    private ArrayList<JLabel> playerShips = new ArrayList<>();
    private ArrayList<JLabel> enemyShips = new ArrayList<>();
    private JPanel playerPanel;
    private JPanel enemyPanel;

    private final int SHIP_ICON_HEIGHT = 30;
    private final int MAX_SHIP_WIDTH = 80;

    public UIStatPanel() {
        super(Toolkit.getDefaultToolkit()
                .createImage(ShipPlacementFrame.class.getResource("/res/images/battlePaper.png")));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setOpaque(false);

        add(createHeaderLabel("YOUR FLEET", new Color(50, 205, 150)));
        add(Box.createVerticalStrut(5));
        playerPanel = createShipIconPanel(playerShips, true);
        add(playerPanel);
        add(Box.createVerticalStrut(10));

        add(createHeaderLabel("ENEMY FLEET", new Color(255, 100, 100)));
        add(Box.createVerticalStrut(5));
        enemyPanel = createShipIconPanel(enemyShips, false);
        add(enemyPanel);
        add(Box.createVerticalGlue());
    }

    private JPanel createShipIconPanel(ArrayList<JLabel> shipList, boolean isPlayer) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < SHIP_SIZES.length; i++) {
            JPanel shipRow = new JPanel(new BorderLayout(5, 0));
            shipRow.setOpaque(false);
            shipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            shipRow.setMaximumSize(new Dimension(280, 45));
            shipRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setOpaque(false);
            statusPanel.setPreferredSize(new Dimension(15, 30));

            JLabel statusIcon = new JLabel();
            statusIcon.setOpaque(true);
            statusIcon.setBackground(new Color(0, 200, 0));
            statusIcon.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            statusPanel.add(statusIcon, BorderLayout.CENTER);

            JPanel shipPanel = new JPanel(new BorderLayout());
            shipPanel.setOpaque(false);
            shipPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            JLabel shipIcon = new JLabel();
            try {
                String imagePath = "/res/images/" + SHIP_IMAGE_PATHS[i];
                URL imgUrl = getClass().getResource(imagePath);
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(ImageIO.read(imgUrl));
                    double aspectRatio = (double) icon.getIconWidth() / icon.getIconHeight();
                    int width = (int) (SHIP_ICON_HEIGHT * aspectRatio);
                    width = Math.min(width, MAX_SHIP_WIDTH);
                    Image scaledImage = icon.getImage().getScaledInstance(
                            width, SHIP_ICON_HEIGHT, Image.SCALE_SMOOTH);
                    shipIcon.setIcon(new ImageIcon(scaledImage));
                    shipIcon.setToolTipText(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
                } else {
                    shipIcon.setText(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
                }
            } catch (Exception e) {
                shipIcon.setText(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + ")");
            }
            shipIcon.setForeground(Color.WHITE);
            shipIcon.setFont(new Font("Arial", Font.BOLD, 11));

            shipPanel.add(shipIcon, BorderLayout.WEST);
            shipRow.add(statusPanel, BorderLayout.WEST);
            shipRow.add(shipPanel, BorderLayout.CENTER);

            shipList.add(statusIcon);
            panel.add(shipRow);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        container.add(scrollPane);
        return container;
    }

    private JLabel createHeaderLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return label;
    }

    public void updateStatus(int[] playerSizes, int[] computerSizes,
                           boolean[] playerShipStates, boolean[] computerShipStates) {
        updateShipStatus(playerShips, playerSizes, playerShipStates);
        updateShipStatus(enemyShips, computerSizes, computerShipStates);
        revalidate();
        repaint();
    }

    private void updateShipStatus(ArrayList<JLabel> shipIcons, int[] shipSizes, boolean[] shipStates) {
        if (shipSizes == null || shipStates == null || shipIcons.isEmpty()) {
            return;
        }

        for (int i = 0; i < shipIcons.size() && i < shipStates.length; i++) {
            shipIcons.get(i).setBackground(
                    shipStates[i] ? new Color(0, 200, 0) : new Color(200, 0, 0)
            );
        }
    }
}