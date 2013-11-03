package org.antinori.chess;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

public abstract class SimpleGame implements ApplicationListener, InputProcessor {

	public ModelBatch modelBatch;
	public ModelBatch shadowBatch;
	public CameraInputController inputController;
	public PerspectiveCamera cam;

	public SimpleGame() {
	}

	public abstract void init();

	public abstract void draw(float delta);

	public void create() {
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(-10f, 30f, 20f);
		cam.lookAt(20, 0, 20);
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		
		inputController = new CameraInputController(cam);
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;

		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController));
		
		init();		

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
