package Model;

import Database.CSVDatabaseManager;

import java.util.ArrayList;

public class Table {
    private Row[] rows ;
    private Box[] boxes;
    private Column[] columns;
    private static Table table;
    private Table()
    {
        rows = new Row[9];
        columns = new Column[9];
        boxes = new Box[9];
        CSVDatabaseManager.loadData(boxes,rows,columns);
    }
    public static Table getTable()
    {
        if(table == null)
        {
            table = new Table();
        }
        return table;
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
