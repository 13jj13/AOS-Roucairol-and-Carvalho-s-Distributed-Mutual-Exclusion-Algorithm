import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This class holds all the information for a node/process.
public class Node {

    // Node Info:
    // Holds nodeID for the node.
    int nodeID;
    // Holds hostname for the node.
    String hostName;
    // Holds listening port for the node.
    int listeningPort;
    // Holds a list of the neighbor IDs for the node.
    ArrayList<Node> neighbors;


    // Concurrent hash maps:
    public Map<Integer, Boolean> permissions = new ConcurrentHashMap<>();
    public Map<Integer, Integer> timeStamps = new ConcurrentHashMap<>();
    public Map<Integer, Boolean> pendingRequests = new ConcurrentHashMap<>();


    // Constructor - initialize all values.
    public Node(int nodeID, String hostName, int listeningPort)
    {
        // Initialize the nodeID, hostname, and listening port for the node.
        this.nodeID = nodeID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
    }

    /*
        Method: toString
        Description: Gives the node ID as the string version of a Node object.
        Parameters: None
        Returns: String NodeID of the node
     */
    @Override
    public String toString()
    {
        return "" + nodeID;
    }

    /*
        Method: addNeighbors
        Description: Provides the list of node IDs of the neighbors for this node.
        Parameters: ArrayList of neighbor node IDs (integers)
        Returns: Nothing
     */
    public void addNeighbors(ArrayList<Node> neighbors)
    {
        this.neighbors = neighbors;
    }

    public ArrayList<Node> getNeighbors()
    {
        return neighbors;
    }

    public void setNeighbors()
    {
        // Make sure that neighbors list has been added.
        if(neighbors == null)
        {
            return;
        }

        // Otherwise, set permissions, timestamp, and pending requests for neighbors of node.
        else
        {
            // Iterate through each neighbor for the node.
            for(Node neighbor: neighbors)
            {
                // Add timestamp for the neighbor.
                timeStamps.put(neighbor.nodeID, Integer.MAX_VALUE);

                // If neighbor's nodeID is larger than this node ID
                if(neighbor.nodeID > nodeID)
                {
                    // Set permission for this neighbor to be true.
                    permissions.put(neighbor.nodeID, true);
                }
                else
                {
                    // Else, set permissions for this neighbor to be false.
                    permissions.put(neighbor.nodeID, false);
                }

                // Mark that no neighbors have a pending request.
                pendingRequests.put(neighbor.nodeID, false);
            }
        }
    }

}
