package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;

public class MoveBackwardAction extends AbstractInputAction {

	 private Node avN;
	 public MoveBackwardAction(Node n)
	 {
		 avN = n;
		 }
	 public void performAction(float time, Event e)
	 {
		 avN.moveForward(-0.02f);
		 avN.setLocalPosition(avN.getLocalPosition().x(), 0.5f, avN.getLocalPosition().z());
	}   
}
