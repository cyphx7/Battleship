package game;

import java.util.Random;

public class Computer {
    private MyLinkedList<Position> availableShots;
    private Random r;
    private int hitState; // 0=Hunt, 1=Target, 2=Lock
    private MyLinkedList<String> possibilities;
    private Position lastShot;
    private String direction;
    private Map playerMap;
    private Position firstHitPosition;

    public Computer(Map opponentMap) {
        availableShots = new MyLinkedList<>();
        this.playerMap = opponentMap;
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
        if (availableShots.isEmpty() && hitState == 0) return null;

        if (hitState == 0) hit = fireRandomly();
        else if (hitState == 1) hit = fireTargetedStage1();
        else if (hitState >= 2) hit = fireTargetedStage2();

        rep.setP(lastShot);
        rep.setHit(hit);

        if (hit) {
            Ship sunkShip = playerMap.checkSunk(lastShot);
            if (sunkShip != null) {
                rep.setSunk(true);
                hitState = 0;
                direction = null;
                possibilities = null;
            } else {
                if (hitState == 0) {
                    hitState = 1;
                    firstHitPosition = lastShot;
                    possibilities = new MyLinkedList<>();
                    if (firstHitPosition.getX() > 0) possibilities.add("N");
                    if (firstHitPosition.getX() < Map.MAP_SIZE - 1) possibilities.add("S");
                    if (firstHitPosition.getY() > 0) possibilities.add("O");
                    if (firstHitPosition.getY() < Map.MAP_SIZE - 1) possibilities.add("E");
                } else if (hitState == 1) {
                    hitState = 2;
                }
            }
        } else {
            if (hitState == 2) reverseDirection();
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
        if (possibilities == null || possibilities.isEmpty()) {
            hitState = 0;
            return fireRandomly();
        }
        int shotIndex = r.nextInt(possibilities.size());
        String where = possibilities.remove(shotIndex);
        Position p = new Position(firstHitPosition);
        p.move(where.charAt(0));
        direction = where;

        if (p.isOffMap() || playerMap.isHit(p) || playerMap.isWater(p)) return fireTargetedStage1();

        availableShots.remove(p);
        lastShot = p;
        return playerMap.fireAt(p);
    }

    private boolean fireTargetedStage2() {
        Position p = new Position(lastShot);
        p.move(direction.charAt(0));

        while (!p.isOffMap() && playerMap.isHit(p)) p.move(direction.charAt(0));

        if (p.isOffMap() || playerMap.isWater(p)) {
            reverseDirection();
            p = new Position(firstHitPosition);
            p.move(direction.charAt(0));
            while (!p.isOffMap() && playerMap.isHit(p)) p.move(direction.charAt(0));

            if (p.isOffMap() || playerMap.isWater(p) || playerMap.isHit(p)) {
                hitState = 0;
                return fireRandomly();
            }
        }
        availableShots.remove(p);
        lastShot = p;
        return playerMap.fireAt(p);
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