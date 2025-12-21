package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SudokuGUI extends JFrame {
    private JButton[][] cellButtons = new JButton[9][9];
    private int[][] currentBoard = new int[9][9];
    private Controller.GameController controller;
    private JButton solveButton;

    public SudokuGUI() {
        controller = new Controller.GameController();
        initializeUI();
        loadInitialGame();
    }

    private void initializeUI() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create grid panel with 9x9 grid
        JPanel gridPanel = new JPanel(new GridLayout(9, 9));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons for each cell
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                JButton button = new JButton("");
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font("Arial", Font.BOLD, 20));

                final int r = row;
                final int c = col;
                button.addActionListener(e -> handleCellClick(r, c));

                cellButtons[row][col] = button;
                gridPanel.add(button);
            }
        }

        // Create control panel
        JPanel controlPanel = new JPanel();
        JButton verifyButton = new JButton("Verify");
        solveButton = new JButton("Solve");
        JButton undoButton = new JButton("Undo");

        verifyButton.addActionListener(e -> handleVerify());
        solveButton.addActionListener(e -> handleSolve());
        undoButton.addActionListener(e -> handleUndo());

        // Disable solve button initially
        solveButton.setEnabled(false);

        controlPanel.add(verifyButton);
        controlPanel.add(solveButton);
        controlPanel.add(undoButton);

        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadInitialGame() {
        // Initialize with empty board
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                currentBoard[i][j] = 0;
                cellButtons[i][j].setText("");
            }
        }

        // In a real implementation, you would load from controller
        // For now, just show a sample board
        showSampleBoard();
    }

    private void showSampleBoard() {
        // Sample Sudoku board (partially filled)
        int[][] sample = {
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        currentBoard = sample;
        updateBoardDisplay();
    }

    private void handleCellClick(int row, int col) {
        String input = JOptionPane.showInputDialog(this, "Enter number (1-9) or 0 to clear:");
        try {
            int value = Integer.parseInt(input);
            if(value >= 0 && value <= 9) {
                int previousValue = currentBoard[row][col];
                currentBoard[row][col] = value;
                updateBoardDisplay();

                // Log the action
                String logEntry = "(" + row + ", " + col + ", " + value + ", " + previousValue + ")";
                try {
                    controller.logUserAction(logEntry);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to log action: " + e.getMessage());
                }

                // Update solve button state
                updateSolveButtonState();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter number between 0-9");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void handleVerify() {
        String result = controller.verifyGame(new Model.Game(currentBoard, null));

        if(result.equals("valid")) {
            JOptionPane.showMessageDialog(this, "Valid Sudoku!");
        } else if(result.equals("incomplete")) {
            JOptionPane.showMessageDialog(this, "Board is incomplete");
        } else if(result.startsWith("invalid")) {
            JOptionPane.showMessageDialog(this, "Invalid board with duplicates");
            // Parse and highlight invalid cells
            highlightInvalidCells(result);
        }
    }

    private void handleSolve() {
        try {
            int[] solution = controller.solveGame(new Model.Game(currentBoard, null));
            // Apply solution to board
            applySolution(solution);
            JOptionPane.showMessageDialog(this, "Game solved!");
        } catch (Exceptions.InvalidGameException e) {
            JOptionPane.showMessageDialog(this, "Cannot solve: " + e.getMessage());
        }
    }

    private void handleUndo() {
        File logFile = new File("games/current/log.txt");
        if(logFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(logFile.toPath());
                if(!lines.isEmpty()) {
                    String lastLine = lines.get(lines.size() - 1);
                    // Parse: (row, col, value, previous)
                    lastLine = lastLine.replaceAll("[()]", "");
                    String[] parts = lastLine.split(", ");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    int previousValue = Integer.parseInt(parts[3]);

                    // Restore previous value
                    currentBoard[row][col] = previousValue;
                    updateBoardDisplay();

                    // Remove last line
                    lines.remove(lines.size() - 1);
                    Files.write(logFile.toPath(), lines);

                    updateSolveButtonState();
                } else {
                    JOptionPane.showMessageDialog(this, "No actions to undo");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading log file: " + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error parsing log entry");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No log file found");
        }
    }

    private void updateBoardDisplay() {
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                int value = currentBoard[row][col];
                if(value == 0) {
                    cellButtons[row][col].setText("");
                    cellButtons[row][col].setBackground(null);
                } else {
                    cellButtons[row][col].setText(String.valueOf(value));
                    cellButtons[row][col].setBackground(null);
                }
            }
        }
    }

    private void highlightInvalidCells(String result) {
        // Reset all cells
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                cellButtons[row][col].setBackground(null);
            }
        }

        // Parse invalid locations: "invalid 1,2 3,4"
        String[] parts = result.split(" ");
        for(int i = 1; i < parts.length; i++) {
            String[] coords = parts[i].split(",");
            if(coords.length == 2) {
                try {
                    int row = Integer.parseInt(coords[0]);
                    int col = Integer.parseInt(coords[1]);
                    if(row >= 0 && row < 9 && col >= 0 && col < 9) {
                        cellButtons[row][col].setBackground(Color.RED);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid coordinate
                }
            }
        }
    }

    private void applySolution(int[] encodedSolution) {
        for(int encoded : encodedSolution) {
            int row = encoded / 81;
            int col = (encoded % 81) / 9;
            int value = (encoded % 9) + 1;
            if(row >= 0 && row < 9 && col >= 0 && col < 9) {
                currentBoard[row][col] = value;
            }
        }
        updateBoardDisplay();
        updateSolveButtonState();
    }

    private void updateSolveButtonState() {
        int emptyCount = 0;
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                if(currentBoard[row][col] == 0) {
                    emptyCount++;
                }
            }
        }
        solveButton.setEnabled(emptyCount == 5);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new SudokuGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage());
            }
        });
    }
}