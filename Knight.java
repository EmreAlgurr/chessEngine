import java.util.ArrayList;
import java.util.List;

class Knight extends Piece {
    private static final int[] KNIGHT_MOVES = {-21, -19, -12, -8, 8, 12, 19, 21};
    
    public Knight(Color color) {
        super(color, PieceType.KNIGHT);
    }
    
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'N' : 'n';
    }
    
    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        
        for (int delta : KNIGHT_MOVES) {
            Position newPos = new Position(pos.square + delta);
            if (newPos.isValid()) {
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                }
            }
        }
        return moves;
    }
}