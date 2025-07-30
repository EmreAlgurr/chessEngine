import java.util.ArrayList;
import java.util.List;

class Rook extends Piece {
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
        int sq = BitboardUtils.posToIndex(pos);
        long occupancy = BitboardUtils.getOccupancy(board, pos);
        long targets = BitboardUtils.rookAttacks(sq, occupancy);
        for (int i = 0; i < 64; i++) {
            if (((targets >>> i) & 1) != 0) {
                int row = i / 8, col = i % 8;
                Position newPos = new Position(row, col);
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, newPos));
                    if (targetPiece != null) continue; // stop after capture
                }
            }
        }
        return moves;
    }
}
