package com.mygdx.game.equipment.ranged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.*;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.status.DamageTypes;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.HitboxFactory;
import static com.mygdx.game.util.Constants.PPM;

import box2dLight.RayHandler;
import com.mygdx.game.util.UserDataTypes;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Beehive extends RangedWeapon {

	private final static String name = "Beehive";
	private final static int clipSize = 24;
	private final static float shootCd = 0.15f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 1.75f;
	private final static int reloadAmount = 24;
	private final static float baseDamage = 12.0f;
	private final static float recoil = 0.0f;
	private final static float knockback = 0.5f;
	private final static float projectileSpeedStart = 3.0f;
	private final static int projectileWidth = 23;
	private final static int projectileHeight = 21;
	private final static float lifespan = 5.0f;
	private final static float homeRadius = 10;
	
	private final static int projDura = 1;
	
	private final static int spread = 45;
	
	private final static String projSpriteId = "bee";
	
	protected Animation<TextureRegion> projectileSprite;
	
	private static final float maxLinearSpeed = 100;
	private static final float maxLinearAcceleration = 1000;
	private static final float maxAngularSpeed = 180;
	private static final float maxAngularAcceleration = 90;
	
	private static final int boundingRadius = 500;
	private static final int decelerationRadius = 0;
	
	public static final int equipID = Constants.EquipIDs.BEEHIVE;

	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox[] makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera, RayHandler rays, String[] bulletIDs, int playerDataNumber) {

			float newDegrees = (float) (startVelocity.angle() + (ThreadLocalRandom.current().nextInt(-spread, spread + 1)));
			
			final SteeringHitbox proj = new SteeringHitbox(state, x, y, projectileWidth, projectileHeight, lifespan, projDura, 0, startVelocity.setAngle(newDegrees),
					filter, false, world, camera, rays, user, projSpriteId, bulletIDs == null ? null : bulletIDs[0], playerDataNumber,
					maxLinearSpeed, maxLinearAcceleration, maxAngularSpeed, maxAngularAcceleration, boundingRadius, decelerationRadius) {
				
				private Schmuck homing;
				private Schmuck homeAttempt;
				private Fixture closestFixture;
				
				private float shortestFraction = 1.0f;
				
				@Override
				public void controller(float delta) {
					super.controller(delta);
					increaseAnimationTime(delta);
					if (homing != null && homing.alive) {
						if (behavior != null) {
							behavior.calculateSteering(steeringOutput);
							applySteering(delta);
						}
					} else {
						world.QueryAABB(new QueryCallback() {

							@Override
							public boolean reportFixture(Fixture fixture) {
								if (fixture.getUserData() instanceof CharacterData && !(fixture.getUserData() instanceof PlayerData)) {
									homeAttempt = ((CharacterData)fixture.getUserData()).getSchmuck();
									shortestFraction = 1.0f;
									
								  	if (body.getPosition().x != homeAttempt.getPosition().x || 
								  			body.getPosition().y != homeAttempt.getPosition().y) {
										world.rayCast(new RayCastCallback() {

											@Override
											public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
												if (fixture.getUserData() == null) {
													if (fraction < shortestFraction) {
														shortestFraction = fraction;
														closestFixture = fixture;
														return fraction;
													}
												} else if (fixture.getUserData() instanceof CharacterData) {
										//			if (((CharacterData)fixture.getUserData()).getSchmuck(). != filter) {
														if (fraction < shortestFraction) {
															shortestFraction = fraction;
															closestFixture = fixture;
															return fraction;
														}
										//			}
												} 
												return -1.0f;
											}
											
										}, getBody().getPosition(), homeAttempt.getPosition());	
										
										if (closestFixture != null) {
											if (closestFixture.getUserData() instanceof CharacterData) {
	
												homing = ((CharacterData)closestFixture.getUserData()).getSchmuck();
												setTarget(homing);
											}
										}	
									}		
								}
								return true;
							}
							
						}, 
						body.getPosition().x - homeRadius, body.getPosition().y - homeRadius, 
						body.getPosition().x + homeRadius, body.getPosition().y + homeRadius);
					}
				}
				
				@Override
				public void render(SpriteBatch batch) {
				
					boolean flip = false;
					
					if (body.getAngle() < 0) {
						flip = true;
					}
					
					batch.setProjectionMatrix(state.sprite.combined);

					batch.draw((TextureRegion) projectileSprite.getKeyFrame(animationTime, true), 
							body.getPosition().x * PPM - width / 2, 
							(flip ? height : 0) + body.getPosition().y * PPM - height / 2, 
							width / 2, 
							(flip ? -1 : 1) * height / 2,
							width, (flip ? -1 : 1) * height, 1, 1, 
							(float) Math.toDegrees(body.getAngle()) - 90);

				}
			};
			
			proj.setUserData(new HitboxData(state, world, proj) {
				
				@Override
				public void onHit(UserData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage, hbox.getBody().getLinearVelocity().nor().scl(knockback), 
								user.getBodyData(), true, DamageTypes.RANGED);
						super.onHit(fixB);
                        if (comp460game.serverMode) {
                            if (fixB.getEntity() instanceof Schmuck) {
                                comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BEE_YOW.toString(), 0.1f));
//                                Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BEE_GDI.toString()));
//                                sound.play(0.7f);
                            }
                        }
//						int a = (int) (Math.random()*2);
//						if (fixB.getEntity() instanceof Player) {
//                            if (a == 1) {
//                                Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BEE_YOW.toString()));
//                                sound.play(0.8f);
//                            } else {
//                                Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BEE_GDI.toString()));
//                                sound.play(0.8f);
//                            }
//                        }
					}
				}
			});

			if (comp460game.serverMode) {
			    comp460game.server.server.sendToAllTCP(new Packets.PlaySound(AssetList.SFX_BEE.toString(), 0.1f));
//                Sound sound = Gdx.audio.newSound(Gdx.files.internal(AssetList.SFX_BEE.toString()));
//                sound.play(0.5f);
            }

            Hitbox[] toReturn = {proj};
            return toReturn;
		}
		
	};
	
	public Beehive(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeedStart, shootCd, shootDelay, reloadAmount, onShoot);
        setEquipID(equipID);
	}

}
