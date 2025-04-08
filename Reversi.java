import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Reversi {

    // Board dimensions (8x8 is standard for Reversi)
    private static final int BOARD_SIZE = 8;

    // Board representation using characters:
    // 'B' for Black, 'W' for White, and '.' for an empty space.
    private char[][] board = new char[BOARD_SIZE][BOARD_SIZE];

    // Directions to search for a valid move and to flip discs (N, NE, E, SE, S, SW, W, NW)
    private static final int[][] DIRECTIONS = {
        {-1,  0}, // North
        {-1,  1}, // North East
        { 0,  1}, // East
        { 1,  1}, // South East
        { 1,  0}, // South
        { 1, -1}, // South West
        { 0, -1}, // West
        {-1, -1}  // North West
    };

    // Entry point of the game
    public static void main(String[] args) {
        Reversi game = new Reversi();
        game.initializeBoard();
        game.playGame();
    }

    // Initialize the board with starting pieces for Reversi
    private void initializeBoard() {
        // Fill board with empty spaces represented by '.'
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = '.';
            }
        }
        // Set the four initial discs in the middle of the board.
        board[3][3] = 'W';
        board[3][4] = 'B';
        board[4][3] = 'B';
        board[4][4] = 'W';
    }

    // Main game loop handling player turns
    private void playGame() {
        Scanner scanner = new Scanner(System.in);
        char currentPlayer = 'B';
        boolean gameEnded = false;

        while (!gameEnded) {
            printBoard();
            List<int[]> validMoves = getValidMoves(currentPlayer);
            if (validMoves.isEmpty()) {
                // No valid moves for current player.
                System.out.println("No valid moves available for player " + currentPlayer + ".");
                // Check if the opponent has valid moves, else end game.
                char opponent = (currentPlayer == 'B') ? 'W' : 'B';
                if (getValidMoves(opponent).isEmpty()) {
                    gameEnded = true;
                    continue;
                } else {
                    // Skip turn for current player
                    System.out.println("Skipping turn.");
                    currentPlayer = opponent;
                    continue;
                }
            }

            System.out.println("Player " + currentPlayer + ", enter your move as row and column (0-indexed, space separated):");
            int row, col;
            while (true) {
                String input = scanner.nextLine();
                String[] tokens = input.trim().split("\\s+");
                if (tokens.length != 2) {
                    System.out.println("Invalid input. Please enter two integers separated by a space.");
                    continue;
                }
                try {
                    row = Integer.parseInt(tokens[0]);
                    col = Integer.parseInt(tokens[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter integer values.");
                    continue;
                }
                if (!isWithinBounds(row, col)) {
                    System.out.println("Move out of bounds. Please enter a valid move.");
                    continue;
                }
                if (!isValidMove(row, col, currentPlayer)) {
                    System.out.println("Invalid move. That move does not capture any opponent discs. Try again.");
                    continue;
                }
                break;
            }
            // Execute the move and flip opponent discs.
            makeMove(row, col, currentPlayer);
            // Switch players.
            currentPlayer = (currentPlayer == 'B') ? 'W' : 'B';
        }
        // Game has ended.
        printBoard();
        int blackCount = 0, whiteCount = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 'B') blackCount++;
                else if (board[i][j] == 'W') whiteCount++;
            }
        }
        System.out.println("Game Over!");
        System.out.println("Black: " + blackCount + " White: " + whiteCount);
        if (blackCount > whiteCount) System.out.println("Black wins!");
        else if (whiteCount > blackCount) System.out.println("White wins!");
        else System.out.println("It's a draw!");

        scanner.close();
    }

    // Print the current board state to the console.
    private void printBoard() {
        System.out.println("  0 1 2 3 4 5 6 7");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Check if given row and col are within board boundaries.
    private boolean isWithinBounds(int row, int col) {
        return (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE);
    }

    // Returns a list of valid moves for the given player.
    // Each move is represented as an int array of length 2: {row, col}
    private List<int[]> getValidMoves(char player) {
        List<int[]> validMoves = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == '.' && isValidMove(row, col, player)) {
                    validMoves.add(new int[]{row, col});
                }
            }
        }
        return validMoves;
    }

    // Check if placing a disc at (row, col) for the specified player is a legal move.
    private boolean isValidMove(int row, int col, char player) {
        // Must place on an empty square.
        if (board[row][col] != '.') {
            return false;
        }
        // Check all directions for a legal capture.
        for (int[] d : DIRECTIONS) {
            int r = row + d[0];
            int c = col + d[1];
            boolean hasOpponentBetween = false;
            // Determine the opponent's disc
            char opponent = (player == 'B') ? 'W' : 'B';

            // Skip if out of bounds or next to an empty square or our own piece.
            if (!isWithinBounds(r, c) || board[r][c] != opponent) {
                continue;
            }
            // There is at least one opponent piece next to our move; continue along the direction.
            r += d[0];
            c += d[1];
            while (isWithinBounds(r, c)) {
                if (board[r][c] == opponent) {
                    // Still encountering opponent discs.
                    r += d[0];
                    c += d[1];
                } else if (board[r][c] == player) {
                    // Found one of our own pieces with opponent(s) between.
                    hasOpponentBetween = true;
                    break;
                } else { // board[r][c] == '.'
                    break;
                }
            }
            if (hasOpponentBetween) {
                return true;
            }
        }
        return false;
    }

    // Execute the move for the player by placing a disc and flipping opponent discs.
    private void makeMove(int row, int col, char player) {
        board[row][col] = player;
        // Check each direction for pieces to flip.
        for (int[] d : DIRECTIONS) {
            List<int[]> discsToFlip = new ArrayList<>();
            int r = row + d[0];
            int c = col + d[1];
            char opponent = (player == 'B') ? 'W' : 'B';

            // Continue in the direction while encountering opponent's discs.
            while (isWithinBounds(r, c) && board[r][c] == opponent) {
                discsToFlip.add(new int[]{r, c});
                r += d[0];
                c += d[1];
            }
            // If we stopped because we found our own disc, flip the pieces in between.
            if (isWithinBounds(r, c) && board[r][c] == player) {
                for (int[] disc : discsToFlip) {
                    board[disc[0]][disc[1]] = player;
                }
            }
        }
    }
}
