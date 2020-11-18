/*
    Name: Jennifer Ward, Evelyn Wong
    Project: 2
    Course: CS 6378.002 - Advanced Operating Systems
    Description: This program is used to handle the following tasks.
    This is an implementation of a mutual exclusion service among n processes using Roucairol and Carvalho's
    distributed mutual exclusion algorithm. The service provides two function calls to the application: cs-enter()
    and cs-leave(). The first allows an application to request permissions to start executing its critical section.
    It is blocking and returns only when the invoking application can execute its critical section. The second allows
    an application to inform the service that it has finished executing its critical section.
    Each process consists of two separate modules. The top module implements the application (requests and executes
    critical sections). The bottom module implements the mutual exclusion service. The two modules interact
    using cs-enter() and cs-leave() functions.
    The application is responsible for generating critical section requests and then executing critical
    sections on receiving permission from the mutual exclusion service. Two parameters, inter-request delay and
    cs-execution time, are random variables with exponential probability distribution used to model the application.
    Inter-request delay is the time elapsed between when a node's current request is satisfied and when it generates
    the next request. CS-execution time denotes the time a node spends in its critical section.
 */
import java.io.*;
import java.time.LocalTime; // Import the LocalTime class
import java.time.Duration;  // Import the Duration class

public class Application
{

    public static void main(String[] args)
    {

        // Launcher script executes this program n times, one time for each node/process.
        // It will pass the process/node ID and config file location as commandline arguments to the program.
        try
        {
            // Check if no commandline argument was passed or more than two arguments were passed.
            if(args.length <= 0 || args.length > 2)
            {
                // Exit out of program.
                return;
            }

            // Otherwise, first commandline argument is nodeID.
            int nodeID = Integer.parseInt(args[0]);

            // Second commandline argument is config file location.
            String filename = args[1];

            // Retrieve config file information.
            ConfigFileInfo configInfo = new ConfigFileInfo(nodeID, filename);

            // Holds all information associated with this node (nodeID, port, host, neighbors, etc.)
            Node node = configInfo.getNode();

            System.out.println("Node: " + nodeID + " on machine " + node.hostName + " on port " + node.listeningPort);

            // Set up Roucairol and Carvalho's distributed mutual exclusion service.
            RCMutualExclusionService rc = new RCMutualExclusionService(node);

            // Check if all neighbors have been connected to node.
            while(!rc.areAllConnected())
            {
                Thread.sleep(100);
            }

            System.out.println("\n\nAll node " + node.nodeID + " neighbors are connected.");

            System.out.println("Number of nodes: " + configInfo.getNumOfNodes());
            System.out.println("Mean Inter-request Delay: " + configInfo.getInterRequestDelay());
            System.out.println("Mean CS-Execution Time: " + configInfo.getCsExecutionTime());
            System.out.println("Number of requests per node: " + configInfo.getNumRequestsPerNode());

            long initTime;
            long termTime;
            LocalTime initResponseTime;
            LocalTime termResponseTime;
            long ResponseTime;
            
            // file names for testing and evaluation
            String dir = "/home/010/e/el/elw160030/AOS/project2/test/" + configInfo.getNumOfNodes() + "-" + configInfo.getInterRequestDelay() + "-" + configInfo.getCsExecutionTime();
            String testfp = dir + "/log-p" + node.nodeID + ".txt";
            String evalfp = dir + "/eval-p" + node.nodeID + ".txt";
            
            File file = new File(testfp);
            File file2 = new File(evalfp);
            
            // create the directory if the dir is not already present
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();   
            }
            if(!file2.getParentFile().exists()) {
                file2.getParentFile.mkdirs();    
            }
            
            // create the file if the file is not already present
            if(!file.exists()){
                file.createNewFile();
            }
            if(!file2.exists()){
                file2.createNewFile();
            }

            //append the content to log file
            FileWriter fw = new FileWriter(file,true);
            FileWriter fw2 = new FileWriter(file2,true);
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            PrintWriter pw = new PrintWriter(bw);
            PrintWriter pw2 = new PrintWriter(bw2);
            
            pw.println();
            pw2.println();

            pw2.println("Throughput init time: " + LocalTime.now());
            System.out.println("Throughput init time: " + LocalTime.now());
            
            // Iterate through each message request for the node.
            for(int reqNum = 1; reqNum <= configInfo.getNumRequestsPerNode(); reqNum++)
            {
                initResponseTime = LocalTime.now();
                
                //System.out.println("Node " + node.nodeID + " requests to enter critical section " + reqNum + ".\n");
                // Request to enter critical section. Returns only when this process can enter its critical section.
                rc.csEnter();
                
                initTime = System.currentTimeMillis();

                // Once it returns, then process can enter its critical section.
                System.out.println("Node " + nodeID + " entering its critical section.");
                System.out.println("Request: " + reqNum);

                // Critical section -  CS Execution Time
                // This is the time the node spends in its critical section.
                Thread.sleep(getExpProbDistRandomVar(configInfo.getCsExecutionTime()));

                // Exit critical section
                System.out.println("Node " + nodeID + " exiting its critical section.");
                termTime = System.currentTimeMillis();
                termResponseTime = LocalTime.now();
                ResponseTime = Duration.between(initResponseTime, termResponseTime).toMillis();
                
                // Write response time to file
                pw2.println("PID #" + node.nodeID + " response time (ms): " + ResponseTime);
                System.out.println("PID #" + node.nodeID + " response time (ms): " + ResponseTime);
                
                // Write cs init and termination time to file
                pw.println("Process ID: " + node.nodeID
                            + "; init(x): " + initTime
                            + "; term(x): " + termTime);
                System.out.println("Process ID: " + node.nodeID
                                   + "; init(x): " + initTime
                                   + "; term(x): " + termTime);
                                
                // Inform mutual exclusion service that process has finished executing its critical section.
                rc.csLeave();

                // Inter request delay - time elapsed between when a node's current request is
                // satisfied and when it generates the next request.
                Thread.sleep(getExpProbDistRandomVar(configInfo.getInterRequestDelay()));
            }
            pw2.println("PID #" + node.nodeID + " - total message count: " + rc.getMsgCount());
            System.out.println("PID #" + node.nodeID + " - total message count: " + rc.getMsgCount());
            pw2.println("Throughput term time: " + LocalTime.now());
            System.out.println("Throughput term time: " + LocalTime.now());
            
            pw.println();
            pw2.println();
            bw.close();
            fw.close();
            pw.close();
            bw2.close();
            fw2.close();
            pw2.close();
            
            System.out.println("Node " + nodeID + " executed all " + configInfo.getNumRequestsPerNode() + " critical sections.");

        }

        catch (Exception e)
        {
            System.out.println("Error occurred.");
            e.printStackTrace();
        }
    }

    /*
        Method: getExpProbDistRandomVar
        Description: Returns random variable with exponential probability distribution.
        Parameters: Mean value for distribution (integer)
        Returns: Random integer with exp prob dist
    */
    public static int getExpProbDistRandomVar(int mean)
    {
        // Assume probability is random value between 0 and 1
        double prob = 1-Math.random();
        // Return random variable with exponential probability distribution.
        return (int) (-1 * mean * Math.log(prob) / Math.log(2));
    }

}
