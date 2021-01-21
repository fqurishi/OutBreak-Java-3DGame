
package myGameEngine;

import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotatePlayerXGP extends AbstractInputAction {

	private Node avN;
	private Camera3Pcontroller cp;
	private ProtocolClient protClient;
	public RotatePlayerXGP(Node n, Camera3Pcontroller c,  ProtocolClient p)
	{
		avN = n;
		cp = c;
		protClient = p;
	}
	
	public void performAction(float time, Event event) {
		if (event.getValue() < -0.1) {
			 avN.roll(Degreef.createFrom(-1.5f));
			 cp.OrbitAroundL();

		}
		if (event.getValue() > 0.1) {
			avN.roll(Degreef.createFrom(1.5f));
			cp.OrbitAroundR();
		}
		protClient.sendRotateMessage(avN.getWorldRotation());
			
	}
}