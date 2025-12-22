package Model;

public class Game {
    private int[][] initialBoard;
    private int[][] currentBoard;
    private boolean[][] fixedCells;
    private Difficulty difficulty;

    public Game(int[][] board, Difficulty difficulty) {
        this.initialBoard = new int[9][9];
        this.currentBoard = new int[9][9];
        this.fixedCells = new boolean[9][9];

        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                this.initialBoard[i][j] = board[i][j];
                this.currentBoard[i][j] = board[i][j];
                this.fixedCells[i][j] = (board[i][j] != 0);
            }
        }
        this.difficulty = difficulty;
    }

    public int[][] getBoard() {
        return currentBoard;
    }

    public int[][] getInitialBoard() {
        return initialBoard;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isFixedCell(int row, int col) {
        return fixedCells[row][col];
    }

    public void setCellValue(int row, int col, int value) {
        currentBoard[row][col] = value;
    }

    // Add a method to check if a cell is an original clue (non-zero in initial board)
    public boolean isOriginalClue(int row, int col) {
        return initialBoard[row][col] != 0;
    }

    // Add a method to update fixed cells based on original clues only
    public void updateFixedCells() {
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                fixedCells[i][j] = (initialBoard[i][j] != 0);
            }
        }
    }

    public int getEmptyCellCount() {
        int count = 0;
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                if(currentBoard[i][j] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isComplete() {
        return getEmptyCellCount() == 0;
    }
}