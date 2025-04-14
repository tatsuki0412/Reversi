package com.reversi.common;

import java.util.Timer;
import java.util.TimerTask;

public class Ticker implements ITicker {
  private Timer timer;

  public Ticker() { this.timer = new Timer(); }

  @Override
  public void start(Runnable onTick, long tickIntervalMillis) {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        onTick.run();
      }
    }, 0, tickIntervalMillis);
  }

  @Override
  public void stop() {
    if (timer != null) {
      timer.cancel();
    }
  }
}
