
package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotateDolphinXGP extends AbstractInputAction {

	private Node avN;
	private Camera3Pcontroller cp;
	public RotateDolphinXGP(Node n, Camera3Pcontroller c)
	{
		avN = n;
		cp = c;
	}
	
	public void performAction(float time, Event event) {
		if (event.getValue() < -0.1) {
			 avN.yaw(Degreef.createFrom(1.0f));
			 cp.OrbitAroundL();

		}
		if (event.getValue() > 0.1) {
			avN.yaw(Degreef.createFrom(-1.0f));
			cp.OrbitAroundR();
		}
			
	}
}