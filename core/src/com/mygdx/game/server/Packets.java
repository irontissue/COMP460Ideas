package com.mygdx.game.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.kryo.Kryo;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.states.PlayState;
//import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
//import javafx.stage.Stage;

import java.util.Set;

public class Packets {
	
	public static class PlayerConnect {
		public PlayerConnect() {}
		public PlayerConnect(String m) {
			message = m;
		}
		public String message;
	}
	
	public static class KeyPressOrRelease {
	    public static final int PRESSED = 0;
        public static final int RELEASED = 1;
	    public KeyPressOrRelease() {}
	    public KeyPressOrRelease(int m, int pOrR, int playerID) {
	        message = m;
	        pressOrRelease = pOrR;
	        this.playerID = playerID;
        }
        public int playerID;
		public int message;
	    public int pressOrRelease; //0 = pressed, 1 = released.
	}

    public static class MousePressOrRelease {
        public static final int PRESSED = 0;
        public static final int RELEASED = 1;
        public MousePressOrRelease() {}
        public MousePressOrRelease(int buttonID, int pOrR, int playerID) {
            this.buttonID = buttonID;
            pressOrRelease = pOrR;
            this.playerID = playerID;
        }
        public int playerID;
        public int buttonID;
        public int pressOrRelease; //0 = pressed, 1 = released.
    }

    public static class MouseReposition {
        public MouseReposition() {}
        public MouseReposition(float x, float y, int playerID) {
            this.playerID = playerID;
            this.x = x;
            this.y = y;
        }
        public int playerID;
        public float x, y;
    }

    /*public static class SetEntityAim {
	    public SetEntityAim() {}
	    public SetEntityAim(String uuid, float delta, int x, int y) {
	        this.uuid = uuid;
	        this.delta = delta;
	        this.x = x;
	        this.y = y;
        }
        public String uuid;
	    public float delta;
	    public int x, y;
    }

    public static class EntityShoot {
	    public EntityShoot() {}
	    public EntityShoot(String uuid, String[] bulletUUIDs) {
	        this.uuid = uuid;
	        this.bulletUUIDs = bulletUUIDs;
        }
        public String uuid;
        public String[] bulletUUIDs;
    }*/

    public static class gameOver {
	    public gameOver() {}
	    public gameOver(boolean won) {
	        this.won = won;
        }
        public boolean won;
    }

	//Client to server
	/*public static class Shoot {
	    public static UUID userID;
	    public static int weaponID;
	    public static Vector2 startingVelocity;
	    public static float x, y;
	    public static short filter;

        public Shoot() {}
	    public Shoot(UUID userID, int weaponID, Vector2 startingVelocity, float x, float y, short filter) {
	        this.userID = userID;
	        this.weaponID = weaponID;
	        this.startingVelocity = startingVelocity;
	        this.x = x;
	        this.y = y;
	        this.filter = filter;
        }
    }*/

    //Server to client
    /*public static class ShootSToC {
        public static UUID userID;
        public static int weaponID;
        public static Vector2 startingVelocity;
        public static float x, y;
        public static short filter;

        public Shoot(UUID userID, int weaponID, Vector2 startingVelocity, float x, float y, short filter) {
            this.userID = userID;
            this.weaponID = weaponID;
            this.startingVelocity = startingVelocity;
            this.x = x;
            this.y = y;
            this.filter = filter;
        }
    }*/

	public static class ReadyToPlay {
	    public ReadyToPlay() {}
    }

    public static class ClientLoadedPlayState {
	    public ClientLoadedPlayState() {}
	    public ClientLoadedPlayState(String level) {
	        this.level = level;
        }
        public String level;
    }

    public static class EnterPlayState {
        public EnterPlayState() {}
        public EnterPlayState(int playerNumber) {
            this.playerNumber = playerNumber;
        }
        public int playerNumber;
	}

