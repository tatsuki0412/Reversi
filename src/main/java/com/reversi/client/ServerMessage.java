package com.reversi.client;

import com.reversi.common.Event;
import com.reversi.common.Message;
import java.time.LocalTime;

public class ServerMessage implements Event {
  private final LocalTime time;
  private final Message msg;

  ServerMessage(Message msg) { this(LocalTime.now(), msg); }
  ServerMessage(LocalTime time, Message msg) {
    this.time = time;
    this.msg = msg;
  }

  @Override
  public LocalTime getTimestamp() {
    return this.time;
  }

  public Message getMessage() { return this.msg; }
}
