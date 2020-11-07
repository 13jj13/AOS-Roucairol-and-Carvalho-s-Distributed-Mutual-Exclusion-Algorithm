import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class ConfigFileInfo {

    // Holds location of config file.
    String filename;
    // Holds nodeID for the node.
    private int nodeID;
    // Holds info for the node.
    private Node node;
    // Holds a list of the neighbor nodes for this process / node.
    ArrayList<Node> neighbors;

    // Holds number of nodes in distributed system.
    private int numOfNodes = 0;
    // Holds the mean value for inter-request delay (in milliseconds).
    private int interRequestDelay = 0;
    // Holds the mean value for cs-execution time (in milliseconds).
    private int csExecutionTime = 0;
    // Holds number of requests each node should generate.
    private int numRequestsPerNode = 0;

    // Constructor - Stores process's ID and config filename. It then initiates
    // process to read config file and gather the info from it.
    public ConfigFileInfo(int nodeID, String filename)
    {
        this.nodeID = nodeID;
        this.filename = filename;
        neighbors = new ArrayList<Node>();

        // Read the config file and gather the information from it.
        readConfigFile();
    }

    /*
        Method: readConfigFile
        Description: Reads the config file and extracts the number of nodes in the distributed system, the node
            information (i.e. nodeID, hostname, listening port) for each node, and the neighbors for each node.
        Parameters: String filename of where config file is located.
        Returns: Nothing.
     */
    private void readConfigFile()
    {
        try {
            // Path of config file.
            Path path = Paths.get(filename);

            // Maximum allowed size for the config file.
            long max_file_size = 100000;

            // Check if file is too large.
            if(Files.size(path) > max_file_size)
            {
                // Print error statement and end program.
                System.out.println("The config file is larger than 100kB, which is too large for this program.");

                return;
            }

            // Open and read in information from config file.
            File config_file_obj = new File(filename);
            Scanner config_reader = new Scanner(config_file_obj);

            // Holds count of current number of valid lines read in config file.
            // It represents the current line being read.
            int line_num = 0;

            // Holds total number of valid lines in config file.
            int max_line_num = 0;

            // While the file is not empty
            while(config_reader.hasNextLine())
            {

                // Otherwise, read line, trim leading/trailing white space, and split around space delimiter/white space.
                String[] line = config_reader.nextLine().trim().split("\\s+");

                // Check that line is valid, i.e. first token of line is an unsigned integer.
                if(!line[0].matches("\\d+"))
                {
                    // Skip this line and go to next line in config file because first token is not an unsigned int.
                    continue;
                }

                // Increment this number at this point because line has been deemed valid.
                line_num++;

                // If first valid line in config file.
                if(line_num == 1)
                {
                    // Number of nodes in distributed system.
                    numOfNodes = Integer.parseInt(line[0]);

                    // Mean value for inter-request delay.
                    interRequestDelay = Integer.parseInt(line[1]);

                    // Mean value for cs-execution time.
                    csExecutionTime = Integer.parseInt(line[2]);

                    // Number of requests each nodes should generate.
                    numRequestsPerNode = Integer.parseInt(line[3]);

                    // Total number of valid lines in config file which is n+1.
                    max_line_num = numOfNodes+1;
                }

                // Else, if the line is one of the next n lines in the config file where nodeID, hostname, and port is given.
                else if(line_num > 1 && line_num <= max_line_num)
                {
                    // Create a node with the information from the line in the config file.
                    // Information : nodeID, hostname, listening port
                    Node curNode = new Node(Integer.parseInt(line[0]), line[1], Integer.parseInt(line[2]));

                    // If the node info read in is this node
                    if(curNode.nodeID == nodeID)
                    {
                        // Store node info
                        node = curNode;
                    }
                    // Else add the node as neighbor of this node.
                    else
                    {
                        neighbors.add(curNode);
                    }
                }

            }

            // Add neighbors to neighbors list in Node object for this process/node.
            node.addNeighbors(neighbors);

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public Node getNode()
    {
        return node;
    }

    public ArrayList<Node> getNeighbors()
    {
        return neighbors;
    }

    public int getNumOfNodes()
    {
        return numOfNodes;
    }

    public int getNodeID()
    {
        return nodeID;
    }

    public String getHostName()
    {
        return node.hostName;
    }

    public int getListeningPort()
    {
        return node.listeningPort;
    }

    public int getInterRequestDelay()
    {
        return interRequestDelay;
    }

    public int getCsExecutionTime()
    {
        return csExecutionTime;
    }

    public int getNumRequestsPerNode()
    {
        return numRequestsPerNode;
    }
}
