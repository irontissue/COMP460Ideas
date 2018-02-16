package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Sensor extends Event {

	private static final String name = "Sensor";

	boolean oneTime;
	
	public Sensor(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, boolean oneTime) {
		super(state, world, camera, rays, name, width, height, x, y);
		this.oneTime = oneTime;
	}
	
	public void create() {
		this.eventData = new EventData(world, this) {
			public void onTouch(UserData fixB) {
				if (comp460game.serverMode) {
					super.onTouch(fixB);
					event.getConnectedEvent().eventData.onActivate(this);
					
					if (oneTime) {
						event.queueDeletion();
					}
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.BIT_SENSOR, 
				(short) (Constants.BIT_PLAYER),
				(short) 0, true, eventData);
	}
}
