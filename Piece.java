import java.util.List;

// Abstract base class for all chess pieces
abstract class Piece {
    protected Color color;
    protected PieceType type;
    boolean hasMoved; // Make package-private instead of protected
    
    public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }
    
    public Color getColor() { return color; }
    public PieceType getType() { return type; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved() { this.hasMoved = true; }
    
    public abstract List<Move> getPossibleMoves(Position pos, ChessBoard board);
    public abstract char getSymbol();
    
    protected boolean isValidPosition(Position pos) {
        return pos.isValid();
    }
    
    protected boolean isEnemyPiece(Piece piece) {
        return piece != null && piece.getColor() != this.color;
    }
    
    protected boolean isFriendlyPiece(Piece piece) {
        return piece != null && piece.getColor() == this.color;
    }
}