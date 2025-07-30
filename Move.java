class Move {
    public Position from, to;
    public Piece capturedPiece;
    public boolean isEnPassant;
    public boolean isCastling;
    public PieceType promotionPiece;
    public Position prevEnPassantTarget;
    public boolean prevFromMoved;
    public boolean prevToMoved;
    public Piece promotedPiece; 
    
    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }
    
    public Move(String fromNotation, String toNotation) {
        this.from = new Position(fromNotation);
        this.to = new Position(toNotation);
    }
    
    @Override
    public String toString() {
        return from.toNotation() + "-" + to.toNotation();
    }
}