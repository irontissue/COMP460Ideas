package com.mygdx.game.client;

import java.io.IOException;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Hitbox;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.manager.GameStateManager.State;
import com.mygdx.game.server.*;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.states.TitleState;
//import com.mygdx.game.server.Packets;

import javax.swing.*;

public class KryoClient {

	int portSocket = 25565;
	String ipAddress = "localhost";
	
	public Client client;
    public comp460game myGame;
    public int myID;

    public static final int timeout = 5000;
    String name;

    public KryoClient(comp460game myGame) {
        this.myGame = myGame;
	}

	public void init() {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        KryoSerialization serialization = new KryoSerialization(kryo);
        this.client = new Client(16384, 2048, serialization);
        client.start();

        registerPackets();

        client.addListener(new Listener() {

            public void connected(Connection c) {
                Packets.PlayerConnect connected = new Packets.PlayerConnect(name);
                client.sendTCP(connected);
            }

            public void disconnected(Connection c) {

            }

            public void received(Connection c, Object o) {

                if (o instanceof Packets.PlayerConnect) {
                    Packets.PlayerConnect p = (Packets.PlayerConnect) o;
                }

                else if (o instanceof Packets.EnterPlayState) {
                    Gdx.app.postRunnable(new Runnable() {
                        public void run() {
                            myGame.getGsm().addState(State.PLAY, TitleState.class);
                        }
                    });
                }

                else if (o instanceof Packets.IDMessage) {
                    Packets.IDMessage p = (Packets.IDMessage) o;
                    myID = p.ID;
                }

                else if (o instanceof Packets.SyncPlayState) {
                    //Log.info("Received Player Entity sync message...");
                    Packets.SyncPlayState p = (Packets.SyncPlayState) o;
                    if (myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        //ps.player.body.setTransform(p.body,p.angle);
                        ps.desiredPlayerAngle = p.angle;
                        ps.desiredPlayerPosition = p.body;
                        ps.needToSetPlayerPos = true;
                    }

                    //Log.info("Processed Player Entity sync message!");
                }

                else if (o instanceof Packets.SyncEntity) {
//                    Log.info("Received Player Entity sync message...");
                    Packets.SyncEntity p = (Packets.SyncEntity) o;
                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
//                    while (ps.updating) {}
                    ps.updateEntity(p.entityID,p.pos,p.velocity,p.angularVelocity,p.angle);
//                    Log.info("Processed Player Entity sync message!");
                }

                else if (o instanceof Packets.SyncHitbox) {
//                    Log.info("Received Hitbox sync message...");
                    Packets.SyncHitbox p = (Packets.SyncHitbox) o;
                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
                    World world = ps.getWorld();
                    RayHandler rays = ps.getRays();
//                    while (ps.updating) {}
                    new Hitbox(ps,p.x,p.y,p.width,p.height,p.lifespan,p.dura,p.rest,p.startVelo,p.filter,p.sensor,world, ps.camera, rays);
//                    Log.info("Processed Hitbox sync message!");

                }

                else if (o instanceof Packets.SyncHitboxImage) {
//                    Log.info("Received HitboxImage sync message...");
                    Packets.SyncHitboxImage p = (Packets.SyncHitboxImage) o;
                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
                    World world = ps.getWorld();
                    RayHandler rays = ps.getRays();
//                    while (ps.updating) {}
                    new HitboxImage(ps,p.x,p.y,p.width,p.height,p.lifespan,p.dura,p.rest,p.startVelo,p.filter,p.sensor,world, ps.camera, rays, p.spriteID);
//                    Log.info("Processed HitboxImage sync message!");

                }


                else if (o instanceof Packets.SyncCreateSchmuck) {
//                    Log.info("Received Schmuck creation sync message...");
                    Packets.SyncCreateSchmuck p = (Packets.SyncCreateSchmuck) o;
                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
                    World world = ps.getWorld();
                    RayHandler rays = ps.getRays();
//                    while (ps.updating) {}
                    new Schmuck(ps, world, ps.camera, rays, p.w, p.h, p.startX, p.startY, p.id);
//                    Log.info("Processed Schmuck creation sync message!");
                }

                else if (o instanceof Packets.KeyPressOrRelease) {
                    Packets.KeyPressOrRelease p = (Packets.KeyPressOrRelease) o;
                    PlayState ps = (PlayState) myGame.getGsm().states.peek();
                    /*if (myGame.getGsm().states.peek() instanceof PlayState) {
                        if (p.message == Input.Keys.W) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.wPressed = true;
                                } else {
                                    ps.player.wPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.wPressed2 = true;
                                    Log.info("W2 pressed");
                                } else {
                                    ps.player.wPressed2 = false;
                                    Log.info("W2 released");

                                }
                            }
                        } else if (p.message == Input.Keys.A) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.aPressed = true;
                                } else {
                                    ps.player.aPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.aPressed2 = true;
                                } else {
                                    ps.player.aPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.S) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.sPressed = true;
                                } else {
                                    ps.player.sPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.sPressed2 = true;
                                } else {
                                    ps.player.sPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.D) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.dPressed = true;
                                } else {
                                    ps.player.dPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.dPressed2 = true;
                                } else {
                                    ps.player.dPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.Q) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.qPressed = true;
                                } else {
                                    ps.player.qPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.qPressed2 = true;
                                } else {
                                    ps.player.qPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.E) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.ePressed = true;
                                } else {
                                    ps.player.ePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.ePressed2 = true;
                                } else {
                                    ps.player.ePressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.SPACE) {
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.spacePressed = true;
                                } else {
                                    ps.player.spacePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.spacePressed = true;
                                } else {
                                    ps.player.spacePressed = false;
                                }
                            }
                        }
                    }*/
                }
            }
        });

        // Request the host from the user.
        String input = (String) JOptionPane.showInputDialog(null, "Host:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE,
                null, null, "localhost");
        if (input == null || input.trim().length() == 0) System.exit(1);
        final String host = input.trim();

        // Request the user's name.
        input = (String)JOptionPane.showInputDialog(null, "Name:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE, null,
                null, "Test");
        if (input == null || input.trim().length() == 0) System.exit(1);
        name = input.trim();



        new Thread("Connect") {
            public void run () {
                try {
                    client.connect(5000, host, portSocket);
                    // Server communication after connection can go here, or in Listener#connected().
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();
    }
	
	private void registerPackets() {
		Kryo kryo = client.getKryo();
        Packets.allPackets(kryo);
    }
}
