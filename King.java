import java.util.ArrayList;
import java.util.List;

class King extends Piece {
    private static final int[] KING_MOVES = {-11, -10, -9, -1, 1, 9, 10, 11};
    
    public King(Color color) {
        super(color, PieceType.KING);
    }
    
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'K' : 'k';
    }
    
    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        return getPossibleMoves(pos, board, false);
    }

    public List<Move> getPossibleMoves(Position pos, ChessBoard board, boolean forAttack) {
        List<Move> moves = new ArrayList<>();
        
        for (int delta : KING_MOVES) {
            Position newPos = new Position(pos.square + delta);
            if (newPos.isValid()) {
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                }
            }
        }
        
        // Only generate castling moves if not for attack detection
        if (!forAttack && !hasMoved) {
            if (color == Color.WHITE) {
                if (board.canCastleWhiteKingside && canCastleBasic(board, pos, true, forAttack)) {
                    moves.add(new Move(pos, new Position(pos.square + 2)));
                }
                if (board.canCastleWhiteQueenside && canCastleBasic(board, pos, false, forAttack)) {
                    moves.add(new Move(pos, new Position(pos.square - 2)));
                }
            } else if (color == Color.BLACK) {
                if (board.canCastleBlackKingside && canCastleBasic(board, pos, true, forAttack)) {
                    moves.add(new Move(pos, new Position(pos.square + 2)));
                }
                if (board.canCastleBlackQueenside && canCastleBasic(board, pos, false, forAttack)) {
                    moves.add(new Move(pos, new Position(pos.square - 2)));
                }
            }
        }
        
        return moves;
    }
    
    private boolean canCastleBasic(ChessBoard board, Position kingPos, boolean kingside, boolean forAttack) {
        if (forAttack) return false;
        
        int rookDelta = kingside ? 3 : -4;
        Position rookPos = new Position(kingPos.square + rookDelta);
        Piece rook = board.getPiece(rookPos);
        
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color || rook.hasMoved()) {
            return false;
        }
        
        if (board.isInCheck(color)) return false;
        
        // Check squares between king and rook are empty
        int step = kingside ? 1 : -1;
        for (int sq = kingPos.square + step; kingside ? sq < rookPos.square : sq > rookPos.square; sq += step) {
            if (board.getPiece(new Position(sq)) != null) return false;
        }
        
        // Check king doesn't pass through check
        for (int sq = kingPos.square; kingside ? sq <= kingPos.square + 2 : sq >= kingPos.square - 2; sq += step) {
            if (board.wouldBeInCheck(color, kingPos, new Position(sq))) return false;
        }
        
        return true;
    }
}