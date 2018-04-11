package com.mygdx.game.entities.userdata;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Player;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.UserDataTypes;

/**
 * This is the basic user data of any entity. The only thing this needs to do is keep track of type and basic stuff like that.
 * @author Zachary Tu
 *
 */
public class UserData {

	//the number of other entities or walls this entity is touching. Atm only used for feet to determine groundedness
	private int numContacts;
	
	//Enum that describes the type of entity. This field is often checked on contact.
	private UserDataTypes type;
	
	//The entity that owns this data
	private Entity entity;
	
	/**
	 * aye
	 * @param world
	 * @param type
	 * @param entity
	 */
	public UserData(World world, UserDataTypes type, Entity entity) {
		this.type = type;
		this.entity = entity;
		this.numContacts = 0;
	}

	/**
	 * This method is called when this entity gets hit. Most non-body entities will only care about the kb.
	 * @param basedamage: amount of damage received
	 * @param knockback: amount of knockback to apply.
	 * @param perp: the schmuck who did this
	 * @param procEffects: Should this proc status effects?
	 * @param tags: damage tags
	 *TODO: include the source of damage
	 */
	public void receiveDamage(float basedamage, Vector2 knockback, CharacterData perp, Boolean procEffects, DamageTypes... tags) {

		Log.info("Something damaged a player..." + entity.entityID.toString());
		if (entity.entityID == comp460game.client.playerUUIDs[1]) {
			Log.info("Player was bride!");
			comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BRIDE_OUCH.toString(), 0.7f));
		} else if (entity.entityID == comp460game.client.playerUUIDs[0]) {
			Log.info("Player was groom!");
			comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_GROOM_OUCH.toString(), 0.7f));
		}

		entity.push(knockback.x, knockback.y);
	}
	
	public int getNumContacts() {
		return numContacts;
	}

	public void setNumContacts(int numContacts) {
		this.numContacts = numContacts;
	}

	public UserDataTypes getType() {
		return type;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
}