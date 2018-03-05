package com.mygdx.game.event;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

import static com.mygdx.game.util.Constants.PPM;

/**
 * An Event is an entity that acts as a catch-all for all misc entities that do not share qualities with schmucks or hitboxes.
 * Events include hp/fuel/weapon pickups, currents, schmuck spawners, springs, literally anything else.
 * @author Zachary Tu
 *
 */
public class Event extends Entity {
	
	//The event's data
	public EventData eventData;
	
	//The event's name
	public String name;
	
	private Event connectedEvent;

	public TextureRegion eventSprite;
    public int spriteWidth = -197;
    public int spriteHeight = -174;

    public static float scale = 1f;

	//This is used by consumable events to avoid being activated multiple times before next engine tick.
	protected boolean consumed = false;
		
	public Event(PlayState state, World world, OrthographicCamera camera, RayHandler rays, String name,
			int width, int height, int x, int y) {
		super(state, world, camera, rays, width, height, x, y);
		this.name = name;
	}

	public Event(PlayState state, World world, OrthographicCamera camera, RayHandler rays, String name,
				 int width, int height, int x, int y, String uuid) {
		super(state, world, camera, rays, width, height, x, y, uuid);
		this.name = name;
	}
	
	@Override
	public void create() {

	}

	@Override
	public void controller(float delta) {
		
	}

	/**
	 * Tentatively, we want to display the event's name information next to the event
	 */
	@Override
	public void render(SpriteBatch batch) {
        if (eventSprite != null) {
            batch.setProjectionMatrix(state.sprite.combined);
            Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
            batch.draw(eventSprite,
                    body.getPosition().x * PPM - spriteWidth / 2,
                    body.getPosition().y * PPM - spriteHeight / 2,
                    spriteWidth / 2, spriteHeight / 2,
                    spriteWidth * scale, spriteHeight * scale, 1, 1,
                    (float) Math.toDegrees(body.getAngle()) - 180);

            batch.setColor(Color.WHITE);
        } else {
            batch.setProjectionMatrix(state.hud.combined);
            Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
            camera.project(bodyScreenPosition);
            state.font.draw(batch, getText(), bodyScreenPosition.x, bodyScreenPosition.y);
        }
	}
	
	public String getText() {
		return name;
	}

	public Event getConnectedEvent() {
		return connectedEvent;
	}

	public void setConnectedEvent(Event connectedEvent) {
		this.connectedEvent = connectedEvent;
	}
}
