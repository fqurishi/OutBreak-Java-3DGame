package myGameEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.MyGame;
import ray.physics.PhysicsEngine;
import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class BulletController extends AbstractController{
	private float cycleTime = 550.0f;// default cycle time
	private float totalTime = 0.0f;
	private PhysicsEngine physEngine;
	private SceneManager sm;
	private List<Node> removeBullets = new ArrayList<Node>();
	private MyGame myGame;
	
	public BulletController(PhysicsEngine physicsEngine, SceneManager sceneM, MyGame g, List<Node> bullets) {
		this.sm = sceneM;
		this.physEngine = physicsEngine;
		this.myGame = g;
		this.removeBullets = bullets;
	}
	
	@Override
	protected void updateImpl(float elapsedTimeMillis){
		totalTime += elapsedTimeMillis;
		//check if bullets have passed their limit
		for (Node n : super.controlledNodesList){
				if (totalTime > cycleTime) {
					removeBullets.add(n);
					totalTime = 0.0f;
				}
		}
		//destroy bullets that have passed their limit
		for(Node n : removeBullets) {
			this.removeNode(n);
			//remove it from physics world too, (needs cast to change to SceneNode)
			physEngine.removeObject(((SceneNode)n).getPhysicsObject().getUID());
			//remove entity
			sm.destroyEntity(((SceneNode) n).getAttachedObject(0).getName());
			//destroy the node
			sm.destroySceneNode((SceneNode) n);
			myGame.setBulletID(null);
		}
		removeBullets.clear();
		
	}
} 