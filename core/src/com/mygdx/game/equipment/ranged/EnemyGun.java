package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.HitboxFactory;

import box2dLight.RayHandler;

public class EnemyGun extends RangedWeapon {

	private final static String name = "Enemy Gun";
	private final static int clipSize = 6;
	private final static float shootCd = 1.2f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 0.5f;
	private final static int reloadAmount = 6;
	private final static float baseDamage = 30.0f;
	private final static float recoil = 0.0f;
	private final static float knockback = 1.5f;
	private final static float projectileSpeed = 10.0f;
	private final static int projectileWidth = 15;
	private final static int projectileHeight = 15;
	private final static float lifespan = 5.5f;
	
	private final static int projDura = 2;
	
	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs) {
			
			Hitbox proj = new Hitbox(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity,
					filter, true, world, camera, rays, user, bulletIDs == null ? null : bulletIDs[0]);
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				public void onHit(UserData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.TESTTYPE1);
					}
					super.onHit(fixB);
				}
			});

			Hitbox[] toReturn = {proj};
			return toReturn;
		}
		
	};
	
	public EnemyGun(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
	}

}
