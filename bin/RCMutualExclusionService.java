import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class RCMutualExclusionService {

    public Node node;

    public int timeStamp = 0;
    private int reqTimeStamp = 0;
    public int msgCounter = 0;

    private boolean inCriticalSection = false;
    private boolean waitingForCriticalSection = false;

    public Map<Integer, Boolean> keys = new ConcurrentHashMap<>();
    public Map<Integer, Boolean> pendingRequests = new ConcurrentHashMap<>();
    public Map<Integer, NeighborConnectionHandler> neighborConnections = new ConcurrentHashMap<>();

    private final Semaphore semaphore = new Semaphore(1);


    public RCMutualExclusionService(Node node) throws Exception {
        this.node = node;

        // Initialize the keys and pending requests.
        initializeHashMaps();

        // Create server - pass the node/process for this program instance.
        SCTPServer server = new SCTPServer(node);

        // Determine if neighbors are clients or servers.
        // If node for this program instance is client (i.e. node id > neighbor node id), then create client
        // with neighbor server info and connect channel.
        // Iterate through all neighbors of node.
        for(NeighborNode neighbor : node.getNeighbors())
        {
            // If node for this program instance is the client.
            if(node.nodeID > neighbor.nodeID)
            {
                // Create client where nodeID is client, neighborID is server.
                // Pass the server (neighbor) and client node information.
                SCTPClient client = new SCTPClient(neighbor, node);

                // Create neighbor connection handler to handle messages between this node and this neighbor.
                NeighborConnectionHandler neighborConHandler = new NeighborConnectionHandler(neighbor, client.getChannel());

                // Add this neighbor to list of connections for this node and connect this node with its neighbor node.
                connectNeighbors(neighbor.nodeID, neighborConHandler);
            }
        }

        // Start server of this node. This handles the rest of the neighbor connections for this node.
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    /*
        Method: initializeHashMaps
        Description: Sets and initializes the keys and pending requests of the neighbors of this node.
        Parameters: None
        Returns: Nothing
     */
    public void initializeHashMaps()
    {
        // Make sure that neighbors list has been added.
        if(node.getNeighbors() == null)
        {
            return;
        }

        // Otherwise, set keys, timestamp, and pending requests for neighbors of node.
        else
        {
            // Iterate through each neighbor of the node.
            for(NeighborNode neighbor: node.getNeighbors())
            {

                // Mark that no neighbors have a pending request.
                pendingRequests.put(neighbor.nodeID, false);

                // If neighbor's nodeID is larger than this node ID
                if(neighbor.nodeID > node.nodeID)
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

    /*
        Method: connectNeighbors
        Description: Adds neighbor to list of connections and starts the neighbor thread.
        Parameters: Neighbor node ID (integer) and neighbor's connection handler
        Returns: Nothing
     */
    public void connectNeighbors(int neighborID, NeighborConnectionHandler handler)
    {
        // Add neighbor to list of connections
        neighborConnections.put(neighborID, handler);

        System.out.println("NEIGHBOR: Node " + node.nodeID + " is connected to " + neighborID);

        // Connect node with neighbor
        Thread thread = new Thread(handler);
        thread.start();

    }

    /*
        Method: areAllConnected
        Description: Determines if all neighbors have been connected to this node.
        Parameters: None
        Returns: Boolean - true if all have been connected, false if not
     */
    public boolean areAllConnected()
    {
        // If all neighbors have been connected
        if(node.neighbors.size() == neighborConnections.size())
        {
            return true;
        }
        // If not all neighbors have been connected
        else
        {
            return false;
        }
    }

    /*
        Method: csEnter
        Description: Allows process to request permission to start executing its critical section. Blocking function.
         It unblocks when it can enter its critical section.
        Parameters: None
        Returns: Nothing
    */
    public void csEnter() throws InterruptedException {
        semaphore.acquire();
        // Mark that process wants to enter its critical section.
        waitingForCriticalSection = true;
        // Increment timestamp for this process because new event is happening.
        timeStamp++;

        // Mark time when process started making requests.
        // This is the timestamp on the request(s) messages sent.
        reqTimeStamp = timeStamp;

        // Send requests to each neighbor.
        for(NeighborNode neighborNode : node.getNeighbors())
        {
            // If node does not already have the key from that neighbor
            if(!keys.get(neighborNode.nodeID))
            {
                // Send request message to that neighbor for the key.
                neighborConnections.get(neighborNode.nodeID).send(MessageType.REQUEST, node.nodeID, neighborNode.nodeID, reqTimeStamp);
            }
        }
        semaphore.release();

        // Block from entering critical section until receives all keys.
        while(true)
        {
            // If received keys from all neighbors
            if(!keys.containsValue(false))
            {
                // Node will enter critical section.
                inCriticalSection = true;

                // Return so node can execute its critical section.
                return;
            }
        }
    }

    /*
        Method: csLeave
        Description: Allows process to inform service that it has finished executing its critical section.
        Parameters: None
        Returns: Nothing
    */
    public void csLeave() throws InterruptedException {
        semaphore.acquire();
        // No longer waiting to enter critical section.
        waitingForCriticalSection = false;
        // No longer in critical section
        inCriticalSection = false;

        // Reset timestamp for request messages sent by this node.
        reqTimeStamp = Integer.MAX_VALUE;

        System.out.println("\nHandle pending requests");
        // Deal with all pending requests - release key to each requesting process.
        // Iterate through each pending request
        for(Map.Entry<Integer, Boolean> req : pendingRequests.entrySet())
        {
            // If there is a pending request from a neighbor, then its value
            // in the hash map will be true
            if(req.getValue())
            {
                // Send release message to the neighbor for the key
                neighborConnections.get(req.getKey()).send(MessageType.RELEASE, node.nodeID, req.getKey(), timeStamp);

                // Mark that this node no longer has the key
                keys.put(req.getKey(), false);

                // Mark that the pending request is satisfied
                pendingRequests.put(req.getKey(), false);
            }
        }
        System.out.println("All pending requests handled.");
        semaphore.release();

    }

    /*
        Method: getMsgCount
        Description: return the number of messages sent
        Parameters: None
        Returns: int - number of messages
     */
    public int getMsgCount() {
        return msgCounter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // This class handles request messages received by this node.
    private class RequestMessageHandler implements Runnable {

        Message msg;

        // Constructor - Holds request message received.
        public RequestMessageHandler(Message msg)
        {
            this.msg = msg;
        }

        /*
            Method: run()
            Description: Handles received request message: updates timestamp of this node based on the message and
             decide whether to send key to requesting process or mark request as pending.
            Parameters: None
            Returns: Nothing
        */
        @Override
        public void run()
        {
            //System.out.println("REQUEST RECEIVED: Node " + node.nodeID + " receives request from " + msg.sourceNodeID);

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If the current timestamp of this node/process is less than the timestamp
            // on the message, then update the current timestamp.
            if(timeStamp < msg.timeStamp)
            {
                timeStamp = msg.timeStamp;
            }

            // Decide whether to send key to requesting process or mark request as pending.

            // If this node is in its critical section, then mark request as pending.
            if(inCriticalSection)
            {
                System.out.println(msg.sourceNodeID + " REQ PENDING: Node " + node.nodeID + " in CS. REQUEST from " + msg.sourceNodeID + " marked pending.");
                pendingRequests.put(msg.sourceNodeID, true);
            }

            // The node is not in its critical section, but it is waiting to enter its critical section.
            // If the node has a pending request with smaller timestamp than the requesting process,
            // then the requesting process is marked as pending.
            else if(waitingForCriticalSection && reqTimeStamp < msg.timeStamp)
            {

                System.out.println(msg.sourceNodeID + " REQ PENDING: Node " + node.nodeID + " has smaller timestamp. REQUEST from " + msg.sourceNodeID + " marked pending.");

                pendingRequests.put(msg.sourceNodeID, true);
            }

            // The node is not in its critical section, but it is waiting to enter its critical section.
            // If the node has a pending request with the same timestamp as the requesting process but the nodeID
            // of the neighbor is greater than the nodeID of this process, then the requesting process is marked as
            // pending.
            else if(waitingForCriticalSection && reqTimeStamp == msg.timeStamp && node.nodeID < msg.sourceNodeID)
            {
                System.out.println(msg.sourceNodeID + " REQ PENDING: Node " + node.nodeID + " has smaller node ID. REQUEST from " + msg.sourceNodeID + " marked pending.");

                pendingRequests.put(msg.sourceNodeID, true);
            }

            // Node sends the key to the requester because this node is not executing its critical section, it
            // does not have a pending request (not waiting to enter its critical section), its pending request has a
            // larger timestamp than that of the requesting process message, or the timestamps are the same but the
            // message source node ID has a smaller node ID.
            else
            {
                System.out.println(msg.sourceNodeID + " REQ MET: Node " + node.nodeID + " satisfies REQUEST from " + msg.sourceNodeID);

                // Mark that key is no longer held by this process.
                keys.put(msg.sourceNodeID, false);

                // Send release message to requesting process.
                neighborConnections.get(msg.sourceNodeID).send(MessageType.RELEASE, node.nodeID, msg.sourceNodeID, timeStamp);

                // If this process is still waiting to execute its critical section, then send a
                // request message to this neighbor for the key again.
                if(waitingForCriticalSection)
                {
                    // Send request message to process that just took the key.
                    neighborConnections.get(msg.sourceNodeID).send(MessageType.REQUEST, node.nodeID, msg.sourceNodeID, reqTimeStamp);
                }
            }

            semaphore.release();
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // This class handles any release messages received by this node.
    private class ReleaseMessageHandler implements Runnable {

        Message msg;

        // Constructor - Holds request message received.
        public ReleaseMessageHandler(Message msg)
        {
            this.msg = msg;
        }

        @Override
        public void run()
        {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(msg.sourceNodeID + " RELEASE RECEIVED: Node " + node.nodeID + " received release from " + msg.sourceNodeID);

            // Add this key to the list of keys.
            keys.put(msg.sourceNodeID, true);
            semaphore.release();
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // This class handles neighbor connections with this node. It maintains the neighbor node info and the channel
    // connection between the neighbor and this node.
    private class NeighborConnectionHandler extends Thread {

        // Neighbor node information
        NeighborNode neighborNode;
        // Channel connection between neighbor and this node
        SctpChannel channel;

        // Size of ByteBuffer to accept incoming messages
        private int MAX_MSG_SIZE = 4096;

        // Constructor - initializes neighbor node info and channel
        public NeighborConnectionHandler(NeighborNode neighborNode, SctpChannel channel)
        {
            this.neighborNode = neighborNode;
            this.channel = channel;
        }

        /*
            Method: run()
            Description: Continuously receive request/release messages and handle them.
            Parameters: None
            Returns: Nothing
         */
        @Override
        public void run()
        {
            while(true) {
                try {

                    // Create buffer for incoming message
                    ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MSG_SIZE);

                    // Receive message
                    channel.receive(buf, null, null);

                    // Retrieve message from byte buffer
                    Message receivedMessage = Message.fromByteBuffer(buf);

                    // If message is a REQUEST message
                    if (receivedMessage.msgType == MessageType.REQUEST)
                    {
                        // Execute REQUEST message handling thread

                        RequestMessageHandler msgHandler = new RequestMessageHandler(receivedMessage);
                        Thread thread = new Thread(msgHandler);
                        thread.start();

                        //requestMessageReceived(receivedMessage);
                    }

                    // If message is a RELEASE message
                    else if (receivedMessage.msgType == MessageType.RELEASE)
                    {
                        // Execute RELEASE message handling thread

                        ReleaseMessageHandler msgHandler = new ReleaseMessageHandler(receivedMessage);
                        Thread thread = new Thread(msgHandler);
                        thread.start();

                        //releaseMessageReceived(receivedMessage);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }

        /*
            Method: send
            Description: Sends message (REQUEST, RELEASE) to destNodeID from sourceNodeID
            Parameters: Message type, source node ID, destination node ID, and timestamp of message
            Returns: Nothing
        */
        public void send(MessageType msgType, int sourceNodeID, int destNodeID, int timeStamp)
        {
            try {
                // MessageInfo for SCTP layer
                MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
                // Message to send - includes message type (REQUEST/RELEASE), source node ID, destination node ID,
                // and timestamp of message
                Message msg = new Message(msgType, sourceNodeID, destNodeID, timeStamp);
                // Messages are sent over SCTP using ByteBuffer
                channel.send(msg.toByteBuffer(), messageInfo);
                msgCounter++;


                System.out.println(msg.message + " MSG SENT: " + msg.message + " to neighbor " + destNodeID);
            }
            catch(Exception e)
            {
                System.out.println("Error in sending message.");
                e.printStackTrace();
            }

        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // This object is a SCTPServer to accept multiple connections from different SCTPClients.
    private class SCTPServer implements Runnable
    {
        // Server node information
        // Client should connect to same port number that server opens
        Node serverInfo;

        // Server channel
        SctpServerChannel sctpServerChannel;

        // Constructor - initialize server node information, create server
        public SCTPServer(Node serverInfo) throws Exception {
            this.serverInfo = serverInfo;

            // Get address from port number
            InetSocketAddress addr = new InetSocketAddress(serverInfo.listeningPort);
            // Open server channel
            sctpServerChannel = SctpServerChannel.open();
            // Bind server channel to address
            sctpServerChannel.bind(addr);

            //System.out.println("SERVER: Server created at node " + serverInfo.nodeID);
        }

        /*
            Method: run()
            Description: Start server - Connect to clients.
            Parameters: None
            Returns: Nothing
         */
        @Override
        public void run() {
            while(true) {
                try {

                    // Wait for incoming connection from client - accept() blocks until connection made.
                    SctpChannel sctpChannel = sctpServerChannel.accept();
                    Thread.sleep(3000);


                    // Create new thread for the new client.
                    Thread thread = new Thread(new ClientHandler(sctpChannel, serverInfo));
                    thread.start();

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }

        // This class handles each client connection.
        private class ClientHandler implements Runnable
        {
            // Size of ByteBuffer to accept incoming messages
            private int MAX_MSG_SIZE = 4096;
            // SCTP channel
            SctpChannel sctpChannel;
            // Holds server node information
            Node serverInfo;
            // Holds client node ID.
            int clientNodeID;

            // Constructor
            public ClientHandler(SctpChannel sctpChannel, Node serverInfo)
            {
                this.sctpChannel = sctpChannel;
                this.serverInfo = serverInfo;
            }

            /*
                Method: run()
                Description: Receives initial message from client, sets up neighbor connection handler, and connects.
                Parameters: None
                Returns: Nothing
            */
            @Override
            public void run()
            {
                try {
                    // Handle initial message exchange between client and server:

                    // Buffer to hold message from client.
                    ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MSG_SIZE);
                    // Messages are received over SCTP from client using ByteBuffer
                    sctpChannel.receive(buf, null, null);
                    // Store client information.
                    clientNodeID = Message.fromByteBuffer(buf).sourceNodeID;

                    //System.out.println("INITIAL MSG RECEIVED: Client node is " + clientNodeID);

                    int clientNodeIndex = node.neighborIDs.indexOf(clientNodeID);

                    // Create neighbor connection handler to handle messages between this node and this neighbor.
                    NeighborConnectionHandler neighborConHandler = new NeighborConnectionHandler(node.neighbors.get(clientNodeIndex), sctpChannel);

                    // Add this neighbor to list of connections for this node and connect this node with its neighbor node.
                    connectNeighbors(clientNodeID, neighborConHandler);

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // This object is an SCTPClient used to connect to SCTPServer.
    private class SCTPClient
    {
        // Size of ByteBuffer to accept incoming messages
        private int MAX_MSG_SIZE = 4096;
        // Holds address of server
        InetSocketAddress addr;

        // Holds server node information
        NeighborNode serverInfo;
        // Holds client node information
        Node clientInfo;

        // SCTP channel
        SctpChannel sctpChannel = null;

        // Constructor - initialize port and hostname of server to connect to
        public SCTPClient(NeighborNode serverInfo, Node clientInfo) throws Exception
        {
            this.serverInfo = serverInfo;
            this.clientInfo = clientInfo;

            // Get address of server using name and port number.
            addr = new InetSocketAddress(serverInfo.hostName, serverInfo.listeningPort);

            // Try to connect to server

            // Loops until connection is made.
            boolean connected = false;

            while(!connected) {

                try {
                    Thread.sleep(3000);

                    // Open SCTP channel to connect to server using the address
                    sctpChannel = SctpChannel.open(addr, 0, 0);
                    //System.out.println("CHANNEL: Client connected to server " + serverInfo.hostName + " of node " + serverInfo.nodeID);
                    connected = true;

                    // Initial message exchange between client and server:

                    // Send message to give server the client node information.
                    // MessageInfo for SCTP layer
                    MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
                    Message msg = new Message("Sending client info to server node " + serverInfo.nodeID,
                            clientInfo.nodeID, serverInfo.nodeID);
                    // Messages are sent over SCTP using ByteBuffer
                    sctpChannel.send(msg.toByteBuffer(), messageInfo);
                    //System.out.println("INITIAL MSG SENT: " + msg.message);

                } catch (Exception e) {
                    System.out.println("Server is offline..Attempting to reconnect.");
                    e.printStackTrace();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        /*
            Method: getChannel
            Description: Returns SCTP channel from this client to the server it connected to.
            Parameters: None
            Returns: SCTP channel
        */
        public SctpChannel getChannel()
        {
            return sctpChannel;
        }

    }

}
