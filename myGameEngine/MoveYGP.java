package myGameEngine;


import game.MyGame;
import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;


public class MoveYGP extends AbstractInputAction {
	 
	private Node avN;
	private ProtocolClient protClient;
	private MyGame myGame;
	public MoveYGP(Node n, MyGame g, ProtocolClient p)
	{
		avN = n;
		protClient = p;
		myGame = g;
	}
	
	public void performAction(float time, Event event) {
			float dir = 0;
			if (event.getValue() < -0.1) {
				 dir = 1;
				 myGame.setIsBRunning(false);
				 myGame.setIsRunning(true);
				 myGame.setIsIdle(false);
				 myGame.playerRun();
				 protClient.sendAnimation("run");
			}
			else if (event.getValue() > 0.1) {
				 dir = -1;
				 myGame.setIsRunning(false);
				 myGame.setIsBRunning(true);
				 myGame.setIsIdle(false);
				 myGame.playerBRun();
				 protClient.sendAnimation("bRun");
			}
			else {
				myGame.setIsRunning(false);
				myGame.setIsBRunning(false);
				myGame.setIsIdle(true);
				if(myGame.getCanShoot()) {
					myGame.playerIdle();
					protClient.sendAnimation("idle");
				}
			}

			if(dir == 1) {
				myGame.moveAvatarForward(true);
				myGame.moveAvatarBackward(false);
			}
			else if(dir == -1) {
				myGame.moveAvatarBackward(true);
				myGame.moveAvatarForward(false);
			}
			else {
				myGame.moveAvatarBackward(false);
				myGame.moveAvatarForward(false);
			}
	}
}