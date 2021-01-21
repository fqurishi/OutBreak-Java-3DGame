package myGameEngine;

import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotatePlayerLeft extends AbstractInputAction {


	private Node avN;
	private Camera3Pcontroller cp;
	private ProtocolClient protClient;
	public RotatePlayerLeft(Node n, Camera3Pcontroller c, ProtocolClient p)
	{
		cp = c;
		avN = n;
		protClient = p;
	}
	
	public void performAction(float time, Event event) {
		avN.roll(Degreef.createFrom(-1.5f));
		cp.OrbitAroundL();
		protClient.sendRotateMessage(avN.getWorldRotation());
	}
}