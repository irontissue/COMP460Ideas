package com.mygdx.game.actors;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.comp460game;
import com.mygdx.game.event.userdata.EventData;
import com.mygdx.game.states.PlayState;

public class PlayStateStage extends Stage {

	private DialogueBox dialogue;
	
	public PlayStateStage(PlayState state) {
		
		dialogue = new DialogueBox(comp460game.assetManager, state.gsm, 0, comp460game.CONFIG_HEIGHT);
		addActor(dialogue);
	}
	
	public void addDialogue(String id, EventData radio, EventData trigger) {
		dialogue.addDialogue(id, radio, trigger);
	}
	
	public void nextDialogue() {
		dialogue.nextDialogue();
	}
}
