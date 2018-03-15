package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

/**
 * A Use Portal is a portal that transports the playerNumber elsewhere when they interact with it.
 * The event they are transported to does not have to be a portal.
 * @author Zachary Tu
 *
 */
public class LevelWarp extends Event {

	private static final String name = "Level Warp";

	String level;

	public LevelWarp(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, String level, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.level = level;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateLevelWarpMessage(x, y, width, height, level, entityID.toString()));
		}

        eventSprite = new TextureRegion(new Texture(AssetList.LEVEL_WARP.toString()));

        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
	}

	public LevelWarp(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					 int x, int y, String level, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.level = level;

		eventSprite = new TextureRegion(new Texture(AssetList.LEVEL_WARP.toString()));

        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
	}
	
	@Override
	public void create() {
		this.eventData = new InteractableEventData(world, this) {
			
			@Override
			public void onInteract(Player p) {
				Log.info("Interacted with level warp, level = " + level);
				if (comp460game.serverMode) {
                    state.loadLevel("maps/" + level);
                }
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	@Override
	public String getText() {
		return name + " (SPACE TO ACTIVATE)";
	}

}
