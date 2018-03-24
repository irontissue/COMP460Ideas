package com.mygdx.game.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.*;
import com.mygdx.game.event.utility.*;
import com.mygdx.game.states.PlayState;

import box2dLight.RayHandler;

/**
 * This util parses a Tiled file into an in-game map.
 * @author Zachary Tu
 *
 */
public class TiledObjectUtil {
	
	/**
	 * Parses objects to create walls and stuff.
	 * @param world: The Box2d world to add the created walls to.
	 * @param objects: The list of Tiled objects to parse through
	 */
    public static void parseTiledObjectLayer(World world, MapObjects objects) {
        for(MapObject object : objects) {
            Shape shape;

            //Atm, we only parse PolyLines into solid walls
            if(object instanceof PolylineMapObject) {
                shape = createPolyline((PolylineMapObject) object);
            } else {
                continue;
            }

            Body body;
            BodyDef bdef = new BodyDef();
            bdef.type = BodyDef.BodyType.StaticBody;
            body = world.createBody(bdef);
            body.createFixture(shape, 1.0f);
            Filter filter = new Filter();
			filter.categoryBits = (short) (Constants.Filters.BIT_WALL);
			filter.maskBits = (short) (Constants.Filters.BIT_SENSOR | Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY | Constants.Filters.BIT_PROJECTILE);
            body.getFixtureList().get(0).setFilterData(filter);
            shape.dispose();
        }
    }
    
    static Map<String, Event> triggeredEvents = new HashMap<String, Event>();
    static Map<Event, String> triggeringEvents = new HashMap<Event, String>();
    static Map<TriggerMulti, String> multiTriggeringEvents = new HashMap<TriggerMulti, String>();
    static Map<TriggerCond, String> condTriggeringEvents = new HashMap<TriggerCond, String>();
    static Map<TriggerRedirect, String> redirectTriggeringEvents = new HashMap<TriggerRedirect, String>();
    static Map<MovingPlatform, String> platformConnections = new HashMap<MovingPlatform, String>();

