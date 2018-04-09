package com.mygdx.game.entities;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

/**
 * A Steering hitbox has steering behaviour and can home and stuff.
 * Currently used for: Beegun
 * @author Zachary Tu
 *
 */
public class SteeringHitbox extends HitboxImage {

	protected Animation<TextureRegion> projectileSprite;
	private TextureAtlas atlas;
	
	public SteeringHitbox(PlayState state, float x, float y, int width, int height, float lifespan,
			int dura, float rest, Vector2 startVelo, short filter, boolean sensor, World world,
			OrthographicCamera camera, RayHandler rays, Schmuck creator, String spriteId, String id, int playerDataNumber,
			float maxLinSpd, float maxLinAcc, float maxAngSpd, float maxAngAcc, float boundingRad, float decelerationRad) {
		super(state, x, y, width, height, lifespan, dura, rest, startVelo, filter, sensor, world, camera, rays, creator,
				spriteId, true, id, playerDataNumber);
		
		atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.PROJ_1_ATL.toString());
		projectileSprite = new Animation<TextureRegion>(0.05f, atlas.findRegions(spriteId));
		
		this.maxLinearSpeed = maxLinSpd;
		this.maxLinearAcceleration = maxLinAcc;
		this.maxAngularSpeed = maxAngSpd;
		this.maxAngularAcceleration = maxAngAcc;
		
		this.boundingRadius = boundingRad;
		this.decelerationRad = decelerationRad;
		
		this.tagged = false;
		
		this.steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
	}
	
	@Override
	public void controller (float delta) {
		super.controller(delta);
		if (behavior != null) {
			behavior.calculateSteering(steeringOutput);
			applySteering(delta);
		}
	}

	public void setTarget(Entity target) {
		Arrive<Vector2> arriveSB = new Arrive<Vector2>(this, target)
				.setArrivalTolerance(2f)
				.setDecelerationRadius(decelerationRad);
		
		this.setBehavior(arriveSB);
	}
}
