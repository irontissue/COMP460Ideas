package com.mygdx.game.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;

public class UIPlay extends A460Actor{

	private Player player;
	private PlayState state;
	private BitmapFont font;
	
	private TextureAtlas atlas;
	
	private TextureRegion hp, hpLow, hpMissing;
	private Texture main;
	private Array<AtlasRegion> itemNull, itemSelect, itemUnselect;
	
	private float scale = 0.75f;
	private static final int x = 0;
	private static final int y = 0;
	
	private static final float hpCatchup = 0.01f;
	private static final float hpLowThreshold = 0.20f;
	private static final float blinkCd = 0.1f;
	
	private float hpDelayed = 1.0f, hpDelayed2 = 1.0f;
	private boolean blinking = false, blinking2 = false;
	private float blinkCdCount = 0.0f, blinkCdCount2 = 0.0f;
	
	
	public UIPlay(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		this.font = comp460game.SYSTEM_FONT_UI;
		
		this.atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.UIATLAS.toString());
		this.main = comp460game.assetManager.get(AssetList.UIMAIN.toString());
		this.hp = atlas.findRegion("UI_main_healthbar");
		this.hpLow = atlas.findRegion("UI_main_health_low");
		this.hpMissing = atlas.findRegion("UI_main_healthmissing");
		this.itemNull = atlas.findRegions("UI_main_null");
		this.itemSelect = atlas.findRegions("UI_main_selected");
		this.itemUnselect = atlas.findRegions("UI_main_unselected");
		
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.setProjectionMatrix(state.hud.combined);
		if (player != null) {
            if (comp460game.serverMode && player.player1Data != null && player.player2Data != null) {
                //Calc the ratios needed to draw the bars
                float hpRatio = player.player1Data.currentHp / player.player1Data.getMaxHp();
                float hpRatio2 = player.player2Data.currentHp / player.player2Data.getMaxHp();

                //This code makes the hp bar delay work.
                if (hpDelayed > hpRatio) {
                    hpDelayed -= hpCatchup;
                } else {
                    hpDelayed = hpRatio;
                }

                //This code makes the hp bar delay work.
                if (hpDelayed2 > hpRatio2) {
                    hpDelayed2 -= hpCatchup;
                } else {
                    hpDelayed2 = hpRatio2;
                }

                batch.draw(hpMissing, x + 233, y + 78, hp.getRegionWidth() * scale * hpDelayed, hp.getRegionHeight() * scale);
                batch.draw(hp, x + 233, y + 78, hp.getRegionWidth() * scale * hpRatio, hp.getRegionHeight() * scale);
                batch.draw(hpMissing, comp460game.CONFIG_WIDTH - x + 233, y + 78, hp.getRegionWidth() * scale * hpDelayed2, hp.getRegionHeight() * scale);
                batch.draw(hp, comp460game.CONFIG_WIDTH - x + 233, y + 78, hp.getRegionWidth() * scale * hpRatio2, hp.getRegionHeight() * scale);

                //This makes low Hp indicator blink at low health
                if (hpRatio <= hpLowThreshold) {

                    blinkCdCount -= 0.01f;

                    if (blinkCdCount < 0) {
                        blinking = !blinking;
                        blinkCdCount = blinkCd;
                    }
                } else {
                    blinking = false;
                }

                if (blinking) {
                    batch.draw(hpLow, x, y, main.getWidth() * scale, main.getHeight() * scale);
                }

                batch.draw(main, x, y, main.getWidth() * scale, main.getHeight() * scale);

                font.getData().setScale(0.4f);
                font.draw(batch, player.player1Data.currentTool.name, x + 60, y + 130);
                font.getData().setScale(0.8f);
                font.draw(batch, player.player1Data.currentTool.getText(), x + 70, y + 75);

                //This makes low Hp indicator blink at low health
                if (hpRatio2 <= hpLowThreshold) {

                    blinkCdCount2 -= 0.01f;

                    if (blinkCdCount2 < 0) {
                        blinking2 = !blinking2;
                        blinkCdCount2 = blinkCd;
                    }
                } else {
                    blinking2 = false;
                }

                if (blinking2) {
                    batch.draw(hpLow, comp460game.CONFIG_WIDTH - x, y, main.getWidth() * scale, main.getHeight() * scale);
                }

                batch.draw(main, x, y, main.getWidth() * scale, main.getHeight() * scale);
                batch.draw(main, x, y, comp460game.CONFIG_WIDTH - main.getWidth() * scale, main.getHeight() * scale);

                font.getData().setScale(0.4f);
                font.draw(batch, player.player1Data.currentTool.name, x + 60, y + 130);
                font.draw(batch, player.player2Data.currentTool.name, comp460game.CONFIG_WIDTH - x + 60, y + 130);
                font.getData().setScale(0.8f);
                font.draw(batch, player.player1Data.currentTool.getText(), x + 70, y + 75);
                font.draw(batch, player.player2Data.currentTool.getText(), comp460game.CONFIG_WIDTH - x + 70, y + 75);

                for (int i = 0; i < 4; i++) {
                    if (player.player1Data.multitools.length <= i) {
                        batch.draw(itemNull.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                    } else {
                        if (i == player.player1Data.currentSlot) {
                            batch.draw(itemSelect.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                        } else {
                            batch.draw(itemUnselect.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                        }
                    }
                }

                for (int i = 0; i < 4; i++) {
                    if (player.player2Data.multitools.length <= i) {
                        batch.draw(itemNull.get(i), comp460game.CONFIG_WIDTH - x, y, main.getWidth() * scale, main.getHeight() * scale);
                    } else {
                        if (i == player.player2Data.currentSlot) {
                            batch.draw(itemSelect.get(i), comp460game.CONFIG_WIDTH - x, y, main.getWidth() * scale, main.getHeight() * scale);
                        } else {
                            batch.draw(itemUnselect.get(i), comp460game.CONFIG_WIDTH - x, y, main.getWidth() * scale, main.getHeight() * scale);
                        }
                    }
                }
            }

            else if (player.playerData != null) {
                //Calc the ratios needed to draw the bars
                float hpRatio = player.playerData.currentHp / player.playerData.getMaxHp();

                //This code makes the hp bar delay work.
                if (hpDelayed > hpRatio) {
                    hpDelayed -= hpCatchup;
                } else {
                    hpDelayed = hpRatio;
                }

                batch.draw(hpMissing, x + 233, y + 78, hp.getRegionWidth() * scale * hpDelayed, hp.getRegionHeight() * scale);
                batch.draw(hp, x + 233, y + 78, hp.getRegionWidth() * scale * hpRatio, hp.getRegionHeight() * scale);

                //This makes low Hp indicator blink at low health
                if (hpRatio <= hpLowThreshold) {

                    blinkCdCount -= 0.01f;

                    if (blinkCdCount < 0) {
                        blinking = !blinking;
                        blinkCdCount = blinkCd;
                    }
                } else {
                    blinking = false;
                }

                if (blinking) {
                    batch.draw(hpLow, x, y, main.getWidth() * scale, main.getHeight() * scale);
                }

                batch.draw(main, x, y, main.getWidth() * scale, main.getHeight() * scale);

                font.getData().setScale(0.4f);
                font.draw(batch, player.playerData.currentTool.name, x + 60, y + 130);
                font.getData().setScale(0.8f);
                font.draw(batch, player.playerData.currentTool.getText(), x + 70, y + 75);

                for (int i = 0; i < 4; i++) {
                    if (player.playerData.multitools.length <= i) {
                        batch.draw(itemNull.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                    } else {
                        if (i == player.playerData.currentSlot) {
                            batch.draw(itemSelect.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                        } else {
                            batch.draw(itemUnselect.get(i), x, y, main.getWidth() * scale, main.getHeight() * scale);
                        }
                    }
                }
            }
        }
	}

}
