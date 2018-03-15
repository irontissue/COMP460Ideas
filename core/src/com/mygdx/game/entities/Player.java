package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.event.Event;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;
import com.mygdx.game.util.b2d.FixtureBuilder;

import box2dLight.ConeLight;
import box2dLight.RayHandler;

import static com.mygdx.game.util.Constants.PPM;

public class Player extends Schmuck {
    public static final int ENTITY_TYPE = Constants.EntityTypes.PLAYER;
	protected MoveStates moveState1, moveState2;

    //Counters that keep track of delay between action initiation + action execution and action execution + next action
    public float shootCdCount;
    public float shootDelayCount;
    //The last used tool. This is used to process equipment with a delay between using and executing.
    public Equipment usedTool;

	//Fixtures and user data
	protected Fixture viewWedge;
    protected Fixture viewWedge2;

	private float lastDelta;
	//is the playerNumber currently in the process of holding their currently used tool?
	private boolean charging = false;
		
	protected float interactCd = 0.15f;
	protected float interactCdCount = 0;

	//is the button for that respective movement pressed currently?
    public boolean wPressed = false, aPressed = false, sPressed = false, dPressed = false, qPressed = false, ePressed = false;
    public boolean mousePressed = false;
    public boolean spacePressed = false;
    public float mousePosX = -1, mousePosY = -1;
		
	//user data
	public PlayerData playerData;
	public Event currentEvent;
	
//	public Player2Dummy dummy;
	public PlayerData old;
	protected Fixture player1Fixture, player2Fixture;

	public ConeLight vision;
	
	private TextureRegion combined, bride, groom, dress;
	
	/**
	 * This constructor is called by the playerNumber spawn event that must be located in each map
	 * @param state: current gameState
	 * @param world: box2d world
	 * @param camera: game camera
	 * @param rays: game rayhandler
	 * @param x: playerNumber starting x position.
	 * @param y: playerNumber starting x position.
	 */
  
	public Player(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y, PlayerData old,
                  int playerNumber, boolean synced) {
		super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384, synced);
		this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
		this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
		this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
		this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
		this.old = old;

        playerData = new PlayerData(world, this);
        playerData.playerNumber = playerNumber;
	}

    /*public Player(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y, boolean synced, String id) {
        super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384, synced, id);
        this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
        this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
        this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
        this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
    }*/
	
	/**
	 * Create the playerNumber's body and initialize playerNumber's user data.
	 */
	public void create() {
        this.bodyData = playerData;

        if (old != null) {
            Log.info("Client copied playerdata.");
            playerData.copyData(old);
        }
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, false, Constants.Filters.BIT_PLAYER, 
				(short) (Constants.Filters.BIT_WALL | Constants.Filters.BIT_SENSOR | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_ENEMY),
				Constants.Filters.PLAYER_HITBOX, false, playerData);

		if (!comp460game.serverMode) {
			if (state.gsm.playerNumber == 1) {
				player1Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(-width / 2 / PPM, 0), true, 0,
						Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL | Constants.Filters.BIT_ENEMY), Constants.Filters.PLAYER_HITBOX));
				player1Fixture.setUserData(playerData);
			} else {
				player2Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(width / 2 / PPM, 0), true, 0,
						Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL | Constants.Filters.BIT_ENEMY), Constants.Filters.PLAYER_HITBOX));
				player2Fixture.setUserData(playerData);
			}
		} else {
			player2Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(- width / 2 / PPM, 0), true, 0,
					Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL | Constants.Filters.BIT_ENEMY), Constants.Filters.PLAYER_HITBOX));
			player2Fixture.setUserData(playerData);
			
			player1Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(width / 2 / PPM, 0), true, 0,
					Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL | Constants.Filters.BIT_ENEMY), Constants.Filters.PLAYER_HITBOX));
			player1Fixture.setUserData(playerData);
		}
				
