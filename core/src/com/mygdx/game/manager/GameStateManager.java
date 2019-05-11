package com.mygdx.game.manager;

import java.util.HashMap;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.states.*;

/**
 * The GameStateManager manages a stack of game states. This delegates logic to the current game state.
 * @author Zachary Tu
 *
 */
public class GameStateManager {
	
	//An instance of the current game
	private comp460game app;
	//Stack of GameStates. These are all the states that the playerNumber has opened in that order.
	public Stack<GameState> states;
    private float syncTimer = 0;
    public int playerNumber;
    
	//temp skin for ui windows
	public Skin skin;
	public NinePatchDrawable patch;
	
	private String level;
	
	//This enum lists all the different types of gamestates.
	public enum State {
		SPLASH,
		TITLE,
		MENU,
		PLAY,
		GAMEOVER,
		VICTORY
	}
	
	public static HashMap<String, String> levelnames = new HashMap<String, String>();
	
	/**
	 * Constructor called by the game upon initialization
	 * @param hadalGame: instance of the current game.
	 */
	public GameStateManager(comp460game hadalGame) {
		this.app = hadalGame;
		this.states = new Stack<GameState>();
		
		this.level = "maps/loadout.tmx";
		
		//Default state is the splash state currently.
		this.addState(State.TITLE, null);
		
		BitmapFont font24 = new BitmapFont();
		this.skin = new Skin();
		this.skin.addRegions((TextureAtlas) comp460game.assetManager.get(AssetList.UISKINATL.toString()));
		this.skin.add("default-font", font24);
		this.skin.load(Gdx.files.internal("ui/uiskin.json"));
		
		this.patch = new NinePatchDrawable(((TextureAtlas) comp460game.assetManager.get(AssetList.UIPATCHATL.toString())).createPatch("UI_box_dialogue"));
		
		levelnames.put("kenney_map.tmx", "Battle 1");
		levelnames.put("trustSample.tmx", "Trust 1");
		levelnames.put("map_1_460.tmx", "Tutorial 1");
		levelnames.put("separateSpawn.tmx", "Independence 1");
		levelnames.put("sportMap.tmx", "Sports 1");
		levelnames.put("good_level.tmx", "Sandbox");
		levelnames.put("cooperation.tmx", "Cooperation 1");
		levelnames.put("cooperation2.tmx", "Cooperation 2");
		levelnames.put("loadout.tmx", "Loadout");
		levelnames.put("puzzle1.tmx", "Puzzle 1");
	}
	
	/**
	 * Getter for the main game
	 * @return: the game
	 */
	public comp460game application() {
		return app;
	}
	
	/**
	 * Run every engine tick. This delegates to the top state telling it how much time has passed since last update.
	 * @param delta: elapsed time in seconds since last engine tick.
	 */
	public void update(float delta) {
		if (!states.empty()) {
			states.peek().update(delta);

			//Any world sync things, even if we wanted to implement something syncing in the title screen, should ideally
			//be done here.
			if (states.peek() instanceof PlayState) {
				//syncTimer += delta;
				if (/*syncTimer > 0.5 && */comp460game.serverMode) {
					PlayState ps = (PlayState) states.peek();
//                Log.info("Number of entities: " + ps.getEntities().size());
					if (ps != null && ps.player != null && ps.player.getBody() != null) {
                    /*comp460game.server.server.sendToAllTCP(new Packets.SyncPlayState(ps.player.getBody().getPosition(),
                            ps.player.getBody().getAngle()));*/
					}
//                Entity[] entities = ps.getEntities().toArray(new Entity[0]);
//                Entity x;
//                for (int i = 0; i < entities.length; i++) {
//                    x = entities[i];
//                    comp460game.server.server.sendToAllTCP(new Packets.SyncEntity(x.entityID, x.getBody().getPosition(),
//                            x.getBody().getLinearVelocity(), x.getBody().getAngularVelocity(), x.getBody().getAngle()));
//				}
					syncTimer = 0;
				}
			}
		}
	}
	
	/**
	 * Run every engine tick after updating. This will draw stuff and works pretty much like update.
	 */
	public void render() {
		
		states.peek().render();
	}
	
	/**
	 * Run upon deletion (exiting game). This disposes of all states and clears the stack.
	 */
	public void dispose() {
		for (GameState gs : states) {
			gs.dispose();
		}
		states.clear();
	}
	
	/**
	 * This is run when the window resizes.
	 * @param w: new width of the screen.
	 * @param h: new height of the screen.
	 */
	public void resize(int w, int h) {
		for (Object state : states.toArray()) {
			((GameState) state).resize(w, h);
		};
	}
	
	/**
	 * This is run when we change the current state.
	 * TODO: At the moment, we only have one state active. Maybe change later?
	 * This code adds the new input state, replacing and disposing the previous state if existent.
	 * @param state: The new state
	 */
	public void addState(State state, Class<? extends GameState> lastState) {
		
		if (states.empty()) {
			states.push(getState(state));
			states.peek().show();
		} else if (states.peek().getClass().equals(lastState)) {
			states.push(getState(state));
			states.peek().show();
		}
	}

	public void addPlayState(String map, PlayerData old, PlayerData old2, Class<? extends GameState> lastState) {
		if (map == null) {
		    map = "maps/loadout.tmx";
        }
		if (states.empty()) {
			states.push(new PlayState(this, map, old, old2));
			states.peek().show();
		} else if (states.peek().getClass().equals(lastState)) {
			states.push(new PlayState(this, map, old, old2));
			states.peek().show();
		}
	}
	
    /**
     * Adds initial title state after restarting from victory/gameover screens
     * @param titleState
     */
	public void addNewTitleState(TitleState titleState) {
	    this.dispose();
	    states.push(titleState);
    }

	public void removeState(Class<? extends GameState> lastState) {
		if (!states.empty()) {
			if (states.peek().getClass().equals(lastState)) {
				states.pop().dispose();
				if (states.empty()) {return;}
				states.peek().show();
			}
		}
	}
	
	/**
	 * This is called upon adding a new state. It maps each state enum to the actual gameState that will be added to the stack
	 * @param state: enum for the new type of state to be added
	 * @return: A new instance of the gameState corresponding to the input enum
	 */
	public GameState getState(State state) {
		switch(state) {
			case SPLASH: return null;
			case TITLE: return new TitleState(this);
			case MENU: return new MenuState(this);
			case PLAY: return new PlayState(this, level, null, null);
			case GAMEOVER: return new GameoverState(this);
			case VICTORY: return new VictoryState(this);
		}
		return null;
	}
}
