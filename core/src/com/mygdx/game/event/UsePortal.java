package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class UsePortal extends Event {

	private static final String name = "Portal";

	boolean oneTime;

	public UsePortal(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, boolean oneTime, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.oneTime = oneTime;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateUsePortalMessage(x, y, width, height, oneTime, entityID.toString()));
		}
        eventSprite = new TextureRegion(new Texture(AssetList.USE_PORTAL.toString()));
        specialScale = 1f;

        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
	}

	public UsePortal(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					 int x, int y, boolean oneTime, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.oneTime = oneTime;
        eventSprite = new TextureRegion(new Texture(AssetList.USE_PORTAL.toString()));
        specialScale = 1f;

        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
	}
	
	public void create() {
		this.eventData = new InteractableEventData(world, this) {
			public void onInteract(Player p) {
				if (comp460game.serverMode) {
					if (event.getConnectedEvent() != null) {
						p.getBody().setTransform(event.getConnectedEvent().getBody().getPosition(), p.getOrientation());

						if (oneTime) {
							event.queueDeletion();
						}
					}
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	public String getText() {
		return name + " (SPACE TO USE)";
	}

}
