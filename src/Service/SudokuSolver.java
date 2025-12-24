package Service;

import Model.*;
import Exceptions.InvalidGameException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SudokuSolver {
    private static class BoardContext {
        private final int[][] board;
        private final List<EmptyCell> emptyCells;
        private final List<Integer>[] rowEmptyIndices;
        private final List<Integer>[] colEmptyIndices;
        private final List<Integer>[] boxEmptyIndices;
        @SuppressWarnings("unchecked")
        public BoardContext(int[][] board) {
            this.board = board;
            this.emptyCells = new ArrayList<>();
            rowEmptyIndices = (List<Integer>[]) new List[9];
            colEmptyIndices = (List<Integer>[]) new List[9];
            boxEmptyIndices = (List<Integer>[]) new List[9];
            for (int i = 0; i < 9; i++) {
                rowEmptyIndices[i] = new ArrayList<>();
                colEmptyIndices[i] = new ArrayList<>();
                boxEmptyIndices[i] = new ArrayList<>();
            }
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (board[i][j] == 0) {
                        int index = emptyCells.size();
                        emptyCells.add(new EmptyCell(i, j));
                        rowEmptyIndices[i].add(index);
                        colEmptyIndices[j].add(index);
                        int boxIndex = (i / 3) * 3 + (j / 3);
                        boxEmptyIndices[boxIndex].add(index);
                    }
                }
            }
            if (emptyCells.size() != 5) {
                throw new IllegalArgumentException("Solver works only for exactly 5 empty cells");
            }
        }
        public List<EmptyCell> getEmptyCells() {
            return emptyCells;
        }
        public int getValue(int row, int col) {
            return board[row][col];
        }
        public List<Integer> getEmptyCellsInRow(int row) {
            return rowEmptyIndices[row];
        }
        public List<Integer> getEmptyCellsInColumn(int col) {
            return colEmptyIndices[col];
        }
        public List<Integer> getEmptyCellsInBox(int box) {
            return boxEmptyIndices[box];
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
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            int[] result = current.clone();
            int i = size - 1;
            while (i >= 0) {
                if (current[i] < 9) {
                    current[i]++;
                    break;
                } else {
                    current[i] = 1;
                    i--;
                }
            }
            if (i < 0) {
                hasNext = false;
            }

            return result;
        }
    }
    private static class VerificationWorker implements Runnable {
        private final BoardContext context;
        private final int[] combination;
        private final AtomicBoolean solutionFound;
        private final AtomicReference<int[]> solution;
        private final CountDownLatch completionLatch;

        public VerificationWorker(BoardContext context, int[] combination,
                                  AtomicBoolean solutionFound, AtomicReference<int[]> solution,
                                  CountDownLatch completionLatch) {
            this.context = context;
            this.combination = combination;
            this.solutionFound = solutionFound;
            this.solution = solution;
            this.completionLatch = completionLatch;
        }

        @Override
        public void run() {
            try {
                if (solutionFound.get()) {
                    return;
                }
                if (isValidCombination(context, combination)) {
                    if (!solutionFound.getAndSet(true)) {
                        int[] encodedSolution = encodeSolution(context.getEmptyCells(), combination);
                        solution.set(encodedSolution);
                    }
                }
            } finally {
                completionLatch.countDown();
            }
        }
    }
    public static int[] solve(Game game) throws InvalidGameException {
        if (game.getEmptyCellCount() != 5) {
            throw new InvalidGameException("Solver only works for exactly 5 empty cells");
        }
        BoardContext context = new BoardContext(game.getBoard());
        AtomicBoolean solutionFound = new AtomicBoolean(false);
        AtomicReference<int[]> solution = new AtomicReference<>();
        PermutationIterator iterator = new PermutationIterator(5);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int threadPoolSize = Math.max(2, availableProcessors);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            List<Future<?>> futures = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(0);

            int batchCounter = 0;
            final int BATCH_SIZE = 50;
            while (iterator.hasNext() && !solutionFound.get()) {
                int[] combination = iterator.next();
                if (batchCounter % BATCH_SIZE == 0) {
                    latch = new CountDownLatch(Math.min(BATCH_SIZE,
                            countRemainingCombinations(iterator) + 1));
                }
                VerificationWorker worker = new VerificationWorker(
                        context, combination, solutionFound, solution, latch);

                Future<?> future = executor.submit(worker);
                futures.add(future);
                batchCounter++;
                if (batchCounter % BATCH_SIZE == 0 || solutionFound.get()) {
                    if (solutionFound.get()) {
                        cancelRemainingFutures(futures);
                    }
                    try {
                        latch.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new InvalidGameException("Solver interrupted");
                    }
                    futures.clear();
                }
            }
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidGameException("Solver interrupted: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
        int[] result = solution.get();
        if (result != null) {
            return result;
        } else {
            throw new InvalidGameException("No solution found for the board");
        }
    }
    private static int countRemainingCombinations(PermutationIterator iterator) {
        return 100;
    }
    private static void cancelRemainingFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
    }
    private static boolean isValidCombination(BoardContext context, int[] combination) {
        for (int row = 0; row < 9; row++) {
            boolean[] seen = new boolean[10];
            boolean valid = true;
            for (int col = 0; col < 9; col++) {
                int value = context.getValue(row, col);
                if (value != 0) {
                    if (seen[value]) {
                        valid = false;
                        break;
                    }
                    seen[value] = true;
                }
            }

            if (!valid) continue;
            for (int emptyIndex : context.getEmptyCellsInRow(row)) {
                int value = combination[emptyIndex];
                if (value != 0) {
                    if (seen[value]) {
                        valid = false;
                        break;
                    }
                    seen[value] = true;
                }
            }

            if (!valid) return false;
        }
        for (int col = 0; col < 9; col++) {
            boolean[] seen = new boolean[10];
            boolean valid = true;
            for (int row = 0; row < 9; row++) {
                int value = context.getValue(row, col);
                if (value != 0) {
                    if (seen[value]) {
                        valid = false;
                        break;
                    }
                    seen[value] = true;
                }
            }

            if (!valid) continue;
            for (int emptyIndex : context.getEmptyCellsInColumn(col)) {
                int value = combination[emptyIndex];
                if (value != 0) {
                    if (seen[value]) {
                        valid = false;
                        break;
                    }
                    seen[value] = true;
                }
            }

            if (!valid) return false;
        }
        for (int box = 0; box < 9; box++) {
            boolean[] seen = new boolean[10];
            boolean valid = true;
            int startRow = (box / 3) * 3;
            int startCol = (box % 3) * 3;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int row = startRow + i;
                    int col = startCol + j;
                    int value = context.getValue(row, col);
                    if (value != 0) {
                        if (seen[value]) {
                            valid = false;
                            break;
                        }
                        seen[value] = true;
                    }
                }
                if (!valid) break;
            }
            if (!valid) continue;
            for (int emptyIndex : context.getEmptyCellsInBox(box)) {
                int value = combination[emptyIndex];
                if (value != 0) {
                    if (seen[value]) {
                        valid = false;
                        break;
                    }
                    seen[value] = true;
                }
            }

            if (!valid) return false;
        }

        return true;
    }
    private static int[] encodeSolution(List<EmptyCell> emptyCells, int[] combination) {
        int[] encoded = new int[emptyCells.size()];
        for (int i = 0; i < emptyCells.size(); i++) {
            EmptyCell cell = emptyCells.get(i);
            encoded[i] = cell.row * 81 + cell.col * 9 + (combination[i] - 1);
        }
        return encoded;
    }
    public static int[] solveSequential(Game game) throws InvalidGameException {
        if (game.getEmptyCellCount() != 5) {
            throw new InvalidGameException("Solver only works for exactly 5 empty cells");
        }

        BoardContext context = new BoardContext(game.getBoard());
        PermutationIterator iterator = new PermutationIterator(5);

        while (iterator.hasNext()) {
            int[] combination = iterator.next();
            if (isValidCombination(context, combination)) {
                return encodeSolution(context.getEmptyCells(), combination);
            }
        }
        throw new InvalidGameException("No solution found for the board");
    }
}