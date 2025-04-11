package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReversiGameTest {

  @Test
  public void testInitialSetup() {
    ReversiGame game = new ReversiGame();

    // Verify that Black starts the game.
    assertEquals(Player.Black, game.getCurrentPlayer(),
                 "The game should start with Black as the current player.");

    String expectedBoard = "........0"    // row 0
                           + "........0"  // row 1
                           + "........0"  // row 2
                           + "...WB...0"  // row 3
                           + "...BW...0"  // row 4
                           + "........0"  // row 5
                           + "........0"  // row 6
                           + "........0"; // row 7

    assertEquals(
        expectedBoard, game.getBoard().toString(),
        "The board should be initialized to the default starting position.");
  }

  @Test
  public void testValidMove() {
    ReversiGame game = new ReversiGame();

    // In the default setup, a common valid move for Black is at (2,3):
    // Placing a disc at (2,3) should capture the white disc at (3,3) along the
    // downward direction.
    assertTrue(game.isValidMove(2, 3),
               "The move at (2,3) should be valid for Black.");

    // Execute the valid move.
    boolean moveExecuted = game.makeMove(2, 3);
    assertTrue(moveExecuted, "The valid move should be executed successfully.");

    // After a valid move, the turn should switch to the opponent (White).
    assertEquals(Player.White, game.getCurrentPlayer(),
                 "After Black's move, the turn should switch to White.");

    // The board should now have a Black disc at (2,3)
    assertEquals(Player.Black, game.getBoard().get(2, 3),
                 "Cell (2,3) should be occupied by Black after the move.");

    // The disc at (3,3) originally White should have been flipped to Black.
    assertEquals(Player.Black, game.getBoard().get(3, 3),
                 "Cell (3,3) should have been flipped to Black.");
  }

  @Test
  public void testInvalidMove() {
    ReversiGame game = new ReversiGame();

    // (0,0) is not a valid move in the default state because it doesn't flip
    // any opponent discs.
    assertFalse(game.isValidMove(0, 0), "The move at (0,0) should be invalid.");

    // Attempting an invalid move should return false and not change the game
    // state.
    boolean moveExecuted = game.makeMove(0, 0);
    assertFalse(moveExecuted, "Executing an invalid move should return false.");

    // The turn should not have switched.
    assertEquals(
        Player.Black, game.getCurrentPlayer(),
        "After an invalid move, the current player should remain unchanged.");
  }

  @Test
  public void testTurnSwitchingSequence() {
    ReversiGame game = new ReversiGame();

    // First, let Black make a valid move.
    // For Black, (2,3) is a valid move in the starting board.
    assertTrue(game.makeMove(2, 3),
               "Black should be able to make a move at (2,3).");
    assertEquals(Player.White, game.getCurrentPlayer(),
                 "Turn should switch to White after Black's move.");

    // For White in the updated board, a valid move is (2,4).
    // Here, (2,4) would capture the disc to the right (from (3,4)) if valid
    // according to the flipped board state.
    assertTrue(game.isValidMove(2, 4),
               "After Black's move, (2,4) should be a valid move for White.");
    assertTrue(game.makeMove(2, 4),
               "White should be able to make a move at (2,4).");
    assertEquals(Player.Black, game.getCurrentPlayer(),
                 "Turn should switch back to Black after White's move.");
  }
}
