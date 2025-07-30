import java.util.ArrayList;
import java.util.List;

// Fixed Pawn piece implementation
class Pawn extends Piece {
    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }
    
    @Override
    public char getSymbol() {
        return color == Color.WHITE ? 'P' : 'p';
    }
    
    @Override
    public List<Move> getPossibleMoves(Position pos, ChessBoard board) {
        List<Move> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? -1 : 1;
        
        // Forward move
        Position oneForward = new Position(pos.row + direction, pos.col);
        if (isValidPosition(oneForward) && board.getPiece(oneForward) == null) {
            moves.add(new Move(pos, oneForward));
            
            // Two squares forward from starting position
            // Fixed: Check if pawn is on starting rank instead of relying on hasMoved flag
            int startingRank = (color == Color.WHITE) ? 6 : 1;
            if (pos.row == startingRank) {
                Position twoForward = new Position(pos.row + 2 * direction, pos.col);
                if (isValidPosition(twoForward) && board.getPiece(twoForward) == null) {
                    moves.add(new Move(pos, twoForward));
                }
            }
        }
        
        // Captures
        int[] captureCols = {pos.col - 1, pos.col + 1};
        for (int captureCol : captureCols) {
            Position capturePos = new Position(pos.row + direction, captureCol);
            if (isValidPosition(capturePos)) {
                Piece targetPiece = board.getPiece(capturePos);
                if (targetPiece != null && isEnemyPiece(targetPiece)) {
                    moves.add(new Move(pos, capturePos));
                }
                
                // En passant
                if (board.getEnPassantTarget() != null && board.getEnPassantTarget().equals(capturePos)) {
                    Move enPassantMove = new Move(pos, capturePos);
                    enPassantMove.isEnPassant = true;
                    moves.add(enPassantMove);
                }
            }
        }
        
        return moves;
    }
}