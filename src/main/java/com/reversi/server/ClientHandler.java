package com.reversi.server;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
  private Socket socket;
  private ServerMain server;
  private PrintWriter out;
  private BufferedReader in;
  private char playerColor; // 'B' or 'W'

  public ClientHandler(Socket s, ServerMain server) {
    this.socket = s;
    this.server = server;
    try {
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
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
        System.out.println("Received from player " + playerColor + ": " + line);
        // Process a move command sent as "MOVE:row,col"
        if (line.startsWith("MOVE:")) {
          String[] parts = line.substring(5).split(",");
          int row = Integer.parseInt(parts[0].trim());
          int col = Integer.parseInt(parts[1].trim());
          GameSession session = server.getGameSession();
          boolean valid = session.makeMove(row, col, playerColor);
          if (!valid) {
            sendMessage("INVALID");
          }
          // Update both clients with the new board state.
          session.updatePlayers();
        }
        // Additional commands (e.g., chat, disconnect) can be added here.
      }
    } catch (IOException e) {
      System.out.println("Connection with player " + playerColor + " lost.");
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
      }
    }
  }
}
