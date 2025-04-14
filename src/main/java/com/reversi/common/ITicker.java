package com.reversi.common;

public interface ITicker {
  void start(Runnable onTick, long tickIntervalMillis);
  void stop();
}
