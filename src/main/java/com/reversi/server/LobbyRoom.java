package com.reversi.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyRoom {
  private final String roomId;
  private final List<ClientHandler> clients;
  private final Map<ClientHandler, Boolean> readiness;

  public LobbyRoom(String roomId) {
    this.roomId = roomId;
    this.clients = new ArrayList<>();
    this.readiness = new HashMap<>();
  }

  public String getRoomId() { return roomId; }

  public synchronized void addClient(ClientHandler client) {
    if (!clients.contains(client) && clients.size() < 2) {
      clients.add(client);
      readiness.put(client, false);
    }
  }

  public synchronized void markReady(ClientHandler client) {
    if (readiness.containsKey(client)) {
      readiness.put(client, true);
    }
  }

  public synchronized boolean isReadyToStart() {
    return clients.size() == 2 && readiness.values().stream().allMatch(r -> r);
  }

  public synchronized List<ClientHandler> getClients() {
    return new ArrayList<>(clients);
  }

  public synchronized void removeClient(ClientHandler client) {
    clients.remove(client);
    readiness.remove(client);
  }
}
