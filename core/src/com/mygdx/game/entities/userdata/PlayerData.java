package com.mygdx.game.entities.userdata;

import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.AnotherGun;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.equipment.ranged.RocketLauncher;
import com.mygdx.game.server.Packets;

public class PlayerData extends CharacterData {

	private int itemSlots = 4;
    private Equipment[] multitools;

    private int currentSlot = 0;
    private int lastSlot = 0;
    private Equipment currentTool;
	
	public Player player;
	
	public PlayerData(World world, Player body) {
		super(world, body);
		this.player = body;
		multitools = new Equipment[itemSlots];
		multitools[0] = new Gun(body);
		multitools[1] = new AnotherGun(body);
		multitools[2] = new RocketLauncher(body);
		this.currentTool = multitools[currentSlot];
	}
	
	public void copyData(PlayerData old) {
		currentHp = old.currentHp;
		//TODO: copy other things that will be carried over across levels. Statuses/Loadout
	}

	public void switchWeapon(int slot) {
		if (multitools.length >= slot && schmuck.shootDelayCount <= 0) {
			if (multitools[slot - 1] != null) {
				lastSlot = currentSlot;
				currentSlot = slot - 1;
				currentTool = multitools[currentSlot];
			}
		}
	}
	
	public void switchToLast() {
		if (schmuck.shootDelayCount <= 0) {
			int tempSlot = lastSlot;
			lastSlot = currentSlot;
			currentSlot = tempSlot;
			currentTool = multitools[currentSlot];
		}
	}
	
	public Equipment pickup(Equipment equip) {
		Log.info("Picked up an equip");
		for (int i = 0; i < itemSlots; i++) {
			if (multitools[i] == null) {
				multitools[i] = equip;
				multitools[i].user = player;
				currentSlot = i;
				currentTool = multitools[currentSlot];
				return null;
			}
		}
		
		Equipment old = multitools[currentSlot];
		
		multitools[currentSlot] = equip;
		multitools[currentSlot].user = player;
		currentTool = multitools[currentSlot];
		
		return old;
	}
	
	@Override
	public void die(CharacterData perp) {
		schmuck.getState().gameOver(false);
		super.die(perp);
	}

    public int getItemSlots() {
        return itemSlots;
    }

    public Equipment[] getMultitools() {
        return multitools;
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public int getLastSlot() {
        return lastSlot;
    }

    public Equipment getCurrentTool() {
        return currentTool;
    }
}
