package com.reversi.client;

import com.reversi.common.Message;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LobbyView {
  private JFrame frame;
  private JPanel mainPanel;
  private CardLayout cardLayout;
  private JPanel lobbyPanel;
  private JPanel gamePanel;
  private JTextField roomField;
  private JButton joinButton;
  private JButton readyButton;
  private JLabel lobbyStatusLabel;
  private GameView gameView;
  private GameController controller;

  public LobbyView() {
    frame = new JFrame("Reversi Lobby");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 600);
    cardLayout = new CardLayout();
    mainPanel = new JPanel(cardLayout);

    // Build Lobby Panel
    lobbyPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.gridx = 0;
    gbc.gridy = 0;
    lobbyPanel.add(new JLabel("Enter Room Number:"), gbc);

    roomField = new JTextField(10);
    gbc.gridx = 1;
    lobbyPanel.add(roomField, gbc);

    joinButton = new JButton("Join Room");
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    lobbyPanel.add(joinButton, gbc);

    readyButton = new JButton("Ready");
    readyButton.setEnabled(false);
    gbc.gridy = 2;
    lobbyPanel.add(readyButton, gbc);

    lobbyStatusLabel = new JLabel("Not connected to any room");
    gbc.gridy = 3;
    lobbyPanel.add(lobbyStatusLabel, gbc);

    // Build Game Panel using existing GameView
    gameView = new GameView();
    gamePanel = gameView.getMainPanel();

    mainPanel.add(lobbyPanel, "lobby");
    mainPanel.add(gamePanel, "game");

    frame.add(mainPanel);
    frame.setVisible(true);

    // Button actions
    joinButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String roomNumber = roomField.getText().trim();
        if (!roomNumber.isEmpty()) {
          controller.send(new Message(new Message.LobbyJoin(roomNumber)));
          lobbyStatusLabel.setText("Joined room: " + roomNumber);
          readyButton.setEnabled(true);
        } else {
          lobbyStatusLabel.setText("Please enter a valid room number");
        }
      }
    });

    readyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        controller.send(new Message(new Message.LobbyReady()));
        lobbyStatusLabel.setText("Ready. Waiting for opponent...");
        readyButton.setEnabled(false);
      }
    });
  }

  // Called by the client when the server starts the game session.
  public void switchToGameView() { cardLayout.show(mainPanel, "game"); }

  public void setController(GameController controller) {
    this.controller = controller;
  }

  public GameView getGameView() { return gameView; }

  // Optionally expose the main panel if needed
  public JPanel getMainPanel() { return mainPanel; }
}
