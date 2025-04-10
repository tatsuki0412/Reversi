package com.reversi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using = Board.BoardSerializer.class)
@JsonDeserialize(using = Board.BoardDeserializer.class)
public class Board {
  private static final Logger logger = LoggerFactory.getLogger(Board.class);

  public static final int BOARD_SIZE = 8;

  public enum Status { Empty, Black, White }
  private Status[][] board;

  Board() {
    this.board = new Status[BOARD_SIZE][BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        board[i][j] = Status.Empty;
      }
    }
  }
  Board(String str) throws IllegalArgumentException {
    this();
    String[] lines = str.split("0");
    if (lines.length != 8) {
      logger.error("Invalid format. input is {}" + str);
      return;
    }

    for (int i = 0; i < 8; i++) {
      String rowLine = lines[i];
      if (rowLine.length() != 8) {
        logger.error("Invalid format. input is {}" + str);
        return;
      }
      for (int j = 0; j < 8; j++) {
        char c = rowLine.charAt(j);
        if (c == 'W')
          board[i][j] = Status.White;
        else if (c == 'B')
          board[i][j] = Status.Black;
        else
          board[i][j] = Status.Empty;
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(BOARD_SIZE * BOARD_SIZE + BOARD_SIZE);
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        switch (board[i][j]) {
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

      sb.append('0');
    }
    return sb.toString();
  }

  public Status get(int row, int col) {
    if (row < 0 || row >= BOARD_SIZE) {
      logger.error("Row {} out of bounds.", row);
      return Status.Empty;
    }
    if (col < 0 || col >= BOARD_SIZE) {
      logger.error("Col {} out of bounds.", col);
      return Status.Empty;
    }

    return this.board[row][col];
  }

  public void set(int row, int col, Status status) {
    if (row < 0 || row >= BOARD_SIZE) {
      logger.error("Row {} out of bounds.", row);
      return;
    }
    if (col < 0 || col >= BOARD_SIZE) {
      logger.error("Col {} out of bounds.", col);
      return;
    }

    this.board[row][col] = status;
  }

  public boolean equals(Object obj) {
    Board other = (Board)obj;
    if (other == null)
      return false;

    for (int i = 0; i < BOARD_SIZE; ++i)
      for (int j = 0; j < BOARD_SIZE; ++j)
        if (board[i][j] != other.get(i, j))
          return false;
    return true;
  }

  // Factory method to create a init prototype
  public static Board createDefault() {
    Board board = new Board();
    board.set(3, 3, Status.White);
    board.set(4, 4, Status.White);
    board.set(3, 4, Status.Black);
    board.set(4, 3, Status.Black);
    return board;
  }

  // Serialization support
  public static class BoardSerializer extends JsonSerializer<Board> {
    @Override
    public void serialize(Board board, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      // Serialize the board by converting it to its string representation.
      gen.writeString(board.toString());
    }
  }
  public static class BoardDeserializer extends JsonDeserializer<Board> {
    @Override
    public Board deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      // Expect the board to be represented as a single String value.
      String boardStr = p.getValueAsString();
      try {
        return new Board(boardStr);
      } catch (IllegalArgumentException e) {
        // You might want to wrap or rethrow exceptions in a real application.
        throw new IOException("Failed to deserialize Board: " + e.getMessage(),
                              e);
      }
    }
  }
}
