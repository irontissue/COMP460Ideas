package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import com.mygdx.game.equipment.RangedWeapon;
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

public class Player extends Schmuck implements InputProcessor {
    public static final int ENTITY_TYPE = Constants.EntityTypes.PLAYER;
	protected MoveStates moveState1, moveState2;

    //Counters that keep track of delay between action initiation + action execution and action execution + next action
    public float shootCdCount1 = 0, shootCdCount2 = 0;
    public float shootDelayCount1 = 0, shootDelayCount2 = 0;
    //The last used tool. This is used to process equipment with a delay between using and executing.
    public Equipment usedTool1, usedTool2;

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
    public boolean wPressed2 = false, aPressed2 = false, sPressed2 = false, dPressed2 = false, qPressed2 = false, ePressed2 = false;
    public boolean mousePressed = false, mousePressed2 = false;
    public boolean spacePressed = false, spacePressed2 = false;
    public int mousePosX = -1, mousePosY = -1, mousePos2X = -1, mousePos2Y = -1;
		
	//user data
	public PlayerData playerData;
	public Event currentEvent;
	
//	public Player2Dummy dummy;
	public PlayerData old, old2, player1Data, player2Data;
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
                  PlayerData old2, boolean synced) {
		super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384, synced);
		this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
		this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
		this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
		this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
		this.old = old;
		this.old2 = old2;
	}

    public Player(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y, boolean synced, String id) {
        super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384, synced, id);
        this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
        this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
        this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
        this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
    }
	
	/**
	 * Create the playerNumber's body and initialize playerNumber's user data.
	 */
	public void create() {
	    setInput();
		
		//server's has 2 datas that represent player1 and player2
		if (comp460game.serverMode) {
			player1Data = new PlayerData(world, this);
			player2Data = new PlayerData(world, this);
			player1Data.playerNumber = 1;
			player2Data.playerNumber = 2;
			this.playerData = player1Data;
            this.bodyData = player1Data;

			if (old != null) {
			    Log.info("Server copied player1data.");
			    player1Data.copyData(old);
            }
            if (old2 != null) {
                Log.info("Server copied player2data.");
                player2Data.copyData(old2);
            }
		} else {
            //Clients will have a single data attached to the half of the body they represent
            this.playerData = new PlayerData(world, this);
            this.playerData.playerNumber = 1;
            this.bodyData = playerData;

            if (old != null) {
                Log.info("Client copied playerdata.");
                playerData.copyData(old);
            }
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
			player2Fixture.setUserData(player2Data);
			
			player1Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(width / 2 / PPM, 0), true, 0,
					Constants.Filters.BIT_SENSOR, (short)(Constants.Filters.BIT_WALL | Constants.Filters.BIT_ENEMY), Constants.Filters.PLAYER_HITBOX));
			player1Fixture.setUserData(player1Data);
		}
				
		if (!comp460game.serverMode) {
			vision = new ConeLight(rays, 32, Color.WHITE, 500, 0, 0, 0, 60);
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
		vision.setSoftnessLength(5);
		
		super.create();
	}

	@Override
    public void useToolStart(float delta, Equipment tool, short filter, int x, int y, boolean wait, int pNumber) {
        //Log.info("Got into user tool start - player " + pNumber);
        //Only register the attempt if the user is not waiting on a tool's delay or cooldown. (or if tool ignores wait)
        if (pNumber == 1) {
            if ((shootCdCount1 < 0 && shootDelayCount1 < 0) || !wait) {

                //account for the tool's use delay.
                shootDelayCount1 = tool.useDelay;

                //Register the tool targeting the input coordinates.
//			if (comp460game.serverMode) {
//			    comp460game.server.server.sendToAllTCP(new Packets.SetEntityAim(entityID.toString(), delta, x, y));
//            }
                tool.mouseClicked(delta, state, player1Data, filter, x, y, world, camera, rays);
                usedTool1 = tool;
            }
        } else {
            if ((shootCdCount2 < 0 && shootDelayCount2 < 0) || !wait) {

                //account for the tool's use delay.
                shootDelayCount2 = tool.useDelay;

                //Register the tool targeting the input coordinates.
//			if (comp460game.serverMode) {
//			    comp460game.server.server.sendToAllTCP(new Packets.SetEntityAim(entityID.toString(), delta, x, y));
//            }
                tool.mouseClicked(delta, state, player2Data, filter, x, y, world, camera, rays);
                usedTool2 = tool;
            }
        }
    }

    /**
     * This method is called after a tool is used following the tool's delay.
     */
    @Override
    public void useToolEnd(int pNumber) {
        String[] uuids;
        if (pNumber == 1) {
            //the schmuck will not register another tool usage for the tool's cd
            shootCdCount1 = usedTool1.useCd * (1 - bodyData.getToolCdReduc());
            //execute the tool.
            uuids = usedTool1.execute(state, player1Data, world, camera, rays, null);//clear the used tool field.
            usedTool1 = null;
        } else {
            //the schmuck will not register another tool usage for the tool's cd
            shootCdCount2 = usedTool2.useCd * (1 - bodyData.getToolCdReduc());
            //execute the tool.
            uuids = usedTool2.execute(state, player2Data, world, camera, rays, null);//clear the used tool field.
            usedTool2 = null;
        }
    }

	public void setInput() {
		Gdx.input.setInputProcessor(this);
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
                desiredYVel += player1Data.maxSpeed;
            }
            if (aPressed) {
                desiredXVel += -player1Data.maxSpeed;
            }
            if (sPressed) {
                desiredYVel += -player1Data.maxSpeed;
            }
            if (dPressed) {
                desiredXVel += player1Data.maxSpeed;
            }

            if (wPressed2) {
                desiredYVel += player2Data.maxSpeed;
            }
            if (aPressed2) {
                desiredXVel += -player2Data.maxSpeed;
            }
            if (sPressed2) {
                desiredYVel += -player2Data.maxSpeed;
            }
            if (dPressed2) {
                desiredXVel += player2Data.maxSpeed;
            }

            if (ePressed) {
                desiredAngleVel += -player1Data.maxAngularSpeed;
            }
            if (ePressed2) {
                desiredAngleVel += -player2Data.maxAngularSpeed;
            }

            if (qPressed) {
                desiredAngleVel += player1Data.maxAngularSpeed;
            }
            if (qPressed2) {
                desiredAngleVel += player2Data.maxAngularSpeed;
            }

            //Clicking left mouse = use tool. charging keeps track of whether button is held.
            if (mousePressed) {
                useToolStart(delta, player1Data.getCurrentTool(), Constants.Filters.PLAYER_HITBOX, mousePosX, Gdx.graphics.getHeight() - mousePosY, true, 1);
            }
            if (mousePressed2) {
                useToolStart(delta, player2Data.getCurrentTool(), Constants.Filters.PLAYER_HITBOX, mousePos2X, Gdx.graphics.getHeight() - mousePos2Y, true, 2);
            }

            if (spacePressed) {
                if (currentEvent != null && interactCdCount < 0) {
                    interactCdCount = interactCd;
                    currentEvent.eventData.onInteract(this, 1);
                }
            }

            if (spacePressed2) {
                if (currentEvent != null && interactCdCount < 0) {
                    interactCdCount = interactCd;
                    currentEvent.eventData.onInteract(this, 2);
                }
            }

            //If playerNumber is reloading, run the reload method of the current equipment.
            if (player1Data.getCurrentTool().reloading) {
                player1Data.getCurrentTool().reload(delta);
            }
            if (player2Data.getCurrentTool().reloading) {
                player2Data.getCurrentTool().reload(delta);
            }

            //process cooldowns
            shootCdCount1 -= delta;
            shootDelayCount1 -= delta;

            //If the delay on using a tool just ended, use the tool.
            if (shootDelayCount1 <= 0 && usedTool1 != null) {
                useToolEnd(1);
            }

            //process cooldowns
            shootCdCount2 -= delta;
            shootDelayCount2 -= delta;
            //If the delay on using a tool just ended, use the tool.
            if (shootDelayCount2 <= 0 && usedTool2 != null) {
                useToolEnd(2);
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
                useToolEnd(0);
            }
        }

        //Stuff below the if statement should happen both on server/client, i.e. doesn't need to be "synced"
        flashingCount-=delta;

	}
	
	@Override
	public void render(SpriteBatch batch) {
		vision.setPosition(body.getPosition());
		
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

    @Override
    public boolean keyDown(int keycode) {
	    if (!comp460game.serverMode) {
            if (keycode == Input.Keys.W) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.W, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.A) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.A, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.S) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.S, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.D) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.D, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.Q) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.Q, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.E) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.E, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.SPACE) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.SPACE, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
            }

            //Pressing 'R' = reload current weapon.
            if (keycode == Input.Keys.R) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.R, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                playerData.getCurrentTool().reloading = true;
            }

            //Pressing '1' ... '0' = switch to weapon slot.
            if (keycode == Input.Keys.NUM_1) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_1, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                playerData.switchWeapon(1);
            }

            if (keycode == Input.Keys.NUM_2) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_2, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                playerData.switchWeapon(2);
            }

            if (keycode == Input.Keys.NUM_3) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_3, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                playerData.switchWeapon(3);
            }

            if (keycode == Input.Keys.NUM_4) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_4, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                playerData.switchWeapon(4);
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!comp460game.serverMode) {
            if (keycode == Input.Keys.W) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.W, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.A) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.A, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.S) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.S, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.D) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.D, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.Q) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.Q, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.E) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.E, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
            if (keycode == Input.Keys.SPACE) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.SPACE, Packets.KeyPressOrRelease.RELEASED, comp460game.client.IDOnServer));
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!comp460game.serverMode) {
            RangedWeapon rw = (RangedWeapon) playerData.getCurrentTool();
            comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button, screenX, screenY,
                    Packets.MousePressOrRelease.PRESSED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
	    if (!comp460game.serverMode) {
            RangedWeapon rw = (RangedWeapon) playerData.getCurrentTool();
            comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button, screenX, screenY,
                    Packets.MousePressOrRelease.RELEASED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!comp460game.serverMode) {
            RangedWeapon rw = (RangedWeapon) playerData.getCurrentTool();
            comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(Input.Buttons.LEFT, screenX, screenY,
                    Packets.MousePressOrRelease.PRESSED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
