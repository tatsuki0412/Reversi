package com.reversi.client;

import com.reversi.common.LobbyRoom;
import com.reversi.common.Message;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;

public class LobbyView {
  private JPanel lobbyPanel;
  private JTextField roomNameField;
  private JButton joinButton;
  private JButton createButton;
  private JLabel lobbyStatusLabel;
  private GameController controller;
  private JPanel playersPanel;

  public LobbyView() {
    // Build Lobby Panel
    lobbyPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);

    playersPanel = new JPanel();
    playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(playersPanel);
    scrollPane.setPreferredSize(new Dimension(300, 400));
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    lobbyPanel.add(scrollPane, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    lobbyPanel.add(new JLabel("Enter Room Number:"), gbc);

    roomNameField = new JTextField(10);
    gbc.gridx = 2;
    lobbyPanel.add(roomNameField, gbc);

    joinButton = new JButton("Join Room");
    gbc.gridx = 1;
    gbc.gridy = 2;
    lobbyPanel.add(joinButton, gbc);

    createButton = new JButton("Create");
    gbc.gridx = 2;
    lobbyPanel.add(createButton, gbc);

    lobbyStatusLabel = new JLabel("Not connected to any room");
    gbc.gridx = 1;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    lobbyPanel.add(lobbyStatusLabel, gbc);

    // Button actions
    joinButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String roomId = roomNameField.getText().trim();
        if (!roomId.isEmpty()) {
          joinRoom(roomId);
        } else {
          lobbyStatusLabel.setText("Please enter a valid room ID");
        }
      }
    });

    createButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        var message = new Message.LobbyCreate(new LobbyRoom(
            roomNameField.getText().trim())); // TODO: add room config support
        controller.send(new Message(message));
        lobbyStatusLabel.setText("Waiting for opponent...");
      }
    });
  }

  private void joinRoom(String roomId) {
    controller.send(new Message(new Message.LobbyJoin(roomId)));
    lobbyStatusLabel.setText("Joining room: " + roomId + " ...");
  }

  public void setController(GameController controller) {
    this.controller = controller;
  }

  public JPanel getMainPanel() { return lobbyPanel; }

  public void showError(String what) {
    SwingUtilities.invokeLater(() -> lobbyStatusLabel.setText(what));
  }

  public void update(Map<String, LobbyRoom> lobbyRooms) {
    SwingUtilities.invokeLater(() -> {
      playersPanel.removeAll();
      for (var entry : lobbyRooms.keySet()) {
        JLabel lbl = new JLabel(entry);
        lbl.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) { joinRoom(lbl.getText()); }
        });
        playersPanel.add(lbl);
      }

      playersPanel.revalidate();
      playersPanel.repaint();
    });
  }
}
