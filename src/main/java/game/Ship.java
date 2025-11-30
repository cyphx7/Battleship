package game;

public class Ship {

    private int startX, startY;
    private int endX, endY;

    public Ship(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    /**
     * Checks if a coordinate (x, y) is part of this ship.
     */
    public boolean contains(int x, int y) {
        if (x <= endX && x >= startX && y <= endY && y >= startY) {
            return true;
        }
        return false;

    }

    public String toString() {
        return startX + "-" + startY + "  " + endX + "-" + endY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ship other = (Ship) obj;
        if (endX != other.endX)
            return false;
        if (startX != other.startX)
            return false;
        if (endY != other.endY)
            return false;
        if (startY != other.startY)
            return false;
        return true;
    }

}