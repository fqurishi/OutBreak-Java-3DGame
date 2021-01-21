package network;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class GhostNPC {
	private int id;
	private SceneNode node;
	private Entity entity;
	private Vector3 position = Vector3f.createFrom(0.0f, 0.0f, 0.0f);
	private Matrix3 rotation = Matrix3f.createIdentityMatrix();

	public GhostNPC(int id, Vector3 position) {
		this.id = id;
		this.position = position;
	}
	
	public GhostNPC(int id, Vector3 position, Matrix3 rotation) {
		this.id = id;
		this.position = position;
		this.rotation = rotation;
	}


	public SceneNode getNode() {
		return node;
	}


	public void setNode(SceneNode node) {
		this.node = node;
	}


	public Entity getEntity() {
		return entity;
	}


	public void setEntity(Entity entity) {
		this.entity = entity;
	}


	public Vector3 getPosition() {
		return position;
	}


	public void setPosition(Vector3 position) {
		this.position = position;
	}
	


	public Matrix3 getRotation() {
		return rotation;
	}


	public void setRotation(Matrix3 rotation) {
		this.rotation = rotation;
	}


	public int getID() {
		return id;
	}
	


}
