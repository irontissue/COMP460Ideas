package com.mygdx.game.equipment;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.HitboxData;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

public class WeaponUtils {

	private static final float selfDamageReduction = 0.4f;
	
	public static Hitbox explode(PlayState state, float x, float y, World world, OrthographicCamera camera, RayHandler rays, 
			final Schmuck user, int explosionRadius, final float explosionDamage, final float explosionKnockback, short filter, int playerDataNumber, boolean synced) {
		Hitbox explosion = new HitboxImage(state, 
				x, y,	explosionRadius, explosionRadius, 0.3f, 1, 0, new Vector2(0, 0),
				filter, true, world, camera, rays, user, "boom", synced, null, playerDataNumber) {
		};

		explosion.setUserData(new HitboxData(state, world, explosion){
			public void onHit(UserData fixB) {
				if (fixB != null) {
					Vector2 kb = new Vector2(fixB.getEntity().getBody().getPosition().x - this.hbox.getBody().getPosition().x,
							fixB.getEntity().getBody().getPosition().y - this.hbox.getBody().getPosition().y);
					
					if (fixB.equals(user.getBodyData())) {
						fixB.receiveDamage(explosionDamage * selfDamageReduction, kb.nor().scl(explosionKnockback), 
								user.getBodyData(), true);
					} else {
						fixB.receiveDamage(explosionDamage, kb.nor().scl(explosionKnockback), 
								user.getBodyData(), true);
					}
				}
			}
		});
		
		return explosion;
	}
}
