
import java.util.ArrayList;
import java.util.List;

class Bishop extends Piece {
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
        int[][] directions = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
        
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