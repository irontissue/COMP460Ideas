package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.AnotherGun;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class EquipPickup extends Event {

	private Equipment equip;
	
	public static final int numWeapons = 9;
	
	private static final String name = "Equip Pickup";

	public EquipPickup(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, int equipId) {
		super(state, world, camera, rays, name, width, height, x, y);
		switch(equipId) {
		case 0:
			this.equip = new Gun(null);
			break;
		case 1:
			this.equip = new AnotherGun(null);
			break;
		default:
			this.equip = new Gun(null);
			break;
		}
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateEquipPickupMessage(x, y, width, height, equipId, entityID.toString()));
		}
	}

	//To be used on client only
	public EquipPickup(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					   int x, int y, int equipId, String uuid) {
		super(state, world, camera, rays, name, width, height, x, y, uuid);
		switch(equipId) {
			case 0:
				this.equip = new Gun(null);
				break;
			case 1:
				this.equip = new AnotherGun(null);
				break;
			default:
				this.equip = new Gun(null);
				break;
		}
	}
	
	public void create() {
		this.eventData = new InteractableEventData(world, this) {
			public void onInteract(Player p, int playerNumber) {
                Equipment temp = null;
				if (comp460game.serverMode) {
				    if (playerNumber == 1) {
                        temp = p.player1Data.pickup(equip);
                    } else {
                        temp = p.player2Data.pickup(equip);
                    }
                } else {
					if (playerNumber == state.gsm.playerNumber) {
						temp = p.playerData.pickup(equip);
					}
                }
				if (temp == null) {
					queueDeletion();
				} else {
					equip = temp;
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.Filters.BIT_SENSOR, 
				(short) (Constants.Filters.BIT_PLAYER),
				(short) 0, true, eventData);
	}
	
	public String getText() {
		return equip.name + " (SPACE TO TAKE)";
	}

}
