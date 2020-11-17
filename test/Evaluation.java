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
        String init = "";
        String term = "";
        String temp = "";

        int initCounter = 0;
        int termCounter = 0;
        int counter = 0;
        
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            // calc average throughput
            if (data.indexOf("Throughput init time:") > -1) {
                temp = data.substring(data.indexOf(":")+2);
                initCounter++;
                if (initCounter == 1) {
                    init = temp;
                } else {
                    if (temp.compareTo(init) < 0) {
                        init = temp;
                    }
                }
            }
            if (data.indexOf("Throughput term time:") > -1) {
                temp = data.substring(data.indexOf(":")+2);
                termCounter++;
                if (termCounter == 1) {
                    term = temp;
                } else {
                    if (temp.compareTo(term) < 0) {
                        term = temp;
                    }
                }
            }
            if (data.indexOf("PID #") > -1) {
                counter ++;
            }
        }

        // calculate throughput
        double csnum = counter;
        //System.out.println("# of Crtical Sections: " + csnum);
        
        LocalTime initSecond = LocalTime.parse(init);
        //System.out.println("init Second: " + initSecond);
        LocalTime termSecond = LocalTime.parse(term);
        //System.out.println("term Second: " + termSecond);
        double seconds = Duration.between(initSecond, termSecond).getSeconds();
        //System.out.println("time (seconds): " + seconds);
                
        // return Throughput
        return csnum/seconds;
    }

    public static void main(String[] args) throws Exception {
        try {
            // Read log file
            String logfile = "";
            logfile = args[0];
            //System.out.println(logfile);
            
            File myObj = new File(logfile);
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
