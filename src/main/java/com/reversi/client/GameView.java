package com.reversi.client;

import com.reversi.common.Board;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.logging.resources.logging;

public class GameView implements EventListener<ServerMessage> {
  private static final Logger logger = LoggerFactory.getLogger(GameView.class);

  private JFrame frame;
  private JButton[][] buttons = new JButton[8][8];
  private GameController controller;
  private JLabel statusLabel;
  private char playerColor;

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
      this.updateBoard(boardupd.getBoard());
      break;

    case Turn:
      Message.Turn turn = (Message.Turn)msg.getMessage();
      this.updateTurn(turn.getIsYours());
      break;

    case Invalid:
      Message.Invalid invalid = (Message.Invalid)msg.getMessage();
      this.showInvalidMove(invalid.getReason());
      break;

    default:
      logger.warn("Message ignored: {}", msg.toString());
      break;
    }
  }

  public void setController(GameController c) { controller = c; }

  public void setPlayerColor(char color) {
    playerColor = color;
    SwingUtilities.invokeLater(() -> {
      statusLabel.setText("You are " + ((color == 'B') ? "Black" : "White"));
    });
  }

  // Create and display the GUI.
  public void createAndShowGUI() {
    frame = new JFrame("Reversi Client");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 600);
    frame.setLayout(new BorderLayout());
    JPanel boardPanel = new JPanel(new GridLayout(8, 8));
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        JButton btn = new JButton("");
        int row = i, col = j;
        btn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // When a cell is clicked, send a move request.
            if (controller != null) {
              controller.sendMove(row, col);
            }
          }
        });
        buttons[i][j] = btn;
        boardPanel.add(btn);
      }
    }
    statusLabel = new JLabel("Connecting to server...");
    frame.add(statusLabel, BorderLayout.NORTH);
    frame.add(boardPanel, BorderLayout.CENTER);
    frame.setVisible(true);
  }

  public void updateBoard(Board board) {
    SwingUtilities.invokeLater(() -> {
      for (int i = 0; i < 8; i++)
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
    });
  }

  // Update the status label to show whose turn it is.
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
      JOptionPane.showMessageDialog(frame, displayMessage, "Error",
                                    JOptionPane.ERROR_MESSAGE);
    });
  }
}