/*		if (!comp460game.serverMode) {
			vision = new ConeLight(rays, 32, Color.WHITE, 500, 0, 0, 0, 80);
			vision.setIgnoreAttachedBody(true);
			
			if (state.gsm.playerNumber == 1) {
				vision.attachToBody(body,0 ,0, 180);
			} else {
				vision.attachToBody(body,0 ,0, 0);
			}
		} else {
			vision = new ConeLight(rays, 360, Color.WHITE, 500, 0, 0, 0, 90);
			vision.setIgnoreAttachedBody(true);
			vision.attachToBody(body,0 ,0, 180);
			
			
			ConeLight extraVision = new ConeLight(rays, 360, Color.WHITE, 500, 0, 0, 0, 90);
			extraVision.setIgnoreAttachedBody(true);
			extraVision.attachToBody(body,0 ,0, 0);
			extraVision.setContactFilter(Constants.Filters.BIT_SENSOR, Constants.Filters.BIT_WALL, (short)0);
			extraVision.setSoftnessLength(50);

		}
		
		vision.setContactFilter(Constants.Filters.BIT_SENSOR, (short)0, Constants.Filters.BIT_WALL);
		vision.setSoft(true);
		vision.setSoftnessLength(5);*/
		
		super.create();
	}

	@Override
    public void useToolStart(float delta, Equipment tool, short filter, float x, float y, boolean wait) {
        //Log.info("Got into user tool start - player " + pNumber);
        //Only register the attempt if the user is not waiting on a tool's delay or cooldown. (or if tool ignores wait)
        if ((shootCdCount < 0 && shootDelayCount < 0) || !wait) {

            //account for the tool's use delay.
            shootDelayCount = tool.useDelay;

            //Register the tool targeting the input coordinates.
//			if (comp460game.serverMode) {
//			    comp460game.server.server.sendToAllTCP(new Packets.SetEntityAim(entityID.toString(), delta, x, y));
//            }
            tool.mouseClicked(delta, state, playerData, filter, x / 32, y / 32, world, camera, rays);
            usedTool = tool;
        }
    }

    /**
     * This method is called after a tool is used following the tool's delay.
     */
    @Override
    public void useToolEnd() {
        String[] uuids;
        //the schmuck will not register another tool usage for the tool's cd
        shootCdCount = usedTool.useCd * (1 - bodyData.getToolCdReduc());
        //execute the tool.
        uuids = usedTool.execute(state, playerData, world, camera, rays, null);
        //clear the used tool field.
        usedTool = null;
    }
	
	/**
	 * The playerNumber's controller currently polls for input.
	 */
	public void controller(float delta) {

	    lastDelta = delta;

	    if (comp460game.serverMode) {
            desiredYVel = 0;
            desiredXVel = 0;
            desiredAngleVel = 0;

            if (wPressed) {
                desiredYVel += playerData.maxSpeed;
            }
            if (aPressed) {
                desiredXVel += -playerData.maxSpeed;
            }
            if (sPressed) {
                desiredYVel += -playerData.maxSpeed;
            }
            if (dPressed) {
                desiredXVel += playerData.maxSpeed;
            }

            if (ePressed) {
                desiredAngleVel += -playerData.maxAngularSpeed;
            }
            if (qPressed) {
                desiredAngleVel += playerData.maxAngularSpeed;
            }

            //Clicking left mouse = use tool. charging keeps track of whether button is held.
            if (mousePressed) {
            	//Log.info("USE TOOL START SERVER AHHHHHHHHH - player " + playerData.playerNumber);
                useToolStart(delta, playerData.getCurrentTool(), Constants.Filters.PLAYER_HITBOX, mousePosX, mousePosY, true);
            }

            if (spacePressed) {
                if (currentEvent != null && interactCdCount < 0) {
                    interactCdCount = interactCd;
                    currentEvent.eventData.onInteract(this);
                }
            }

            //If playerNumber is reloading, run the reload method of the current equipment.
            if (playerData.getCurrentTool().reloading) {
                playerData.getCurrentTool().reload(delta);
            }

            //process cooldowns
            shootCdCount -= delta;
            shootDelayCount -= delta;

			//If the delay on using a tool just ended, use the tool.
			if (shootDelayCount <= 0 && usedTool != null) {
				useToolEnd();
			}

            controllerCount += delta;
            if (controllerCount >= 1 / 60f) {
                controllerCount -= 1 / 60f;

                Vector2 currentVel = body.getLinearVelocity();

                float newX = acceleration * desiredXVel + (1 - acceleration) * currentVel.x;

                float newY = acceleration * desiredYVel + (1 - acceleration) * currentVel.y;

                Vector2 force = new Vector2(newX - currentVel.x, newY - currentVel.y).scl(body.getMass());
                body.applyLinearImpulse(force.scl((1 + bodyData.getBonusLinSpeed())), body.getWorldCenter(), true);

                desiredXVel = 0.0f;
                desiredYVel = 0.0f;

                float currentAngleVel = body.getAngularVelocity();

                float newAngleVel = acceleration * desiredAngleVel + (1 - acceleration) * currentAngleVel;


                float angularForce = (newAngleVel - currentAngleVel) * (body.getMass());
                body.applyAngularImpulse(angularForce * (1 + bodyData.getBonusAngSpeed()), true);

                desiredAngleVel = 0.0f;
            }

            if (synced) {
                comp460game.server.server.sendToAllTCP(new Packets.SyncEntity(entityID.toString(), this.body.getPosition(),
                        this.body.getLinearVelocity(), this.body.getAngularVelocity(), this.body.getAngle()));
            }

            interactCdCount-=delta;
        } else {
            //If playerNumber is reloading, run the reload method of the current equipment.
            if (playerData.getCurrentTool().reloading) {
                playerData.getCurrentTool().reload(delta);
            }

            //process cooldowns
            shootCdCount -= delta;
            shootDelayCount -= delta;

            //If the delay on using a tool just ended, use the tool.
            if (shootDelayCount <= 0 && usedTool != null) {
                useToolEnd();
            }
        }

        //Stuff below the if statement should happen both on server/client, i.e. doesn't need to be "synced"
        flashingCount-=delta;

	}
	
	@Override
	public void render(SpriteBatch batch) {
//		vision.setPosition(body.getPosition());
		
		batch.setProjectionMatrix(state.sprite.combined);

		if (flashingCount > 0) {
			batch.setColor(Color.RED);
		}
		
		batch.draw(combined, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));
		
		batch.setColor(Color.WHITE);
		
/*		batch.draw(groom, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));
		
		batch.draw(dress, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));
		
		batch.draw(bride, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));*/
	}
	
	public void dispose() {
		super.dispose();
	}
}
