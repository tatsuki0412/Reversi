package com.reversi.server;

import com.reversi.common.Message;
import com.reversi.common.Player;
import com.reversi.common.ReversiGame;
import java.util.Timer;
import java.util.TimerTask;

public class GameSession {
  private ReversiGame game;
  private ClientSocket blackPlayer;
  private ClientSocket whitePlayer;

  private Timer turnTimer;
  private static final long TURN_TIME = 60000; // 1min timer
  private boolean gameOver = false;

  public GameSession(ClientSocket black, ClientSocket white) {
    this.game = new ReversiGame();
    this.blackPlayer = black;
    this.whitePlayer = white;
    startTurnTimer();
  }

  private synchronized void startTurnTimer() {
    if (turnTimer != null) {
      turnTimer.cancel();
    }
    turnTimer = new Timer();
    turnTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        onTimeout();
      }
    }, TURN_TIME);
  }

  private synchronized void onTimeout() {
    if (gameOver)
      return;
    gameOver = true;
    // The current player has timed out – they lose.
    Player current = game.getCurrentPlayer();
    if (current == Player.Black) {
      blackPlayer.sendMessage(
          new Message(new Message.GameOver("Time expired, you lose")));
      whitePlayer.sendMessage(
          new Message(new Message.GameOver("Opponent timed out, you win")));
    } else if (current == Player.White) {
      whitePlayer.sendMessage(
          new Message(new Message.GameOver("Time expired, you lose")));
      blackPlayer.sendMessage(
          new Message(new Message.GameOver("Opponent timed out, you win")));
    }
  }

  // Helper method to check if a client is part of this game.
  public boolean containsClient(ClientSocket handler) {
    return handler.equals(blackPlayer) || handler.equals(whitePlayer);
  }

  public int getBlackId() { return blackPlayer.getClientId(); }
  public int getWhiteId() { return whitePlayer.getClientId(); }

  public Player getClientPlayer(ClientSocket handler) {
    if (handler == blackPlayer)
      return Player.Black;
    else if (handler == whitePlayer)
      return Player.White;
    else
      return Player.None;
  }

  public synchronized boolean makeMove(int row, int col, ClientSocket client) {
    if (gameOver)
      return false;
    Player player = getClientPlayer(client);
    if (player != game.getCurrentPlayer())
      return false;

    boolean moveMade = game.makeMove(row, col);
    if (moveMade) {
      if (turnTimer != null) {
        turnTimer.cancel();
      }
      // Start timer for the opponent’s turn.
      startTurnTimer();
    }
    return moveMade;
  }

  public boolean isValidMove(int row, int col, ClientSocket client) {
    Player player = getClientPlayer(client);
    if (player != game.getCurrentPlayer())
      return false;

    return game.isValidMove(row, col);
  }

  public ReversiGame getGame() { return game; }
}
