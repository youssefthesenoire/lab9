package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Box {
    private int[] boxElements;
    int sRow,sCol;
    public Box(int sRow,int sCol) {
        this.boxElements = new int[9];
        this.sRow = sRow;
        this.sCol = sCol;
    }

    public int[] getBoxElements() {
        return boxElements;
    }

    public void setBoxElements(int[] boxElements) {
        this.boxElements = boxElements;
    }
    public HashMap<Integer,ArrayList<LocationOnBoard>> getDuplicatedBox()
    {
        HashMap<Integer,ArrayList<LocationOnBoard>> dup = new HashMap<>();
        for(int i=0;i<9;i++)
        {
            int number = boxElements[i];
            if(number == 0) continue;
            int r = sRow + i/3;
            int c = sCol + i%3;
            if(!dup.containsKey(number))
                dup.put(number,new ArrayList<>());
            dup.get(number).add(new LocationOnBoard(r,c));
        }
        dup.entrySet().removeIf(e -> e.getValue().size()==1);
        return dup;
    }
}