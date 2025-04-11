package com.reversi.client;

import com.reversi.common.Board;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import com.reversi.common.Player;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameView {
  private static final Logger logger = LoggerFactory.getLogger(GameView.class);

  private JPanel mainPanel;
  private JButton[][] buttons = new JButton[8][8];
  private GameController controller;
  private JLabel statusLabel;
  private Player player;

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

  public void setPlayer(Player p) {
    this.player = p;
    SwingUtilities.invokeLater(
        () -> { statusLabel.setText("You are " + this.player.toString()); });
  }

  public void updateBoard(Board board) {
    SwingUtilities.invokeLater(() -> {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          String txt;
          if (board.get(i, j) == Player.Black)
            txt = "B";
          else if (board.get(i, j) == Player.White)
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
      if (isYours)
        statusLabel.setText("Your turn (" + player.toString() + ")");
      else
        statusLabel.setText("Opponent's turn");
    });
  }

  public void showInvalidMove(String displayMessage) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(mainPanel, displayMessage, "Error",
                                    JOptionPane.ERROR_MESSAGE);
    });
  }
}
