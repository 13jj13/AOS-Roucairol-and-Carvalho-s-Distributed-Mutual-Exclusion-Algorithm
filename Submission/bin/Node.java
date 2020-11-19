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


}
