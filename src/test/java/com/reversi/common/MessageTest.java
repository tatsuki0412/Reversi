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
  void testSerializeDeserializeTurn() {
    Message.Turn turn = new Message.Turn(false);
    Message msg = new Message(turn);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.Turn, deserialized.getType(),
                 "Message type should be Turn");

    Message.Turn turnDeserialized = (Message.Turn)deserialized.getMessage();
    assertFalse(turnDeserialized.getIsYours(), "Turn flag does not match");
  }

  @Test
  void testSerializeDeserializeBoard() {
    Message.BoardUpdate board = new Message.BoardUpdate(Board.createDefault());
    Message msg = new Message(board);

    String json = assertDoesNotThrow(() -> serialize(msg));
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> deserialize(json));
    assertEquals(Message.Type.Board, deserialized.getType(),
                 "Message type should be Board");

    Message.BoardUpdate boardDeserialized =
        (Message.BoardUpdate)deserialized.getMessage();
    assertEquals(Board.createDefault(), boardDeserialized.getBoard(),
                 "Board does not match");
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
}
