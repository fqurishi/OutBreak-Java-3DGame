package myGameEngine;
import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class BounceController extends AbstractController{
	private float bounceRate = .003f;// growth per second
	private float cycleTime = 2000.0f;// default cycle time
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	
	@Override
	protected void updateImpl(float elapsedTimeMillis){
		totalTime += elapsedTimeMillis;
		float bounceAmt = 1.0f + direction * bounceRate;
		if (totalTime > cycleTime){
			direction = direction + direction;
			totalTime = 0.0f;
			}
		for (Node n : super.controlledNodesList){
			Vector3 curScale = n.getLocalPosition();
			curScale = Vector3f.createFrom(curScale.x(), curScale.y()*bounceAmt, curScale.z());
			n.setLocalPosition(curScale);
		}
	}
} 