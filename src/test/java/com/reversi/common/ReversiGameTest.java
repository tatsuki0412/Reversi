package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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

    // For White in the updated board, a valid move is (2,4) (assuming it flips
    // at least one disc).
    assertTrue(game.isValidMove(2, 4),
               "After Black's move, (2,4) should be a valid move for White.");
    assertTrue(game.makeMove(2, 4),
               "White should be able to make a move at (2,4).");
    assertEquals(Player.Black, game.getCurrentPlayer(),
                 "Turn should switch back to Black after White's move.");
  }

  @Test
  public void testBoardSerialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Board defaultBoard = Board.createDefault();
    String json = mapper.writeValueAsString(defaultBoard);

    // Check that the serialized JSON contains a "grid" field.
    assertTrue(json.contains("\"grid\":"),
               "Serialized board JSON should contain a 'grid' field.");

    // Deserialize the JSON back into a Board and verify equality.
    Board deserializedBoard = mapper.readValue(json, Board.class);
    assertEquals(defaultBoard, deserializedBoard,
                 "Deserialized board should equal the original board.");
  }

  @Test
  public void testBoardDeserialization_invalidGridSize() {
    ObjectMapper mapper = new ObjectMapper();
    // Create an invalid JSON board with only 7 rows instead of 8.
    String invalidJson = "{ \"grid\": ["
                         + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\"W\",\"B\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\"B\",\"W\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
                         + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"]"
                         + "] }";
    assertThrows(IOException.class,
                 () -> { mapper.readValue(invalidJson, Board.class); });
  }

  @Test
  public void testReversiGameSerialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ReversiGame game = new ReversiGame();

    // Serialize the game to JSON.
    String json = mapper.writeValueAsString(game);

    // Verify that JSON contains both "board" and "current_player" fields.
    assertTrue(json.contains("\"board\":"),
               "Serialized game JSON should contain a 'board' field.");
    assertTrue(json.contains("\"current_player\":"),
               "Serialized game JSON should contain a 'current_player' field.");

    // Deserialize the JSON back into a game.
    ReversiGame deserializedGame = mapper.readValue(json, ReversiGame.class);
    assertNotNull(deserializedGame.getBoard(),
                  "Deserialized game should have a valid board.");
    assertNotNull(deserializedGame.getCurrentPlayer(),
                  "Deserialized game should have a valid current player.");

    // Validate that the deserialized board and current player match the
    // originals.
    assertEquals(game.getBoard(), deserializedGame.getBoard(),
                 "Deserialized board should equal the original board.");
    assertEquals(game.getCurrentPlayer(), deserializedGame.getCurrentPlayer(),
                 "Deserialized current player should equal the original "
                     + "current player.");
  }

  @Test
  public void testReversiGameDeserialization_missingFields() {
    ObjectMapper mapper = new ObjectMapper();

    // JSON missing the 'board' field.
    String jsonMissingBoard = "{ \"current_player\": \"B\" }";
    Exception exception1 = assertThrows(IOException.class, () -> {
      mapper.readValue(jsonMissingBoard, ReversiGame.class);
    });
    assertTrue(
        exception1.getMessage().contains("Missing required field: board"),
        "Exception should mention missing board field.");

    // JSON missing the 'current_player' field.
    String jsonMissingPlayer =
        "{ \"board\": { \"grid\": ["
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\"W\",\"B\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\"B\",\"W\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"],"
        + "[\".\",\".\",\".\",\".\",\".\",\".\",\".\",\".\"]"
        + "] } }";
    assertThrows(IOException.class, () -> {
      mapper.readValue(jsonMissingPlayer, ReversiGame.class);
    });
  }
}
