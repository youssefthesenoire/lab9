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
    private JButton mainMenuButton;
    private boolean hasUnsavedChanges = false;

    public SudokuGUI(ControllerFacade controller, GameController gameController, Game game, MainGUI mainGUI, MusicPlayer musicPlayer, boolean musicEnabled) {
        this.controller = controller;
        this.gameController = gameController;
        this.currentGame = game;
        this.mainGUI = mainGUI;
        this.musicPlayer = musicPlayer;
        this.musicEnabled = musicEnabled;
        initializeUI();
        updateSolveButtonState();
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
        int top = (row % 3 == 0) ? 3 : 1;
        int left = (col % 3 == 0) ? 3 : 1;
        int bottom = (row == 8 || (row + 1) % 3 == 0) ? 3 : 1;
        int right = (col == 8 || (col + 1) % 3 == 0) ? 3 : 1;
        button.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
        if(currentGame.isFixedCell(row, col)) {
            button.setBackground(new Color(240, 240, 240));
            button.setForeground(Color.BLACK);
            button.setEnabled(false);
        } else {
            button.setBackground(Color.WHITE);
            if(currentGame.getBoard()[row][col] != 0) {
                button.setForeground(new Color(0, 100, 200));
            } else {
                button.setForeground(new Color(0, 100, 200));
            }
            button.addActionListener(new CellClickListener(row, col));
        }
        return button;
    }
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(240, 240, 240));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        verifyButton = createStyledButton("VERIFY", new Color(50, 150, 50));
        solveButton = createStyledButton("SOLVE", new Color(50, 100, 200));
        undoButton = createStyledButton("UNDO", new Color(200, 150, 50));
        mainMenuButton = createStyledButton("MAIN MENU", new Color(200, 100, 50));
        Color musicButtonColor = new Color(128, 0, 128); // Purple color
        JButton musicButton = new JButton("Music: " + (musicEnabled ? "ON" : "OFF"));
        musicButton.setFont(new Font("Arial", Font.BOLD, 12));
        if (musicEnabled) {
            musicButton.setBackground(musicButtonColor);
            musicButton.setForeground(Color.WHITE);
        } else {
            musicButton.setBackground(new Color(200, 180, 220)); // Lighter purple when off
            musicButton.setForeground(Color.BLACK);
        }
        musicButton.setFocusPainted(false);
        musicButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        musicButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (musicEnabled) {
                    musicButton.setBackground(musicButtonColor.brighter());
                } else {
                    musicButton.setBackground(new Color(220, 200, 240));
                }
            }
            public void mouseExited(MouseEvent evt) {
                if (musicEnabled) {
                    musicButton.setBackground(musicButtonColor);
                } else {
                    musicButton.setBackground(new Color(200, 180, 220));
                }
            }
        });
        verifyButton.addActionListener(e -> verifyBoard());
        solveButton.addActionListener(e -> solveGame());
        undoButton.addActionListener(e -> undoMove());
        mainMenuButton.addActionListener(e -> returnToMainMenu());
        musicButton.addActionListener(e -> {
            musicEnabled = !musicEnabled;
            if (musicPlayer != null) {
                if (musicEnabled) {
                    musicPlayer.playInGameMusic();
                    musicButton.setBackground(musicButtonColor);
                    musicButton.setForeground(Color.WHITE);
                } else {
                    musicPlayer.stopMusic();
                    musicButton.setBackground(new Color(200, 180, 220));
                    musicButton.setForeground(Color.BLACK);
                }
            }
            musicButton.setText("Music: " + (musicEnabled ? "ON" : "OFF"));
        });
        verifyButton.setToolTipText("Verify current board");
        solveButton.setToolTipText("Solve puzzle (available when exactly 5 cells empty)");
        undoButton.setToolTipText("Undo last move");
        musicButton.setToolTipText("Toggle background music");
        mainMenuButton.setToolTipText("Return to main menu");
        controlPanel.add(verifyButton);
        controlPanel.add(solveButton);
        controlPanel.add(undoButton);
        controlPanel.add(musicButton);
        controlPanel.add(mainMenuButton);
        return controlPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(220, 220, 230));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        String difficulty = currentGame.getDifficulty() != null ?
                currentGame.getDifficulty().toString() : "Unfinished";
        JLabel difficultyLabel = new JLabel("Game Mode: " + difficulty);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel emptyCellsLabel = new JLabel("Empty cells when started: " + currentGame.getEmptyCellCount());
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
                        "This cell is a preloaded clue and cannot be edited!",
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
                    updateSolveButtonState();
                } catch (NumberFormatException ex) {
                }
            }
        }
    }
    private void verifyBoard() {
        boolean[][] cellValidity = controller.verifyGame(currentGame.getBoard());
        resetAllCellColors();
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
                if (musicEnabled && musicPlayer != null) {
                    try {
                        musicPlayer.playSuccessMusic();
                    } catch (Exception e) {
                        System.err.println("Error playing success music: " + e.getMessage());
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "CONGRATULATIONS!\n\n" +
                                "The Sudoku is COMPLETE and VALID!\n\n" +
                                "All games will be deleted and you will return to the main menu.",
                        "Game Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                gameController.deleteCompletedGame(currentGame);
                SwingUtilities.invokeLater(() -> {
                    mainGUI.showLoadSolvedDialog();
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Board is Valid\n\n" +
                                "But still incomplete.\n" +
                                "Empty cells remaining: " + currentGame.getEmptyCellCount(),
                        "Valid",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Board is Invalid\n\n");
            message.append("Duplicate values found at cells:\n");
            int count = 0;
            for(String cell : invalidCells) {
                String[] coords = cell.split(",");
                int row = Integer.parseInt(coords[0]) + 1;
                int col = Integer.parseInt(coords[1]) + 1;
                message.append("(").append(row).append(",").append(col).append(") ");
                count++;
                if(count % 4 == 0) message.append("\n");
            }
            message.append("\n\nNote: Highlighted cells contain duplicate values");
            JOptionPane.showMessageDialog(this, message.toString(),
                    "Invalid Board",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void resetAllCellColors() {
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                if(currentGame.isFixedCell(row, col)) {
                    cellButtons[row][col].setBackground(new Color(240, 240, 240));
                    cellButtons[row][col].setForeground(Color.BLACK);
                } else {
                    cellButtons[row][col].setBackground(Color.WHITE);
                    if(currentGame.getBoard()[row][col] != 0) {
                        cellButtons[row][col].setForeground(new Color(0, 100, 200));
                    } else {
                        cellButtons[row][col].setForeground(new Color(0, 100, 200));
                    }
                }
            }
        }
    }
    private void solveGame() {
        if(currentGame.getEmptyCellCount() != 5) {
            JOptionPane.showMessageDialog(this,
                    "Cannot Solve\n\n" +
                            "Solve feature is only available when exactly 5 cells are empty.\n" +
                            "Current empty cells: " + currentGame.getEmptyCellCount(),
                    "Cannot Solve",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this,
                "Solve the puzzle?\n\n" +
                        "This will fill all empty cells.",
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
                    UserAction userAction = new UserAction(row, col, value, 0);
                    try {
                        controller.logUserAction(userAction);
                    } catch (IOException ex) {
                    }
                }
                updateBoardDisplay();
                hasUnsavedChanges = true;
                updateSolveButtonState();
                JOptionPane.showMessageDialog(this,
                        "Puzzle Solved!\n\n" +
                                "All cells have been filled correctly.",
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
            List<String> lines = Files.readAllLines(logFile.toPath());
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
            if(!currentGame.isFixedCell(row, col)) {
                currentGame.setCellValue(row, col, previousValue);
                lines.remove(lines.size() - 1);
                Files.write(logFile.toPath(), lines);
                updateBoardDisplay();
                hasUnsavedChanges = true;
                updateSolveButtonState();
                JOptionPane.showMessageDialog(this,
                        "Last move undone",
                        "Undo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot undo fixed clue cell",
                        "Undo Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error undoing move: " + e.getMessage(),
                    "Undo Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void returnToMainMenu() {
        if(hasUnsavedChanges) {
            int response = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes.\n" +
                            "Choose 'No' to delete all progress and return to main menu.",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.YES_OPTION) {
                mainGUI.returnToMainMenu();
            } else if(response == JOptionPane.NO_OPTION) {
                gameController.deleteCurrentGameFiles();
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
            solveButton.setToolTipText("Solve disabled - requires 5 empty cells or less (current: " + emptyCells + ")");
        }
    }
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
}