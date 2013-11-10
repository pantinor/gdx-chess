package org.antinori.chess;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class SimpleGame implements ApplicationListener, InputProcessor {

	public ModelBatch modelBatch;
	public ModelBatch shadowBatch;
	public CameraInputController inputController;
	public PerspectiveCamera cam;
	
	public final static int PREF_HUDWIDTH = 640;
	public final static int PREF_HUDHEIGHT = 480;
	protected Stage hud;
	protected float hudWidth, hudHeight;
	protected Skin skin;

	public SimpleGame() {
	}

	public abstract void init();

	public abstract void draw(float delta);

	public void create() {
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(-25, 25, 0);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();
		
		inputController = new CameraInputController(cam);
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;

		hud = new Stage(PREF_HUDWIDTH, PREF_HUDHEIGHT, true);
		hudWidth = hud.getWidth();
		hudHeight = hud.getHeight();
		skin = new Skin(Gdx.files.classpath("uiskin.json"));
		
		init();		
		
		Gdx.input.setInputProcessor(new InputMultiplexer(this, hud, inputController));
	}

	public void render() {
		draw(Gdx.graphics.getDeltaTime());
		
		hud.draw();
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
