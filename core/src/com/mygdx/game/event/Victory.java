package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Victory extends Event {

	private static final String name = "VICTORY";

	boolean touched = false;
	
	public Victory(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateVictoryMessage(x, y, width, height, entityID.toString()));
		}

        eventSprite = new TextureRegion(new Texture(AssetList.VICTORY.toString()));

        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
	}

	public Victory(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);

		eventSprite = new TextureRegion(new Texture(AssetList.VICTORY.toString()));

		spriteHeight = eventSprite.getRegionHeight();
		spriteWidth = eventSprite.getRegionWidth();
	}
	
	public void create() {


		this.eventData = new EventData(world, this) {
			
			@Override
			public void onActivate(EventData activator) {
				if (comp460game.serverMode) {
					if (!touched) {
						touched = true;
						state.gameOver(true);
					}
				}
			}
			
		};
		
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
}
