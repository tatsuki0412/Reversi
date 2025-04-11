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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReversiGame encapsulates the core game logic: maintaining the board state,
 * validating moves, flipping discs, and managing turns.
 */
@JsonSerialize(using = ReversiGame.Serializer.class)
@JsonDeserialize(using = ReversiGame.Deserializer.class)
public class ReversiGame {
  private static final Logger logger =
      LoggerFactory.getLogger(ReversiGame.class);

  // The current game board.
  private Board board;

  // The current player turn: 'B' for Black and 'W' for White.
  private Player currentPlayer;

  /**
   * Constructs a new game with the default starting board.
   */
  public ReversiGame() {
    board = Board.createDefault();
    currentPlayer = Player.Black; // Black starts first.
  }

  public ReversiGame(Board board, Player currentPlayer) {
    this.board = board;
    this.currentPlayer = currentPlayer;
  }

  /**
   * Returns the current state of the board.
   */
  public Board getBoard() { return board; }

  /**
   * Returns the current player enum.
   */
  public Player getCurrentPlayer() { return currentPlayer; }

  /**
   * Validates whether a move by the current player at (row, col) is valid.
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param player the player enum
   * @return true if the move is legal, false otherwise
   */
  public boolean isValidMove(int row, int col) {
    return board.isValidMove(row, col, currentPlayer);
  }

  /**
   * Attempts to make a move for the given player at (row, col). If the move is
   * valid, the method updates the board (including flipping opponent discs) and
   * switches turns.
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param playerChar the player enum
   * @return true if the move was executed; false otherwise
   */
  public boolean makeMove(int row, int col) {
    if (!board.makeMove(row, col, currentPlayer))
      return false;

    // Switch the current player.
    currentPlayer = currentPlayer.opponent();
    return true;
  }

  // ===================== Serialization Support =====================
  public static class Serializer extends JsonSerializer<ReversiGame> {
    @Override
    public void serialize(ReversiGame game, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      // Delegate the board serialization to its custom serializer.
      gen.writeFieldName("board");
      serializers.defaultSerializeValue(game.getBoard(), gen);
      // Write the current player as a string.
      gen.writeStringField("current_player",
                           String.valueOf(game.getCurrentPlayer().toChar()));
      gen.writeEndObject();
    }
  }
  public static class Deserializer extends JsonDeserializer<ReversiGame> {
    @Override
    public ReversiGame deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      Board board = null;
      Player currentPlayer = null;

      // Verify that we start with a JSON object.
      if (p.currentToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected START_OBJECT token, but found: " +
                              p.currentToken());
      }

      // Process each field within the object.
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.getCurrentName();
        p.nextToken(); // move to the value of the field

        if ("board".equals(fieldName)) {
          // Delegate board deserialization to its custom deserializer.
          board = ctxt.readValue(p, Board.class);
        } else if ("current_player".equals(fieldName)) {
          String cpStr = p.getValueAsString();
          if (cpStr == null || cpStr.isEmpty()) {
            throw new IOException(
                "Invalid or missing value for field 'current_player'");
          }
          currentPlayer = Player.from(cpStr.charAt(0));
        } else {
          throw new IOException("Unexpected field: " + fieldName);
        }
      }

      // Validate that both required fields were properly deserialized.
      if (board == null) {
        throw new IOException("Missing required field: board");
      }
      if (currentPlayer == null) {
        throw new IOException("Missing required field: current_player");
      }

      return new ReversiGame(board, currentPlayer);
    }
  }
}
