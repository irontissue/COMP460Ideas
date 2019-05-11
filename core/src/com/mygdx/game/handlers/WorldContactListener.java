package com.mygdx.game.handlers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.util.UserDataTypes;

/**
 * This listener keeps track of whenever 2 fixtures in the box2d world collide.
 * @author Zachary Tu
 *
 */
public class WorldContactListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		UserData fixA = (UserData) contact.getFixtureA().getUserData();
		UserData fixB = (UserData) contact.getFixtureB().getUserData();
		
		//When 2 fixtures collide, increment their number of contacts.
		//Projectiles and events should register hits.
		if (fixA != null) {
			fixA.setNumContacts(fixA.getNumContacts() + 1);
			if (fixA.getType().equals(UserDataTypes.HITBOX)) {
				if (comp460game.serverMode)
				    ((HitboxData) fixA).onHit(fixB);
			}
			if (fixA.getType().equals(UserDataTypes.EVENT)) {
				((EventData) fixA).onTouch(fixB);
			}
			
		}
		if (fixB != null) {
			fixB.setNumContacts(fixB.getNumContacts() + 1);
			if (fixB.getType().equals(UserDataTypes.HITBOX)) {
                if (comp460game.serverMode)
                    ((HitboxData) fixB).onHit(fixA);
			}
			if (fixB.getType().equals(UserDataTypes.EVENT)) {
				((EventData) fixB).onTouch(fixA);
			}
		}
		
	}

	@Override
	public void endContact(Contact contact) {
		UserData fixA = (UserData) contact.getFixtureA().getUserData();
		UserData fixB = (UserData) contact.getFixtureB().getUserData();
		
		if (fixA != null) {
			fixA.setNumContacts(fixA.getNumContacts() - 1);
			if (fixA.getType().equals(UserDataTypes.EVENT)) {
				((EventData) fixA).onRelease(fixB);
			}
		}
		if (fixB != null) {
			fixB.setNumContacts(fixB.getNumContacts() - 1);
			if (fixB.getType().equals(UserDataTypes.EVENT)) {
				((EventData) fixB).onRelease(fixA);
			}
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

}
