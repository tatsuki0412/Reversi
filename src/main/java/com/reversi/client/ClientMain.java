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
  private GameController controller = new GameController(eventBus);

  private JFrame frame;
  private JPanel mainPanel;
  private CardLayout cardLayout;

  private LobbyView lobbyView;
  private GameView gameView;

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
      mainPanel.add(gameView.getMainPanel(), "game");

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
      Message.Start start = (Message.Start)msg.getMessage();
      gameView.setPlayer(Player.from(start.getColor()));
      break;
    case Board:
      Message.BoardUpdate boardupd = (Message.BoardUpdate)msg.getMessage();
      gameView.updateBoard(boardupd.getBoard());
      break;
    case Turn:
      Message.Turn turn = (Message.Turn)msg.getMessage();
      gameView.updateTurn(turn.getIsYours());
      break;
    case Invalid:
      Message.Invalid invalid = (Message.Invalid)msg.getMessage();
      gameView.showInvalidMove(invalid.getReason());
      break;
    default:
      break;
    }
  }
}
