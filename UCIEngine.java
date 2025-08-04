import java.util.*;

public class UCIEngine {
    private ChessBoard board;
    private RandomEngine engine;
    private boolean debug = false;
    
    public UCIEngine() {
        board = new ChessBoard();
        engine = new RandomEngine();
        // Set up starting position
        board.setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            
            if (line.isEmpty()) continue;
            
            String[] tokens = line.split("\\s+");
            String command = tokens[0];
            
            switch (command) {
                case "uci":
                    handleUCI();
                    break;
                case "debug":
                    handleDebug(tokens);
                    break;
                case "isready":
                    handleIsReady();
                    break;
                case "setoption":
                    handleSetOption(tokens);
                    break;
                case "register":
                    handleRegister();
                    break;
                case "ucinewgame":
                    handleUCINewGame();
                    break;
                case "position":
                    handlePosition(tokens);
                    break;
                case "go":
                    handleGo(tokens);
                    break;
                case "stop":
                    handleStop();
                    break;
                case "ponderhit":
                    handlePonderHit();
                    break;
                case "quit":
                    handleQuit();
                    return;
                default:
                    if (debug) {
                        System.out.println("info string Unknown command: " + command);
                    }
                    break;
            }
        }
    }
    
    private void handleUCI() {
        System.out.println("id name RandomChessEngine 1.0");
        System.out.println("id author YourName");
        // You can add options here if needed
        // System.out.println("option name Hash type spin default 64 min 1 max 1024");
        System.out.println("uciok");
    }
    
    private void handleDebug(String[] tokens) {
        if (tokens.length > 1) {
            debug = tokens[1].equals("on");
        }
    }
    
    private void handleIsReady() {
        System.out.println("readyok");
    }
    
    private void handleSetOption(String[] tokens) {
        // Handle engine options here if you have any
        // Format: setoption name <name> value <value>
    }
    
    private void handleRegister() {
        // For free engines, just ignore this
    }
    
    private void handleUCINewGame() {
        // Reset for new game
        board = new ChessBoard();
        board.setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }
    
    private void handlePosition(String[] tokens) {
        if (tokens.length < 2) return;
        
        if (tokens[1].equals("startpos")) {
            board.setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            
            // Apply moves if provided
            int movesIndex = -1;
            for (int i = 2; i < tokens.length; i++) {
                if (tokens[i].equals("moves")) {
                    movesIndex = i + 1;
                    break;
                }
            }
            
            if (movesIndex != -1) {
                for (int i = movesIndex; i < tokens.length; i++) {
                    makeUCIMove(tokens[i]);
                }
            }
        } else if (tokens[1].equals("fen")) {
            // Extract FEN string
            StringBuilder fenBuilder = new StringBuilder();
            int movesIndex = -1;
            
            for (int i = 2; i < tokens.length; i++) {
                if (tokens[i].equals("moves")) {
                    movesIndex = i + 1;
                    break;
                }
                if (i > 2) fenBuilder.append(" ");
                fenBuilder.append(tokens[i]);
            }
            
            board.setFromFEN(fenBuilder.toString());
            
            // Apply moves if provided
            if (movesIndex != -1) {
                for (int i = movesIndex; i < tokens.length; i++) {
                    makeUCIMove(tokens[i]);
                }
            }
        }
    }
    
    private void makeUCIMove(String uciMove) {
        // Convert UCI move (e.g., "e2e4", "e7e8q") to internal move format
        if (uciMove.length() < 4) return;
        
        String fromSquare = uciMove.substring(0, 2);
        String toSquare = uciMove.substring(2, 4);
        
        Position from = new Position(fromSquare);
        Position to = new Position(toSquare);
        
        // Check for promotion
        PieceType promotion = null;
        if (uciMove.length() == 5) {
            char promChar = uciMove.charAt(4);
            switch (promChar) {
                case 'q': promotion = PieceType.QUEEN; break;
                case 'r': promotion = PieceType.ROOK; break;
                case 'b': promotion = PieceType.BISHOP; break;
                case 'n': promotion = PieceType.KNIGHT; break;
            }
        }
        
        // Find the matching legal move
        Color currentColor = getCurrentPlayerFromBoard();
        List<Move> legalMoves = board.getAllLegalMoves(currentColor);
        
        for (Move move : legalMoves) {
            if (move.from.equals(from) && move.to.equals(to)) {
                if (promotion == null || move.promotionPiece == promotion) {
                    board.makeMove(move);
                    return;
                }
            }
        }
        
        if (debug) {
            System.out.println("info string Illegal move: " + uciMove);
        }
    }
    
    private Color getCurrentPlayerFromBoard() {
        // Simple heuristic: count moves to determine current player
        // In a real implementation, you'd track this properly
        int whitePieces = 0, blackPieces = 0;
        
        for (int square = 21; square <= 98; square++) {
            if ((square % 10) < 1 || (square % 10) > 8) continue;
            Piece piece = board.getPiece(new Position(square));
            if (piece != null) {
                if (piece.getColor() == Color.WHITE) whitePieces++;
                else blackPieces++;
            }
        }
        
        // This is a rough heuristic - in practice you'd track the current player
        return Color.WHITE; // Default to white for simplicity
    }
    
    private void handleGo(String[] tokens) {
        // Parse go command parameters
        long wtime = 0, btime = 0, winc = 0, binc = 0;
        int depth = 0, movetime = 0;
        boolean infinite = false;
        
        for (int i = 1; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "wtime":
                    if (i + 1 < tokens.length) wtime = Long.parseLong(tokens[++i]);
                    break;
                case "btime":
                    if (i + 1 < tokens.length) btime = Long.parseLong(tokens[++i]);
                    break;
                case "winc":
                    if (i + 1 < tokens.length) winc = Long.parseLong(tokens[++i]);
                    break;
                case "binc":
                    if (i + 1 < tokens.length) binc = Long.parseLong(tokens[++i]);
                    break;
                case "depth":
                    if (i + 1 < tokens.length) depth = Integer.parseInt(tokens[++i]);
                    break;
                case "movetime":
                    if (i + 1 < tokens.length) movetime = Integer.parseInt(tokens[++i]);
                    break;
                case "infinite":
                    infinite = true;
                    break;
            }
        }
        
        // Get the best move from engine
        Color currentColor = getCurrentPlayerFromBoard();
        Move bestMove = engine.getBestMove(board, currentColor);
        
        if (bestMove != null) {
            String uciMove = moveToUCI(bestMove);
            System.out.println("bestmove " + uciMove);
        } else {
            // No legal moves
            System.out.println("bestmove (none)");
        }
    }
    
    private String moveToUCI(Move move) {
        String uciMove = move.from.toNotation() + move.to.toNotation();
        
        // Add promotion piece if applicable
        if (move.promotionPiece != null) {
            switch (move.promotionPiece) {
                case QUEEN: uciMove += "q"; break;
                case ROOK: uciMove += "r"; break;
                case BISHOP: uciMove += "b"; break;
                case KNIGHT: uciMove += "n"; break;
            }
        }
        
        return uciMove;
    }
    
    private void handleStop() {
        // Stop thinking and return best move found so far
        // For a simple engine like this, we don't have background thinking
    }
    
    private void handlePonderHit() {
        // Switch from pondering to normal search
        // Not implemented for this simple engine
    }
    
    private void handleQuit() {
        // Clean shutdown
        System.exit(0);
    }
    
    public static void main(String[] args) {
        UCIEngine engine = new UCIEngine();
        engine.run();
    }
}