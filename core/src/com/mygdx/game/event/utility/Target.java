package com.mygdx.game.event.utility;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.Event;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Target extends Event {

	private static final String name = "Target";

	boolean oneTime;
	
	public Target(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, boolean oneTime, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.oneTime = oneTime;

		eventSprite = new TextureRegion(new Texture(AssetList.TARGET.toString()));

		spriteHeight = eventSprite.getRegionHeight();
		spriteWidth = eventSprite.getRegionWidth();
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateTargetMessage(x, y, width, height, oneTime, entityID.toString()));
		}
	}

	public Target(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
				  int x, int y, boolean oneTime, boolean synced, String id) {
		super(state, world, camera, rays, name, width, height, x, y, synced, id);
		this.oneTime = oneTime;

		eventSprite = new TextureRegion(new Texture(AssetList.TARGET.toString()));

		spriteHeight = eventSprite.getRegionHeight();
		spriteWidth = eventSprite.getRegionWidth();
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
