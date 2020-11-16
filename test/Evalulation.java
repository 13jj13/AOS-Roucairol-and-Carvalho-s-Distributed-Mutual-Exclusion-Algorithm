import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalTime; // Import the LocalTime class
import java.time.Duration;  // Import the Duration class


class Evaluation {
    /*
        Method: getResponseTime
        Description: Function to calculate and return response time. Response time is the time it takes to retrieve all keys and the time it takes to execute the critical section.
        Parameters: Scanner object to readfile
        Returns: double type response time in ms
    */
    static double getResponseTime(Scanner myReader) {
        double totalResponseTime = 0.0;
        double responseCounter = 0.0;
                
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            if (data.indexOf("response time") > -1) {
                //System.out.println(data.substring(data.indexOf(":")+2));
                totalResponseTime += Integer.parseInt(data.substring(data.indexOf(":")+2));
                responseCounter++;
            }
        }
        
        // calculate average response time
        double avgResponseTime = totalResponseTime/responseCounter;
        //System.out.println("total response time: " + totalResponseTime);
        //System.out.println("number of responses: " + responseCounter);
        
        // Response Time (ms)
        //System.out.println("Response Time (ms): " + avgResponseTime);
        return avgResponseTime;
    }
    /*
        Method: getMessageComplexity
        Description: Function calculate and return getMessageComplexity - total number of messages sent per requests
        Parameters: Scanner object to readfile
        Returns: int message complexity (number of messages sent / number of requests)
    */
    static double getMessageComplexity(Scanner myReader) {
        int totalMessages = 0;
        int numRequests = 0;
        
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            // calculate total messages sent by all machines
            if (data.indexOf("message count") > -1) {
                totalMessages += Integer.parseInt(data.substring(data.indexOf(":")+2));
                numRequests++;
            }
        }
        // Number of messages sent
        //System.out.println("Number of messages sent: " + totalMessages);
        return ((double) totalMessages) / ((double) numRequests);
    }
    /*
        Method: getThroughput
        Description: Function calculate and return throughput (# of critical sections / seconds)
        Parameters: Scanner object to readfile
        Returns: int throughput (# of critical sections / seconds)

    */
    static double getThroughput(Scanner myReader) {
        ArrayList<ArrayList<String>> dataMatrix = new ArrayList<ArrayList<String>>();

        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            // calc average throughput
            if (data.indexOf("Process ID:") > -1) {
                String[] tokens = data.split("; ");
                // Create a temp ArrayList object of data
                ArrayList<String> dataRow = new ArrayList<String>();
                for (String t : tokens) {
                    dataRow.add(t.substring(t.indexOf(":")+2));
                }
                dataMatrix.add(dataRow);
            }
        }

        // sort the entries from the log file by the init timestamp in ascending order
        Collections.sort(dataMatrix, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                return o1.get(1).compareTo(o2.get(1));
            }
        });

        // calculate throughput
        double csnum = dataMatrix.size();
        //System.out.println("# of Crtical Sections: " + csnum);
        
        LocalTime initSecond = LocalTime.parse(dataMatrix.get(0).get(1));
        //System.out.println("init Second: " + initSecond);
        LocalTime termSecond = LocalTime.parse(dataMatrix.get(dataMatrix.size()-1).get(2));
        //System.out.println("term Second: " + termSecond);
        double seconds = Duration.between(initSecond, termSecond).getSeconds();
        //System.out.println("time (seconds): " + seconds);
                
        // return Throughput
        return csnum/seconds;
    }

    public static void main(String[] args) throws Exception {
        try {
            // Read log file
            File myObj = new File("./log.txt");
            Scanner myReader;
        
            // get number of messages sent per number of requests
            myReader = new Scanner(myObj);
            double msgComplexity = getMessageComplexity(myReader);
            System.out.println("Message Complexity: " + msgComplexity);

            // get response time (ms)
            myReader = new Scanner(myObj);
            double rTime = getResponseTime(myReader);
            System.out.println("Response Time (ms): " + rTime);
            
            // get throughput (number of critical section per second)
            myReader = new Scanner(myObj);
            double throughput = getThroughput(myReader);
            System.out.println("System Throughput: " + throughput);

            // close file after finish read
            myReader.close();
          } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
          }

    }
}
