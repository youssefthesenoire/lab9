package Service;

import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Mode_Zero {
    public static ValidationResult validate(Table table) {
        //Table table = Table.getTable();
        Set<LocationOnBox> duplicateLocations = new HashSet<>();
        boolean isValid = true;
        
        for(int i = 0; i < 9; i++) {
            HashMap<Integer, ArrayList<Integer>> rowDup = table.getRows()[i].getDuplicatedRow();
            for(int num : rowDup.keySet()) {
                ArrayList<Integer> columns = rowDup.get(num);
                for(int col : columns) {
                    duplicateLocations.add(new LocationOnBox(i, col));
                    isValid = false;
                }
            }
        }
        
        for(int i = 0; i < 9; i++) {
            HashMap<Integer, ArrayList<Integer>> colDup = table.getColumns()[i].getDuplicatedColumn();
            for(int num : colDup.keySet()) {
                ArrayList<Integer> rows = colDup.get(num);
                for(int row : rows) {
                    duplicateLocations.add(new LocationOnBox(row, i));
                    isValid = false;
                }
            }
        }
        
        for(Box box : table.getBoxes()) {
            HashMap<Integer, ArrayList<LocationOnBox>> boxDup = box.getDuplicatedBox();
            for(int num : boxDup.keySet()) {
                ArrayList<LocationOnBox> locations = boxDup.get(num);
                duplicateLocations.addAll(locations);
                isValid = false;
            }
        }
        
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }
}
