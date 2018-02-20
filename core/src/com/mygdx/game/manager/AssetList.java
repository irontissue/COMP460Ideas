package com.mygdx.game.manager;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public enum AssetList {
	BUTLER_FONT("fonts/butler.fnt", null),
	LEARNING_FONT("fonts/learning_curve.fnt", null),
	FIXEDSYS_FONT("fonts/fixedsys.fnt", null),
	
	PROJ_1("sprites/projectiles.png", Texture.class),
	PROJ_1_ATL("sprites/projectiles.atlas", TextureAtlas.class),
	
	FISH_1("sprites/fish.png", Texture.class),
	FISH_ATL("sprites/fish.atlas", TextureAtlas.class),

    //The following sprite was acquired from:
    //https://opengameart.org/content/animated-top-down-survivor-player
	GUN_DUDE_1("sprites/gun_dude.png", Texture.class),
	BRIDE("sprites/bride.png", Texture.class),
	DRESS("sprites/bride_dress.png", Texture.class),	
	GROOM("sprites/groom.png", Texture.class),
	COMBINED("sprites/combined.png", Texture.class),
	
	EMPTY_HEART("ui/heart_meter.png", Texture.class),
	FULL_HEART("ui/heart_gauge.png", Texture.class),
	UIMAIN("ui/UI_main_overlay.png", Texture.class),
	UI1("ui/UI.png", Texture.class),
	UI2("ui/UI2.png", Texture.class),
	UIATLAS("ui/UI.atlas", TextureAtlas.class);
	
	//Enum constructor and methods.
	private String pathname;
    private Class<?> type;
    
    AssetList(String s, Class<?> c) {
        this.pathname = s;
        this.type = c;
    }

    @Override
    public String toString() {
        return this.pathname;
    }

    public Class<?> getType() { 
    	return type; 
    }
}