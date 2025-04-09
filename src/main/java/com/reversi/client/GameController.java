package com.reversi.client;

import java.io.*;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameController {
  private static final Logger logger =
      LoggerFactory.getLogger(GameController.class);

  private GameView view;
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private String playerColor; // "B" or "W"

  public GameController(GameView view) { this.view = view; }

  // Connect to the server (assumed to be running on localhost:5000)
  public void connectToServer() {
    try {
      socket = new Socket("localhost", 5000);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // Listen for messages from the server on a separate thread.
      new Thread(() -> listenToServer()).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Continuously listen for server messages and update the view accordingly.
  private void listenToServer() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        logger.info("Received: {}", line);

        if (line.startsWith("START:")) {
          playerColor = line.substring(6).trim();
          view.setPlayerColor(playerColor);
        } else if (line.startsWith("BOARD:")) {
          // Extract the board string (8 lines separated by newlines).
          String boardStr = line.substring(6);
          view.updateBoard(boardStr);
        } else if (line.startsWith("TURN:")) {
          String turnInfo = line.substring(5).trim();
          view.updateTurn(turnInfo);
        } else if (line.startsWith("INVALID")) {
          view.showInvalidMove();
        }
      }
    } catch (IOException e) {
      logger.error("Error listening to server", e);
      e.printStackTrace();
    }
  }

  // Called by the view when a cell is clicked.
  public void sendMove(int row, int col) {
    if (out != null) {
      out.println("MOVE:" + row + "," + col);
    }
  }
}
