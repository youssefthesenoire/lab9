package UI;

import Model.Column;
import Model.Row;
import Model.Table;
import Model.ValidationResult;
import Service.Mode_Three;
import Service.Mode_TwentySeven;
import Service.Mode_Zero;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar MyApp.jar <csv-file> <mode>");
            return;
        }

        String csvPath = args[0];  // first argument
        String mode = args[1];     // second argument

        System.out.println("CSV Path: " + csvPath);
        System.out.println("Mode: " + mode);
        Table t=Table.getTable(args[1]);
        Row[]r=t.getRows();
        Column[]c=t.getColumns();
        for(Row row:r){
            
        }


        ValidationResult v = null;

        switch (mode) {
            case "Method0":
                v = Mode_Zero.validate(t);
                break;
            case "Method3":
                v = Mode_Three.validate(t);
                break;
            case "Method27":
                v = Mode_TwentySeven.validate(t);
                break;
            default:
                System.out.println("Unknown mode: " + mode);
                return;
        }

        if (!v.isValid()) {
            System.out.println("Invalid data found!");
        } else {
            System.out.println("Data is valid.");
        }
    }
}
