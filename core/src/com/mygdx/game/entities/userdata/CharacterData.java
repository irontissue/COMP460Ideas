package com.mygdx.game.entities.userdata;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.status.Status;
import com.mygdx.game.util.UserDataTypes;

public class CharacterData extends UserData {

	//The Character that owns this data
	public Schmuck schmuck;
	
	/**
	 * Stats:
	 * 0: Max Hp
	 * 1: Hp Regeneration per second
	 * 2: Linear Speed Modification
	 * 3: Angular Speed Modification
	 * 4: Universal Damage Amplification
	 * 5: Universal Damage Reduction
	 * 6: Universal Knockback on Hit (to others)
	 * 7: Universal Knockback Resistance (to self)
	 * 8: Universal Tool-Use Speed
	 * 9: Ranged Damage on Hit
	 * 10: Ranged Reload Rate
	 * 11: Ranged Clip Size
	 * 12: Ranged Projectile Speed
	 * 13: Ranged Projectile Size
	 * 14: Ranged Projectile Lifespan
	 * 15: Ranged Projectile Durability
	 * 16: Ranged Projectile Bounciness
	 * 17: Ranged Recoil
	 *
	 * 
	 * 
	 * 
	 */
	
	public float[] baseStats;
	public float[] buffedStats;	
		
	//Speed on ground
	public float maxSpeed = 7.0f;
	public float maxAngularSpeed = 1.5f;

	//Hp and regen
	public int maxHp = 100;
	public float currentHp = 100;
	public float hpRegen = 0.0f;
	
	public ArrayList<Status> statuses;
	public ArrayList<Status> statusesChecked;
	
	private final static float flashDuration = 0.08f;

	/**
	 * This is created upon the create() method of any schmuck.
	 * Character are the Body data type.
	 * @param world
	 * @param schmuck
	 */
	public CharacterData(World world, Schmuck schmuck) {
		super(world, UserDataTypes.BODY, schmuck);
		this.schmuck = schmuck;		
		
		this.baseStats = new float[52];
		this.buffedStats = new float[52];
		
		baseStats[0] = maxHp;
		baseStats[1] = hpRegen;
		
		this.statuses = new ArrayList<Status>();
		this.statusesChecked = new ArrayList<Status>();
		
		calcStats();

		currentHp = getMaxHp();
	}
	
	public float statusProcTime(int procTime, CharacterData schmuck, float amount, Status status, DamageTypes... tags) {
		float finalAmount = amount;
		ArrayList<Status> oldChecked = new ArrayList<Status>();
		for(Status s : this.statusesChecked){
			this.statuses.add(0,s);
			oldChecked.add(s);
		}
		this.statusesChecked.clear();
		
		while(!this.statuses.isEmpty()) {
			Status tempStatus = this.statuses.get(0);
			switch(procTime) {
			case 0:
				tempStatus.statChanges(this);
				break;
			case 1:
				finalAmount = tempStatus.onDealDamage(finalAmount, schmuck, tags);
				break;
			case 2:
				finalAmount = tempStatus.onReceiveDamage(finalAmount, schmuck, tags);
				break;
			case 3:
				tempStatus.timePassing(amount);
				break;
			case 4:
				tempStatus.onKill(schmuck);
				break;
			case 5:
				tempStatus.onDeath(schmuck);
				break;
			}
			
			if(this.statuses.contains(tempStatus)){
				this.statuses.remove(tempStatus);
				this.statusesChecked.add(tempStatus);
			}
		}
		
		for(Status s : this.statusesChecked){
			if(!oldChecked.contains(s)){
				this.statuses.add(s);
			}
		}
		this.statusesChecked.clear();
		for(Status s : oldChecked){
			this.statusesChecked.add(s);
		}
		return finalAmount;
				
	}
	
	public void addStatus(Status s) {
		statuses.add(s);
		calcStats();
	}
	
	public void removeStatus(Status s) {
		statuses.remove(s);
		statusesChecked.remove(s);
		calcStats();
	}
	
	public void calcStats() {
		
		//Keep Hp% constant in case of changing max hp
		float hpPercent = currentHp / getMaxHp();
		
		for (int i = 0; i < buffedStats.length; i++) {
			buffedStats[i] = baseStats[i];
		}
		statusProcTime(0, this, 0, null);
		
		currentHp = hpPercent * getMaxHp();
	}
	
