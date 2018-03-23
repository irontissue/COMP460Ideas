package com.mygdx.game.states;

import java.lang.reflect.Array;
import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.actors.Text;
import com.mygdx.game.comp460game;
import com.mygdx.game.actors.HpBar;
import com.mygdx.game.actors.PlayStateStage;
import com.mygdx.game.actors.UIPlay;
import com.mygdx.game.actors.UIReload;
import com.mygdx.game.entities.*;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.event.Event;
import com.mygdx.game.handlers.WorldContactListener;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.manager.GameStateManager.State;
import com.mygdx.game.server.Packets;
import com.mygdx.game.util.CameraStyles;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.Pair;
import com.mygdx.game.util.TiledObjectUtil;
import static com.mygdx.game.util.Constants.PPM;

import box2dLight.RayHandler;

import javax.sound.sampled.FloatControl;

/**
 * The PlayState is the main state of the game and holds the Box2d world, all characters + gameplay.
 * @author Zachary Tu
 *
 */
public class PlayState extends GameState implements InputProcessor {
	
	public static final boolean playerBulletsAboveShadow = true;
	public static final boolean enemyBulletsAboveShadow = true;
	public static final boolean eventsAboveShadow = true;
	
	
	//This is an entity representing the playerNumber. Atm, playerNumber is not initialized here, but rather by a "Player Spawn" event in the map.
	public Player player, player2;
	
	//These process and store the map parsed from the Tiled file.
	private TiledMap map;
	OrthogonalTiledMapRenderer tmr;
	
	//The font is for writing text.
    public BitmapFont font;
    
    //TODO: rays will eventually implement lighting.
	private RayHandler rays;
	
	//world manages the Box2d world and physics. b2dr renders debug lines for testing
	private Box2DDebugRenderer b2dr;

    private World world;

    public ArrayList<Entity> getRemoveList() {
        return removeList;
    }

    public void setRemoveList(ArrayList<Entity> removeList) {
        this.removeList = removeList;
    }

    public ArrayList<Entity> getCreateList() {
        return createList;
    }

    public void setCreateList(ArrayList<Entity> createList) {
        this.createList = createList;
    }

    //These represent the set of entities to be added to/removed from the world. This is necessary to ensure we do this between world steps.
	private ArrayList<Entity> removeList;
	private ArrayList<Pair<Entity, Float>> graveyard;
	private ArrayList<Entity> createList;
	//This is the set of entities to be updated in the world. This is necessary to ensure we do this between world steps.
    //The Object[] is a list of attributes that will be used to update the corresponding entity.
	private ArrayList<Pair<UUID, Object[]>> updateList;
	
	//This is a list of all entities in the world
	private ArrayList<Entity> entities;
	
	//TODO: Temporary tracker of number of enemies defeated. Will replace eventually
	public int score = 0;
	
	public boolean gameover = false, gameEnded = false;
	public boolean won = false;
	public static final float gameoverCd = 2.5f;
	public float gameoverCdCount;

	//these 2 counters keep track of when the graveyard is cleared.
	public static final float clearGraveCd = 20.0f;
	
	public float desiredPlayerAngle = Float.NEGATIVE_INFINITY;
	public Vector2 desiredPlayerPosition = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	public boolean needToSetPlayerPos = false;
	
//	public Set<Zone> zones;
	
//	private float controllerCounter = 0;
	
	//sourced effects from the world are attributed to this dummy.
	public Enemy worldDummy;
		
	public boolean updating = false;
	
	public Event lastSave;
	
	public PlayStateStage stage;
	
