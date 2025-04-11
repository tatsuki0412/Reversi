package com.reversi.server;

import com.reversi.common.EventBus;
import com.reversi.common.EventListener;
import com.reversi.common.Message;
import com.reversi.common.Player;
import com.reversi.common.ReversiGame;
import com.reversi.server.events.GameSessionUpdate;
import java.io.*;
import java.net.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
  private static final Logger logger =
      LoggerFactory.getLogger(ServerMain.class);

  public static final int PORT = 5000;

  public static void main(String[] args) { new ServerMain().startServer(); }

  // List of all connected clients
  private Map<Integer, ClientHandler> clients = new HashMap<>();
  // Map room ID to LobbyRoom
  private Map<String, LobbyRoom> lobbyRooms = new HashMap<>();
  // Map room ID to active game session
  private Map<String, GameSession> activeGameSessions = new HashMap<>();

  private EventBus eventBus = new EventBus();
  private List<EventListener> listeners = new ArrayList<>();

  public void startServer() {
    // Create event listeners and add them to a list, to prevent gc
    var clientListener = this.new ClientMessageListener();
    var gameListener = this.new GameSessionUpdateListener();
    listeners.add(clientListener);
    listeners.add(gameListener);
    eventBus.register(ClientMessage.class, clientListener);
    eventBus.register(GameSessionUpdate.class, gameListener);

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      logger.info("Server started on port {}", PORT);
      while (true) {
        Socket socket = serverSocket.accept();
        int clientId = genClientId();
        ClientHandler handler = new ClientHandler(clientId, socket, eventBus);
        synchronized (clients) { clients.put(clientId, handler); }
        handler.start();
        logger.info("Client connected. Total clients: {}", clients.size());
      }
    } catch (IOException e) {
      logger.error("Error starting server", e);
    }
  }

  private static final Random rand = new Random();
  private int genClientId() {
    int id = 0;
    while (clients.containsKey(id))
      id = rand.nextInt();
    return id;
  }

  // ---------------------------------------------------------------
  // EventListener ClientMessageListener
  class ClientMessageListener implements EventListener<ClientMessage> {
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
        logger.info("Client {} joined room {}", handler.getID(), roomId);
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
          logger.info("Client {} is ready in room {}", handler.getID(),
                      clientRoom.getRoomId());
          if (clientRoom.isReadyToStart()) {
            List<ClientHandler> players = clientRoom.getClients();

            if (players.size() == 2) {
              ClientHandler blackPlayer = players.get(0);
              ClientHandler whitePlayer = players.get(1);

              GameSession gameSession =
                  new GameSession(blackPlayer, whitePlayer);
              activeGameSessions.put(clientRoom.getRoomId(), gameSession);

              // Notify players that game just started
              blackPlayer.sendMessage(new Message(new Message.Start('B')));
              whitePlayer.sendMessage(new Message(new Message.Start('W')));
              eventBus.post(new GameSessionUpdate(gameSession));

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
          boolean valid =
              session.makeMove(move.getRow(), move.getCol(), handler);
          if (!valid) {
            handler.sendMessage(
                new Message(new Message.Invalid("Invalid move")));
          }
          eventBus.post(new GameSessionUpdate(session));
        } else {
          logger.error(
              "Received move from client {} with no active game session.",
              handler.getID());
        }
        break;
      }

      default:
        logger.warn("Message ignored: {}", msg.toString());
        break;
      }
    }
  }

  // ---------------------------------------------------------------
  // EventListener GameSessionUpdateListener
  class GameSessionUpdateListener implements EventListener<GameSessionUpdate> {
    @Override
    public void onEvent(GameSessionUpdate e) {
      GameSession session = e.getSession();
      ReversiGame game = session.getGame();

      var boardUpd = new Message.BoardUpdate(game.getBoard());
      ClientHandler blackPlayer = clients.get(session.getBlackId());
      ClientHandler whitePlayer = clients.get(session.getWhiteId());

      blackPlayer.sendMessage(new Message(boardUpd));
      whitePlayer.sendMessage(new Message(boardUpd));

      // TODO: remove this later
      blackPlayer.sendMessage(new Message(
          new Message.Turn(game.getCurrentPlayer() == Player.Black)));
      whitePlayer.sendMessage(new Message(
          new Message.Turn(game.getCurrentPlayer() == Player.White)));
    }
  }
}
