package com.reversi.common;

/**
 * ReversiGame encapsulates the core game logic: maintaining the board state,
 * validating moves, flipping discs, and managing turns.
 */
public class ReversiGame {

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
}
