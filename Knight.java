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
        int sq = BitboardUtils.posToIndex(pos);
        long targets = BitboardUtils.KNIGHT_MOVES[sq];
        for (int i = 0; i < 64; i++) {
            if (((targets >>> i) & 1) != 0) {
                int row = i / 8, col = i % 8;
                Position newPos = new Position(row, col);
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                }
            }
        }
        return moves;
    }
}