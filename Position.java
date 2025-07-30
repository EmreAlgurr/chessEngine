
// ChessGame.java - Complete Chess Game Implementation

import java.util.*;

// Enum for piece colors
enum Color {
    WHITE, BLACK, GRAY
}

// Enum for piece types
enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}
// Position class to represent board coordinates
// Position class to represent board coordinates
class Position {
    public int row, col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public Position(String notation) {
        // Convert chess notation (e.g., "e4") to array indices
        this.col = notation.charAt(0) - 'a';
        this.row = 8 - (notation.charAt(1) - '0');
    }
    
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    public String toNotation() {
        return "" + (char)('a' + col) + (8 - row);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position pos = (Position) obj;
        return row == pos.row && col == pos.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}