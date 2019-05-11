package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class SavePoint extends Event {

	private static final String name = "Save Point";

	public SavePoint(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateSavePointMessage(x, y, width, height, entityID.toString()));
		}
	}

	public SavePoint(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					 int x, int y, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
	}
	
	public void create() {
		this.eventData = new InteractableEventData(world, this) {
			public void onInteract(Player p) {
				state.lastSave = this.getEvent();
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	public String getText() {
		return name + " (SPACE TO ACTIVATE)";
	}

}
