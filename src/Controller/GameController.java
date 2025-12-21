package Controller;

import Interfaces.Viewable;
import Model.*;
import Service.*;
import Exceptions.*;

import java.io.*;
import java.util.List;
import java.util.Random;

public class GameController implements Viewable {
    private Game currentGame;
    private final GameDriver gameDriver;
    private final String basePath = "games";

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
        File folder = new File(basePath + "/" + level.toString().toLowerCase());
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));

        if(files == null || files.length == 0) {
            throw new NotFoundException("No games found for difficulty: " + level);
        }

        // Return a random game from the folder
        Random random = new Random();
        File selectedFile = files[random.nextInt(files.length)];

        try {
            int[][] board = loadBoardFromFile(selectedFile);
            Game game = new Game(board, level);
            this.currentGame = game;

            // Copy to current folder for incomplete state tracking
            gameDriver.copyGameToCurrent(board);
            initializeLogFile();

            return game;
        } catch (IOException e) {
            throw new NotFoundException("Error loading game: " + e.getMessage(), e);
        }
    }

    @Override
    public void driveGames(Game sourceGame) throws SolutionInvalidException {
        // Actually, this method should load from a file path
        // But based on the interface, it takes a Game object
        // The ControllerFacade handles the conversion

        // Verify the source solution first
        VerificationResult result = SequentialVerifier.verify(sourceGame.getBoard());

        if(result.getState() != GameState.VALID) {
            throw new SolutionInvalidException(
                    "Source solution is " + result.getState() +
                            ". Must be a fully valid Sudoku board.");
        }

        // Generate three difficulty levels
        gameDriver.generateGamesFromValidBoard(sourceGame.getBoard());
    }

    // Add a helper method for direct file path loading
    public void driveGamesFromFile(String filePath) throws SolutionInvalidException, IOException {
        int[][] board = loadBoardFromFile(new File(filePath));
        Game sourceGame = new Game(board, null);
        driveGames(sourceGame);
    }

    @Override
    public String verifyGame(Game game) {
        VerificationResult result = SequentialVerifier.verify(game.getBoard());

        switch(result.getState()) {
            case VALID:
                // Check if complete (no empty cells)
                boolean hasEmpty = false;
                int[][] board = game.getBoard();
                for(int i = 0; i < 9; i++) {
                    for(int j = 0; j < 9; j++) {
                        if(board[i][j] == 0) {
                            hasEmpty = true;
                            break;
                        }
                    }
                    if(hasEmpty) break;
                }
                return hasEmpty ? "incomplete" : "valid";

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
        if(game.getEmptyCellCount() != 5) {
            throw new InvalidGameException("Solver only works for exactly 5 empty cells");
        }

        return SudokuSolver.solve(game);
    }

    @Override
    public void logUserAction(String userAction) throws IOException {
        File logFile = new File(basePath + "/current/log.txt");
        File parentDir = logFile.getParentFile();
        if(!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(userAction);
            writer.newLine();
        }
    }

    @Override
    public void saveCurrentGame(Game game) {
        if(game != null) {
            gameDriver.saveCurrentGame(game.getBoard());
        }
    }

    @Override
    public void deleteCompletedGame(Game game) {
        // Only delete from difficulty folder if the game has a difficulty
        if(game.getDifficulty() != null) {
            String difficultyFolderPath = basePath + "/" + game.getDifficulty().toString().toLowerCase();
            File difficultyFolder = new File(difficultyFolderPath);
            if(difficultyFolder.exists() && difficultyFolder.isDirectory()) {
                File[] files = difficultyFolder.listFiles();
                if(files != null && files.length > 0) {
                    // Delete the first game in the folder
                    files[0].delete();
                }
            }
        }

        // Always delete from current folder
        gameDriver.deleteCurrentGame();
    }

    private boolean checkUnfinishedGame() {
        File currentFolder = new File(basePath + "/current");
        if(!currentFolder.exists()) {
            return false;
        }

        File gameFile = new File(currentFolder, "game.csv");
        return gameFile.exists() && gameFile.length() > 0;
    }

    private boolean checkAllDifficultyGamesExist() {
        File easyFolder = new File(basePath + "/easy");
        File mediumFolder = new File(basePath + "/medium");
        File hardFolder = new File(basePath + "/hard");

        boolean easyExists = easyFolder.exists() && easyFolder.isDirectory() &&
                easyFolder.listFiles((dir, name) -> name.endsWith(".csv")) != null &&
                easyFolder.listFiles((dir, name) -> name.endsWith(".csv")).length > 0;

        boolean mediumExists = mediumFolder.exists() && mediumFolder.isDirectory() &&
                mediumFolder.listFiles((dir, name) -> name.endsWith(".csv")) != null &&
                mediumFolder.listFiles((dir, name) -> name.endsWith(".csv")).length > 0;

        boolean hardExists = hardFolder.exists() && hardFolder.isDirectory() &&
                hardFolder.listFiles((dir, name) -> name.endsWith(".csv")) != null &&
                hardFolder.listFiles((dir, name) -> name.endsWith(".csv")).length > 0;

        return easyExists && mediumExists && hardExists;
    }

    private int[][] loadBoardFromFile(File file) throws IOException {
        int[][] board = new int[9][9];

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;

            while ((line = reader.readLine()) != null && row < 9) {
                // Handle empty lines
                if(line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                for (int col = 0; col < 9 && col < values.length; col++) {
                    String val = values[col].trim();
                    board[row][col] = val.isEmpty() ? 0 : Integer.parseInt(val);
                }
                row++;
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format in CSV file: " + file.getName(), e);
        }

        return board;
    }

    public Game loadUnfinishedGame() throws NotFoundException, IOException {
        File gameFile = new File(basePath + "/current/game.csv");
        if(!gameFile.exists() || gameFile.length() == 0) {
            throw new NotFoundException("No unfinished game found");
        }

        int[][] board = loadBoardFromFile(gameFile);
        Game game = new Game(board, null);
        this.currentGame = game;
        return game;
    }

    private void initializeLogFile() {
        File logFile = new File(basePath + "/current/log.txt");
        if(logFile.exists()) {
            logFile.delete();
        }
    }

    // Helper method for GUI
    public boolean[] getCatalogAsBooleans() {
        Catalog catalog = getCatalog();
        return new boolean[]{catalog.isHasUnfinished(), catalog.isAllModesExist()};
    }
}