package com.reversi.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyRoom {
  private final String roomName;

  private final List<Integer> players;
  private final Map<Integer, Boolean> isReady;
  private final Map<Integer, Player> role;

  public LobbyRoom(String roomName) {
    this.roomName = roomName;
    this.players = new ArrayList<>();
    this.isReady = new HashMap<>();
    this.role = new HashMap<>();
  }

  public String getRoomName() { return roomName; }

  public boolean isEmpty() { return players.isEmpty(); }

  public int size() { return players.size(); }

  public synchronized boolean addPlayer(Integer id) {
    if (players.size() >= 2 || players.contains(id))
      return false;
    players.add(id);
    isReady.put(id, false);
    role.put(id, role.isEmpty() ? Player.Black : Player.White);
    return true;
  }

  private synchronized void cancelReady() {
    for (var player : isReady.keySet())
      isReady.replace(player, false);
  }

  public synchronized boolean setReadiness(Integer id, boolean readiness) {
    if (!players.contains(id))
      return false;
    if (isReady.get(id) == readiness)
      return false;

    isReady.replace(id, readiness);
    if (readiness == false)
      cancelReady();
    return true;
  }

  public synchronized boolean setRole(Integer id, Player newRole) {
    if (!players.contains(id) || newRole == Player.None)
      return false;
    if (role.get(id) == newRole)
      return false;
    role.replace(id, newRole);
    cancelReady();
    return true;
  }

  public synchronized boolean isReadyToStart() {
    return players.size() == 2 && isReady.values().stream().allMatch(r -> r) &&
        role.values().containsAll(EnumSet.of(Player.Black, Player.White));
  }

  public synchronized List<Integer> getPlayers() {
    return new ArrayList<>(players);
  }

  public synchronized boolean removePlayer(Integer id) {
    if (!players.contains(id))
      return false;
    players.remove(id);
    isReady.remove(id);
    role.remove(id);
    cancelReady();
    return true;
  }
}
