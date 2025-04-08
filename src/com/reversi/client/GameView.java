package com.reversi.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameView {
    private JFrame frame;
    private JButton[][] buttons = new JButton[8][8];
    private GameController controller;
    private JLabel statusLabel;
    private String playerColor;

    public void setController(GameController c){
        controller = c;
    }

    public void setPlayerColor(String color) {
        playerColor = color;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("You are " + (color.equals("B") ? "Black" : "White"));
        });
    }

    // Create and display the GUI.
    public void createAndShowGUI(){
        frame = new JFrame("Reversi Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,600);
        frame.setLayout(new BorderLayout());
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                JButton btn = new JButton("");
                int row = i, col = j;
                btn.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
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

    // Update the board using the board string sent by the server.
    // The string is expected to be 8 lines (rows) separated by newlines,
    // with each line having 8 characters ('.', 'B', 'W').
    public void updateBoard(String boardStr){
        SwingUtilities.invokeLater(() -> {
            String[] lines = boardStr.split("\n");
            for (int i = 0; i < 8; i++){
                String rowLine = lines[i];
                for (int j = 0; j < 8; j++){
                    char c = rowLine.charAt(j);
                    buttons[i][j].setText((c == '.') ? "" : String.valueOf(c));
                }
            }
        });
    }

    // Update the status label to show whose turn it is.
    public void updateTurn(String turnInfo){
        SwingUtilities.invokeLater(() -> {
            if (turnInfo.equals("YOUR")) {
                statusLabel.setText("Your turn (" + (playerColor.equals("B") ? "Black" : "White") + ")");
            } else {
                statusLabel.setText("Opponent's turn");
            }
        });
    }

    public void showInvalidMove(){
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, "Invalid move!", "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
