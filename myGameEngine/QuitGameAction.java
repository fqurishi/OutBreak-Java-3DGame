package myGameEngine;

import ray.input.action.AbstractInputAction;
import game.MyGame;
import net.java.games.input.Event;
import network.ProtocolClient;

public class QuitGameAction extends AbstractInputAction {

	private MyGame game;
	private ProtocolClient protClient;
	
	public QuitGameAction(MyGame g,ProtocolClient p) {
		game = g;
		protClient = p;
	}
	
	public void performAction(float time, Event event) {
		System.out.println("shutdown requested");
		if(protClient != null && game.getIsConnected() == true){
			protClient.sendByeMessage();
			}
		game.shutdown();
	}
}