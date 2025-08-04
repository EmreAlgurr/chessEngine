import java.util.List;
import java.util.Random;

class RandomEngine {
    private Random random;
    
    public RandomEngine() {
        this.random = new Random();
    }
    
    public RandomEngine(long seed) {
        this.random = new Random(seed);
    }
    
    public Move getBestMove(ChessBoard board, Color color) {
        List<Move> legalMoves = board.getAllLegalMoves(color);
        
        if (legalMoves.isEmpty()) {
            return null; // No legal moves (checkmate or stalemate)
        }
        
        // Return a random legal move
        int randomIndex = random.nextInt(legalMoves.size());
        return legalMoves.get(randomIndex);
    }
}