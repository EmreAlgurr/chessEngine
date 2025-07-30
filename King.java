
import java.util.ArrayList;
import java.util.List;

// King piece implementation
class King extends Piece {
    public King(Color color) {
        super(color, PieceType.KING);
    }
    
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'K' : 'k';
    }
    
    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int[] dir : directions) {
            Position newPos = new Position(pos.row + dir[0], pos.col + dir[1]);
            if (isValidPosition(newPos)) {
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                }
            }
        }
        
        // Castling logic - don't check for check here to avoid recursion
        if (!hasMoved) {
            // Kingside castling
            if (canCastleBasic(board, pos, true)) {
                moves.add(new Move(pos, new Position(pos.row, pos.col + 2)));
            }
            // Queenside castling
            if (canCastleBasic(board, pos, false)) {
                moves.add(new Move(pos, new Position(pos.row, pos.col - 2)));
            }
        }
        
        return moves;
    }
    
    private boolean canCastleBasic(ChessBoard board, Position kingPos, boolean kingside) {
        int rookCol = kingside ? 7 : 0;
        int direction = kingside ? 1 : -1;
        
        Piece rook = board.getPiece(new Position(kingPos.row, rookCol));
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) {
            return false;
        }
        
        // Check if squares between king and rook are empty
        int end = kingside ? 6 : 2;
        for (int col = kingPos.col + direction; col != end + direction; col += direction) {
            if (board.getPiece(new Position(kingPos.row, col)) != null) {
                return false;
            }
        }
        
        return true;
    }
}