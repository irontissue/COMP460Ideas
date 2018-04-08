package com.mygdx.game.manager;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public enum AssetList {
	BUTLER_FONT("fonts/butler.fnt", null),
	LEARNING_FONT("fonts/learning_curve.fnt", null),
	FIXEDSYS_FONT("fonts/fixedsys.fnt", null),
	
	PROJ_1("sprites/projectiles.png", Texture.class),
	PROJ_1_ATL("sprites/projectiles.atlas", TextureAtlas.class),
	
	FISH_1("sprites/Man Blue/manBlue_gun.png", Texture.class),
	FISH_ATL("sprites/fish.atlas", TextureAtlas.class),

    //The following sprite was acquired from:
    //https://opengameart.org/content/animated-top-down-survivor-player
	GUN_DUDE_1("sprites/gun_dude.png", Texture.class),
    BLACK("images/black.png", Texture.class),

	KENNEY_HITMAN("sprites/Hitman 1/hitman1_gun.png", Texture.class),
	BRIDE("sprites/bride.png", Texture.class),
	DRESS("sprites/bride_dress.png", Texture.class),	
	GROOM("sprites/groom.png", Texture.class),
	COMBINED("sprites/combined.png", Texture.class),
	
	EMPTY_HEART("ui/heart_meter.png", Texture.class),
	FULL_HEART("ui/heart_gauge.png", Texture.class),
	UIMAIN("ui/UI_main_overlay.png", Texture.class),
	UI1("ui/UI.png", Texture.class),
	UI2("ui/UI2.png", Texture.class),
	UIATLAS("ui/UI.atlas", TextureAtlas.class),
	UISKINIMG("ui/uiskin.png", Texture.class),
	UISKINATL("ui/uiskin.atlas", TextureAtlas.class),
	UIPATCHIMG("ui/window.png", Texture.class),
	UIPATCHATL("ui/window.atlas", TextureAtlas.class),
	
    DOOR("Images/TankPack2/PNG/Retina/barricadeWood.png", Texture.class),
    VICTORY("Images/SportsPack/PNG/Equipment/flag_checkered.png", Texture.class),
    TARGET("Images/RacingPack/PNG/Objects/cone_straight.png", Texture.class),
    LEVEL_WARP("sprites/KenneyTileSprites/tile_537.png", Texture.class),
    SPORTS_CHAR_EQUIP("Images/SportsPack/Spritesheet/sheet_charactersEquipment.png", Texture.class),
    SPORTS_CHAR_EQUIP_ATL("Images/SportsPack/Spritesheet/sheet_charactersEquipment.atlas", TextureAtlas.class),
    SPORT_EQUIP("Images/SportsPack/Spritesheet/sheet_equipment.png", Texture.class),
	SPORTS_EQUIP_ATL("Images/SportsPack/Spritesheet/sheet_equipment.atlas", TextureAtlas.class),

    USE_PORTAL("sprites/KenneyTileSprites/tile_535.png", Texture.class),
    SWITCH_OFF("Images/TankPack2/PNG/Retina/barrelRed_top.png", Texture.class),
    SWITCH_ON("Images/TankPack2/PNG/Retina/barrelGreen_top.png", Texture.class),
    CURRENT("Images/RacingPack/PNG/Objects/arrow_yellow.png", Texture.class),
	SPIKE_DOWN("sprites/SpikeDown.png", Texture.class),
	SPIKE_UP("sprites/SpikeUp.png", Texture.class),
    POISON_CLOUD("sprites/PoisonCloud.png", Texture.class),

    GUN("sprites/weapon_gun.png",Texture.class),
    SHOTGUN("sprites/weapon_machine.png",Texture.class),
//    ROCKET("",Texture.class),
//    BOOMERANG("",Texture.class),
    MACHINE("sprites/weapon_silencer.png",Texture.class),

    SFX_BGM("sounds/bgm/Overworld.mp3", Sound.class),
    SFX_BOOMERANG("sounds/weaponSFX/Boomerang/boomerangSound.wav", Sound.class),

    SFX_BEE("sounds/weaponSFX/Beehive/bees.wav", Sound.class),
    SFX_POISON("sounds/weaponSFX/PoisonGun/steam.mp3", Sound.class),

    SFX_BEE_YOW("sounds/weaponSFX/Beehive/yow.mp3", Sound.class),
    SFX_BEE_GDI("sounds/weaponSFX/Beehive/goddamnit.wav", Sound.class),

    SFX_AR("sounds/weaponSFX/AssaultRifle/arSingleShot.mp3", Sound.class),
    SFX_AR_RELOAD("sounds/weaponSFX/AssaultRifle/reloadSlow.mp3", Sound.class),

    SFX_RL("sounds/weaponSFX/RocketLauncher/rocketLaunchTrimmed.mp3", Sound.class),
    SFX_RL_RELOAD("sounds/weaponSFX/Boomerang/boomerangSound.wav", Sound.class),

    SFX_MED("sounds/weaponSFX/MediGun/mediShot.mp3", Sound.class),
    SFX_MED_RELOAD("sounds/weaponSFX/MediGun/reloadSlow.mp3", Sound.class),

    SFX_GUN("sounds/weaponSFX/Gun/arSingleShot.mp3", Sound.class),
    SFX_GUN_RELOAD("sounds/weaponSFX/Gun/reloadSlow.mp3", Sound.class),

    SFX_BB("sounds/weaponSFX/BouncingBlade/BBsound.mp3", Sound.class),
    SFX_BB_RELOAD("sounds/weaponSFX/Boomerang/boomerangSound.wav", Sound.class),

    SFX_SHOTGUN("sounds/weaponSFX/Shotgun/Shotgun trimmed.wav", Sound.class),
    SFX_SHOTGUN_RELOAD("sounds/weaponSFX/Shotgun/Pump Shotgun-SoundBible.com-1653268682 (1).wav", Sound.class);
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