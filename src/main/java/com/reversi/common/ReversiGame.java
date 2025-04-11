package com.reversi.common;

import java.util.ArrayList;
import java.util.List;

/**
 * ReversiGame encapsulates the core game logic: maintaining the board state,
 * validating moves, flipping discs, and managing turns.
 */
public class ReversiGame {
  public static final int BOARD_SIZE = 8;

  // The current game board.
  private Board board;

  // The current player turn: 'B' for Black and 'W' for White.
  private Player currentPlayer;

  // Directions to search for discs to flip (N, NE, E, SE, S, SW, W, NW).
  private static final int[][] DIRECTIONS = {
      {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};

  /**
   * Constructs a new game with the default starting board.
   */
  public ReversiGame() {
    board = Board.createDefault();
    currentPlayer = Player.Black; // Black starts first.
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
   * Checks whether the given coordinates are within the board bounds.
   */
  private boolean isWithinBounds(int row, int col) {
    return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
  }

  /**
   * Validates whether a move by the specified player at (row, col) is valid.
   *
   * @param row the row index (0-based)
   * @param col the column index (0-based)
   * @param player the player enum
   * @return true if the move is legal, false otherwise
   */
  public boolean isValidMove(int row, int col, Player player) {
    Player opponent = player.opponent();

    if (!isWithinBounds(row, col) || board.get(row, col) != Player.None) {
      return false;
    }
    // For each direction, check if placing here would capture opponent discs.
    for (int[] d : DIRECTIONS) {
      int r = row + d[0], c = col + d[1];
      if (!isWithinBounds(r, c) || board.get(r, c) != opponent) {
        continue;
      }
      // Continue in the current direction.
      r += d[0];
      c += d[1];
      while (isWithinBounds(r, c)) {
        if (board.get(r, c) == opponent) {
          r += d[0];
          c += d[1];
        } else if (board.get(r, c) == player) {
          return true;
        } else {
          break;
        }
      }
    }
    return false;
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
  public boolean makeMove(int row, int col, Player player) {
    // Ensure it is the calling player's turn.
    if (player != currentPlayer)
      return false;
    if (!isValidMove(row, col, player))
      return false;

    Player opponent = player.opponent();

    // Place the disc.
    board.set(row, col, player);

    // For each direction, flip captured opponent discs.
    for (int[] d : DIRECTIONS) {
      List<int[]> discsToFlip = new ArrayList<>();
      int r = row + d[0], c = col + d[1];
      // Collect opponent discs in the current direction.
      while (isWithinBounds(r, c) && board.get(r, c) == opponent) {
        discsToFlip.add(new int[] {r, c});
        r += d[0];
        c += d[1];
      }
      // Check if the sequence is bounded by a player's disc.
      if (isWithinBounds(r, c) && board.get(r, c) == player) {
        // Flip all collected discs.
        for (int[] pos : discsToFlip) {
          board.set(pos[0], pos[1], player);
        }
      }
    }

    // Switch the current player.
    currentPlayer = currentPlayer.opponent();
    return true;
  }

  /**
   *
   * Returns all valid moves for the current player.
   *
   * Each move is represented as an int[] with two elements: row index and
   * column index.
   *
   * @return a list of valid moves available for the current player
   */
  public List<int[]> getValidMoves() {
    List<int[]> validMoves = new ArrayList<>();
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (isValidMove(row, col, currentPlayer)) {
          validMoves.add(new int[] {row, col});
        }
      }
    }
    return validMoves;
  }
}
