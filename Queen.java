import java.util.ArrayList;
import java.util.List;

class Queen extends Piece {
    private static final int[] QUEEN_DIRECTIONS = {-11, -10, -9, -1, 1, 9, 10, 11};
    
    public Queen(Color color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'Q' : 'q';
    }

    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        
        for (int direction : QUEEN_DIRECTIONS) {
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