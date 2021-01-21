package network;

import game.MyGame;
import ray.networking.client.GameConnectionClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;


public class ProtocolClient extends GameConnectionClient {
	
	private MyGame game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	private Vector<GhostNPC> ghostNPCs;
	public ProtocolClient(InetAddress remAddr, int remPort,ProtocolType pType, MyGame game) throws IOException
	{   super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
		this.ghostNPCs = new Vector<GhostNPC>();
	} 
	
	@Override
	protected void processPacket(Object msg)
	{   String strMessage = (String)msg;
		String[] messageTokens = strMessage.split(",");
		if(messageTokens.length > 0)
		{	if(messageTokens[0].compareTo("join") == 0)	// receive “join”
			{ //  format:  join, success   or   join, failure
			if(messageTokens[1].compareTo("success") == 0){
				game.setIsConnected(true);
				createNPCInfo();
				System.out.println("Sending Create");
				sendCreateMessage(game.getPlayerPosition(), game.getPlayerRotationX(), game.getPlayerRotationY(), game.getPlayerRotationZ(), game.getPlayerAvatar());
			}
			if(messageTokens[1].compareTo("failure") == 0)
			{   game.setIsConnected(false);
		}   }
		if(messageTokens[0].compareTo("bye") == 0)	// receive “bye”
			{ //  format:  bye, remoteId
			UUID ghostID = UUID.fromString(messageTokens[1]);
			removeGhostAvatar(ghostID);
			} 
		if((messageTokens[0].compareTo("dsfr") == 0 )	// receive “dsfr”
				|| (messageTokens[0].compareTo("create")==0))
		{ //  format:  create, remoteId, x,y,z, v1,v2,v3   or   dsfr, remoteId, x,y,z, v1,v2,v3
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
			Matrix3 ghostRotation = Matrix3f.createFrom(Vector3f.createFrom(Float.parseFloat(messageTokens[5]), Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7])),
														Vector3f.createFrom(Float.parseFloat(messageTokens[8]), Float.parseFloat(messageTokens[9]), Float.parseFloat(messageTokens[10])),
														Vector3f.createFrom(Float.parseFloat(messageTokens[11]), Float.parseFloat(messageTokens[12]), Float.parseFloat(messageTokens[13])));
			String avatar = messageTokens[14];
			if(ghostAvatars.isEmpty())
				createGhostAvatar(ghostID, ghostPosition, ghostRotation, avatar);
			System.out.println("receiving create/details");
		} 
		  
