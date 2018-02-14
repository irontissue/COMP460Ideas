package com.mygdx.game.states;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.client.KryoClient;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Player;
import com.mygdx.game.event.Event;
import com.mygdx.game.handlers.WorldContactListener;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.manager.GameStateManager.State;
import com.mygdx.game.util.CameraStyles;
import com.mygdx.game.util.TiledObjectUtil;
import static com.mygdx.game.util.Constants.PPM;

import box2dLight.RayHandler;

/**
 * The PlayState is the main state of the game and holds the Box2d world, all characters + gameplay.
 * @author Zachary Tu
 *
 */
public class PlayState extends GameState {
	
	//This is an entity representing the player. Atm, player is not initialized here, but rather by a "Player Spawn" event in the map.
	public Player player;
	
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

    public Set<Entity> getRemoveList() {
        return removeList;
    }

    public void setRemoveList(Set<Entity> removeList) {
        this.removeList = removeList;
    }

    public Set<Entity> getCreateList() {
        return createList;
    }

    public void setCreateList(Set<Entity> createList) {
        this.createList = createList;
    }

    //These represent the set of entities to be added to/removed from the world. This is necessary to ensure we do this between world steps.
	private Set<Entity> removeList;
	private Set<Entity> createList;
	
	//This is a set of all entities in the world
	private Set<Entity> entities;
	
	//TODO: Temporary tracker of number of enemies defeated. Will replace eventually
	public int score = 0;
	
	public boolean gameover = false;
	public boolean won = false;
	public static final float gameoverCd = 2.5f;
	public float gameoverCdCount;

	public float desiredPlayerAngle = Float.NEGATIVE_INFINITY;
	public Vector2 desiredPlayerPosition = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	public boolean needToSetPlayerPos = false;
	
//	public Set<Zone> zones;
	
//	private float controllerCounter = 0;
	
	public Stage stage;
	public boolean updating = false;
	
	public Event lastSave;
	
	/**
	 * Constructor is called upon player beginning a game.
	 * @param gsm: StateManager
	 */
	public PlayState(GameStateManager gsm) {
		super(gsm);
		
		//Initialize font and text camera for ui purposes.
        font = new BitmapFont();
        
        //Initialize box2d world and related stuff
		world = new World(new Vector2(0, 0), false);
		world.setContactListener(new WorldContactListener());
		rays = new RayHandler(world);
        rays.setAmbientLight(0.1f);
        rays.setCulling(false);
        rays.useDiffuseLight(true);
        rays.setCombinedMatrix(camera);
		b2dr = new Box2DDebugRenderer();
		
		//Initialize sets to keep track of active entities
		removeList = new HashSet<Entity>();
		createList = new HashSet<Entity>();
		entities = new HashSet<Entity>();
		
		//TODO: Load a map from Tiled file. Eventually, this will take an input map that the player chooses.
		//map = new TmxMapLoader().load("maps/map_1_460.tmx");
		map = new TmxMapLoader().load("maps/argh.tmx");
		
		tmr = new OrthogonalTiledMapRenderer(map);
		
		rays.setCombinedMatrix(camera);
		//rays.setCombinedMatrix(camera.combined.cpy().scl(PPM));
		
		player = new Player(gsm.application().getClient(), this, world, camera, rays, 100, 100);
		
		TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("collision-layer").getObjects());
		
		TiledObjectUtil.parseTiledEventLayer(this, world, camera, rays, map.getLayers().get("event-layer").getObjects());	
		
