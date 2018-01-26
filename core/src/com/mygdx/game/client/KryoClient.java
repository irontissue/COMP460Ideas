package com.mygdx.game.client;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.comp460game;
import com.mygdx.game.equipment.ranged.Gun;
import com.mygdx.game.manager.GameStateManager.State;
import com.mygdx.game.server.*;
import com.mygdx.game.states.GameState;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.states.TitleState;
import com.mygdx.game.util.Constants;
//import com.mygdx.game.server.Packets;

import javax.swing.*;

public class KryoClient {

	int portSocket = 25565;
	String ipAddress = "localhost";
	
	public Client client;
    comp460game myGame;
    public int myID;
    public boolean master = false;

    public static final int timeout = 5000;
    String name;

    public KryoClient(comp460game myGame) {
        this.myGame = myGame;
	}

	public void init() {
        this.client = new Client();
        client.start();

        registerPackets();

        client.addListener(new Listener() {

            public void connected(Connection c) {
                Packets.Packet01Message connected = new Packets.Packet01Message(name);
                client.sendTCP(connected);
            }

            public void disconnected(Connection c) {

            }

            public void received(Connection c, Object o) {

                if (o instanceof Packets.Packet01Message) {
                    Packets.Packet01Message p = (Packets.Packet01Message) o;
                }

                else if (o instanceof Packets.Packet04EnterPlayState) {
                    Gdx.app.postRunnable(new Runnable() {
                        public void run() {
                            myGame.getGsm().addState(State.PLAY, TitleState.class);
                        }
                    });
                }

                else if (o instanceof Packets.IDMessage) {
                    Packets.IDMessage p = (Packets.IDMessage) o;
                    myID = p.ID;
                    master = p.master;
                }

                else if (o instanceof Packets.SyncPlayState) {
                    Packets.SyncPlayState p = (Packets.SyncPlayState) o;
                    PlayState ps = (PlayState)myGame.getGsm().states.peek();
                    ps.player.body.setTransform(p.body,p.angle);
                    Log.info("Received sync message...");
                }

                else if (o instanceof Packets.Packet03Click) {
                    Packets.Packet03Click p = (Packets.Packet03Click) o;
                    PlayState ps = (PlayState) myGame.getGsm().states.peek();
                    ps.player.dummy.useToolStart(p.delta, ps.player.dummy.dummyWeapon, Constants.PLAYER_HITBOX, (int) p.location.x , (int)(Gdx.graphics.getHeight() - p.location.y), true);
                }

                else if (o instanceof Packets.Packet02Input) {
                    Packets.Packet02Input p = (Packets.Packet02Input) o;
                    if (p.message == Input.Keys.W) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.wPressed = true;
                                } else {
                                    ps.player.wPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.wPressed2 = true;
                                    Log.info("W2 pressed");
                                } else {
                                    ps.player.wPressed2 = false;
                                    Log.info("W2 released");

                                }
                            }
                        }
                    } else if (p.message == Input.Keys.A) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.aPressed = true;
                                } else {
                                    ps.player.aPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.aPressed2 = true;
                                } else {
                                    ps.player.aPressed2 = false;
                                }
                            }
                        }
                    } else if (p.message == Input.Keys.S) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.sPressed = true;
                                } else {
                                    ps.player.sPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.sPressed2 = true;
                                } else {
                                    ps.player.sPressed2 = false;
                                }
                            }
                        }
                    } else if (p.message == Input.Keys.D) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.dPressed = true;
                                } else {
                                    ps.player.dPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.dPressed2 = true;
                                } else {
                                    ps.player.dPressed2 = false;
                                }
                            }
                        }
                    } else if (p.message == Input.Keys.Q) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.qPressed = true;
                                } else {
                                    ps.player.qPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.qPressed2 = true;
                                } else {
                                    ps.player.qPressed2 = false;
                                }
                            }
                        }
                    } else if (p.message == Input.Keys.E) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.ePressed = true;
                                } else {
                                    ps.player.ePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.ePressed2 = true;
                                } else {
                                    ps.player.ePressed2 = false;
                                }
                            }
                        }
                    }
                    else if (p.message == Input.Keys.SPACE) {
                        if (myGame.getGsm().states.peek() instanceof PlayState) {
                            PlayState ps = (PlayState) myGame.getGsm().states.peek();
                            if (p.playerID == myID) {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.spacePressed = true;
                                } else {
                                    ps.player.spacePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.Packet02Input.PRESSED) {
                                    ps.player.spacePressed = true;
                                } else {
                                    ps.player.spacePressed = false;
                                }
                            }
                        }
                    }
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
