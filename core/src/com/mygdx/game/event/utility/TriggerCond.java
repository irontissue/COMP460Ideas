package com.mygdx.game.event.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.event.Event;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

/**
 * A Conditional trigger is a multitrigger that triggers one of the triggers in its triggered group.
 * Which event this is is decided by the "condition" string which can be set by triggering this with an alt-trigger.
 * 
 * @author Zachary Tu
 *
 */
public class TriggerCond extends Event {

	private static final String name = "CondTrigger";

	private Map<String, Event> triggered = new HashMap<String, Event>();
	private String condition;
	Random generator = new Random();
	
	public TriggerCond(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, String start) {
		super(state, world, camera, rays, name, width, height, x, y, false);
		this.condition = start;
	}
	
	@Override
	public void create() {
		this.eventData = new EventData(world, this) {
			
			@Override
			public void onActivate(EventData activator) {
				if (activator.getEvent() instanceof TriggerAlt) {
					condition = ((TriggerAlt)activator.getEvent()).getMessage();
				} else {
					if (condition.equals("random")) {
						Object[] values = triggered.values().toArray();
						((Event)values[generator.nextInt(values.length)]).eventData.onActivate(this);
						
					} else {
						if (triggered.get(condition) != null) {
							triggered.get(condition).eventData.onActivate(this);
						}
					}	
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) 0, (short) 0, true, eventData);
	}
	
	public void addTrigger(String s, Event e) {
		triggered.put(s, e);
	}
}
