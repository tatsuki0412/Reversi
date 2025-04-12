package com.reversi.server;

import com.reversi.common.Event;
import com.reversi.common.Message;

public class ClientMessage extends Event {
  private final Message msg;
  private final ClientSocket handler; // handler to which this event comes from

  ClientMessage(Message msg, ClientSocket handler) {
    super();
    this.msg = msg;
    this.handler = handler;
  }

  public Message getMessage() { return this.msg; }

  public ClientSocket getHandler() { return this.handler; }
}
