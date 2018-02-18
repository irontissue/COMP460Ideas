package com.mygdx.game.actors;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.game.entities.Player;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;

public class HpBar extends A460Actor {

	private Player player;
	private PlayState state;
	
	//The font is for writing text.
    public BitmapFont font;
    
    private Texture empty, full;
    
	public HpBar(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		font = new BitmapFont();
		this.empty = new Texture(AssetList.EMPTY_HEART.toString());
		this.full = new Texture(AssetList.FULL_HEART.toString());
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		//Draw player information for temporary ui.
		//Check for null because player is not immediately spawned in a map.
		batch.setProjectionMatrix(state.hud.combined);

		if (player != null) {
			if (player.getPlayerData() != null) {
				font.getData().setScale(1.5f);
				font.draw(batch, " Hp: " + Math.round(player.getPlayerData().currentHp) + "/" + player.getPlayerData().getMaxHp(), 100, 80);
				font.draw(batch, player.getPlayerData().currentTool.getText(), 100, 60);
				
				float percent = player.getPlayerData().currentHp / player.getPlayerData().getMaxHp();
				
				batch.draw(empty, 100 - empty.getWidth() / 2, 100 - empty.getHeight() / 2, 
						empty.getWidth() / 2, empty.getHeight() / 2,
						empty.getWidth(), empty.getHeight(),
						1, 1, 0, 0, 0, empty.getWidth(), empty.getHeight(), false, false);
				
				batch.draw(full, 100 - full.getWidth() / 2, 100 - full.getHeight() / 2 - (int)(full.getHeight() * (1 - percent)), 
						full.getWidth() / 2, full.getHeight() / 2,
						full.getWidth(), full.getHeight(),
						1, 1, 0, 0, (int) (full.getHeight() * (1 - percent)),
						full.getWidth(), full.getHeight(), false, false);

			}
		}
	}

}
