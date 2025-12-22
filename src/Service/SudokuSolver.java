package Service;

import Model.*;
import Exceptions.InvalidGameException;

import java.util.*;

public class SudokuSolver {
    private static class BoardContext {
        private final int[][] board;
        private final List<EmptyCell> emptyCells;

        public BoardContext(int[][] board) {
            this.board = board;
            this.emptyCells = new ArrayList<>();

            // Identify empty cells
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    if(board[i][j] == 0) {
                        emptyCells.add(new EmptyCell(i, j));
                    }
                }
            }

            if(emptyCells.size() != 5) {
                throw new IllegalArgumentException("Solver works only for exactly 5 empty cells");
            }
        }

        public List<EmptyCell> getEmptyCells() {
            return emptyCells;
        }

        public int getValue(int row, int col) {
            return board[row][col];
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

    private static class PermutationIterator implements Iterator<int[]> {
        private final int size;
        private final int[] current;
        private boolean hasNext;

        public PermutationIterator(int size) {
            this.size = size;
            this.current = new int[size];
            Arrays.fill(current, 1);
            this.hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public int[] next() {
            if(!hasNext) {
                throw new NoSuchElementException();
            }

            int[] result = current.clone();

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

        BoardContext context = new BoardContext(game.getBoard());
        PermutationIterator iterator = new PermutationIterator(5);


        while(iterator.hasNext()) {
            int[] combination = iterator.next();

            if(isValidCombination(context, combination)) {
                return encodeSolution(context.getEmptyCells(), combination);
            }
        }

        throw new InvalidGameException("No solution found for the board");
    }

    private static boolean isValidCombination(BoardContext context, int[] combination) {
        int[][] tempBoard = new int[9][9];

        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                tempBoard[i][j] = context.getValue(i, j);
            }
        }

        List<EmptyCell> emptyCells = context.getEmptyCells();
        for(int i = 0; i < emptyCells.size(); i++) {
            EmptyCell cell = emptyCells.get(i);
            tempBoard[cell.row][cell.col] = combination[i];
        }

        VerificationResult result = SequentialVerifier.verify(tempBoard);
        return result.getState() == GameState.VALID;
    }

    private static int[] encodeSolution(List<EmptyCell> emptyCells, int[] combination) {
        int[] encoded = new int[emptyCells.size()];
        for(int i = 0; i < emptyCells.size(); i++) {
            EmptyCell cell = emptyCells.get(i);
            encoded[i] = cell.row * 81 + cell.col * 9 + (combination[i] - 1);
        }
        return encoded;
    }
}