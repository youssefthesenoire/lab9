package UI;

import Model.LocationOnBoard;
import Model.Table;
import Model.ValidationResult;
import Service.Mode_Three;
import Service.Mode_TwentySeven;
import Service.Mode_Zero;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class GUI_Used_Later extends JFrame {
    private JTable sudokuTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> methodComboBox;
    private JButton validateButton;
    private JButton LoadTableButton;
    Table t;
    Mode_Zero mode = new Mode_Zero();
    Mode_TwentySeven mode27 = new Mode_TwentySeven();
    Mode_Three mode3 = new Mode_Three();
    ValidationResult v;

    public GUI_Used_Later() {
        initializeGUI();
    }

    private void initializeGUI() {
        // Set up the main frame
        setTitle("Sudoku Validator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Create the game board
        JPanel boardPanel = createBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Create the control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // Center the window
    }

    private JPanel createBoardPanel() {
        JPanel mainBoardPanel = new JPanel();
        mainBoardPanel.setLayout(new BorderLayout());
        mainBoardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with 9 rows and 9 columns
        tableModel = new DefaultTableModel(9, 9) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells uneditable
                return false;
            }
        };

        // Create the table
        sudokuTable = new JTable(tableModel);

        // Configure table appearance
        sudokuTable.setRowHeight(50);
        sudokuTable.setFont(new Font("Arial", Font.BOLD, 20));
        sudokuTable.setGridColor(Color.GRAY);
        sudokuTable.setShowGrid(true);
        sudokuTable.setCellSelectionEnabled(false);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < sudokuTable.getColumnCount(); i++) {
            sudokuTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Remove table header
        sudokuTable.setTableHeader(null);

        // Set preferred column widths
        for (int i = 0; i < sudokuTable.getColumnCount(); i++) {
            sudokuTable.getColumnModel().getColumn(i).setPreferredWidth(50);
        }

        // Create custom borders for 3x3 boxes
        JScrollPane scrollPane = new JScrollPane(sudokuTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        mainBoardPanel.add(scrollPane, BorderLayout.CENTER);
        mainBoardPanel.setPreferredSize(new Dimension(500, 500));

        return mainBoardPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        String[] methods = {"Method 0", "Method 3", "Method 27"};
        methodComboBox = new JComboBox<>(methods);
        methodComboBox.setSelectedItem("Method 0");

        // Create buttons
        validateButton = new JButton("Validate");
        LoadTableButton = new JButton("Load Table");
        LoadTableButton.addActionListener(e -> LoadTable());
        validateButton.addActionListener(e -> V());

        // Add components to control panel
        controlPanel.add(new JLabel("Method:"));
        controlPanel.add(methodComboBox);
        controlPanel.add(Box.createHorizontalStrut(20)); // Add some space
        controlPanel.add(validateButton);
        controlPanel.add(Box.createHorizontalStrut(10)); // Add some space
        controlPanel.add(LoadTableButton);

        return controlPanel;
    }

    public void LoadTable() {
        String selectedMethod = methodComboBox.getSelectedItem().toString();
        Frame frame = new Frame(); // parent frame
        FileDialog dialog = new FileDialog(frame, "Select CSV file", FileDialog.LOAD);
        dialog.setFile("*.csv");  // filter CSV files
        dialog.setVisible(true);

        String directory = dialog.getDirectory();
        String filename = dialog.getFile();

        if (filename != null) {
            File selectedFile = new File(directory, filename);
            t = Table.getTable(selectedFile.getAbsolutePath());

            System.out.println("Selected: " + selectedFile.getAbsolutePath());
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int value = t.getRows()[row].getRowElements()[col];
                    tableModel.setValueAt(value, row, col);
                }
            }
        } else {
            System.out.println("No file selected");
        }
    }

    public void V() {
        if (methodComboBox.getSelectedItem().toString().equals("Method 0")) {
            v = Mode_Zero.validate(t);
        } else if (methodComboBox.getSelectedItem().toString().equals("Method 3")) {
            v = Mode_Three.validate(t);
        } else if (methodComboBox.getSelectedItem().toString().equals("Method 27")) {
            v = Mode_TwentySeven.validate(t);
        }

        if (!v.isValid()) {
            ArrayList<LocationOnBoard> lb =  v.getDuplicateLocations();
            for (LocationOnBoard l : lb) {
                int r = l.getX();
                int c = l.getY();
                int b = (r / 3) * 3 + (c / 3);
                highlightRow(r);
                highlightColumn(c);
                highlightBox(b);
            }
        }
    }

    void highlightRow(int r) {
        // Create a custom renderer for the highlighted row
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == r) {
                    // Set border for the entire row
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(3, 0, 3, 0, Color.RED),
                            BorderFactory.createLineBorder(Color.GRAY, 1)
                    ));
                } else {
                    // Regular border
                    setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }

                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Arial", Font.BOLD, 20));
                return c;
            }
        };

        // Apply the renderer to all columns for this row
        for (int col = 0; col < sudokuTable.getColumnCount(); col++) {
            sudokuTable.getColumnModel().getColumn(col).setCellRenderer(rowRenderer);
        }
    }

    void highlightColumn(int c) {
        // Create a custom renderer for the highlighted column
        DefaultTableCellRenderer colRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == c) {
                    // Set border for the entire column
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 3, 0, 3, Color.BLUE),
                            BorderFactory.createLineBorder(Color.GRAY, 1)
                    ));
                } else {
                    // Regular border
                    setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }

                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Arial", Font.BOLD, 20));
                return comp;
            }
        };

        // Apply the renderer to the specific column
        sudokuTable.getColumnModel().getColumn(c).setCellRenderer(colRenderer);
    }

    void highlightBox(int boxIndex) {
        int startRow = (boxIndex / 3) * 3;
        int startCol = (boxIndex % 3) * 3;

        // Create a custom renderer for the highlighted box
        DefaultTableCellRenderer boxRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                boolean isInBox = (row >= startRow && row < startRow + 3 &&
                        column >= startCol && column < startCol + 3);

                if (isInBox) {
                    // Determine border thickness based on position within box
                    int top = (row == startRow) ? 3 : 1;
                    int left = (column == startCol) ? 3 : 1;
                    int bottom = (row == startRow + 2) ? 3 : 1;
                    int right = (column == startCol + 2) ? 3 : 1;

                    setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.GREEN));
                } else {
                    // Regular border
                    setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }

                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Arial", Font.BOLD, 20));
                return comp;
            }
        };

        // Apply the renderer to all cells
        for (int col = 0; col < sudokuTable.getColumnCount(); col++) {
            sudokuTable.getColumnModel().getColumn(col).setCellRenderer(boxRenderer);
        }
    }
}