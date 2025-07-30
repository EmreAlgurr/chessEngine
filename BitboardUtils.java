public class BitboardUtils {
    // Precomputed knight moves for each square (0-63)
    public static final long[] KNIGHT_MOVES = new long[64];

    static {
        for (int sq = 0; sq < 64; sq++) {
            KNIGHT_MOVES[sq] = computeKnightMoves(sq);
        }
    }

    private static long computeKnightMoves(int sq) {
        int r = sq / 8, c = sq % 8;
        long moves = 0L;
        int[][] deltas = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
        for (int[] d : deltas) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                moves |= 1L << (nr * 8 + nc);
            }
        }
        return moves;
    }

    // Convert Position to square index (0-63)
    public static int posToIndex(Position pos) {
        return pos.row * 8 + pos.col;
    }

    // Directions for sliding pieces
    private static final int[][] BISHOP_DIRS = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
    private static final int[][] ROOK_DIRS = {{-1,0}, {1,0}, {0,-1}, {0,1}};

    // Generate bishop attacks from a square, given occupancy
    public static long bishopAttacks(int sq, long occupancy) {
        return slidingAttacks(sq, occupancy, BISHOP_DIRS);
    }

    // Generate rook attacks from a square, given occupancy
    public static long rookAttacks(int sq, long occupancy) {
        return slidingAttacks(sq, occupancy, ROOK_DIRS);
    }

    // Generate queen attacks from a square, given occupancy
    public static long queenAttacks(int sq, long occupancy) {
        return bishopAttacks(sq, occupancy) | rookAttacks(sq, occupancy);
    }

    // Helper for sliding attacks
    private static long slidingAttacks(int sq, long occupancy, int[][] dirs) {
        int r = sq / 8, c = sq % 8;
        long attacks = 0L;
        for (int[] dir : dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            while (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                int nsq = nr * 8 + nc;
                attacks |= 1L << nsq;
                // Stop if a piece blocks further movement
                if (((occupancy >>> nsq) & 1) != 0) break;
                nr += dir[0];
                nc += dir[1];
            }
        }
        return attacks;
    }

    // Get occupancy bitboard for all pieces (optionally skip a square)
    public static long getOccupancy(ChessBoard board, Position skip) {
        long occ = 0L;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position p = new Position(row, col);
                if (skip != null && p.equals(skip)) continue;
                if (board.getPiece(p) != null) {
                    occ |= 1L << (row * 8 + col);
                }
            }
        }
        return occ;
    }
}
