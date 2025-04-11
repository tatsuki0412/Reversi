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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Board class models an 8x8 board for the game of Reversi.
 * <p>
 * It provides functionality for board initialization, move validation,
 * move execution (including flipping the opponent's pieces), and serialization.
 * The board is serialized to a string representation and deserialized from the
 * same format.
 * </p>
 */
@JsonSerialize(using = Board.BoardSerializer.class)
@JsonDeserialize(using = Board.BoardDeserializer.class)
public class Board {
  private static final Logger logger = LoggerFactory.getLogger(Board.class);

  /** The dimension of the board (8x8). */
  public static final int BOARD_SIZE = 8;

  /**
   * Offsets representing all 8 directions (N, NE, E, SE, S, SW, W, NW)
   * to search for discs that may be captured.
   */
  private static final int[][] DIRECTIONS = {
      {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};

  /**
   * The board state represented as a 2D array of Player values.
   * Each element indicates which player occupies that position, or Player.None
   * if empty.
   */
  private Player[][] status;

  /**
   * Default constructor. Initializes the board to an empty state.
   */
  Board() {
    this.status = new Player[BOARD_SIZE][BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        status[i][j] = Player.None;
      }
    }
  }

  /**
   * Constructs a Board from a String representation.
   * <p>
   * Expected format: Eight 8-character rows separated by "0". Each character
   * represents: <ul> <li>'W' for a White disc</li> <li>'B' for a Black
   * disc</li> <li>Any other character for an empty cell</li>
   * </ul>
   * </p>
   *
   * @param str the string representation of the board
   * @throws IllegalArgumentException if the input format is invalid
   */
  Board(String str) throws IllegalArgumentException {
    this();
    String[] lines = str.split("0");
    if (lines.length != BOARD_SIZE) {
      logger.error("Invalid format: expected {} rows but got {}. Input: {}",
                   BOARD_SIZE, lines.length, str);
      throw new IllegalArgumentException(
          "Invalid board format: Incorrect number of rows.");
    }

    for (int i = 0; i < BOARD_SIZE; i++) {
      String rowLine = lines[i];
      if (rowLine.length() != BOARD_SIZE) {
        logger.error(
            "Invalid format: each row must have {} characters. Input: {}",
            BOARD_SIZE, str);
        throw new IllegalArgumentException(
            "Invalid board format: Incorrect row length.");
      }
      for (int j = 0; j < BOARD_SIZE; j++) {
        char c = rowLine.charAt(j);
        if (c == 'W') {
          status[i][j] = Player.White;
        } else if (c == 'B') {
          status[i][j] = Player.Black;
        } else {
          status[i][j] = Player.None;
        }
      }
    }
  }

  /**
   * Factory method that creates a board with the default starting position for
   * Reversi. <p> The initial configuration places two white discs and two black
   * discs in the center.
   * </p>
   *
   * @return a Board instance set to the default starting state
   */
  public static Board createDefault() {
    Board board = new Board();
    board.set(3, 3, Player.White);
    board.set(4, 4, Player.White);
    board.set(3, 4, Player.Black);
    board.set(4, 3, Player.Black);
    return board;
  }

  /**
   * Converts the board to its string representation.
   * <p>
   * Each row is converted to a sequence of characters representing the discs:
   * 'B' for black, 'W' for white, and '.' for an empty cell.
   * Rows are separated by the delimiter "0".
   * </p>
   *
   * @return the string representation of the board
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(BOARD_SIZE * BOARD_SIZE + BOARD_SIZE);
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        switch (status[i][j]) {
        case Black:
          sb.append('B');
          break;
        case White:
          sb.append('W');
          break;
        default:
          sb.append('.');
          break;
        }
      }
      // Separate rows with a '0' delimiter.
      sb.append('0');
    }
    return sb.toString();
  }

  /**
   * Retrieves the Player at the specified position.
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @return the Player occupying the cell; Player.None if out of bounds or
   *     empty
   */
  public Player get(int row, int col) {
    if (row < 0 || row >= BOARD_SIZE) {
      logger.error("Row {} out of bounds.", row);
      return Player.None;
    }
    if (col < 0 || col >= BOARD_SIZE) {
      logger.error("Column {} out of bounds.", col);
      return Player.None;
    }
    return this.status[row][col];
  }

  /**
   * Sets the specified cell on the board to the given player's disc.
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param status the player disc to set (or Player.None to clear)
   */
  public void set(int row, int col, Player status) {
    if (row < 0 || row >= BOARD_SIZE) {
      logger.error("Row {} out of bounds.", row);
      return;
    }
    if (col < 0 || col >= BOARD_SIZE) {
      logger.error("Column {} out of bounds.", col);
      return;
    }
    this.status[row][col] = status;
  }

  /**
   * Compares this board with another for equality.
   * <p>
   * Two boards are considered equal if all corresponding cells have the same
   * Player value.
   * </p>
   *
   * @param obj the object to compare against
   * @return true if the boards have identical states; false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Board)) {
      return false;
    }
    Board other = (Board)obj;
    for (int i = 0; i < BOARD_SIZE; ++i) {
      for (int j = 0; j < BOARD_SIZE; ++j) {
        if (this.status[i][j] != other.get(i, j)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks whether the specified coordinates are within the bounds of the
   * board.
   *
   * @param row the row index
   * @param col the column index
   * @return true if the coordinates are valid; false otherwise
   */
  private boolean isWithinBounds(int row, int col) {
    return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
  }

  /**
   * Validates whether a move by the specified player at (row, col) is allowed.
   * <p>
   * A move is valid if the cell is empty and there is at least one straight
   * line (in one of the 8 directions) where one or more contiguous opponent
   * discs are bracketed by the player's disc.
   * </p>
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param player the player attempting the move
   * @return true if the move is legal; false otherwise
   */
  public boolean isValidMove(int row, int col, Player player) {
    Player opponent = player.opponent();

    if (!isWithinBounds(row, col) || get(row, col) != Player.None) {
      return false;
    }
    // Check each direction for a potential capture.
    for (int[] d : DIRECTIONS) {
      int r = row + d[0], c = col + d[1];
      if (!isWithinBounds(r, c) || get(r, c) != opponent) {
        continue;
      }
      // Move further in the same direction.
      r += d[0];
      c += d[1];
      while (isWithinBounds(r, c)) {
        if (get(r, c) == opponent) {
          r += d[0];
          c += d[1];
        } else if (get(r, c) == player) {
          return true;
        } else {
          break;
        }
      }
    }
    return false;
  }

  /**
   * Attempts to execute a move for the given player at the specified position.
   * <p>
   * This method first validates the move. If valid, it places the player's disc
   * and flips all the captured opponent discs along each valid direction.
   * </p>
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param player the player making the move
   * @return true if the move was successfully executed; false otherwise
   */
  public boolean makeMove(int row, int col, Player player) {
    if (!isValidMove(row, col, player)) {
      return false;
    }

    Player opponent = player.opponent();
    // Place the player's disc at the specified location.
    set(row, col, player);

    // Process all directions to determine if any opponent discs can be
    // captured.
    for (int[] d : DIRECTIONS) {
      List<int[]> discsToFlip = new ArrayList<>();
      int r = row + d[0], c = col + d[1];
      // Accumulate positions of consecutive opponent discs.
      while (isWithinBounds(r, c) && get(r, c) == opponent) {
        discsToFlip.add(new int[] {r, c});
        r += d[0];
        c += d[1];
      }
      // If the sequence is bounded by a disc belonging to the current player,
      // all intermediate opponent discs are captured.
      if (isWithinBounds(r, c) && get(r, c) == player) {
        for (int[] pos : discsToFlip) {
          set(pos[0], pos[1], player);
        }
      }
    }
    return true;
  }

  /**
   * Returns a list of all valid moves for the specified player.
   * <p>
   * Each move is represented as an integer array of size 2, where the first
   * element is the row index and the second is the column index.
   * </p>
   *
   * @param player the player for whom valid moves are determined
   * @return a list of valid moves, where each move is represented as an int
   *     array [row, col]
   */
  public List<int[]> getValidMoves(Player player) {
    List<int[]> validMoves = new ArrayList<>();
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (isValidMove(row, col, player)) {
          validMoves.add(new int[] {row, col});
        }
      }
    }
    return validMoves;
  }

  // ===================== Serialization Support =====================
  public static class BoardSerializer extends JsonSerializer<Board> {
    @Override
    public void serialize(Board board, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      // Write an explicit "grid" field as an array of arrays.
      gen.writeArrayFieldStart("grid");
      for (int i = 0; i < BOARD_SIZE; i++) {
        gen.writeStartArray();
        for (int j = 0; j < BOARD_SIZE; j++) {
          // Map Player status to explicit string values.
          switch (board.status[i][j]) {
          case Black:
            gen.writeString("B");
            break;
          case White:
            gen.writeString("W");
            break;
          default:
            gen.writeString("None");
            break;
          }
        }
        gen.writeEndArray();
      }
      gen.writeEndArray(); // End of "grid" array.
      gen.writeEndObject();
    }
  }

  public static class BoardDeserializer extends JsonDeserializer<Board> {
    @Override
    public Board deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      // Ensure that we start with a JSON object.
      if (p.currentToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected START_OBJECT token, but got: " +
                              p.currentToken());
      }

      Board board = new Board(); // Create an empty board.

      // Process the fields of the object.
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.getCurrentName();
        p.nextToken(); // Move to the field value.

        if ("grid".equals(fieldName)) {
          // The "grid" field must be an array.
          if (p.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException(
                "Expected 'grid' field to be an array, but got: " +
                p.currentToken());
          }
          int row = 0;
          while (p.nextToken() != JsonToken.END_ARRAY) {
            // Each row must itself be an array.
            if (p.currentToken() != JsonToken.START_ARRAY) {
              throw new IOException(
                  "Expected each row to be an array, but got: " +
                  p.currentToken());
            }
            int col = 0;
            while (p.nextToken() != JsonToken.END_ARRAY) {
              if (p.currentToken() != JsonToken.VALUE_STRING) {
                throw new IOException("Expected a string value at row " + row +
                                      " column " + col +
                                      ", but got: " + p.currentToken());
              }
              String cellValue = p.getText();
              Player cell;
              if ("B".equals(cellValue)) {
                cell = Player.Black;
              } else if ("W".equals(cellValue)) {
                cell = Player.White;
              } else if ("None".equals(cellValue)) {
                cell = Player.None;
              } else {
                throw new IOException("Invalid cell value '" + cellValue +
                                      "' at row " + row + " column " + col);
              }
              board.status[row][col] = cell;
              col++;
            }
            if (col != BOARD_SIZE)
              throw new IOException("Row " + row + " has " + col +
                                    " cells; expected " + BOARD_SIZE);
            row++;
          }
          if (row != BOARD_SIZE)
            throw new IOException("Grid has " + row + " rows; expected " +
                                  BOARD_SIZE);
        } else
          throw new IOException("Unexpected field: " + fieldName);
      }
      return board;
    }
  }
}
