package com.reversi.common;

import java.time.LocalTime;

public class Event {
  private final LocalTime timestamp;

  public Event(LocalTime timestamp) { this.timestamp = timestamp; }
  public Event() { this(LocalTime.now()); }

  /**
   * Returns the timestamp when the event was created.
   * Useful for logging or comparing event times.
   */
  public LocalTime getTimestamp() { return timestamp; }
}
