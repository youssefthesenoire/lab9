package Service;

import Exceptions.InvalidGameException;
import Model.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SudokuSolver {
    // Flyweight: Shared board structure
    private static class BoardFlyweight {
        private final int[][] fixedCells;
        private final List<EmptyCell> emptyCells;
        
        public BoardFlyweight(int[][] board) {
            this.fixedCells = new int[9][9];
            this.emptyCells = new ArrayList<>();
            
            // Initialize fixed cells and identify empty cells
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    if(board[i][j] == 0) {
                        emptyCells.add(new EmptyCell(i, j));
                    } else {
                        fixedCells[i][j] = board[i][j];
                    }
                }
            }
            
            if(emptyCells.size() != 5) {
                throw new IllegalArgumentException("Solver works only for exactly 5 empty cells");
            }
        }
        
        public int getValue(int row, int col, int[] combination) {
            // Check if this cell is empty
            for(int i = 0; i < emptyCells.size(); i++) {
                EmptyCell cell = emptyCells.get(i);
                if(cell.row == row && cell.col == col) {
                    return combination[i];
                }
            }
            return fixedCells[row][col];
        }
        
        public List<EmptyCell> getEmptyCells() {
            return emptyCells;
        }
    }
    
    private static class EmptyCell {
        final int row;
        final int col;
        
        EmptyCell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    
    // Iterator Pattern: Generate all permutations
    private static class CombinationIterator implements Iterator<int[]> {
        private final int size;
        private final int[] current;
        private boolean hasNext;
        
        public CombinationIterator(int size) {
            this.size = size;
            this.current = new int[size];
            for(int i = 0; i < size; i++) {
                current[i] = 1; // Start with all 1s
            }
            this.hasNext = true;
        }
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        
        @Override
        public int[] next() {
            int[] result = current.clone();
            
            // Generate next combination (like counting in base 9)
            int i = size - 1;
            while(i >= 0) {
                if(current[i] < 9) {
                    current[i]++;
                    break;
                } else {
                    current[i] = 1;
                    i--;
                }
            }
            
            // Check if we've generated all combinations (9^size)
            if(i < 0) {
                hasNext = false;
            }
            
            return result;
        }
    }
    
    public static int[] solve(Game game) throws InvalidGameException {
        if(game.getEmptyCellCount() != 5) {
            throw new InvalidGameException("Solver only works for exactly 5 empty cells");
        }
        
        BoardFlyweight flyweight = new BoardFlyweight(game.getBoard());
        CombinationIterator iterator = new CombinationIterator(5);
        
        while(iterator.hasNext()) {
            int[] combination = iterator.next();
            
            // Verify this combination
            if(isValidCombination(flyweight, combination)) {
                // Return solution encoded as specified: x*81 + y*9 + (value-1)
                return encodeSolution(flyweight.getEmptyCells(), combination);
            }
        }
        
        throw new InvalidGameException("No solution found");
    }
    
    private static boolean isValidCombination(BoardFlyweight flyweight, int[] combination) {
        // Create a verifier that uses the flyweight
        for(int row = 0; row < 9; row++) {
            boolean[] seen = new boolean[10];
            for(int col = 0; col < 9; col++) {
                int value = flyweight.getValue(row, col, combination);
                if(value != 0 && seen[value]) {
                    return false;
                }
                seen[value] = true;
            }
        }
        
        // Check columns
        for(int col = 0; col < 9; col++) {
            boolean[] seen = new boolean[10];
            for(int row = 0; row < 9; row++) {
                int value = flyweight.getValue(row, col, combination);
                if(value != 0 && seen[value]) {
                    return false;
                }
                seen[value] = true;
            }
        }
        
        // Check boxes
        for(int boxRow = 0; boxRow < 3; boxRow++) {
            for(int boxCol = 0; boxCol < 3; boxCol++) {
                boolean[] seen = new boolean[10];
                for(int i = 0; i < 3; i++) {
                    for(int j = 0; j < 3; j++) {
                        int row = boxRow * 3 + i;
                        int col = boxCol * 3 + j;
                        int value = flyweight.getValue(row, col, combination);
                        if(value != 0 && seen[value]) {
                            return false;
                        }
                        seen[value] = true;
                    }
                }
            }
        }
        
        return true;
    }
    
    private static int[] encodeSolution(List<EmptyCell> emptyCells, int[] combination) {
        int[] encoded = new int[emptyCells.size()];
        for(int i = 0; i < emptyCells.size(); i++) {
            EmptyCell cell = emptyCells.get(i);
            // Encode as: x * 81 + y * 9 + (value - 1)
            encoded[i] = cell.row * 81 + cell.col * 9 + (combination[i] - 1);
        }
        return encoded;
    }
}
