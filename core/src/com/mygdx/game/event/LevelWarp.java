package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.manager.GameStateManager;
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
				//Log.info("Interacted with level warp, level = " + level);
				if (comp460game.serverMode) {
					comp460game.server.server.sendToAllTCP(new Packets.EventInteractMessage(entityID.toString(),
							p.entityID.toString(), p.playerData.playerNumber));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					state.loadLevel("maps/" + level);
                    //Log.info("Levelwarp id = " + entityID.toString());
                } else {
					//Log.info("setting fadedelta on client (levelwarp interacated)");
					state.fadeDelta = 0.05f;
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	@Override
	public void render(SpriteBatch batch) {
        super.render(batch);
        batch.setProjectionMatrix(state.hud.combined);
        Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
        camera.project(bodyScreenPosition);
        comp460game.SYSTEM_FONT_UI.getData().setScale(0.4f);
		comp460game.SYSTEM_FONT_UI.draw(batch, getText(), bodyScreenPosition.x - 50, bodyScreenPosition.y + 30);
        
	}
	
	@Override
	public String getText() {
		return GameStateManager.levelnames.getOrDefault(level, "");
	}
}
