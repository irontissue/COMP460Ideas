package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;
import com.mygdx.game.util.Constants;

import java.util.UUID;

/**
 * A HadalEntity is enything in the Game world that does stuff.
 * A HadalEntity contains the method to create a Box2d body. It is not a body itself.
 * The entity also runs a method every engine tick. Anything that reacts to anything must be an entity.
 * Children: Schmucks, Hitboxes, Events. Walls are not entities.
 * All entities must have a HadalData. Class hierarchy for userData corresponds to that of entities (so far).
 * @author Zachary Tu
 *
 */
public abstract class Entity {

	//References to game fields.
	protected transient PlayState state;
	protected World world;
	protected OrthographicCamera camera;
	protected RayHandler rays;
	
	//Fields common to all entities.
	public Body body;
	protected UserData userData;
	public float height, width;
	protected float startX, startY;
	public UUID entityID;
	public boolean alive = true;
	public boolean synced;
    public static final int ENTITY_TYPE = Constants.EntityTypes.ENTITY;
	/**
	 * Constructor is called when an entity is created. USE THIS FOR SERVER
	 * @param state: Current playstate
	 * @param world: Current game world
	 * @param camera: Current game camera
	 * @param rays: Current rayhandler
	 * @param w: Width
	 * @param h: Height
	 * @param startX: Starting x position
	 * @param startY: Starting y position
	 */
	public Entity(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h, float startX, float startY, boolean synced) {

        entityID = UUID.randomUUID();

        this.state = state;
        this.world = world;
        this.camera = camera;
        this.rays = rays;

        this.width = w;
        this.height = h;
        this.startX = startX;
        this.startY = startY;
        this.synced = synced;

        //Queue this entity up for creating in the world next engine tick
        state.create(this);


//        if (state.playerNumber == null || state.playerNumber.getClient().allowedToCreate || state.playerNumber.getClient().master) {
//            state.create(this);
//        }

	}
	/**
	 * Constructor is called when an entity is created. USE THIS FOR CLIENT.
	 * @param state: Current playstate
	 * @param world: Current game world
	 * @param camera: Current game camera
	 * @param rays: Current rayhandler
	 * @param w: Width
	 * @param h: Height
	 * @param startX: Starting x position
	 * @param startY: Starting y position
	 * @param id: UUID of entity you are creating, in String format
	 */
    public Entity(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h, float startX, float startY, boolean synced, String id) {
        if (id == null) {
            entityID = UUID.randomUUID();
        } else {
            entityID = UUID.fromString(id);
        }

        this.state = state;
        this.world = world;
        this.camera = camera;
        this.rays = rays;

        this.width = w;
        this.height = h;
        this.startX = startX;
        this.startY = startY;
        this.synced = synced;

        //Queue this entity up for creating in the world next engine tick
        state.create(this);

    }
		
	/**
	 * This method is called by the playstate next engine tick after initializing this entity.
	 * Usually, this is where the entity's body + data is created.
	 */
	public abstract void create();

	/**
	 * This method is run every engine tick. Here goes the entities game logic like enemy ai or playerNumber input.
	 * @param delta: time elapsed since last engine tick.
	 */
	public abstract void controller(float delta);
	
	/**
	 * Draw the entity
	 * @param batch: SpriteBatch for rendering
	 */
	public abstract void render(SpriteBatch batch);
	
	public void renderAboveShadow(SpriteBatch batch) {
		
	}
	
	
	/**
	 * Call this method to delete a body. NOT dispose().
	 * This tells the playstate to remove this entity next engine tick.
	 */
	public void queueDeletion() {
		alive = false;
		state.destroy(this);
	}
	
	/**
	 * This method is called by the playstate next engine tick after deleting this entity.
	 * This is where the body is actually deleted
	 */
	public void dispose() {
		world.destroyBody(body);
	}	
	
	/**
	 * A simple helper method that converts a screen coordinate into an impulse applied to this entity's body.
	 * @param x: x position in screen coordinates
	 * @param y: y position in screen coordinates
	 * @param power: Magnitude of impulse
	 */
	public void recoil(float x, float y, float power) {
		
		Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
				
		camera.project(bodyScreenPosition);
		
		float powerDiv = bodyScreenPosition.dst(x, y, 0) / power;
		
		float xImpulse = (bodyScreenPosition.x - x) / powerDiv;
		float yImpulse = (bodyScreenPosition.y - y) / powerDiv;
		
		body.applyLinearImpulse(new Vector2(xImpulse, yImpulse), body.getWorldCenter(), true);
	}
	
	public void push(float impulseX, float impulseY) {
		body.applyLinearImpulse(new Vector2(impulseX, impulseY), body.getWorldCenter(), true);
	}

	/**
	 * Getter method for the entity's body.
	 * @return: Entity's body.
	 */
	public Body getBody() {
		return body;
	}
	public PlayState getState() {
		return state;
	}
	
}