	/**
	 * Constructor is called upon playerNumber beginning a game.
	 * @param gsm: StateManager
	 */
	public PlayState(GameStateManager gsm, String level, PlayerData old, PlayerData old2) {
		super(gsm);
		
		//Initialize font and text camera for ui purposes.
        font = new BitmapFont();
        
        //Initialize box2d world and related stuff
		world = new World(new Vector2(0, 0), false);
		world.setContactListener(new WorldContactListener());
		rays = new RayHandler(world);
        rays.setAmbientLight(1f, 1f, 1f, 0.5f);
        rays.setBlurNum(3);
        rays.setCulling(false);
        
        RayHandler.useDiffuseLight(true);
        
        rays.setCombinedMatrix(camera);
		b2dr = new Box2DDebugRenderer();
		b2dr.setDrawBodies(false);
		
		//Initialize sets to keep track of active entities
		removeList = new ArrayList<Entity>();
		createList = new ArrayList<Entity>();
		graveyard = new ArrayList<Pair<Entity, Float>>();
		updateList = new ArrayList<Pair<UUID, Object[]>>();
		entities = new ArrayList<Entity>();
		
		//The "worldDummy" will be the source of map-effects that want a perpetrator
		worldDummy = new Enemy(this, world, camera, rays, 1, 1, -1000, -1000, true);
				
//        map = new TmxMapLoader().load("maps/map_1_460.tmx");
//        map = new TmxMapLoader().load("maps/map_2_460.tmx");
//        map = new TmxMapLoader().load("maps/argh.tmx");
//        map = new TmxMapLoader().load("maps/kenney_map.tmx");
		map = new TmxMapLoader().load(level);
		
		tmr = new OrthogonalTiledMapRenderer(map);
		
		rays.setCombinedMatrix(camera);
		//rays.setCombinedMatrix(camera.combined.cpy().scl(PPM));
		
		player = new Player(this, world, camera, rays, 100, 100, old, 1, true);
		player2 = new Player(this, world, camera, rays, 100, 150, old2, 2, true);
		
        if (comp460game.serverMode) {
            comp460game.server.server.sendToAllTCP(new Packets.SyncCreateSchmuck(player.entityID.toString(), 32,32, 100, 100, Constants.EntityTypes.PLAYER, true, 1));
            comp460game.server.server.sendToAllTCP(new Packets.SyncCreateSchmuck(player2.entityID.toString(), 32,32, 100, 150, Constants.EntityTypes.PLAYER, true, 2));
            Log.info("Server sending playerNumber UUID message: " + player.entityID.toString());
            Log.info("Server sending playerNumber UUID message: " + player2.entityID.toString());
        }
		TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("collision-layer").getObjects());

        if (comp460game.serverMode) {
			TiledObjectUtil.parseTiledEventLayer(this, world, camera, rays, map.getLayers().get("event-layer").getObjects());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		TiledObjectUtil.parseTiledTriggerLayer(this, world, camera, rays);

		if (!comp460game.serverMode) {
		    Log.info("Client loaded playstate, level = " + level);
		    comp460game.client.client.sendTCP(new Packets.ClientLoadedPlayState(level));
        }

        setInput();
	}
	
	public void loadLevel(String level) {
	    if (comp460game.serverMode) {
	        comp460game.server.server.sendToAllTCP(new Packets.LoadLevel(level));
        } else {
	        gsm.removeState(PlayState.class);
            gsm.addPlayState(level, player.playerData, null, TitleState.class);
        }
	}

	@Override
	public void show() {
		
		this.stage = new PlayStateStage(this);
		stage.addActor(new UIPlay(comp460game.assetManager, this, player, player2));
		stage.addActor(new UIReload(comp460game.assetManager, this, player, player2));
		app.newMenu(stage);
		
		if (player != null) {
			setInput();
		}
	}
	
