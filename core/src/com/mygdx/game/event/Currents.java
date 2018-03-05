package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Currents extends Event {
	
	private Vector2 vec;

	private float controllerCount = 0;
	
	private static final String name = "Conveyor Belt";

	public Currents(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, Vector2 vec, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.vec = vec;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateCurrentsMessage(x, y, width, height, vec, entityID.toString()));
		}
	}

	public Currents(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, Vector2 vec, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.vec = vec;
	}
	
	public void create() {

		this.eventData = new EventData(world, this);
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY),
				(short) 0, true, eventData);
	}
	
	public void controller(float delta) {
		if (comp460game.serverMode) {
			controllerCount += delta;
			if (controllerCount >= 1 / 60f) {
				controllerCount = 0;

				for (Entity entity : eventData.schmucks) {
//				entity.getBody().applyLinearImpulse(vec, entity.getBody().getWorldCenter(), true);
					entity.getBody().setTransform(entity.getBody().getPosition().add(vec.x / 32, vec.y / 32), entity.getBody().getAngle());
				}
			}
		}
		
	}
	
	public String getText() {
		return  name + " " + vec;
	}
	
}
