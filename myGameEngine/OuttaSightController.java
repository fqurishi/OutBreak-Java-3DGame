package myGameEngine;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;


public class OuttaSightController extends AbstractController{
	private float cycleTime = 550.0f;// default cycle time
	private float totalTime = 0.0f;
	
	
	@Override
	protected void updateImpl(float elapsedTimeMillis){
		totalTime += elapsedTimeMillis;
		for (Node n : super.controlledNodesList){
				if (totalTime > cycleTime) {
					n.moveForward(5.0f);
				}
		}
	}
} 