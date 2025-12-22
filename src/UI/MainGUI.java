package UI;

import Controller.ControllerFacade;
import Controller.GameController;
import Model.Difficulty;
import Model.Game;
import Utility.MusicPlayer;
import Exceptions.NotFoundException;
import Exceptions.SolutionInvalidException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainGUI extends JFrame {
    private ControllerFacade controller;
    private GameController gameController;
    private SudokuGUI sudokuGUI;
    private MusicPlayer musicPlayer;
    private boolean musicEnabled = true;

    public MainGUI() {
        try {
            MusicPlayer.checkMusicFiles();
            musicPlayer = new MusicPlayer();
            musicEnabled = true;
        } catch (IOException e) {
            System.err.println("Music files not found: " + e.getMessage());
            musicEnabled = false;
        }

        gameController = new GameController();
        controller = new ControllerFacade(gameController);

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleWindowClosing();
            }
        });

        setLayout(new BorderLayout());

        boolean[] catalog = controller.getCatalog();
        boolean hasUnfinished = catalog[0];
        boolean allModesExist = catalog[1];

        if(hasUnfinished) {
            showContinueDialog();
        } else if(allModesExist) {
            showDifficultySelection();
        } else {
            showLoadSolvedDialog();
        }

        setSize(650, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showContinueDialog() {
        int response = JOptionPane.showConfirmDialog(this,
                "You have an unfinished game. Do you want to continue?",
                "Continue Game",
                JOptionPane.YES_NO_OPTION);

        if(response == JOptionPane.YES_OPTION) {
            try {
                Game game = gameController.loadUnfinishedGame();
                loadGameUI(game, "Unfinished Game");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading unfinished game: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                showDifficultySelection();
            }
        } else {
            // User chose not to continue, delete current game files
            gameController.deleteCurrentGameFiles();
            showDifficultySelection();
        }
    }

    private void showDifficultySelection() {
        getContentPane().removeAll();

        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 100, 100));
        buttonPanel.setOpaque(false);

        JButton easyButton = createStyledButton("  EASY  - 10 empty cells", new Color(100, 200, 100));
        JButton mediumButton = createStyledButton("  MEDIUM - 20 empty cells", new Color(255, 200, 50));
        JButton hardButton = createStyledButton("  HARD  - 25 empty cells", new Color(255, 100, 100));
        JButton backButton = createStyledButton("  LOAD A SOLVED SUDOKU", new Color(150, 150, 150));

        easyButton.addActionListener(e -> loadGameByDifficulty('E'));
        mediumButton.addActionListener(e -> loadGameByDifficulty('M'));
        hardButton.addActionListener(e -> loadGameByDifficulty('H'));
        backButton.addActionListener(e -> loadSolvedSudoku());

        buttonPanel.add(easyButton);
        buttonPanel.add(mediumButton);
        buttonPanel.add(hardButton);
        buttonPanel.add(backButton);

        selectionPanel.add(titleLabel, BorderLayout.CENTER);
        selectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(selectionPanel);
        revalidate();
        repaint();
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.playStartingMusic();
        }
    }

    private void loadGameByDifficulty(char difficultyChar) {
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.stopMusic();
        }

        try {
            int[][] board = controller.getGame(difficultyChar);

            Difficulty difficulty;
            switch(difficultyChar) {
                case 'E': difficulty = Difficulty.EASY; break;
                case 'M': difficulty = Difficulty.MEDIUM; break;
                case 'H': difficulty = Difficulty.HARD; break;
                default: difficulty = null;
            }

            Game game = new Game(board, difficulty);
            loadGameUI(game, difficulty + " Game");
        } catch (NotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "No " + difficultyChar + " games found. Please load a solved Sudoku first.",
                    "No Games Found",
                    JOptionPane.ERROR_MESSAGE);
            showLoadSolvedDialog();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading game: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showLoadSolvedDialog() {
        getContentPane().removeAll();

        JPanel loadPanel = new JPanel(new BorderLayout());
        loadPanel.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel("LOAD SOLVED SUDOKU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JLabel instructionLabel = new JLabel(
                "\t\t\tNo games found in storage.\n" +
                        "\t\t\tPlease load a fully solved Sudoku CSV file to generate games\n" +
                        "\t\t\tThe file must be 9x9 with numbers 1-9.\n");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setForeground(Color.BLACK);

        JButton loadButton = createStyledButton("  LOAD CSV FILE", new Color(100, 150, 255));
        loadButton.setPreferredSize(new Dimension(300, 60));
        loadButton.addActionListener(e -> loadSolvedSudoku());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        centerPanel.add(instructionLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(30, 0, 0, 0);
        centerPanel.add(loadButton, gbc);

        loadPanel.add(titleLabel, BorderLayout.NORTH);
        loadPanel.add(centerPanel, BorderLayout.CENTER);
        add(loadPanel);
        revalidate();
        repaint();
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.playStartingMusic();
        }
    }

    private void loadSolvedSudoku() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Solved Sudoku CSV File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        int result = fileChooser.showOpenDialog(this);

        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                controller.driveGames(selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Games Generated Successfully!\n\n" +
                                "Three difficulty levels have been created:\n" +
                                "• Easy (10 empty cells)\n" +
                                "• Medium (20 empty cells)\n" +
                                "• Hard (25 empty cells)",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                showDifficultySelection();
            } catch (SolutionInvalidException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Solution\n\n" + e.getMessage() + "\n" +
                                "Please provide a fully solved, valid Sudoku board.",
                        "Invalid Solution",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error generating games: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGameUI(Game game, String title) {
        getContentPane().removeAll();
        sudokuGUI = new SudokuGUI(controller, gameController, game, this, musicPlayer, musicEnabled);
        add(sudokuGUI);
        setTitle("Sudoku Game - " + title);
        revalidate();
        repaint();
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.playInGameMusic();
        }
    }

    private void showWelcomeScreen() {
        boolean[] catalog = controller.getCatalog();
        boolean hasUnfinished = catalog[0];
        boolean allModesExist = catalog[1];

        if(hasUnfinished) {
            showContinueDialog();
        } else if(allModesExist) {
            showDifficultySelection();
        } else {
            showLoadSolvedDialog();
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(300, 50));

        return button;
    }

    private void handleWindowClosing() {
        if (sudokuGUI != null && sudokuGUI.hasUnsavedChanges()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Do you want to save your current progress before exiting?\n" +
                            "Choose 'No' to delete all progress and exit.",
                    "Save Progress",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if(option == JOptionPane.YES_OPTION) {
                disposeAndCleanup();
            } else if(option == JOptionPane.NO_OPTION) {
                gameController.deleteCurrentGameFiles();
                disposeAndCleanup();
            }
        } else {
            disposeAndCleanup();
        }
    }

    private void disposeAndCleanup() {
        if (musicPlayer != null) {
            musicPlayer.stopMusic();
        }
        dispose();
    }

    public void returnToMainMenu() {
        sudokuGUI = null;
        showDifficultySelection();
        setTitle("Sudoku Game");

        if (musicEnabled && musicPlayer != null) {
            musicPlayer.playStartingMusic();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainGUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Fatal Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}