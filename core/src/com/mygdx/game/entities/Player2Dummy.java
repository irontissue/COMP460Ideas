package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

public class Player2Dummy extends Schmuck {

	Player player;
	
	public Equipment dummyWeapon;
	
	public Player2Dummy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
			float startX, float startY, Player player) {
		super(state, world, camera, rays, w, h, startX, startY);
		this.player = player;
		dummyWeapon = new Gun(this);
	}

	@Override
	public void create() {

	}
	
	public void controller(float delta) {
		super.controller(delta);
		if (dummyWeapon.reloading) {
			dummyWeapon.reload(delta);
		}
	}
	
}
