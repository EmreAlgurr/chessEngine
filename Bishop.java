import java.util.ArrayList;
import java.util.List;

class Bishop extends Piece {
    private static final int[] BISHOP_DIRECTIONS = {-11, -9, 9, 11};
    
    public Bishop(Color color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'B' : 'b';
    }

    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        
        for (int direction : BISHOP_DIRECTIONS) {
            for (int sq = pos.square + direction; ; sq += direction) {
                Position newPos = new Position(sq);
                if (!newPos.isValid()) break;
                
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null) {
                    moves.add(new Move(pos, newPos));
                } else {
                    if (isEnemyPiece(targetPiece)) {
                        moves.add(new Move(pos, newPos));
                    }
                    break; // Stop sliding in this direction
                }
            }
        }
        return moves;
    }
}