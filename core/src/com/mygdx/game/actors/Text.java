package com.mygdx.game.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.comp460game;

/**
 * Simple actor that displays floating text. Not suitable for long messages.
 */
public class Text extends A460Actor{
	
	private String text;
	private BitmapFont font;
	private Color color;
	private GlyphLayout layout;

	private float scale = 1.0f;

	public Text(AssetManager assetManager, String text, int x, int y) {
		super(assetManager, x, y);
		this.text = text;
		font = comp460game.SYSTEM_FONT_TEXT;
		color = comp460game.DEFAULT_TEXT_COLOR;
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		updateHitBox();
	}
	
	public Text(AssetManager assetManager, String text, int x, int y, boolean title) {
		this(assetManager, text, x, y);
		if (title) {
			font = comp460game.SYSTEM_FONT_TITLE;
			updateHitBox();
		}
	}
	
	public Text(AssetManager assetManager, String text, int x, int y, Color color) {
		this(assetManager, text, x, y);
		this.color = color;
	}
	
	public Text(AssetManager assetManager, String text, int x, int y, Color color, boolean title) {
		this(assetManager, text, x, y, color);
		if (title) {
			font = comp460game.SYSTEM_FONT_TITLE;
			font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			updateHitBox();
		}
	}

	@Override
    public void draw(Batch batch, float alpha) {
		 font.getData().setScale(scale);
		 font.setColor(color);
         font.draw(batch, text, getX(), getY() + layout.height);
         //Return scale and color to default values.
         font.getData().setScale(1.0f);
         font.setColor(comp460game.DEFAULT_TEXT_COLOR);
    }
	
	@Override
	public void updateHitBox() {
		font.getData().setScale(scale);
		layout = new GlyphLayout(font, text);
		setWidth(layout.width);
		setHeight(layout.height);
		super.updateHitBox();
		font.getData().setScale(1.0f);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		updateHitBox();
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
	}
	
	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
		updateHitBox();
	}
}
