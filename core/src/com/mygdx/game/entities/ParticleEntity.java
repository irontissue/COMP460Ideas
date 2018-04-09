package com.mygdx.game.entities;

import static com.mygdx.game.util.Constants.PPM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.userdata.UserData;
import com.mygdx.game.manager.AssetList;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.UserDataTypes;
import com.mygdx.game.util.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class ParticleEntity extends Entity {
    public static final int ENTITY_TYPE = Constants.EntityTypes.PARTICLE_ENTITY;
	
    private static TextureAtlas particleAtlas;
    
    private ParticleEffect effect;
	private Entity attachedEntity;
	private float linger, interval, lifespan;
	private boolean despawn, temp;
	
	public ParticleEntity(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
			float startX, float startY, String effect, float linger, float lifespan, boolean startOn, boolean synced) {
		super(state, world, camera, rays, 0, 0, startX, startY, synced);
		
		particleAtlas = comp460game.assetManager.get(AssetList.PARTICLE_ATLAS.toString());
		
		this.effect = new ParticleEffect();
		this.effect.load(Gdx.files.internal(effect), particleAtlas);
		this.linger = linger;
		
		this.despawn = false;
		temp = lifespan != 0;
		this.lifespan = lifespan;
		
		if (startOn) {
			this.effect.start();
		} else {
			this.effect.allowCompletion();
		}
		
		this.effect.setPosition(startX, startY);
	}

	public ParticleEntity(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
			Entity entity, String effect, float linger, float lifespan, boolean startOn, boolean synced) {
		this(state, world, camera, rays, 0, 0, effect, linger, lifespan, startOn, synced);
		this.attachedEntity = entity;
		this.linger = linger;
	}
	
	public ParticleEntity(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
						  float startX, float startY, ParticleEffect effect, float lifespan, boolean synced, String id) {
		super(state, world, camera, rays, 0, 0, startX, startY, synced, id);
		this.effect = effect;
		this.despawn = false;
		this.lifespan = lifespan;

		effect.start();
	}
	
	public ParticleEntity(PlayState state, World world, OrthographicCamera camera, RayHandler rays,
						  Entity entity, ParticleEffect effect, float lifespan, boolean synced, String id) {
		super(state, world, camera, rays, 0, 0, 0, 0, synced, id);
		this.attachedEntity = entity;
		this.effect = effect;
		this.despawn = false;
		this.lifespan = lifespan;
		effect.start();

	}

	@Override
	public void create() {
	
	}

	@Override
	public void controller(float delta) {
		if (attachedEntity != null) {
			if (attachedEntity.alive) {
				effect.setPosition(attachedEntity.getBody().getPosition().x * PPM, attachedEntity.getBody().getPosition().y * PPM);
			} else {
				despawn = true;
				effect.allowCompletion();
			}
		}
		
		if (despawn) {
			linger -= delta;
			
			if (linger <= 0) {
				this.queueDeletion();
			}
		}

		if (temp) {
			lifespan -= delta;
			
			if (lifespan <= 0) {
				despawn = true;
				effect.allowCompletion();
			}
		}
		
		if (interval > 0) {
			interval -= delta;
			
			if (interval <= 0) {
				effect.allowCompletion();
			}
		}
		
	}

	public void turnOn() {
		if (effect.isComplete()) {
			effect.start();
		}
	}
	
	public void turnOff() {
		effect.allowCompletion();
	}

	public void onForBurst(float duration) {
		turnOn();
		interval = duration;
	}
	
	@Override
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(state.sprite.combined);
		if (effect != null) {
			effect.draw(batch, Gdx.graphics.getDeltaTime());
		}
	}
	
	@Override
	public void dispose() {
		effect.dispose();
		super.dispose();
	}

	public ParticleEffect getEffect() {
		return effect;
	}

	public void setEffect(ParticleEffect effect) {
		this.effect = effect;
	}
}
