import java.util.*;

public class ChessGameWithEngine {
    private ChessBoard board;
    private Color currentPlayer;
    private Scanner scanner;
    private ChessEngine engine;
    private boolean playAgainstEngine;
    private Color humanColor;
    private Color engineColor;
    private StringBuilder moveHistory = new StringBuilder();
    private int moveCount = 1; // Add this as a class field

    public ChessGameWithEngine() {
        board = new ChessBoard();
        currentPlayer = Color.WHITE; // Game always starts with white
        scanner = new Scanner(System.in);
        engine = new ChessEngine();
        playAgainstEngine = false;
    }

    public void play() {
        System.out.println("Welcome to Chess!");
        System.out.println("1. Human vs Human");
        System.out.println("2. Human vs Engine");
        System.out.print("Choose mode (1 or 2): ");
        
        String choice = scanner.nextLine().trim();
        
        if (choice.equals("2")) {
            playAgainstEngine = true;
            System.out.print("Choose your color (1(white)/2(black)): ");
            String colorChoice = scanner.nextLine().trim();
            
            if (colorChoice.equals("2")) {
                humanColor = Color.BLACK;
                engineColor = Color.WHITE;
                System.out.println("You are playing as BLACK");
                System.out.println("Engine is playing as WHITE");
            } else {
                humanColor = Color.WHITE;
                engineColor = Color.BLACK;
                System.out.println("You are playing as WHITE");
                System.out.println("Engine is playing as BLACK");
            }
        }

        System.out.println("\nGame starting...");
        System.out.println("Enter moves in format: e2-e4 (from-to)");
        System.out.println("Type 'quit' to exit, 'help' for commands, 'moves' to see legal moves\n");

        boolean gameEnded = false;
        while (!gameEnded) {
            board.display();
            System.out.println();
            
            // Check game state
            if (board.isInCheck(currentPlayer)) {
                System.out.println(currentPlayer + " is in CHECK!");
            }
            
            if (board.isCheckmate(currentPlayer)) {
                Color winner = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
                System.out.println("CHECKMATE! " + winner + " wins!");
                gameEnded = true;
                break;
            }
            
            if (board.isStalemate(currentPlayer)) {
                System.out.println("STALEMATE! Game is a draw.");
                gameEnded = true;
                break;
            }

            // Determine if it's engine's turn
            if (playAgainstEngine && currentPlayer == engineColor) {
                System.out.println("Engine (" + engineColor + ") is thinking...");
                List<Move> availableMoves = board.getAllLegalMoves(currentPlayer);
                if (availableMoves.isEmpty()) {
                    System.out.println("No legal moves available for engine!");
                    gameEnded = true;
                    break;
                }
                Move engineMove = engine.getBestMove(board, currentPlayer);
                Move actualMove = null;
                for (Move move : availableMoves) {
                    if (move.from.equals(engineMove.from) && move.to.equals(engineMove.to)) {
                        actualMove = move;
                        break;
                    }
                }
                if (actualMove == null) {
                    actualMove = availableMoves.get(0);
                }
                System.out.println("Engine plays: " + actualMove);
                board.makeMove(actualMove);
                recordMove(actualMove, null);
                currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
                continue;
            }

            // Human player's turn
            System.out.print(currentPlayer + " to move: ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Game ended by player.");
                gameEnded = true;
                break;
            } else if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            } else if (input.equalsIgnoreCase("moves")) {
                showLegalMoves();
                continue;
            }
            
