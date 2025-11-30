package game;

public class Report {
    private Position p;
    private boolean hit;
    private boolean sunk;

    public Report() {
    }

    public Report(Position p, boolean hit, boolean sunk) {
        this.p = p;
        this.hit = hit;
        this.sunk = sunk;
    }

    public Position getP() {
        return p;
    }

    public void setP(Position p) {
        this.p = p;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isSunk() {
        return sunk;
    }

    public void setSunk(boolean sunk) {
        this.sunk = sunk;
    }

    public String toString() {
        return "coordinates:" + p + " hit:" + hit + " sunk:" + sunk;
    }
}