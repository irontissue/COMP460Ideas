package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.HitboxFactory;

import box2dLight.RayHandler;

import java.util.UUID;

public class AssaultRifle extends RangedWeapon {

	private final static String name = "Assault Rifle";
	private final static int clipSize = 25;
	private final static float shootCd = 0.05f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 0.75f;
	private final static int reloadAmount = 25;
	private final static float baseDamage = 25.0f;
	private final static float recoil = 0.25f;
	private final static float knockback = 0.05f;
	private final static float projectileSpeed = 40.0f;
	private final static int projectileWidth = 64;
	private final static int projectileHeight = 8;
	private final static float lifespan = 1.2f;
	
	private final static int projDura = 1;

	public static final int equipID = Constants.EquipIDs.MACHINE;

	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			Hitbox proj = new HitboxImage(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity,
                    filter, true, world, camera, rays, user, "bullet", false, bulletIDs == null ? null : bulletIDs[0], playerDataNumber);
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				public void onHit(UserData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.TESTTYPE1);
					}
					super.onHit(fixB);
				}
			});

			Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_AR.toString()));
			sound.play(1.0f);
            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public AssaultRifle(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
        setEquipID(equipID);
	}

}
