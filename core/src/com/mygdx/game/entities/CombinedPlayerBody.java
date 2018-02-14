package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

public class CombinedPlayerBody extends Entity {

    public CombinedPlayerBody(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w,
			float h, float startX, float startY) {
		super(state, world, camera, rays, w, h, startX, startY);
		// TODO Auto-generated constructor stub
	}

	public Player p1, p2;

    @Override
    public void create() {

    }

    @Override
    public void controller(float delta) {

    }

    @Override
    public void render(SpriteBatch batch) {

    }
}
