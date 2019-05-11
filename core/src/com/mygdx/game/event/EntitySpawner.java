package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Enemy;
import com.mygdx.game.entities.StandardEnemy;
import com.mygdx.game.entities.SteeringEnemy;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;
import static com.mygdx.game.util.Constants.PPM;

public class EntitySpawner extends Event {

	public int id;
	public float interval;
	public int limit;
	
	public float spawnCount = 0;
	public int amountCount = 0;
	
	public int spawnX, spawnY;
	
	private static final String name = "Schmuck Spawner";
	
	public EntitySpawner(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, int schmuckId, float interval, int limit, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
		this.id = schmuckId;
		this.interval = interval;
		this.limit = limit;
		this.spawnX = x;
		this.spawnY = y;
	}
	
	public void create() {

		this.eventData = new EventData(world, this);
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR,
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY | Constants.Filters.BIT_PROJECTILE),
				(short) 0, true, eventData);
	}
	
	public void controller(float delta) {

		spawnCount += delta;
		if (spawnCount >= interval && (limit == 0 || amountCount < limit)) {
			spawnCount = 0;
			amountCount++;
			//ENEMY ID LOOKUP TABLE HERE!!!!!!!!!!!!!
			switch(id) {
                case 0:
                    state.player.getBody().setTransform(
                            spawnX / PPM + state.getPlayer().width / PPM / 2,
                            spawnY / PPM + state.getPlayer().height / PPM / 2, 0);
//                    state.player2.getBody().setTransform(
//                            spawnX / PPM + state.getPlayer().width / PPM / 2,
//                            spawnY / PPM + state.getPlayer().height / PPM / 2 * 4, 0);
                    break;
				case -1:
					state.player2.getBody().setTransform(
							spawnX / PPM + state.getPlayer().width / PPM / 2,
							spawnY / PPM + state.getPlayer().height / PPM / 2, 0);
					break;
                case 1:
                    Enemy e = new Enemy(state, world, camera, rays, 50, 50, spawnX, spawnY, true);
                    Log.info("Created enemy on server.");
                    if (comp460game.serverMode) {
                        comp460game.server.server.sendToAllTCP(new Packets.SyncCreateSchmuck(e.entityID.toString(), 24,24, spawnX, spawnY, Constants.EntityTypes.ENEMY, true, 0));
                    }
                    break;
                case 2:
                    StandardEnemy s = new StandardEnemy(state, world, camera, rays, 24, 24, spawnX, spawnY, true);
                    Log.info("Created standard enemy on server.");
                    if (comp460game.serverMode) {
                        comp460game.server.server.sendToAllTCP(new Packets.SyncCreateSchmuck(s.entityID.toString(), 24, 24, spawnX, spawnY, Constants.EntityTypes.STANDARD_ENEMY, true, 0));
                    }
                    break;
                case 3:
                    SteeringEnemy q = new SteeringEnemy(state, world, camera, rays, 24, 24, spawnX, spawnY, true);
                    Log.info("Created steering enemy on server.");
                    if (comp460game.serverMode) {
                        comp460game.server.server.sendToAllTCP(new Packets.SyncCreateSchmuck(q.entityID.toString(), 24, 24, spawnX, spawnY, Constants.EntityTypes.STEERING_ENEMY, true, 0));
                    }
                    break;
            }
		}
	}

}
