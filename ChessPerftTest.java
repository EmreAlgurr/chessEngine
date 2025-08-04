public class ChessPerftTest {
    public static void main(String[] args) {
        // Standard starting position
        ChessBoard board = new ChessBoard();
      
        // Test position
        String fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        board.setFromFEN(fen);
        System.out.println("\nTest position perft:");
        for (int depth = 1; depth <= 5; depth++) {
            long nodes = board.perft(Color.WHITE, depth);
            System.out.println("Depth " + depth + ": " + nodes);
        }
    }
}