		TiledObjectUtil.parseTiledTriggerLayer(this, world, camera, rays);
	}

	@Override
	public void show() {

		this.stage = new Stage(); 
		app.newMenu(stage);
	}
	
	/**
	 * Every engine tick, the GameState must process all entities in it according to the time elapsed.
	 */
	@Override
	public void update(float delta) {
		
		//The box2d world takes a step. This handles collisions + physics stuff. Maybe change delta to set framerate?
        updating = true;
		world.step(delta, 6, 2);

		//All entities that are set to be removed are removed.
		for (Entity entity : removeList) {
			entities.remove(entity);
			entity.dispose();
		}
		removeList.clear();
		
		//All entities that are set to be added are added.
		for (Entity entity : createList) {
			entities.add(entity);
			entity.create();
		}
		createList.clear();
		
		
/*		controllerCounter += delta;
		
		if (controllerCounter >= 1/60f) {
			controllerCounter  -= 1/60f;
			for (HadalEntity schmuck : schmucks) {
				schmuck.controller(1 / 60f);
			}
		}*/
		
		//This processes all entities in the world. (for example, player input/cooldowns/enemy ai)
		for (Entity entity : entities) {
			entity.controller(delta);
		}
        if (needToSetPlayerPos) {
            player.body.setTransform(desiredPlayerPosition, desiredPlayerAngle);
            needToSetPlayerPos = false;
        }
		//Update the game camera and batch.
		cameraUpdate();
		tmr.setView(camera);
		batch.setProjectionMatrix(camera.combined);
//		rays.setCombinedMatrix(camera.combined.cpy().scl(PPM));
		
		//process gameover
		if (gameover) {
			gameoverCdCount -= delta;
			if (gameoverCdCount < 0) {
				if (lastSave != null) {
					gsm.removeState(PlayState.class);
					if (won) {
//						gsm.addState(State.VICTORY, TitleState.class);
					} else {
//						gsm.addState(State.GAMEOVER, TitleState.class);
					}
				} else {
					player = new Player(this, world, camera, rays,
							(int)(lastSave.getBody().getPosition().x * PPM),
							(int)(lastSave.getBody().getPosition().y * PPM));
					
//					controller.setPlayer(player);
					
					gameover = false;
				}
			}
		}
        updating = false;

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
			schmuck.render(batch);
		}
		
		//Update rays. Does nothing yet.
		rays.setCombinedMatrix(camera);
		rays.updateAndRender();
				
		batch.setProjectionMatrix(hud.combined);
		
		//Draw player information for temporary ui.
		//Check for null because player is not immediately spawned in a map.
		if (player != null) {
			if (player.getPlayerData() != null) {
				font.getData().setScale(1);
				font.draw(batch, "Score: " + score+ " Hp: " + Math.round(player.getPlayerData().currentHp) + "/" + player.getPlayerData().getMaxHp(), 20, 80);
				font.draw(batch, player.getPlayerData().currentTool.getText(), 20, 60);
			}
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { gsm.addState(State.MENU, PlayState.class); }
		
		batch.end();
		updating = false;
	}	
	
	/**
	 * This is called every update. This resets the camera zoom and makes it move towards the player.
	 */
	private void cameraUpdate() {
		camera.zoom = 1;
		sprite.zoom = 1;
		if (player != null) {
			if (player.getBody() != null) {
//				CameraStyles.lerpToPlayerAngle(camera, player.getBody().getPosition().scl(PPM), player.getBody().getAngle());
//				CameraStyles.lerpToPlayerAngle(sprite, player.getBody().getPosition().scl(PPM), player.getBody().getAngle());
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
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be deleted next engine tick.
	 * @param entity: delet this
	 */
	public void destroy(Entity entity) {
		removeList.add(entity);
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be created next engine tick.
	 * @param entity: entity to be created
	 */
	public void create(Entity entity) {
		createList.add(entity);
	}
	
	/**
	 * Getter for the player. This will return null if the player has not been spawned
	 * @return: The Player entity.
	 */
	public Player getPlayer() {
		return player;
	}

	public Set<Entity> getEntities() {
		return entities;
	}

	public void setEntities(Set<Entity> e) {
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
	 * Tentative tracker of player kill number.
	 * @param i: Number to increase score by.
	 */

	public void incrementScore(int i) {
		score += i;
	}
	
	public void gameOver(boolean won) {
		this.won = won;
		gameover = true;
		gameoverCdCount = gameoverCd;
	}
	public Entity getEntity(UUID entityID) {
	    Entity[] e = (Entity[]) entities.toArray();
	    for (int i = 0; i < e.length; i++) {
	        if (e[i].entityID == entityID) {
	            return e[i];
            }
        }
        return null;
    }
	public void updateEntity(UUID entityID, Vector2 pos, Vector2 vel, float aVel, float a) {
	    Entity target = getEntity(entityID);
	    if (target == null) { return; }
        target.getBody().setTransform(pos,a);
	    target.getBody().setLinearVelocity(vel);
	    target.getBody().setAngularVelocity(aVel);
    }

}
