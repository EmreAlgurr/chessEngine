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

// Position class for 120-square mailbox
class Position {
    public int square; // 0-119 mailbox index
    
    public Position(int square) {
        this.square = square;
    }
    
    public Position(int row, int col) {
        // Convert 8x8 to 120-square mailbox (21 + row*10 + col)
        this.square = 21 + row * 10 + col;
    }
    
    public Position(String notation) {
        // Convert chess notation (e.g., "e4") to mailbox index
        int col = notation.charAt(0) - 'a';
        int row = 8 - (notation.charAt(1) - '0');
        this.square = 21 + row * 10 + col;
    }
    
    public boolean isValid() {
        return square >= 21 && square <= 98 && (square % 10) >= 1 && (square % 10) <= 8;
    }
    
    public int getRow() {
        return (square - 21) / 10;
    }
    
    public int getCol() {
        return (square - 21) % 10;
    }
    
    public String toNotation() {
        int row = getRow();
        int col = getCol();
        return "" + (char)('a' + col) + (8 - row);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position pos = (Position) obj;
        return square == pos.square;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(square);
    }
}