package Service;

import Model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Mode_Three {
    public static ValidationResult validate() {
        Table table = Table.getTable();
        Set<LocationOnBox> duplicateLocations = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        executor.execute(new RowValidator(table.getRows(), duplicateLocations));
        executor.execute(new ColumnValidator(table.getColumns(), duplicateLocations));
        executor.execute(new BoxValidator(table.getBoxes(), duplicateLocations));
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = duplicateLocations.isEmpty();
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }
    
    private static class RowValidator implements Runnable {
        private final Row[] rows;
        private final Set<LocationOnBox> duplicates;
        
        public RowValidator(Row[] rows, Set<LocationOnBox> duplicates) {
            this.rows = rows;
            this.duplicates = duplicates;
        }
        
        @Override
        public void run() {
            for(int i = 0; i < 9; i++) {
                var rowDup = rows[i].getDuplicatedRow();
                for(var entry : rowDup.entrySet()) {
                    for(int col : entry.getValue()) {
                        duplicates.add(new LocationOnBox(i, col));
                    }
                }
            }
        }
    }
    
    private static class ColumnValidator implements Runnable {
        private final Column[] columns;
        private final Set<LocationOnBox> duplicates;
        
        public ColumnValidator(Column[] columns, Set<LocationOnBox> duplicates) {
            this.columns = columns;
            this.duplicates = duplicates;
        }
        
        @Override
        public void run() {
            for(int i = 0; i < 9; i++) {
                var colDup = columns[i].getDuplicatedColumn();
                for(var entry : colDup.entrySet()) {
                    for(int row : entry.getValue()) {
                        duplicates.add(new LocationOnBox(row, i));
                    }
                }
            }
        }
    }
    
    private static class BoxValidator implements Runnable {
        private final Box[] boxes;
        private final Set<LocationOnBox> duplicates;
        
        public BoxValidator(Box[] boxes, Set<LocationOnBox> duplicates) {
            this.boxes = boxes;
            this.duplicates = duplicates;
        }
        
        @Override
        public void run() {
            for(Box box : boxes) {
                var boxDup = box.getDuplicatedBox();
                for(var entry : boxDup.entrySet()) {
                    duplicates.addAll(entry.getValue());
                }
            }
        }
    }
}
