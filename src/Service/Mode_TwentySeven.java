package Service;

import Model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Mode_TwentySeven {
    public static ValidationResult validate() {
        Table table = Table.getTable();
        Set<LocationOnBox> duplicateLocations = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService executor = Executors.newFixedThreadPool(27);
        
        for(int i = 0; i < 9; i++) {
            final int index = i;
            executor.execute(() -> validateRow(table.getRows()[index], index, duplicateLocations));
            executor.execute(() -> validateColumn(table.getColumns()[index], index, duplicateLocations));
            executor.execute(() -> validateBox(table.getBoxes()[index], duplicateLocations));
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = duplicateLocations.isEmpty();
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }
    
    private static void validateRow(Row row, int rowIndex, Set<LocationOnBox> duplicates) {
        var rowDup = row.getDuplicatedRow();
        for(var entry : rowDup.entrySet()) {
            for(int col : entry.getValue()) {
                duplicates.add(new LocationOnBox(rowIndex, col));
            }
        }
    }
    
    private static void validateColumn(Column column, int colIndex, Set<LocationOnBox> duplicates) {
        var colDup = column.getDuplicatedColumn();
        for(var entry : colDup.entrySet()) {
            for(int row : entry.getValue()) {
                duplicates.add(new LocationOnBox(row, colIndex));
            }
        }
    }
    
    private static void validateBox(Box box, Set<LocationOnBox> duplicates) {
        var boxDup = box.getDuplicatedBox();
        for(var entry : boxDup.entrySet()) {
            duplicates.addAll(entry.getValue());
        }
    }
}
