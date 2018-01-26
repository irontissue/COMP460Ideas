package com.mygdx.game.server;

import box2dLight.RayHandler;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Player;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.states.PlayState;
//import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
//import javafx.stage.Stage;

public class Packets {
	
	public static class Packet01Message {
		public Packet01Message() {}
		public Packet01Message(String m) {
			message = m;
		}
		public String message;
	}
	
	public static class Packet02Input {
	    public static final int PRESSED = 0;
        public static final int RELEASED = 1;
	    public Packet02Input() {}
	    public Packet02Input(int m, int pOrR, int playerID) {
	        message = m;
	        pressOrRelease = pOrR;
	        this.playerID = playerID;
        }
        public int playerID;
		public int message;
	    public int pressOrRelease; //0 = pressed, 1 = released.
	}
	
	public static class Packet03Click {
		public Packet03Click() {}
		public Packet03Click(Vector2 loc, Equipment tool, int id, float d) {
		    location = loc;
		    usedTool = tool;
		    playerID = id;
		    delta = d;
        }
        public int playerID;
        public Vector2 location;
		public Equipment usedTool;
		public float delta;
	}

	public static class PacketReadyToPlay {
	    public PacketReadyToPlay() {}
    }

	public static class Packet04EnterPlayState {
        public Packet04EnterPlayState() {}
	}

	public static class IDMessage {
        public IDMessage() {}
        public IDMessage(int ID, boolean mas) {
            this.ID = ID;
            master = mas;
        }
        public int ID;
        public boolean master;
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

    public static void allPackets(Kryo kryo) {
        kryo.register(Packets.Packet01Message.class);
        kryo.register(Packets.Packet02Input.class);
        kryo.register(Packets.Packet03Click.class);
        kryo.register(Packets.Packet04EnterPlayState.class);
        kryo.register(Packets.PacketReadyToPlay.class);
        kryo.register(Packets.IDMessage.class);
        kryo.register(Vector2.class);
        kryo.register(Gun.class);
        kryo.register(RangedWeapon.class);
        kryo.register(Equipment.class);
        kryo.register(PlayState.class);
        kryo.register(SyncPlayState.class);
        kryo.register(Body.class);

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