		if(messageTokens[0].compareTo("wsds") == 0)	// rec. “wants...”
			{  //  etc.....  
			System.out.println("sending details");
			sendDetailsForMessage(this.id, game.getPlayerPosition(),game.getPlayerRotationX(),game.getPlayerRotationY(),game.getPlayerRotationZ(),game.getPlayerAvatar());
			
			}   
		if(messageTokens[0].compareTo("move") == 0)	// rec. “move...”
			{  //  etc.....  
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
			moveGhostAvatar(ghostID, ghostPosition);
			}
		if(messageTokens[0].compareTo("rotate") == 0)	// rec. “rotate...”
			{  //  etc.....  
			UUID ghostID = UUID.fromString(messageTokens[1]);
			Matrix3 ghostRotation = Matrix3f.createFrom(Vector3f.createFrom(Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]), Float.parseFloat(messageTokens[4])),
														Vector3f.createFrom(Float.parseFloat(messageTokens[5]), Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7])),
														Vector3f.createFrom(Float.parseFloat(messageTokens[8]), Float.parseFloat(messageTokens[9]), Float.parseFloat(messageTokens[10])));
			rotateGhostAvatar(ghostID, ghostRotation);
			}
		//health update
		if(messageTokens[0].compareTo("health") == 0)	// rec. “rotate...”
		{  //  etc.....  
			UUID ghostID = UUID.fromString(messageTokens[1]);
			int health = Integer.parseInt(messageTokens[2]);
			damageGhostAvatar(ghostID, health);
		}
		//npc messages
		if(messageTokens[0].compareTo("mnpc") == 0) {
			int ghostID = Integer.parseInt(messageTokens[1]);
			String ghostNName = messageTokens[2];
			if(ghostNPCs.isEmpty() != true)
				moveGhostNPC(ghostID, ghostNName);
		}
		if(messageTokens[0].compareTo("cnpc") == 0) {
			int ghostID = Integer.parseInt(messageTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4]));
			if(ghostNPCs.size() < 5)
				createGhostNPC(ghostID, ghostPosition);
		}
		if(messageTokens[0].compareTo("dnpc") == 0) {
			int ghostID = Integer.parseInt(messageTokens[1]);
			removeGhostNPC(ghostID);
		}
		
		//start message to synchronize games
		if(messageTokens[0].compareTo("start") == 0) {
			startGame();
		}
		
		//animation messages
		if(messageTokens[0].compareTo("anim") == 0) {
			String animation = messageTokens[1];
			player2Animate(animation);
		}
		
		}   } 
			
	
	public void sendJoinMessage()	//  format:  join, localId
	{   System.out.println("Sending join message!");
		try 
		{	sendPacket(new String("join," + id.toString()));
		}	catch (IOException e)  { e.printStackTrace();
	}   } 
			
	public void sendCreateMessage(Vector3 pos, Vector3 right, Vector3 up, Vector3 fwd, String avatar)
	{ //  format:  (create, localId, x,y,z)
		try
		{   String message = new String("create," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z() +
					 "," + right.x() + ","  + right.y() + ","  + right.z() +
					 "," + up.x() + ","  + up.y() + ","  + up.z() +
					 "," + fwd.x() + ","  + fwd.y() + ","  + fwd.z() + "," + avatar;
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		}
		
	
	} 
	
	public void sendNeedNPCsMessage()
	{ 
		try
		{   String message = new String("needNPC," + id.toString());
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		}
		
	
	} 
	
	public void sendDestroyNPCMessage(int ghostID) {
		try
		{   String message = new String("destroyNPC," + id.toString());
			message += "," + ghostID;
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		}
	}
	
	public void sendByeMessage()
	{  //  etc..... 
		try
		{   String message = new String("bye," + id.toString());
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		}
	}
	
	public void sendDetailsForMessage(UUID remId, Vector3 pos,  Vector3 fwd, Vector3 up, Vector3 right, String avatar)
	{  //  etc..... 
		try
		{   String message = new String("dsfr," + remId.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z() +
					 "," + right.x() + ","  + right.y() + ","  + right.z() +
					 "," + up.x() + ","  + up.y() + ","  + up.z() +
					 "," + fwd.x() + ","  + fwd.y() + ","  + fwd.z() + "," + avatar;
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
	
	}	}
	
	public void sendMoveMessage(Vector3 pos)
	{  // etc..... 
		try
		{   String message = new String("move," + id.toString());
			message += "," + pos.x()+ "," + pos.y() + "," + pos.z();
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		
	}   }  
	
	public void sendRotateMessage(Matrix3 rot)
	{  // etc..... 
		float[] floats = rot.toFloatArray();
		try
		{   String message = new String("rotate," + id.toString());
		
			message += "," + floats[0] + "," + floats[1] + "," + floats[2] +
					"," + floats[3] + "," + floats[4] + "," + floats[5] +
					"," + floats[6] + "," + floats[7] + "," + floats[8];
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		
	}   }  
	
	public void sendHealthMessage(int health) {
		try
		{   String message = new String("health," + id.toString());
			message += "," + health;
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		
	}   }  
	
	public void sendNPCDamageMessage(int npcid, int health) {
		try
		{   String message = new String("npcDamage," + id.toString());
			message += "," + npcid;
			message += "," + health;
			sendPacket(message);
		}  catch (IOException e)  {  e.printStackTrace();
		
	}   }  
	
	public void createGhostAvatar(UUID ghostID, Vector3 ghostPosition, Matrix3 ghostRotation, String skin) {
		final GhostAvatar avatar = new GhostAvatar(ghostID, ghostPosition, ghostRotation);
		try {
			game.addGhostAvatarToGameWorld(avatar, skin);
			ghostAvatars.add(avatar);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		final GhostAvatar avatar = findGhostAvatar(ghostID);
		if (avatar != null) {
			ghostAvatars.remove(avatar);
			game.removeGhostAvatarFromGameWorld(avatar);
		}
		
		
		
	}
	
	public void moveGhostAvatar(UUID ghostID, Vector3 pos) {
		final GhostAvatar avatar = findGhostAvatar(ghostID);
		try {
			avatar.getNode().setLocalPosition(pos);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}
	
	public void rotateGhostAvatar(UUID ghostID, Matrix3 rot) {
		final GhostAvatar avatar = findGhostAvatar(ghostID);
		try {
			avatar.getNode().setLocalRotation(rot);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}
	
	public void damageGhostAvatar(UUID ghostID, int health) {
		final GhostAvatar avatar = findGhostAvatar(ghostID);
		try {
			avatar.takeDamage(health);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	
	private GhostAvatar findGhostAvatar(UUID uuid) {
		if (uuid == null) return null;
		final Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while (it.hasNext()) {
			final GhostAvatar avatar = it.next();
			if (avatar.getUUID().toString().contentEquals(uuid.toString())) {
				return avatar;
			}
		}
		return null;
	}
	
	public void createGhostNPC(int ghostID, Vector3 ghostPosition) {
		GhostNPC npc = new GhostNPC(ghostID, ghostPosition);
		try {
			game.addGhostNPCToGameWorld(npc);
			ghostNPCs.add(npc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void removeGhostNPC(int ghostID) {
		final GhostNPC npc = findGhostNPC(ghostID);
		if (npc != null) {
			ghostNPCs.remove(npc);
			game.removeGhostNPCFromGameWorld(npc);
		}
		
		
		
	}
	
	public void moveGhostNPC(int ghostID, String nName) {
		final GhostNPC npc = findGhostNPC(ghostID);
		try {
			Vector3 pos = this.game.getEngine().getSceneManager().getSceneNode(nName).getWorldPosition();
			npc.getNode().setLocalPosition(pos);
			System.out.println("Moving ghost npc" + ghostID + ": " + pos);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}
	
	public void rotateGhostNPC(int ghostID, Matrix3 rot) {
		final GhostNPC npc = findGhostNPC(ghostID);
		try {
			npc.getNode().setLocalRotation(rot);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}
	
	private GhostNPC findGhostNPC(int id) {
		final Iterator<GhostNPC> it = ghostNPCs.iterator();
		while (it.hasNext()) {
			final GhostNPC npc = it.next();
			if (npc.getID() == id) {
				return npc;
			}
		}
		return null;
	}
	
	 public static float randInRangeFloat(float min, float max) {
	        return min + (float) (Math.random() * ((1 + max) - min));
	    }
	 
	 public void askForNPCInfo() {
		 try
		 {
			 String message = new String("needNPC," + id.toString());
			 sendPacket(message);
		 }
		 catch(IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public void createNPCInfo() {
		 try
		 {
			 String message = new String("createNPC," + id.toString());
			 sendPacket(message);
		 }
		 catch(IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 //ghost avatar animation and sounds
	 
	 public void sendAnimation(String animation) {
		 try {
			 String message = new String("animate," + id.toString());
			 message += "," + animation;
			 sendPacket(message);
		 }
		 catch(IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public void player2Animate(String animation) {
		 switch(animation) {
		 	case "run":
	    		game.setIsGARunning(true);
	    		game.setIsBRunning(false);
	    		game.setIsGAIdle(false);
	    		game.player2Run();
	    		break;
		 	case "idle":
	    		game.setIsGAIdle(true);
	    		game.setIsGARunning(false);
	    		game.setIsBRunning(false);
	    		game.player2Idle();
	    		break;
	    	case "bRun":
	    		game.setIsGARunning(false);
	    		game.setIsGAIdle(false);
	    		game.setIsGABRunning(true);
	    		game.player2BRun();
	    		break;
	    	case "shoot":
	    		game.player2Bullet();
	    		break;
	    	case "death":
	    		game.playerGADeath();
	    		break;
	    	default:
	    		break;
		 }
	 }
	 
	 
	 
	 public void startGame() {
		 game.setStart();
	 }
	
	
	
	
	
}
