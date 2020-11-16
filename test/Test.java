import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Test {
    // function to check if there's any process' critical sections overlap
    static boolean checkOverlap(ArrayList<ArrayList<String>> dataMatrix) {
        boolean check = false;
        for(int row = 0; row < dataMatrix.size(); row++) {
            ArrayList<String> currentRowData = dataMatrix.get(row);
            
            // The init timestamp is greater than or equal to the term timestamp of the same process’s critical section.
            if (currentRowData.get(1).compareTo(currentRowData.get(2)) > 0) {
                check = true;
                System.out.println("Overlap occured!");
            }
            
            // The init timestamp of this current process’s critical section is less than the term timestamp of the previous process’s critical section.
            if (row != 0) {
                ArrayList<String> previousRowData = dataMatrix.get(row-1);
                if (currentRowData.get(1).compareTo(previousRowData.get(1)) < 0) {
                    check = true;
                    System.out.println("Overlap occured!");
                }
            }
        }
        return check;
    }
    
    public static void main(String[] args) throws Exception {
        try {
            // Read in the timestamps from the log file as well as the process ID for those timestamps
            // create a data structure  where each element of the data structure holds an enry form the log file 
            File myObj = new File("./log.txt");
            Scanner myReader = new Scanner(myObj);
            
            ArrayList<ArrayList<String>> dataMatrix = new ArrayList<ArrayList<String>>();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.indexOf("Process ID:") > -1) {
                    String[] tokens = data.split("; ");
                    // Create a temp ArrayList object of data
                    ArrayList<String> d = new ArrayList<String>();
                    for (String t : tokens) {
                        d.add(t.substring(t.indexOf(":")+2));
                    }   
                    dataMatrix.add(d);
                }
            }
            
            // sort the entries from the log file by the init timestamp in ascending order
            Collections.sort(dataMatrix, new Comparator<ArrayList<String>>() {    
                @Override
                public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                    return o1.get(1).compareTo(o2.get(1));
                }               
            });

            // Check if not implemented correctly because critical sections overlapped
            if (!checkOverlap(dataMatrix)) {
                System.out.println("Test Result: implemented correctly.");
            } else {
                System.out.println("Test Result: implemented NOT correctly.");
            }
            
            // close file after finish read
            myReader.close();
          } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
          }
    }
}