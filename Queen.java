
import java.util.ArrayList;
import java.util.List;

// Queen piece implementation
class Queen extends Piece {
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
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(pos.row + i * dir[0], pos.col + i * dir[1]);
                if (!isValidPosition(newPos)) break;
                
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null) {
                    moves.add(new Move(pos, newPos));
                } else {
                    if (isEnemyPiece(targetPiece)) {
                        moves.add(new Move(pos, newPos));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}