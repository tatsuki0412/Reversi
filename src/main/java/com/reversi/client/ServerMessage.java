package com.reversi.client;

import com.reversi.common.Event;
import com.reversi.common.Message;

public class ServerMessage extends Event {
  private final Message msg;

  ServerMessage(Message msg) {
    super();
    this.msg = msg;
  }

  public Message getMessage() { return this.msg; }
}
