package com.reversi.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LobbyRoom {
  private final String roomName;
  // Map that associates player IDs with their status (role and readiness)
  private final Map<Integer, PlayerStatus> playerStatus;

  public LobbyRoom(String roomName) {
    this.roomName = roomName;
    this.playerStatus = new HashMap<>();
  }

  public String getRoomName() { return roomName; }

  public boolean isEmpty() { return playerStatus.isEmpty(); }

  public int size() { return playerStatus.size(); }

  public boolean contains(Integer id) { return playerStatus.containsKey(id); }

  public synchronized boolean addPlayer(PlayerStatus status) {
    Integer id = status.getId();
    if (size() >= 2 || contains(id))
      return false;

    // Assign the default role: first player gets Black, second gets White.
    Player defaultRole = playerStatus.isEmpty() ? Player.Black : Player.White;
    status.setRole(defaultRole);
    playerStatus.put(id, status);
    return true;
  }

  /**
   * Resets the readiness for all players to false.
   */
  private synchronized void cancelReadiness() {
    for (PlayerStatus status : playerStatus.values()) {
      status.setReady(false);
    }
  }

  /**
   * Updates a player's status by accepting a new PlayerStatus object.
   * If the new status differs in either readiness or role from the current
   * status, the player's status is updated, and readiness for all players is
   * canceled. Returns true if the update changed the player’s status; false
   * otherwise.
   *
   * @param id the player's id
   * @param newStatus the new PlayerStatus to apply for the player
   * @return boolean indicating whether an update occurred
   */
  public synchronized boolean updatePlayerStatus(Integer id,
                                                 PlayerStatus newStatus) {
    if (!contains(id) || newStatus == null)
      return false;
    if (playerStatus.get(id).equals(newStatus)) // no change
      return false;

    playerStatus.put(id, newStatus);
    cancelReadiness();
    return true;
  }

  public synchronized boolean isReadyToStart() {
    if (size() != 2)
      return false;

    // Check if both players are ready.
    boolean allReady =
        playerStatus.values().stream().allMatch(PlayerStatus::isReady);

    // Ensure that both required roles (Black and White) are present.
    boolean hasBothRoles =
        playerStatus.values()
            .stream()
            .map(PlayerStatus::getRole)
            .collect(Collectors.toSet())
            .containsAll(EnumSet.of(Player.Black, Player.White));

    return allReady && hasBothRoles;
  }

  public synchronized Map<Integer, PlayerStatus> getPlayers() {
    return new HashMap<>(playerStatus);
  }

  public synchronized PlayerStatus getPlayerStatus(Integer id) {
    return playerStatus.get(id);
  }

  public synchronized boolean removePlayer(Integer id) {
    if (!contains(id))
      return false;
    playerStatus.remove(id);
    cancelReadiness();
    return true;
  }
}
