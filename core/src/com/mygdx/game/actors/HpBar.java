package com.mygdx.game.actors;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.game.comp460game;
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
		//Draw playerNumber information for temporary ui.
		//Check for null because playerNumber is not immediately spawned in a map.
		batch.setProjectionMatrix(state.hud.combined);

		if (player != null) {
			if (comp460game.serverMode && player.player1Data != null && player.player2Data != null) {
                font.getData().setScale(1.5f);
                font.draw(batch, " Hp: " + Math.round(player.player1Data.currentHp) + "/" +
                        player.player1Data.getMaxHp(), 100, 80);
                font.draw(batch, " Hp 2: " + Math.round(player.player2Data.currentHp) + "/" +
                        player.player2Data.getMaxHp(), comp460game.CONFIG_WIDTH - 100, 80);
                font.draw(batch, player.player1Data.currentTool.getText(), 100, 60);
                font.draw(batch, player.player2Data.currentTool.getText(), comp460game.CONFIG_WIDTH - 100, 60);

                float percent = player.player1Data.currentHp / player.player1Data.getMaxHp();
                float percent2 = player.player2Data.currentHp / player.player2Data.getMaxHp();

                batch.draw(empty, 100 - empty.getWidth() / 2, 100 - empty.getHeight() / 2,
                        empty.getWidth() / 2, empty.getHeight() / 2,
                        empty.getWidth(), empty.getHeight(),
                        1, 1, 0, 0, 0, empty.getWidth(), empty.getHeight(), false, false);

                batch.draw(full, 100 - full.getWidth() / 2, 100 - full.getHeight() / 2 - (int)(full.getHeight() * (1 - percent)),
                        full.getWidth() / 2, full.getHeight() / 2,
                        full.getWidth(), full.getHeight(),
                        1, 1, 0, 0, (int) (full.getHeight() * (1 - percent)),
                        full.getWidth(), full.getHeight(), false, false);

                batch.draw(empty, comp460game.CONFIG_WIDTH - 100 - empty.getWidth() / 2, 100 - empty.getHeight() / 2,
                        empty.getWidth() / 2, empty.getHeight() / 2,
                        empty.getWidth(), empty.getHeight(),
                        1, 1, 0, 0, 0, empty.getWidth(), empty.getHeight(), false, false);

                batch.draw(full, comp460game.CONFIG_WIDTH - 100 - full.getWidth() / 2, 100 - full.getHeight() / 2 - (int)(full.getHeight() * (1 - percent2)),
                        full.getWidth() / 2, full.getHeight() / 2,
                        full.getWidth(), full.getHeight(),
                        1, 1, 0, 0, (int) (full.getHeight() * (1 - percent2)),
                        full.getWidth(), full.getHeight(), false, false);

			} else if (player.playerData != null) {
                font.getData().setScale(1.5f);
                font.draw(batch, " Hp: " + Math.round(player.playerData.currentHp) + "/" + player.playerData.getMaxHp(), 100, 80);
                font.draw(batch, player.playerData.currentTool.getText(), 100, 60);

                float percent = player.playerData.currentHp / player.playerData.getMaxHp();

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
