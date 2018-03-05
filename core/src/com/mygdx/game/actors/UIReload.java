package com.mygdx.game.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
public class UIReload extends A460Actor{

	private Player player;
	private PlayState state;
	
	private TextureAtlas atlas;
	
	private TextureRegion reload, reloadMeter, reloadBar;
	
	private float scale = 0.50f;
	
	public UIReload(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		
		this.atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.UIATLAS.toString());
		this.reload = atlas.findRegion("UI_reload");
		this.reloadMeter = atlas.findRegion("UI_reload_meter");
		this.reloadBar = atlas.findRegion("UI_reload_bar");
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.setProjectionMatrix(state.hud.combined);
		if (player != null) {
			if (comp460game.serverMode && player.player1Data != null && player.player1Data.getCurrentTool().reloading) {
                Vector3 bodyScreenPosition = new Vector3(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
                state.camera.project(bodyScreenPosition);

                float x = bodyScreenPosition.x - reload.getRegionWidth() * scale / 2;
                float y = bodyScreenPosition.y + reload.getRegionHeight() * scale;// + playerNumber.hbHeight * Player.scale / 2;

                float percent = player.player1Data.getCurrentTool().reloadCd /
                        (player.player1Data.getCurrentTool().reloadTime * (1 - player.player1Data.getReloadRate()));

                batch.draw(reloadBar, x + 12, y + 5, reloadBar.getRegionWidth() * scale * percent, reloadBar.getRegionHeight() * scale);
                batch.draw(reload, x, y, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
                batch.draw(reloadMeter, x, y, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
                if (player.player2Data != null && player.player2Data.getCurrentTool().reloading) {
                    Vector3 bodyScreenPosition2 = new Vector3(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
                    state.camera.project(bodyScreenPosition2);

                    float x2 = bodyScreenPosition2.x - reload.getRegionWidth() * scale / 2;
                    float y2 = bodyScreenPosition2.y + reload.getRegionHeight() * scale;// + playerNumber.hbHeight * Player.scale / 2;

                    float percent2 = player.player2Data.getCurrentTool().reloadCd /
                            (player.player2Data.getCurrentTool().reloadTime * (1 - player.player2Data.getReloadRate()));

                    batch.draw(reloadBar, x2 + 12, y2 + 5, reloadBar.getRegionWidth() * scale * percent2, reloadBar.getRegionHeight() * scale);
                    batch.draw(reload, x2, y2, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
                    batch.draw(reloadMeter, x2, y2, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
                }
            } else if (player.playerData != null && player.playerData.getCurrentTool().reloading) {
                Vector3 bodyScreenPosition = new Vector3(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
                state.camera.project(bodyScreenPosition);

                float x = bodyScreenPosition.x - reload.getRegionWidth() * scale / 2;
                float y = bodyScreenPosition.y + reload.getRegionHeight() * scale;// + playerNumber.hbHeight * Player.scale / 2;

                float percent = player.playerData.getCurrentTool().reloadCd /
                        (player.playerData.getCurrentTool().reloadTime * (1 - player.playerData.getReloadRate()));

                batch.draw(reloadBar, x + 12, y + 5, reloadBar.getRegionWidth() * scale * percent, reloadBar.getRegionHeight() * scale);
                batch.draw(reload, x, y, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
                batch.draw(reloadMeter, x, y, reload.getRegionWidth() * scale, reload.getRegionHeight() * scale);
            }
		}
	}

}
