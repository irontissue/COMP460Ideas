package com.mygdx.game.event.userdata;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.Event;
import com.mygdx.game.util.UserDataTypes;

public class EventData extends UserData {

	protected Event event;
	
	public Set<Entity> schmucks;

	public EventData(World world, Event event) {
		super(world, UserDataTypes.EVENT, event);
		this.event = event;
		this.schmucks = new HashSet<Entity>();
	}
	
	public EventData(World world, Event event, UserDataTypes type) {
		super(world, type, event);
		this.event = event;
		this.schmucks = new HashSet<Entity>();
	}

	public void onTouch(UserData fixB) {
		if (fixB != null) {	
			schmucks.add(fixB.getEntity());
		}
	}
	
	public void onRelease(UserData fixB) {
		if (fixB != null) {
			schmucks.remove(fixB.getEntity());
		}
	}

	/**
	 * The things to happen when this event is interacted with by the player (or in the future, perhaps any entity?)
	 * @param p The player to interact with
	 * @param playerNumber The player number (p1 or p2) - this is useful for server only
	 */
	public void onInteract(Player p, int playerNumber) {
		
	}
	
	public void onActivate(EventData activator) {
		
	}
	
	public Event getEvent() {
		return event;
	}

	
}
