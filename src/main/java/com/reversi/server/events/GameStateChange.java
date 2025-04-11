package com.reversi.server.events;

import com.reversi.common.Event;
import com.reversi.server.GameSession;
import java.time.LocalTime;

public class GameStateChange implements Event {
  private final LocalTime timestamp;
  private final GameSession session;

  public GameStateChange(GameSession session) {
    this.timestamp = LocalTime.now();
    this.session = session;
  }

  public GameSession getSession() { return session; }

  @Override
  public LocalTime getTimestamp() {
    return timestamp;
  }
}
