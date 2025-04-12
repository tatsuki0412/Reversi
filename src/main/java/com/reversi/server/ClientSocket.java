package com.reversi.server;

import com.reversi.common.EventBus;
import com.reversi.common.JacksonObjMapper;
import com.reversi.common.Message;
import java.io.*;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSocket implements Runnable {
  private static final Logger logger =
      LoggerFactory.getLogger(ClientSocket.class);

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private EventBus eventBus;
  private int id;

  public ClientSocket(int id, Socket s, EventBus eventBus) {
    this.id = id;
    this.socket = s;
    this.eventBus = eventBus;

    // Establish input/output stream with the client
    try {
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      logger.error("Error initializing streams for client {}", id, e);
    }
  }

  public int getClientId() { return id; }

  public void sendMessage(Message msg) {
    try {
      out.println(JacksonObjMapper.get().writeValueAsString(msg));
    } catch (Exception e) {
      logger.error("Failed to send message: {}", msg.toString());
    }
  }

  @Override
  public void run() {
    try {
      String line;
      // Listen for incoming messages from the client.
      while ((line = in.readLine()) != null) {
        logger.info("Received from client {}: {}", id, line);
        Message msg;
        try {
          msg = JacksonObjMapper.get().readValue(line, Message.class);
          eventBus.post(new ClientMessage(msg, this));
        } catch (Exception e) {
          logger.error("Failed to process received data: {}\n", line, e);
        }
      }
    } catch (IOException e) {
      logger.error("Connection with client {} lost.", id, e);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        logger.error("Error closing socket for client {}", id, e);
      }
    }
  }
}
