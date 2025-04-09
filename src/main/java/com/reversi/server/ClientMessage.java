package com.reversi.server;

import com.reversi.common.Event;
import java.time.LocalTime;

public class ClientMessage implements Event {
  LocalTime time;
  String msg;
  ClientHandler handler; // handler to which this event comes from

  ClientMessage(String msg, ClientHandler handler) {
    this(LocalTime.now(), msg, handler);
  }
  ClientMessage(LocalTime time, String msg, ClientHandler handler) {
    this.time = time;
    this.msg = msg;
    this.handler = handler;
  }

  @Override
  public LocalTime getTimestamp() {
    return this.time;
  }

  public String getMessage() { return this.msg; }

  public ClientHandler getHandler() { return this.handler; }
}
