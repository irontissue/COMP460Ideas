package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.client.KryoClient;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.comp460game;
import com.mygdx.game.actors.Text;
import com.mygdx.game.server.KryoServer;
import com.mygdx.game.server.Packets;

public class TitleState extends GameState {

	private Stage stage;

    //Temporary links to other modules for testing.
	private Actor playOption, exitOption, joinServerOption, startServerOption;
	
	public TitleState(GameStateManager gsm) {
		super(gsm);
	}

	public void startGame() {

    }

	@Override
	public void show() {
		stage = new Stage() {
			{
				playOption = new Text(comp460game.assetManager, "PLAY?", 150, comp460game.CONFIG_HEIGHT - 180);
				startServerOption = new Text(comp460game.assetManager, "START SERVER?", 150, comp460game.CONFIG_HEIGHT - 240);
				joinServerOption = new Text(comp460game.assetManager, "JOIN SERVER?", 150, comp460game.CONFIG_HEIGHT - 300);
				exitOption = new Text(comp460game.assetManager, "EXIT?", 150, comp460game.CONFIG_HEIGHT - 360);
				
				playOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			            Log.info("Clicked play button...");
			            if (comp460game.client == null) return;
                        Log.info("Client successfully set");
                        Packets.ReadyToPlay r2p = new Packets.ReadyToPlay();

                        if (!comp460game.serverMode) {
                            comp460game.client.client.sendTCP(r2p);
                        }
                        
//                        gsm.addState(State.PLAY, TitleState.class);
			        }
			    });
				playOption.setScale(0.5f);
				
				joinServerOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
                        comp460game.client.init();
			        }
			    });
				joinServerOption.setScale(0.5f);
				
				startServerOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {

						/*if (client.myGame.server == null) {
						    client.myGame.server = new KryoServer(gsm);
                        }*/
//			        	try {
//							new KryoServer();
//						} catch (IOException e1) {
//							e1.printStackTrace();
//						}
			        }
			    });
				startServerOption.setScale(0.5f);
				
				exitOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	Gdx.app.exit();
			        }
			    });
				exitOption.setScale(0.5f);
				
				addActor(playOption);
				addActor(startServerOption);
				addActor(joinServerOption);
				addActor(exitOption);
			}
		};
		app.newMenu(stage);
	}
	
	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		stage.dispose();	
	}

}
