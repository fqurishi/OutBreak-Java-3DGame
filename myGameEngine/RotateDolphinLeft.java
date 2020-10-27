package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;

public class RotateDolphinLeft extends AbstractInputAction {


	private Node avN;
	private Camera3Pcontroller cp;
	public RotateDolphinLeft(Node n, Camera3Pcontroller c)
	{
		cp = c;
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.yaw(Degreef.createFrom(1.0f));
		cp.OrbitAroundL();
	}
}