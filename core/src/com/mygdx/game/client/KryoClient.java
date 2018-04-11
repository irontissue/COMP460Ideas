package com.mygdx.game.client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.HitboxImage;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.Schmuck;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.equipment.RangedWeapon;
import com.mygdx.game.event.*;
import com.mygdx.game.event.utility.Switch;
import com.mygdx.game.event.utility.Target;
import com.mygdx.game.event.utility.UIChanger;
import com.mygdx.game.manager.AssetList;
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

    public LinkedList<Sound> last100;
    public UUID[] playerUUIDs = {null,null};

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

        last100 = new LinkedList<Sound>();

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
                            if (myGame.getGsm().states.peek() instanceof PlayState) {
                                Player p = ((PlayState) (myGame.getGsm().states.peek())).player;
                                Player p2 = ((PlayState) (myGame.getGsm().states.peek())).player2;
                                myGame.getGsm().playerNumber = PNUMBER;
                                Log.info("Set playerNumber number to: " + myGame.getGsm().playerNumber);
                                myGame.getGsm().removeState(PlayState.class);
                                myGame.getGsm().addPlayState(null, p.playerData, p2.playerData, TitleState.class);
                            } else if (myGame.getGsm().states.peek() instanceof TitleState) {
                                myGame.getGsm().playerNumber = PNUMBER;
                                Log.info("Set playerNumber number to: " + myGame.getGsm().playerNumber);
                                myGame.getGsm().removeState(TitleState.class);
                                myGame.getGsm().addState(State.PLAY, TitleState.class);
                            }
                        }
                    });
                }

                else if (o instanceof Packets.ServerIDMessage) {
                    Packets.ServerIDMessage p = (Packets.ServerIDMessage) o;
                    IDOnServer = p.IDOnServer;
                    myGame.getGsm().playerNumber = p.IDOnServer;
                }

                else if (o instanceof Packets.PlaySound) {
                    Packets.PlaySound p = (Packets.PlaySound) o;
                    Sound sound = Gdx.audio.newSound(Gdx.files.internal(p.name));
                    sound.play(p.volume);

                    last100.add(sound);
                    if (last100.size() > 25) {
                        last100.get(0).dispose();
                        last100.remove(0);
                    }
                }

                else if (o instanceof Packets.LoadLevel) {
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                PlayState ps = (PlayState) myGame.getGsm().states.peek();
                                Packets.LoadLevel p = (Packets.LoadLevel) o;
                                ps.loadLevel(p.level);
                                Log.info("Client received loadlevel. Level = " + p.level);
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
                                ps.won = won;
                                ps.gameend();
                            }
                        });
                    }
                }

                else if (o instanceof Packets.SyncPlayState) {
                    //Log.info("Received Player Entity sync message...");
                    Packets.SyncPlayState p = (Packets.SyncPlayState) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        //ps.playerNumber.body.setTransform(p.body,p.angle);
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

                else if (o instanceof Packets.CreateHitboxImage) {
//                    Log.info("Received HitboxImage sync message...");
                    Packets.CreateHitboxImage p = (Packets.CreateHitboxImage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        new HitboxImage(ps, p.x, p.y, p.width, p.height, p.lifespan, p.dura, p.rest, p.startVelo,
                                p.filter, p.sensor, ps.getWorld(), ps.camera, ps.getRays(),
                                (Schmuck)ps.getEntity(UUID.fromString(p.creatorUUID)), p.spriteID, false, p.uuid, p.playerDataNumber);
//                        Log.info("Received CreateHitboxImage. player number = " + p.playerDataNumber);
                    }

                }

                else if (o instanceof Packets.PlayerShoot) {
//                    Log.info("Received HitboxImage sync message...");
                    Packets.PlayerShoot p = (Packets.PlayerShoot) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        if (ps.player.playerData != null && p.playerNumber == myGame.getGsm().playerNumber) {
                            RangedWeapon rw = (RangedWeapon) ps.player.playerData.getCurrentTool();
                            rw.clipLeft--;
                            rw.checkReload();
                        }
                    }

                }

                else if (o instanceof Packets.CreateCurrentsMessage) {
                    final Packets.CreateCurrentsMessage p = (Packets.CreateCurrentsMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Currents(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.vec, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateDestructibleBlockMessage) {
                    final Packets.CreateDestructibleBlockMessage p = (Packets.CreateDestructibleBlockMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new DestructibleBlock(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.hp, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateDoorMessage) {
                    final Packets.CreateDoorMessage p = (Packets.CreateDoorMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Door(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateEquipPickupMessage) {
                    final Packets.CreateEquipPickupMessage p = (Packets.CreateEquipPickupMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new EquipPickup(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.equipID, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateInfoFlagMessage) {
                    final Packets.CreateInfoFlagMessage p = (Packets.CreateInfoFlagMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new InfoFlag(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.text, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateLevelWarpMessage) {
                    final Packets.CreateLevelWarpMessage p = (Packets.CreateLevelWarpMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new LevelWarp(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.level, false, p.entityID);
                                //Log.info("LEVEL WARP CREATED ON CLIENT TO " + p.level + ", ENTITYID = " + p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateMedpakMessage) {
                    final Packets.CreateMedpakMessage p = (Packets.CreateMedpakMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Medpak(ps, ps.getWorld(), ps.camera, ps.getRays(), p.x, p.y, null, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreatePoisonVentMessage) {
                    final Packets.CreatePoisonVentMessage p = (Packets.CreatePoisonVentMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new PoisonVent(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.dps, p.startOn, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateSavePointMessage) {
                    final Packets.CreateSavePointMessage p = (Packets.CreateSavePointMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new SavePoint(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateSpikeTrapMessage) {
                    final Packets.CreateSpikeTrapMessage p = (Packets.CreateSpikeTrapMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new SpikeTrap(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.dps, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateSwitchMessage) {
                    final Packets.CreateSwitchMessage p = (Packets.CreateSwitchMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Switch(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateTargetMessage) {
                    final Packets.CreateTargetMessage p = (Packets.CreateTargetMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Target(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.oneTime, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateUsePortalMessage) {
                    final Packets.CreateUsePortalMessage p = (Packets.CreateUsePortalMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new UsePortal(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.oneTime, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateVictoryMessage) {
                    final Packets.CreateVictoryMessage p = (Packets.CreateVictoryMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Victory(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, false, p.entityID);
                            }
                        });
                    }
                }

                else if (o instanceof Packets.CreateMovingPlatformMessage) {
                    final Packets.CreateMovingPlatformMessage p = (Packets.CreateMovingPlatformMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new MovingPlatform(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.speed, p.entityID);
                            }
                        });
                    }
                }
                
                else if (o instanceof Packets.CreateUIChangerMessage) {
                    final Packets.CreateUIChangerMessage p = (Packets.CreateUIChangerMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new UIChanger(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.types, p.changeType, p.scoreIncr, p.timerIncr, p.misc, p.entityID);
                            }
                        });
                    }
                }
                
                else if (o instanceof Packets.CreateDialogMessage) {
                    final Packets.CreateDialogMessage p = (Packets.CreateDialogMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        final PlayState ps = (PlayState)myGame.getGsm().states.peek();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                new Radio(ps, ps.getWorld(), ps.camera, ps.getRays(), p.width, p.height, p.x, p.y, p.id, p.entityID);
                            }
                        });
                    }
                }
                
                else if (o instanceof Packets.EventInteractMessage) {
                    final Packets.EventInteractMessage p = (Packets.EventInteractMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                PlayState ps = (PlayState) myGame.getGsm().states.peek();
                                Event e = (Event) ps.getEntity(UUID.fromString(p.eventID));
                                Entity ent = ps.getEntity(UUID.fromString(p.entityID));
                                System.out.print("EVENT INTERACT ID = " + p.eventID);
                                System.out.println(", TYPE: " + e.getClass());
                                System.out.println("EVENT INTERACT ENTITY TYPE: " + ent.getClass());
                                if (ent != null && ent instanceof Player) {
                                    if (e != null) {
                                        /*if (myGame.getGsm().playerNumber == p.playerNumber) {
                                            e.eventData.onInteract(ps.player);
                                        } else {
                                            e.eventData.onInteract(ps.player2);
                                        }*/
                                        e.eventData.onInteract((Player) ent);
                                    }
                                }
                            }
                        });
                    }
                }

                else if (o instanceof Packets.EventActivateMessage) {
                    final Packets.EventActivateMessage p = (Packets.EventActivateMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                PlayState ps = (PlayState) myGame.getGsm().states.peek();
                                Event e = (Event) ps.getEntity(UUID.fromString(p.eventID));
                                Event activator = (Event) ps.getEntity(UUID.fromString(p.activatorID));
                                if (activator == null) {
                                    e.eventData.onActivate(null);
                                } else if (e != null) {
                                    e.eventData.onActivate(activator.eventData);
                                }
                            }
                        });
                    }
                }

                else if (o instanceof Packets.EventReleaseMessage) {
                    final Packets.EventReleaseMessage p = (Packets.EventReleaseMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                PlayState ps = (PlayState)myGame.getGsm().states.peek();
                                Event e = (Event) ps.getEntity(UUID.fromString(p.eventID));
                                Entity ent = ps.getEntity(UUID.fromString(p.entityID));
                                if (ent != null && ent instanceof Player) {
                                    if (e != null) {
                                        /*if (myGame.getGsm().playerNumber == p.playerNumber) {
                                            e.eventData.onRelease(((Player) ent).playerData);
                                        } else {
                                            e.eventData.onRelease(((Player) ent).playerData);
                                        }*/
                                        e.eventData.onRelease(((Player) ent).playerData);
                                    }
                                }
                            }
                        });
                    }
                }

                else if (o instanceof Packets.EventTouchMessage) {
                    final Packets.EventTouchMessage p = (Packets.EventTouchMessage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                PlayState ps = (PlayState) myGame.getGsm().states.peek();
                                Event e = (Event) ps.getEntity(UUID.fromString(p.eventID));
                                Entity ent = ps.getEntity(UUID.fromString(p.entityID));
                                if (ent != null && ent instanceof Player) {
                                    if (/*myGame.getGsm().playerNumber == p.playerNumber &&*/ e != null) {
                                        e.eventData.onTouch(((Player) ent).playerData);
                                    }
                                }
                            }
                        });
                    }
                }

                else if (o instanceof Packets.SyncCreateSchmuck) {
                    //Log.info("Received Schmuck creation sync message...");
                    Packets.SyncCreateSchmuck p = (Packets.SyncCreateSchmuck) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        //Log.info("PlayState ready when message received...");
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
//                    while (ps.updating) {}
                        ps.clientCreateSchmuck(p.id, p.w, p.h, p.startX, p.startY, p.entityType, p.synced, p.playerNumber);
                    } else {
                        Log.info("Tossing SyncCreateSchmuck message");
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
                                    ps.playerNumber.wPressed = true;
                                } else {
                                    ps.playerNumber.wPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.wPressed2 = true;
                                    Log.info("W2 pressed");
                                } else {
                                    ps.playerNumber.wPressed2 = false;
                                    Log.info("W2 released");

                                }
                            }
                        } else if (p.message == Input.Keys.A) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.aPressed = true;
                                } else {
                                    ps.playerNumber.aPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.aPressed2 = true;
                                } else {
                                    ps.playerNumber.aPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.S) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.sPressed = true;
                                } else {
                                    ps.playerNumber.sPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.sPressed2 = true;
                                } else {
                                    ps.playerNumber.sPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.D) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.dPressed = true;
                                } else {
                                    ps.playerNumber.dPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.dPressed2 = true;
                                } else {
                                    ps.playerNumber.dPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.Q) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.qPressed = true;
                                } else {
                                    ps.playerNumber.qPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.qPressed2 = true;
                                } else {
                                    ps.playerNumber.qPressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.E) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.ePressed = true;
                                } else {
                                    ps.playerNumber.ePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.ePressed2 = true;
                                } else {
                                    ps.playerNumber.ePressed2 = false;
                                }
                            }
                        } else if (p.message == Input.Keys.SPACE) {
                            if (p.playerID == IDOnServer) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.spacePressed = true;
                                } else {
                                    ps.playerNumber.spacePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.playerNumber.spacePressed = true;
                                } else {
                                    ps.playerNumber.spacePressed = false;
                                }
                            }
                        }
                    }*/
                }

                /*else if (o instanceof Packets.SetEntityAim) {
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
                }*/

                else if (o instanceof Packets.EntityTakeDamage) {
                    Packets.EntityTakeDamage sea = (Packets.EntityTakeDamage) o;
                    if (!myGame.getGsm().states.empty() && myGame.getGsm().states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) myGame.getGsm().states.peek();
                        Entity e = ps.getEntity(UUID.fromString(sea.uuid));
                        Entity attackerEntity = null;
                        if (sea.attackerUUID != null) {
                            attackerEntity = ps.getEntity(UUID.fromString(sea.attackerUUID));
                        }
                        if (e != null && e instanceof Schmuck) {
                            Schmuck s = (Schmuck) e;
                            CharacterData theData = null;
                            if (attackerEntity != null && attackerEntity instanceof Schmuck) {
                                theData = ((Schmuck) attackerEntity).getBodyData();
                            }
                            s.getBodyData().receiveDamage(sea.damage, new Vector2(0, 0),
                                    theData, true, DamageTypes.TESTTYPE1);
                        } else if (e != null && e instanceof Event) {
                            Event ee = (Event) e;
                            CharacterData theData = null;
                            if (attackerEntity != null && attackerEntity instanceof Schmuck) {
                                theData = ((Schmuck) attackerEntity).getBodyData();
                            }
                            ee.eventData.receiveDamage(sea.damage, new Vector2(0, 0),
                                    theData, true, DamageTypes.TESTTYPE1);
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
            String input = (String) JOptionPane.showInputDialog(null, "Host:", "Connect to game server", JOptionPane.QUESTION_MESSAGE,
                    null, null, "localhost");
            if (input == null || input.trim().length() == 0) System.exit(1);
            hostIP = input.trim();

            // Request the user's name.
            input = (String) JOptionPane.showInputDialog(null, "Name:", "Connect to game server", JOptionPane.QUESTION_MESSAGE, null,
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
