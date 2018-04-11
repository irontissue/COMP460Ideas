package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.HitboxFactory;

import box2dLight.RayHandler;

import java.util.UUID;

public class BouncingBlade extends RangedWeapon {

	private final static String name = "Bouncing Blade";
	private final static int clipSize = 5;
	private final static float shootCd = 0.60f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 1.75f;
	private final static int reloadAmount = 5;
	private final static float baseDamage = 20.0f;
	private final static float recoil = 1.5f;
	private final static float knockback = 7.5f;
	private final static float projectileSpeed = 25.0f;
	private final static int projectileWidth = 75;
	private final static int projectileHeight = 75;
	private final static float lifespan = 5.0f;
	
	private final static int projDura = 1;

	public static final int equipID = Constants.EquipIDs.GUN;

	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			Hitbox proj = new HitboxImage(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 1.2f, startVelocity,
                    filter, false, world, camera, rays, user, "bouncing_blade", true, bulletIDs == null ? null : bulletIDs[0], playerDataNumber);
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				public void onHit(UserData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.TESTTYPE1);
					}
					hbox.particle.onForBurst(0.25f);
                    if (comp460game.serverMode) {
                        comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BB_CUT.toString(), 0.3f));
                    }
				}
			});

            if (comp460game.serverMode) {
                comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BB.toString(), 1.0f));
            }
//			Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BB.toString()));
//			sound.play(1.0f);
            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public BouncingBlade(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
        setEquipID(equipID);
	}

}
