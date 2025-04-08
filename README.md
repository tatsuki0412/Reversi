

## Compile the Code:

Organize the files in directories that match their package names. For example, in your source folder:

```text
src/com/reversi/server/ServerMain.java
src/com/reversi/server/GameSession.java
src/com/reversi/server/ClientHandler.java  
src/com/reversi/client/ClientMain.java  
src/com/reversi/client/GameController.java  
src/com/reversi/client/GameView.java  
```

Then compile with:

```bash
javac -d bin src/com/reversi/server/*.java src/com/reversi/client/*.java
```

## Run the Server:

In one terminal window, run:

```bash
java -cp bin com.reversi.server.ServerMain
```

## Run the Clients:

In two separate terminal windows, run:

```bash
java -cp bin com.reversi.client.ClientMain
```
