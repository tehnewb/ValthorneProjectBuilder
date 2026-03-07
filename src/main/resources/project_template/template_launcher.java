package PACKAGE_NAME;

import valthorne.JGL;
import valthorne.scene.GameScreen;

public class Launcher {

    public static void main(String[] args) {
        JGL.init(new GameScreen(new APPLICATION_NAME()), "PROJECT_NAME", 800, 600);
    }

}