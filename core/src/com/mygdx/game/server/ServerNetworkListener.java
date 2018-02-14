package com.mygdx.game.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.game.server.Packets.*;

public class ServerNetworkListener extends Listener {
	public int playerCount = 0;

	public ServerNetworkListener() {
		
	}
	
	public void disconnected(Connection c) {
		// This message should be sent when a player disconnects from the game

	}
	
	public void received(Connection c, Object o) {
		
		if (o instanceof PlayerConnect) {
			// We have received a player connection message.
			PlayerConnect p = (PlayerConnect) o;

			// Ignore the object if the name is invalid.
			String name = ((PlayerConnect)o).message;
//			if (name == null) return server.s ;
			name = name.trim();
			if (name.length() == 0) return;
			// Store the name on the connection.

		}
		
		if (o instanceof KeyPressOrRelease) {
			// We have received a player movement message.
			KeyPressOrRelease p = (KeyPressOrRelease) o;
		}

		if (o instanceof Packets.Shoot) {
			// We have received a mouse click.
			Shoot p = (Shoot) o;
		}
	}
}
