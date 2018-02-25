package com.mygdx.game.event.userdata;

import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.Event;
import com.mygdx.game.util.UserDataTypes;

public class InteractableEventData extends EventData {

	public InteractableEventData(World world, Event event) {
		super(world, event);
	}
	
	public void onTouch(UserData fixB) {
		if (fixB != null) {	
			if (fixB.getType().equals(UserDataTypes.BODY)) {
				((PlayerData)fixB).player.currentEvent = event;
			}
		}
		super.onTouch(fixB);
	}
	
	public void onRelease(UserData fixB) {
		if (fixB != null) {
			if (fixB.getType().equals(UserDataTypes.BODY)) {
				if (((PlayerData)fixB).player.currentEvent == event) {
					((PlayerData)fixB).player.currentEvent = null;
				}
			}
		}
		super.onRelease(fixB);
	}

	@Override
	public void onInteract(Player p, int playerNumber) {

	}

}
