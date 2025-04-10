package com.reversi.server;

import com.reversi.common.Board;
import com.reversi.common.Message;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
  public static final int BOARD_SIZE = 8;
  private Board board;
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
    board = Board.createDefault();
  }

  private Board.Status convertPlayer(char player) {
    return player == 'W' ? Board.Status.White : Board.Status.Black;
  }
  private Board.Status getOpponent(Board.Status player) {
    return player == Board.Status.Black ? Board.Status.White
                                        : Board.Status.Black;
  }

  // Called by a ClientHandler to attempt a move.
  // This method is synchronized to prevent concurrent updates.
  public synchronized boolean makeMove(int row, int col, char playerChar) {
    if (playerChar != currentPlayer)
      return false; // Not this player's turn.
    if (!isValidMove(row, col, playerChar))
      return false;

    Board.Status player = convertPlayer(playerChar);
    Board.Status opponent = getOpponent(player);

    board.set(row, col, player);
    // For each direction, flip opponent discs if bracketing is valid.
    for (int[] d : DIRECTIONS) {
      List<int[]> discsToFlip = new ArrayList<>();
      int r = row + d[0], c = col + d[1];
      while (isWithinBounds(r, c) && board.get(r, c) == opponent) {
        discsToFlip.add(new int[] {r, c});
        r += d[0];
        c += d[1];
      }
      if (isWithinBounds(r, c) && board.get(r, c) == player) {
        for (int[] pos : discsToFlip)
          board.set(pos[0], pos[1], player);
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
  public boolean isValidMove(int row, int col, char playerChar) {
    Board.Status player = convertPlayer(playerChar);
    Board.Status opponent = getOpponent(player);

    if (!isWithinBounds(row, col) || board.get(row, col) != Board.Status.Empty)
      return false;
    for (int[] d : DIRECTIONS) {
      int r = row + d[0], c = col + d[1];
      if (!isWithinBounds(r, c) || board.get(r, c) != opponent)
        continue;
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

  // Send the initial messages to both players and update them with the board
  // state.
  public void startGame() {
    // Inform clients of their colors.
    blackPlayer.sendMessage(new Message(new Message.Start('B')));
    whitePlayer.sendMessage(new Message(new Message.Start('W')));
    updatePlayers();
  }

  // Sends the current board state and whose turn it is.
  public void updatePlayers() {
    blackPlayer.sendMessage(new Message(new Message.BoardUpdate(board)));
    whitePlayer.sendMessage(new Message(new Message.BoardUpdate(board)));
    // Indicate whose turn it is.
    if (currentPlayer == 'B') {
      blackPlayer.sendMessage(new Message(new Message.Turn(true)));
      whitePlayer.sendMessage(new Message(new Message.Turn(false)));
    } else {
      blackPlayer.sendMessage(new Message(new Message.Turn(false)));
      whitePlayer.sendMessage(new Message(new Message.Turn(true)));
    }
  }
}
