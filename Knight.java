
import java.util.ArrayList;
import java.util.List;

class Knight extends Piece {
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
        int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
        
        for (int[] move : knightMoves) {
            Position newPos = new Position(pos.row + move[0], pos.col + move[1]);
            if (isValidPosition(newPos)) {
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                }
            }
        }
        return moves;
    }
}