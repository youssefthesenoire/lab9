package Model;

public class LocationOnBoard {
    private int x;
    private int y;

    public LocationOnBoard(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LocationOnBoard that = (LocationOnBoard) obj;
        return x == that.x && y == that.y;
    }
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    @Override
    public String toString() {
        return x + "," + y;
    }
}