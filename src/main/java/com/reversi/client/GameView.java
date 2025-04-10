package com.reversi.client;

import com.reversi.common.Board;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameView implements EventListener<ServerMessage> {
  private static final Logger logger = LoggerFactory.getLogger(GameView.class);

  private JPanel mainPanel;
  private JButton[][] buttons = new JButton[8][8];
  private GameController controller;
  private JLabel statusLabel;
  private char playerColor;

  public GameView() {
    mainPanel = new JPanel(new BorderLayout());
    initComponents();
  }

  private void initComponents() {
    JPanel boardPanel = new JPanel(new GridLayout(8, 8));
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        JButton btn = new JButton("");
        final int row = i, col = j;
        btn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (controller != null) {
              controller.sendMove(row, col);
            } else {
              logger.error("Game controller not set.");
            }
          }
        });
        buttons[i][j] = btn;
        boardPanel.add(btn);
      }
    }
    statusLabel = new JLabel("Game has not started.");
    mainPanel.add(statusLabel, BorderLayout.NORTH);
    mainPanel.add(boardPanel, BorderLayout.CENTER);
  }

  public JPanel getMainPanel() { return mainPanel; }

  public void setController(GameController c) { controller = c; }

  public void setPlayerColor(char color) {
    playerColor = color;
    SwingUtilities.invokeLater(() -> {
      statusLabel.setText("You are " + ((color == 'B') ? "Black" : "White"));
    });
  }

  @Override
  public void onEvent(ServerMessage e) {
    Message msg = e.getMessage();
    switch (msg.getType()) {
    case Start:
      Message.Start start = (Message.Start)msg.getMessage();
      this.playerColor = start.getColor();
      break;
    case Board:
      Message.BoardUpdate boardupd = (Message.BoardUpdate)msg.getMessage();
      updateBoard(boardupd.getBoard());
      break;
    case Turn:
      Message.Turn turn = (Message.Turn)msg.getMessage();
      updateTurn(turn.getIsYours());
      break;
    case Invalid:
      Message.Invalid invalid = (Message.Invalid)msg.getMessage();
      showInvalidMove(invalid.getReason());
      break;
    default:
      break;
    }
  }

  public void updateBoard(Board board) {
    SwingUtilities.invokeLater(() -> {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          String txt;
          if (board.get(i, j) == Board.Status.Black)
            txt = "B";
          else if (board.get(i, j) == Board.Status.White)
            txt = "W";
          else
            txt = "";
          buttons[i][j].setText(txt);
        }
      }
    });
  }

  public void updateTurn(boolean isYours) {
    SwingUtilities.invokeLater(() -> {
      if (isYours) {
        statusLabel.setText("Your turn (" +
                            (playerColor == 'B' ? "Black" : "White") + ")");
      } else {
        statusLabel.setText("Opponent's turn");
      }
    });
  }

  public void showInvalidMove(String displayMessage) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(mainPanel, displayMessage, "Error",
                                    JOptionPane.ERROR_MESSAGE);
    });
  }
}
