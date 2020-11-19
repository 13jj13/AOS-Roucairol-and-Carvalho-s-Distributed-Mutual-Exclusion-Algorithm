import java.io.*;
import java.util.*;
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
        
        // return response Time (ms)
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
            }
            if (data.indexOf("response time") > -1) {
                numRequests++;
            }
        }
        
        // Number of messages sent
        //System.out.println("Number of messages sent: " + totalMessages);
        //System.out.println("Number of requests: " + numRequests);
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
                //System.out.println(temp);
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
                //System.out.println(temp);
                termCounter++;
                //System.out.println("term counter: " + termCounter);
                if (termCounter == 1) {
                    term = temp;
                } else {
                    //System.out.println(term.compareTo(temp));
                    if (term.compareTo(temp) < 0) {
                        term = temp;
                    }
                }
            }
            if (data.indexOf("response time") > -1) {
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

    /*
        Method: combineLogs
        Description: Function to combine all log files from all machines into one log file
        Parameters: String logfolder directory of where the logs are located, and String logfile output filename
    */
    static void combineLogs(String logfolder, String logfile) {
        String[] fileNames;
        File dir = new File(logfolder);

        // This filter will only include files starting with log
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("eval");
            }
        };
        
        fileNames = dir.list(filter);
        try {
            PrintWriter pw = new PrintWriter(logfile);
            for (String fileName : fileNames) {
                // Print the names of files and directories
                //System.out.println(fileName);
                //System.out.println("Reading from " + fileName);

                // create instance of file from Name of
                // the file stored in string Array
                File f = new File(dir, fileName);
      
                // create object of BufferedReader
                BufferedReader br = new BufferedReader(new FileReader(f));
                
                // Read from current file
                String line = br.readLine();
                while (line != null) {
                    // write to the output file
                    pw.println(line);
                    line = br.readLine();
                }
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        //System.out.println("log file populated.");
    }

    public static void main(String[] args) throws Exception {
        try {
            // Read and combine log files into one
            String logfile = "log.txt";
            combineLogs(args[0], logfile);

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