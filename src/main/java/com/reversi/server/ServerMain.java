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
  public static final int PORT = 5000;

  // List of all connected clients
  private List<ClientHandler> allClients = new ArrayList<>();
  // Map room ID to LobbyRoom
  private Map<String, LobbyRoom> lobbyRooms = new HashMap<>();
  // Map room ID to active game session
  private Map<String, GameSession> activeGameSessions = new HashMap<>();
  private EventBus eventBus = new EventBus();

  public static void main(String[] args) { new ServerMain().startServer(); }

  public void startServer() {
    eventBus.register(ClientMessage.class, this);

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      logger.info("Server started on port {}", PORT);
      while (true) {
        Socket socket = serverSocket.accept();
        ClientHandler handler = new ClientHandler(socket, eventBus);
        synchronized (allClients) { allClients.add(handler); }
        handler.start();
        logger.info("Client connected. Total clients: {}", allClients.size());
      }
    } catch (IOException e) {
      logger.error("Error starting server", e);
    }
  }

  @Override
  public void onEvent(ClientMessage e) {
    Message msg = e.getMessage();
    ClientHandler handler = e.getHandler();

    switch (msg.getType()) {
    case LobbyJoin: {
      Message.LobbyJoin lobbyJoin = (Message.LobbyJoin)msg.getMessage();
      String roomId = lobbyJoin.getRoomNumber();
      LobbyRoom room;
      synchronized (lobbyRooms) {
        room = lobbyRooms.get(roomId);
        if (room == null) {
          room = new LobbyRoom(roomId);
          lobbyRooms.put(roomId, room);
        }
      }
      room.addClient(handler);
      logger.info("Client {} joined room {}", handler.getId(), roomId);
      break;
    }
    case LobbyReady: {
      // Mark the client as ready
      LobbyRoom clientRoom = null;
      for (LobbyRoom room : lobbyRooms.values()) {
        if (room.getClients().contains(handler)) {
          clientRoom = room;
          break;
        }
      }
      if (clientRoom != null) {
        clientRoom.markReady(handler);
        logger.info("Client {} is ready in room {}", handler.getId(),
                    clientRoom.getRoomId());
        if (clientRoom.isReadyToStart()) {
          List<ClientHandler> players = clientRoom.getClients();
          if (players.size() == 2) {
            GameSession gameSession =
                new GameSession(players.get(0), players.get(1));
            activeGameSessions.put(clientRoom.getRoomId(), gameSession);
            gameSession.startGame();
            logger.info("Game session started for room {}",
                        clientRoom.getRoomId());
            synchronized (lobbyRooms) {
              lobbyRooms.remove(clientRoom.getRoomId());
            }
          }
        }
      }
      break;
    }
    case Move: {
      // Route move messages to the appropriate game session.
      GameSession session = null;
      for (GameSession gs : activeGameSessions.values()) {
        if (gs.containsClient(handler)) {
          session = gs;
          break;
        }
      }
      if (session != null) {
        Message.Move move = (Message.Move)msg.getMessage();
        boolean valid = session.makeMove(move.getRow(), move.getCol(),
                                         handler.getPlayerColor());
        if (!valid) {
          handler.sendMessage(new Message(new Message.Invalid("Invalid move")));
        }
        session.updatePlayers();
      } else {
        logger.warn("Received move from client {} with no active game session",
                    handler.getId());
      }
      break;
    }
    default:
      logger.warn("Message ignored: {}", msg.toString());
      break;
    }
  }
}
