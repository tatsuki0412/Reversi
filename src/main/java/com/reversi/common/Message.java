package com.reversi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;

@JsonSerialize(using = Message.Serializer.class)
@JsonDeserialize(using = Message.Deserializer.class)
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

  public static class Start {
    private final char color;
    @JsonCreator
    public Start(@JsonProperty("color") char color) {
      this.color = color;
    }
    public char getColor() { return color; }
  }

  public static class GameUpdate {
    private final ReversiGame game;
    @JsonCreator
    public GameUpdate(@JsonProperty("game") ReversiGame game) {
      this.game = game;
    }
    public ReversiGame getGame() { return game; }
  }

  // Tagged union storage
  private final Object msg;
  private final Type type;
  public enum Type {
    Move,
    Invalid,
    Start,
    Turn,
    Board,
    LobbyJoin,
    LobbyReady,
    GameUpdate
  }

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
  public Message(LobbyJoin msg) {
    this.msg = msg;
    this.type = Type.LobbyJoin;
  }
  public Message(LobbyReady msg) {
    this.msg = msg;
    this.type = Type.LobbyReady;
  }
  public Message(GameUpdate msg) {
    this.msg = msg;
    this.type = Type.GameUpdate;
  }

  // No-arg constructor for Jackson
  protected Message() {
    this.msg = null;
    this.type = null;
  }

  public Type getType() { return type; }
  public Object getMessage() { return msg; }

  // ===================== Serialization Support =====================
  static class Serializer extends JsonSerializer<Message> {
    @Override
    public void serialize(Message m, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      // Begin writing the JSON object
      gen.writeStartObject();

      // Write the "type" field using the enum name.
      gen.writeStringField("type", m.getType().name());

      // Write the "body" field.
      // Here we indicate to the generator that the "body" field will contain
      // a nested JSON object. Jackson will use the default serializer for this.
      gen.writeFieldName("body");
      serializers.defaultSerializeValue(m.getMessage(), gen);

      // Complete the JSON object.
      gen.writeEndObject();
    }
  }

  static class Deserializer extends JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      ObjectMapper mapper = JacksonObjMapper.get();

      JsonNode node = p.getCodec().readTree(p);
      String typeStr = node.get("type").asText();
      Message.Type type = Message.Type.valueOf(typeStr);
      JsonNode msgNode = node.get("body");

      switch (type) {
      case Move:
        Message.Move move = mapper.treeToValue(msgNode, Message.Move.class);
        return new Message(move);

      case Invalid:
        Message.Invalid invalid =
            mapper.treeToValue(msgNode, Message.Invalid.class);
        return new Message(invalid);

      case Start:
        Message.Start start = mapper.treeToValue(msgNode, Message.Start.class);
        return new Message(start);

      case LobbyJoin:
        Message.LobbyJoin lobbyJoin =
            mapper.treeToValue(msgNode, Message.LobbyJoin.class);
        return new Message(lobbyJoin);

      case LobbyReady:
        Message.LobbyReady lobbyReady =
            mapper.treeToValue(msgNode, Message.LobbyReady.class);
        return new Message(lobbyReady);

      case GameUpdate:
        Message.GameUpdate gameUpdate =
            mapper.treeToValue(msgNode, Message.GameUpdate.class);
        return new Message(gameUpdate);

      default:
        throw new IllegalStateException("Unexpected type: " + typeStr);
      }
    }
  }
}
