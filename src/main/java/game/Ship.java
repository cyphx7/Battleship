package game;

public class Ship {

    private int startX, startY;
    private int endX, endY;

    // --- NEW: Image Property ---
    private String imageName;
    // ---------------------------

    public Ship(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    // --- NEW: Methods for Images ---
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    // -------------------------------

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }

    public boolean contains(int x, int y) {
        return x <= endX && x >= startX && y <= endY && y >= startY;
    }

    public String toString() {
        return startX + "-" + startY + "  " + endX + "-" + endY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ship other = (Ship) obj;
        return endX == other.endX && startX == other.startX && endY == other.endY && startY == other.startY;
    }
}