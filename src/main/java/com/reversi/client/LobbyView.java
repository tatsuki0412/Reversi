package com.reversi.client;

import com.reversi.common.Message;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LobbyView {
  private JPanel lobbyPanel;
  private JTextField roomField;
  private JButton joinButton;
  private JButton readyButton;
  private JLabel lobbyStatusLabel;
  private GameController controller;

  public LobbyView() {
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
        controller.send(new Message(new Message.LobbyReady(true)));
        lobbyStatusLabel.setText("Ready. Waiting for opponent...");
        readyButton.setEnabled(false);
      }
    });
  }

  public void setController(GameController controller) {
    this.controller = controller;
  }

  public JPanel getMainPanel() { return lobbyPanel; }
}
