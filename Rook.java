import java.util.ArrayList;
import java.util.List;

class Rook extends Piece {
    private static final int[] ROOK_DIRECTIONS = {-10, -1, 1, 10};
    
    public Rook(Color color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'R' : 'r';
    }

    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        
        for (int direction : ROOK_DIRECTIONS) {
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
