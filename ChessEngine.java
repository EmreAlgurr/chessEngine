
import java.util.*;

// Chess Engine class that implements minimax algorithm with alpha-beta pruning
class ChessEngine {
    private static final int MAX_DEPTH = 5; // Adjustable search depth
    private static final int INFINITY = 100000;
    
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
            MoveEvaluation result = minimax(board, MAX_DEPTH, -INFINITY, INFINITY, true, color);
            
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
    
    // Minimax algorithm with alpha-beta pruning
    private MoveEvaluation minimax(ChessBoard board, int depth, int alpha, int beta, boolean isMaximizing, Color engineColor) {
        Color currentColor = isMaximizing ? engineColor : (engineColor == Color.WHITE ? Color.BLACK : Color.WHITE);
        
        // Base case: reached maximum depth or game is over
        if (depth == 0 || board.isCheckmate(currentColor) || board.isStalemate(currentColor)) {
            return new MoveEvaluation(null, evaluateBoard(board, engineColor));
        }
        
        List<Move> moves = board.getAllLegalMoves(currentColor);
        if (moves.isEmpty()) {
            return new MoveEvaluation(null, evaluateBoard(board, engineColor));
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
            return new MoveEvaluation(null, isMaximizing ? -INFINITY : INFINITY);
        }
        
        // Order moves for better alpha-beta pruning
        orderMoves(safeMoves, board);
        
        Move bestMove = safeMoves.get(0);
        
        if (isMaximizing) {
            int maxEval = -INFINITY;
            for (Move move : safeMoves) {
                try {
                    // Make the move
                    board.makeMove(move);
                    
                    // Recursively evaluate
                    MoveEvaluation eval = minimax(board, depth - 1, alpha, beta, false, engineColor);
                    
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
            return new MoveEvaluation(bestMove, maxEval);
        } else {
            int minEval = INFINITY;
            for (Move move : safeMoves) {
                try {
                    // Make the move
                    board.makeMove(move);
                    
                    // Recursively evaluate
                    MoveEvaluation eval = minimax(board, depth - 1, alpha, beta, true, engineColor);
                    
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
            return new MoveEvaluation(bestMove, minEval);
        }
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
            score += (engineMoves - opponentMoves) * 10;
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