            Move lastMove = null;
            if (processMove(input)) {
                // processMove already did makeMove, so retrieve last move
                // we passed the new Move to recordMove inside processMove
                currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
            } else {
                System.out.println("Invalid move! Try again.");
            }
        }
        
        // After game ends, print move history
        System.out.println("\nPGN:\n");
        System.out.println("[Event \"Casual Game\"]");
        System.out.println("[Site \"?\"]");
        System.out.println("[Date \"" + java.time.LocalDate.now() + "\"]");
        System.out.println("[Round \"-\"]");
        System.out.println("[White \"Player1\"]");
        System.out.println("[Black \"Player2\"]");
        String result = "*";
        if (board.isCheckmate(currentPlayer)) {
            result = (currentPlayer == Color.WHITE) ? "0-1" : "1-0";
        } else if (board.isStalemate(currentPlayer)) {
            result = "1/2-1/2";
        }
        System.out.println("[Result \"" + result + "\"]\n");
        System.out.println(moveHistory.toString().trim() + " " + result);
        System.out.println("\nThanks for playing!");
        scanner.close();
    }

    private boolean processMove(String input) {
        try {
            String[] parts = input.split("-");
            if (parts.length != 2) {
                System.out.println("Invalid format! Use: e2-e4");
                return false;
            }
            
            Move move = new Move(parts[0].trim(), parts[1].trim());
            Piece piece = board.getPiece(move.from);
            if (piece == null) {
                System.out.println("No piece at " + move.from.toNotation());
                return false;
            }
            if (piece.getColor() != currentPlayer) {
                System.out.println("That's not your piece! You are " + currentPlayer);
                return false;
            }
            List<Move> legalMoves = board.getAllLegalMoves(currentPlayer);
            Move legalMove = null;
            for (Move legal : legalMoves) {
                if (legal.from.equals(move.from) && legal.to.equals(move.to)) {
                    legalMove = legal;
                    break;
                }
            }
            if (legalMove == null) {
                System.out.println("Illegal move!");
                return false;
            }
            // Pawn promotion
            if (piece.getType() == PieceType.PAWN && (move.to.row == 0 || move.to.row == 7)) {
                System.out.print("Promote to (Q/R/B/N): ");
                String promotion = scanner.nextLine().trim().toUpperCase();
                switch (promotion) {
                    case "R": legalMove.promotionPiece = PieceType.ROOK; break;
                    case "B": legalMove.promotionPiece = PieceType.BISHOP; break;
                    case "N": legalMove.promotionPiece = PieceType.KNIGHT; break;
                    default: legalMove.promotionPiece = PieceType.QUEEN; break;
                }
            }
            board.makeMove(legalMove);
            recordMove(legalMove, piece.getType());
            return true;
        } catch (Exception e) {
            System.out.println("Invalid input format! Use: e2-e4");
            return false;
        }
    }

    private void recordMove(Move move, PieceType originalType) {
        // Determine if capture
        boolean isCapture = (move.capturedPiece != null);

        // Check/checkmate
        Color opponent = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean isCheck = board.isInCheck(opponent);
        boolean isCheckmate = board.isCheckmate(opponent);

        // Piece type
        PieceType pieceType = (originalType != null) ? originalType : board.getPiece(move.to).getType();

        // Move number
        if (currentPlayer == Color.WHITE) {
            moveHistory.append(moveCount).append(". ");
        }

        // SAN notation
        String san = toSAN(move, pieceType, isCapture, isCheck, isCheckmate);
        moveHistory.append(san).append(" ");

        if (currentPlayer == Color.BLACK) {
            moveCount++;
        }
    }

    // Returns SAN (Standard Algebraic Notation) for a move
    private String toSAN(Move move, PieceType pieceType, boolean isCapture, boolean isCheck, boolean isCheckmate) {
        // Handle castling
        if (move.isCastling) {
            if (move.to.col == 6) return "O-O" + (isCheckmate ? "#" : isCheck ? "+" : "");
            if (move.to.col == 2) return "O-O-O" + (isCheckmate ? "#" : isCheck ? "+" : "");
        }

        StringBuilder san = new StringBuilder();

        // Piece letter (empty for pawn)
        if (pieceType != PieceType.PAWN) {
            san.append(getPieceLetter(pieceType));
        }

        // Pawn captures: file letter
        if (pieceType == PieceType.PAWN && isCapture) {
            san.append((char)('a' + move.from.col));
        }

        // Capture marker
        if (isCapture) {
            san.append("x");
        }

        // Destination square
        san.append(move.to.toNotation());

        // Promotion
        if (move.promotionPiece != null) {
            san.append("=").append(getPieceLetter(move.promotionPiece));
        }

        // Check or mate
        if (isCheckmate) {
            san.append("#");
        } else if (isCheck) {
            san.append("+");
        }

        return san.toString();
    }

    // Helper to get SAN piece letter
    private String getPieceLetter(PieceType type) {
        switch (type) {
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case ROOK:   return "R";
            case QUEEN:  return "Q";
            case KING:   return "K";
            default:     return "";
        }
    }

    private void showHelp() {
        System.out.println("\nCommands:");
        System.out.println("  move format: e2-e4 (from square - to square)");
        System.out.println("  quit - exit game");
        System.out.println("  help - show this help");
        System.out.println("  moves - show all legal moves");
        System.out.println("\nPiece symbols: K=King, Q=Queen, R=Rook, B=Bishop, N=Knight, P=Pawn");
        System.out.println("Uppercase = White, Lowercase = Black\n");
    }
    
    private void showLegalMoves() {
        List<Move> legalMoves = board.getAllLegalMoves(currentPlayer);
        System.out.println("\nLegal moves for " + currentPlayer + ":");
        int count = 0;
        for (Move move : legalMoves) {
            Piece piece = board.getPiece(move.from);
            System.out.print(piece.getSymbol() + move.toString() + " ");
            count++;
            if (count % 8 == 0) System.out.println();
        }
        System.out.println("\n");
    }

    public static void main(String[] args) {
        new ChessGameWithEngine().play();
    }
}
