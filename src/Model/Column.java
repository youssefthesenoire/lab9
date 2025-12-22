package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Column {
    private int[] columnElements;

    public Column() {
        this.columnElements = new int[9];
    }
    public int[] getcolumnElements() {
        return columnElements;
    }
    public void setcolumnElements(int[] columnElements) {
        this.columnElements = columnElements;
    }
    public HashMap<Integer, ArrayList<Integer>> getDuplicatedColumn() {
        HashMap<Integer, ArrayList<Integer>> dup = new HashMap<>();
        for(int i = 0; i < 9; i++) {
            int number = columnElements[i];
            if(number == 0) continue;
            if(!dup.containsKey(number))
                dup.put(number, new ArrayList<Integer>());
            dup.get(number).add(i);
        }
        dup.entrySet().removeIf(e -> e.getValue().size() == 1);
        return dup;
    }
}