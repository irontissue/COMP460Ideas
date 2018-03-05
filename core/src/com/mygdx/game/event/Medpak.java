package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Medpak extends Event {

	private static final int width = 16;
	private static final int height = 16;
	
	private static final int hpRegained = 25;

	private MedpakSpawner spawner;
	
	private static final String name = "Medpak";

	public Medpak(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y,
			MedpakSpawner medpakSpawner, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.spawner = medpakSpawner;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateMedpakMessage(x, y, width, height, medpakSpawner.entityID.toString(), entityID.toString()));
		}
	}

	public Medpak(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y,
				  MedpakSpawner medpakSpawner, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
		this.spawner = medpakSpawner;
	}
	
	public void create() {

		this.eventData = new EventData(world, this) {
			@Override
			public void onTouch(UserData fixB) {
				if (fixB != null && !consumed) {
					if (fixB.getType().equals(UserDataTypes.BODY)) {
						if (((PlayerData)fixB).currentHp < ((PlayerData)fixB).getMaxHp()) {
							((PlayerData)fixB).regainHp(hpRegained);
							if (spawner != null) {
								spawner.setReadyToSpawn(true);
							}
							queueDeletion();
						}
					}
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
}
