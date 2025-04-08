package com.reversi.client;

import javax.swing.SwingUtilities;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameView view = new GameView();
            GameController controller = new GameController(view);
            view.setController(controller);
            view.createAndShowGUI();
            controller.connectToServer();
        });
    }
}
