package Controller;

import Interfaces.Viewable;
import Interfaces.Controllable;
import Model.*;
import Exceptions.*;
import UI.UserAction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ControllerFacade implements Controllable {
    private final Viewable viewable;

    public ControllerFacade(Viewable viewable) {
        this.viewable = viewable;
    }

    @Override
    public boolean[] getCatalog() {
        Catalog catalog = viewable.getCatalog();
        return new boolean[]{catalog.isHasUnfinished(), catalog.isAllModesExist()};
    }

    @Override
    public int[][] getGame(char level) throws NotFoundException {
        Difficulty difficulty;
        switch(Character.toUpperCase(level)) {
            case 'E': difficulty = Difficulty.EASY; break;
            case 'M': difficulty = Difficulty.MEDIUM; break;
            case 'H': difficulty = Difficulty.HARD; break;
            default: throw new NotFoundException("Invalid level character: " + level);
        }

        Game game = viewable.getGame(difficulty);
        return game.getBoard();
    }

    @Override
    public void driveGames(String sourcePath) throws SolutionInvalidException, IOException {
        // Actually, the Viewable.driveGames() should handle loading from file
        // Based on the lab requirements, we need to create a temporary Game object
        // with the loaded board and pass it to viewable.driveGames()

        try {
            // Load the board from file
            int[][] board = loadBoardFromFile(sourcePath);

            // Create a Game object with the loaded board
            Game sourceGame = new Game(board, null);

            // Pass the Game object to the viewable implementation
            viewable.driveGames(sourceGame);

        } catch (IOException e) {
            throw new IOException("Failed to load Sudoku file: " + e.getMessage(), e);
        } catch (SolutionInvalidException e) {
            throw e; // Re-throw SolutionInvalidException
        } catch (Exception e) {
            throw new IOException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean[][] verifyGame(int[][] game) {
        Game gameObj = new Game(game, null);
        String result = viewable.verifyGame(gameObj);

        boolean[][] cellValidity = new boolean[9][9];

        // Initialize all cells as valid
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                cellValidity[i][j] = true;
            }
        }

        // Mark invalid cells if result is "invalid"
        if(result.startsWith("invalid")) {
            String[] parts = result.split(" ");
            for(int i = 1; i < parts.length; i++) {
                String[] coords = parts[i].split(",");
                if(coords.length == 2) {
                    try {
                        int row = Integer.parseInt(coords[0]);
                        int col = Integer.parseInt(coords[1]);
                        if(row >= 0 && row < 9 && col >= 0 && col < 9) {
                            cellValidity[row][col] = false;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid coordinate
                        System.err.println("Invalid coordinate format: " + parts[i]);
                    }
                }
            }
        }

        return cellValidity;
    }

    @Override
    public int[][] solveGame(int[][] game) throws InvalidGameException {
        Game gameObj = new Game(game, null);
        int[] solution = viewable.solveGame(gameObj);

        if(solution == null || solution.length == 0) {
            throw new InvalidGameException("No solution found");
        }

        // Convert to format: each row is [x, y, value]
        int[][] result = new int[solution.length][3];
        for(int i = 0; i < solution.length; i++) {
            int encoded = solution[i];
            int row = encoded / 81;
            int col = (encoded % 81) / 9;
            int value = (encoded % 9) + 1;
            result[i][0] = row;
            result[i][1] = col;
            result[i][2] = value;
        }

        return result;
    }

    @Override
    public void logUserAction(UserAction userAction) throws IOException {
        String logEntry = "(" + userAction.getRow() + ", " + userAction.getCol() +
                ", " + userAction.getValue() + ", " + userAction.getPreviousValue() + ")";
        viewable.logUserAction(logEntry);
    }

    private int[][] loadBoardFromFile(String filePath) throws IOException {
        int[][] board = new int[9][9];

        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);

            int row = 0;
            for(String line : lines) {
                if(row >= 9) break; // Only read first 9 rows
                if(line.trim().isEmpty()) continue; // Skip empty lines

                String[] values = line.split(",");
                for(int col = 0; col < 9 && col < values.length; col++) {
                    String val = values[col].trim();
                    board[row][col] = val.isEmpty() ? 0 : Integer.parseInt(val);
                }
                row++;
            }

            // Fill remaining rows with zeros if file has less than 9 rows
            while(row < 9) {
                for(int col = 0; col < 9; col++) {
                    board[row][col] = 0;
                }
                row++;
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format in file: " + filePath, e);
        }

        return board;
    }
}