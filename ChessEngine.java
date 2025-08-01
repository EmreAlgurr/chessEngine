import java.util.*;

// Chess Engine class that implements minimax algorithm with alpha-beta pruning
class ChessEngine {
    private static final int MAX_DEPTH = 4; // Adjustable search depth
    private static final int INFINITY = 100000;
    
    // Transposition table constants
    private static final int TT_SIZE = 1048576; // 2^20 entries (adjust as needed)
    private static final int EXACT = 0;
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 2;
    
    // Zobrist hashing random numbers
    private static final long[][][] ZOBRIST_PIECES = new long[8][8][12]; // [row][col][piece_type_color]
    private static final long ZOBRIST_BLACK_TO_MOVE = generateRandomLong();
    private static final long[] ZOBRIST_CASTLING = new long[16]; // All castling combinations
    private static final long[] ZOBRIST_EN_PASSANT = new long[8]; // En passant file
    
    // Transposition table
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TT_SIZE];
    
    static {
        // Initialize Zobrist hash values
        Random random = new Random(12345); // Fixed seed for consistency
        
        // Initialize piece position hashes
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int piece = 0; piece < 12; piece++) {
                    ZOBRIST_PIECES[row][col][piece] = random.nextLong();
                }
            }
        }
        
        // Initialize castling rights hashes
        for (int i = 0; i < 16; i++) {
            ZOBRIST_CASTLING[i] = random.nextLong();
        }
        
        // Initialize en passant file hashes
        for (int i = 0; i < 8; i++) {
            ZOBRIST_EN_PASSANT[i] = random.nextLong();
        }
    }
    
    // Piece values for evaluation
    private static final Map<PieceType, Integer> PIECE_VALUES = new HashMap<PieceType, Integer>() {{
        put(PieceType.PAWN, 100);
        put(PieceType.KNIGHT, 320);
        put(PieceType.BISHOP, 330);
        put(PieceType.ROOK, 500);
        put(PieceType.QUEEN, 900);
        put(PieceType.KING, 20000);
    }};
    
    // Piece-square tables for positional evaluation
    private static final int[][] PAWN_TABLE = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5,  5, 10, 25, 25, 10,  5,  5},
        {0,  0,  0, 20, 20,  0,  0,  0},
        {5, -5,-10,  0,  0,-10, -5,  5},
        {5, 10, 10,-20,-20, 10, 10,  5},
        {0,  0,  0,  0,  0,  0,  0,  0}
    };
    
    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };
    
    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };
    
    private static final int[][] ROOK_TABLE = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {5, 10, 10, 10, 10, 10, 10,  5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {0,  0,  0,  5,  5,  0,  0,  0}
    };
    
    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-5,  0,  5,  5,  5,  5,  0, -5},
        {0,  0,  5,  5,  5,  5,  0, -5},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };
    
    private static final int[][] KING_MIDGAME_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        {20, 20,  0,  0,  0,  0, 20, 20},
        {20, 30, 10,  0,  0, 10, 30, 20}
    };
    
    // Get the best move for the given color using minimax with alpha-beta pruning
    public Move getBestMove(ChessBoard board, Color color) {
        System.out.println("Engine analyzing position for " + color + "...");
        
        // Clear transposition table for new search
        Arrays.fill(transpositionTable, null);
        
        // Get all legal moves first
        List<Move> legalMoves = board.getAllLegalMoves(color);
        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves available!");
            return null;
        }
        
        System.out.println("Found " + legalMoves.size() + " legal moves");
        
        // Validate each move before evaluation
        List<Move> validMoves = new ArrayList<>();
        for (Move move : legalMoves) {
            if (isMoveSafe(board, move, color)) {
                validMoves.add(move);
            }
        }
        
        if (validMoves.isEmpty()) {
            System.out.println("No safe moves found! Using first legal move as fallback.");
            return legalMoves.get(0);
        }
        
        System.out.println("Found " + validMoves.size() + " safe moves");
        
        try {
            long zobristHash = calculateZobristHash(board);
            MoveEvaluation result = minimax(board, MAX_DEPTH, -INFINITY, INFINITY, true, color, zobristHash);
            
            if (result.move == null) {
                System.out.println("Minimax returned null, using first safe move");
                return validMoves.get(0);
            }
            
            // Double-check that the returned move is actually legal
            boolean isLegal = false;
            for (Move legal : legalMoves) {
                if (legal.from.equals(result.move.from) && legal.to.equals(result.move.to)) {
                    isLegal = true;
                    break;
                }
            }
            
            if (!isLegal) {
                System.out.println("Minimax returned illegal move, using first safe move");
                return validMoves.get(0);
            }
            
            System.out.println("Engine selected move: " + result.move + " (score: " + result.evaluation + ")");
            return result.move;
            
        } catch (Exception e) {
            System.out.println("Error in minimax: " + e.getMessage());
            e.printStackTrace();
            return validMoves.get(0);
        }
    }
    
    // Check if a move is safe (doesn't leave king in check)
    private boolean isMoveSafe(ChessBoard board, Move move, Color color) {
        try {
            board.makeMove(move);
            boolean inCheck = board.isInCheck(color);
            board.undoMove(move);
            return !inCheck;
        } catch (Exception e) {
            // If move causes exception, it's not safe
            try {
                board.undoMove(move);
            } catch (Exception ignored) {}
            return false;
        }
    }
    
    // Minimax algorithm with alpha-beta pruning and transposition table
    private MoveEvaluation minimax(ChessBoard board, int depth, int alpha, int beta, boolean isMaximizing, Color engineColor, long zobristHash) {
        // Check transposition table
        int ttIndex = (int)(zobristHash & (TT_SIZE - 1));
        TranspositionEntry ttEntry = transpositionTable[ttIndex];
        
        if (ttEntry != null && ttEntry.zobristKey == zobristHash && ttEntry.depth >= depth) {
            if (ttEntry.flag == EXACT) {
                return new MoveEvaluation(ttEntry.bestMove, ttEntry.score);
            } else if (ttEntry.flag == LOWER_BOUND && ttEntry.score >= beta) {
                return new MoveEvaluation(ttEntry.bestMove, ttEntry.score);
            } else if (ttEntry.flag == UPPER_BOUND && ttEntry.score <= alpha) {
                return new MoveEvaluation(ttEntry.bestMove, ttEntry.score);
            }
        }
        
        Color currentColor = isMaximizing ? engineColor : (engineColor == Color.WHITE ? Color.BLACK : Color.WHITE);
        
        // Base case: reached maximum depth or game is over
        if (depth == 0 || board.isCheckmate(currentColor) || board.isStalemate(currentColor)) {
            int eval = evaluateBoard(board, engineColor);
            return new MoveEvaluation(null, eval);
        }
        
        List<Move> moves = board.getAllLegalMoves(currentColor);
        if (moves.isEmpty()) {
            int eval = evaluateBoard(board, engineColor);
            return new MoveEvaluation(null, eval);
        }
        
        // Filter out moves that leave the king in check
        List<Move> safeMoves = new ArrayList<>();
        for (Move move : moves) {
            if (isMoveSafe(board, move, currentColor)) {
                safeMoves.add(move);
            }
        }
        
        if (safeMoves.isEmpty()) {
            // If no safe moves, we're in checkmate or have a bug
            int eval = isMaximizing ? -INFINITY : INFINITY;
            return new MoveEvaluation(null, eval);
        }
        
        // Order moves for better alpha-beta pruning (TT move first if available)
        if (ttEntry != null && ttEntry.bestMove != null) {
            // Move TT best move to front
            for (int i = 0; i < safeMoves.size(); i++) {
                Move move = safeMoves.get(i);
                if (move.from.equals(ttEntry.bestMove.from) && move.to.equals(ttEntry.bestMove.to)) {
                    safeMoves.remove(i);
                    safeMoves.add(0, move);
                    break;
                }
            }
        }
        orderMoves(safeMoves, board);
        
        Move bestMove = safeMoves.get(0);
        int originalAlpha = alpha;
        
        if (isMaximizing) {
            int maxEval = -INFINITY;
            for (Move move : safeMoves) {
                try {
                    // Calculate new zobrist hash after move
                    long newZobristHash = updateZobristHash(zobristHash, board, move);
                    
                    // Make the move
                    board.makeMove(move);
                    
                    // Recursively evaluate
                    MoveEvaluation eval = minimax(board, depth - 1, alpha, beta, false, engineColor, newZobristHash);
                    
                    // Undo the move
                    board.undoMove(move);
                    
                    if (eval.evaluation > maxEval) {
                        maxEval = eval.evaluation;
                        bestMove = move;
                    }
                    
                    alpha = Math.max(alpha, eval.evaluation);
                    if (beta <= alpha) {
                        break; // Alpha-beta pruning
                    }
                } catch (Exception e) {
                    // If move causes exception, skip it
                    try {
                        board.undoMove(move);
                    } catch (Exception ignored) {}
                    continue;
                }
            }
            
            // Store in transposition table
            int flag = EXACT;
            if (maxEval <= originalAlpha) {
                flag = UPPER_BOUND;
            } else if (maxEval >= beta) {
                flag = LOWER_BOUND;
            }
            transpositionTable[ttIndex] = new TranspositionEntry(zobristHash, depth, maxEval, flag, bestMove);
            
            return new MoveEvaluation(bestMove, maxEval);
        } else {
            int minEval = INFINITY;
            for (Move move : safeMoves) {
                try {
                    // Calculate new zobrist hash after move
                    long newZobristHash = updateZobristHash(zobristHash, board, move);
                    
                    // Make the move
                    board.makeMove(move);
                    
                    // Recursively evaluate
                    MoveEvaluation eval = minimax(board, depth - 1, alpha, beta, true, engineColor, newZobristHash);
                    
                    // Undo the move
                    board.undoMove(move);
                    
                    if (eval.evaluation < minEval) {
                        minEval = eval.evaluation;
                        bestMove = move;
                    }
                    
                    beta = Math.min(beta, eval.evaluation);
                    if (beta <= alpha) {
                        break; // Alpha-beta pruning
                    }
                } catch (Exception e) {
                    // If move causes exception, skip it
                    try {
                        board.undoMove(move);
                    } catch (Exception ignored) {}
                    continue;
                }
            }
            
            // Store in transposition table
            int flag = EXACT;
            if (minEval <= originalAlpha) {
                flag = UPPER_BOUND;
            } else if (minEval >= beta) {
                flag = LOWER_BOUND;
            }
            transpositionTable[ttIndex] = new TranspositionEntry(zobristHash, depth, minEval, flag, bestMove);
            
            return new MoveEvaluation(bestMove, minEval);
        }
    }
    
    // Calculate initial Zobrist hash for a board position
    private long calculateZobristHash(ChessBoard board) {
        long hash = 0;
        
        // Hash pieces on board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    int pieceIndex = getPieceIndex(piece);
                    hash ^= ZOBRIST_PIECES[row][col][pieceIndex];
                }
            }
        }
        
        // Hash side to move (assuming we can get this from board)
        // Note: You may need to adapt this based on your ChessBoard implementation
        try {
            // If it's black's turn, XOR with the black-to-move constant
            // This is a placeholder - adapt based on your board's API
            // hash ^= ZOBRIST_BLACK_TO_MOVE;
        } catch (Exception ignored) {}
        
        // Hash castling rights and en passant (if available in your implementation)
        // These would need to be adapted based on your ChessBoard class
        
        return hash;
    }
    
    // Update Zobrist hash incrementally after a move
    private long updateZobristHash(long currentHash, ChessBoard board, Move move) {
        long newHash = currentHash;
        
        // Remove piece from source square
        Piece movingPiece = board.getPiece(move.from);
        if (movingPiece != null) {
            int pieceIndex = getPieceIndex(movingPiece);
            newHash ^= ZOBRIST_PIECES[move.from.row][move.from.col][pieceIndex];
        }
        
        // Remove captured piece from destination square (if any)
        Piece capturedPiece = board.getPiece(move.to);
        if (capturedPiece != null) {
            int pieceIndex = getPieceIndex(capturedPiece);
            newHash ^= ZOBRIST_PIECES[move.to.row][move.to.col][pieceIndex];
        }
        
        // Add piece to destination square
        if (movingPiece != null) {
            int pieceIndex = getPieceIndex(movingPiece);
            newHash ^= ZOBRIST_PIECES[move.to.row][move.to.col][pieceIndex];
        }
        
        // Toggle side to move
        newHash ^= ZOBRIST_BLACK_TO_MOVE;
        
        return newHash;
    }
    
    // Convert piece to index for Zobrist table
    private int getPieceIndex(Piece piece) {
        int colorOffset = piece.getColor() == Color.WHITE ? 0 : 6;
        switch (piece.getType()) {
            case PAWN: return 0 + colorOffset;
            case KNIGHT: return 1 + colorOffset;
            case BISHOP: return 2 + colorOffset;
            case ROOK: return 3 + colorOffset;
            case QUEEN: return 4 + colorOffset;
            case KING: return 5 + colorOffset;
            default: return 0;
        }
    }
    
    // Generate random long for Zobrist initialization
    private static long generateRandomLong() {
        Random random = new Random(12345);
        return random.nextLong();
    }
    
    // Evaluate the current board position
    private int evaluateBoard(ChessBoard board, Color engineColor) {
        int score = 0;
        
        // Check for checkmate/stalemate
        if (board.isCheckmate(engineColor)) {
            return -INFINITY + 1000; // Avoid immediate checkmate
        }
        if (board.isCheckmate(engineColor == Color.WHITE ? Color.BLACK : Color.WHITE)) {
            return INFINITY - 1000; // Prefer quick checkmate
        }
        if (board.isStalemate(engineColor) || board.isStalemate(engineColor == Color.WHITE ? Color.BLACK : Color.WHITE)) {
            return 0; // Stalemate is neutral
        }
        
        // Evaluate all pieces on the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    int pieceValue = evaluatePiece(piece, row, col);
                    if (piece.getColor() == engineColor) {
                        score += pieceValue;
                    } else {
                        score -= pieceValue;
                    }
                }
            }
        }
        
        // Add mobility bonus (more moves = better position)
        try {
            int engineMoves = board.getAllLegalMoves(engineColor).size();
            int opponentMoves = board.getAllLegalMoves(engineColor == Color.WHITE ? Color.BLACK : Color.WHITE).size();
            if(engineMoves> opponentMoves)
                score += Math.log(engineMoves - opponentMoves) * 10; 
            else
                score -= Math.log(opponentMoves - engineMoves) * 10;

        } catch (Exception e) {
            // If we can't get legal moves, ignore mobility bonus
        }
        
        return score;
    }
    
    // Evaluate individual piece value including positional factors
    private int evaluatePiece(Piece piece, int row, int col) {
        int value = PIECE_VALUES.get(piece.getType());
        
        // Add positional bonus based on piece-square tables
        int positionalValue = 0;
        boolean isWhite = piece.getColor() == Color.WHITE;
        int tableRow = isWhite ? 7 - row : row; // Flip for white pieces
        
        switch (piece.getType()) {
            case PAWN:
                positionalValue = PAWN_TABLE[tableRow][col];
                break;
            case KNIGHT:
                positionalValue = KNIGHT_TABLE[tableRow][col];
                break;
            case BISHOP:
                positionalValue = BISHOP_TABLE[tableRow][col];
                break;
            case ROOK:
                positionalValue = ROOK_TABLE[tableRow][col];
                break;
            case QUEEN:
                positionalValue = QUEEN_TABLE[tableRow][col];
                break;
            case KING:
                positionalValue = KING_MIDGAME_TABLE[tableRow][col];
                break;
        }
        
        return value + positionalValue;
    }
    
    // Order moves to improve alpha-beta pruning efficiency
    private void orderMoves(List<Move> moves, ChessBoard board) {
        moves.sort((move1, move2) -> {
            int score1 = getMoveOrderingScore(move1, board);
            int score2 = getMoveOrderingScore(move2, board);
            return Integer.compare(score2, score1); // Higher scores first
        });
    }
    
    // Calculate ordering score for a move (captures, checks, promotions first)
    private int getMoveOrderingScore(Move move, ChessBoard board) {
        int score = 0;
        
        try {
            Piece movingPiece = board.getPiece(move.from);
            Piece capturedPiece = board.getPiece(move.to);
            
            if (movingPiece == null) return score;
            
            // Prioritize captures (MVV-LVA: Most Valuable Victim - Least Valuable Attacker)
            if (capturedPiece != null) {
                score += PIECE_VALUES.get(capturedPiece.getType()) - PIECE_VALUES.get(movingPiece.getType()) / 10;
            }
            
            // Prioritize pawn promotions
            if (movingPiece.getType() == PieceType.PAWN && 
                (move.to.row == 0 || move.to.row == 7)) {
                score += 800;
            }
            
            // Prioritize moves that give check
            board.makeMove(move);
            Color opponentColor = movingPiece.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE;
            if (board.isInCheck(opponentColor)) {
                score += 50;
            }
            board.undoMove(move);
        } catch (Exception e) {
            // If move causes exception, give it low priority
            score = -1000;
        }
        
        return score;
    }
    
    // Transposition table entry
    private static class TranspositionEntry {
        long zobristKey;
        int depth;
        int score;
        int flag; // EXACT, LOWER_BOUND, or UPPER_BOUND
        Move bestMove;
        
        public TranspositionEntry(long zobristKey, int depth, int score, int flag, Move bestMove) {
            this.zobristKey = zobristKey;
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }
    
    // Helper class to store move evaluation results
    private static class MoveEvaluation {
        Move move;
        int evaluation;
        
        public MoveEvaluation(Move move, int evaluation) {
            this.move = move;
            this.evaluation = evaluation;
        }
    }
}