	/**
	 * Every engine tick, the GameState must process all entities in it according to the time elapsed.
	 */
	@Override
	public void update(float delta) {
		
		//The box2d world takes a step. This handles collisions + physics stuff. Maybe change delta to set framerate?
        updating = true;
		world.step(delta, 6, 2);

		if (comp460game.serverMode) {
			for (int i = 0; i < graveyard.size(); i++) {
				graveyard.get(i).setRight(graveyard.get(i).getValue() - delta);
				if (graveyard.get(i).getValue() <= 0) {
					graveyard.remove(i);
					i--;
				}
			}
		}
		
		//All entities that are set to be removed are removed.
        while (!removeList.isEmpty()) {
            Entity entity = removeList.remove(0);
            if (entities.contains(entity)) {
                entities.remove(entity);
                if (comp460game.serverMode && entity instanceof Schmuck) {
                    comp460game.server.server.sendToAllTCP(new Packets.RemoveEntity(entity.entityID.toString()));
                }
                entity.dispose();
            }
        }
        /*for (Entity entity : removeList) {
            if (entities.contains(entity)) {
                entities.remove(entity);
                if (comp460game.serverMode && entity instanceof Schmuck) {
                    comp460game.server.server.sendToAllTCP(new Packets.RemoveEntity(entity.entityID.toString()));
                }
                entity.dispose();
            }
        }
        removeList.clear();*/

		//All entities that are set to be added are added.
        while (!createList.isEmpty()) {
            Entity entity = createList.remove(0);
            entities.add(entity);
            entity.create();
        }
        /*for (Entity entity : createList) {
            entities.add(entity);
            entity.create();
        }
        createList.clear();*/

        //All entities that are set to be updated are updated.
        while (!updateList.isEmpty()) {
            Pair<UUID, Object[]> p = updateList.remove(0);
            //Log.info(p.getKey().toString());
            //This null check is to prevent bugs - but, the root cause of p being null is unknown. Theoretically
            //it shouldn't ever be null??
            if (p != null) {
                Entity e = getEntity(p.getKey());
                if (e != null && entities.contains(e)) {
                    e.getBody().setTransform((Vector2) p.getValue()[0], (Float) p.getValue()[3]);
                    e.getBody().setLinearVelocity((Vector2) p.getValue()[1]);
                    e.getBody().setAngularVelocity((Float) p.getValue()[2]);
                }
            }
        }
		
		
/*		controllerCounter += delta;
		
		if (controllerCounter >= 1/60f) {
			controllerCounter  -= 1/60f;
			for (HadalEntity schmuck : schmucks) {
				schmuck.controller(1 / 60f);
			}
		}*/
		
		//This processes all entities in the world. (for example, playerNumber input/cooldowns/enemy ai)
		if (!gameEnded) {
			for (Entity entity : entities) {
				entity.controller(delta);
			}
		}
        /*if (needToSetPlayerPos) {
            player.body.setTransform(desiredPlayerPosition, desiredPlayerAngle);
            needToSetPlayerPos = false;
        }*/
		//Update the game camera and batch.
		cameraUpdate();
		tmr.setView(camera);
		batch.setProjectionMatrix(camera.combined);
//		rays.setCombinedMatrix(camera.combined.cpy().scl(PPM));
		
		//process gameover
		if (gameover) {
			gameoverCdCount -= delta;
			
			/*if (!comp460game.serverMode) {
				if (player.vision.getDistance() > 0) {
					player.vision.setDistance(player.vision.getDistance() - 1.0f);
				}
			}*/
			
			if (gameoverCdCount < 0) {
//				if (lastSave != null) {
//					gsm.removeState(PlayState.class);
                    if (comp460game.serverMode) {
                        gameend();
                    }
					gameover = false;
/*				} else {
					playerNumber = new Player(this, world, camera, rays,
							(int)(lastSave.getBody().getPosition().x * PPM),
							(int)(lastSave.getBody().getPosition().y * PPM));
					
//					controller.setPlayer(playerNumber);
					
					gameover = false;
				}*/
			}
		}
        updating = false;

	}

