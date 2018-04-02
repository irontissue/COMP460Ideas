package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class PoisonVent extends Event {

	private float controllerCount = 0;
	private float dps;
	private CharacterData perp;
	private boolean on;

	private static final String name = "Poison";

	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, 
			int x, int y, float dps, boolean startOn, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreatePoisonVentMessage(x, y, width, height, dps, startOn, entityID.toString()));
		}
	}

	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					  int x, int y, float dps, boolean startOn, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
	}
	
	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, 
			int x, int y, float dps, boolean startOn, float duration, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, duration, synced);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreatePoisonVentMessage(x, y, width, height, dps, startOn, entityID.toString()));
		}
	}
	
	public void create() {

		this.eventData = new EventData(world, this) {
			@Override
			public void onActivate(EventData activator) {
				on = !on;
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	@Override
	public void controller(float delta) {
		if (on) {
			controllerCount+=delta;
			if (controllerCount >= 1/60f) {
				controllerCount = 0;
				
				for (Entity entity : eventData.schmucks) {
					if (entity instanceof Schmuck) {
						((Schmuck)entity).getBodyData().receiveDamage(dps, new Vector2(0, 0), perp, true);
					}
				}
			}
		}
		super.controller(delta);
	}
}
