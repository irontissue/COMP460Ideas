package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class DestructibleBlock extends Event {

	private static final String name = "Destructable Object";

	private int hp;
	
	public DestructibleBlock(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
                             int width, int height, int x, int y, int hp) {
		super(state, world, camera, rays, name, width, height, x, y);
		this.hp = hp;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateDestructibleBlockMessage(x, y, width, height, hp, entityID.toString()));
		}
	}

	public DestructibleBlock(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
							 int width, int height, int x, int y, int hp, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, entityID);
		this.hp = hp;
	}

	public void create() {

		this.eventData = new EventData(world, this, UserDataTypes.WALL) {
			
			public void receiveDamage(float basedamage, Vector2 knockback, CharacterData perp, Boolean procEffects, DamageTypes... tags) {
				hp -= basedamage;
				
				if (hp <= 0) {
					event.queueDeletion();
				}

				if (comp460game.serverMode) {
					comp460game.server.server.sendToAllTCP(new Packets.EntityTakeDamage(entityID.toString(), basedamage, perp.getEntity().entityID.toString()));
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 50, 0, false, true, Constants.Filters.BIT_WALL, 
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_WALL),
				(short) 0, false, eventData);
	}

}
