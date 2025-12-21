package UI;

import Controller.ControllerFacade;
import Controller.GameController;
import Model.*;
import Utility.MusicPlayer;
import Exceptions.InvalidGameException;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SudokuGUI extends JPanel {
    private ControllerFacade controller;
    private GameController gameController;
    private Game currentGame;
    private MainGUI mainGUI;
    private MusicPlayer musicPlayer;
    private boolean musicEnabled;

    private JButton[][] cellButtons = new JButton[9][9];
    private JButton verifyButton;
    private JButton solveButton;
    private JButton undoButton;
    private JButton newGameButton;
    private JButton mainMenuButton;

    private boolean hasUnsavedChanges = false;
    private Map<String, Color> boxColors = new HashMap<>();

    public SudokuGUI(ControllerFacade controller, GameController gameController, Game game, MainGUI mainGUI, MusicPlayer musicPlayer, boolean musicEnabled) {
        this.controller = controller;
        this.gameController = gameController;
        this.currentGame = game;
        this.mainGUI = mainGUI;
        this.musicPlayer = musicPlayer;
        this.musicEnabled = musicEnabled;

        initializeBoxColors();
        initializeUI();
        updateSolveButtonState();
    }

    private void initializeBoxColors() {
        Color[] colors = {
                new Color(255, 230, 230),
                new Color(230, 255, 230),
                new Color(230, 230, 255),
                new Color(255, 255, 230),
                new Color(255, 230, 255),
                new Color(230, 255, 255),
                new Color(255, 240, 220),
                new Color(240, 255, 240),
                new Color(240, 240, 255)
        };

        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                int boxIndex = boxRow * 3 + boxCol;
                boxColors.put(boxRow + "," + boxCol, colors[boxIndex % colors.length]);
            }
        }
    }

    private Color getBoxColor(int row, int col) {
        int boxRow = row / 3;
        int boxCol = col / 3;
        return boxColors.get(boxRow + "," + boxCol);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel gridPanel = createGridPanel();
        JPanel controlPanel = createControlPanel();
        JPanel infoPanel = createInfoPanel();

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        updateBoardDisplay();
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 0, 0));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                JButton button = createCellButton(row, col);
                cellButtons[row][col] = button;
                gridPanel.add(button);
            }
        }

        return gridPanel;
    }

    private JButton createCellButton(int row, int col) {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        Color boxColor = getBoxColor(row, col);
        int top = (row % 3 == 0) ? 3 : 1;
        int left = (col % 3 == 0) ? 3 : 1;
        int bottom = (row == 8 || (row + 1) % 3 == 0) ? 3 : 1;
        int right = (col == 8 || (col + 1) % 3 == 0) ? 3 : 1;

        button.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));

        if(currentGame.isFixedCell(row, col)) {
            button.setBackground(new Color(
                    Math.min(boxColor.getRed() - 30, 255),
                    Math.min(boxColor.getGreen() - 30, 255),
                    Math.min(boxColor.getBlue() - 30, 255)
            ));
            button.setForeground(Color.BLACK);
            button.setEnabled(false);
        } else {
            button.setBackground(boxColor);
            button.setForeground(new Color(0, 100, 200));
            button.addActionListener(new CellClickListener(row, col));
        }

        return button;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(240, 240, 245));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        verifyButton = createStyledButton("VERIFY", new Color(50, 150, 50));
        solveButton = createStyledButton("SOLVE", new Color(50, 100, 200));
        undoButton = createStyledButton("UNDO", new Color(200, 150, 50));
        newGameButton = createStyledButton("NEW GAME", new Color(100, 100, 100));
        mainMenuButton = createStyledButton("MAIN MENU", new Color(200, 100, 50));
        verifyButton.addActionListener(e -> verifyBoard());
        solveButton.addActionListener(e -> solveGame());
        undoButton.addActionListener(e -> undoMove());
        newGameButton.addActionListener(e -> newGame());
        mainMenuButton.addActionListener(e -> returnToMainMenu());
        verifyButton.setToolTipText("Verify current board");
        solveButton.setToolTipText("Solve puzzle (available when exactly 5 cells empty)");
        undoButton.setToolTipText("Undo last move");
        newGameButton.setToolTipText("Start a new game");
        mainMenuButton.setToolTipText("Return to main menu");
        controlPanel.add(verifyButton);
        controlPanel.add(solveButton);
        controlPanel.add(undoButton);
        controlPanel.add(createMusicButton());
        controlPanel.add(newGameButton);
        controlPanel.add(mainMenuButton);
        return controlPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(220, 220, 230));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        String difficulty = currentGame.getDifficulty() != null ?
                currentGame.getDifficulty().toString() : "Unfinished";
        JLabel difficultyLabel = new JLabel("Difficulty: " + difficulty);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel emptyCellsLabel = new JLabel("Empty cells: " + currentGame.getEmptyCellCount());
        emptyCellsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(difficultyLabel, BorderLayout.WEST);
        infoPanel.add(emptyCellsLabel, BorderLayout.EAST);

        return infoPanel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private class CellClickListener implements ActionListener {
        private int row;
        private int col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(currentGame.isFixedCell(row, col)) {
                JOptionPane.showMessageDialog(SudokuGUI.this,
                        "This cell is fixed and cannot be edited!",
                        "Fixed Cell",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object[] possibilities = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "Clear"};
            String selected = (String)JOptionPane.showInputDialog(
                    SudokuGUI.this,
                    "Select value for cell (" + (row+1) + "," + (col+1) + "):",
                    "Enter Value",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities,
                    currentGame.getBoard()[row][col] == 0 ? "1" : String.valueOf(currentGame.getBoard()[row][col])
            );
            if(selected != null) {
                try {
                    int value = selected.equals("Clear") ? 0 : Integer.parseInt(selected);
                    int previousValue = currentGame.getBoard()[row][col];

                    if(value == previousValue) {
                        return;
                    }
                    currentGame.setCellValue(row, col, value);
                    updateBoardDisplay();
                    hasUnsavedChanges = true;
                    UserAction userAction = new UserAction(row, col, value, previousValue);
                    try {
                        controller.logUserAction(userAction);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(SudokuGUI.this,
                                "Error logging move: " + ex.getMessage(),
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    gameController.saveCurrentGame(currentGame);
                    updateSolveButtonState();

                } catch (NumberFormatException ex) {
                    // Should not happen with our options
                }
            }
        }
    }

    private void verifyBoard() {
        // Use the ControllerFacade to get boolean array validation
        boolean[][] cellValidity = controller.verifyGame(currentGame.getBoard());

        // Reset all cells to normal first
        resetAllCellColors();

        // Check if all cells are valid and if board is complete
        boolean allValid = true;
        boolean hasEmpty = false;
        Set<String> invalidCells = new HashSet<>();

        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                if(!cellValidity[i][j]) {
                    allValid = false;
                    invalidCells.add(i + "," + j);
                    cellButtons[i][j].setBackground(Color.RED);
                    cellButtons[i][j].setForeground(Color.WHITE);
                }
                if(currentGame.getBoard()[i][j] == 0) {
                    hasEmpty = true;
                }
            }
        }

        if(allValid) {
            if(!hasEmpty) {
                // Game is complete and valid
                if (musicEnabled && musicPlayer != null) {
                    try {
                        musicPlayer.playSuccessMusic();
                    } catch (Exception e) {
                        System.err.println("Error playing success music: " + e.getMessage());
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align: center;'>" +
                                "<h1 style='color: green;'>CONGRATULATIONS!</h1>" +
                                "<p style='font-size: 14pt;'>The Sudoku is <b>COMPLETE</b> and <b>VALID</b>!</p>" +
                                "</div></html>",
                        "Game Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                gameController.deleteCompletedGame(currentGame);
            } else {
                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align: center;'>" +
                                "<h3 style='color: blue;'>Board is Valid</h3>" +
                                "<p>But still incomplete.</p>" +
                                "<p>Empty cells remaining: <b>" + currentGame.getEmptyCellCount() + "</b></p>" +
                                "</div></html>",
                        "Valid",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Board has invalid cells
            StringBuilder message = new StringBuilder();
            message.append("<html><div style='text-align: center;'>");
            message.append("<h2 style='color: red;'>Board is Invalid</h2>");
            message.append("<p>Duplicate values found at cells:</p>");
            message.append("<p><b>");

            int count = 0;
            for(String cell : invalidCells) {
                String[] coords = cell.split(",");
                int row = Integer.parseInt(coords[0]) + 1;
                int col = Integer.parseInt(coords[1]) + 1;
                message.append("(").append(row).append(",").append(col).append(") ");
                count++;
                if(count % 4 == 0) message.append("<br>");
            }

            message.append("</b></p>");
            message.append("<p style='color: gray; font-size: 10pt;'>");
            message.append("Note: Highlighted cells contain duplicate values</p>");
            message.append("</div></html>");

            JOptionPane.showMessageDialog(this, message.toString(),
                    "Invalid Board",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetAllCellColors() {
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                Color boxColor = getBoxColor(row, col);
                if(currentGame.isFixedCell(row, col)) {
                    cellButtons[row][col].setBackground(new Color(
                            Math.min(boxColor.getRed() - 30, 255),
                            Math.min(boxColor.getGreen() - 30, 255),
                            Math.min(boxColor.getBlue() - 30, 255)
                    ));
                    cellButtons[row][col].setForeground(Color.BLACK);
                } else {
                    cellButtons[row][col].setBackground(boxColor);
                    cellButtons[row][col].setForeground(new Color(0, 100, 200));
                }
            }
        }
    }

    private void solveGame() {
        if(currentGame.getEmptyCellCount() != 5) {
            JOptionPane.showMessageDialog(this,
                    "<html><div style='text-align: center;'>" +
                            "<h3 style='color: orange;'>Cannot Solve</h3>" +
                            "<p>Solve feature is only available when exactly 5 cells are empty.</p>" +
                            "<p>Current empty cells: <b>" + currentGame.getEmptyCellCount() + "</b></p>" +
                            "</div></html>",
                    "Cannot Solve",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
                "<html><div style='text-align: center;'>" +
                        "<p>Solve the puzzle?</p>" +
                        "<p>This will fill all empty cells.</p>" +
                        "</div></html>",
                "Solve Puzzle",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if(response == JOptionPane.YES_OPTION) {
            try {
                int[][] solution = controller.solveGame(currentGame.getBoard());

                for(int[] cell : solution) {
                    int row = cell[0];
                    int col = cell[1];
                    int value = cell[2];
                    currentGame.setCellValue(row, col, value);
                }

                updateBoardDisplay();
                hasUnsavedChanges = true;
                gameController.saveCurrentGame(currentGame);
                updateSolveButtonState();

                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align: center;'>" +
                                "<h3 style='color: green;'>Puzzle Solved!</h3>" +
                                "<p>All cells have been filled correctly.</p>" +
                                "</div></html>",
                        "Solved",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (InvalidGameException e) {
                JOptionPane.showMessageDialog(this,
                        "Error solving puzzle: " + e.getMessage(),
                        "Solve Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void undoMove() {
        File logFile = new File("games/current/log.txt");

        if(!logFile.exists() || logFile.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No moves to undo",
                    "Undo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            java.util.List<String> lines = Files.readAllLines(logFile.toPath());

            if(lines.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No moves to undo",
                        "Undo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String lastLine = lines.get(lines.size() - 1);
            lastLine = lastLine.replaceAll("[()]", "");
            String[] parts = lastLine.split(", ");

            if(parts.length != 4) {
                throw new IOException("Invalid log format");
            }

            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            int previousValue = Integer.parseInt(parts[3]);

            currentGame.setCellValue(row, col, previousValue);

            lines.remove(lines.size() - 1);
            Files.write(logFile.toPath(), lines);

            updateBoardDisplay();
            gameController.saveCurrentGame(currentGame);
            hasUnsavedChanges = true;
            updateSolveButtonState();

            JOptionPane.showMessageDialog(this,
                    "Last move undone",
                    "Undo",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error undoing move: " + e.getMessage(),
                    "Undo Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newGame() {
        int response = JOptionPane.showConfirmDialog(this,
                "Start a new game? Current progress will be saved.",
                "New Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if(response == JOptionPane.YES_OPTION) {
            saveGame();
            mainGUI.returnToMainMenu();
        }
    }

    private void returnToMainMenu() {
        if(hasUnsavedChanges) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Save current game before returning to main menu?",
                    "Save Game",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if(response == JOptionPane.YES_OPTION) {
                saveGame();
                mainGUI.returnToMainMenu();
            } else if(response == JOptionPane.NO_OPTION) {
                mainGUI.returnToMainMenu();
            }
        } else {
            mainGUI.returnToMainMenu();
        }
    }

    private void updateBoardDisplay() {
        int[][] board = currentGame.getBoard();

        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                JButton button = cellButtons[row][col];
                int value = board[row][col];

                if(value == 0) {
                    button.setText("");
                } else {
                    button.setText(String.valueOf(value));
                }
            }
        }
    }

    private void updateSolveButtonState() {
        int emptyCells = currentGame.getEmptyCellCount();
        solveButton.setEnabled(emptyCells == 5);

        if(emptyCells == 5) {
            solveButton.setBackground(new Color(0, 180, 0));
            solveButton.setToolTipText("Solve enabled - exactly 5 empty cells");
        } else {
            solveButton.setBackground(new Color(50, 100, 200));
            solveButton.setToolTipText("Solve disabled - requires exactly 5 empty cells (current: " + emptyCells + ")");
        }
    }

    public void saveGame() {
        gameController.saveCurrentGame(currentGame);
        hasUnsavedChanges = false;
        JOptionPane.showMessageDialog(this,
                "Game saved successfully",
                "Save",
                JOptionPane.INFORMATION_MESSAGE);
    }
    private JButton createMusicButton() {
        JButton musicButton = new JButton("Music: " + (musicEnabled ? "ON" : "OFF"));

        musicButton.setFont(new Font("Arial", Font.PLAIN, 12));
        musicButton.setFocusPainted(false);
        musicButton.setBackground(new Color(200, 200, 200));

        musicButton.addActionListener(e -> {
            musicEnabled = !musicEnabled;
            if (musicPlayer != null) {
                if (musicEnabled) {
                    // Always play starting music when turning on from main menu
                    musicPlayer.playInGameMusic();
                } else {
                    musicPlayer.stopMusic();
                }
            }
            musicButton.setText("Music: " + (musicEnabled ? "ON" : "OFF"));
            musicButton.setBackground(musicEnabled ?
                    new Color(180, 230, 180) : new Color(200, 200, 200));
        });

        return musicButton;
    }


    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
}