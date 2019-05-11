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

/**
 * A radio is a simple event that when interacted with will put up a test dialogue actor into the play stage
 * @author Zachary Tu
 *
 */
public class Radio extends Event {

	private static final String name = "Dialogue";

	private String id;
	
	public Radio(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, String id) {
		super(state, world, camera, rays, name, width, height, x, y, false);
		this.id = id;
		
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateDialogMessage(x, y, width, height, id, entityID.toString()));
		}
	}
	
	public Radio(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, String id, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, false, entityID);
		this.id = id;
	}
	
	@Override
	public void create() {

		this.eventData = new EventData(world, this) {
			
			@Override
			public void onActivate(EventData activator) {
				if (event.getConnectedEvent() != null) {
					state.stage.addDialogue(id, this, event.getConnectedEvent().eventData);
				} else {
					state.stage.addDialogue(id, this, null);
				}
				
				if (comp460game.serverMode) {
                    comp460game.server.server.sendToAllTCP(new Packets.EventActivateMessage(entityID.toString(), activator.getEvent().entityID.toString()));
                }
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),	(short) 0, true, eventData);
	}
	
	@Override
	public String getText() {
		if (eventData.schmucks.isEmpty()) {
			return "RADIO";
		} else {
			return "RADIO (E TO LISTEN)";
		}
	}

}
