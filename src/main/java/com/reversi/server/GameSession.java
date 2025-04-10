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

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};

  public GameSession(ClientHandler p1, ClientHandler p2) {
    this.blackPlayer = p1;
    this.whitePlayer = p2;
    p1.setPlayerColor('B');
    p2.setPlayerColor('W');
    currentPlayer = 'B';
    board = Board.createDefault();
  }

  // Helper method to check if a client is part of this game.
  public boolean containsClient(ClientHandler handler) {
    return handler.equals(blackPlayer) || handler.equals(whitePlayer);
  }

  private Board.Status convertPlayer(char player) {
    return player == 'W' ? Board.Status.White : Board.Status.Black;
  }
  private Board.Status getOpponent(Board.Status player) {
    return player == Board.Status.Black ? Board.Status.White
                                        : Board.Status.Black;
  }

  public synchronized boolean makeMove(int row, int col, char playerChar) {
    if (playerChar != currentPlayer)
      return false;
    if (!isValidMove(row, col, playerChar))
      return false;

    Board.Status player = convertPlayer(playerChar);
    Board.Status opponent = getOpponent(player);

    board.set(row, col, player);
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
    currentPlayer = (currentPlayer == 'B') ? 'W' : 'B';
    return true;
  }

  private boolean isWithinBounds(int row, int col) {
    return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
  }

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

  public void startGame() {
    blackPlayer.sendMessage(new Message(new Message.Start('B')));
    whitePlayer.sendMessage(new Message(new Message.Start('W')));
    updatePlayers();
  }

  public void updatePlayers() {
    blackPlayer.sendMessage(new Message(new Message.BoardUpdate(board)));
    whitePlayer.sendMessage(new Message(new Message.BoardUpdate(board)));
    if (currentPlayer == 'B') {
      blackPlayer.sendMessage(new Message(new Message.Turn(true)));
      whitePlayer.sendMessage(new Message(new Message.Turn(false)));
    } else {
      blackPlayer.sendMessage(new Message(new Message.Turn(false)));
      whitePlayer.sendMessage(new Message(new Message.Turn(true)));
    }
  }
}
