package com.reversi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;

class MessageJsonUtil {
  private static final ObjectMapper objectMapper = JacksonObjMapper.get();

  public static String serialize(Message message) throws Exception {
    // Create a JSON object that has both "type" and "msg" properties.
    // Using Jackson’s default POJO-to-JSON, we can set up an intermediary
    // container. One common way is to create a little anonymous POJO as a
    // wrapper:
    Wrapper wrapper = new Wrapper();
    wrapper.type = message.getType();
    wrapper.msg = message.getMessage();
    return objectMapper.writeValueAsString(wrapper);
  }

  public static Message deserialize(String json) throws Exception {
    return objectMapper.readValue(json, Message.class);
  }

  // Simple helper inner class for serialization.
  public static class Wrapper {
    public Message.Type type;
    public Object msg;
  }
}

class MessageDeserializer extends JsonDeserializer<Message> {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Message deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    String typeStr = node.get("type").asText();
    Message.Type type = Message.Type.valueOf(typeStr);
    JsonNode msgNode = node.get("msg");

    switch (type) {
    case Move:
      Message.Move move = mapper.treeToValue(msgNode, Message.Move.class);
      return new Message(move);

    case Invalid:
      Message.Invalid invalid =
          mapper.treeToValue(msgNode, Message.Invalid.class);
      return new Message(invalid);

    case Turn:
      Message.Turn turn = mapper.treeToValue(msgNode, Message.Turn.class);
      return new Message(turn);

    case Start:
      Message.Start start = mapper.treeToValue(msgNode, Message.Start.class);
      return new Message(start);

    case Board:
      Message.BoardUpdate board =
          mapper.treeToValue(msgNode, Message.BoardUpdate.class);
      return new Message(board);

    case LobbyJoin:
      Message.LobbyJoin lobbyJoin =
          mapper.treeToValue(msgNode, Message.LobbyJoin.class);
      return new Message(lobbyJoin);

    case LobbyReady:
      Message.LobbyReady lobbyReady =
          mapper.treeToValue(msgNode, Message.LobbyReady.class);
      return new Message(lobbyReady);

    default:
      throw new IllegalStateException("Unexpected type: " + typeStr);
    }
  }
}

@JsonDeserialize(using = MessageDeserializer.class)
public class Message {
  // Nested message types for Client -> Server
  public static class LobbyJoin {
    private final String roomNumber;
    @JsonCreator
    public LobbyJoin(@JsonProperty("roomNumber") String roomNumber) {
      this.roomNumber = roomNumber;
    }
    public String getRoomNumber() { return roomNumber; }
  }

  public static class LobbyReady {
    private final boolean isReady;
    @JsonCreator
    public LobbyReady(@JsonProperty("isReady") boolean isReady) {
      this.isReady = isReady;
    }
    public boolean getIsReady() { return isReady; }
  }

  public static class Move {
    private final int row, col;
    @JsonCreator
    public Move(@JsonProperty("row") int row, @JsonProperty("col") int col) {
      this.row = row;
      this.col = col;
    }
    public int getRow() { return row; }
    public int getCol() { return col; }
  }

  // Nested message types for Server -> Client
  public static class Invalid {
    private final String reason;
    @JsonCreator
    public Invalid(@JsonProperty("reason") String reason) {
      this.reason = reason;
    }
    public String getReason() { return reason; }
  }

  public static class BoardUpdate {
    private final Board board;
    @JsonCreator
    public BoardUpdate(@JsonProperty("board") Board board) {
      this.board = board;
    }
    public Board getBoard() { return board; }
  }

  public static class Start {
    private final char color;
    @JsonCreator
    public Start(@JsonProperty("color") char color) {
      this.color = color;
    }
    public char getColor() { return color; }
  }

  public static class Turn {
    private final boolean isYours;
    @JsonCreator
    public Turn(@JsonProperty("isYours") boolean isYours) {
      this.isYours = isYours;
    }
    public boolean getIsYours() { return isYours; }
  }

  // Tagged union storage
  private final Object msg;
  private final Type type;
  public enum Type { Move, Invalid, Start, Turn, Board, LobbyJoin, LobbyReady }

  // Constructors for different message types.
  public Message(Move msg) {
    this.msg = msg;
    this.type = Type.Move;
  }
  public Message(Invalid msg) {
    this.msg = msg;
    this.type = Type.Invalid;
  }
  public Message(Start msg) {
    this.msg = msg;
    this.type = Type.Start;
  }
  public Message(Turn msg) {
    this.msg = msg;
    this.type = Type.Turn;
  }
  public Message(BoardUpdate msg) {
    this.msg = msg;
    this.type = Type.Board;
  }
  public Message(LobbyJoin msg) {
    this.msg = msg;
    this.type = Type.LobbyJoin;
  }
  public Message(LobbyReady msg) {
    this.msg = msg;
    this.type = Type.LobbyReady;
  }

  // No-arg constructor for Jackson
  protected Message() {
    this.msg = null;
    this.type = null;
  }

  public Type getType() { return type; }
  public Object getMessage() { return msg; }

  // JSON Serialization helper methods.
  public String serialize() throws Exception {
    return MessageJsonUtil.serialize(this);
  }
  public static Message deserialize(String json) throws Exception {
    return MessageJsonUtil.deserialize(json);
  }
}
