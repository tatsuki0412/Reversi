package com.reversi.server;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
  public static final int BOARD_SIZE = 8;
  private char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
  private ClientHandler blackPlayer;
  private ClientHandler whitePlayer;
  private char currentPlayer; // 'B' or 'W'

  // Directions to search for flips (N, NE, E, SE, S, SW, W, NW)
  private static final int[][] DIRECTIONS = {
      {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};

  public GameSession(ClientHandler p1, ClientHandler p2) {
    // assign p1 the Black discs and p2 the White discs
    this.blackPlayer = p1;
    this.whitePlayer = p2;
    p1.setPlayerColor('B');
    p2.setPlayerColor('W');
    currentPlayer = 'B';
    initializeBoard();
  }

  private void initializeBoard() {
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        board[i][j] = '.';
      }
    }
    board[3][3] = 'W';
    board[3][4] = 'B';
    board[4][3] = 'B';
    board[4][4] = 'W';
  }

  // Convert board state into a string for transmission.
  // Here we send 8 lines (rows) separated by newline characters.
  public String boardToString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        sb.append(board[i][j]);
      }
      sb.append("0");
    }
    return sb.toString();
  }

  // Called by a ClientHandler to attempt a move.
  // This method is synchronized to prevent concurrent updates.
  public synchronized boolean makeMove(int row, int col, char player) {
    if (player != currentPlayer)
      return false; // Not this player's turn.
    if (!isValidMove(row, col, player))
      return false;
    board[row][col] = player;
    // For each direction, flip opponent discs if bracketing is valid.
    for (int[] d : DIRECTIONS) {
      List<int[]> discsToFlip = new ArrayList<>();
      int r = row + d[0], c = col + d[1];
      char opponent = (player == 'B') ? 'W' : 'B';
      while (isWithinBounds(r, c) && board[r][c] == opponent) {
        discsToFlip.add(new int[] {r, c});
        r += d[0];
        c += d[1];
      }
      if (isWithinBounds(r, c) && board[r][c] == player) {
        for (int[] pos : discsToFlip) {
          board[pos[0]][pos[1]] = player;
        }
      }
    }
    // Switch turn.
    currentPlayer = (currentPlayer == 'B') ? 'W' : 'B';
    return true;
  }

  private boolean isWithinBounds(int row, int col) {
    return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
  }

  // Validate whether placing a disc at (row, col) for player is legal.
  public boolean isValidMove(int row, int col, char player) {
    if (!isWithinBounds(row, col) || board[row][col] != '.')
      return false;
    char opponent = (player == 'B') ? 'W' : 'B';
    for (int[] d : DIRECTIONS) {
      int r = row + d[0], c = col + d[1];
      if (!isWithinBounds(r, c) || board[r][c] != opponent)
        continue;
      r += d[0];
      c += d[1];
      while (isWithinBounds(r, c)) {
        if (board[r][c] == opponent) {
          r += d[0];
          c += d[1];
        } else if (board[r][c] == player) {
          return true;
        } else {
          break;
        }
      }
    }
    return false;
  }

  // Send the initial messages to both players and update them with the board
  // state.
  public void startGame() {
    // Inform clients of their colors.
    blackPlayer.sendMessage("START:B");
    whitePlayer.sendMessage("START:W");
    updatePlayers();
  }

  // Sends the current board state and whose turn it is.
  public void updatePlayers() {
    String boardStr = boardToString();
    blackPlayer.sendMessage("BOARD:" + boardStr);
    whitePlayer.sendMessage("BOARD:" + boardStr);
    // Indicate whose turn it is.
    if (currentPlayer == 'B') {
      blackPlayer.sendMessage("TURN:YOUR");
      whitePlayer.sendMessage("TURN:OPPONENT");
    } else {
      whitePlayer.sendMessage("TURN:YOUR");
      blackPlayer.sendMessage("TURN:OPPONENT");
    }
  }
}
