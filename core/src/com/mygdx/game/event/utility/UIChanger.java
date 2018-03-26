package com.mygdx.game.event.utility;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.actors.UITag;
import com.mygdx.game.actors.UITag.uiType;
import com.mygdx.game.event.Event;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

/**
 * A UIChanger changes the UI. specifically, the UILevel (name tentative) actor to display different information or change 
 * some extra, non-score field like lives.
 * @author Zachary Tu
 *
 */
public class UIChanger extends Event {

	private static final String name = "UI Changer";

	private ArrayList<UITag> tags;
	private int changeType, scoreIncr;
	private float timerIncr;
	private String miscTag;
	
	public UIChanger(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, String types, int changeType, int scoreIncr, float timerIncr, String misc) {
		super(state, world, camera, rays, name, width, height, x, y, false);
		this.changeType = changeType;
		this.scoreIncr = scoreIncr;
		this.timerIncr = timerIncr;
		this.miscTag = misc;
		this.tags = new ArrayList<UITag>();
		if (types != null) {
			for (String type : types.split(",")) {
				uiType newType = uiType.valueOf(type);
				
				UITag newTag = new UITag(newType);
				
				if (newType.equals(uiType.MISC)) {
					newTag.setMisc(miscTag);
				}
				
				this.tags.add(newTag);
			}
		}
		
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateUIChangerMessage(x, y, width, height, types, changeType,scoreIncr, timerIncr, misc, entityID.toString()));
		}
	}
	
	public UIChanger(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, String types, int changeType, int scoreIncr, float timerIncr, String misc, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, false, entityID);
		this.changeType = changeType;
		this.scoreIncr = scoreIncr;
		this.timerIncr = timerIncr;
		this.miscTag = misc;
		this.tags = new ArrayList<UITag>();
		if (types != null) {
			for (String type : types.split(",")) {
				uiType newType = uiType.valueOf(type);
				
				UITag newTag = new UITag(newType);
				
				if (newType.equals(uiType.MISC)) {
					newTag.setMisc(miscTag);
				}
				
				this.tags.add(newTag);
			}
		}
	}
	
	@Override
	public void create() {
		this.eventData = new EventData(world, this) {
			
			@Override
			public void onActivate(EventData activator) {
				state.uiLevel.changeTypes(changeType, tags);
				state.uiLevel.incrementScore(scoreIncr);
				state.uiLevel.incrementTimer(timerIncr);
				
				if (comp460game.serverMode) {
                    comp460game.server.server.sendToAllTCP(new Packets.EventActivateMessage(entityID.toString(), activator.getEvent().entityID.toString()));
                }
			}
			
			
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) 0, (short) 0, true, eventData);
	}
}
