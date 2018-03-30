package com.mygdx.game.event.utility;


import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.event.Event;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Timer extends Event {
	
	//How frequently will the spawns occur? Every interval seconds.
	private float interval;
	
	//The event will spawn limit entites before stopping. If this is 0, the event will never stop.
	private int limit;
	
	private float timeCount = 0;
	private int amountCount = 0;
	private boolean on;
	
	private static final String name = "Timer";
	
	public Timer(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, float interval, int limit, boolean startOn, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.interval = interval;
		this.limit = limit;
		this.on = startOn;
	}
	
	public void create() {

		this.eventData = new EventData(world, this) {
				
			@Override
			public void onActivate(EventData activator) {
				((Timer)event).on = !((Timer)event).on;
				amountCount = 0;
				timeCount = 0;
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY | Constants.Filters.BIT_PROJECTILE),
				(short) 0, true, eventData);
	}
	
	public void controller(float delta) {		
		if (on) {
			timeCount += delta;
			if (timeCount >= interval) {
				timeCount = 0;
				amountCount++;
				System.out.println("Timer should proc");
				if (getConnectedEvent() != null) {
					System.out.println("Timer activating event " + eventData.getEvent().name);
					getConnectedEvent().eventData.onActivate(eventData);
				}
			}
			if ((limit != 0 && amountCount >= limit)) {
				on = false;
			}
		}
		
	}
}
