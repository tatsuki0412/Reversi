

## Compile the Code:

```bash
mvn clean compile
```

## Run the Server:

In one terminal window, run:

```bash
mvn exec:java -Dexec.mainClass=com.reversi.server.ServerMain
```

## Run the Clients:

In two separate terminal windows, run:

```bash
mvn exec:java -Dexec.mainClass=com.reversi.client.ClientMain
```
