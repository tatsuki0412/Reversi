package com.reversi.client;

import com.reversi.common.Board;
import com.reversi.common.Player;
import com.reversi.common.ReversiGame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameView {
  private static final Logger logger = LoggerFactory.getLogger(GameView.class);

  // ui
  private JPanel gamePanel;
  private JButton[][] buttons = new JButton[8][8];
  private JLabel statusLabel;

  // Reference to game model
  private ReversiGame game;
  private Player us;

  // Reference to the controller
  private ServerSocket controller;

  public GameView() {
    this.game = new ReversiGame();
    this.us = Player.None;
    initComponents();
  }

  private void initComponents() {
    gamePanel = new JPanel(new BorderLayout());
    JPanel boardPanel = new JPanel(new GridLayout(8, 8));
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        JButton btn = new JButton("");
        final int row = i, col = j;
        btn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (controller == null) {
              logger.error("Game controller not set.");
              return;
            }

            if (game.getCurrentPlayer() == us && game.isValidMove(row, col))
              controller.sendMove(row, col);
          }
        });
        buttons[i][j] = btn;
        boardPanel.add(btn);
      }
    }
    statusLabel = new JLabel("Game has not started.");
    gamePanel.add(statusLabel, BorderLayout.NORTH);
    gamePanel.add(boardPanel, BorderLayout.CENTER);
  }

  public JPanel getGamePanel() { return gamePanel; }

  public void setController(ServerSocket c) { controller = c; }

  public void setUs(Player p) {
    this.us = p;
    SwingUtilities.invokeLater(
        () -> { statusLabel.setText("You are " + this.us.toString()); });
  }

  public void update(ReversiGame game) {
    this.game = game;
    SwingUtilities.invokeLater(() -> {
      // update board
      Board board = game.getBoard();

      for (int i = 0; i < 8; i++)
        for (int j = 0; j < 8; j++) {
          buttons[i][j].setText(switch (board.get(i, j)) {
            case Black -> "B";
            case White -> "W";
            default ->
              game.getCurrentPlayer() == us &&board.isValidMove(i, j, us) ? "."
                                                                          : "";
          });
        }

      // update label
      boolean ourTurn = game.getCurrentPlayer() == us;
      if (ourTurn)
        statusLabel.setText("Your turn (" + us.toString() + ")");
      else
        statusLabel.setText("Opponent's turn");
    });
  }

  public void showInvalidMove(String displayMessage) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(gamePanel, displayMessage, "Error",
                                    JOptionPane.ERROR_MESSAGE);
    });
  }

  public void showGameOver(String message) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(gamePanel, message, "Game Over",
                                    JOptionPane.INFORMATION_MESSAGE);
      statusLabel.setText("Game Over: " + message);
    });
  }
}
