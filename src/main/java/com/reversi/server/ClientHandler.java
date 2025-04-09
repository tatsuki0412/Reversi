package com.reversi.server;

import com.reversi.common.EventBus;
import java.io.*;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends Thread {
  private static final Logger logger =
      LoggerFactory.getLogger(ClientHandler.class);

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private char playerColor; // 'B' or 'W'
  private EventBus eventBus;

  public ClientHandler(Socket s, EventBus eventBus) {
    this.socket = s;
    this.eventBus = eventBus;

    // Establish in/out stream with client
    try {
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      logger.error("Error initializing streams for player {}", playerColor, e);
    }
  }

  public void setPlayerColor(char color) { playerColor = color; }

  public char getPlayerColor() { return playerColor; }

  public void sendMessage(String msg) { out.println(msg); }

  public void run() {
    try {
      String line;
      // Listen for incoming messages from the client.
      while ((line = in.readLine()) != null) {
        logger.info("Received from player {}: {}", playerColor, line);
        eventBus.post(new ClientMessage(line, this));
      }
    } catch (IOException e) {
      logger.error("Connection with player {} lost.", playerColor, e);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        logger.error("Error closing socket for player {}", playerColor, e);
      }
    }
  }
}
