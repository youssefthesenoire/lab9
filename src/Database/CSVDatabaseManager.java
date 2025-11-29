package Database;

import java.io.FileReader;
import Model.Box;
import Model.Column;
import Model.Row;
import com.opencsv.CSVReader;

public class CSVDatabaseManager {
    public static void loadData(Box[] boxes, Row[] rows, Column[] columns, String filePath) {
        try {
            FileReader filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;
            int counter = 0;
            int[][] elements = new int[9][9];
            
            while ((nextRecord = csvReader.readNext()) != null && counter < 9) {
                for(int j = 0; j < 9; j++) {
                    elements[counter][j] = Integer.parseInt(nextRecord[j].trim());
                }
                counter++;
            }
            csvReader.close();
            
            for(int i = 0; i < 9; i++) {
                int[] r = new int[9];
                int[] c = new int[9];
                for(int j = 0; j < 9; j++) {
                    r[j] = elements[i][j];
                    c[j] = elements[j][i];
                }
                rows[i].setRowElements(r);
                columns[i].setcolumnElements(c);
            }
            
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    int[] b = new int[9];
                    int count = 0;
                    for(int k = i * 3; k < i * 3 + 3; k++) {
                        for(int t = j * 3; t < j * 3 + 3; t++) {
                            b[count++] = elements[k][t];
                        }
                    }
                    int boxIndex = i * 3 + j;
                    boxes[boxIndex] = new Box(i * 3, j * 3);
                    boxes[boxIndex].setBoxElements(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
