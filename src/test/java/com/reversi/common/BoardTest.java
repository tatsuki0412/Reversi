package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import com.reversi.common.Board;
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
        assertEquals(Board.Status.Empty, board.get(i, j),
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
    // Here, row 0: Black at position 0, rest empty; row 1: White at position 7,
    // rest empty; other rows all empty.
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
    assertEquals(Board.Status.Black, board.get(0, 0));
    for (int j = 1; j < Board.BOARD_SIZE; j++) {
      assertEquals(Board.Status.Empty, board.get(0, j));
    }

    // Second row: last cell is White.
    for (int j = 0; j < Board.BOARD_SIZE - 1; j++) {
      assertEquals(Board.Status.Empty, board.get(1, j));
    }
    assertEquals(Board.Status.White, board.get(1, 7));

    // Remaining rows should be entirely empty.
    for (int i = 2; i < Board.BOARD_SIZE; i++) {
      for (int j = 0; j < Board.BOARD_SIZE; j++) {
        assertEquals(Board.Status.Empty, board.get(i, j));
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
   * Verify that the at method returns the correct status when indexes are
   * within bounds.
   */
  @Test
  public void testAtValidIndices() {
    Board board = new Board();
    // Manually set a couple of cells (via string constructor for convenience)
    String boardString = "B.......0"
                         + ".W......0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0"
                         + "........0";
    board = new Board(boardString);

    assertEquals(Board.Status.Black, board.get(0, 0),
                 "Cell (0,0) should be Black");
    assertEquals(Board.Status.White, board.get(1, 1),
                 "Cell (1,1) should be White");
    assertEquals(Board.Status.Empty, board.get(2, 2),
                 "Cell (2,2) should be Empty");
  }

  /**
   * Verify that after encode to and then decode from a string ,the board
   * remains the same
   */
  @Test
  public void testSerialization() {
    String boardString = Board.createDefault().toString();
    Board board = new Board(boardString);
    assertTrue(board.equals(Board.createDefault()));
  }

  /**
   * Verify the equals method
   */
  @Test
  public void testEquals() {
    Board board = new Board();
    assertTrue(board.equals(board));
    board = Board.createDefault();
    assertTrue(board.equals(Board.createDefault()));
    assertFalse(board.equals(new Board()));
  }
}
