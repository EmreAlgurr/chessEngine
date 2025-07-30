import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Chess board class
class ChessBoard {
    private Piece[][] board;
    private Position enPassantTarget;
    private Map<Color, Position> kingPositions;
    
    public ChessBoard() {
        board = new Piece[8][8];
        kingPositions = new HashMap<>();
        setupInitialPosition();
    }
    
    private void setupInitialPosition() {
        // Set up pawns
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(Color.BLACK);
            board[6][col] = new Pawn(Color.WHITE);
        }
        
        // Set up other pieces
        PieceType[] backRank = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, 
                               PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, 
                               PieceType.KNIGHT, PieceType.ROOK};
        
        for (int col = 0; col < 8; col++) {
            board[0][col] = createPiece(Color.BLACK, backRank[col]);
            board[7][col] = createPiece(Color.WHITE, backRank[col]);
        }
        
        // Store king positions
        kingPositions.put(Color.WHITE, new Position(7, 4));
        kingPositions.put(Color.BLACK, new Position(0, 4));

        
        
    }
    
    private Piece createPiece(Color color, PieceType type) {
        switch (type) {
            case KING: return new King(color);
            case QUEEN: return new Queen(color);
            case ROOK: return new Rook(color);
            case BISHOP: return new Bishop(color);
            case KNIGHT: return new Knight(color);
            case PAWN: return new Pawn(color);
            default: return null;
        }
    }
    
    public Piece getPiece(Position pos) {
        if (!pos.isValid()) return null;
        return board[pos.row][pos.col];
    }
    
    public void setPiece(Position pos, Piece piece) {
        if (pos.isValid()) {
            board[pos.row][pos.col] = piece;
            if (piece != null && piece.getType() == PieceType.KING) {
                kingPositions.put(piece.getColor(), pos);
            }
        }
    }
    
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    
    public void setEnPassantTarget(Position target) {
        this.enPassantTarget = target;
    }
    
    public Position getKingPosition(Color color) {
        return kingPositions.get(color);
    }
    
    public boolean makeMove(Move move) {
    Piece piece = getPiece(move.from);
    if (piece == null) return false;

    move.capturedPiece = getPiece(move.to);
    move.prevEnPassantTarget = enPassantTarget;
    move.prevFromMoved = piece.hasMoved();
    if (move.capturedPiece != null)
        move.prevToMoved = move.capturedPiece.hasMoved();

    // Handle en passant
    if (move.isEnPassant) {
        int captureRow = (piece.getColor() == Color.WHITE) ? move.to.row + 1 : move.to.row - 1;
        move.capturedPiece = getPiece(new Position(captureRow, move.to.col));
        setPiece(new Position(captureRow, move.to.col), null);
    }

    // Handle castling
    if (piece.getType() == PieceType.KING && Math.abs(move.to.col - move.from.col) == 2) {
        move.isCastling = true;
        boolean kingside = move.to.col > move.from.col;
        int rookFromCol = kingside ? 7 : 0;
        int rookToCol = kingside ? move.to.col - 1 : move.to.col + 1;

        Piece rook = getPiece(new Position(move.from.row, rookFromCol));
        setPiece(new Position(move.from.row, rookFromCol), null);
        setPiece(new Position(move.from.row, rookToCol), rook);
       // rook.setMoved();
    }

    // Make the move
    setPiece(move.from, null);
    setPiece(move.to, piece);
    piece.setMoved();

    // Update en passant target
    enPassantTarget = null;
    if (piece.getType() == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
        int epRow = (move.from.row + move.to.row) / 2;
        enPassantTarget = new Position(epRow, move.from.col);
    }

    // Handle pawn promotion
    if (piece.getType() == PieceType.PAWN && move.promotionPiece != null) {
        move.promotedPiece = createPiece(piece.getColor(), move.promotionPiece);
        setPiece(move.to, move.promotedPiece);
    }

    return true;
}
    
    public void undoMove(Move move) {
        Piece piece = getPiece(move.to);

        // Undo promotion
        if (move.promotedPiece != null) {
            // Restore pawn to from square
            setPiece(move.from, new Pawn(piece.getColor()));
        } else {
            setPiece(move.from, piece);
        }
        setPiece(move.to, move.capturedPiece);

        // Restore moved flags
        Piece fromPiece = getPiece(move.from);
        if (fromPiece != null) fromPiece.hasMoved = move.prevFromMoved;
        if (move.capturedPiece != null) move.capturedPiece.hasMoved = move.prevToMoved;

        // Handle castling undo
        if (move.isCastling) {
            boolean kingside = move.to.col > move.from.col;
            int rookFromCol = kingside ? 7 : 0;
            int rookToCol = kingside ? move.to.col - 1 : move.to.col + 1;

            Piece rook = getPiece(new Position(move.from.row, rookToCol));
            setPiece(new Position(move.from.row, rookToCol), null);
            setPiece(new Position(move.from.row, rookFromCol), rook);
        }

        // Handle en passant undo
        if (move.isEnPassant) {
            int captureRow = (piece.getColor() == Color.WHITE) ? move.to.row + 1 : move.to.row - 1;
            setPiece(new Position(captureRow, move.to.col), move.capturedPiece);
            setPiece(move.to, null);
        }

        // Restore en passant target
        enPassantTarget = move.prevEnPassantTarget;
    }
    
    public boolean isInCheck(Color color) {
        Position kingPos = getKingPosition(color);
        return isSquareUnderAttack(kingPos, color);
    }
    
    public boolean wouldBeInCheck(Color color, Position from, Position to) {
        // Simulate the move
        Piece piece = getPiece(from);
        Piece captured = getPiece(to);
        
        setPiece(from, null);
        setPiece(to, piece);
        if (piece.getType() == PieceType.KING) {
            kingPositions.put(color, to);
        }
        
        boolean inCheck = isInCheck(color);
        
        // Undo the move
        setPiece(from, piece);
        setPiece(to, captured);
        if (piece.getType() == PieceType.KING) {
            kingPositions.put(color, from);
        }
        
        return inCheck;
    }
    
    private boolean isSquareUnderAttack(Position pos, Color defendingColor) {
        Color attackingColor = (defendingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == attackingColor) {
                    List<Move> moves = piece.getPossibleMoves(new Position(row, col), this);
                    for (Move move : moves) {
                        if (move.to.equals(pos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public List<Move> getAllLegalMoves(Color color) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == color) {
                    Position from = new Position(row, col);
                    List<Move> possibleMoves = piece.getPossibleMoves(from, this);
                    
                    for (Move move : possibleMoves) {
                        if (isLegalMove(move, color)) {
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        return legalMoves;
    }
    
    private boolean isLegalMove(Move move, Color color) {
        // Check if move would leave king in check
        if (wouldBeInCheck(color, move.from, move.to)) {
            return false;
        }
        
        // Special handling for castling
        Piece piece = getPiece(move.from);
        if (piece.getType() == PieceType.KING && Math.abs(move.to.col - move.from.col) == 2) {
            // King is trying to castle
            if (isInCheck(color)) {
                return false; // Can't castle while in check
            }
            
            // Check if king passes through check
            boolean kingside = move.to.col > move.from.col;
            int direction = kingside ? 1 : -1;
            
            for (int col = move.from.col + direction; col != move.to.col + direction; col += direction) {
                if (wouldBeInCheck(color, move.from, new Position(move.from.row, col))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public boolean isCheckmate(Color color) {
        return isInCheck(color) && getAllLegalMoves(color).isEmpty();
    }
    
    public boolean isStalemate(Color color) {
        return !isInCheck(color) && getAllLegalMoves(color).isEmpty();
    }
    
    public void display() {
        System.out.println("  a b c d e f g h");
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                char symbol = piece != null ? piece.getSymbol() : '.';
                System.out.print(symbol + " ");
            }
            System.out.println(8 - row);
        }
        System.out.println("  a b c d e f g h");
    }
}
