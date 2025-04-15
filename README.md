
## Obtain the Code:

```bash
git clone https://github.com/Mickey-snow/Reversi.git
cd Reversi/
```

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
mvn exec:java -Dexec.mainClass=com.reversi.client.ReversiApp
```

## Run unit tests:

```bash
mvn clean test
```

## Build documentation

```bash
mvn javadoc:javadoc
```
