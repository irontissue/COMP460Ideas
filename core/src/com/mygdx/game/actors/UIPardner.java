package com.mygdx.game.actors;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Player;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.SteeringUtil;

/**
 * UIMomentum appears in the bottom right screen and displays information about the player's momentum freezing cd and stored momentums
 * @author Zachary Tu
 *
 */
public class UIPardner extends A460Actor{

	private Player player;
	private PlayState state;
	
	private TextureAtlas atlas;
	
	private TextureRegion base;
	private float scale = 0.25f;
	
    private Texture empty, full;

	
	private Player otherPlayer;
	
	
	public UIPardner(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		
		this.atlas = (TextureAtlas) comp460game.assetManager.get(AssetList.UIATLAS.toString());
		this.base = atlas.findRegion("UI_momentum_base");
		
		otherPlayer = state.getPlayer2();
		
		this.empty = new Texture(AssetList.EMPTY_HEART.toString());
		this.full = new Texture(AssetList.FULL_HEART.toString());
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.setProjectionMatrix(state.hud.combined);

		
		float x = 500;
		float y = 500;

		Vector3 playerScreenPosition = new Vector3(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
		state.camera.project(playerScreenPosition);
		
		Vector3 objectiveScreenPosition = new Vector3(otherPlayer.getBody().getPosition().x, otherPlayer.getBody().getPosition().y, 0);
		state.camera.project(objectiveScreenPosition);
		
		float xDist = playerScreenPosition.x - objectiveScreenPosition.x;
		float yDist = playerScreenPosition.y - objectiveScreenPosition.y;
		
		
		
		if (Math.abs(xDist) > comp460game.CONFIG_WIDTH / 2 || Math.abs(yDist) > comp460game.CONFIG_HEIGHT / 2) {
			Vector2 toObjective = new Vector2(xDist, yDist);
			
			float angle = SteeringUtil.vectorToAngle(toObjective);
			float corner = SteeringUtil.vectorToAngle(new Vector2(comp460game.CONFIG_WIDTH, comp460game.CONFIG_HEIGHT));

			if (angle < corner && angle > -(Math.PI + corner)) {
				x = (float) (base.getRegionWidth() * scale);
				y = (float) (comp460game.CONFIG_HEIGHT / 2 + Math.tan(Math.abs(angle) - Math.PI / 2) * (comp460game.CONFIG_WIDTH / 2 - base.getRegionWidth() * scale));
			}
			if (angle > -corner && angle < (Math.PI + corner)) {
				x = (float) (comp460game.CONFIG_WIDTH - base.getRegionWidth() * scale);
				y = (float) (comp460game.CONFIG_HEIGHT / 2 + Math.tan(angle - Math.PI / 2) * (comp460game.CONFIG_WIDTH / 2 - base.getRegionWidth() * scale));
			}
			if (angle <= -corner && angle >= corner) {
				x = (float) (comp460game.CONFIG_WIDTH / 2 + Math.tan(angle) * (comp460game.CONFIG_HEIGHT / 2 - base.getRegionHeight() * scale));
				y = (float) (base.getRegionHeight() * scale);
			}
			if (angle >= (Math.PI + corner) || angle <= -(Math.PI + corner)) {				
				x = (float) (comp460game.CONFIG_WIDTH / 2 + (angle > 0 ? -1 : 1) * Math.tan(Math.abs(angle) - Math.PI) * (comp460game.CONFIG_HEIGHT / 2 - base.getRegionHeight() * scale));
				y = (float) (comp460game.CONFIG_HEIGHT - base.getRegionHeight() * scale);
			}	
			
			float hpRatio = otherPlayer.getBodyData().currentHp / otherPlayer.getBodyData().getMaxHp();

			batch.draw(empty, x - empty.getWidth() / 2, y - empty.getHeight() / 2,
	                empty.getWidth() / 2, empty.getHeight() / 2,
	                empty.getWidth(), empty.getHeight(),
	                1, 1, 0, 0, 0, empty.getWidth(), empty.getHeight(), false, false);

	        batch.draw(full, x - full.getWidth() / 2, y - full.getHeight() / 2 - (int)(full.getHeight() * (1 - hpRatio)),
	                full.getWidth() / 2, full.getHeight() / 2,
	                full.getWidth(), full.getHeight(),
	                1, 1, 0, 0, (int) (full.getHeight() * (1 - hpRatio)),
	                full.getWidth(), full.getHeight(), false, false);
		}
		
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}
