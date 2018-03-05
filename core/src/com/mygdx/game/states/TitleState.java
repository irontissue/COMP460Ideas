package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
	private Actor playOption, exitOption, joinServerOption, startServerOption, waitingOnPlayer2, disconnect, title;
	Texture bground1, bground2;
    private boolean isWaiting = false;
	public TitleState(GameStateManager gsm) {
		super(gsm);
	}


	public void startGame() {

    }

	@Override
	public void show() {
		if (!comp460game.serverMode) {
			stage = new Stage() {
				{
                    final Table table = new Table();
				    // https://twomann.com/wp-content/uploads/2017/03/Two-Mann-Studios-Worlds-Best-Wedding-Photography-Best-of-2016-001-1080x720.jpg
                    // Source of tree image
                    bground1 = new Texture("Images/title_background.png");
                    bground1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                    Actor bg1 = new Image(bground1);

                    bground2 = new Texture("Images/title_background.png");
                    bground2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                    Actor bg2 = new Image(bground2);
                    
                    Actor overlay = new Image(new Texture("Images/Overlay.png"));
                    overlay.setWidth(300);
                    overlay.setHeight(comp460game.CONFIG_HEIGHT);
                    overlay.setPosition(390, 0);
                    
                    bg1.addAction(Actions.sequence(Actions.moveTo(-bg1.getWidth(), 0, 20.0f), 
                    		Actions.repeat(RepeatAction.FOREVER, Actions.sequence(Actions.moveTo(bg1.getWidth(), 0), 
                    				Actions.moveTo(-bg1.getWidth(), 0, 40.0f)))));
                    
                    bg2.setPosition(bg2.getWidth(), 0);
                    bg2.addAction(Actions.sequence(Actions.moveTo(0, 0, 10.0f), Actions.moveTo(-bg2.getWidth(), 0, 20.0f), 
                    		Actions.repeat(RepeatAction.FOREVER, Actions.sequence(Actions.moveTo(bg1.getWidth(), 0), 
                    				Actions.moveTo(-bg2.getWidth(), 0, 40.0f)))));
                    
                    addActor(bg1);
                    addActor(bg2);
                    addActor(overlay);
                    Text nothing = new Text(comp460game.assetManager, "", 0,0);
                    title = new Text(comp460game.assetManager, "Couple's Therapy", 150, comp460game.CONFIG_HEIGHT, Color.WHITE);
					playOption = new Text(comp460game.assetManager, "PLAY?", 150, comp460game.CONFIG_HEIGHT - 180, Color.WHITE);
                    waitingOnPlayer2 = new Text(comp460game.assetManager, "Waiting on other player...", 150, comp460game.CONFIG_HEIGHT - 180, Color.WHITE);
					//startServerOption = new Text(comp460game.assetManager, "START SERVER?", 150, comp460game.CONFIG_HEIGHT - 240);
					joinServerOption = new Text(comp460game.assetManager, "ENTER IP", 150, comp460game.CONFIG_HEIGHT - 240, Color.WHITE);
                    disconnect = new Text(comp460game.assetManager, "DISCONNECT", 150, comp460game.CONFIG_HEIGHT - 240, Color.WHITE);
					exitOption = new Text(comp460game.assetManager, "EXIT?", 150, comp460game.CONFIG_HEIGHT - 300, Color.WHITE);

					disconnect.setVisible(false);
					waitingOnPlayer2.setVisible(false);
                    waitingOnPlayer2.setScale(0.5f);

					playOption.setScale(0.5f);
                    playOption.addListener(new ClickListener() {
                        public void clicked(InputEvent e, float x, float y) {
                            Log.info("Clicked play button...");
                            if (comp460game.client.client == null || !comp460game.client.client.isConnected()) return;

                            Log.info("Client successfully set");
                            Packets.ReadyToPlay r2p = new Packets.ReadyToPlay();

                            comp460game.client.client.sendTCP(r2p);
                            swap(table, waitingOnPlayer2, playOption);
                            playOption.setVisible(false);
                            waitingOnPlayer2.setVisible(true);
                            isWaiting = true;
                        }
                    });
					joinServerOption.addListener(new ClickListener() {
						public void clicked(InputEvent e, float x, float y) {
							comp460game.client.init(false);

							swap(table, disconnect, joinServerOption);
							disconnect.setVisible(true);
							joinServerOption.setVisible(false);
						}
					});
					joinServerOption.setScale(0.5f);

                    disconnect.addListener(new ClickListener() {
                        public void clicked(InputEvent e, float x, float y) {
                            comp460game.client.client.close();
                            swap(table, joinServerOption, disconnect);
                            disconnect.setVisible(false);
                            joinServerOption.setVisible(true);
                            if (isWaiting) {
                                swap(table, playOption, waitingOnPlayer2);
                                playOption.setVisible(true);
                                waitingOnPlayer2.setVisible(false);
                                isWaiting = false;
                            }
                        }
                    });
                    disconnect.setScale(0.5f);

					exitOption.addListener(new ClickListener() {
						public void clicked(InputEvent e, float x, float y) {
							Gdx.app.exit();
						}
					});
					exitOption.setScale(0.5f);

					addActor(playOption);
                    addActor(joinServerOption);
                    addActor(disconnect);
					addActor(exitOption);
                    addActor(title);

//                    bground.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
//                    this.getBatch().begin();
//                    this.getBatch().draw(bground, 0, 0, 1080,1080);
//                    this.getBatch().end();

                    table.setFillParent(true);
                    addActor(table);
                    //add buttons to table
                    table.add(title).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(nothing).fillX().uniformX().center();
                    table.row();
                    table.add(playOption).fillX().uniformX().center();
                    table.row().pad(10, 0, 10, 0);
                    if (comp460game.client.client == null || !comp460game.client.client.isConnected()) {
                        table.add(joinServerOption).fillX().uniformX().center();
                    } else {
                        table.add(disconnect).fillX().uniformX().center();
                        joinServerOption.setVisible(false);
                        disconnect.setVisible(true);
                    }
                    table.row().pad(10, 0, 10, 0);
                    table.add(exitOption).fillX().uniformX().center();
                    table.row().pad(10, 0, 10, 0);
                    table.add(waitingOnPlayer2).fillX().uniformX().center();
                    table.row().pad(10, 0, 10, 0);
                    if (comp460game.client.client == null || !comp460game.client.client.isConnected()) {
                        table.add(disconnect).fillX().uniformX().center();
                    } else {
                        table.add(joinServerOption).fillX().uniformX().center();
                    }

				}
			};
		} else {
			stage = new Stage() {
				{
                    // https://i.ytimg.com/vi/utpTIOJve-g/maxresdefault.jpg
                    // Source of image
                    bground1 = new Texture("maps/dotBackground.jpg");
                    bground1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                    Actor bg = new Image(bground1);

                    addActor(bg);
					playOption = new Text(comp460game.assetManager, "Server Mode: Waiting for players...", 150, comp460game.CONFIG_HEIGHT - 180, Color.WHITE);
					playOption.setScale(0.5f);
					addActor(playOption);
				}
			};
		}
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

	public void swap(Table old, Actor a, Actor b) {
        Cell<Actor> cellA = old.getCell(a);
        Cell<Actor> cellB = old.getCell(b);

        Actor tempA = cellA.getActor();
        Actor tempB = cellB.getActor();

        cellA.setActor(b);
        cellB.setActor(a);
    }

}
