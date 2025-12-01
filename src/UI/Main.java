package UI;

import Model.*;
import Service.Mode_Three;
import Service.Mode_TwentySeven;
import Service.Mode_Zero;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar MyApp.jar <csv-file> <mode>");
            return;
        }
        String csvPath = args[0];
        String mode = args[1];
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
        String o = "null";

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
        StringBuilder out = new StringBuilder();
        ArrayList<LocationOnBoard> l = v.getDuplicateLocations();


//Print Results
        if (v != null) {
            if (v.isValid()) {
                System.out.println("Valid");
            } else {
                System.out.println("Invalid");
                //Print Dup Rows

                for (int i = 0; i < r.length; i++) {
                    HashMap<Integer, ArrayList<Integer>> dup = r[i].getDuplicatedRow();
                    if (dup != null && !dup.isEmpty()) {
                        for (Map.Entry<Integer, ArrayList<Integer>> entry : dup.entrySet()) {
                            int number = entry.getKey();
                            ArrayList<Integer> positions = entry.getValue();
                            out.append("Row " + (i + 1) + ", #" + number + ", [");
                            for (int j = 0; j < positions.size(); j++) {
                                out.append(positions.get(j) + 1);
                                if (j != positions.size() - 1) {
                                    out.append(",");
                                }

                            }

                        }
                        out.append("]\n");

                    }
                }

                out.append("------------------------------------------------------------\n");
                //Print Dup Columns
                for (int i = 0; i < c.length; i++) {
                    HashMap<Integer, ArrayList<Integer>> dup = c[i].getDuplicatedColumn();
                    if (dup != null && !dup.isEmpty()) {

                            for (Map.Entry<Integer, ArrayList<Integer>> entry : dup.entrySet()) {
                                int number = entry.getKey();
                                ArrayList<Integer> positions = entry.getValue();
                                out.append("Column " + (i + 1) + ", #" + number + ", [");
                                for (int j = 0; j < positions.size(); j++) {
                                    out.append(positions.get(j) + 1);
                                    if (j != positions.size() - 1) {
                                        out.append(",");
                                    }
                                }

                        }
                        out.append("]\n");
                    }
                }
                    out.append("------------------------------------------------------------\n");
                    //Print Dup Boxes
                    for (int i = 0; i < b.length; i++) {
                        HashMap<Integer, ArrayList<LocationOnBoard>> dup = b[i].getDuplicatedBox();
                        if (dup != null && !dup.isEmpty()) {

                            for (Map.Entry<Integer, ArrayList<LocationOnBoard>> entry : dup.entrySet()) {
                                int number = entry.getKey();
                                ArrayList<LocationOnBoard> positions = entry.getValue();

                                out.append("Box " + (i + 1) + ", #" + number + ", [");

                                for (int j = 0; j < positions.size(); j++) {
                                    int x = positions.get(j).getY();
                                    int y = positions.get(j).getX();

                                    int localIndex = (y % 3) * 3 + (x % 3);
                                    out.append(localIndex + 1);

                                    if (j != positions.size() - 1) {
                                        out.append(",");
                                    }
                                }
                                out.append("]\n");
                            }
                        }
                    }
                }
                System.out.println(out);
            }
        }
    }
