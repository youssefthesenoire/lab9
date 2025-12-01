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
    public String toString() {
        return "(" + x + "," + y + ")";
    }

}
