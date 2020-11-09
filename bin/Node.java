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
    // Holds a list of the neighbor nodes for the node.
    ArrayList<NeighborNode> neighbors;
    // Holds a list of the neighbor node IDs for the node.
    ArrayList<Integer> neighborIDs  = new ArrayList<>();


    // Concurrent hash maps:
    public Map<Integer, Boolean> keys = new ConcurrentHashMap<>();
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
        Description: Provides the list of NeighborNodes for this node.
        Parameters: ArrayList of NeighborNodes.
        Returns: Nothing
     */
    public void addNeighbors(ArrayList<NeighborNode> neighbors)
    {
        this.neighbors = neighbors;

        // Set neighbor IDs for this node
        for(NeighborNode neighbor : neighbors)
        {
            neighborIDs.add(neighbor.nodeID);
        }
    }

    /*
        Method: getNeighbors
        Description: Provides the list of NeighborNodes for this node.
        Parameters: None
        Returns: ArrayList of NeighborNodes
     */
    public ArrayList<NeighborNode> getNeighbors()
    {
        return neighbors;
    }

    /*
        Method: setUpNeighborMaps
        Description: Sets and initializes the keys, timestamps, and pending requests of the neighbors of this node.
        Parameters: None
        Returns: Nothing
     */
    public void setUpNeighborMaps()
    {
        // Make sure that neighbors list has been added.
        if(neighbors == null)
        {
            return;
        }

        // Otherwise, set keys, timestamp, and pending requests for neighbors of node.
        else
        {
            // Iterate through each neighbor of the node.
            for(NeighborNode neighbor: neighbors)
            {
                // Add timestamp for the neighbor - initialized to large value.
                timeStamps.put(neighbor.nodeID, Integer.MAX_VALUE);

                // Mark that no neighbors have a pending request.
                pendingRequests.put(neighbor.nodeID, false);

                // If neighbor's nodeID is larger than this node ID
                if(neighbor.nodeID > nodeID)
                {
                    // Set that this node holds the key for this neighbor.
                    keys.put(neighbor.nodeID, true);
                }
                else
                {
                    // Else, this node does not hold the key for this neighbor (the neighbor holds the key).
                    keys.put(neighbor.nodeID, false);
                }

            }
        }
    }

}
