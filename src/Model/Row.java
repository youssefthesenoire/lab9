package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Row {
    private int[] rowElements;
    public Row()
    {
        this.rowElements = new int[9];
    }

    public int[] getRowElements() {
        return rowElements;
    }

    public void setRowElements(int[] rowElements) {
        this.rowElements = rowElements;
    }
    public HashMap<Integer,ArrayList<Integer>> getDuplicatedRow()
    {
        HashMap<Integer,ArrayList<Integer>> dup = new HashMap<>();
        for(int i=0;i<8;i++)
        {
            int number = rowElements[i];
            if(number == 0) continue;
            if(!dup.containsKey(number))
                dup.put(number,new ArrayList<Integer>());
            dup.get(number).add(i);
        }
        dup.entrySet().removeIf(e -> e.getValue().size()==1);
        return dup;
    }
}