	/**
	 * This method is called when this schmuck receives damage.
	 * @param basedamage: amount of damage received
	 * @param knockback: amount of knockback to apply.
	 */
	@Override
	public void receiveDamage(float basedamage, Vector2 knockback, CharacterData perp, Boolean procEffects, DamageTypes... tags) {
		
		float damage = basedamage;
		
		damage -= basedamage * (getDamageReduc());
		damage += basedamage * (perp.getDamageAmp());
		
		if (Arrays.asList(tags).contains(DamageTypes.RANGED)) {
			damage *= (1 + perp.getBonusRangedDamage());
		}
		
		if (procEffects) {
			damage = perp.statusProcTime(1, perp, damage, null);
			damage = statusProcTime(2, this, damage, null);
		}
		
		currentHp -= damage;
		
		//Make shmuck flash upon receiving damage
		if (damage > 0 && schmuck.flashingCount < -flashDuration) {
			schmuck.flashingCount = flashDuration;
		}
				
		float kbScale = 1;
		
		kbScale -= getKnockbackReduc();
		kbScale += perp.getKnockbackAmp();
		
		schmuck.getBody().applyLinearImpulse(knockback.scl(kbScale), schmuck.getBody().getWorldCenter(), true);
		if (currentHp <= 0) {
			currentHp = 0;
			die(perp);
		}
	}
	
	/**
	 * This method is called when the schmuck is healed
	 * @param heal: amount of Hp to regenerate
	 */
	public void regainHp(float heal) {
		currentHp += heal;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
	}
	
	/**
	 * This method is called when the schmuck dies. Queue up to be deleted next engine tick.
	 */
	public void die(CharacterData perp) {
		
		perp.statusProcTime(4, perp, 0, null);
		statusProcTime(5, this, 0, null);
		
		schmuck.queueDeletion();
	}

	public Schmuck getSchmuck() {
		return schmuck;
	}
	
	public float getMaxHp() {
		return buffedStats[0];
	}
	
	public void setMaxHp(float buff) {
		float Hp = currentHp / buffedStats[0];
		buffedStats[0] = buff;
		currentHp = Hp * buff;
	}
	
	public float getHpRegen() {
		return buffedStats[1];
	}
	
	public void setHpRegen(float buff) {
		buffedStats[1] = buff;
	}
	
	public float getBonusLinSpeed() {
		return buffedStats[2];
	}
	
	public void setBonusLinSpeed(float buff) {
		buffedStats[2] = buff;
	}
	
	public float getBonusAngSpeed() {
		return buffedStats[3];
	}
	
	public void setBonusAngSpeed(float buff) {
		buffedStats[3] = buff;
	}
	
	public float getDamageAmp() {
		return buffedStats[4];
	}
	
	public void setDamageAmp(float buff) {
		buffedStats[4] = buff;
	}
	
	public float getDamageReduc() {
		return buffedStats[5];
	}
	
	public void setDamageReduc(float buff) {
		buffedStats[5] = buff;
	}
	
	public float getKnockbackAmp() {
		return buffedStats[6];
	}
	
	public void setKnockbackAmp(float buff) {
		buffedStats[6] = buff;
	}
	
	public float getKnockbackReduc() {
		return buffedStats[7];
	}
	
	public void setKnockbackReduc(float buff) {
		buffedStats[7] = buff;
	}
	
	public float getToolCdReduc() {
		return buffedStats[8];
	}
	
	public void setToolCdReduc(float buff) {
		buffedStats[8] = buff;
	}
	
	public float getBonusRangedDamage() {
		return buffedStats[9];
	}
	
	public void setBonusRangedDamage(float buff) {
		buffedStats[9] = buff;
	}
	
	public float getReloadRate() {
		return buffedStats[10];
	}
	
	public void setReloadRate(float buff) {
		buffedStats[10] = buff;
	}
	
	public float getBonusClipSize() {
		return buffedStats[11];
	}
	
	public void setBonusClipSize(float buff) {
		buffedStats[11] = buff;
	}
	
	public float getProjectileSpeed() {
		return buffedStats[12];
	}
	
	public void setProjectileSpeed(float buff) {
		buffedStats[12] = buff;
	}
	
	public float getProjectileSize() {
		return buffedStats[13];
	}
	
	public void setProjectileSize(float buff) {
		buffedStats[13] = buff;
	}
	
	public float getProjectileLifespan() {
		return buffedStats[14];
	}
	
	public void setProjectileLifespan(float buff) {
		buffedStats[14] = buff;
	}
	
	public float getProjectileDurability() {
		return buffedStats[15];
	}
	
	public void setProjectileDurability(float buff) {
		buffedStats[15] = buff;
	}
	
	public float getProjectileBounciness() {
		return buffedStats[16];
	}
	
	public void setProjectileBounciness(float buff) {
		buffedStats[16] = buff;
	}
	
	public float getBonusRecoil() {
		return buffedStats[17];
	}
	
	public void setBonusRecoil(float buff) {
		buffedStats[17] = buff;
	}
}
