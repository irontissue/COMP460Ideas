package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Target extends Event {

	private static final String name = "Target";

	boolean oneTime;
	
	public Target(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, boolean oneTime) {
		super(state, world, camera, rays, name, width, height, x, y);
		this.oneTime = oneTime;
	}
	
	public void create() {
		this.eventData = new EventData(world, this, UserDataTypes.EVENT) {
			public void onTouch(UserData fixB) {
				super.onTouch(fixB);
				if (event.getConnectedEvent() != null) {
					event.getConnectedEvent().eventData.onActivate(this);
				}
				
				if (oneTime) {
					event.queueDeletion();
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PROJECTILE),
				(short) 0, true, eventData);
	}
}
