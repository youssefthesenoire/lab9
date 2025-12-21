package Service;

import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SequentialVerifier {

    public static VerificationResult verify(int[][] board) {
        List<LocationOnBoard> duplicates = new ArrayList<>();
        boolean hasEmptyCells = false;
        
        // Check rows
        for(int row = 0; row < 9; row++) {
            HashMap<Integer, List<Integer>> rowMap = new HashMap<>();
            for(int col = 0; col < 9; col++) {
                int value = board[row][col];
                if(value == 0) {
                    hasEmptyCells = true;
                    continue;
                }
                if(!rowMap.containsKey(value)) {
                    rowMap.put(value, new ArrayList<>());
                }
                rowMap.get(value).add(col);
            }
            // Add duplicates from this row
            for(var entry : rowMap.entrySet()) {
                if(entry.getValue().size() > 1) {
                    for(int col : entry.getValue()) {
                        duplicates.add(new LocationOnBoard(row, col));
                    }
                }
            }
        }
        
        // Check columns
        for(int col = 0; col < 9; col++) {
            HashMap<Integer, List<Integer>> colMap = new HashMap<>();
            for(int row = 0; row < 9; row++) {
                int value = board[row][col];
                if(value == 0) continue;
                if(!colMap.containsKey(value)) {
                    colMap.put(value, new ArrayList<>());
                }
                colMap.get(value).add(row);
            }
            // Add duplicates from this column
            for(var entry : colMap.entrySet()) {
                if(entry.getValue().size() > 1) {
                    for(int row : entry.getValue()) {
                        duplicates.add(new LocationOnBoard(row, col));
                    }
                }
            }
        }
        
        // Check boxes (3x3)
        for(int boxRow = 0; boxRow < 3; boxRow++) {
            for(int boxCol = 0; boxCol < 3; boxCol++) {
                HashMap<Integer, List<LocationOnBoard>> boxMap = new HashMap<>();
                for(int i = 0; i < 3; i++) {
                    for(int j = 0; j < 3; j++) {
                        int row = boxRow * 3 + i;
                        int col = boxCol * 3 + j;
                        int value = board[row][col];
                        if(value == 0) continue;
                        if(!boxMap.containsKey(value)) {
                            boxMap.put(value, new ArrayList<>());
                        }
                        boxMap.get(value).add(new LocationOnBoard(row, col));
                    }
                }
                // Add duplicates from this box
                for(var entry : boxMap.entrySet()) {
                    if(entry.getValue().size() > 1) {
                        duplicates.addAll(entry.getValue());
                    }
                }
            }
        }
        
        // Determine game state
        if(!duplicates.isEmpty()) {
            return new VerificationResult(GameState.INVALID, duplicates);
        } else if(hasEmptyCells) {
            return new VerificationResult(GameState.INCOMPLETE, new ArrayList<>());
        } else {
            return new VerificationResult(GameState.VALID, new ArrayList<>());
        }
    }
}
