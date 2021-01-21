package myGameEngine;


import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.*;
import ray.rml.*;

public class Camera3Pcontroller {
	 private Camera camera;//the camera being controlled
	 private SceneNode cameraN;//the node the camera is attached to
	 private SceneNode target;//the target the camera looks at
	 private float cameraAzimuth;//rotation of camera around Y axis
	 private float cameraElevation;//elevation of camera above target
	 private float radias;//distance between camera and target
	 private Vector3 targetPos;//target’s position in the world
	 private Vector3 worldUpVec;
	
	 public Camera3Pcontroller(Camera cam, SceneNode camN,SceneNode targ, String controllerName, InputManager im)
	 {
		 camera = cam;
		 cameraN = camN;
		 target = targ;
		 cameraAzimuth = 225.0f;// start from BEHIND and ABOVE the target
		 cameraElevation = 25.0f;// elevation is in degrees
		 radias = 2.5f;
		 worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
		 setupInput(im, controllerName);
		 updateCameraPosition();
	 
	 }
	 
	 public void updateCameraOnDeath(SceneNode targ) {
		 target = targ;
	 }
	 
	// Updates camera position: computes azimuth, elevation, and distance
	 // relative to the target in spherical coordinates, then converts those
	 // to world Cartesian coordinates and setting the camera position
	 public void updateCameraPosition()
	 {
		 double theta = Math.toRadians(cameraAzimuth);// rot around target
		 double phi = Math.toRadians(cameraElevation);// altitude angle
		 double x = radias * Math.cos(phi) * Math.sin(theta);
		 double y = radias * Math.sin(phi);
		 double z = radias * Math.cos(phi) * Math.cos(theta);
		 targetPos = target.getWorldPosition();
		 
		 cameraN.setLocalPosition(Vector3f.createFrom
				 ((float)x, (float)y, (float)z).add(targetPos));
		 cameraN.setLocalRotation(
				 (cameraN.getLocalRotation()).add(target.getLocalRotation()));
		 cameraN.lookAt(target, worldUpVec);   
		
	 }
	 
	 private void setupInput(InputManager im, String cn)
	 {
		 Action orbitAAction = new OrbitAroundAction();
		 Action orbitEAction = new OrbitElevationAction();
		 Action orbitRAction = new OrbitRadiasAction();
		 Action orbitRActionM = new OrbitRadiasActionM();
		 if(im.getFirstGamepadName() != null) {
			 if(im.getFirstGamepadName() == cn) {
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.Z,
				 orbitAAction,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.RZ,
				 orbitEAction,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.POV,
				 orbitRAction,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			 }else {
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.X,
				 orbitAAction,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.Y,
				 orbitEAction,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 im.associateAction(cn,
				 net.java.games.input.Component.Identifier.Axis.Z,
				 orbitRActionM,
				 InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			 }
		 }
		  
	 }
	
	 @SuppressWarnings("unused")
	private class OrbitAroundAction extends AbstractInputAction
	 { // Moves the camera around the target (changes camera azimuth).
		 public void performAction(float time, net.java.games.input.Event evt)
		 { 
			 float rotAmount;
			 if (evt.getValue() < -0.2)
			 {
				 rotAmount=-0.2f; }
			 else
			 {
				 if (evt.getValue() > 0.2)
				 {
					 rotAmount=0.2f; }
				 else
				 {
					 rotAmount=0.0f; }
				 }
			 cameraAzimuth += rotAmount;
			 cameraAzimuth = cameraAzimuth % 360;
			 updateCameraPosition();
			 }
		 //  similar for OrbitRadiasAction, OrbitElevationAction 
	 }
	public void OrbitAroundL()
	{ 
		float rotAmount;
		rotAmount=1.5f; 
		cameraAzimuth += rotAmount;
		cameraAzimuth = cameraAzimuth % 360;
		updateCameraPosition();
	 }
	public void OrbitAroundR()
	{
		float rotAmount;
		rotAmount=-1.5f; 
		cameraAzimuth += rotAmount;
		cameraAzimuth = cameraAzimuth % 360;
		updateCameraPosition();
	}
	@SuppressWarnings("unused")
	private class OrbitElevationAction  extends AbstractInputAction
	{// Moves the camera around the target (changes camera elevation).
		 public void performAction(float time, net.java.games.input.Event evt)
		 { 
			 float rotAmount;
			 if (evt.getValue() < -0.2)
			 {
				 rotAmount=-0.2f; }
			 else
			 {
				 if (evt.getValue() > 0.2)
				 {
					 rotAmount=0.2f; }
				 else
				 {
					 rotAmount=0.0f; }
				 }
			 cameraElevation += rotAmount;
			 cameraElevation = cameraElevation % 360;
			 updateCameraPosition();
			 }
		 //  similar for OrbitRadiasAction, OrbitAroundAction 
		
	}
	@SuppressWarnings("unused")
	private class OrbitRadiasAction  extends AbstractInputAction
	{// Moves the camera around the target (changes camera radius aka zoom).
		 public void performAction(float time, net.java.games.input.Event evt)
		 { 
			 float rotAmount;
			 if (evt.getValue() == 0.25)
			 {
				 rotAmount=-0.2f; }
			 else
			 {
				 if (evt.getValue() == 0.75)
				 {
					 rotAmount=0.2f; }
				 else
				 {
					 rotAmount=0.0f; }
				 }
			 //System.out.println(evt.getValue());
			 radias += rotAmount;
			 radias = radias % 360;
			 if (radias <= 0.5f) {
				 radias = 0.5f;
			 }
			 else
			 {
				 if(radias >= 4.0f) {
					 radias = 4.0f;
				 }
			 }
			 updateCameraPosition();
			 }
		 //  similar for OrbitElevationAction, OrbitAroundAction 
		
	}
	@SuppressWarnings("unused")
	private class OrbitRadiasActionM  extends AbstractInputAction
	{// Moves the camera around the target (changes camera radius aka zoom).
		 public void performAction(float time, net.java.games.input.Event evt)
		 { 
			 float rotAmount;
			 if (evt.getValue() < -0.2)
			 {
				 rotAmount=-0.2f; }
			 else
			 {
				 if (evt.getValue() > 0.2)
				 {
					 rotAmount=0.2f; }
				 else
				 {
					 rotAmount=0.0f; }
				 }
			 //System.out.println(evt.getValue());
			 radias += rotAmount;
			 radias = radias % 360;
			 if (radias <= 0.5f) {
				 radias = 0.5f;
			 }
			 else
			 {
				 if(radias >= 4.0f) {
					 radias = 4.0f;
				 }
			 }
			 updateCameraPosition();
			 }
		 //  similar for OrbitElevationAction, OrbitAroundAction 
		
	}
	
	public float getAzimuth() {
		return cameraAzimuth;
	}

	

}
