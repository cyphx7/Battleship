package game;

import ui.ShipPlacementFrame;
import ui.FrameSplashscreen;

public class BattleShip {

    public static void main(String[] args) {
        FrameSplashscreen intro = new FrameSplashscreen();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        intro.setVisible(false);
        ShipPlacementFrame manage = new ShipPlacementFrame();

        manage.setVisible(true);
    }
}