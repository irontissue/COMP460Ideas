package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.SteeringUtil;
import com.badlogic.gdx.ai.utils.Location;

import static com.mygdx.game.util.Constants.PPM;


import box2dLight.RayHandler;

import java.util.UUID;

public class Schmuck extends Entity implements Location<Vector2> {

	//user data.
	protected CharacterData bodyData;
	
	//Counters that keep track of delay between action initiation + action execution and action execution + next action
	public float shootCdCount = 0;
	public float shootDelayCount = 0;
	
	//The last used tool. This is used to process equipment with a delay between using and executing.
	public Equipment usedTool;
	
	//This counter keeps track of elapsed time so the entity behaves the same regardless of engine tick time.
	public float controllerCount = 0;
	
	public float desiredXVel = 0.0f;
	public float desiredYVel = 0.0f;
	
	public float desiredAngleVel = 0.0f;
	
	public float acceleration = 0.1f;
	
	public int spriteWidth = 197;
	public int spriteHeight = 76;
	
	public int hbWidth = 76;
	public int hbHeight = 197;
	
	public static float scale = 0.25f;
	
	private TextureAtlas atlas;
	private TextureRegion schmuckSprite;
	
	/**
	 * This constructor is called when a Schmuck is made.
	 * @param state: Current playState
	 * @param world: Box2d world
	 * @param camera: Game camera
	 * @param rays: game rayhandler
	 * @param w: width
	 * @param h: height
	 * @param startX: starting x position
	 * @param startY: starting y position
	 */
	public Schmuck(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
			float startX, float startY) {
		super(state, world, camera, rays, w * scale, h * scale, startX, startY);
		atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.FISH_ATL.toString());
		schmuckSprite = atlas.findRegion("spittlefish_swim");
//		if (state.player != null) {
//            state.player.getClient().client.sendTCP(new Packets.SyncCreateSchmuck(w,h,startX,startY,this.entityID));
//        }
	}

    public Schmuck(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
                   float startX, float startY, UUID id) {
        super(state, world, camera, rays, w * scale, h * scale, startX, startY, id);
        atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.FISH_ATL.toString());
        schmuckSprite = atlas.findRegion("spittlefish_swim");
    }
	
	public Schmuck(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
			float startX, float startY, String spriteId, int width, int height, int hbWidth, int hbHeight) {
		super(state, world, camera, rays, width * scale, height * scale, startX, startY);
		this.atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.FISH_ATL.toString());
		this.schmuckSprite = atlas.findRegion(spriteId);
		this.schmuckSprite = new TextureRegion(new Texture(AssetList.GROOM.toString()));
		this.hbWidth = hbWidth;
		this.hbHeight = hbHeight;
		this.spriteWidth = width;
		this.spriteHeight = height;
	}

	/**
	 * When this schmuck is added to the world, give it a foot to keep track of whether it is grounded or not.
	 * IMPORTANT: this method does not create the entity's body! 
	 * Subclasses must create the schmuck's body before calling super.create()! Otherwise body + bodyData will be null.
	 */
	@Override
	public void create() {
		this.userData = bodyData;
	}

	
	/**
	 * The basic behaviour of a schmuck depends on its moveState.
	 * This method contains some physics that constrains schmucks in addition to box2d stuff.
	 */
	@Override
	public void controller(float delta) {
		controllerCount+=delta;
		if (controllerCount >= 1/60f) {
			controllerCount -= 1/60f;
			
			Vector2 currentVel = body.getLinearVelocity();
			
			float newX = acceleration * desiredXVel + (1 - acceleration) * currentVel.x;
			
			float newY = acceleration * desiredYVel + (1 - acceleration) * currentVel.y;
			
			Vector2 force = new Vector2(newX - currentVel.x, newY - currentVel.y).scl(body.getMass());
			body.applyLinearImpulse(force.scl((1 + bodyData.getBonusLinSpeed())), body.getWorldCenter(), true);
			
			desiredXVel = 0.0f;
			desiredYVel = 0.0f;
			
			float currentAngleVel = body.getAngularVelocity();
			
			float newAngleVel = acceleration * desiredAngleVel + (1 - acceleration) * currentAngleVel;
			
			
			float angularForce = (newAngleVel - currentAngleVel) * (body.getMass());
			body.applyAngularImpulse(angularForce * (1 + bodyData.getBonusAngSpeed()), true);
			
			desiredAngleVel = 0.0f;
		}
		
		//Apply base hp regen
		bodyData.regainHp(bodyData.getHpRegen() * delta);
		
		//process cooldowns
		shootCdCount-=delta;
		shootDelayCount-=delta;
		
		//If the delay on using a tool just ended, use the tool.
		if (shootDelayCount <= 0 && usedTool != null) {
			useToolEnd();
		}
	}


	/**
	 * Draw the schmuck
	 */
	@Override
	public void render(SpriteBatch batch) {
		
		batch.setProjectionMatrix(state.sprite.combined);

		batch.draw(schmuckSprite, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));
	}
	
	/**
	 * This method is called when a schmuck wants to use a tool.
	 * @param delta: Time passed since last usage. This is used for Charge tools that keep track of time charged.
	 * @param tool: Equipment that the schmuck wants to use
	 * @param hitbox: aka filter. Who will be affected by this equipment? Player or enemy or neutral?
	 * @param x: x screen coordinate that represents where the tool is being directed.
	 * @param y: y screen coordinate that represents where the tool is being directed.
	 * @param wait: Should this tool wait for base cooldowns. No for special tools like built-in airblast/momentum freezing/some enemy attacks
	 */
	public void useToolStart(float delta, Equipment tool, short hitbox, int x, int y, boolean wait) {
		
		//Only register the attempt if the user is not waiting on a tool's delay or cooldown. (or if tool ignores wait)
		if ((shootCdCount < 0 && shootDelayCount < 0) || !wait) {
	
			//account for the tool's use delay.
			shootDelayCount = tool.useDelay;
			
			//Register the tool targeting the input coordinates.
			tool.mouseClicked(delta, state, bodyData, hitbox, x, y, world, camera, rays);
			
			//set the tool that will be executed after delay to input tool.
			usedTool = tool;
		}
	}

	/**
	 * This method is called after a tool is used following the tool's delay.
	 */
	public void useToolEnd() {
			
		//the schmuck will not register another tool usage for the tool's cd
		shootCdCount = usedTool.useCd * (1 - bodyData.getToolCdReduc());
		
		//execute the tool.
		usedTool.execute(state, bodyData, world, camera, rays);
		
		//clear the used tool field.
		usedTool = null;
	}

	/**
	 * This method is called after the user releases the button for a tool. Mostly used by charge weapons that execute when releasing
	 * instead of after pressing.
	 * @param tool: tool to release
	 * @param hitbox: aka filter. Who will be affected by this equipment? Player or enemy or neutral?
	 * @param x: x screen coordinate that represents where the tool is being directed.
	 * @param y: y screen coordinate that represents where the tool is being directed.
	 */
	public void useToolRelease(Equipment tool, short hitbox, int x, int y) {
		tool.release(state, bodyData, world, camera, rays);
	}

	public CharacterData getBodyData() {
		return bodyData;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public float getOrientation() {
		return body.getAngle();
	}

	@Override
	public void setOrientation(float orientation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float vectorToAngle(Vector2 vector) {
		return SteeringUtil.vectorToAngle(vector);
	}

	@Override
	public Vector2 angleToVector(Vector2 outVector, float angle) {
		return SteeringUtil.angleToVector(outVector, angle);
	}

	@Override
	public Location<Vector2> newLocation() {
		System.out.println("newLocation was run? I don't know what this function does but this should never appear.");
		return null;//new Location<Vector2>();
	}
}
