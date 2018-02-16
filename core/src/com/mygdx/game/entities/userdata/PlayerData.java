package com.mygdx.game.entities.userdata;

import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Player;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.AnotherGun;
import com.mygdx.game.equipment.ranged.Gun;

public class PlayerData extends CharacterData {

	public int itemSlots = 4;
	public Equipment[] multitools;
	public int currentSlot = 0;
	public int lastSlot = 0;
	public Equipment currentTool;
	
	public Player player;
	
	public PlayerData(World world, Player body) {
		super(world, body);
		this.player = body;
		multitools = new Equipment[itemSlots];
		multitools[0] = new Gun(body);
		multitools[1] = new AnotherGun(body);
		this.currentTool = multitools[currentSlot];
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
}
