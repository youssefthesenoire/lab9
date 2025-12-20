package Model;

public class Game {
    private int[][] board;
    private Difficulty difficulty;
    private String filePath;
    
    public Game(int[][] board, Difficulty difficulty) {
        // IMPORTANT: DON'T COPY THE BOARD BY VALUE
        // USE REFERENCES as specified in requirements
        this.board = board;
        this.difficulty = difficulty;
    }
    
    public int[][] getBoard() {
        return board;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setBoard(int[][] board) {
        this.board = board;
    }
    
    public int getEmptyCellCount() {
        int count = 0;
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                if(board[i][j] == 0) {
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
