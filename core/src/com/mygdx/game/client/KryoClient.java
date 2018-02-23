package com.mygdx.game.client;

import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.actors.Text;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.manager.GameStateManager.State;
import com.mygdx.game.server.*;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.states.TitleState;
import com.mygdx.game.status.DamageTypes;
//import com.mygdx.game.server.Packets;

import javax.swing.*;

public class KryoClient {

	int portSocket = 25565;
	String ipAddress = "localhost";
	
	public Client client;
    public comp460game myGame;
    public int IDOnServer;
    public static String hostIP, name;

    public static final int timeout = 5000;

    public KryoClient(comp460game myGame) {
        this.myGame = myGame;
	}

	public void init(boolean reconnect) {
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
                //JOptionPane.showConfirmDialog(null, "You have been disconnected from the server.");
                /*myGame.getGsm().removeState(PlayState.class);
                myGame.getGsm().removeState(TitleState.class);
                myGame.getGsm().addState(State.TITLE, null);
                myGame.resetClient();*/
            }

            public void received(Connection c, final Object o) {

                if (o instanceof Packets.PlayerConnect) {
                    Packets.PlayerConnect p = (Packets.PlayerConnect) o;
                }

                else if (o instanceof Packets.EnterPlayState) {
                    final int PNUMBER = ((Packets.EnterPlayState) o).playerNumber;
                    Gdx.app.postRunnable(new Runnable() {
                        public void run() {
                        	myGame.getGsm().player = PNUMBER;
                        	Log.info("Set player number to: " + myGame.getGsm().player);
                            myGame.getGsm().addState(State.PLAY, TitleState.class);
                        }
                    });
                }

                else if (o instanceof Packets.ServerIDMessage) {
                    Packets.ServerIDMessage p = (Packets.ServerIDMessage) o;
                    IDOnServer = p.IDOnServer;
                    myGame.getGsm().player = p.IDOnServer;
                }

                else if (o instanceof Packets.LoadLevel) {
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                PlayState ps = (PlayState) myGame.getGsm().states.peek();
                                Packets.LoadLevel p = (Packets.LoadLevel) o;
                                ps.loadLevel(p.level);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.gameOver) {
                    Packets.gameOver p = (Packets.gameOver) o;
                    final boolean won = p.won;
                    Log.info("Received gameover message");
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState) myGame.getGsm().states.peek();

                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                Actor gameOver;
//                                if (won) {
//                                    gameOver = new Text(comp460game.assetManager, "YOU WON!", 150, comp460game.CONFIG_HEIGHT - 180);
//                                    gameOver.setScale(0.5f);
//                                    gameOver.setVisible(true);
//                                    ps.stage.addActor(gameOver);
//                                } else {
//                                    gameOver = new Text(comp460game.assetManager, "YOU LOST!", 150, comp460game.CONFIG_HEIGHT - 180);
//                                    gameOver.setScale(0.5f);
//                                    gameOver.setVisible(true);
//                                    ps.stage.addActor(gameOver);
//                                }
                                    myGame.getGsm().removeState(PlayState.class);
                                if (won) {
                                    myGame.getGsm().addState(State.VICTORY, TitleState.class);
                                } else {
                                    myGame.getGsm().addState(State.GAMEOVER, TitleState.class);
                                }
                            }
                        });
                    }
                }

                else if (o instanceof Packets.SyncPlayState) {
                    //Log.info("Received Player Entity sync message...");
                    Packets.SyncPlayState p = (Packets.SyncPlayState) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
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
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
//                    while (ps.updating) {}
                        ps.updateEntity(UUID.fromString(p.entityID), p.pos, p.velocity, p.angularVelocity, p.angle);
//                    Log.info("Processed Player Entity sync message!");
                    }
                }

//                else if (o instanceof Packets.SyncHitbox) {
////                    Log.info("Received Hitbox sync message...");
//                    Packets.SyncHitbox p = (Packets.SyncHitbox) o;
//                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
//                    World world = ps.getWorld();
//                    RayHandler rays = ps.getRays();
////                    while (ps.updating) {}
//                    new Hitbox(ps,p.x,p.y,p.width,p.height,p.lifespan,p.dura,p.rest,p.startVelo,p.filter,p.sensor,world, ps.camera, rays);
////                    Log.info("Processed Hitbox sync message!");
//
//                }

