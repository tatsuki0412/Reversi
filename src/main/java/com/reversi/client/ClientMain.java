package com.reversi.client;

import com.reversi.common.EventBus;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import javax.swing.SwingUtilities;

public class ClientMain implements EventListener<ServerMessage> {
  // Entry point
  public static void main(String[] args) { new ClientMain().start(); }

  EventBus eventBus = new EventBus();
  LobbyView lobbyView = new LobbyView();
  GameController controller = new GameController(eventBus);

  private void start() {
    SwingUtilities.invokeLater(() -> {
      lobbyView.setController(controller);
      eventBus.register(ServerMessage.class, lobbyView.getGameView());
      eventBus.register(ServerMessage.class, this);

      controller.connectToServer();
    });
  }

  @Override
  public void onEvent(ServerMessage e) {
    if (e.getMessage().getType() == Message.Type.Start)
      lobbyView.switchToGameView();
  }
}
