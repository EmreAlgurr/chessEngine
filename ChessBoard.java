import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChessBoard {
    private Piece[] board; // 120-square mailbox
    private Position enPassantTarget;
    private Map<Color, Position> kingPositions;
    public boolean canCastleWhiteKingside = false;
    public boolean canCastleWhiteQueenside = false;
    public boolean canCastleBlackKingside = false;
    public boolean canCastleBlackQueenside = false;

    // Mailbox setup: squares 21-28, 31-38, ..., 91-98 are valid
    public ChessBoard() {
        board = new Piece[120];
        kingPositions = new HashMap<>();
        initializeBoard();
    }

    private void initializeBoard() {
        // Clear board
        for (int i = 0; i < 120; i++) {
            board[i] = null;
        }
        
        // Set up pawns
        for (int col = 0; col < 8; col++) {
            board[31 + col] = new Pawn(Color.BLACK); // Row 1
            board[81 + col] = new Pawn(Color.WHITE); // Row 6
        }
        
        // Set up other pieces
        PieceType[] backRank = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, 
                               PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, 
                               PieceType.KNIGHT, PieceType.ROOK};
        
        for (int col = 0; col < 8; col++) {
            board[21 + col] = createPiece(Color.BLACK, backRank[col]); // Row 0
            board[91 + col] = createPiece(Color.WHITE, backRank[col]); // Row 7
        }
        
        // Store king positions
        kingPositions.put(Color.WHITE, new Position(91 + 4)); // e1
        kingPositions.put(Color.BLACK, new Position(21 + 4)); // e8
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
        return board[pos.square];
    }
    
    public void setPiece(Position pos, Piece piece) {
        board[pos.square] = piece;
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
            int captureSquare = (piece.getColor() == Color.WHITE) ? move.to.square + 10 : move.to.square - 10;
            move.capturedPiece = getPiece(new Position(captureSquare));
            setPiece(new Position(captureSquare), null);
        }

        // Handle castling
        if (piece.getType() == PieceType.KING && Math.abs(move.to.square - move.from.square) == 2) {
            move.isCastling = true;
            boolean kingside = move.to.square > move.from.square;
            int rookFromSquare = kingside ? move.from.square + 3 : move.from.square - 4;
            int rookToSquare = kingside ? move.to.square - 1 : move.to.square + 1;

            Piece rook = getPiece(new Position(rookFromSquare));
            setPiece(new Position(rookFromSquare), null);
            setPiece(new Position(rookToSquare), rook);
        }

        // Make the move
        setPiece(move.from, null);
        setPiece(move.to, piece);
        piece.setMoved();

        // Update king position
        if (piece.getType() == PieceType.KING) {
            kingPositions.put(piece.getColor(), move.to);
        }

        // Update en passant target
        enPassantTarget = null;
        if (piece.getType() == PieceType.PAWN && Math.abs(move.to.square - move.from.square) == 20) {
            int epSquare = (move.from.square + move.to.square) / 2;
            enPassantTarget = new Position(epSquare);
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
            setPiece(move.from, new Pawn(piece.getColor()));
        } else {
            setPiece(move.from, piece);
        }
        setPiece(move.to, move.capturedPiece);

        // Restore moved flags
        Piece fromPiece = getPiece(move.from);
        if (fromPiece != null) fromPiece.hasMoved = move.prevFromMoved;
        if (move.capturedPiece != null) move.capturedPiece.hasMoved = move.prevToMoved;

        // Update king position
        if (fromPiece != null && fromPiece.getType() == PieceType.KING) {
            kingPositions.put(fromPiece.getColor(), move.from);
        }

        // Handle castling undo
        if (move.isCastling) {
            boolean kingside = move.to.square > move.from.square;
            int rookFromSquare = kingside ? move.from.square + 3 : move.from.square - 4;
            int rookToSquare = kingside ? move.to.square - 1 : move.to.square + 1;

            Piece rook = getPiece(new Position(rookToSquare));
            setPiece(new Position(rookToSquare), null);
            setPiece(new Position(rookFromSquare), rook);
        }

        // Handle en passant undo
        if (move.isEnPassant) {
            int captureSquare = (piece.getColor() == Color.WHITE) ? move.to.square + 10 : move.to.square - 10;
            setPiece(new Position(captureSquare), move.capturedPiece);
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
        
        // Check all squares for attacking pieces
        for (int square = 21; square <= 98; square++) {
            if ((square % 10) < 1 || (square % 10) > 8) continue; // Skip invalid squares
            
            Piece piece = board[square];
            if (piece != null && piece.getColor() == attackingColor) {
                List<Move> moves;
                if (piece.getType() == PieceType.KING) {
                    moves = ((King)piece).getPossibleMoves(new Position(square), this, true);
                } else {
                    moves = piece.getPossibleMoves(new Position(square), this);
                }
                for (Move move : moves) {
                    if (move.to.equals(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public List<Move> getAllLegalMoves(Color color) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (int square = 21; square <= 98; square++) {
            if ((square % 10) < 1 || (square % 10) > 8) continue; // Skip invalid squares
            
            Piece piece = board[square];
            if (piece != null && piece.getColor() == color) {
                Position from = new Position(square);
                List<Move> possibleMoves = piece.getPossibleMoves(from, this);
                
                for (Move move : possibleMoves) {
                    if (isLegalMove(move, color)) {
                        legalMoves.add(move);
                    }
                }
            }
        }
        return legalMoves;
    }
    
    private boolean isLegalMove(Move move, Color color) {
        // Special handling for en passant - need to simulate the actual capture
        if (move.isEnPassant) {
            Piece piece = getPiece(move.from);
            Piece captured = getPiece(move.to);
            
            // Calculate the square of the captured pawn
            int captureSquare = (piece.getColor() == Color.WHITE) ? move.to.square + 10 : move.to.square - 10;
            Piece capturedPawn = getPiece(new Position(captureSquare));
            
            // Simulate the en passant move
            setPiece(move.from, null);
            setPiece(move.to, piece);
            setPiece(new Position(captureSquare), null);
            
            boolean inCheck = isInCheck(color);
            
            // Undo the simulation
            setPiece(move.from, piece);
            setPiece(move.to, captured);
            setPiece(new Position(captureSquare), capturedPawn);
            
            if (inCheck) return false;
        } else {
            // Normal move check
            if (wouldBeInCheck(color, move.from, move.to)) {
                return false;
            }
        }
        
        // Special handling for castling
        Piece piece = getPiece(move.from);
        if (piece.getType() == PieceType.KING && Math.abs(move.to.square - move.from.square) == 2) {
            // King is trying to castle
            if (isInCheck(color)) {
                return false; // Can't castle while in check
            }
            
            // Check if king passes through check
            boolean kingside = move.to.square > move.from.square;
            int direction = kingside ? 1 : -1;
            
            for (int sq = move.from.square + direction; sq != move.to.square + direction; sq += direction) {
                if (wouldBeInCheck(color, move.from, new Position(sq))) {
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
                Piece piece = board[21 + row * 10 + col];
                char symbol = piece != null ? piece.getSymbol() : '.';
                System.out.print(symbol + " ");
            }
            System.out.println(8 - row);
        }
        System.out.println("  a b c d e f g h");
    }

    // Set up the board from a FEN string
    public void setFromFEN(String fen) {
        // Clear board
        for (int i = 0; i < 120; i++) {
            board[i] = null;
        }
        kingPositions = new HashMap<>();
        
        String[] parts = fen.split(" ");
        String[] rows = parts[0].split("/");
        for (int r = 0; r < 8; r++) {
            int c = 0;
            for (char ch : rows[r].toCharArray()) {
                if (Character.isDigit(ch)) {
                    c += ch - '0';
                } else {
                    Color color = Character.isUpperCase(ch) ? Color.WHITE : Color.BLACK;
                    PieceType type;
                    switch (Character.toLowerCase(ch)) {
                        case 'k': type = PieceType.KING; break;
                        case 'q': type = PieceType.QUEEN; break;
                        case 'r': type = PieceType.ROOK; break;
                        case 'b': type = PieceType.BISHOP; break;
                        case 'n': type = PieceType.KNIGHT; break;
                        case 'p': type = PieceType.PAWN; break;
                        default: continue;
                    }
                    int square = 21 + r * 10 + c;
                    board[square] = createPiece(color, type);
                    if (type == PieceType.KING) kingPositions.put(color, new Position(square));
                    c++;
                }
            }
        }
        
        // Castling rights
        canCastleWhiteKingside = false;
        canCastleWhiteQueenside = false;
        canCastleBlackKingside = false;
        canCastleBlackQueenside = false;
        if (parts.length > 2) {
            String castling = parts[2];
            if (castling.contains("K")) canCastleWhiteKingside = true;
            if (castling.contains("Q")) canCastleWhiteQueenside = true;
            if (castling.contains("k")) canCastleBlackKingside = true;
            if (castling.contains("q")) canCastleBlackQueenside = true;
        }
        
        // En passant
        if (parts.length > 3 && !parts[3].equals("-")) {
            enPassantTarget = new Position(parts[3]);
        } else {
            enPassantTarget = null;
        }
    }

    // Add simple perft method
    public long perft(Color color, int depth) {
        if (depth == 0) return 1;
        long nodes = 0;
        List<Move> moves = getAllLegalMoves(color);
        for (Move move : moves) {
            makeMove(move);
            Color nextColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
            nodes += perft(nextColor, depth - 1);
            undoMove(move);
        }
        return nodes;
    }
}
