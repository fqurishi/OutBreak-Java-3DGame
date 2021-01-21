
package myGameEngine;


import game.MyGame;
import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;

public class MoveForwardAction extends AbstractInputAction {
	
	private Node avN;
	private ProtocolClient protClient;
	private MyGame myGame;
	public MoveForwardAction(Node n, MyGame g, ProtocolClient p)
	{
		 avN = n;
		 protClient = p;
		 myGame = g;
	}
	public void performAction(float time, Event e)
	{
		if (e.getValue() == 1) {
			myGame.setIsBRunning(false);
			myGame.setIsRunning(true);
			myGame.setIsIdle(false);
			myGame.playerRun();
			myGame.moveAvatarForward(true);
			protClient.sendAnimation("run");
		}
		else {
			myGame.setIsRunning(false);
			myGame.setIsIdle(true);
			myGame.moveAvatarForward(false);
			myGame.playerIdle();
			protClient.sendAnimation("idle");
		}
		
	}   
}


