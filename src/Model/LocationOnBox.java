package Model;

public class LocationOnBox {
    private int x;
    private int y;
    
    public LocationOnBox(int x, int y) {
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LocationOnBox that = (LocationOnBox) obj;
        return x == that.x && y == that.y;
    }
    
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
