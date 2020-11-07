/*
    Name: Jennifer Ward
    Project: 2
    Course: CS 6378.002 - Advanced Operating Systems
    Description: This program is used to handle the following tasks.
    Distributed system: This program takes a process/node ID and config file location as an input and is used
    to implement a distributed system consisting of n nodes, number 0 to n-1, arranged in a certain topology.
    A process/node can only exchange messages with its neighbors. All channels are bidirectional, reliable, and
    satisfy the FIFO property. Each channel is implemented using a reliable socket connection via SCTP. For each
    channel, the socket connection is created at the beginning of the program and stays intact until the end of
    the program. All messages between neighboring nodes are exchanged over these connections.
    Synchronizer: This program also implements a synchronizer to simulate a synchronous distributed system. All nodes execute
    a sequence of rounds. In each round, a node sends one message to each of its neighbors, then waits to receive one
    message from each of its neighbors sent in that round and then advances to the next round. Any message received
    from a future round is buffered until the node has moved to that round.
    Distributed Algorithm: The synchronizer is then used to determine the k-hop neighbors and eccentricity of each node.
    The eccentricity of a node is defined as the max distance between a node to all other nodes in the topology.
 */

public class DistributedSystem
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

            System.out.println("Node: " + nodeID + " on machine " + configInfo.getHostName() + " on port " + configInfo.getListeningPort());

            // Check that node has its neighbors.
            while(node.getNeighbors() == null)
            {
                Thread.sleep(5);
            }

            // Set up neighbors' pending requests, permissions, and timestamps.
            node.setNeighbors();

            // Create server - pass the node/process for this program instance.
            SCTPServer server = new SCTPServer(configInfo);

            // Start server of this node.
            Thread serverThread = new Thread(server);
            serverThread.start();

            // Determine if neighbors are clients or servers.
            // If node for this program instance is client (i.e. node id > neighbor node id), then create client
            // with neighbor server info and connect channel.
            // Iterate through all neighbors of node.
            for(Node neighbor : node.getNeighbors())
            {
                // If node for this program instance is the client.
                if(nodeID > neighbor.nodeID)
                {
                    // Create client where nodeID is client, neighborID is server.
                    // Pass ClientServer for synchronization, the server and client node information, and number of rounds.
                    SCTPClient client = new SCTPClient(neighbor, node);

                    // Connect client to server.
                    Thread clientThread = new Thread(client);
                    clientThread.start();
                }
            }

            System.out.println("System setup is complete. Processes can now make critical section requests.");

            // Iterate each message request for the node.
            for(int reqNum = 0; reqNum < configInfo.getNumRequestsPerNode(); reqNum++)
            {
                // Request to enter critical section.
                node.csEnter();

                // Once it returns, then process can enter its critical section.
                System.out.println("Node " + nodeID + " entering its critical section.");
                System.out.println("Request: " + reqNum);

                // Critical section -  CS Execution Time
                // This is the time the node spends in its critical section.
                Thread.sleep(getExpProbDistRandomVar(configInfo.getCsExecutionTime()));

                // Exit critical section
                System.out.println("Node " + nodeID + " exiting its critical section.");

                node.csLeave();

                // Inter request delay - time elapsed between when a node's current request is
                // satisfied and when it generates the next request.
                Thread.sleep(getExpProbDistRandomVar(configInfo.getInterRequestDelay()));
            }

            System.out.println("Node " + nodeID + " executed all " + configInfo.getNumRequestsPerNode() + " critical sections.");


        }

        catch (Exception e)
        {
            System.out.println("Error occurred.");
            e.printStackTrace();
        }
    }

    //
    public static int getExpProbDistRandomVar(int mean)
    {
        int lambda = 1 / mean;
        double prob = Math.random();
        return (int) (-1 * mean * Math.log(prob / lambda));
    }

}
