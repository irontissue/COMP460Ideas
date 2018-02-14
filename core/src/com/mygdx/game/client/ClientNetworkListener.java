package com.mygdx.game.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.game.server.Packets.PlayerConnect;

public class ClientNetworkListener extends Listener {

	private Client client;
	
	public void init(Client client) {
		this.client = client;
	}
	
	public void connected(Connection c) {
		
	}
	
	public void disconnected(Connection c) {
		
	}
	
	public void received(Connection c, Object o) {
		
		if (o instanceof PlayerConnect) {
			PlayerConnect p = (PlayerConnect) o;
		}
	}
}