//                else if (o instanceof Packets.SyncHitboxImage) {
////                    Log.info("Received HitboxImage sync message...");
//                    Packets.SyncHitboxImage p = (Packets.SyncHitboxImage) o;
//                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
//                    World world = ps.getWorld();
//                    RayHandler rays = ps.getRays();
////                    while (ps.updating) {}
//                    new HitboxImage(ps,p.x,p.y,p.width,p.height,p.lifespan,p.dura,p.rest,p.startVelo,p.filter,p.sensor,world, ps.camera, rays, p.spriteID);
////                    Log.info("Processed HitboxImage sync message!");
//
//                }


                else if (o instanceof Packets.SyncCreateSchmuck) {
                    //Log.info("Received Schmuck creation sync message...");
                    Packets.SyncCreateSchmuck p = (Packets.SyncCreateSchmuck) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        //Log.info("PlayState ready when message received...");
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
//                    while (ps.updating) {}
                        ps.clientCreateSchmuck(p.id, p.w, p.h, p.startX, p.startY, p.entityType);
                    }
//                    Log.info("Processed Schmuck creation sync message!");

                }

                else if (o instanceof Packets.KeyPressOrRelease) {
//                    Packets.KeyPressOrRelease p = (Packets.KeyPressOrRelease) o;
//                    PlayState ps = (PlayState) myGame.getGsm().states.peek();
                    /*if (myGame.getGsm().states.peek() instanceof PlayState) {
                        if (p.message == Input.Keys.W) {
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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
                            if (p.playerID == IDOnServer) {
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

                else if (o instanceof Packets.SetEntityAim) {
                    //Log.info("Received SetEntityAim message");
                    Packets.SetEntityAim sea = (Packets.SetEntityAim) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        ps.setEntityAim(UUID.fromString(sea.uuid), sea.delta, sea.x, sea.y);
                    }
                }

                else if (o instanceof Packets.EntityShoot) {
                    //Log.info("Received EntityShoot message");
                    Packets.EntityShoot sea = (Packets.EntityShoot) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        ps.entityShoot(UUID.fromString(sea.uuid), sea.bulletUUIDs);
                    }
                }

                else if (o instanceof Packets.EntityTakeDamage) {
                    Packets.EntityTakeDamage sea = (Packets.EntityTakeDamage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        Entity e = ps.getEntity(UUID.fromString(sea.uuid));
                        if (e instanceof Schmuck) {
                            Schmuck s = (Schmuck) e;
                            Entity attackerEntity = ps.getEntity(UUID.fromString(sea.attackerUUID));
                            if (attackerEntity instanceof Schmuck) {
                                Schmuck attackerSchmuck = (Schmuck) attackerEntity;
                                s.getBodyData().receiveDamage(sea.damage, new Vector2(0, 0),
                                        attackerSchmuck.getBodyData(), true, DamageTypes.TESTTYPE1);
                            }
                        }
                    }
                }

                else if (o instanceof Packets.EntityAdjustHealth) {
                    /*Packets.EntityAdjustHealth sea = (Packets.EntityAdjustHealth) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        Entity e = ps.getEntity(UUID.fromString(sea.uuid));
                        if (e instanceof Schmuck) {
                            Schmuck s = (Schmuck) e;
                            s.getBodyData().regainHp(sea.adjustAmount);
                        }
                    }*/
                }

                else if (o instanceof Packets.RemoveEntity) {
                    //Log.info("Received RemoveEntity message");
                    Packets.RemoveEntity sea = (Packets.RemoveEntity) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        Entity e = ps.getEntity(UUID.fromString(sea.id));
                        if (e != null) {
                            e.queueDeletion();
                        }
                    }
                }

                else if (o instanceof Packets.DisconnectMessage) {
                    //Log.info("Received DisconnectMessage message");
                    JOptionPane.showMessageDialog(null, "You have been kicked by the server.");
                    Gdx.app.postRunnable(new Runnable() {
                        public void run() {
                            myGame.getGsm().removeState(PlayState.class);
                            myGame.getGsm().addState(State.TITLE, PlayState.class);
                            myGame.resetClient(true);
                        }
                    });
                }
            }
        });

        if (!reconnect) {
            // Request the host from the user.
            String input = (String) JOptionPane.showInputDialog(null, "Host:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE,
                    null, null, "localhost");
            if (input == null || input.trim().length() == 0) System.exit(1);
            hostIP = input.trim();

            // Request the user's name.
            input = (String) JOptionPane.showInputDialog(null, "Name:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE, null,
                    null, "Test");
            if (input == null || input.trim().length() == 0) System.exit(1);
            name = input.trim();
        }


        new Thread("Connect") {
            public void run () {
                try {
                    client.connect(5000, hostIP, portSocket);
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
