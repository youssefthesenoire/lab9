package Service;

import Model.*;
import java.io.*;
import java.util.List;

public class GameDriver {
    private RandomPairs randomPairs;
    private String basePath = "games";
    
    public GameDriver() {
        this.randomPairs = new RandomPairs();
        createDirectoryStructure();
    }
    
    private void createDirectoryStructure() {
        new File(basePath).mkdir();
        new File(basePath + "/easy").mkdir();
        new File(basePath + "/medium").mkdir();
        new File(basePath + "/hard").mkdir();
        new File(basePath + "/current").mkdir();
    }
    
    public void generateGamesFromSolution(String sourceFilePath) throws SolutionInvalidException {
        // Load and verify source solution
        int[][] sourceBoard = loadBoardFromCSV(sourceFilePath);
        VerificationResult result = SequentialVerifier.verify(sourceBoard);
        
        if(result.getState() != GameState.VALID) {
            throw new SolutionInvalidException("Source solution is not valid");
        }
        
        // Generate three difficulty levels
        generateAndSaveGame(sourceBoard, Difficulty.EASY, 10);
        generateAndSaveGame(sourceBoard, Difficulty.MEDIUM, 20);
        generateAndSaveGame(sourceBoard, Difficulty.HARD, 25);
    }
    
    private void generateAndSaveGame(int[][] sourceBoard, Difficulty difficulty, int cellsToRemove) {
        // Deep copy the board
        int[][] gameBoard = new int[9][9];
        for(int i = 0; i < 9; i++) {
            System.arraycopy(sourceBoard[i], 0, gameBoard[i], 0, 9);
        }
        
        // Remove random cells
        List<int[]> positionsToRemove = randomPairs.generateDistinctPairs(cellsToRemove);
        for(int[] pos : positionsToRemove) {
            gameBoard[pos[0]][pos[1]] = 0;
        }
        
        // Save game
        saveGame(gameBoard, difficulty);
        
        // Also save as current game if no current exists
        File currentFolder = new File(basePath + "/current");
        if(currentFolder.listFiles() == null || currentFolder.listFiles().length == 0) {
            saveCurrentGame(gameBoard);
        }
    }
    
    private int[][] loadBoardFromCSV(String filePath) {
        // Similar to CSVDatabaseManager from Lab 9
        int[][] board = new int[9][9];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int row = 0;
            while((line = reader.readLine()) != null && row < 9) {
                String[] values = line.split(",");
                for(int col = 0; col < 9; col++) {
                    board[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return board;
    }
    
    private void saveGame(int[][] board, Difficulty difficulty) {
        String fileName = "game_" + System.currentTimeMillis() + ".csv";
        String filePath = basePath + "/" + difficulty.toString().toLowerCase() + "/" + fileName;
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    writer.write(String.valueOf(board[i][j]));
                    if(j < 8) writer.write(",");
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveCurrentGame(int[][] board) {
        String filePath = basePath + "/current/game.csv";
        saveGame(board, null); // Overloaded version
    }
}
