package com.reversi.server;

import com.reversi.common.Event;
import com.reversi.common.Message;
import java.time.LocalTime;

public class ClientMessage implements Event {
  private final LocalTime time;
  private final Message msg;
  private final ClientHandler handler; // handler to which this event comes from

  ClientMessage(Message msg, ClientHandler handler) {
    this(LocalTime.now(), msg, handler);
  }
  ClientMessage(LocalTime time, Message msg, ClientHandler handler) {
    this.time = time;
    this.msg = msg;
    this.handler = handler;
  }

  @Override
  public LocalTime getTimestamp() {
    return this.time;
  }

  public Message getMessage() { return this.msg; }

  public ClientHandler getHandler() { return this.handler; }
}