	public static class ServerIDMessage {
        public ServerIDMessage() {}
        public ServerIDMessage(int IDOnServer) {
            this.IDOnServer = IDOnServer;
        }
        public int IDOnServer;
    }

    public static class SyncPlayState {
	    public SyncPlayState() {}
	    public SyncPlayState(Vector2 bod, float a) {
	        body = bod;
	        angle = a;
        }
        public Vector2 body;
	    public float angle;
    }

    public static class SyncEntity {
        public SyncEntity() {}
        public SyncEntity(String entityID, Vector2 pos, Vector2 vel, float aVel, float a) {
            this.entityID = entityID;
            this.pos = pos;
            this.velocity = vel;
            this.angularVelocity = aVel;
            this.angle = a;
        }
        public String entityID;
        public Vector2 pos;
        public Vector2 velocity;
        public float angularVelocity;
        public float angle;
    }

//    public static class SyncHitbox {
//	    public SyncHitbox() {}
//        public SyncHitbox(float x, float y, int width, int height, float lifespan, int dura, float rest,
//                          Vector2 startVelo, short filter, boolean sensor) {
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//	        this.lifespan = lifespan;
//            this.filter = filter;
//            this.sensor = sensor;
//            this.dura = dura;
//            this.rest = rest;
//            this.startVelo = startVelo;
//        }
//	    public float x, y, lifespan, rest;
//        public int width, height, dura;
//        public Vector2 startVelo;
//        public short filter;
//        public boolean sensor;
//    }

