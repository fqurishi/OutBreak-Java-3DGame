package myGameEngine;
import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class StretchController extends AbstractController{
	private float scaleRate = .003f;// growth per second
	private float cycleTime = 1000.0f;// default cycle time
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	@Override
	protected void updateImpl(float elapsedTimeMillis){
		totalTime += elapsedTimeMillis;
		float scaleAmt =  1.0f + direction * scaleRate;
		if (totalTime > cycleTime){
			direction = direction + direction;
			totalTime = 0.0f;
			}
		for (Node n : super.controlledNodesList){
			Vector3 curScale = n.getLocalScale();
			curScale = Vector3f.createFrom(curScale.x()+scaleAmt, curScale.y(), curScale.z());
			n.setLocalPosition(curScale);
		}
	}
} 