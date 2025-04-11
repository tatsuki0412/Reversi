package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class MessageTest {
  static String serialize(Message m) throws IOException {
    var mapper = JacksonObjMapper.get();
    return mapper.writeValueAsString(m);
  }
  static Message deserialize(String s) throws IOException {
    var mapper = JacksonObjMapper.get();
    return mapper.readValue(s, Message.class);
  }

  @Test
  void testSerializeDeserializeMove() {
    Message.Move move = new Message.Move(3, 2);
    Message msg = new Message(move);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.Move, deserialized.getType(),
                 "Message type should be Move");

    Message.Move moveDeserialized = (Message.Move)deserialized.getMessage();
    assertEquals(3, moveDeserialized.getRow(), "Row value not matching");
    assertEquals(2, moveDeserialized.getCol(), "Column value not matching");
  }

  @Test
  void testSerializeDeserializeInvalid() {
    Message.Invalid invalid = new Message.Invalid("Invalid operation");
    Message msg = new Message(invalid);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.Invalid, deserialized.getType(),
                 "Message type should be Invalid");

    Message.Invalid invalidDeserialized =
        (Message.Invalid)deserialized.getMessage();
    assertEquals("Invalid operation", invalidDeserialized.getReason(),
                 "Invalid reason does not match");
  }

  @Test
  void testSerializeDeserializeStart() {
    Message.Start start = new Message.Start('B');
    Message msg = new Message(start);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.Start, deserialized.getType(),
                 "Message type should be Start");

    Message.Start startDeserialized = (Message.Start)deserialized.getMessage();
    assertEquals('B', startDeserialized.getColor(),
                 "Starting color does not match");
  }

  @Test
  void testSerializeDeserializeLobbyJoin() {
    Message.LobbyJoin lobbyJoin = new Message.LobbyJoin("1234");
    Message msg = new Message(lobbyJoin);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON for LobbyJoin should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.LobbyJoin, deserialized.getType(),
                 "Message type should be LobbyJoin");

    Message.LobbyJoin lobbyJoinDeserialized =
        (Message.LobbyJoin)deserialized.getMessage();
    assertEquals("1234", lobbyJoinDeserialized.getRoomNumber(),
                 "Room number does not match");
  }

  @Test
  void testSerializeDeserializeLobbyReady() {
    Message.LobbyReady lobbyReady = new Message.LobbyReady(true);
    Message msg = new Message(lobbyReady);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON for LobbyReady should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.LobbyReady, deserialized.getType(),
                 "Message type should be LobbyReady");

    // For LobbyReady there is no field to assert; testing successful
    // deserialization is sufficient.
    Message.LobbyReady lobbyReadyDeserialized =
        (Message.LobbyReady)deserialized.getMessage();
    assertEquals(true, lobbyReadyDeserialized.getIsReady(),
                 "Ready status does not match");
  }

  @Test
  void testSerializeDeserializeGameUpdate() {
    var game = new ReversiGame();

    Message.GameUpdate gameUpdate = new Message.GameUpdate(game);
    var msg = new Message(gameUpdate);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON for LobbyReady should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.GameUpdate, deserialized.getType(),
                 "Message type should be GameUpdate");

    Message.GameUpdate deserializedMessage =
        (Message.GameUpdate)deserialized.getMessage();
    assertTrue(game.equals(deserializedMessage.getGame()));
  }
}
