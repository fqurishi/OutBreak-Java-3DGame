package myGameEngine;

import game.MyGame;
import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;

public class MoveBackwardAction extends AbstractInputAction {

	 private Node avN;
	private ProtocolClient protClient;
	private MyGame myGame;
	public MoveBackwardAction(Node n, MyGame g, ProtocolClient p)
	{
		avN = n;
		protClient = p;
		myGame = g;
	}
	public void performAction(float time, Event e)
	{
		if (e.getValue() == 1) {
			myGame.setIsBRunning(true);
			myGame.setIsRunning(false);
			myGame.setIsIdle(false);
			myGame.playerBRun();
			myGame.moveAvatarBackward(true);
			protClient.sendAnimation("bRun");
		}
		else {
			myGame.setIsBRunning(false);
			myGame.setIsIdle(true);
			myGame.moveAvatarBackward(false);
			myGame.playerIdle();
			protClient.sendAnimation("idle");
		}
	}   
		
}   
