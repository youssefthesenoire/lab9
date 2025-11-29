package UI;

import Model.*;
import Service.Mode_Three;
import Service.Mode_TwentySeven;
import Service.Mode_Zero;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar MyApp.jar <csv-file> <mode>");
            return;
        }

        String csvPath = args[0];  // first argument
        String mode = args[1];     // second argument
//        String path = "C:\\Users\\ahmed\\Desktop\\New Text Document (2).csv";
        System.out.println("CSV Path: " + csvPath);
        System.out.println("Mode: " + mode);
        Table t = Table.getTable(csvPath);
        Row[] r = t.getRows();
        Column[] c = t.getColumns();
        Box[] b = t.getBoxes();
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < c.length; j++) {
                int value = r[i].getRowElements()[j];
                if (j != 0 && j % 3 == 0) {

                    s.append("*");
                    s.append("     " + value + "     ");

                } else {

                    s.append(value + "     ");
                }

            }
            if (i != 0 && (i + 1) % 3 == 0 && (i + 1) != 9) {

                s.append("\n");
                s.append("**************************************************************\n");

            } else {
                s.append("\n");
                s.append("------------------*-----------------------*------------------\n");
            }
        }


        System.out.println(s);


        ValidationResult v = null;
        String o="null";

        switch (mode) {
            case "Method0":
                v = Mode_Zero.validate(t);
                o = "#1";
                break;
            case "Method3":
                v = Mode_Three.validate(t);
                o = "#2";
                break;
            case "Method27":
                v = Mode_TwentySeven.validate(t);
                o = "#3";
                break;
            default:
                System.out.println("Unknown mode: " + mode);


                return;
        }
        StringBuilder out = new StringBuilder();

        if (v != null) {
            if (v.isValid()) {
                System.out.println("Valid");
            } else {
                System.out.println("Invalid");
                for (int i = 0; i < r.length; i++) {
                    out.append("Row" + (i + 1) + "," + o + ",[");
                    int[] rowElements = r[i].getRowElements();
                    for (int j = 0; j < rowElements.length; j++) {
                        out.append(rowElements[j]);
                        if (j < rowElements.length - 1) {
                            out.append(",");
                        }
                    }
                    out.append("]\n");
                }
                out.append("------------------------------------------------------------\n");
                for (int i = 0; i < c.length; i++) {
                    out.append("Column" + (i + 1) + "," + o + ",[");
                    int[] columnElements = c[i].getcolumnElements();
                    for (int j = 0; j < columnElements.length; j++) {
                        out.append(columnElements[j]);
                        if (j < columnElements.length - 1) {
                            out.append(",");
                        }
                    }
                    out.append("]\n"); // newline after each column
                }
                out.append("------------------------------------------------------------\n");
                for (int i = 0; i < b.length; i++) {
                    out.append("Box" + (i + 1) + "," + o + ",[");
                    int[] boxElements = b[i].getBoxElements();
                    for (int j = 0; j < b.length; j++) {
                        out.append(boxElements[j]);
                        if (j < boxElements.length - 1) {
                            out.append(",");
                        }
                    }
                    out.append("]\n");

                }

            }



            System.out.println(out);
        }
    }
}
