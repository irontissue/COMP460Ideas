package com.mygdx.game.event;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.ParticleEntity;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

import static com.mygdx.game.util.Constants.PPM;

public class PoisonVent extends Event {

	private float controllerCount = 0;
	private float dps;
	private CharacterData perp;
	private boolean on;

	private float currPoisonSpawnTimer = 0f, spawnTimerLimit;

	private static final String name = "Poison";

	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, 
			int x, int y, float dps, boolean startOn, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreatePoisonVentMessage(x, y, width, height, dps, startOn, entityID.toString()));
		}
		spawnTimerLimit = 4096f/(width * height);
	}

	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					  int x, int y, float dps, boolean startOn, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
		Log.info("poison vent width = "  + width + " height = " + height + ". Will spawn " + (4096f/(width * height)) + " pps.");
		spawnTimerLimit = 4096f/(width * height);
	}
	
	public PoisonVent(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, 
			int x, int y, float dps, boolean startOn, float duration, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, duration, synced);
		this.dps = dps;
		this.perp = state.worldDummy.getBodyData();
		this.on = startOn;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreatePoisonVentMessage(x, y, width, height, dps, startOn, entityID.toString()));
		}
		spawnTimerLimit = 4096f/(width * height);
	}
	
	public void create() {

		this.eventData = new EventData(world, this) {
			@Override
			public void onActivate(EventData activator) {
				on = !on;
				if (comp460game.serverMode) {
					comp460game.server.server.sendToAllTCP(new Packets.EventActivateMessage(entityID.toString(), activator.getEvent().entityID.toString()));
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY),
				(short) 0, true, eventData);
	}
	
	@Override
	public void controller(float delta) {
		if (on) {
			controllerCount+=delta;
			if (controllerCount >= 1/60f) {
				controllerCount = 0;

				if (comp460game.serverMode) {
					for (Entity entity : eventData.schmucks) {
						if (entity instanceof Schmuck) {
							((Schmuck) entity).getBodyData().receiveDamage(dps, new Vector2(0, 0), perp, true);
						}
					}
				}
			}
			if (!comp460game.serverMode) {
				currPoisonSpawnTimer += delta;
				while (currPoisonSpawnTimer >= spawnTimerLimit) {
					currPoisonSpawnTimer -= spawnTimerLimit;
					int randX = (int) ((Math.random() * width * specialScale) - (width * specialScale / 2) + body.getPosition().x * PPM);
					int randY = (int) ((Math.random() * height * specialScale) - (height * specialScale / 2) + body.getPosition().y * PPM);
					new ParticleEntity(state, world, camera, rays, randX, randY, AssetList.POISON.toString(), 0.0f, 1.5f, true, false);
				}
			}
		}
		super.controller(delta);
	}

	/*@Override
	public void render(SpriteBatch batch) {
		if (eventSprite != null) {
			batch.setProjectionMatrix(state.sprite.combined);
			Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);

			if (on) {
				batch.draw(eventSprite,
						body.getPosition().x * PPM - width * specialScale / 2,
						body.getPosition().y * PPM - height * specialScale / 2,
						width * specialScale / 2, height * specialScale / 2,
						width * scale * specialScale, height * scale * specialScale, 1, 1,
						(float) Math.toDegrees(body.getAngle()) - 180 - specialAngle);
			}

			batch.setColor(Color.WHITE);
		} else {
			batch.setProjectionMatrix(state.hud.combined);
			Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
			camera.project(bodyScreenPosition);
			comp460game.SYSTEM_FONT_UI.getData().setScale(0.4f);
			comp460game.SYSTEM_FONT_UI.draw(batch, getText(), bodyScreenPosition.x, bodyScreenPosition.y);
		}
	}*/
}
