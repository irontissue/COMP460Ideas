package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.comp460game;

import javax.swing.*;
import java.io.IOException;


public class DesktopLauncher {
	public static void main (String[] arg) throws IOException {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1080;
		config.height = 720;
//		config.fullscreen = true;
		config.foregroundFPS = 60;
		config.backgroundFPS = 60;
//		new ChatServer();
//		new ChatClient();
/*		try {
			new ChatServer();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	new ChatClient();*/

        int serverMode = JOptionPane.showConfirmDialog(null, "Launch in server mode?", "Select launch mode",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (serverMode == 0) {
            new LwjglApplication(new comp460game(true), config);
        } else if (serverMode == 1) {
            new LwjglApplication(new comp460game(false), config);
        } else {
            System.exit(0);
        }

	}
}