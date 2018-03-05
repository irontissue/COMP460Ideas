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

public class InfoFlag extends Event {

	private static final String name = "Info Flag";

	private String text;
	
	public InfoFlag(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, String text) {
		super(state, world, camera, rays, name, width, height, x, y);
		this.text = text;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateInfoFlagMessage(x, y, width, height, text, entityID.toString()));
		}
	}

	public InfoFlag(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, String text, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, entityID);
		this.text = text;
	}
	
	public void create() {

		this.eventData = new EventData(world, this);
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	public String getText() {
		if (eventData.schmucks.isEmpty()) {
			return "";
		} else {
			return text;
		}
	}

}
