package game;

import java.util.Random;
// Removed: import java.util.LinkedList;

public class Map {
    public static final int MAP_SIZE = 10;
    public static final char EMPTY = '0', SHIP = 'X', WATER = 'A', HIT = 'C';

    private char[][] mapGrid;
    private MyLinkedList<Ship> shipList; // Changed to MyLinkedList

    public Map() {
        shipList = new MyLinkedList<>(); // Changed
        mapGrid = new char[MAP_SIZE][MAP_SIZE];
        for (int i = 0; i < MAP_SIZE; i++)
            for (int j = 0; j < MAP_SIZE; j++)
                mapGrid[i][j] = EMPTY;
    }

    public char getGridAt(int row, int col) {
        return mapGrid[row][col];
    }

    public void fillRandomly() {
        clear();
        Random r = new Random();

        // Standard Battleship Fleet
        placeShipRandomly(r, 5);
        placeShipRandomly(r, 4);
        placeShipRandomly(r, 3);
        placeShipRandomly(r, 3);
        placeShipRandomly(r, 2);
    }

    private void clear() {
        shipList.clear();
        for (int i = 0; i < MAP_SIZE; i++)
            for (int j = 0; j < MAP_SIZE; j++)
                mapGrid[i][j] = EMPTY;
    }

    public boolean placeShip(int x, int y, int size, int direction) {
        if (direction == 1 && x + size > MAP_SIZE) return false;
        if (direction == 0 && y + size > MAP_SIZE) return false;

        boolean placed;
        if (direction == 0) placed = checkHorizontal(x, y, size);
        else placed = checkVertical(x, y, size);

        if (!placed) return false;

        if (direction == 0) {
            shipList.add(new Ship(x, y, x, y + size - 1));
        } else {
            shipList.add(new Ship(x, y, x + size - 1, y));
        }

        for (int i = 0; i < size; i++) {
            if (direction == 0) mapGrid[x][y + i] = SHIP;
            else mapGrid[x + i][y] = SHIP;
        }
        return true;
    }

    public int[] placeShipRandomly(Random random, int size) {
        boolean placed;
        int[] data = new int[4];
        int direction, row, col;
        do {
            placed = true;
            direction = random.nextInt(2);
            if (direction == 0) {
                col = random.nextInt(MAP_SIZE - size + 1);
                row = random.nextInt(MAP_SIZE);
            } else {
                col = random.nextInt(MAP_SIZE);
                row = random.nextInt(MAP_SIZE - size + 1);
            }
            if (direction == 0) placed = checkHorizontal(row, col, size);
            else placed = checkVertical(row, col, size);
        } while (!placed);

        placeShip(row, col, size, direction);

        data[0] = row; data[1] = col; data[2] = size; data[3] = direction;
        return data;
    }

    // Strict Adjacency Checks
    public boolean checkVertical(int row, int col, int size) {
        int rStart = Math.max(0, row - 1);
        int rEnd = Math.min(MAP_SIZE - 1, row + size);
        int cStart = Math.max(0, col - 1);
        int cEnd = Math.min(MAP_SIZE - 1, col + 1);

        for (int r = rStart; r <= rEnd; r++) {
            for (int c = cStart; c <= cEnd; c++) {
                if (mapGrid[r][c] == SHIP) return false;
            }
        }
        return true;
    }

    public boolean checkHorizontal(int row, int col, int size) {
        int rStart = Math.max(0, row - 1);
        int rEnd = Math.min(MAP_SIZE - 1, row + 1);
        int cStart = Math.max(0, col - 1);
        int cEnd = Math.min(MAP_SIZE - 1, col + size);

        for (int r = rStart; r <= rEnd; r++) {
            for (int c = cStart; c <= cEnd; c++) {
                if (mapGrid[r][c] == SHIP) return false;
            }
        }
        return true;
    }

    public boolean fireAt(Position p) {
        int row = p.getX();
        int col = p.getY();
        if (mapGrid[row][col] == SHIP) {
            mapGrid[row][col] = HIT;
            checkSunk(p);
            return true;
        } else if (mapGrid[row][col] == EMPTY) {
            mapGrid[row][col] = WATER;
        }
        return false;
    }

    public Ship checkSunk(Position p) {
        int row = p.getX();
        int col = p.getY();
        Ship ship = null;

        // This loop works because MyLinkedList implements Iterable
        for (Ship s : shipList) {
            if (s.contains(row, col)) {
                ship = s;
                break;
            }
        }
        if (ship == null) return null;

        for (int i = ship.getStartX(); i <= ship.getEndX(); i++) {
            for (int j = ship.getStartY(); j <= ship.getEndY(); j++) {
                if (mapGrid[i][j] != HIT) return null;
            }
        }
        shipList.remove(ship);
        return ship;
    }

    public void setWater(Position p) { mapGrid[p.getX()][p.getY()] = WATER; }
    public boolean isWater(Position p) { return mapGrid[p.getX()][p.getY()] == WATER; }
    public boolean isHit(Position p) { return mapGrid[p.getX()][p.getY()] == HIT; }
    public boolean hasShips() { return !shipList.isEmpty(); }
}