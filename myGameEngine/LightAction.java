package myGameEngine;


import game.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Light;
import ray.rage.scene.SceneNode;


public class LightAction extends AbstractInputAction{

	private MyGame myGame;
	public LightAction(MyGame g)
	{

		myGame = g;
	}
	
	public void performAction(float time, Event event) {
		//light switch
		SceneNode plight2Node = myGame.getEngine().getSceneManager().getSceneNode("plightNode2");
		Light plight2 = myGame.getEngine().getSceneManager().getLight("testLamp2");
		if (myGame.getLightOn() == true) {
			myGame.setLightOn(false);
			plight2Node.attachObject(plight2);
			}
		else {
			myGame.setLightOn(true);
			plight2Node.detachAllObjects();
			plight2Node.setLocalPosition(myGame.getEngine().getSceneManager().getSceneNode("myPlayerNode").getLocalPosition());
			
		}
	}
}