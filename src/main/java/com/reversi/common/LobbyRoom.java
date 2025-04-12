package com.reversi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@JsonSerialize(using = LobbyRoom.LobbyRoomSerializer.class)
@JsonDeserialize(using = LobbyRoom.LobbyRoomDeserializer.class)
public class LobbyRoom {
  private final String roomName;
  // Map that associates player IDs with their status (role and readiness)
  private final Map<Integer, PlayerStatus> playerStatus;

  public LobbyRoom(String roomName) { this(roomName, null); }
  public LobbyRoom(String roomName, Map<Integer, PlayerStatus> playerStatus) {
    this.roomName = roomName;
    this.playerStatus =
        playerStatus != null ? new HashMap<>(playerStatus) : new HashMap<>();
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
      status.setReadiness(false);
    }
  }

  /**
   * Updates a player's status by accepting a new PlayerStatus object.
   * If the new status differs in either readiness or role from the current
   * status, the player's status is updated, and readiness for all players is
   * canceled.
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

    // Ensure that both required roles (Black and White) are present.
    boolean hasBothRoles =
        playerStatus.values()
            .stream()
            .map(PlayerStatus::getRole)
            .collect(Collectors.toSet())
            .containsAll(EnumSet.of(Player.Black, Player.White));

    return hasBothRoles;
  }

  /**
   * Expose a copy of the players' statuses.
   */
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

  // ============================================================
  // Serialization support
  public static class LobbyRoomSerializer extends JsonSerializer<LobbyRoom> {
    @Override
    public void serialize(LobbyRoom room, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      // Write out the room name.
      gen.writeStringField("roomName", room.getRoomName());
      // Write out the player statuses map.
      gen.writeObjectField("playerStatus", room.getPlayers());
      gen.writeEndObject();
    }
  }

  public static class LobbyRoomDeserializer
      extends JsonDeserializer<LobbyRoom> {
    @Override
    public LobbyRoom deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      String roomName = null;
      Map<Integer, PlayerStatus> playerStatus = null;

      if (p.currentToken() == null) {
        p.nextToken();
      }

      if (p.currentToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected START_OBJECT token, but got: " +
                              p.currentToken());
      }

      // Process the fields within the object.
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.getCurrentName();
        p.nextToken(); // move to the value for this field
        if ("roomName".equals(fieldName)) {
          roomName = p.getText();
        } else if ("playerStatus".equals(fieldName)) {
          // Deserialize the playerStatus map.
          playerStatus = ctxt.readValue(
              p, ctxt.getTypeFactory().constructMapType(
                     Map.class, Integer.class, PlayerStatus.class));
        } else {
          // Skip any unexpected fields.
          p.skipChildren();
        }
      }
      return new LobbyRoom(roomName, playerStatus);
    }
  }
}
