package org.antinori.chess;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

public abstract class SimpleGame implements ApplicationListener, InputProcessor {

	public ModelBatch modelBatch;

	public SimpleGame() {
	}

	public abstract void init();

	public abstract void draw(float delta);

	public void create() {
		init();
		Gdx.input.setInputProcessor(this);
		Gdx.graphics.setVSync(true);
	}

	public void render() {
		draw(Gdx.graphics.getDeltaTime());
	}

	public boolean keyDown(int keycode) {
		return false;
	}

	public boolean keyUp(int keycode) {
		return false;
	}

	public boolean keyTyped(char character) {
		return false;
	}

	public boolean touchDown(int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	public boolean scrolled(int amount) {
		return false;
	}

	public void pause() {
	}

	public void resume() {
	}

	public void dispose() {
		modelBatch.dispose();
	}

	public void resize(int width, int height) {

	}
}
