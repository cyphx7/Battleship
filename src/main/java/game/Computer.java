package game;

import java.util.Random;

public class Computer {
    private MyLinkedList<Position> availableShots;
    private Random r;
    // hitState 0: Hunt (Random)
    // hitState 1: Target (Finding Direction)
    // hitState 2: Target (Following Direction)
    private int hitState;
    private MyLinkedList<String> possibilities;
    private Position lastShot;
    private String direction;
    private Map playerMap;
    private Position firstHitPosition;

    public Computer(Map opponentMap) {
        availableShots = new MyLinkedList<>();
        this.playerMap = opponentMap;

        // Fill available shots with all grid coordinates
        for (int i = 0; i < Map.MAP_SIZE; i++) {
            for (int j = 0; j < Map.MAP_SIZE; j++) {
                availableShots.add(new Position(i, j));
            }
        }
        r = new Random();
        hitState = 0;
    }

    public Report takeTurn() {
        Report rep = new Report();
        boolean hit = false;

        // Safety Fallback: If AI has no shots left, do nothing
        if (availableShots.isEmpty() && hitState == 0) {
            return null;
        }

        // --- Execute Strategy based on State ---
        if (hitState == 0) {
            hit = fireRandomly();
        } else if (hitState == 1) {
            hit = fireTargetedStage1();
        } else if (hitState >= 2) {
            hit = fireTargetedStage2();
        }

        rep.setP(lastShot);
        rep.setHit(hit);

        // --- Post-Shot Logic ---
        if (hit) {
            Ship sunkShip = playerMap.checkSunk(lastShot);
            if (sunkShip != null) {
                // Ship Sunk: Reset to Hunting Mode
                rep.setSunk(true);
                hitState = 0;
                direction = null;
                possibilities = null; // Clear memory
            } else {
                // Hit but not sunk: Advance Targeting
                if (hitState == 0) {
                    hitState = 1;
                    firstHitPosition = lastShot;
                    possibilities = new MyLinkedList<>();
                    initializePossibilities();
                } else if (hitState == 1) {
                    hitState = 2; // Found the line, lock it in
                }
            }
        } else {
            // Miss: Adjust Strategy
            if (hitState == 2) {
                reverseDirection(); // Try the other end of the ship
            }
        }
        return rep;
    }

    private boolean fireRandomly() {
        if (availableShots.isEmpty()) return false;

        int shotIndex = r.nextInt(availableShots.size());
        Position p = availableShots.remove(shotIndex);

        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage1() {
        // --- FIX 1: DEFENSIVE CHECK ---
        // This prevents the NullPointerException if 'possibilities' is missing
        if (possibilities == null || possibilities.isEmpty()) {
            hitState = 0; // Give up targeting, go back to random
            return fireRandomly();
        }

        int shotIndex = r.nextInt(possibilities.size());
        String where = possibilities.remove(shotIndex);

        Position p = new Position(firstHitPosition);
        p.move(where.charAt(0));
        direction = where;

        // Validate shot: If off map or already hit, try again (recurse)
        // If the spot is invalid, we immediately try the next possibility
        if (p.isOffMap() || playerMap.isHit(p) || playerMap.isWater(p)) {
            return fireTargetedStage1();
        }

        // Valid shot found
        availableShots.remove(p); // Remove from global pool so we don't shoot twice
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage2() {
        Position p = new Position(lastShot);
        p.move(direction.charAt(0));

        // --- FIX 2: "Jump" over debris ---
        // If we see a HIT, it's likely part of our ship. Keep moving in the same direction.
        while (!p.isOffMap() && playerMap.isHit(p)) {
            p.move(direction.charAt(0));
        }

        // Now we are at a new spot. Check if it's valid (Empty).
        // If it is Water (Miss) or Off Map, we hit a true dead end.
        if (p.isOffMap() || playerMap.isWater(p)) {
            reverseDirection();
            p = new Position(firstHitPosition);
            p.move(direction.charAt(0));

            // --- FIX 2 (Reverse side): Jump over debris in the NEW direction too ---
            while (!p.isOffMap() && playerMap.isHit(p)) {
                p.move(direction.charAt(0));
            }

            // If BOTH ends are blocked by Water/Walls, this ship is done or confusing.
            // Reset to random hunt.
            if (p.isOffMap() || playerMap.isWater(p) || playerMap.isHit(p)) {
                hitState = 0;
                return fireRandomly();
            }
        }

        // If we get here, 'p' is a valid, unhit target.
        availableShots.remove(p);
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private void initializePossibilities() {
        // Add valid cardinal directions to the List
        // Check boundaries to avoid adding directions that are immediately off-map
        if (firstHitPosition.getX() > 0) possibilities.add("N");
        if (firstHitPosition.getX() < Map.MAP_SIZE - 1) possibilities.add("S");
        if (firstHitPosition.getY() > 0) possibilities.add("O"); // West (Ovest)
        if (firstHitPosition.getY() < Map.MAP_SIZE - 1) possibilities.add("E");
    }

    private void reverseDirection() {
        if (direction == null) return;
        switch (direction) {
            case "N": direction = "S"; break;
            case "S": direction = "N"; break;
            case "E": direction = "O"; break;
            case "O": direction = "E"; break;
        }
    }
}