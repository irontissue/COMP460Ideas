package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.BadGun;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Enemy extends Schmuck {
    public static final int ENTITY_TYPE = Constants.EntityTypes.ENEMY;
	public Equipment weapon;
	
	public Enemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
			float startX, float startY) {
		super(state, world, camera, rays, w, h, startX, startY);
		weapon = new BadGun(this);
	}

	public Enemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
				 float startX, float startY, String id) {
		super(state, world, camera, rays, w, h, startX, startY, id);
		weapon = new BadGun(this);
	}

	/**
	 * Create the enemy's body and initialize player's user data.
	 */
	public void create() {
		this.bodyData = new CharacterData(world, this);
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, true, Constants.Filters.BIT_ENEMY, 
				(short) (Constants.Filters.BIT_WALL | Constants.Filters.BIT_SENSOR | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY),
				Constants.Filters.ENEMY_HITBOX, false, bodyData);
	}
	
	@Override
	public void controller(float delta) {
		super.controller(delta);
	}
	
	/**
	 * Deletes enemy. Currently also increments game score.
	 */
	public void dispose() {
		state.incrementScore(1);
		super.dispose();
	}
	
	
}
