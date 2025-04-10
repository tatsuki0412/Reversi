package com.reversi.server;

import com.reversi.common.EventBus;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain implements EventListener<ClientMessage> {
  private static final Logger logger =
      LoggerFactory.getLogger(ServerMain.class);

  public static void main(String[] args) { new ServerMain().startServer(); }

  public static final int PORT = 5000;
  private List<ClientHandler> clients = new ArrayList<>();
  private EventBus eventBus = new EventBus();
  private GameSession gameSession;

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
    Message msg = e.getMessage();
    ClientHandler handler = e.getHandler();

    switch (msg.getType()) {
    case Move:
      Message.Move move = (Message.Move)msg.getMessage();
      boolean valid = gameSession.makeMove(move.getRow(), move.getCol(),
                                           handler.getPlayerColor());
      if (!valid)
        // handler.sendMessage("INVALID");
        handler.sendMessage(new Message(new Message.Invalid("Invalid move")));
      gameSession.updatePlayers();
      break;

    default:
      logger.warn("Message ignored: {}", msg.toString());
      break;
    }
  }
}
