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
    private Position firstHitPosition;

    public Computer(Map opponentMap) {
        availableShots = new LinkedList<Position>();
        this.playerMap = opponentMap;
        for (int i = 0; i < Map.MAP_SIZE; i++) {
            for (int j = 0; j < Map.MAP_SIZE; j++) {
                Position p = new Position(i, j);
                availableShots.add(p);
            }
        }
        r = new Random();
        hitState = 0;
    }

    public Report takeTurn() {
        Report rep = new Report();

        // --- STATE 0: HUNTING (Random Fire) ---
        if (hitState == 0) {
            boolean hit = fireRandomly();
            rep.setP(lastShot);
            rep.setHit(hit);

            if (hit) {
                hitState++;
                Ship sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    // REMOVED: removeSurroundingWater(sunkShip); <--- caused blue markers
                    hitState = 0;
                    direction = null;
                } else {
                    firstHitPosition = lastShot;
                    possibilities = new LinkedList<String>();
                    initializePossibilities();
                }
            }
            return rep;
        }

        // --- STATE 1: TARGETING (Finding Direction) ---
        if (hitState == 1) {
            boolean hit = fireTargetedStage1();
            rep.setP(lastShot);
            rep.setHit(hit);
            rep.setSunk(false);

            if (hit) {
                hitState++;
                possibilities = null; // Found direction, stop guessing
                Ship sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    // REMOVED: removeSurroundingWater(sunkShip);
                    hitState = 0;
                    direction = null;
                }
            }
            return rep;
        }

        // --- STATE 2: TARGETING (Following Direction) ---
        if (hitState >= 2) {
            boolean hit = fireTargetedStage2();
            rep.setP(lastShot);
            rep.setHit(hit);
            rep.setSunk(false);

            if (hit) {
                hitState++;
                Ship sunkShip = playerMap.checkSunk(lastShot);
                if (sunkShip != null) {
                    rep.setSunk(true);
                    // REMOVED: removeSurroundingWater(sunkShip);
                    hitState = 0;
                    direction = null;
                }
            } else {
                reverseDirection(); // Missed, so go the other way
            }
            return rep;
        }
        return null; // unreachable
    }

    private boolean fireRandomly() {
        if (availableShots.isEmpty()) return false; // Safety check
        int shot = r.nextInt(availableShots.size());
        Position p = availableShots.remove(shot);
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage1() {
        boolean error = true;
        Position p = null;
        do {
            // FIX: Prevent crash if possibilities run out
            if (possibilities.isEmpty()) {
                hitState = 0; // Reset to random hunting
                return fireRandomly();
            }

            int shot = r.nextInt(possibilities.size());
            String where = possibilities.remove(shot);
            p = new Position(firstHitPosition);
            p.move(where.charAt(0));
            direction = where;

            // Check valid target
            if (!playerMap.isWater(p) && !playerMap.isHit(p)) {
                availableShots.remove(p);
                error = false;
            }
        } while (error);

        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage2() {
        boolean canHit = false;
        Position p = new Position(lastShot);

        // Loop to find next valid spot in current direction
        int attempts = 0;
        do {
            p.move(direction.charAt(0));
            attempts++;

            if (p.isOffMap() || playerMap.isWater(p) || playerMap.isHit(p)) {
                reverseDirection();
                p = new Position(firstHitPosition); // Reset to anchor
                // If we reversed and still can't find a spot, break to avoid infinite loop
                if (attempts > Map.MAP_SIZE) {
                    hitState = 0;
                    return fireRandomly();
                }
            } else {
                canHit = true;
            }
        } while (!canHit);

        availableShots.remove(p);
        lastShot = p;
        return playerMap.fireAt(p);
    }

    // REMOVED: private void removeSurroundingWater(Ship sunkShip) { ... }

    private void initializePossibilities() {
        if (lastShot.getX() != 0) possibilities.add("N");
        if (lastShot.getX() != Map.MAP_SIZE - 1) possibilities.add("S");
        if (lastShot.getY() != 0) possibilities.add("O");
        if (lastShot.getY() != Map.MAP_SIZE - 1) possibilities.add("E");
    }

    private void reverseDirection() {
        if (direction == null) return;
        if (direction.equals("N")) direction = "S";
        else if (direction.equals("S")) direction = "N";
        else if (direction.equals("E")) direction = "O";
        else if (direction.equals("O")) direction = "E";
    }
}