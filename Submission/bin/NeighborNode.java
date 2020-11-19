public class NeighborNode {

    // Node Info:
    // Holds nodeID for the node.
    int nodeID;
    // Holds hostname for the node.
    String hostName;
    // Holds listening port for the node.
    int listeningPort;

    // Constructor - initialize all values.
    public NeighborNode(int nodeID, String hostName, int listeningPort)
    {
        // Initialize the nodeID, hostname, and listening port for the node.
        this.nodeID = nodeID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
    }
}