	private Actor back, readyToBack;
	public void gameend() {
		if (won) {
			if (comp460game.serverMode) {
				comp460game.server.server.sendToAllTCP(new Packets.gameOver(true));
			}
//			gsm.addState(State.VICTORY, TitleState.class);
			Text victory = new Text(comp460game.assetManager, "VICTORY", 300, 500, Color.WHITE);
			victory.setScale(0.5f);
			stage.addActor(victory);
		} else {
			if (comp460game.serverMode) {
				comp460game.server.server.sendToAllTCP(new Packets.gameOver(false));
			}
//			gsm.addState(State.GAMEOVER, TitleState.class);
			Text defeat = new Text(comp460game.assetManager, "YOU DIED", 300, 500, Color.WHITE);
			defeat.setScale(0.5f);
			stage.addActor(defeat);
		}
		if (!comp460game.serverMode) {
			back = new Text(comp460game.assetManager, "CLICK HERE TO RETURN TO LOADOUT", 300, 400, Color.WHITE);
			readyToBack = new Text(comp460game.assetManager, "WAITING ON OTHER PLAYER...", 300, 400, Color.WHITE);
			readyToBack.setVisible(false);
			back.setScale(0.5f);
			readyToBack.setScale(0.5f);
			Gdx.input.setInputProcessor(stage);
			back.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent e, float x, float y) {
					back.setVisible(false);
					readyToBack.setVisible(true);
					Log.info("yay");
					comp460game.client.client.sendTCP(new Packets.ReadyToPlay());
					//Gdx.input.setInputProcessor(player);
				}
			});
			stage.addActor(back);
			stage.addActor(readyToBack);
		}
		
	}

	/**
	 * This method renders stuff to the screen after updating.
	 * TODO: atm, this is mostly debug info + temporary ui. Will replace eventually
	 */
	@Override
	public void render() {
	    updating = true;
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Render Tiled Map + world
		tmr.render();				

		//Render debug lines for box2d objects.
		b2dr.render(world, camera.combined.scl(PPM));
		
		
		//Iterate through entities in the world to render
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		for (Entity schmuck : entities) {
		    if (!(schmuck instanceof Player)) {
                schmuck.render(batch);
            }
		}
		player.render(batch);
		player2.render(batch);
		batch.end();
		
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { gsm.addState(State.MENU, PlayState.class); }
		
		updating = false;
		
		//Update rays. Does nothing yet.
		rays.setCombinedMatrix(camera);
		rays.updateAndRender();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		for (Entity schmuck : entities) {
		    if (!(schmuck instanceof Player)) {
                schmuck.renderAboveShadow(batch);
            }
		}
		player.renderAboveShadow(batch);
		player2.renderAboveShadow(batch);
		batch.end();
	}	
	
	/**
	 * This is called every update. This resets the camera zoom and makes it move towards the playerNumber.
	 */
	private void cameraUpdate() {
		camera.zoom = 1;
		sprite.zoom = 1;
		if (player != null) {
			if (player.getBody() != null) {
//				CameraStyles.lerpToPlayerAngle(camera, playerNumber.getBody().getPosition().scl(PPM), playerNumber.getBody().getAngle());
//				CameraStyles.lerpToPlayerAngle(sprite, playerNumber.getBody().getPosition().scl(PPM), playerNumber.getBody().getAngle());
				CameraStyles.lerpToTarget(camera, player.getBody().getPosition().scl(PPM));
				CameraStyles.lerpToTarget(sprite, player.getBody().getPosition().scl(PPM));
			}
		}
	}
	
	/**
	 * This is called upon exiting. Dispose of all created fields.
	 */
	@Override
	public void dispose() {
		b2dr.dispose();
		
		for (Entity schmuck : entities) {
			schmuck.dispose();
		}
		
		world.dispose();
		tmr.dispose();
		map.dispose();
		
		if (stage != null ) {
			stage.dispose();
		}
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be deleted next engine tick.
	 * @param entity: delet this
	 */
	public void destroy(Entity entity) {
	    if (!removeList.contains(entity)) {
            removeList.add(entity);
            
            if (comp460game.serverMode) {
			    comp460game.server.server.sendToAllTCP(new Packets.RemoveEntity(entity.entityID.toString()));
            }
            
            graveyard.add(new Pair<Entity, Float>(entity, clearGraveCd));
            
        }
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be created next engine tick.
	 * @param entity: entity to be created
	 */
	public void create(Entity entity) {
		createList.add(entity);
	}
	
	/**
	 * Getter for the playerNumber. This will return null if the playerNumber has not been spawned
	 * @return: The Player entity.
	 */
	public Player getPlayer() {
		return player;
	}
	
	public Player getPlayer2() {
		return player2;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<Entity> e) {
		entities = e;
	}

	public void addEntity(Entity e) { entities.add(e); }

    public RayHandler getRays() {
        return rays;
    }

    public World getWorld() {
        return world;
    }

	/**
	 * Tentative tracker of playerNumber kill number.
	 * @param i: Number to increase score by.
	 */

	public void incrementScore(int i) {
		score += i;
	}
	
	public void gameOver(boolean won) {
		if (!gameEnded) {
			this.won = won;
			gameover = true;
			gameoverCdCount = gameoverCd;
			gameEnded = true;
		}
	}

	/**
	 * Gets an entity given its UUID.
	 * The reason why it's around a try catch is that currently there's no way to (efficiently) handle
	 * concurrent modification exceptions due to the client accessing this method at any time.
	 * This may result in an entity not being updated in a tick, so perhaps there will be slight stuttering issues?
	 * @param entityID The UUID of the entity to get
	 * @return the entity, null if no entity was found.
	 */
	public Entity getEntity(UUID entityID) {
		try {
			for (int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				if (e.entityID.equals(entityID)) {
					return e;
				}
			}
			for (int i = 0; i < graveyard.size(); i++) {
				Entity e = graveyard.get(i).getKey();
				if (e.entityID.equals(entityID)) {
					return e;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

	public void updateEntity(UUID entityID, Vector2 pos, Vector2 vel, float aVel, float a) {
        Object[] toUpdate = {pos, vel, aVel, a};
	    updateList.add(new Pair<UUID, Object[]>(entityID, toUpdate));
	    //Log.info("Added UUID to update list: " + entityID.toString() + ". To be updated with values: " + Arrays.toString(toUpdate));
    }

    /**
     * Makes the given entity set its shooting direction by calling mouseClicked(). Only to be used on client side,
     * when the client receives a message from the server telling it to shoot.
     *
     * @param entityID ID of entity
     * @param delta time since last engine tick
     * @param x X aim direction
     * @param y Y aim direction
     */
    public void setEntityAim(UUID entityID, float delta, int x, int y) {
        Entity target = getEntity(entityID);
        if (target == null) { return; }
        if (target instanceof Player) {
            ((Player) target).playerData.getCurrentTool().mouseClicked(delta, this, ((Player) target).getBodyData(),
                    Constants.Filters.PLAYER_HITBOX, x, y, world, camera, rays);
        } else if (target instanceof Enemy) {
            ((Enemy) target).weapon.mouseClicked(delta, this, ((Enemy) target).getBodyData(),
                    Constants.Filters.ENEMY_HITBOX, x, y, world, camera, rays);
        }
    }

//    /**
//     * Makes the given entity shoot its weapon by calling execute(). Only to be used on client side, when the client
//     * receives a message from the server telling it to shoot.
//     *
//     * @param entityID ID of entity
//     */
//    public void entityShoot(UUID entityID, String[] bulletIDs) {
//        Entity target = getEntity(entityID);
////        Log.info("ShootID = " + entityID.toString());
//        if (target == null) {
//            Log.info("NULL Target!!!!");
//            return; }
//        if (target instanceof Player) {
////            Log.info("Player shoots (instruction from server)!");
//            ((Player) target).playerData.currentTool.execute(this, ((Player) target).getBodyData(), world, camera, rays, bulletIDs);
//        } else if (target instanceof Enemy) {
////            Log.info("Player shoots (instruction from server)!");
//            ((Enemy) target).weapon.execute(this, ((Enemy) target).getBodyData(), world, camera, rays, bulletIDs);
//        }
//    }

    public void clientCreateSchmuck(String id, float w, float h, float startX, float startY, int type, boolean synced, int playerNumber) {
        UUID entityID = UUID.fromString(id);
        switch(type) {
            case Constants.EntityTypes.PLAYER : {
                if (playerNumber == gsm.playerNumber) {
					Log.info("MY PLAYER (P" + playerNumber + ") entityID assigned as: " + id);
					player.entityID = entityID;
				} else {
					Log.info("OTHER PLAYER (P" + playerNumber + ") entityID assigned as: " + id);
                	player2.entityID = entityID;
				}
                break;
            }
            case Constants.EntityTypes.ENEMY : {
                new Enemy(this, world, camera, rays, w, h, startX, startY, synced, id);
                break;
            }
            case Constants.EntityTypes.RANGED_ENEMY : {
                new RangedEnemy(this, world, camera, rays, w, h, startX, startY, synced, id);
                break;
            }
            case Constants.EntityTypes.STANDARD_ENEMY : {
                new StandardEnemy(this, world, camera, rays, w, h, startX, startY, synced, id);
                break;
            }
            case Constants.EntityTypes.STEERING_ENEMY : {
                new SteeringEnemy(this, world, camera, rays, w, h, startX, startY, synced, id);
                break;
            }
            default : break;
        }
    }

    public void setInput() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!comp460game.serverMode && player.playerData != null) {
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
                if (stage != null) {
    				stage.nextDialogue();
    			}
            }

            //Pressing 'R' = reload current weapon.
            if (keycode == Input.Keys.R) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.R, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                player.playerData.getCurrentTool().reloading = true;
            }

            //Pressing '1' ... '0' = switch to weapon slot.
            if (keycode == Input.Keys.NUM_1) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_1, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                player.playerData.switchWeapon(1);
            }

            if (keycode == Input.Keys.NUM_2) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_2, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                player.playerData.switchWeapon(2);
            }

            if (keycode == Input.Keys.NUM_3) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_3, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                player.playerData.switchWeapon(3);
            }

            if (keycode == Input.Keys.NUM_4) {
                comp460game.client.client.sendTCP(new Packets.KeyPressOrRelease(Input.Keys.NUM_4, Packets.KeyPressOrRelease.PRESSED, comp460game.client.IDOnServer));
                player.playerData.switchWeapon(4);
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!comp460game.serverMode && player.playerData != null) {
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
        if (!comp460game.serverMode && player.playerData != null) {
            RangedWeapon rw = (RangedWeapon) player.playerData.getCurrentTool();
            
            Vector3 mousePosition = new Vector3(screenX, screenY, 0);
    		camera.unproject(mousePosition);
			comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button,
					Packets.MousePressOrRelease.PRESSED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!comp460game.serverMode && player.playerData != null) {
            RangedWeapon rw = (RangedWeapon) player.playerData.getCurrentTool();
            Vector3 mousePosition = new Vector3(screenX, screenY, 0);
    		camera.unproject(mousePosition);
			comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(button,
					Packets.MousePressOrRelease.RELEASED, comp460game.client.IDOnServer));
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!comp460game.serverMode && player.playerData != null) {
            RangedWeapon rw = (RangedWeapon) player.playerData.getCurrentTool();

            Vector3 mousePosition = new Vector3(screenX, screenY, 0);
    		camera.unproject(mousePosition);
			comp460game.client.client.sendTCP(new Packets.MouseReposition(mousePosition.x, mousePosition.y,
					comp460game.client.IDOnServer));
			/*comp460game.client.client.sendTCP(new Packets.MousePressOrRelease(Input.Buttons.LEFT,
					Packets.MousePressOrRelease.PRESSED, comp460game.client.IDOnServer));*/
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
		if (!comp460game.serverMode && player.playerData != null) {
			RangedWeapon rw = (RangedWeapon) player.playerData.getCurrentTool();

			Vector3 mousePosition = new Vector3(screenX, screenY, 0);
			camera.unproject(mousePosition);
			comp460game.client.client.sendTCP(new Packets.MouseReposition(mousePosition.x, mousePosition.y,
					comp460game.client.IDOnServer));
		}
    	return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