    /**
     * Parses Tiled objects into in game events
     * @param state: Current GameState
	 * @param world: The Box2d world to add the created events to.
     * @param camera: The camera to pass to the created events.
     * @param rays: The rayhandler to pass to the created events.
     * @param objects: The list of Tiled objects to parse into events.
     */
    public static void parseTiledEventLayer(PlayState state, World world, OrthographicCamera camera, RayHandler rays, MapObjects objects) {
    	for(MapObject object : objects) {
    		
    		//atm, all events are just rectangles.
    		RectangleMapObject current = (RectangleMapObject)object;
			Rectangle rect = current.getRectangle();
			
			Event e = null;
			
			if (object.getName().equals("Spawn") && comp460game.serverMode) {
    			e = new EntitySpawner(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), object.getProperties().get("id", int.class), 
    					object.getProperties().get("interval", float.class), object.getProperties().get("limit", int.class), false);
    		}
			if (object.getName().equals("Current")) {
    			Vector2 power = new Vector2(object.getProperties().get("currentX", float.class), object.getProperties().get("currentY", float.class));
    			e = new Currents(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), power, false);
    		}
			if (object.getName().equals("Equip")) {
    			e = new EquipPickup(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("equipId", int.class), false);
    		}
			if (object.getName().equals("Door")) {
				e = new Door(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), false);
    		}
			if (object.getName().equals("Text")) {
    			e = new InfoFlag(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("text", String.class), false);
    		}
    		if (object.getName().equals("Switch")) {
    			e = new Switch(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), false);
      		}
    		
    		if (object.getName().equals("Sensor")) {
    			e = new Sensor(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("oneTime", boolean.class), false);
    		}
    		if (object.getName().equals("Timer")) {
    			e = new Timer(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("interval", float.class), object.getProperties().get("limit", int.class),
    					object.getProperties().get("startOn", true, boolean.class), false);
    		}
    		if (object.getName().equals("Dialog")) {
    			e = new Radio(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("id", String.class));
    		}
    		if (object.getName().equals("Target")) {
    			e = new Target(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("oneTime", boolean.class), false);
    		}
    		
    		if (object.getName().equals("Counter")) {
    			e = new Counter(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("count", int.class), object.getProperties().get("countStart", 0, int.class), false);
    		}
    		if (object.getName().equals("Multitrigger")) {
    			e = new TriggerMulti(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), false);
    			multiTriggeringEvents.put((TriggerMulti)e, object.getProperties().get("triggeringId", "", String.class));
    		}
    		if (object.getName().equals("TriggerSpawn") && comp460game.serverMode) {
    			e = new TriggerSpawn(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), object.getProperties().get("enemyId", int.class), 
    					object.getProperties().get("limit", int.class), false);
    		}
    		if (object.getName().equals("UsePortal")) {
    			
    			e = new UsePortal(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("oneTime", boolean.class), false);
    		}
    		if (object.getName().equals("Victory")) {
    			e = new Victory(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), false);
    		}
    		if (object.getName().equals("Destr_Obj")) {
    			e = new DestructibleBlock(state, world, camera, rays, (int)rect.width, (int)rect.height,
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("Hp", Integer.class), false);
    		}
    		if (object.getName().equals("Save")) {
    			e = new SavePoint(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), false);
    		}
    		if (object.getName().equals("Warp")) {
    			e = new LevelWarp(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("Level", String.class), false);
    		}
    		if (object.getName().equals("Medpak")) {
    			e = new MedpakSpawner(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2),
    					object.getProperties().get("interval", float.class), false);
    		}
    		if (object.getName().equals("Poison")) {
    			e = new PoisonVent(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2),
    					object.getProperties().get("damage", float.class), object.getProperties().get("startOn", true, boolean.class), false);
    		}
    		if (object.getName().equals("Spike")) {
    			e = new SpikeTrap(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2),
    					object.getProperties().get("damage", float.class), false);
    		}
    		if (object.getName().equals("Condtrigger")) {
    			e = new TriggerCond(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("start", "", String.class));
    			condTriggeringEvents.put((TriggerCond)e, object.getProperties().get("triggeringId", "", String.class));
    		}
    		if (object.getName().equals("Alttrigger")) {
    			e = new TriggerAlt(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), object.getProperties().get("message","", String.class));
    		}
    		if (object.getName().equals("Redirecttrigger")) {
    			e = new TriggerRedirect(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2));
    			redirectTriggeringEvents.put((TriggerRedirect)e, object.getProperties().get("blameId", "", String.class));
    		}
    		if (object.getName().equals("Dummy")) {
    			e = new PositionDummy(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2));
    		}
    		if (object.getName().equals("Platform")) {
    			e = new MovingPlatform(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2), 
    					object.getProperties().get("speed", 1.0f, float.class));
    			platformConnections.put((MovingPlatform)e, object.getProperties().get("connections", "", String.class));
    		}
    		if (object.getName().equals("UI")) {
    			e = new UIChanger(state, world, camera, rays, (int)rect.width, (int)rect.height, 
    					(int)(rect.x + rect.width / 2), (int)(rect.y + rect.height / 2),
    					object.getProperties().get("tags", String.class),
    					object.getProperties().get("change", 0, Integer.class),
    					object.getProperties().get("score", 0, Integer.class),
    					object.getProperties().get("timer", 0.0f, float.class),
    					object.getProperties().get("misc", "", String.class));
    		}
    		
    		if (e != null) {
    			triggeringEvents.put(e, object.getProperties().get("triggeringId", "", String.class));
				triggeredEvents.put(object.getProperties().get("triggeredId", "", String.class), e);
//				triggeredEvents.put(object.getProperties().get("id", "", String.class), e);
    		}
    	}
    }

    public static void parseTiledTriggerLayer(PlayState state, World world, OrthographicCamera camera, RayHandler rays) {
    	for (Event key : triggeringEvents.keySet()) {
    		if (!triggeringEvents.get(key).equals("")) {
    			key.setConnectedEvent(triggeredEvents.getOrDefault(triggeringEvents.get(key), null));
    		}
    	}
    	
    	for (TriggerMulti key : multiTriggeringEvents.keySet()) {
    		for (String id : multiTriggeringEvents.get(key).split(",")) {
    			if (!id.equals("")) {
    				key.addTrigger(triggeredEvents.getOrDefault(id, null));
    			}
    		}
    	}
    	for (TriggerCond key : condTriggeringEvents.keySet()) {
    		for (String id : condTriggeringEvents.get(key).split(",")) {
    			if (!id.equals("")) {
    				key.addTrigger(id, triggeredEvents.getOrDefault(id, null));
    			}
    		}
    	}
    	for (TriggerRedirect key : redirectTriggeringEvents.keySet()) {
    		if (!redirectTriggeringEvents.get(key).equals("")) {
        		key.setBlame(triggeredEvents.getOrDefault(redirectTriggeringEvents.get(key), null));
    		}
    	}
    	for (MovingPlatform key : platformConnections.keySet()) {
    		for (String id : platformConnections.get(key).split(",")) {
    			if (!id.equals("")) {
        			key.addConnection(triggeredEvents.getOrDefault(id, null));
    			}
    		}
    	}
    }
    
    
    
    /**
     * Helper function for parseTiledObjectLayer that creates line bodies
     * @param polyline: Tiled map object
     * @return Box2d body
     */
    private static ChainShape createPolyline(PolylineMapObject polyline) {
        float[] vertices = polyline.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length / 2];
        
        
        for(int i = 0; i < worldVertices.length; i++) {
            worldVertices[i] = new Vector2(vertices[i * 2] / Constants.PPM, vertices[i * 2 + 1] / Constants.PPM);
        }
        ChainShape cs = new ChainShape();
        cs.createChain(worldVertices);
        return cs;
    }
}