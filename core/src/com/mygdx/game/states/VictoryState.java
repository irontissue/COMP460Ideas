package com.mygdx.game.states;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.client.KryoClient;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.comp460game;
import com.mygdx.game.actors.Text;

public class VictoryState extends GameState {

	private Stage stage;
	
	//Temporary links to other modules for testing.
	private Actor playOption, title;
    Texture bground;

	public VictoryState(GameStateManager gsm) {
		super(gsm);
	}

	@Override
	public void show() {
		stage = new Stage() {
			{
				// https://i.ytimg.com/vi/utpTIOJve-g/maxresdefault.jpg
                // Source of image
                bground = new Texture("maps/dotBackground.jpg");
                bground.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                Actor bg = new Image(bground);
                addActor(bg);

				playOption = new Text(comp460game.assetManager, "CLICK HERE TO RETURN TO TITLE SCREEN", 150, comp460game.CONFIG_HEIGHT - 180, Color.WHITE);

				playOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	gsm.removeState(VictoryState.class);
			        	gsm.removeState(TitleState.class);
                        gsm.addState(GameStateManager.State.TITLE, null);
			        }
			    });
				playOption.setScale(0.5f);

				addActor(playOption);
				addActor(new Text(comp460game.assetManager, "VICTORY!", 150, comp460game.CONFIG_HEIGHT - 300, Color.WHITE));
			}
		};
		app.newMenu(stage);

        if (comp460game.serverMode) {
            gsm.removeState(VictoryState.class);
            gsm.removeState(TitleState.class);
            gsm.addState(GameStateManager.State.TITLE, null);
        }
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
