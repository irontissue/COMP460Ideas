package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class MedpakSpawner extends Event {

private float interval;
	
	private float spawnCount = 0;
	
	private int spawnX, spawnY;
	
	private boolean readyToSpawn = true;
	
	private static final String name = "Medpak Spawner";

	public MedpakSpawner(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, float interval, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.interval = interval;
		this.spawnX = x;
		this.spawnY = y;
	}
	
	public void create() {

		this.eventData = new EventData(world, this);
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	@Override
	public void controller(float delta) {
		if (readyToSpawn) {
			spawnCount += delta;
		}
		if (spawnCount >= interval) {
			spawnCount = 0;
			
			if (readyToSpawn) {
				readyToSpawn = false;
				new Medpak(state, world, camera, rays, spawnX, spawnY, this, false);
			}
		}
	}

	public void setReadyToSpawn(boolean readyToSpawn) {
		this.readyToSpawn = readyToSpawn;
	}
}