    /**
     * Syncs a hitboxImage. playerDataNumber is which player is shooting the bullet, if at all. 1 means player 1,
     * 2 means player 2, and 0 means something other than the player shot the bullet.
     */
    public static class CreateHitboxImage {
        public CreateHitboxImage() {}
        public CreateHitboxImage(float x, float y, int width, int height, float lifespan, int dura, float rest,
                                 Vector2 startVelo, short filter, boolean sensor, String creatorUUID, String spriteID,
                                 String uuid, int playerDataNumber) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.lifespan = lifespan;
            this.filter = filter;
            this.sensor = sensor;
            this.dura = dura;
            this.rest = rest;
            this.startVelo = startVelo;
            this.spriteID = spriteID;
            this.uuid = uuid;
            this.creatorUUID = creatorUUID;
            this.playerDataNumber = playerDataNumber;
        }
        public float x, y, lifespan, rest;
        public int width, height, dura, playerDataNumber;
        public Vector2 startVelo;
        public short filter;
        public boolean sensor;
        public String spriteID;
        public String uuid, creatorUUID;
    }

    public static class SyncCreateSchmuck {
	    public SyncCreateSchmuck() {}
	    public SyncCreateSchmuck(String id, float w, float h, float startX, float startY, int entityType, boolean synced, int playerNumber) {
	        this.w = w;
	        this.h = h;
	        this.startX = startX;
	        this.startY = startY;
	        this.id = id;
	        this.entityType = entityType;
	        this.synced = synced;
	        this.playerNumber = playerNumber;
        }
	    public float w, h, startX, startY;
	    public String id;
	    public int entityType, playerNumber;
	    public boolean synced;
    }

    public static class RemoveEntity {
	    public RemoveEntity() {}
	    public RemoveEntity(String id) {
	        this.id = id;
        }
        public String id;
    }

    public static class EntityTakeDamage {
	    public EntityTakeDamage() {}
        public EntityTakeDamage(String uuid, float damage, String attackerUUID) {
	        this.uuid = uuid;
	        this.damage = damage;
	        this.attackerUUID = attackerUUID;
        }
        public String uuid, attackerUUID;
	    public float damage;
    }

    public static class PlayerShoot {
        public PlayerShoot() {}
        public PlayerShoot(int playerNumber) {
            this.playerNumber = playerNumber;
        }
        public int playerNumber;
    }

    /**
     * If the adjustAmount is positive, it will increase the entity's HP by that amount.
     */
    public static class EntityAdjustHealth {
	    public EntityAdjustHealth() {}
	    public EntityAdjustHealth(String uuid, float adjustAmount) {
	        this.uuid = uuid;
	        this.adjustAmount = adjustAmount;
        }
        public String uuid;
	    public float adjustAmount;
    }

    public static class DisconnectMessage {
	    public DisconnectMessage() {}
    }

    public static class LoadLevel {
        public LoadLevel() {}
        public LoadLevel(String level) {
            this.level = level;
        }
        public String level;
    }

    /**
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ALL EVENT TRIGGER MESSAGES BELOW THIS.
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     */

    public static class EventInteractMessage {
        public EventInteractMessage() {}
        public EventInteractMessage(String eventID, String entityID, int playerNumber) {
            this.entityID = entityID;
            this.playerNumber = playerNumber;
            this.eventID = eventID;
        }
        public String eventID;
        public String entityID;
        public int playerNumber;
    }

    public static class EventTouchMessage {
        public EventTouchMessage() {}
        public EventTouchMessage(String eventID, String entityID, int playerNumber) {
            this.entityID = entityID;
            this.playerNumber = playerNumber;
            this.eventID = eventID;
        }
        public String eventID;
        public String entityID;
        public int playerNumber;
    }

    public static class EventReleaseMessage {
        public EventReleaseMessage() {}
        public EventReleaseMessage(String eventID, String entityID, int playerNumber) {
            this.entityID = entityID;
            this.eventID = eventID;
            this.playerNumber = playerNumber;
        }
        public String eventID;
        public String entityID;
        public int playerNumber;
    }

    public static class EventActivateMessage {
        public EventActivateMessage() {}
        public EventActivateMessage(String eventID, String activatorID) {
            this.eventID = eventID;
            this.activatorID = activatorID;
        }
        public String eventID;
        public String activatorID;
    }

    /**
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ALL EVENT CREATION MESSAGES BELOW THIS.
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     * ---------------------------------------
     */

    public static class CreateCurrentsMessage {
        public CreateCurrentsMessage() {}
        public CreateCurrentsMessage(int x, int y, int width, int height, Vector2 vec, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.vec = vec;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
        public Vector2 vec;
    }

    public static class CreateDestructibleBlockMessage {
        public CreateDestructibleBlockMessage() {}
        public CreateDestructibleBlockMessage(int x, int y, int width, int height, int hp, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.hp = hp;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
        public int hp;
    }

    public static class CreateDoorMessage {
        public CreateDoorMessage() {}
        public CreateDoorMessage(int x, int y, int width, int height, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
    }

    public static class CreateEntitySpawnerMessage {
        public CreateEntitySpawnerMessage() {}
        public CreateEntitySpawnerMessage(int x, int y, int width, int height, int schmuckID, float interval, int limit, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.schmuckID = schmuckID;
            this.interval = interval;
            this.limit = limit;
        }
        public int x, y, width, height;
        public String entityID;
        public int schmuckID, limit;
        public float interval;
    }

    public static class CreateEquipPickupMessage {
        public CreateEquipPickupMessage() {}
        public CreateEquipPickupMessage(int x, int y, int width, int height, int equipID, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.equipID = equipID;
            this.entityID = entityID;
        }
        public int x, y, width, height, equipID;
        public String entityID;
    }

    public static class CreateInfoFlagMessage {
        public CreateInfoFlagMessage() {}
        public CreateInfoFlagMessage(int x, int y, int width, int height, String text, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.text = text;
        }
        public int x, y, width, height;
        public String entityID;
        public String text;
    }

    public static class CreateLevelWarpMessage {
        public CreateLevelWarpMessage() {}
        public CreateLevelWarpMessage(int x, int y, int width, int height, String level, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.level = level;
        }
        public int x, y, width, height;
        public String entityID;
        public String level;
    }

    public static class CreateMedpakMessage {
        public CreateMedpakMessage() {}
        public CreateMedpakMessage(int x, int y, int width, int height, String entityID, String spawnerID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.spawnerID = spawnerID;
        }
        public int x, y, width, height;
        public String entityID;
        public String spawnerID;
    }

    public static class CreateMedpakSpawnerMessage {
        public CreateMedpakSpawnerMessage() {}
        public CreateMedpakSpawnerMessage(int x, int y, int width, int height, float interval, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.interval = interval;
        }
        public int x, y, width, height;
        public String entityID;
        public float interval;
    }

    public static class CreatePoisonVentMessage {
        public CreatePoisonVentMessage() {}
        public CreatePoisonVentMessage(int x, int y, int width, int height, float dps, boolean startOn, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.dps = dps;
            this.startOn = startOn;
        }
        public int x, y, width, height;
        public String entityID;
        public float dps;
        public boolean startOn;
    }

    public static class CreateSavePointMessage {
        public CreateSavePointMessage() {}
        public CreateSavePointMessage(int x, int y, int width, int height, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
    }

    public static class CreateSpikeTrapMessage {
        public CreateSpikeTrapMessage() {}
        public CreateSpikeTrapMessage(int x, int y, int width, int height, float dps, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.dps = dps;
        }
        public int x, y, width, height;
        public String entityID;
        public float dps;
    }

    public static class CreateSwitchMessage {
        public CreateSwitchMessage() {}
        public CreateSwitchMessage(int x, int y, int width, int height, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
    }

    public static class CreateTargetMessage {
        public CreateTargetMessage() {}
        public CreateTargetMessage(int x, int y, int width, int height, boolean oneTime, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.oneTime = oneTime;
        }
        public int x, y, width, height;
        public String entityID;
        public boolean oneTime;
    }

    public static class CreateTriggerSpawnMessage {
        public CreateTriggerSpawnMessage() {}
        public CreateTriggerSpawnMessage(int x, int y, int width, int height, int schmuckID, int limit, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.schmuckID = schmuckID;
            this.limit = limit;
        }
        public int x, y, width, height;
        public String entityID;
        public int schmuckID, limit;
    }

    public static class CreateUsePortalMessage {
        public CreateUsePortalMessage() {}
        public CreateUsePortalMessage(int x, int y, int width, int height, boolean oneTime, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
            this.oneTime = oneTime;
        }
        public int x, y, width, height;
        public String entityID;
        public boolean oneTime;
    }

    public static class CreateVictoryMessage {
        public CreateVictoryMessage() {}
        public CreateVictoryMessage(int x, int y, int width, int height, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String entityID;
    }
    
    public static class CreateMovingPlatformMessage {
        public CreateMovingPlatformMessage() {}
        public CreateMovingPlatformMessage(int x, int y, int width, int height, float speed, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public float speed;
        public String entityID;
    }
    
    public static class CreateDialogMessage {
        public CreateDialogMessage() {}
        public CreateDialogMessage(int x, int y, int width, int height, String id, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.id = id;
            this.entityID = entityID;
        }
        public int x, y, width, height;
        public String id;
        public String entityID;
    }
    
    public static class CreateUIChangerMessage {
        public CreateUIChangerMessage() {}
        public CreateUIChangerMessage(int x, int y, int width, int height, String types, int changeType, int scoreIncr, float timerIncr, String misc, String entityID) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.types= types;
            this.changeType = changeType;
            this.scoreIncr = scoreIncr;
            this.timerIncr = timerIncr;
            this.misc= misc;
            this.entityID = entityID;
        }
        public String types, misc;
        public int x, y, width, height, changeType, scoreIncr;
        public float timerIncr;
        public String entityID;
    }

    /**
     * REGISTER ALL THE CLASSES FOR KRYO TO SERIALIZE AND SEND
     * @param kryo The kryo object
     */
    public static void allPackets(Kryo kryo) {
        kryo.register(PlayerConnect.class);
        kryo.register(KeyPressOrRelease.class);
        //kryo.register(Shoot.class);
        kryo.register(EnterPlayState.class);
        kryo.register(ReadyToPlay.class);
        kryo.register(ServerIDMessage.class);
        kryo.register(Vector2.class);
        kryo.register(Gun.class);
        kryo.register(RangedWeapon.class);
        kryo.register(Equipment.class);
        kryo.register(PlayState.class);
        kryo.register(SyncPlayState.class);
        kryo.register(Body.class);
//        kryo.register(SyncHitbox.class);
        kryo.register(SyncCreateSchmuck.class);
        kryo.register(CreateHitboxImage.class);
        kryo.register(SyncEntity.class);
        kryo.register(MousePressOrRelease.class);
        kryo.register(MouseReposition.class);
//        kryo.register(SetEntityAim.class);
//        kryo.register(EntityShoot.class);
        kryo.register(ClientLoadedPlayState.class);
        kryo.register(gameOver.class);
        kryo.register(EntityTakeDamage.class);
        kryo.register(EntityAdjustHealth.class);
        kryo.register(LoadLevel.class);
        kryo.register(CreateEquipPickupMessage.class);
        kryo.register(PlayerShoot.class);

        kryo.register(EventInteractMessage.class);
        kryo.register(EventTouchMessage.class);
        kryo.register(EventReleaseMessage.class);
        kryo.register(EventActivateMessage.class);

        kryo.register(CreateCurrentsMessage.class);
        kryo.register(CreateDestructibleBlockMessage.class);
        kryo.register(CreateDoorMessage.class);
        kryo.register(CreateEntitySpawnerMessage.class);
        kryo.register(CreateEquipPickupMessage.class);
        kryo.register(CreateInfoFlagMessage.class);
        kryo.register(CreateLevelWarpMessage.class);
        kryo.register(CreateMedpakMessage.class);
        kryo.register(CreateMedpakSpawnerMessage.class);
        kryo.register(CreatePoisonVentMessage.class);
        kryo.register(CreateSavePointMessage.class);
        kryo.register(CreateSpikeTrapMessage.class);
        kryo.register(CreateSwitchMessage.class);
        kryo.register(CreateTargetMessage.class);
        kryo.register(CreateTriggerSpawnMessage.class);
        kryo.register(CreateUsePortalMessage.class);
        kryo.register(CreateVictoryMessage.class);

        kryo.register(Set.class);
        kryo.register(Entity.class);
        kryo.register(java.util.HashSet.class);
        kryo.register(com.mygdx.game.event.Door.class);
        kryo.register(com.mygdx.game.event.InfoFlag.class);
        kryo.register(com.badlogic.gdx.utils.Array.class);
        kryo.register(com.mygdx.game.event.UsePortal.class);
        kryo.register(Object[].class);
        kryo.register(com.badlogic.gdx.physics.box2d.Fixture.class);

//        kryo.register(Player.class);
//        kryo.register(TiledMap.class);
//        kryo.register(OrthogonalTiledMapRenderer.class);
//        kryo.register(BitmapFont.class);
//        kryo.register(RayHandler.class);
//        kryo.register(Box2DDebugRenderer.class);
//        kryo.register(World.class);
//        kryo.register(Entity.class);
//        kryo.register(Stage.class);
//        kryo.register(GameStateManager.class);
//        kryo.register(SpriteBatch.class);
//        kryo.register(OrthographicCamera.class);
//        kryo.register(com.mygdx.game.comp460game.class);
//        kryo.register(Matrix4.class);
//        kryo.register(float[].class);
//        kryo.register(com.badlogic.gdx.graphics.Mesh.class);
//        kryo.register(com.badlogic.gdx.graphics.glutils.IndexArray.class);

    }
}
