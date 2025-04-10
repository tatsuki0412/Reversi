package com.reversi.client;

import com.reversi.common.EventBus;
import javax.swing.SwingUtilities;

public class ClientMain {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      EventBus eventBus = new EventBus();
      LobbyView lobbyView = new LobbyView();
      GameController controller = new GameController(eventBus);
      lobbyView.setController(controller);
      // Register the game view to receive server messages.
      eventBus.register(ServerMessage.class, lobbyView.getGameView());
      // Connect to the server.
      controller.connectToServer();
    });
  }
}
