package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BoardTest {

  /**
   * Verify that the default constructor creates an 8x8 board with all cells set
   * to Empty.
   */
  @Test
  public void testDefaultConstructor() {
    Board board = new Board();
    for (int i = 0; i < Board.BOARD_SIZE; i++) {
      for (int j = 0; j < Board.BOARD_SIZE; j++) {
        assertEquals(Player.None, board.get(i, j),
                     "Expected cell at (" + i + "," + j + ") to be Empty");
      }
    }
  }

  /**
   * Verify that creating a board from a valid string properly sets the status.
   * A sample string is composed of eight rows separated by the delimiter '0'.
   */
  @Test
  public void testStringConstructorValid() {
    // Create a board string with a mixture of empty, Black, and White cells.
    // Row 0: Black at position 0, rest empty; row 1: White at position 7, rest
    // empty; remaining rows empty.
    String boardString = "B.......0"
                         + ".......W0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0";

    Board board = new Board(boardString);

    // First row: first cell is Black, remaining empty.
    assertEquals(Player.Black, board.get(0, 0));
    for (int j = 1; j < Board.BOARD_SIZE; j++) {
      assertEquals(Player.None, board.get(0, j));
    }

    // Second row: last cell is White.
    for (int j = 0; j < Board.BOARD_SIZE - 1; j++) {
      assertEquals(Player.None, board.get(1, j));
    }
    assertEquals(Player.White, board.get(1, 7));

    // Remaining rows should be entirely empty.
    for (int i = 2; i < Board.BOARD_SIZE; i++) {
      for (int j = 0; j < Board.BOARD_SIZE; j++) {
        assertEquals(Player.None, board.get(i, j));
      }
    }
  }

  /**
   * Verify that toString produces a board string where each row of 8 characters
   * is followed by the delimiter '0'. For an empty board, each row should
   * consist of eight '.' characters.
   */
  @Test
  public void testToString() {
    Board board = new Board();
    String expectedRow = "........0";
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < Board.BOARD_SIZE; i++) {
      expected.append(expectedRow);
    }
    assertEquals(expected.toString(), board.toString(),
                 "toString output does not match the expected pattern");
  }

  /**
   * Verify that the get method returns the correct status when indexes are
   * within bounds.
   */
  @Test
  public void testAtValidIndices() {
    // Using string constructor for convenience.
    String boardString = "B.......0"
                         + ".W......0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0";
    Board board = new Board(boardString);

    assertEquals(Player.Black, board.get(0, 0), "Cell (0,0) should be Black");
    assertEquals(Player.White, board.get(1, 1), "Cell (1,1) should be White");
    assertEquals(Player.None, board.get(2, 2), "Cell (2,2) should be Empty");
  }

  /**
   * Verify the equals method.
   */
  @Test
  public void testEquals() {
    Board board = new Board();
    assertTrue(board.equals(board));
    board = Board.createDefault();
    assertTrue(board.equals(Board.createDefault()));
    assertFalse(board.equals(new Board()));
  }

  /**
   * Verify that encoding to and then decoding from a string preserves the board
   * state.
   */
  @Test
  public void testSaveLoad() {
    String boardString = Board.createDefault().toString();
    Board board = new Board(boardString);
    assertTrue(board.equals(Board.createDefault()));
  }

  /**
   * Verify that JSON serialization and deserialization produce an equivalent
   * board.
   */
  @Test
  public void testJsonSerialization() {
    ObjectMapper mapper = JacksonObjMapper.get();
    Board board = Board.createDefault();
    String json = assertDoesNotThrow(() -> mapper.writeValueAsString(board));
    Board deserialized =
        assertDoesNotThrow(() -> mapper.readValue(json, Board.class));
    assertTrue(board.equals(deserialized));
  }

  /**
   * Verify the move validation logic using the isValidMove method.
   * <p>
   * For the default board, a common valid move for Black is at (2,3)
   * and for White is at (2,4). The test also covers invalid moves due to
   * occupied cells and out-of-bounds positions.
   * </p>
   */
  @Test
  public void testIsValidMove() {
    Board board = Board.createDefault();

    // For Black, the move at (2,3) should be valid.
    assertTrue(board.isValidMove(2, 3, Player.Black),
               "Expected (2,3) to be a valid move for Black.");

    // For White, the move at (2,4) should be valid.
    assertTrue(board.isValidMove(2, 4, Player.White),
               "Expected (2,4) to be a valid move for White.");

    // Attempt a move on an already occupied cell should be invalid.
    assertFalse(
        board.isValidMove(3, 3, Player.Black),
        "Expected move at (3,3) to be invalid since the cell is occupied.");

    // Out-of-bound moves should also be invalid.
    assertFalse(board.isValidMove(-1, 0, Player.Black),
                "Expected move (-1,0) to be invalid due to out-of-bounds row.");
    assertFalse(board.isValidMove(0, Board.BOARD_SIZE, Player.White),
                "Expected move (0," + Board.BOARD_SIZE +
                    ") to be invalid due to out-of-bounds column.");
  }

  /**
   * Verify that making a move with makeMove correctly updates the board state,
   * including flipping the opponent's discs.
   */
  @Test
  public void testMakeMove() {
    Board board = Board.createDefault();

    // Black makes a valid move at (2,3). This should flip the White disc at
    // (3,3).
    assertTrue(board.makeMove(2, 3, Player.Black),
               "Expected move (2,3) by Black to be successful.");
    // Check that the move was executed.
    assertEquals(Player.Black, board.get(2, 3),
                 "Expected (2,3) to now hold a Black disc.");
    // Verify that the opponent disc at (3,3) flips to Black.
    assertEquals(Player.Black, board.get(3, 3),
                 "Expected (3,3) to flip to Black after the move.");

    // Attempt to make an illegal move (placing on an already occupied cell).
    assertFalse(board.makeMove(2, 3, Player.White),
                "Expected move at (2,3) by White to fail because the cell is " +
                "occupied.");
  }

  /**
   * Verify that getValidMoves returns the correct list of valid moves for a
   * player. <p> For the default board, it is expected that both Black and White
   * have four valid moves. The test specifically checks for the presence of a
   * known valid move.
   * </p>
   */
  @Test
  public void testGetValidMoves() {
    Board board = Board.createDefault();

    // For a default board, Black is expected to have 4 valid moves.
    List<int[]> blackValidMoves = board.getValidMoves(Player.Black);
    assertEquals(4, blackValidMoves.size(),
                 "Expected 4 valid moves for Black on a default board.");

    // Check that one known valid move (2,3) is present in the list.
    boolean found = false;
    for (int[] move : blackValidMoves) {
      if (move[0] == 2 && move[1] == 3) {
        found = true;
        break;
      }
    }
    assertTrue(
        found,
        "Expected to find the valid move (2,3) for Black on a default board.");

    // Similarly, White should have 4 valid moves.
    List<int[]> whiteValidMoves = board.getValidMoves(Player.White);
    assertEquals(4, whiteValidMoves.size(),
                 "Expected 4 valid moves for White on a default board.");
  }
}
