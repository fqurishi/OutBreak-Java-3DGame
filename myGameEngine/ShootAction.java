package myGameEngine;

import java.io.IOException;
import java.util.UUID;

import game.MyGame;
import net.java.games.input.Event;
import network.ProtocolClient;
import ray.input.action.AbstractInputAction;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Entity;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;


public class ShootAction extends AbstractInputAction{

	private Node avN;
	private ProtocolClient protClient;
	private MyGame myGame;
	public ShootAction(Node n, MyGame g, ProtocolClient p)
	{
		avN = n;
		protClient = p;
		myGame = g;
	}
	
	public void performAction(float time, Event event) {
		try {
			
			if(myGame.getCanShoot() && myGame.getIsAnimating() != true) {
			UUID id = UUID.randomUUID();
			Entity bulletE = myGame.getEngine().getSceneManager().createEntity("bullet" + id.toString(), "earth.obj");
			Texture tex = myGame.getEngine().getSceneManager().getTextureManager().getAssetByPath("whiteColor.jpg");
		    TextureState texState = (TextureState) myGame.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		    texState.setTexture(tex);
			bulletE.setRenderState(texState);
			SceneNode bulletNode = myGame.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("bullet" + id.toString() + "node");
			bulletNode.scale(0.02f, 0.02f, 0.02f);
			bulletNode.attachObject(bulletE);
			Vector3 bulletNodePos = avN.getWorldPosition();
			bulletNode.setLocalPosition(bulletNodePos.x(), bulletNodePos.y()+0.7f, bulletNodePos.z());
			
			PhysicsEngine physEng = myGame.getPhysicsEngine();
			double[] temptf = myGame.toDoubleArray(bulletNode.getLocalTransform().toFloatArray());
			PhysicsObject physObj = physEng.addSphereObject(physEng.nextUID(), 100f, temptf, 25f);
			physObj.setBounciness(0.25f);
			bulletNode.setPhysicsObject(physObj);
			
			final Vector3 forward = avN.getLocalUpAxis();
			float xForce = forward.x() == 0 ? 0 : forward.x() * 400000f;
			float yForce = forward.y() == 0 ? 0 : forward.y() * 400000f;
			float zForce = forward.z() == 0 ? 0 : forward.z() * 400000f;
			
			physObj.applyForce(xForce, yForce, zForce, 0, 0, 0);
			
			
			myGame.playerShoot(id);
			protClient.sendAnimation("shoot");
			}
			
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}