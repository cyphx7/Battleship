package game;

public class Position {
    private int x, y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void move(char direction) {
        switch (direction) {
            case 'N': // North
                x--;
                break;
            case 'S': // South
                x++;
                break;
            case 'O': // West (Ovest)
                y--;
                break;
            case 'E': // East
                y++;
                break;
        }
    }

    public String toString() {
        char Y = (char) (y + 65);
        return "" + (x + 1) + " " + Y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    public boolean isOffMap() {
        if (x >= Map.MAP_SIZE || y >= Map.MAP_SIZE || x < 0 || y < 0)
            return true;
        return false;
    }

}