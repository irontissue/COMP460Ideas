package com.mygdx.game.entities;

import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;

import box2dLight.RayHandler;

public class StandardEnemy extends SteeringEnemy {
    public static final int ENTITY_TYPE = Constants.EntityTypes.STANDARD_ENEMY;
    public Vector2 direction;
    
    public static final float moveCd = 0.25f;
    public float moveCdCount = 0;
    
    public static final float aiCd = 0.25f;
    public float aiCdCount = 0;
    
    public static final float moveMag = 0.25f;
	    
	float shortestFraction;
  	Fixture closestFixture;
  	Body target;
  	
  	private enemyState aiState;

	public StandardEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
			float startX, float startY, boolean synced) {
		super(state, world, camera, rays, w, h, startX, startY, synced);
		
		this.aiState = enemyState.ROAMING;

	}

	public StandardEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
						 float startX, float startY, boolean synced, String id) {
		super(state, world, camera, rays, w, h, startX, startY, synced, id);

		this.aiState = enemyState.ROAMING;

	}
	
	/**
	 * Enemy ai goes here. Default enemy behaviour just wanders until seeing player.
	 */
	public void controller(float delta) {
		if (comp460game.serverMode) {
            switch (aiState) {
                case ROAMING:

                    direction = new Vector2(
                            0,
                            0).nor().scl(moveMag);
                    break;
                case CHASING:
                	if (target != null) {
                		Vector3 aim = new Vector3(target.getPosition().x, target.getPosition().y, 0);
 //                       camera.project(aim);

                        useToolStart(delta, weapon, Constants.Filters.ENEMY_HITBOX, (int) aim.x, (int) aim.y, true);

                        super.controller(delta);
                	}
                    

                    break;
                default:
                    break;

            }

            if (moveCdCount < 0) {
                moveCdCount += moveCd;
                switch (aiState) {
                    case ROAMING:
                        push(direction.x, direction.y);
                        break;
                    case CHASING:
                        break;
                }
            }

            if (aiCdCount < 0) {
                aiCdCount += aiCd;
                aiState = enemyState.ROAMING;

                shortestFraction = 1.0f;

                if (getBody().getPosition().x != state.getPlayer().getBody().getPosition().x ||
                        getBody().getPosition().y != state.getPlayer().getBody().getPosition().y) {
                    world.rayCast(new RayCastCallback() {

                        @Override
                        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                            if (fixture.getUserData() == null) {
                                if (fraction < shortestFraction) {
                                    shortestFraction = fraction;
                                    closestFixture = fixture;
                                    return fraction;
                                }
                            } else if (fixture.getUserData() instanceof PlayerData) {
                                if (fraction < shortestFraction) {
                                    shortestFraction = fraction;
                                    closestFixture = fixture;
                                    return fraction;
                                }

                            }
                            return -1.0f;
                        }

                    }, getBody().getPosition(), state.getPlayer().getBody().getPosition());
                    if (closestFixture != null) {
                        if (closestFixture.getUserData() instanceof PlayerData) {
                        	target = state.getPlayer().getBody();
                            aiState = enemyState.CHASING;
                            
                            Arrive<Vector2> arriveSB = new Arrive<Vector2>(this, state.getPlayer())
                    				.setArrivalTolerance(2f)
                    				.setDecelerationRadius(decelerationRad);
                    		
                    		this.setBehavior(arriveSB);
                        }
                    }
                }
                
                shortestFraction = 1.0f;
                
                if (getBody().getPosition().x != state.getPlayer2().getBody().getPosition().x ||
                        getBody().getPosition().y != state.getPlayer2().getBody().getPosition().y) {
                    world.rayCast(new RayCastCallback() {

                        @Override
                        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                            if (fixture.getUserData() == null) {
                                if (fraction < shortestFraction) {
                                    shortestFraction = fraction;
                                    closestFixture = fixture;
                                    return fraction;
                                }
                            } else if (fixture.getUserData() instanceof PlayerData) {
                                if (fraction < shortestFraction) {
                                    shortestFraction = fraction;
                                    closestFixture = fixture;
                                    return fraction;
                                }

                            }
                            return -1.0f;
                        }

                    }, getBody().getPosition(), state.getPlayer2().getBody().getPosition());
                    if (closestFixture != null) {
                        if (closestFixture.getUserData() instanceof PlayerData) {
                        	target = state.getPlayer2().getBody();
                            aiState = enemyState.CHASING;
                            
                            Arrive<Vector2> arriveSB = new Arrive<Vector2>(this, state.getPlayer2())
                    				.setArrivalTolerance(2f)
                    				.setDecelerationRadius(decelerationRad);
                    		
                    		this.setBehavior(arriveSB);
                        }
                    }
                }

            }

            shootCdCount -= delta;
            shootDelayCount -= delta;
            
            //If the delay on using a tool just ended, use thte tool.
            if (shootDelayCount <= 0 && usedTool != null) {
                useToolEnd();
            }

            if (weapon.reloading) {
                weapon.reload(delta);
            }

            moveCdCount -= delta;
            aiCdCount -= delta;
        }

        //Stuff below the if statement should happen both on server/client, i.e. doesn't need to be "synced"
        flashingCount-=delta;
	}

	public enum enemyState {
		CHASING,
		ROAMING
	}
}
