package com.mygdx.game.equipment;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.HitboxFactory;
import com.mygdx.game.util.SteeringUtil;

import static com.mygdx.game.util.Constants.PPM;

import box2dLight.RayHandler;

import com.mygdx.game.entities.userdata.CharacterData;

import java.util.UUID;

/**
 * Ranged Weapons are weapons used by clicking somewhere on the screen to probably fire a projcetile or whatever in that direction.
 * Ranged weapons have a clip size and can be reloaded.
 * @author Za chary Tu
 *
 */
public class RangedWeapon extends Equipment {

	public int clipSize;
	public int clipLeft;
	public int reloadAmount;
	public float recoil;
	public float projectileSpeed;
	public HitboxFactory onShoot;
	
	public Vector2 velo;
	public int x, y;
	public short faction;

	/**
	 * Ranged weapons, like most equipment, is constructed when creating tool spawns or default schmuck loadouts
	 * @param user: Schmuck that is using this tool.
	 * @param name: Name of the weapon
	 * @param clipSize: Amount of times the weapon can be fired before reloading
	 * @param reloadTime: The time in seconds it takes to reload this weapon once.
	 * @param recoil: The amount of force pushing the playerNumber upon firing.
	 * @param projectileSpeed: The initial velocity of hitboxes created by this weapon.
	 * @param shootCd: The delay after using this tool before you can use a tool again.
	 * @param shootDelay: The delay between pressing the button for this tool and it activating. 
	 * @param reloadAmount: The amount of clip restored upon one reload
	 * @param onShoot: This is a factory that creates a hitbox
	 */
	public RangedWeapon(Schmuck user, String name, int clipSize, float reloadTime, float recoil, 
			float projectileSpeed, float shootCd, float shootDelay, int reloadAmount, HitboxFactory onShoot) {
		super(user, name, shootCd, shootDelay);
		this.clipSize = clipSize;
		this.clipLeft = clipSize;
		this.reloadTime = reloadTime;
		this.reloadAmount = reloadAmount;
		this.recoil = recoil;
		this.projectileSpeed = projectileSpeed;
		this.onShoot = onShoot;
	}

	/**
	 * This method is called when a schmuck targets a point with this weapon.
	 * The weapon is not fired yet. Instead, a vector keeping track of the target is set.
	 */
	@Override
	public void mouseClicked(float delta, PlayState state, CharacterData shooter, short faction, int x, int y, World world, OrthographicCamera camera, RayHandler rays) {
		
		//Convert screen coordinates into a starting velocity for the projectile.
		Vector3 bodyScreenPosition = new Vector3(
				shooter.getSchmuck().getBody().getPosition().x,
				shooter.getSchmuck().getBody().getPosition().y, 0);
		camera.project(bodyScreenPosition);
		
		float powerDiv = bodyScreenPosition.dst(x, y, 0) / projectileSpeed;
		
		float xImpulse = -(bodyScreenPosition.x - x) / powerDiv;
		float yImpulse = -(bodyScreenPosition.y - y) / powerDiv;
		this.velo = new Vector2(xImpulse, yImpulse);

		//Also store the recoil vector and filter.
		this.faction = faction;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * This is run after the weapon's shootDelay to actually fire.
	 * Here, the stored velo, recoil, filter are used to generate a projectile
	 */
	@Override
	public String[] execute(PlayState state, CharacterData shooter, World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDS) {

		String[] returnIDS = null;
		//Check clip size. empty clip = reload instead. This makes reloading automatic.
		if (clipLeft > 0) {
			
			float bodyAngle = shooter.getEntity().getBody().getAngle() * MathUtils.radiansToDegrees;
			float shootAngle = SteeringUtil.vectorToAngle(velo) * MathUtils.radiansToDegrees - 90;
			
			float phi = Math.abs(bodyAngle - shootAngle) % 360;
	        float distance = phi > 180 ? 360 - phi : phi;
	        
//			if (distance <= 60) {
				//Generate the hitbox(s). This method's return is unused, so it may not return a hitbox or whatever at all.
				//This code determines which player is shooting, if any at all.

				Hitbox[] h = onShoot.makeHitbox(user, state, velo,
						shooter.getSchmuck().getBody().getPosition().x * PPM, 
						shooter.getSchmuck().getBody().getPosition().y * PPM, 
						faction, world, camera, rays, bulletIDS, shooter.playerNumber);
				returnIDS = new String[h.length];
				for (int i = 0; i < h.length; i++) {
				    returnIDS[i] = h[i].entityID.toString();
                }
				clipLeft--;
				if (comp460game.serverMode) {
					comp460game.server.server.sendToAllTCP(new Packets.PlayerShoot(shooter.playerNumber));
				}
				
				//If playerNumber fires in the middle of reloading, reset reload progress
				reloading = false;
				reloadCd = reloadTime * (1 - shooter.getReloadRate());
				
				//process weapon recoil.
				user.recoil(x, y, recoil * (1 + shooter.getBonusRecoil()));
//			}
			checkReload();
		}

		return returnIDS;
	}

	/**
	 * Checks if this weapon needs to reload, and starts the reload sequence. This is in a separate function
	 * because KryoClient uses this exact same code, on receiving a SyncHitboxImage packet.
	 */
	public void checkReload() {
		if (clipLeft <= 0) {
			if (!reloading) {
				reloading = true;
				reloadCd = reloadTime;
			}
		}
	}

	/**
	 * Default behaviour for releasing mouse is nothing.
	 * Override this in charge weapons or other weapons that care about mouse release.
	 */
	@Override
	public void release(PlayState state, CharacterData bodyData, World world, OrthographicCamera camera, RayHandler rays) {}
	
	/**
	 * This method is run every engine tick when reloading.
	 */
	public void reload(float delta) {
		
		//Keep track of how long schmuck has been reloading. If done, get more ammo.
		if (reloadCd > 0) {
			reloadCd -= delta;
		} else {
			clipLeft += reloadAmount;
			reloadCd = reloadTime;

			//If clip is full, finish reloading.
			if (clipLeft >= getClipSize()) {
				clipLeft = getClipSize();
				reloading = false;
			}
		}
	}

	public int getClipSize() {
		
		if (clipSize * user.getBodyData().getBonusClipSize() > 0 && clipSize * user.getBodyData().getBonusClipSize() < 1) {
			return clipSize + 1;
		} else {
			return (int) (clipSize * (1 + user.getBodyData().getBonusClipSize()));
		}
	}

	/**
	 * Return name + clip + reload status
	 */
	@Override
	public String getText() {
		if (reloading) {
			return clipLeft + "/" + getClipSize() + " RELOADING";
		} else {
			return clipLeft + "/" + getClipSize();

		}
	}
	
	/**
	 * helper method for gaining ammo. Not currently used, but could be useful for stuff that gives you free reloads
	 * @param gained: amount of ammo to gain.
	 */
	public void gainAmmo(int gained) {
		clipLeft += gained;
		if (clipLeft >= getClipSize()) {
			clipLeft = getClipSize();
		}
	}
}