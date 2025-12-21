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