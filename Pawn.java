import java.util.ArrayList;
import java.util.List;

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
        int direction = (color == Color.WHITE) ? -10 : 10; // Up/down in mailbox
        
        // Forward move
        Position oneForward = new Position(pos.square + direction);
        if (oneForward.isValid() && board.getPiece(oneForward) == null) {
            // Check for promotion
            int promotionRow = (color == Color.WHITE) ? 0 : 7;
            if (oneForward.getRow() == promotionRow) {
                moves.add(new Move(pos, oneForward, PieceType.QUEEN));
                moves.add(new Move(pos, oneForward, PieceType.ROOK));
                moves.add(new Move(pos, oneForward, PieceType.BISHOP));
                moves.add(new Move(pos, oneForward, PieceType.KNIGHT));
            } else {
                moves.add(new Move(pos, oneForward));
                // Two squares forward from starting position
                int startRow = (color == Color.WHITE) ? 6 : 1;
                if (pos.getRow() == startRow) {
                    Position twoForward = new Position(pos.square + 2 * direction);
                    if (board.getPiece(twoForward) == null) {
                        moves.add(new Move(pos, twoForward));
                    }
                }
            }
        }

        // Captures (including promotions)
        int[] captureDirs = {direction - 1, direction + 1}; // Diagonal captures
        for (int captureDir : captureDirs) {
            Position diag = new Position(pos.square + captureDir);
            if (diag.isValid()) {
                Piece target = board.getPiece(diag);
                
                // Regular capture
                if (target != null && isEnemyPiece(target)) {
                    int promotionRow = (color == Color.WHITE) ? 0 : 7;
                    if (diag.getRow() == promotionRow) {
                        moves.add(new Move(pos, diag, PieceType.QUEEN));
                        moves.add(new Move(pos, diag, PieceType.ROOK));
                        moves.add(new Move(pos, diag, PieceType.BISHOP));
                        moves.add(new Move(pos, diag, PieceType.KNIGHT));
                    } else {
                        moves.add(new Move(pos, diag));
                    }
                }
                
                // En passant - diagonal square must be empty and match en passant target
                else if (target == null && board.getEnPassantTarget() != null && board.getEnPassantTarget().equals(diag)) {
                    Move enPassantMove = new Move(pos, diag);
                    enPassantMove.isEnPassant = true;
                    moves.add(enPassantMove);
                }
            }
        }
        return moves;
    }
}