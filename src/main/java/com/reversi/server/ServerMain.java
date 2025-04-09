package com.reversi.server;

import com.reversi.common.EventBus;
import com.reversi.common.EventListener;
import java.io.*;
import java.net.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain implements EventListener<ClientMessage> {
  private static final Logger logger =
      LoggerFactory.getLogger(ServerMain.class);

  public static final int PORT = 5000;
  private List<ClientHandler> clients = new ArrayList<>();
  private EventBus eventBus = new EventBus();
  private GameSession gameSession;

  public static void main(String[] args) { new ServerMain().startServer(); }

  public void startServer() {
    // Setup event listener
    eventBus.register(ClientMessage.class, this);

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      logger.info("Server started on port {}", PORT);

      // Accept exactly two client connections for a single game session.
      while (clients.size() < 2) {
        Socket socket = serverSocket.accept();
        ClientHandler handler = new ClientHandler(socket, eventBus);
        clients.add(handler);
        handler.start();
        logger.info("Client connected. Total clients: {}", clients.size());
      }
      // Once two clients are connected, start the game session.
      gameSession = new GameSession(clients.get(0), clients.get(1));
      gameSession.startGame();
    } catch (IOException e) {
      logger.error("Error starting server", e);
    }
  }

  public GameSession getGameSession() { return gameSession; }

  @Override
  public void onEvent(ClientMessage e) {
    String line = e.getMessage();
    ClientHandler handler = e.getHandler();

    if (line.startsWith("MOVE:")) {
      String[] parts = line.substring(5).split(",");
      int row = Integer.parseInt(parts[0].trim());
      int col = Integer.parseInt(parts[1].trim());

      boolean valid = gameSession.makeMove(row, col, handler.getPlayerColor());
      if (!valid)
        handler.sendMessage("INVALID");

      // Update both clients with the new board state.
      gameSession.updatePlayers();
    }
    // Additional commands...
  }
}
