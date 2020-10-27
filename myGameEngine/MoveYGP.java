package myGameEngine;


import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;


public class MoveYGP extends AbstractInputAction {
	 
	private Node avN;
	public MoveYGP(Node n)
	{
		avN = n;
	}
	
	public void performAction(float time, Event event) {
			if (event.getValue() < -0.1) {
				 avN.moveForward(event.getValue() * -0.02f);
				 avN.setLocalPosition(avN.getLocalPosition().x(), 0.5f, avN.getLocalPosition().z());
			}
			if (event.getValue() > 0.1) {
				 avN.moveForward(event.getValue() * -0.02f);
				 avN.setLocalPosition(avN.getLocalPosition().x(), 0.5f, avN.getLocalPosition().z());
			}
	}
}