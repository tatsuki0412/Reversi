package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import com.reversi.common.Message;
import org.junit.jupiter.api.Test;

public class MessageTest {
  @Test
  void testSerializeDeserializeMove() {
    Message.Move move = new Message.Move(3, 2);
    Message msg = new Message(move);

    String json = assertDoesNotThrow(() -> msg.serialize());
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> Message.deserialize(json));
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

    String json = assertDoesNotThrow(() -> msg.serialize());
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> Message.deserialize(json));
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

    String json = assertDoesNotThrow(() -> msg.serialize());
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> Message.deserialize(json));
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

    String json = assertDoesNotThrow(() -> msg.serialize());
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> Message.deserialize(json));
    assertEquals(Message.Type.Turn, deserialized.getType(),
                 "Message type should be Turn");

    Message.Turn turnDeserialized = (Message.Turn)deserialized.getMessage();
    assertFalse(turnDeserialized.getIsYours(), "Turn flag does not match");
  }

  @Test
  void testSerializeDeserializeBoard() {
    Message.Board board = new Message.Board("Board layout string");
    Message msg = new Message(board);

    String json = assertDoesNotThrow(() -> msg.serialize());
    assertNotNull(json, "Serialized JSON should not be null");

    Message deserialized = assertDoesNotThrow(() -> Message.deserialize(json));
    assertEquals(Message.Type.Board, deserialized.getType(),
                 "Message type should be Board");

    Message.Board boardDeserialized = (Message.Board)deserialized.getMessage();
    assertEquals("Board layout string", boardDeserialized.getStr(),
                 "Board string does not match");
  }
}
