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
import com.mygdx.game.util.b2d.FixtureBuilder;

import box2dLight.RayHandler;

import java.util.UUID;
import static com.mygdx.game.util.Constants.PPM;

public class Boomerang extends RangedWeapon {

	private final static String name = "Boomerang";
	private final static int clipSize = 1;
	private final static float shootCd = 0.25f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 0.75f;
	private final static int reloadAmount = 6;
	private final static float baseDamage = 50.0f;
	private final static float recoil = 1.5f;
	private final static float knockback = 6.0f;
	private final static float projectileSpeed = 30.0f;
	private final static int projectileWidth = 60;
	private final static int projectileHeight = 57;
	private final static float lifespan = 4.0f;
	
	private final static int projDura = 1;

	public static final int equipID = Constants.EquipIDs.BOOMERANG;

	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			Hitbox proj = new HitboxImage(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity,
                    filter, true, world, camera, rays, user, "boomerang", true, bulletIDs == null ? null : bulletIDs[0], playerDataNumber) {
				
				float controllerCount = 0;
				
				@Override
				public void create() {
					super.create();
					body.setAngularVelocity(5);
					getBody().createFixture(FixtureBuilder.createFixtureDef(projectileWidth / 2, projectileHeight / 2, 
							new Vector2(0,  0), false, 0, Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL), Constants.Filters.PLAYER_HITBOX));
				}
				
				@Override
				public void controller(float delta) {
					controllerCount+=delta;
					if (controllerCount >= 1/60f) {
						Vector2 diff = new Vector2(user.getBody().getPosition().x * PPM - body.getPosition().x * PPM, 
								user.getBody().getPosition().y * PPM - body.getPosition().y * PPM);
						body.applyForceToCenter(diff.nor().scl(projectileSpeed * body.getMass() * 1.5f), true);
						controllerCount = 0;
					}
					super.controller(delta);
				}
				
			};
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				public void onHit(UserData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.TESTTYPE1);
					}
					super.onHit(fixB);
                    if (comp460game.serverMode  && fixB!= null && fixB.getEntity() instanceof Schmuck) {
                        comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BOOMERANG_WHACK.toString(), 0.7f));
                    }
				}
			});

            if (comp460game.serverMode) {
                comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BOOMERANG.toString(), 1.0f));
            }
//			Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BOOMERANG.toString()));
//			sound.play(1.0f);

            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public Boomerang(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
        setEquipID(equipID);
	}

}
