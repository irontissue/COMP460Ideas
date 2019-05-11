package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.event.PoisonVent;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.HitboxFactory;
import com.mygdx.game.util.UserDataTypes;

import box2dLight.RayHandler;

import java.util.UUID;
import static com.mygdx.game.util.Constants.PPM;

public class PoisonGun extends RangedWeapon {

	private final static String name = "Poison Gun";
	private final static int clipSize = 3;
	private final static float shootCd = 0.45f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 1.0f;
	private final static int reloadAmount = 1;
	private final static float baseDamage = 30.0f;
	private final static float recoil = 0.0f;
	private final static float knockback = 1.0f;
	private final static float projectileSpeed = 25.0f;
	private final static int projectileWidth = 45;
	private final static int projectileHeight = 45;
	private final static float lifespan = 3.0f;
	
	private final static int projDura = 1;

	private final static int poisonRadius = 250;
	private final static float poisonDamage = 40/60f;
	private final static float poisonDuration = 5.0f;
	
	public static final int equipID = Constants.EquipIDs.POISON_GUN;

	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				final World world, final OrthographicCamera camera, final RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			Hitbox proj = new HitboxImage(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity,
                    filter, true, world, camera, rays, user, "debris_c", false, bulletIDs == null ? null : bulletIDs[0], playerDataNumber);
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
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
						new PoisonVent(state, world, camera, rays, poisonRadius, poisonRadius,
								(int)(this.hbox.getBody().getPosition().x * PPM), 
								(int)(this.hbox.getBody().getPosition().y * PPM), poisonDamage, true, poisonDuration, false);
						hbox.queueDeletion();
					}

                    if (comp460game.serverMode) {
                        comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_POISON.toString(), 0.7f));
//					    if (fixB != null && fixB.getEntity() instanceof Player) {
//                            Log.info("Poison damaged a player...");
//					        if (fixB.getEntity().entityID == state.brideID) {
//                                Log.info("Player was bride!");
//                                comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BRIDE_OUCH.toString(), 0.7f));
//                            } else if (fixB.getEntity().entityID == state.groomID) {
//                                Log.info("Player was groom!");
//                                comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_GROOM_OUCH.toString(), 0.7f));
//                            }
//                        } else {
//                            comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_POISON.toString(), 0.7f));
//                        }
                    }
//                    Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_POISON.toString()));
//                    sound.play(0.7f);
				}
			});


            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public PoisonGun(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
        setEquipID(equipID);
	}

}
