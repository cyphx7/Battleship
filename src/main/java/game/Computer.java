package game;

import java.util.LinkedList;
import java.util.Random;

public class Computer {
    private LinkedList<Position> availableShots;
    private Random r;
    private int hitState; // 0 = searching, 1 = hit, 2+ = targeting
    private LinkedList<String> possibilities;
    private Position lastShot;
    private String direction;
    private Map playerMap;
    private Position firstHitPosition; // position where the ship was first hit

    public Computer(Map opponentMap) {
        availableShots = new LinkedList<Position>();
        this.playerMap = opponentMap;
        for (int i = 0; i < Map.MAP_SIZE; i++) {
            for (int j = 0; j < Map.MAP_SIZE; j++) {
                Position p = new Position(i, j);
                availableShots.add(p); // Initialize all possible shots
            }
        }
        r = new Random();
        hitState = 0;
    }

    public Report takeTurn() {

        Report rep = new Report();
        if (hitState == 0) {
            boolean hit = fireRandomly();
            rep.setP(lastShot);
            rep.setHit(hit);
            Ship sunkShip;
            if (hit) {
                hitState++;
                sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    removeSurroundingWater(sunkShip);
                    hitState = 0;
                    direction = null;
                } else {
                    firstHitPosition = lastShot;
                    possibilities = new LinkedList<String>();
                    initializePossibilities();
                }
            }
            return rep;
        } // Firing randomly
        if (hitState == 1) {
            boolean hit = fireTargetedStage1();
            Ship sunkShip;
            rep.setP(lastShot);
            rep.setHit(hit);
            rep.setSunk(false);
            if (hit) {
                hitState++;
                possibilities = null;
                sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    removeSurroundingWater(sunkShip);
                    hitState = 0;
                    direction = null;
                }
            }
            return rep;
        }
        if (hitState >= 2) {
            boolean hit = fireTargetedStage2();
            Ship sunkShip;
            rep.setP(lastShot);
            rep.setHit(hit);
            rep.setSunk(false);
            if (hit) {
                hitState++;
                sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    removeSurroundingWater(sunkShip);
                    hitState = 0;
                    direction = null;
                }
            } else {
                reverseDirection();
            }
            return rep;
        }
        return null; // unreachable
    }

    private boolean fireRandomly() {
        int shot = r.nextInt(availableShots.size());
        Position p = availableShots.remove(shot);
        lastShot = p;
        boolean hit = playerMap.fireAt(p);
        return hit;
    }

    private boolean fireTargetedStage1() {
        boolean error = true;
        Position p = null;
        do {
            int shot = r.nextInt(possibilities.size());
            String where = possibilities.remove(shot);
            p = new Position(firstHitPosition);
            p.move(where.charAt(0));
            direction = where;
            if (!playerMap.isWater(p)) {
                availableShots.remove(p);
                error = false;
            }
        } while (error); // verifies that we don't try to shoot at a position already hit
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage2() {
        boolean canHit = false;
        Position p = new Position(lastShot);
        do {
            p.move(direction.charAt(0));

            if (p.isOffMap() || playerMap.isWater(p)) {
                reverseDirection();
            } else {
                if (!playerMap.isHit(p)) {
                    canHit = true;
                }
            }
        } while (!canHit);
        availableShots.remove(p);
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private void removeSurroundingWater(Ship sunkShip) {
        int Xin = sunkShip.getStartX();
        int Xfin = sunkShip.getEndX();
        int Yin = sunkShip.getStartY();
        int Yfin = sunkShip.getEndY();
        if (Xin == Xfin) { // horizontal
            if (Yin != 0) {
                Position p = new Position(Xin, Yin - 1);
                if (!playerMap.isWater(p)) {
                    availableShots.remove(p);
                    playerMap.setWater(p);
                }
            }
            if (Yfin != Map.MAP_SIZE - 1) {
                Position p = new Position(Xin, Yfin + 1);
                if (!playerMap.isWater(p)) {
                    availableShots.remove(p);
                    playerMap.setWater(p);
                }
            }
            if (Xin != 0) {
                for (int i = 0; i <= Yfin - Yin; i++) {
                    Position p = new Position(Xin - 1, Yin + i);
                    if (!playerMap.isWater(p)) {
                        availableShots.remove(p);
                        playerMap.setWater(p);
                    }
                }
            }
            if (Xin != Map.MAP_SIZE - 1) {
                for (int i = 0; i <= Yfin - Yin; i++) {
                    Position p = new Position(Xin + 1, Yin + i);
                    if (!playerMap.isWater(p)) {
                        availableShots.remove(p);
                        playerMap.setWater(p);
                    }
                }
            }
        } else { // vertical
            if (Xin != 0) {
                Position p = new Position(Xin - 1, Yin);
                if (!playerMap.isWater(p)) {
                    availableShots.remove(p);
                    playerMap.setWater(p);
                }
            }
            if (Xfin != Map.MAP_SIZE - 1) {
                Position p = new Position(Xfin + 1, Yin);
                if (!playerMap.isWater(p)) {
                    availableShots.remove(p);
                    playerMap.setWater(p);
                }
            }
            if (Yin != 0) {
                for (int i = 0; i <= Xfin - Xin; i++) {
                    Position p = new Position(Xin + i, Yin - 1);
                    if (!playerMap.isWater(p)) {
                        availableShots.remove(p);
                        playerMap.setWater(p);
                    }
                }
            }
            if (Yfin != Map.MAP_SIZE - 1) {
                for (int i = 0; i <= Xfin - Xin; i++) {
                    Position p = new Position(Xin + i, Yin + 1);
                    if (!playerMap.isWater(p)) {
                        availableShots.remove(p);
                        playerMap.setWater(p);
                    }
                }
            }
        }
    }

    private void initializePossibilities() {
        if (lastShot.getX() != 0) {
            possibilities.add("N");
        }
        if (lastShot.getX() != Map.MAP_SIZE - 1) {
            possibilities.add("S");
        }
        if (lastShot.getY() != 0) {
            possibilities.add("O");
        }
        if (lastShot.getY() != Map.MAP_SIZE - 1) {
            possibilities.add("E");
        }
    }

    private void reverseDirection() {
        if (direction.equals("N")) {
            direction = "S";
        } else if (direction.equals("S")) {
            direction = "N";
        } else if (direction.equals("E")) {
            direction = "O";
        } else if (direction.equals("O")) {
            direction = "E";
        }
    }
}