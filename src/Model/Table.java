package Model;

import Database.CSVDatabaseManager;

public class Table {
    private Row[] rows;
    private Box[] boxes;
    private Column[] columns;
    private static Table table;
    
    private Table(String filePath) {
        rows = new Row[9];
        columns = new Column[9];
        boxes = new Box[9];
        for(int i = 0; i < 9; i++) {
            rows[i] = new Row();
            columns[i] = new Column();
        }
        CSVDatabaseManager.loadData(boxes, rows, columns, filePath);
    }
    
    public static Table getTable(String filePath) {
        if(table == null) {
            table = new Table(filePath);
        }
        return table;
    }
    
    public static void resetTable() {
        table = null;
    }

    public Row[] getRows() {
        return rows;
    }

    public Box[] getBoxes() {
        return boxes;
    }

    public Column[] getColumns() {
        return columns;
    }
}
