package Service;

import Model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Mode_Zero {
    public static boolean validate(ArrayList<LocationOnBox> locations)
    {
        Table t = Table.getTable();
        boolean  valid = true;
        for (int i= 0; i < 9; i++) {
            HashMap<Integer, ArrayList<Integer>> dup = t.getRows()[i].getDuplicatedRow();
            for (int num : dup.keySet()) {
                ArrayList<Integer> columns = dup.get(num);
                for (int c : columns) {
                    locations.add(new LocationOnBox(i, c));
                    valid = false;
                }
            }
        }
        for (int i= 0; i < 9; i++) {
            HashMap<Integer, ArrayList<Integer>> dup = t.getColumns()[i].getDuplicatedColumn();
            for (int num : dup.keySet()) {
                ArrayList<Integer> rows = dup.get(num);
                for (int r : rows) {
                    locations.add(new LocationOnBox(i, r));
                    valid = false;
                }
            }
        }
        for (Box box : t.getBoxes()) {
            HashMap<Integer, ArrayList<LocationOnBox>> dup = box.getDuplicatedBox();
            for (int num : dup.keySet()) {
                ArrayList<LocationOnBox> locs = dup.get(num);
                for (LocationOnBox loc : locs) {
                    locations.add(loc);
                    valid = false;
                }
            }
        }
        return valid;
    }
}
