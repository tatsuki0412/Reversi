package com.reversi.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerMain {
    public static final int PORT = 5000;
    private List<ClientHandler> clients = new ArrayList<>();
    private GameSession gameSession;

    public static void main(String[] args) {
        new ServerMain().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            // Accept exactly two client connections for a single game session.
            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                handler.start();
                System.out.println("Client connected. Total clients: " + clients.size());
            }
            // Once two clients are connected, start the game session.
            gameSession = new GameSession(clients.get(0), clients.get(1));
            gameSession.startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public GameSession getGameSession() {
        return gameSession;
    }
}
