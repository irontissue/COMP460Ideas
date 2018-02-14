package com.mygdx.game.manager;

import java.util.Set;
import java.util.Stack;

import com.esotericsoftware.minlog.Log;
import com.mygdx.game.client.KryoClient;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.server.Packets;
import com.mygdx.game.states.*;

/**
 * The GameStateManager manages a stack of game states. This delegates logic to the current game state.
 * @author Zachary Tu
 *
 */
public class GameStateManager {
	
	//An instance of the current game
	private comp460game app;
	//Stack of GameStates. These are all the states that the player has opened in that order.
	public Stack<GameState> states;
    private float syncTimer = 0;
	
	//This enum lists all the different types of gamestates.
	public enum State {
		SPLASH,
		TITLE,
		MENU,
		PLAY
	}
	
	/**
	 * Constructor called by the game upon initialization
	 * @param hadalGame: instance of the current game.
	 */
	public GameStateManager(comp460game hadalGame) {
		this.app = hadalGame;
		this.states = new Stack<GameState>();
		
		//Default state is the splash state currently.
		this.addState(State.TITLE, null);
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
		states.peek().update(delta);

		//Any world sync things, even if we wanted to implement something syncing in the title screen, should ideally
        //be done here.
		if (states.peek() instanceof PlayState) {
            //syncTimer += delta;
            if (/*syncTimer > 0.5 && */comp460game.serverMode) {
                PlayState ps = (PlayState) states.peek();
//                Log.info("Number of entities: " + ps.getEntities().size());
                comp460game.server.server.sendToAllTCP(new Packets.SyncPlayState(ps.player.getBody().getPosition(),
                        ps.player.getBody().getAngle()));
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
	
	public void removeState(Class<? extends GameState> lastState) {
		if (!states.empty()) {
			if (states.peek().getClass().equals(lastState)) {
				states.pop().dispose();
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
			case PLAY: return new PlayState(this);
		}
		return null;
	}
}
