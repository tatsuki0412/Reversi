package com.reversi.client;

import com.reversi.common.Event;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMessage implements Event {
  private static final Logger logger =
      LoggerFactory.getLogger(ServerMessage.class);

  private final LocalTime time;
  private final String msg;

  // message type enum
  public enum Type { UNKNOWN, START, BOARD, TURN, INVALID }
  private final Type type;

  ServerMessage(String msg) { this(LocalTime.now(), msg); }
  ServerMessage(LocalTime time, String msg) {
    this.time = time;

    // parse message
    if (msg.startsWith("START:")) {
      this.type = Type.START;
      this.msg = msg.substring(6).trim();
    } else if (msg.startsWith("BOARD:")) {
      this.type = Type.BOARD;
      this.msg = msg.substring(6);
    } else if (msg.startsWith("TURN:")) {
      this.type = Type.TURN;
      this.msg = msg.substring(5).trim();
    } else if (msg.startsWith("INVALID")) {
      this.type = Type.INVALID;
      this.msg = new String(); // empty
    } else {
      this.msg = msg;
      this.type = Type.UNKNOWN;
      logger.warn("Unable to parse: {}", msg);
    }
  }

  @Override
  public LocalTime getTimestamp() {
    return this.time;
  }

  public String getMessage() { return this.msg; }

  public Type getType() { return type; }
}
