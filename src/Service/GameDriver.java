package Service;

import Model.*;
import Exceptions.SolutionInvalidException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GameDriver {
    private RandomPairs randomPairs;
    private String basePath = "games";

    public GameDriver() {
        this.randomPairs = new RandomPairs();
        createDirectoryStructure();
    }

    private void createDirectoryStructure() {
        new File(basePath).mkdirs();
        new File(basePath + "/easy").mkdirs();
        new File(basePath + "/medium").mkdirs();
        new File(basePath + "/hard").mkdirs();
        new File(basePath + "/current").mkdirs();
    }

    public void generateGamesFromSolution(String sourceFilePath) throws SolutionInvalidException, IOException {
        // Load the source solution
        int[][] sourceBoard = loadBoardFromCSV(sourceFilePath);

        // Verify the source solution
        VerificationResult result = SequentialVerifier.verify(sourceBoard);

        if(result.getState() != GameState.VALID) {
            throw new SolutionInvalidException("Source solution is " + result.getState() +
                    ". Must be a fully valid Sudoku board.");
        }

        // Generate games from valid board
        generateGamesFromValidBoard(sourceBoard);
    }

    public void generateGamesFromValidBoard(int[][] sourceBoard) {
        // Generate three difficulty levels at once
        generateAndSaveGame(sourceBoard, Difficulty.EASY, 10);
        generateAndSaveGame(sourceBoard, Difficulty.MEDIUM, 20);
        generateAndSaveGame(sourceBoard, Difficulty.HARD, 25);
    }

    private void generateAndSaveGame(int[][] sourceBoard, Difficulty difficulty, int cellsToRemove) {
        // Create a copy of the source board
        int[][] gameBoard = new int[9][9];
        for(int i = 0; i < 9; i++) {
            System.arraycopy(sourceBoard[i], 0, gameBoard[i], 0, 9);
        }

        // Generate distinct random positions to remove
        List<int[]> positionsToRemove = randomPairs.generateDistinctPairs(cellsToRemove);
        for(int[] pos : positionsToRemove) {
            gameBoard[pos[0]][pos[1]] = 0; // Remove cell value
        }

        saveGame(gameBoard, difficulty);
    }

    private int[][] loadBoardFromCSV(String filePath) throws IOException {
        int[][] board = new int[9][9];
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for(int row = 0; row < 9 && row < lines.size(); row++) {
            String[] values = lines.get(row).split(",");
            for(int col = 0; col < 9 && col < values.length; col++) {
                String val = values[col].trim();
                board[row][col] = val.isEmpty() ? 0 : Integer.parseInt(val);
            }
        }
        return board;
    }

    private void saveGame(int[][] board, Difficulty difficulty) {
        String folderName = difficulty.toString().toLowerCase();
        String fileName = "game_" + System.currentTimeMillis() + ".csv";
        String filePath = basePath + "/" + folderName + "/" + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    writer.write(String.valueOf(board[i][j]));
                    if(j < 8) writer.write(",");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCurrentGame(int[][] board) {
        String filePath = basePath + "/current/game.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    writer.write(String.valueOf(board[i][j]));
                    if(j < 8) writer.write(",");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteCurrentGame() {
        File currentFolder = new File(basePath + "/current");
        if(currentFolder.exists()) {
            File[] files = currentFolder.listFiles();
            if(files != null) {
                for(File file : files) {
                    file.delete();
                }
            }
        }
    }

    public void copyGameToCurrent(int[][] board) {
        saveCurrentGame(board);

        // Clear the log file when starting a new game
        File logFile = new File(basePath + "/current/log.txt");
        if(logFile.exists()) {
            logFile.delete();
        }
    }
}