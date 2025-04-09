package com.reversi.client;

import com.reversi.common.EventBus;
import javax.swing.SwingUtilities;

public class ClientMain {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      EventBus eventBus = new EventBus();
      GameView view = new GameView();
      eventBus.register(ServerMessage.class, view);
      GameController controller = new GameController(eventBus);
      view.setController(controller);
      view.createAndShowGUI();
      controller.connectToServer();
    });
  }
}
