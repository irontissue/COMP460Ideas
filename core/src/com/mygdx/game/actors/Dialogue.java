package com.mygdx.game.actors;

import com.mygdx.game.event.userdata.EventData;

public class Dialogue {

	private String name, text;
	private boolean end;
	
	private EventData trigger, triggered;
	
	private float duration;
	
	public Dialogue(String name, String text, boolean end, float duration, EventData trigger, EventData triggered) {
		this.name = name;
		this.text = text;
		this.end = end;
		this.duration = duration;
		this.trigger = trigger;
		this.triggered = triggered;
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public EventData getTrigger() {
		return trigger;
	}

	public void setTrigger(EventData trigger) {
		this.trigger = trigger;
	}

	public EventData getTriggered() {
		return triggered;
	}

	public void setTriggered(EventData triggered) {
		this.triggered = triggered;
	}
}
