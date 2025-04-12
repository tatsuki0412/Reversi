package com.reversi.server;

import com.reversi.common.Player;
import com.reversi.common.ReversiGame;

public class GameSession {
  private ReversiGame game;
  private ClientSocket blackPlayer;
  private ClientSocket whitePlayer;

  public GameSession(ClientSocket black, ClientSocket white) {
    this.game = new ReversiGame();
    this.blackPlayer = black;
    this.whitePlayer = white;
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
    Player player = getClientPlayer(client);
    if (player != game.getCurrentPlayer())
      return false;

    return game.makeMove(row, col);
  }

  public boolean isValidMove(int row, int col, ClientSocket client) {
    Player player = getClientPlayer(client);
    if (player != game.getCurrentPlayer())
      return false;

    return game.isValidMove(row, col);
  }

  public ReversiGame getGame() { return game; }
}
