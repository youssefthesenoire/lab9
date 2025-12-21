package Controller;

import Interfaces.Viewable;
import Model.*;
import Service.*;
import Exceptions.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class GameController implements Viewable {
    private Game currentGame;
    private final GameDriver gameDriver;
    private final String logFilePath = "games/current/log.txt";

    public GameController() {
        this.gameDriver = new GameDriver();
    }

    @Override
    public Catalog getCatalog() {
        boolean hasUnfinished = checkUnfinishedGame();
        boolean allModesExist = checkAllDifficultyGamesExist();
        return new Catalog(hasUnfinished, allModesExist);
    }

    @Override
    public Game getGame(Difficulty level) throws NotFoundException {
        File folder = new File("games/" + level.toString().toLowerCase());
        File[] files = folder.listFiles();

        if(files == null || files.length == 0) {
            throw new NotFoundException("No games found for difficulty: " + level);
        }

        Random random = new Random();
        File selectedFile = files[random.nextInt(files.length)];

        int[][] board = loadBoardFromFile(selectedFile);
        Game game = new Game(board, level);
        this.currentGame = game;
        saveCurrentGame(board);

        return game;
    }

    @Override
    public void driveGames(Game sourceGame) throws SolutionInvalidException {
        // Implementation would generate games from source solution
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String verifyGame(Game game) {
        VerificationResult result = SequentialVerifier.verify(game.getBoard());

        switch(result.getState()) {
            case VALID:
                if(game.isComplete()) {
                    deleteCompletedGame(game);
                }
                return "valid";

            case INCOMPLETE:
                return "incomplete";

            case INVALID:
                StringBuilder sb = new StringBuilder("invalid");
                for(LocationOnBoard loc : result.getDuplicateLocations()) {
                    sb.append(" ").append(loc.toString());
                }
                return sb.toString();

            default:
                return "error";
        }
    }

    @Override
    public int[] solveGame(Game game) throws InvalidGameException {
        try {
            return SudokuSolver.solve(game);
        } catch (Exception e) {
            throw new InvalidGameException("Failed to solve game: " + e.getMessage(), e);
        }
    }

    @Override
    public void logUserAction(String userAction) throws IOException {
        File logFile = new File(logFilePath);
        if(!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(userAction);
            writer.newLine();
        }
    }

    private boolean checkUnfinishedGame() {
        File currentFolder = new File("games/current");
        File gameFile = new File(currentFolder, "game.csv");
        File logFile = new File(currentFolder, "log.txt");

        return gameFile.exists() && logFile.exists();
    }

    private boolean checkAllDifficultyGamesExist() {
        File easyFolder = new File("games/easy");
        File mediumFolder = new File("games/medium");
        File hardFolder = new File("games/hard");

        return easyFolder.exists() && easyFolder.list() != null && easyFolder.list().length > 0 &&
                mediumFolder.exists() && mediumFolder.list() != null && mediumFolder.list().length > 0 &&
                hardFolder.exists() && hardFolder.list() != null && hardFolder.list().length > 0;
    }

    private int[][] loadBoardFromFile(File file) {
        int[][] board = new int[9][9];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int row = 0;
            while((line = reader.readLine()) != null && row < 9) {
                String[] values = line.split(",");
                for(int col = 0; col < 9 && col < values.length; col++) {
                    board[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }
            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            // Return empty board on error
            for(int i = 0; i < 9; i++) {
                Arrays.fill(board[i], 0);
            }
        }
        return board;
    }

    private void saveCurrentGame(int[][] board) {
        File currentFolder = new File("games/current");
        if(!currentFolder.exists()) {
            currentFolder.mkdirs();
        }

        File gameFile = new File(currentFolder, "game.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(gameFile))) {
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

    private void deleteCompletedGame(Game game) {
        // Delete from difficulty folder
        File difficultyFolder = new File("games/" + game.getDifficulty().toString().toLowerCase());
        if(difficultyFolder.exists()) {
            // Find and delete the corresponding game file
            File[] files = difficultyFolder.listFiles();
            if(files != null) {
                for(File file : files) {
                    // Simple approach: delete one file (could be enhanced to find exact match)
                    file.delete();
                    break;
                }
            }
        }

        // Delete current game
        File currentFolder = new File("games/current");
        File[] currentFiles = currentFolder.listFiles();
        if(currentFiles != null) {
            for(File file : currentFiles) {
                file.delete();
            }
        }
    }
}