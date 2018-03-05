package com.mygdx.game.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.Shotgun;
import com.mygdx.game.equipment.ranged.AssaultRifle;
import com.mygdx.game.equipment.ranged.Boomerang;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.equipment.ranged.RocketLauncher;
import com.mygdx.game.event.userdata.InteractableEventData;
import com.mygdx.game.manager.AssetList;
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
			int x, int y, int equipId, boolean synced) {
		super(state, world, camera, rays, name, width, height, x, y, synced);
        setEquip(equipId);
        setSprite(equipId);
		if (comp460game.serverMode) {
			comp460game.server.server.sendToAllTCP(new Packets.CreateEquipPickupMessage(x, y, width, height, equipId, entityID.toString()));
		}
	}

	//To be used on client only
	public EquipPickup(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
					   int x, int y, int equipId, boolean synced, String entityID) {
		super(state, world, camera, rays, name, width, height, x, y, synced, entityID);
        setEquip(equipId);
        setSprite(equipId);
	}

	public void setEquip(int equipID) {
        switch(equipID) {
            case Constants.EquipIDs.GUN:
                this.equip = new Gun(null);
                break;
            case Constants.EquipIDs.SHOTGUN:
                this.equip = new Shotgun(null);
                break;
            case Constants.EquipIDs.ROCKET_LAUNCHER:
                this.equip = new RocketLauncher(null);
                break;
            case Constants.EquipIDs.BOOMERANG:
                this.equip = new Boomerang(null);
                break;
            case Constants.EquipIDs.MACHINE:
                this.equip = new AssaultRifle(null);
                break;
            default:
                this.equip = new Gun(null);
                break;
        }
    }
	public void setSprite(int equipID) {
        TextureAtlas atlas = new TextureAtlas(AssetList.PROJ_1_ATL.toString());
        switch(equipID) {
            case Constants.EquipIDs.GUN:
                eventSprite = new TextureRegion(new Texture(AssetList.GUN.toString()));
                break;
            case Constants.EquipIDs.SHOTGUN:
                eventSprite = new TextureRegion(new Texture(AssetList.SHOTGUN.toString()));
                specialScale = 1f;
                break;
            case Constants.EquipIDs.ROCKET_LAUNCHER:
                eventSprite = atlas.findRegion("torpedo");
                specialScale = 0.5f;
                break;
            case Constants.EquipIDs.BOOMERANG:
                eventSprite = atlas.findRegion("boomerang");
                specialScale = 1f;
                break;
            case Constants.EquipIDs.MACHINE:
                eventSprite = new TextureRegion(new Texture(AssetList.MACHINE.toString()));
                specialScale = 1f;
                break;
            default:
                eventSprite = new TextureRegion(new Texture(AssetList.GUN.toString()));
                specialScale = 1f;
                break;
        }
        spriteHeight = eventSprite.getRegionHeight();
        spriteWidth = eventSprite.getRegionWidth();
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
					comp460game.server.server.sendToAllTCP(new Packets.EventInteractMessage(entityID.toString(), p.entityID.toString(), playerNumber));
                } else {
					if (playerNumber == state.gsm.playerNumber) {
						temp = p.playerData.pickup(equip);
					}
                }
				if (temp == null) {
					queueDeletion();
				} else {
					equip = temp;
                    setSprite(equip.getEquipID());
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
