package com.mygdx.game.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.game.entities.Player;
import com.mygdx.game.states.PlayState;

public class HpBar extends A460Actor {

	private Player player;
	private PlayState state;
	
	//The font is for writing text.
    public BitmapFont font;
    
	public HpBar(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		font = new BitmapFont();
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		//Draw player information for temporary ui.
		//Check for null because player is not immediately spawned in a map.
		batch.setProjectionMatrix(state.hud.combined);

		if (player != null) {
			if (player.getPlayerData() != null) {
				font.getData().setScale(8);
				font.draw(batch, " Hp: " + Math.round(player.getPlayerData().currentHp) + "/" + player.getPlayerData().getMaxHp(), 20, 200);
				font.draw(batch, player.getPlayerData().currentTool.getText(), 20, 120);
			}
		}
	}

}
