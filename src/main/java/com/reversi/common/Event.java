package com.reversi.common;

import java.time.LocalTime;

public interface Event {
  /**
   * Returns the timestamp when the event was created.
   * Useful for logging or comparing event times.
   */
  LocalTime getTimestamp();
}
