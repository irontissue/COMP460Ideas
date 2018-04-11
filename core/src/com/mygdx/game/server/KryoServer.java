package com.mygdx.game.server;

import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.entities.userdata.PlayerData;
import com.mygdx.game.manager.GameStateManager;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.states.TitleState;

public class KryoServer {

	int serverPort = 25565;
	boolean p1ReadyCheck = false, p2ReadyCheck = false;

	//The POSITION in this array is the playerNumber number (i.e. playerNumber 1 vs playerNumber 2). The actual value stored in the array
    //is that playerNumber's connection ID.
	public int[] playerIDs = {-1,-1};
	public UUID[] playerUUIDs = {null,null};

	public Server server;
	GameStateManager gsm;
	boolean setMaster = true;

	public KryoServer(GameStateManager gameStateManager) {
		Kryo kryo = new Kryo();
		kryo.setReferences(true);
		KryoSerialization serialization = new KryoSerialization(kryo);
		this.server = new Server(16384, 2048, serialization);

		gsm = gameStateManager;

		server.addListener(new Listener() {
			public void disconnected(Connection c) {
				// This message should be sent when a playerNumber disconnects from the game
                p1ReadyCheck = false;
                p2ReadyCheck = false;
                playerIDs[0] = -1;
                playerIDs[1] = -1;
                server.sendToAllExceptTCP(c.getID(), new Packets.DisconnectMessage());
                Gdx.app.postRunnable(new Runnable() {
                     @Override
                     public void run() {
                         gsm.removeState(PlayState.class);
                         gsm.addState(GameStateManager.State.TITLE, PlayState.class);
                     }
                 });
			}

			public void received(Connection c, Object o) {
				//Log.info("" + (o.getClass().getName()));
				if (o instanceof Packets.PlayerConnect) {
					// We have received a playerNumber connection message.
					Packets.PlayerConnect p = (Packets.PlayerConnect) o;

					// Ignore the object if the name is invalid.
					String name = p.message;
					if (name == null) {
						server.sendToTCP(c.getID(), new Packets.PlayerConnect("Invalid Player name."));
						return;
					}
					name = name.trim();
					if (name.length() == 0) {
						server.sendToTCP(c.getID(), new Packets.PlayerConnect("Cannot have empty playerNumber name."));
						return;
					}
					Packets.PlayerConnect newPlayer = new Packets.PlayerConnect( name + " has joined the game server.");
					Log.info(name + " has joined the game.");
					server.sendToAllExceptTCP(c.getID(), newPlayer);
					server.sendToTCP(c.getID(), new Packets.ServerIDMessage(c.getID()));
					setMaster = false;

				}

				else if (o instanceof Packets.KeyPressOrRelease) {
					// We have received a playerNumber movement message.
					Packets.KeyPressOrRelease p = (Packets.KeyPressOrRelease) o;
					server.sendToAllTCP(p);
//                    Packets.KeyPressOrRelease p = (Packets.KeyPressOrRelease) o;
                    if (!gsm.states.empty() && gsm.states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) gsm.states.peek();
                        if (p.message == Input.Keys.W) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.wPressed = true;
                                } else {
                                    ps.player.wPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.wPressed = true;
                                    //Log.info("W2 pressed");
                                } else {
                                    ps.player2.wPressed = false;
                                    //Log.info("W2 released");

                                }
                            }
                        } else if (p.message == Input.Keys.A) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.aPressed = true;
                                } else {
                                    ps.player.aPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.aPressed = true;
                                } else {
                                    ps.player2.aPressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.S) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.sPressed = true;
                                } else {
                                    ps.player.sPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.sPressed = true;
                                } else {
                                    ps.player2.sPressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.D) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.dPressed = true;
                                } else {
                                    ps.player.dPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.dPressed = true;
                                } else {
                                    ps.player2.dPressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.Q) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.qPressed = true;
                                } else {
                                    ps.player.qPressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.qPressed = true;
                                } else {
                                    ps.player2.qPressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.E) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.ePressed = true;
                                } else {
                                    ps.player.ePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.ePressed = true;
                                } else {
                                    ps.player2.ePressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.R) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.playerData.getCurrentTool().reloading = true;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.playerData.getCurrentTool().reloading = true;
                                }
                            }
                        } else if (p.message == Input.Keys.SPACE) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.spacePressed = true;
                                } else {
                                    ps.player.spacePressed = false;
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.spacePressed = true;
                                } else {
                                    ps.player2.spacePressed = false;
                                }
                            }
                        } else if (p.message == Input.Keys.NUM_1) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.playerData.switchWeapon(1);
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.playerData.switchWeapon(1);
                                }
                            }
                        } else if (p.message == Input.Keys.NUM_2) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.playerData.switchWeapon(2);
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.playerData.switchWeapon(2);
                                }
                            }
                        } else if (p.message == Input.Keys.NUM_3) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.playerData.switchWeapon(3);
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.playerData.switchWeapon(3);
                                }
                            }
                        } else if (p.message == Input.Keys.NUM_4) {
                            if (p.playerID == playerIDs[0]) {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player.playerData.switchWeapon(4);
                                }
                            } else {
                                if (p.pressOrRelease == Packets.KeyPressOrRelease.PRESSED) {
                                    ps.player2.playerData.switchWeapon(4);
                                }
                            }
                        }
                    }
				}

				else if (o instanceof Packets.MousePressOrRelease) {
                    Packets.MousePressOrRelease p = (Packets.MousePressOrRelease) o;
                    if (!gsm.states.empty() && gsm.states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) gsm.states.peek();
                        //Log.info("MousePressOrRelease on server");
                        if (p.buttonID == Input.Buttons.LEFT) {
                            if (p.playerID == playerIDs[0]) {
                                //Log.info("MousePressOrRelease on server - player 1");
                                if (p.pressOrRelease == Packets.MousePressOrRelease.PRESSED) {
                                    ps.player.mousePressed = true;
                                } else {
                                    ps.player.mousePressed = false;
                                }
                            } else {
                                //Log.info("MousePressOrRelease on server - player 2");
                                if (p.pressOrRelease == Packets.MousePressOrRelease.PRESSED) {
                                    ps.player2.mousePressed = true;
                                } else {
                                    ps.player2.mousePressed = false;
                                }
                            }
                        }
                    }
                }

                else if (o instanceof Packets.MouseReposition) {
                    Packets.MouseReposition p = (Packets.MouseReposition) o;
                    if (!gsm.states.empty() && gsm.states.peek() instanceof PlayState) {
                        PlayState ps = (PlayState) gsm.states.peek();
                        //Log.info("MouseReposition on server");
                        if (p.playerID == playerIDs[0]) {
                            //Log.info("MouseReposition on server - player 1");
                            ps.player.mousePosX = p.x;
                            ps.player.mousePosY = p.y;
                        } else {
                            //Log.info("MouseReposition on server - player 2");
                            ps.player2.mousePosX = p.x;
                            ps.player2.mousePosY = p.y;
                        }
                    }
                }

				else if (o instanceof Packets.ReadyToPlay) {
					//Log.info("Server received ReadyToPlay from connection id = " + c.getID());
				    Packets.ReadyToPlay p = (Packets.ReadyToPlay) o;
				    if (c.getID() == playerIDs[0] || playerIDs[0] == -1) {
				        p1ReadyCheck = true;
				        playerIDs[0] = c.getID();
                        Log.info("Player " + c.getID() + " ready.");
                    } else if (c.getID() == playerIDs[1] || playerIDs[1] == -1) {
				        p2ReadyCheck = true;
                        playerIDs[1] = c.getID();
                        Log.info("Player " + c.getID() + " ready.");
                    }
				    if (p1ReadyCheck && p2ReadyCheck) {
//				        if (playerIDs[0] > playerIDs[1]) {
                            server.sendToTCP(playerIDs[0], new Packets.EnterPlayState(1));
                            server.sendToTCP(playerIDs[1], new Packets.EnterPlayState(2));
                            Log.info("Sending playernumber = 1 to connectionID = " + playerIDs[0]);
                            Log.info("Sending playernumber = 2 to connectionID = " + playerIDs[1]);
//                        } else {
//                            server.sendToTCP(playerIDs[0], new Packets.EnterPlayState(2));
//                            server.sendToTCP(playerIDs[1], new Packets.EnterPlayState(1));
//                            int temp = playerIDs[0];
//                            playerIDs[0] = playerIDs[1];
//                            playerIDs[1] = temp;
//                        }
				        p1ReadyCheck = false;
				        p2ReadyCheck = false;
                    }
                }

                else if (o instanceof Packets.ClientLoadedPlayState) {
                    final Packets.ClientLoadedPlayState p = (Packets.ClientLoadedPlayState) o;
                    if (c.getID() == playerIDs[0] || playerIDs[0] == -1) {
                        p1ReadyCheck = true;
                        playerIDs[0] = c.getID();
                        Log.info("ClientLoadedPlayState (p1). CID = " + c.getID() + ". level = " + p.level);
                    } else if (c.getID() == playerIDs[1] || playerIDs[1] == -1) {
                        p2ReadyCheck = true;
                        playerIDs[1] = c.getID();
                        Log.info("ClientLoadedPlayState (p2). CID = " + c.getID() + ". level = " + p.level);
                    }
                    if (p1ReadyCheck && p2ReadyCheck) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                Log.info("Both clients loaded playstate. Adding new playstate.");
                                PlayerData pd1 = null, pd2 = null;
                                if (gsm.states.peek() instanceof PlayState) {
                                    pd1 = ((PlayState) gsm.states.peek()).player.playerData;
                                    pd2 = ((PlayState) gsm.states.peek()).player2.playerData;
                                    gsm.removeState(PlayState.class);
                                } else {
                                    gsm.removeState(TitleState.class);
                                }
                                gsm.addPlayState(p.level, pd1, pd2, TitleState.class);
                            }
                        });
                        p1ReadyCheck = false;
                        p2ReadyCheck = false;
                    }
                }

                else if (o instanceof Packets.SyncPlayState) {
					//Log.info("Syncing PlayStates...");
					Packets.SyncPlayState p = (Packets.SyncPlayState) o;
					server.sendToAllExceptTCP(c.getID(),p);
				}

				/*else if (o instanceof Packets.SyncHitbox) {
					//Log.info("Syncing Hitbox...");
					Packets.SyncHitbox p = (Packets.SyncHitbox) o;
					server.sendToAllTCP(p);
				}*/

				else if (o instanceof Packets.SyncCreateSchmuck) {
					//Log.info("Syncing Schmuck Creation...");
					Packets.SyncCreateSchmuck p = (Packets.SyncCreateSchmuck) o;
					server.sendToAllExceptTCP(c.getID(),p);
				}
			}
		});

		try {
			server.bind(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		registerPackets();

		server.start();
	}

	private void registerPackets() {
		Kryo kryo = server.getKryo();
		Packets.allPackets(kryo);
	}
}

