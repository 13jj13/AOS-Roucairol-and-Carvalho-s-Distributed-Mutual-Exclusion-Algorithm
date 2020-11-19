import java.io.Serializable;
import java.nio.ByteBuffer;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

// Enumeration to store message types
enum MessageType{REQUEST, RELEASE};

// Object to store message passing between nodes
// Message class can be modified to incorporate all fields that need to be passed
// Message needs to be serializable
// Most base classes and arrays are serializable
public class Message implements Serializable 
{
	MessageType msgType;
	// Holds the message string.
	public String message;
	// Holds the source node's ID.
	public int sourceNodeID;
	// Holds the destination node's ID.
	public int destNodeID;
	// Holds timestamp of message.
	public int timeStamp = 0;


	// Constructor
	public Message(String message, int sourceNodeID, int destNodeID)
	{
		this.message = message;
		this.sourceNodeID = sourceNodeID;
		this.destNodeID = destNodeID;
	}

	public Message(MessageType msgType, int sourceNodeID, int destNodeID, int timeStamp)
	{
		this.msgType = msgType;
		this.sourceNodeID = sourceNodeID;
		this.destNodeID = destNodeID;
		this.timeStamp = timeStamp;

		if(msgType == MessageType.RELEASE)
		{
			this.message = "RELEASE";
		}
		else if(msgType == MessageType.REQUEST)
		{
			this.message = "REQUEST";
		}
	}

	// Convert current instance of Message to ByteBuffer in order to send message over SCTP
	public ByteBuffer toByteBuffer() throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		oos.flush();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(bos.size());
		buf.put(bos.toByteArray());
		
		oos.close();
		bos.close();

		// Buffer needs to be flipped after writing
		// Buffer flip should happen only once		
		buf.flip();
		return buf;
	}

	// Retrieve Message from ByteBuffer received from SCTP
	public static Message fromByteBuffer(ByteBuffer buf) throws Exception
	{
		// Buffer needs to be flipped before reading
		// Buffer flip should happen only once
		buf.flip();
		byte[] data = new byte[buf.limit()];
		buf.get(data);
		buf.clear();

		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Message msg = (Message) ois.readObject();

		bis.close();
		ois.close();

		return msg;
	}
	
}
