package UI;

public class UserAction {
    private int row;
    private int col;
    private int value;
    private int previousValue;

    public UserAction(int row, int col, int value, int previousValue) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.previousValue = previousValue;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ", " + value + ", " + previousValue + ")";
    }
}