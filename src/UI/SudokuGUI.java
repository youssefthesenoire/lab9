package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SudokuGUI extends JFrame implements Controllable {
    private JButton[][] cellButtons = new JButton[9][9];
    private int[][] currentBoard = new int[9][9];
    private GameController controller;
    
    public SudokuGUI() {
        controller = new GameController();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create grid panel
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
        JButton solveButton = new JButton("Solve");
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
        
        // Load initial game state
        loadInitialGame();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void loadInitialGame() {
        // Check catalog
        boolean[] catalog = getCatalog();
        
        if(catalog[0]) { // Has unfinished game
            loadGame('i'); // 'i' for incomplete
        } else if(catalog[1]) { // Has all difficulty levels
            // Show difficulty selection dialog
            String[] options = {"Easy", "Medium", "Hard"};
            int choice = JOptionPane.showOptionDialog(this,
                "Select difficulty level:",
                "New Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            if(choice != JOptionPane.CLOSED_OPTION) {
                char level = options[choice].charAt(0);
                loadGame(Character.toLowerCase(level));
            }
        } else {
            // Ask for solved Sudoku file
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                String sourcePath = fileChooser.getSelectedFile().getPath();
                driveGames(sourcePath);
                // After generating games, ask for difficulty
                loadInitialGame();
            }
        }
    }
    
    @Override
    public boolean[] getCatalog() {
        Catalog catalog = controller.getCatalog();
        return new boolean[]{catalog.isHasUnfinished(), catalog.isAllModesExist()};
    }
    
    @Override
    public int[][] getGame(char level) {
        try {
            Difficulty difficulty;
            switch(level) {
                case 'e': difficulty = Difficulty.EASY; break;
                case 'm': difficulty = Difficulty.MEDIUM; break;
                case 'h': difficulty = Difficulty.HARD; break;
                case 'i': // incomplete
                    // Load current game
                    return loadCurrentGame();
                default:
                    throw new IllegalArgumentException("Invalid level");
            }
            
            Game game = controller.getGame(difficulty);
            return game.getBoard();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public void driveGames(String sourcePath) {
        try {
            controller.driveGames(null); // Would need source game
            JOptionPane.showMessageDialog(this, "Games generated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating games: " + e.getMessage());
        }
    }
    
    @Override
    public boolean[][] verifyGame(int[][] game) {
        String result = controller.verifyGame(new Game(game, null));
        
        if(result.equals("valid")) {
            JOptionPane.showMessageDialog(this, "Valid Sudoku!");
            return null;
        } else if(result.equals("incomplete")) {
            JOptionPane.showMessageDialog(this, "Board is incomplete");
            return null;
        } else if(result.startsWith("invalid")) {
            JOptionPane.showMessageDialog(this, "Invalid board");
            // Parse invalid locations
            String[] parts = result.split(" ");
            boolean[][] invalidCells = new boolean[9][9];
            for(int i = 1; i < parts.length; i++) {
                String[] coords = parts[i].split(",");
                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);
                invalidCells[row][col] = true;
            }
            return invalidCells;
        }
        return null;
    }
    
    @Override
    public int[][] solveGame(int[][] game) {
        try {
            int[] encodedSolution = controller.solveGame(new Game(game, null));
            
            // Decode solution
            int[][] solvedBoard = new int[9][9];
            for(int i = 0; i < 9; i++) {
                System.arraycopy(game[i], 0, solvedBoard[i], 0, 9);
            }
            
            for(int encoded : encodedSolution) {
                int row = encoded / 81;
                int col = (encoded % 81) / 9;
                int value = (encoded % 9) + 1;
                solvedBoard[row][col] = value;
            }
            
            return solvedBoard;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error solving game: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public void logUserAction(UserAction action) {
        // Implement logging
    }
    
    private void handleCellClick(int row, int col) {
        String input = JOptionPane.showInputDialog(this, "Enter number (1-9):");
        try {
            int value = Integer.parseInt(input);
            if(value >= 1 && value <= 9) {
                int previousValue = currentBoard[row][col];
                currentBoard[row][col] = value;
                updateBoardDisplay();
                
                // Log the action: (x, y, value, previous)
                String logEntry = "(" + row + ", " + col + ", " + value + ", " + previousValue + ")";
                controller.logUserAction(logEntry);
                
                // Check if solve button should be enabled
                checkSolveButton();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter number between 1-9");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }
    
    private void handleVerify() {
        boolean[][] invalidCells = verifyGame(currentBoard);
        if(invalidCells != null) {
            // Highlight invalid cells in red
            for(int row = 0; row < 9; row++) {
                for(int col = 0; col < 9; col++) {
                    if(invalidCells[row][col]) {
                        cellButtons[row][col].setBackground(Color.RED);
                    } else {
                        cellButtons[row][col].setBackground(null);
                    }
                }
            }
        }
    }
    
    private void handleSolve() {
        int[][] solution = solveGame(currentBoard);
        if(solution != null) {
            currentBoard = solution;
            updateBoardDisplay();
        }
    }
    
    private void handleUndo() {
        // Read last action from log file and reverse it
        try {
            File logFile = new File("games/current/log.txt");
            if(logFile.exists()) {
                List<String> lines = Files.readAllLines(logFile.toPath());
                if(!lines.isEmpty()) {
                    String lastLine = lines.get(lines.size() - 1);
                    // Parse: (x, y, value, previous)
                    lastLine = lastLine.replaceAll("[()]", "");
                    String[] parts = lastLine.split(", ");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    int previousValue = Integer.parseInt(parts[3]);
                    
                    // Restore previous value
                    currentBoard[row][col] = previousValue;
                    updateBoardDisplay();
                    
                    // Remove last line from log
                    lines.remove(lines.size() - 1);
                    Files.write(logFile.toPath(), lines);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void updateBoardDisplay() {
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                int value = currentBoard[row][col];
                if(value == 0) {
                    cellButtons[row][col].setText("");
                } else {
                    cellButtons[row][col].setText(String.valueOf(value));
                }
            }
        }
    }
    
    private void checkSolveButton() {
        // Count empty cells
        int emptyCount = 0;
        for(int row = 0; row < 9; row++) {
            for(int col = 0; col < 9; col++) {
                if(currentBoard[row][col] == 0) {
                    emptyCount++;
                }
            }
        }
        
        // Enable solve button only when exactly 5 empty cells
        // This would need to find the solve button in the UI
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGUI());
    }
}
