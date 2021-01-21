package network;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Vector3f;

public class GameServerUDP extends GameConnectionServer<UUID>
{
	private List<GhostNPC> NPCs = new ArrayList<GhostNPC>();
	private int NPCSize = 10;
	private int playerCount = 0;

	public GameServerUDP(int localPort) throws IOException
	{super(localPort, ProtocolType.UDP);
		for (int i = 0; i < NPCSize; i++) {
			GhostNPC npc = new GhostNPC(i, Vector3f.createFrom(randInRangeFloat(-25, 25), 0.0f, randInRangeFloat(-25, 25)));
			NPCs.add(npc);
		}
	
	}
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sndPort)
	{
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) 
		{
			// case where server receives a JOIN message
			// format:  join,localid
			if(msgTokens[0].compareTo("join") == 0)
			{	try
				{	IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, sndPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					addClient(ci, clientID);
					sendJoinedMessage(clientID, true);
					System.out.println("Player " + clientID + " has joined");
					playerCount++;
					if (playerCount == 2) {
						sendStartMessage();
					}
					else if (playerCount == 3) {
						System.exit(0);
					}
				}
				catch (IOException e)
				{	e.printStackTrace();
			}	}
			
			// case where server receives a CREATE message
			// format:  create,localid,x,y,z
			if(msgTokens[0].compareTo("create") == 0)
			{	UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				String[] rot = {msgTokens[5], msgTokens[6], msgTokens[7],
								msgTokens[8], msgTokens[9], msgTokens[10],
								msgTokens[11], msgTokens[12], msgTokens[13]};
				String avatar = msgTokens[14];
				System.out.println("receiving create");
				sendCreateMessages(clientID, pos, rot, avatar);
				sendWantsDetailsMessages(clientID);
			}
			
			// case where server receives a BYE message
			// format:  bye,localid
			if(msgTokens[0].compareTo("bye") == 0)
			{	UUID clientID = UUID.fromString(msgTokens[1]);
				sendByeMessages(clientID);
				System.out.println("Player " + clientID + " has left");
				removeClient(clientID);
				playerCount--;
				if (playerCount == 0) {
					System.exit(0);
				}
				
			}
			
			// case where server receives a DETAILS-FOR message
			if(msgTokens[0].compareTo("dsfr") == 0)
			{	UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				String[] rot = {msgTokens[5], msgTokens[6], msgTokens[7],
								msgTokens[8], msgTokens[9], msgTokens[10],
								msgTokens[11], msgTokens[12], msgTokens[13]};
				String avatar = msgTokens[14];
				sendDetailsMsg(clientID, pos, rot, avatar);
				//sendNPCInfo(clientID);
			}
			// case where server receives a MOVE message
			if(msgTokens[0].compareTo("move") == 0)
			{	//  etc.....
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				//System.out.println("receiving move");
				sendMoveMessages(clientID, pos);
			}
			// case where server receives a ROTATE message
			if(msgTokens[0].compareTo("rotate") == 0)
			{	//  etc.....
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] rot = {msgTokens[2], msgTokens[3], msgTokens[4],
								msgTokens[5], msgTokens[6], msgTokens[7],
								msgTokens[8], msgTokens[9], msgTokens[10]};
				//System.out.println("receiving rotate");
				sendRotateMessages(clientID, rot);
			}
			//health update
			if(msgTokens[0].compareTo("health") == 0)
			{	//  etc.....
				UUID clientID = UUID.fromString(msgTokens[1]);
				String health = msgTokens[2];
				//System.out.println("receiving move");
				sendHealthMessages(clientID, health);
			}
			//npc messages
			if(msgTokens[0].compareTo("needNPC") == 0)
			{
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendNPCInfo(clientID);
			}
			if(msgTokens[0].compareTo("createNPC") == 0)
			{
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendCreateNPCInfo(clientID);
			}
			if(msgTokens[0].compareTo("npcDamage") == 0)
			{	//  etc.....
				UUID clientID = UUID.fromString(msgTokens[1]);
				String npcid = msgTokens[2];
				String health = msgTokens[3];
				//System.out.println("receiving move");
				sendNPCDamageMessages(clientID, npcid, health);
			}
