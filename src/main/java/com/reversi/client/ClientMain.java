package com.reversi.client;

import com.reversi.common.EventBus;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import com.reversi.common.Player;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ClientMain implements EventListener<ServerMessage> {
  // Entry point
  public static void main(String[] args) { new ClientMain().start(); }

  private EventBus eventBus = new EventBus();
  private ServerSocket controller = new ServerSocket(eventBus);

  private JFrame frame;
  private JPanel mainPanel;
  private CardLayout cardLayout;

  private LobbyView lobbyView;
  private GameView gameView;

  private int status = 0;

  private void start() {
    SwingUtilities.invokeLater(() -> {
      frame = new JFrame();
      cardLayout = new CardLayout();
      mainPanel = new JPanel(cardLayout);
      frame.add(mainPanel);
      frame.setSize(600, 600);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      lobbyView = new LobbyView();
      lobbyView.setController(controller);
      mainPanel.add(lobbyView.getMainPanel(), "lobby");

      gameView = new GameView();
      gameView.setController(controller);
      mainPanel.add(gameView.getGamePanel(), "game");

      eventBus.register(ServerMessage.class, this);

      controller.connectToServer();
      cardLayout.show(mainPanel, "lobby");
      frame.setVisible(true);
    });
  }

  @Override
  public void onEvent(ServerMessage e) {
    Message msg = e.getMessage();
    switch (msg.getType()) {
    case Start:
      cardLayout.show(mainPanel, "game");
      status = 1;
      Message.Start start = (Message.Start)msg.getMessage();
      gameView.setUs(Player.from(start.getColor()));
      break;
    case GameUpdate:
      Message.GameUpdate upd = (Message.GameUpdate)msg.getMessage();
      gameView.update(upd.getGame());
      break;
    case GameOver:
      Message.GameOver over = (Message.GameOver)msg.getMessage();
      gameView.showGameOver(over.getReason());
    case Invalid:
      Message.Invalid invalid = (Message.Invalid)msg.getMessage();
      if (status == 1)
        gameView.showInvalidMove(invalid.getReason());
      else
        lobbyView.showError(invalid.getReason());
      break;
    case LobbyUpdate:
      Message.LobbyUpdate update = (Message.LobbyUpdate)msg.getMessage();
      lobbyView.update(update.getLobbyRooms());
      break;
    default:
      break;
    }
  }
}
