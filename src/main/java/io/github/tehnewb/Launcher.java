package io.github.tehnewb;

import valthorne.JGL;

public class Launcher {

    public static void main(String[] args) {
        JGL.init(new ProjectBuilderApplication(), "Valthorne Project Builder " + Global.VERSION, 700, 700);
    }

}