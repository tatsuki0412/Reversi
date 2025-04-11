package com.reversi.server.events;

import com.reversi.common.Event;
import com.reversi.server.GameSession;

public class GameStateChange extends Event {
  private final GameSession session;

  public GameStateChange(GameSession session) {
    super();
    this.session = session;
  }

  public GameSession getSession() { return session; }
}