//			if(msgTokens[0].compareTo("destroyNPC") == 0) {
//				UUID clientID = UUID.fromString(msgTokens[1]);
//				int ghostID = Integer.parseInt(msgTokens[2]);
//				sendDestroyNPCInfo(clientID, ghostID);
//			}
			//animate messages
			if(msgTokens[0].compareTo("animate") == 0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String animation = msgTokens[2];
				sendAnimationInfo(clientID, animation);
			}

		} }
	
	public void sendJoinedMessage(UUID clientID, boolean success)
	{   //  format:  join, success   or   join, failure
		try
		{	String message = new String("join,");
			if (success)  message += "success";
			else  message += "failure";
			sendPacket(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendCreateMessages(UUID clientID, String[] position, String[] rotation, String avatar)
	{	//  format:  create, remoteId, x, y, z
		try
		{	String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + rotation[0];
			message += "," + rotation[1];
			message += "," + rotation[2];
			message += "," + rotation[3];
			message += "," + rotation[4];
			message += "," + rotation[5];
			message += "," + rotation[6];
			message += "," + rotation[7];
			message += "," + rotation[8];
			message += "," + avatar;
			forwardPacketToAll(message, clientID);
			System.out.println("sending create");
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendDetailsMsg(UUID clientID, String[] position, String[] rotation, String avatar)
	{   
		try
		{	String message = new String("dsfr," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + rotation[0];
			message += "," + rotation[1];
			message += "," + rotation[2];
			message += "," + rotation[3];
			message += "," + rotation[4];
			message += "," + rotation[5];
			message += "," + rotation[6];
			message += "," + rotation[7];
			message += "," + rotation[8];
			message += "," + avatar;
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendWantsDetailsMessages(UUID clientID)
	{	//  etc.....
		try
		{	String message = new String("wsds," + clientID.toString());
		forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendMoveMessages(UUID clientID, String[] position)
	{
		try
		{	String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendRotateMessages(UUID clientID, String[] rotation)
	{
		try
		{	String message = new String("rotate," + clientID.toString());
			message += "," + rotation[0];
			message += "," + rotation[1];
			message += "," + rotation[2];
			message += "," + rotation[3];
			message += "," + rotation[4];
			message += "," + rotation[5];
			message += "," + rotation[6];
			message += "," + rotation[7];
			message += "," + rotation[8];
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendHealthMessages(UUID clientID, String health) {
		try
		{	String message = new String("health," + clientID.toString());
			message += "," + health;
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendByeMessages(UUID clientID)
	{  //   etc..... 
		try
		{	String message = new String("bye," + clientID.toString());
		forwardPacketToAll(message, clientID);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendNPCInfo(UUID clientID) {
		for(int i = 0; i < NPCSize; i++) {
			try
			{	String message = new String("mnpc," + i);
			message += "," + "npcN" + i;
			sendPacketToAll(message);
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
	}
	
	public void sendCreateNPCInfo(UUID clientID) {
		for(int i = 0; i < NPCSize; i++) {
			try
			{	String message = new String("cnpc," + i);
			message += "," + NPCs.get(i).getPosition().x();
			message += "," + NPCs.get(i).getPosition().y();
			message += "," + NPCs.get(i).getPosition().z();
			sendPacketToAll(message);
			System.out.println("Creating NPC");
			}
			catch (IOException e)
			{	e.printStackTrace();
			}
		}
	}
	
	public void sendNPCDamageMessages(UUID clientID, String ghostid, String health) {
		try
		{	String message = new String("npcDam");
			message += "," + ghostid;
			message += "," + health;
			sendPacketToAll(message);
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendDestroyNPCInfo(UUID clientID, int id) {
		try
		{	String message = new String("dnpc," + id);
		forwardPacketToAll(message, clientID);
		System.out.println("Destroying NPC");
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	public void sendStartMessage() {
		try
		{	String message = new String("start,");
		sendPacketToAll(message);
		System.out.println("Starting game");
		}
		catch (IOException e)
		{	e.printStackTrace();
		}
	}
	
	 public static float randInRangeFloat(float min, float max) {
	        return min + (float) (Math.random() * ((1 + max) - min));
	    }
	 
	 //animation message
	 public void sendAnimationInfo(UUID clientID, String animation) {
		 try
		 {
			 String message = new String("anim," + animation);
			 forwardPacketToAll(message, clientID);
		 }
		 catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	
	
	
	
} 
	
	
	
	
		
		
	
	
		
		
			
			
	
				
			
				
				
			
		
	
