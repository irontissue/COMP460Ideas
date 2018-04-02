package com.mygdx.game.entities;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class SteeringEnemy extends Enemy {
    public static final int ENTITY_TYPE = Constants.EntityTypes.STEERING_ENEMY;
	
	public SteeringEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
			float startX, float startY, boolean synced) {
		super(state, world, camera, rays, w, h, startX, startY, synced);

		this.maxLinearSpeed = 10;
		this.maxLinearAcceleration = 75;
		this.maxAngularSpeed = 6;
		this.maxAngularAcceleration = 3;
		
		this.boundingRadius = 75;
		this.decelerationRad = 50;
		
		this.tagged = false;
		
		this.steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
		
	}

	public SteeringEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
						 float startX, float startY, boolean synced, String id) {
		super(state, world, camera, rays, w, h, startX, startY, synced, id);

		this.maxLinearSpeed = 10;
		this.maxLinearAcceleration = 75;
		this.maxAngularSpeed = 6;
		this.maxAngularAcceleration = 3;

		this.boundingRadius = 75;
		this.decelerationRad = 50;

		this.tagged = false;

		this.steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());

	}
	
	public void create() {
		this.bodyData = new CharacterData(world, this);
		this.body = BodyBuilder.createBox(world, startX, startY, (int)( width / 0.15), (int)(height / 0.15), 0, 1, 0f, false, true, Constants.Filters.BIT_ENEMY, 
				(short) (Constants.Filters.BIT_WALL | Constants.Filters.BIT_SENSOR | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY),
				Constants.Filters.ENEMY_HITBOX, false, bodyData);
		
		Arrive<Vector2> arriveSB = new Arrive<Vector2>(this, state.getPlayer())
				.setArrivalTolerance(2f)
				.setDecelerationRadius(decelerationRad);
		
		this.setBehavior(arriveSB);
	}

    /**
     * Server mode: sends a message to the clients updating the position. This is here because super() isn't called,
     * which normally would automatically send the message.
     */
	public void controller (float delta) {
		if (comp460game.serverMode) {
            if (behavior != null) {
                behavior.calculateSteering(steeringOutput);
                applySteering(delta);
                if (synced) {
					comp460game.server.server.sendToAllTCP(new Packets.SyncEntity(entityID.toString(), this.body.getPosition(),
							this.body.getLinearVelocity(), this.body.getAngularVelocity(), this.body.getAngle()));
				}
            }
        }
	}
}
