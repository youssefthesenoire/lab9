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
        ArrayList<LocationOnBox> l = v.getDuplicateLocations();


//Print Results
        if (v != null) {
            if (v.isValid()) {
                System.out.println("Valid");
            } else {
                System.out.println("Invalid");
                //Print Dup Rows

                for (int i = 0; i < r.length; i++) {
                    HashMap<Integer, ArrayList<Integer>> dup = r[i].getDuplicatedRow();
                    if (dup != null) {

                        for (Map.Entry<Integer, ArrayList<Integer>> entry : dup.entrySet()) {
                            int number = entry.getKey();
                            ArrayList<Integer> positions = entry.getValue();
                            out.append("Row " + (i + 1) + ", #" + number + ", [");

                            // Get positions one by one
                            for (int j = 0; j < positions.size(); j++) {
                                out.append(positions.get(j) + 1); // convert to 1-based column
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
                    if (dup != null) {

                        for (Map.Entry<Integer, ArrayList<Integer>> entry : dup.entrySet()) {
                            int number = entry.getKey();
                            ArrayList<Integer> positions = entry.getValue();
                            out.append("Column " + (i + 1) + ", #" + number + ", [");

                            // Get positions one by one
                            for (int j = 0; j < positions.size(); j++) {
                                out.append(positions.get(j) + 1); // convert to 1-based column
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
                    HashMap<Integer, ArrayList<LocationOnBox>> dup = b[i].getDuplicatedBox();
                    if (dup != null && !dup.isEmpty()) {

                        for (Map.Entry<Integer, ArrayList<LocationOnBox>> entry : dup.entrySet()) {
                            int number = entry.getKey();
                            ArrayList<LocationOnBox> positions = entry.getValue();

                            out.append("Box " + (i + 1) + ", #" + number + ", [");

                            for (int j = 0; j < positions.size(); j++) {
                                int x = positions.get(j).getY();
                                int y = positions.get(j).getX();

                                int localIndex = (y % 3) * 3 + (x % 3); // position inside the box 0–8
                                out.append(localIndex + 1); // convert to 1-based if you like

                                if (j != positions.size() - 1) {
                                    out.append(",");
                                }
                            }

                            out.append("]\n"); // close each number
                        }

                    }
                }


            }


                System.out.println(out);
            }
        }
    }

