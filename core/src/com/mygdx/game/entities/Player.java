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

	private ConeLight vision;
	
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
                  PlayerData old2) {
		super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384);
		this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
		this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
		this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
		this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
		this.old = old;
		this.old2 = old2;
	}

    public Player(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y, String id) {
        super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384, id);
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
			vision = new ConeLight(rays, 360, Color.WHITE, 500, 0, 0, 0, 60);
			vision.setIgnoreAttachedBody(true);
			vision.attachToBody(body,0 ,0, 180);
			
			ConeLight extraVision = new ConeLight(rays, 360, Color.WHITE, 500, 0, 0, 0, 60);
			extraVision.setIgnoreAttachedBody(true);
			extraVision.attachToBody(body,0 ,0, 0);
			extraVision.setContactFilter(Constants.Filters.BIT_SENSOR, Constants.Filters.BIT_WALL, (short)0);

		}
		
		vision.setContactFilter(Constants.Filters.BIT_SENSOR, (short)0, Constants.Filters.BIT_WALL);
		vision.setSoft(true);
		
		super.create();
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
                useToolStart(delta, player1Data.currentTool, Constants.Filters.PLAYER_HITBOX, mousePosX, Gdx.graphics.getHeight() - mousePosY, true);
            }
            if (mousePressed2) {
                useToolStart(delta, player2Data.currentTool, Constants.Filters.PLAYER_HITBOX, mousePos2X, Gdx.graphics.getHeight() - mousePos2Y, true);
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
            if (player1Data.currentTool.reloading) {
                player1Data.currentTool.reload(delta);
            }
            if (player2Data.currentTool.reloading) {
                player2Data.currentTool.reload(delta);
            }

            interactCdCount-=delta;
        } else {
            //If playerNumber is reloading, run the reload method of the current equipment.
            if (playerData.currentTool.reloading) {
                playerData.currentTool.reload(delta);
            }
        }

		super.controller(delta);

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
                playerData.currentTool.reloading = true;
            }

            //Pressing '1' ... '0' = switch to weapon slot.
            if (keycode == Input.Keys.NUM_1) {
                playerData.switchWeapon(1);
            }

            if (keycode == Input.Keys.NUM_2) {
                playerData.switchWeapon(2);
            }

            if (keycode == Input.Keys.NUM_3) {
                playerData.switchWeapon(3);
            }

            if (keycode == Input.Keys.NUM_4) {
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
            RangedWeapon rw = (RangedWeapon) playerData.currentTool;
            comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button, screenX, screenY,
                    Packets.MousePressOrRelease.PRESSED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
	    if (!comp460game.serverMode) {
            RangedWeapon rw = (RangedWeapon) playerData.currentTool;
            comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button, screenX, screenY,
                    Packets.MousePressOrRelease.RELEASED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!comp460game.serverMode) {
            RangedWeapon rw = (RangedWeapon) playerData.currentTool;
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
