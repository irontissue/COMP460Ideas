package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;
import com.mygdx.game.util.Constants;

public class RangedHitbox extends Hitbox {
    public static final int ENTITY_TYPE = Constants.EntityTypes.RANGED_HITBOX;
	public RangedHitbox(PlayState state, float x, float y, int width, int height, float lifespan, int dura,
			float rest, Vector2 startVelo, short filter, boolean sensor, World world, OrthographicCamera camera,
			RayHandler rays, Schmuck creator, boolean synced) {
		super(state, x, y, 
				(int) (width * (1 + creator.getBodyData().getProjectileSize())), 
				(int) (height * (1 + creator.getBodyData().getProjectileSize())), 
				lifespan * (1 + creator.getBodyData().getProjectileLifespan()),
				(int) (dura + creator.getBodyData().getProjectileDurability()), 
				rest + creator.getBodyData().getProjectileBounciness(), 
				startVelo.scl(1 + creator.getBodyData().getProjectileSpeed()), filter, sensor, world, camera, rays, creator, synced);
	}

	public RangedHitbox(PlayState state, float x, float y, int width, int height, float lifespan, int dura,
						float rest, Vector2 startVelo, short filter, boolean sensor, World world, OrthographicCamera camera,
						RayHandler rays, Schmuck creator, boolean synced, String id) {
		super(state, x, y,
				(int) (width * (1 + creator.getBodyData().getProjectileSize())),
				(int) (height * (1 + creator.getBodyData().getProjectileSize())),
				lifespan * (1 + creator.getBodyData().getProjectileLifespan()),
				(int) (dura + creator.getBodyData().getProjectileDurability()),
				rest + creator.getBodyData().getProjectileBounciness(),
				startVelo.scl(1 + creator.getBodyData().getProjectileSpeed()), filter, sensor, world, camera, rays, creator, synced, id);
	}

}
