package com.mygdx.game.event;


import java.util.ArrayList;
import static com.mygdx.game.util.Constants.PPM;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

/**
 * This is a platform that continuously moves towards its connected event.
 * @author Zachary Tu
 *
 */
public class MovingPlatform extends Event {

	private static final String name = "Moving Platform";

	private float speed;
	
	private ArrayList<Event> connected = new ArrayList<Event>();
	
	public MovingPlatform(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
			int width, int height, int x, int y, float speed) {
		super(state, world, camera, rays, name, width, height, x, y, true);
		this.speed = speed;
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateMovingPlatformMessage(x, y, width, height, speed, entityID.toString()));
		}
		eventSprite = new TextureRegion(new Texture(AssetList.DOOR.toString()));
	}
	
	public MovingPlatform(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
			int width, int height, int x, int y, float speed, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, true, entityID);
		this.speed = speed;
		
		eventSprite = new TextureRegion(new Texture(AssetList.DOOR.toString()));
	}

	@Override
	public void create() {

		this.eventData = new EventData(world, this, UserDataTypes.WALL) {
			
			@Override
			public void onActivate(EventData activator) {
				event.setConnectedEvent(activator.getEvent());
			}

		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, true, Constants.Filters.BIT_WALL, 
				(short) (Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_SENSOR),
				(short) 0, false, eventData);
		
		this.body.setType(BodyDef.BodyType.KinematicBody);
	}
	
	@Override
	public void controller(float delta) {
		super.controller(delta);
		if (comp460game.serverMode) {
			if (getConnectedEvent() != null) {
				Vector2 dist = getConnectedEvent().getBody().getPosition().sub(body.getPosition()).scl(PPM);
	
				if ((int)dist.len2() <= 1) {
					if (getConnectedEvent().getConnectedEvent() == null) {
						body.setLinearVelocity(0, 0);
						for (Event e : connected) {
							if (e.getBody() != null && e.alive) {
								e.getBody().setLinearVelocity(0, 0);
							}
						}
					} else {
						body.setTransform(getConnectedEvent().getBody().getPosition(), 0);
						setConnectedEvent(getConnectedEvent().getConnectedEvent());
					}
				} else {
					body.setLinearVelocity(dist.nor().scl(speed));
					for (Event e : connected) {
						if (e.getBody() != null && e.alive) {
							e.getBody().setLinearVelocity(dist.nor().scl(speed));
						}
					}
				}
			} else {
				for (Event e : connected) {
					if (e.getBody() != null && e.alive) {
						e.getBody().setLinearVelocity(0, 0);
					}
				}
	
			}
		}
	}
	
	public void addConnection(Event e) {
		if (e != null) {
			connected.add(e);
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
        if (eventSprite != null) {
            batch.setProjectionMatrix(state.sprite.combined);
            Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
            batch.draw(eventSprite,
                    body.getPosition().x * PPM - width*specialScale / 2,
                    body.getPosition().y * PPM - height*specialScale / 2,
                    width*specialScale / 2, height*specialScale / 2,
                    width * scale * specialScale, height * scale * specialScale, 1, 1,
                    (float) Math.toDegrees(body.getAngle()) - 180 - specialAngle);

            batch.setColor(Color.WHITE);
        } else {
            batch.setProjectionMatrix(state.hud.combined);
            Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
            camera.project(bodyScreenPosition);
            comp460game.SYSTEM_FONT_UI.getData().setScale(0.4f);
			comp460game.SYSTEM_FONT_UI.draw(batch, getText(), bodyScreenPosition.x, bodyScreenPosition.y);
        }
	}

}
