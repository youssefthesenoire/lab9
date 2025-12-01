package Service;

import Model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Mode_Three implements Validation {
    public static ValidationResult validate(Table table) {
        Set<LocationOnBoard> duplicateLocations = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.execute(() -> validateAllRows(table.getRows(), duplicateLocations));
        executor.execute(() -> validateAllColumns(table.getColumns(), duplicateLocations));
        executor.execute(() -> validateAllBoxes(table.getBoxes(), duplicateLocations));
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = duplicateLocations.isEmpty();
        return new ValidationResult(isValid, new ArrayList<>(duplicateLocations));
    }

    private static void validateAllRows(Row[] rows, Set<LocationOnBoard> duplicates) {
        for (int i = 0; i < 9; i++) {
            var rowDup = rows[i].getDuplicatedRow();
            for (var entry : rowDup.entrySet()) {
                for (int col : entry.getValue()) {
                    duplicates.add(new LocationOnBoard(i, col));
                }
            }
        }
    }

    private static void validateAllColumns(Column[] columns, Set<LocationOnBoard> duplicates) {
        for (int i = 0; i < 9; i++) {
            var colDup = columns[i].getDuplicatedColumn();
            for (var entry : colDup.entrySet()) {
                for (int row : entry.getValue()) {
                    duplicates.add(new LocationOnBoard(row, i));
                }
            }
        }
    }

    private static void validateAllBoxes(Box[] boxes, Set<LocationOnBoard> duplicates) {
        for (Box box : boxes) {
            var boxDup = box.getDuplicatedBox();
            for (var entry : boxDup.entrySet()) {
                duplicates.addAll(entry.getValue());
            }
        }
    }
}

