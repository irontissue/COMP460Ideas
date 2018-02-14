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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.event.Event;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;
import com.mygdx.game.util.b2d.FixtureBuilder;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import static com.mygdx.game.util.Constants.PPM;

public class Player extends Schmuck implements InputProcessor {

	protected MoveStates moveState1, moveState2;

	//Fixtures and user data
	protected Fixture viewWedge;
    protected Fixture viewWedge2;

	private float lastDelta;
	public boolean spacePressed;
	//is the player currently in the process of holding their currently used tool?
	private boolean charging = false;
		
	protected float interactCd = 0.15f;
	protected float interactCdCount = 0;

	//is the button for that respective movement pressed currently?
    public boolean wPressed = false, aPressed = false, sPressed = false, dPressed = false, qPressed = false, ePressed = false;
    public boolean wPressed2 = false, aPressed2 = false, sPressed2 = false, dPressed2 = false, qPressed2 = false, ePressed2 = false;
		
	//user data
	public PlayerData playerData;
	public Event currentEvent;
	
//	public Player2Dummy dummy;
	public PlayerData player2Data;
	protected Fixture player1Fixture, player2Fixture;

	private ConeLight vision;
	
	private TextureRegion combined, bride, groom, dress;
	
	/**
	 * This constructor is called by the player spawn event that must be located in each map
	 * @param state: current gameState
	 * @param world: box2d world
	 * @param camera: game camera
	 * @param rays: game rayhandler
	 * @param x: player starting x position.
	 * @param y: player starting x position.
	 */

	public Player(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y) {
		super(state, world, camera, rays, x, y, "torpedofish_swim", 250, 161, 161, 250);
		
		dummy = new Player2Dummy(state, world, camera, rays, 250, 161, x, y, this);
  }
  
	public Player(KryoClient client, PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y) {
		super(state, world, camera, rays, x, y, "torpedofish_swim", 384, 256, 256, 384);
		this.client = client;
		this.combined = new TextureRegion(new Texture(AssetList.COMBINED.toString()));
		this.bride = new TextureRegion(new Texture(AssetList.BRIDE.toString()));
		this.groom = new TextureRegion(new Texture(AssetList.GROOM.toString()));
		this.dress = new TextureRegion(new Texture(AssetList.DRESS.toString()));
//		dummy = new Player2Dummy(state, world, camera, rays, 250, 161, x, y, this);
	}
	
	/**
	 * Create the player's body and initialize player's user data.
	 */
	public void create() {
	    Gdx.input.setInputProcessor(this);
		this.playerData = new PlayerData(world, this);
		player2Data = new PlayerData(world, this);
		
		this.bodyData = playerData;
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, false, Constants.BIT_PLAYER, 
				(short) (Constants.BIT_WALL | Constants.BIT_SENSOR | Constants.BIT_PROJECTILE | Constants.BIT_ENEMY),
				Constants.PLAYER_HITBOX, false, playerData);
		
		player2Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(- width / 2 / PPM, 0), true, 0,
				Constants.BIT_SENSOR, (short)(Constants.BIT_WALL | Constants.BIT_ENEMY), Constants.PLAYER_HITBOX));
		player2Fixture.setUserData(player2Data);
		
		player1Fixture = this.body.createFixture(FixtureBuilder.createFixtureDef(width / 2, height, new Vector2(height / 2 / PPM, 0), true, 0,
				Constants.BIT_SENSOR, (short)(Constants.BIT_WALL | Constants.BIT_ENEMY), Constants.PLAYER_HITBOX));
		player1Fixture.setUserData(playerData);
		
//		dummy.body = this.body;
//		dummy.bodyData = this.bodyData;
		
		vision = new ConeLight(rays, 32, Color.WHITE, 500, 0, 0, 90, 60);
		vision.setIgnoreAttachedBody(true);
		vision.attachToBody(body);
		
		PointLight light = new PointLight(rays, 32, Color.WHITE, 10, 0, 0);
		light.setIgnoreAttachedBody(true);
		light.attachToBody(body);
		
		super.create();
	}

	/**
	 * The player's controller currently polls for input.
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

            if (wPressed2) {
                desiredYVel += playerData.maxSpeed;
            }
            if (aPressed2) {
                desiredXVel += -playerData.maxSpeed;
            }
            if (sPressed2) {
                desiredYVel += -playerData.maxSpeed;
            }
            if (dPressed2) {
                desiredXVel += playerData.maxSpeed;
            }

            if (ePressed) {
                desiredAngleVel += -playerData.maxAngularSpeed;
            }
            if (ePressed2) {
                desiredAngleVel += -playerData.maxAngularSpeed;
            }

            if (qPressed) {
                desiredAngleVel += playerData.maxAngularSpeed;
            }
            if (qPressed2) {
                desiredAngleVel += playerData.maxAngularSpeed;
            }
        }
		
		//Clicking left mouse = use tool. charging keeps track of whether button is held.
		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			charging = true;
			useToolStart(delta, playerData.currentTool, Constants.PLAYER_HITBOX, Gdx.input.getX() , Gdx.graphics.getHeight() - Gdx.input.getY(), true);
		} else {
			if (charging) {
				useToolRelease(playerData.currentTool, Constants.PLAYER_HITBOX, Gdx.input.getX() , Gdx.graphics.getHeight() - Gdx.input.getY());
			}
			charging = false;
		}
		
		//Pressing 'SPACE' = interact with an event
//		if(Gdx.input.isKeyJustPressed((Input.Keys.SPACE))) {
//			if (currentEvent != null && interactCdCount < 0) {
//				interactCdCount = interactCd;
//				currentEvent.eventData.onInteract(this);
//			}
//		}
        if(spacePressed) {
            if (currentEvent != null && interactCdCount < 0) {
                interactCdCount = interactCd;
                currentEvent.eventData.onInteract(this);
            }
        }
				
		//If player is reloading, run the reload method of the current equipment.
		if (playerData.currentTool.reloading) {
			playerData.currentTool.reload(delta);
		}				
				
		interactCdCount-=delta;

		super.controller(delta);
	}
	
	@Override
	public void render(SpriteBatch batch) {
		vision.setPosition(body.getPosition());
		vision.setDirection(body.getAngle());
		
		batch.setProjectionMatrix(state.sprite.combined);

		batch.draw(combined, 
				body.getPosition().x * PPM - hbHeight * scale / 2, 
				body.getPosition().y * PPM - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				spriteWidth * scale, spriteHeight * scale, 1, 1, 
				(float) Math.toDegrees(body.getAngle()));
		
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
	
	public PlayerData getPlayerData() {
		return playerData;
	}

    @Override
    public boolean keyDown(int keycode) {
	    if (!comp460game.serverMode) {
            if (keycode == Input.Keys.W) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.W, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.A) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.A, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.S) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.S, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.D) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.D, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.Q) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.Q, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.E) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.E, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.SPACE) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.SPACE, Packets.KeyPressOrRelease.PRESSED, comp460game.client.myID));
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
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.W, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.A) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.A, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.S) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.S, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.D) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.D, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.Q) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.Q, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.E) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.E, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
            }
            if (keycode == Input.Keys.SPACE) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.SPACE, Packets.KeyPressOrRelease.RELEASED, comp460game.client.myID));
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

//       comp460game.client.client.sendTCP(new Packets.Packet03Click(new Vector2(screenX,screenY), null,comp460game.client.myID, lastDelta));

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
