package com.mygdx.game.util;

/**
 * Constants used throughout the game.
 * @author Zachary Tu
 *
 */
public class Constants {

    public class Filters {
        public static final short BIT_WALL = 1;
        public static final short BIT_PLAYER = 2;
        public static final short BIT_SENSOR = 4;
        public static final short BIT_PROJECTILE = 8;
        public static final short BIT_ENEMY = 16;

        public static final short PLAYER_HITBOX = -1;
        public static final short ENEMY_HITBOX = -2;
    }
	
	//Pixels per Meter. Transitioning between Box2d coordinates and libgdx ones.
	public static final float PPM = 32;

    public class EntityTypes {
        public static final int ENTITY = 0;
        public static final int SCHMUCK = 1;
        public static final int HITBOX = 2;
        public static final int EVENT = 3;
        public static final int PARTICLE_ENTITY = 4;
        public static final int PLAYER = 5;
        public static final int ENEMY = 6;
        public static final int RANGED_ENEMY = 7;
        public static final int HITBOX_IMAGE = 8;
        public static final int RANGED_HITBOX = 9;
        public static final int STEERING_ENEMY = 10;
        public static final int STANDARD_ENEMY = 11;
    }
    
}
