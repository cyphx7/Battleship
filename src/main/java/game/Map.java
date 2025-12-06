package game;

import java.util.Random;

public class Map {
    public static final int MAP_SIZE = 10;
    public static final char EMPTY = '0', SHIP = 'X', WATER = 'A', HIT = 'C', MISS = 'M';
    private char[][] mapGrid;
    private MyLinkedList<Ship> shipList;
    private MyLinkedList<Ship> sunkList;

    public Map() {
        shipList = new MyLinkedList<>();
        sunkList = new MyLinkedList<>();
        mapGrid = new char[MAP_SIZE][MAP_SIZE];
        for (int i = 0; i < MAP_SIZE; i++)
            for (int j = 0; j < MAP_SIZE; j++)
                mapGrid[i][j] = EMPTY;
    }

    public char getGridAt(int row, int col) {
        if (!isValidPos(row, col)) return EMPTY;
        return mapGrid[row][col];
    }

    public void setGridAt(int row, int col, char c) {
        if(isValidPos(row, col)) mapGrid[row][col] = c;
    }

    public boolean isValidPos(int row, int col) {
        return row >= 0 && row < MAP_SIZE && col >= 0 && col < MAP_SIZE;
    }

    public MyLinkedList<Ship> getShipList() {
        return shipList;
    }

    // --- NEW: Accessor for sunk ships ---
    public MyLinkedList<Ship> getSunkList() {
        return sunkList;
    }

    public Ship getShipAt(Position p) {
        for (int i = 0; i < shipList.size(); i++) {
            Ship s = shipList.get(i);
            if (s.contains(p.getX(), p.getY())) {
                return s;
            }
        }
        return null;
    }

    public void fillRandomly() {
        clear();
        Random r = new Random();
        Ship s;
        s = placeShipGet(r, 5); if(s!=null) s.setImageName("ship5.png");
        s = placeShipGet(r, 4); if(s!=null) s.setImageName("ship4.png");
        s = placeShipGet(r, 3); if(s!=null) s.setImageName("ship3.png");
        s = placeShipGet(r, 3); if(s!=null) s.setImageName("ship2.png");
        s = placeShipGet(r, 2); if(s!=null) s.setImageName("ship1.png");
    }

    private Ship placeShipGet(Random r, int size) {
        placeShipRandomly(r, size);
        if (shipList.size() > 0) {
            return shipList.get(shipList.size() - 1);
        }
        return null;
    }

    private void clear() {
        shipList.clear();
        sunkList.clear(); // Clear sunk list too
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
        if (!isValidPos(row, col)) return false;

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

        for (int i = 0; i < shipList.size(); i++) {
            Ship s = shipList.get(i);
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

        // --- UPDATED: Move to sunk list instead of just removing ---
        sunkList.add(ship);
        shipList.remove(ship);
        return ship;
    }

    public boolean isSunkAt(int row, int col) {
        for (int i = 0; i < sunkList.size(); i++) {
            if (sunkList.get(i).contains(row, col)) return true;
        }
        return false;
    }

    public boolean isHit(Position p) { return mapGrid[p.getX()][p.getY()] == HIT; }
    public boolean isWater(Position p) { return mapGrid[p.getX()][p.getY()] == WATER || mapGrid[p.getX()][p.getY()] == MISS; }
    public boolean hasShips() { return !shipList.isEmpty(); }

    public boolean isShipAlive(int size) {
        for (int i = 0; i < shipList.size(); i++) {
            Ship s = shipList.get(i);
            int shipSize = Math.max(Math.abs(s.getEndX() - s.getStartX()), Math.abs(s.getEndY() - s.getStartY())) + 1;
            if (shipSize == size) return true;
        }
        return false;
    }
}