package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.equipment.WeaponUtils;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.HitboxFactory;
import com.mygdx.game.util.UserDataTypes;

import box2dLight.RayHandler;

import java.util.UUID;
import static com.mygdx.game.util.Constants.PPM;

public class RocketLauncher extends RangedWeapon {

	private final static String name = "Rocket Launcher";
	private final static int clipSize = 4;
	private final static float shootCd = 0.25f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 0.4f;
	private final static int reloadAmount = 1;
	private final static float baseDamage = 30.0f;
	private final static float recoil = 1.5f;
	private final static float knockback = 0.0f;
	private final static float projectileSpeed = 30.0f;
	private final static int projectileWidth = 75;
	private final static int projectileHeight = 15;
	private final static float lifespan = 3.0f;
	
	private final static int projDura = 1;
	
	private final static int explosionRadius = 300;
	private final static float explosionDamage = 60.0f;
	private final static float explosionKnockback = 10.0f;
	
	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			final World world2 = world;
			final OrthographicCamera camera2 = camera;
			final RayHandler rays2 = rays;
			
			Hitbox proj = new HitboxImage(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity,
                    filter, true, world, camera, rays, user, "torpedo", bulletIDs == null ? null : bulletIDs[0], playerDataNumber) {
				
				@Override
				public void controller(float delta) {
					super.controller(delta);
					if (lifeSpan <= 0) {
						WeaponUtils.explode(state, this.body.getPosition().x * PPM , this.body.getPosition().y * PPM, 
								world2, camera2, rays2, user, explosionRadius, explosionDamage, explosionKnockback, (short)0);
					}
				}
				
			};
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				@Override
				public void onHit(UserData fixB) {
					boolean explode = false;
					if (fixB != null) {
						if (fixB.getType().equals(UserDataTypes.BODY) || fixB.getType().equals(UserDataTypes.WALL)) {
							explode = true;
						}
						fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.RANGED);
					} else {
						explode = true;
					}
					if (explode) {
						WeaponUtils.explode(state, this.hbox.getBody().getPosition().x * PPM , this.hbox.getBody().getPosition().y * PPM, 
								world2, camera2, rays2, user, explosionRadius, explosionDamage, explosionKnockback, (short)0);
						hbox.queueDeletion();
					}
					
				}
			});

            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public RocketLauncher(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
	}

}
