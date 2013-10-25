package org.antinori.chess;


import net.sourceforge.frittle.Frittle;
import net.sourceforge.frittle.Game;
import net.sourceforge.frittle.GameState;
import net.sourceforge.frittle.Moves;
import net.sourceforge.frittle.ai.AI;
import net.sourceforge.frittle.ui.CommunicationProtocol;
import net.sourceforge.frittle.ui.XBoard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Main extends SimpleGame {
		
    Frittle frittle;

	public Environment environment;
	
	public PerspectiveCamera cam;
	public static final float CAMERA_HEIGHT = 30f;
	public static final Vector3 startCamerPosition = new Vector3(-10f, CAMERA_HEIGHT, 20f);

	private Vector3 cameraPosition = startCamerPosition;
	
	public Board board;
	
	public static Texture ROCK_TEXTURE;
	
	//properties for selecting cube with mouse clicks
	public static final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
	public static final Vector3 intersection = new Vector3();
	
	final Vector3 curr = new Vector3();
	final Vector3 last = new Vector3(-1, -1, -1);
	final Vector3 delta = new Vector3();
	Cube lastSelectedTile = null;

	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Chess";
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 768;
		new LwjglApplication(new Main(), cfg);

	}
	

	@Override
	public void init() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(cameraPosition);
		cam.lookAt(20, 0, 20);

		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();
		
		ROCK_TEXTURE = new Texture(Gdx.files.classpath("data/rock.png"), true);

		board = new Board();
		
		//createAxes();
		
		
		// Create new game and restart AI engine
		Frittle.setGame(new Game());
		Frittle.setAi(new AI());
		Frittle.setProtocol(new XBoard());
		
		
	}

	@Override
	public void draw(float delta) {
		
		cam.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		
		Cube[][] cubes = board.getCubes();
		for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_HEIGHT; j++) {
				Cube cube = cubes[i][j];
				if (cube != null) {
					modelBatch.render(cube.getInstance(), environment);
					//modelBatch.render(cube.getOutline(), environment);
				}
			}
		}
		
		Piece[][] pieces = board.getPieces();
		for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_HEIGHT; j++) {
				Piece piece = pieces[i][j];
				if (piece != null) {
					modelBatch.render(piece.instance, environment);
				}
			}
		}
		
        //modelBatch.render(axesInstance);

		modelBatch.end();
		

		
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		
		Ray pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
		
		Cube[][] cubes = board.getCubes();
		outer: for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_HEIGHT; j++) {
				Cube cube = cubes[i][j];
				if (cube != null) {
					if (Intersector.intersectRayBoundsFast(pickRay, cube.getBoundingBox())) {
						if (lastSelectedTile != null) {
							lastSelectedTile.changeColor(false);
						}
						lastSelectedTile = cube;
						lastSelectedTile.changeColor(true);
						break outer;
					}
				}
			}
		}
		
		printBoard();
		
		return false;
	}
	
	@Override
	public boolean keyDown (int keycode) {
		
		if (keycode == Input.Keys.NUM_1) {
			cam.position.set(startCamerPosition);
			return false;
		}

		if (keycode == Input.Keys.RIGHT) cameraPosition.z += 1f;
		if (keycode == Input.Keys.UP) cameraPosition.x += 1f;
		if (keycode == Input.Keys.LEFT) cameraPosition.z -= 1f;
		if (keycode == Input.Keys.DOWN) cameraPosition.x -= 1f;

		cam.position.set(cameraPosition);

		return false;
	}



	@Override
	public boolean scrolled (int amount) {
		float scrollFactor = -0.1f;
		cam.translate(new Vector3(cam.direction).scl(amount * scrollFactor * 10f));
		cameraPosition = cam.position;
		System.out.println(cameraPosition);
		return false;
	}
	
//	@Override
//	public boolean touchDragged (int screenX, int screenY, int pointer) {
//		
//	}
	
	
	final float GRID_MIN = -40f;
	final float GRID_MAX = 40f;
	final float GRID_STEP = 1f;
	public Model axesModel;
	public ModelInstance axesInstance;

	private void createAxes() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		// grid
		MeshPartBuilder builder = modelBuilder.part("grid", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		// axes
		builder = modelBuilder.part("axes", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 100, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 100, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 100);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}
    
	/**
	 * Prints an ASCII equivalent of the current state of the board to standard
	 * output. Generated by the "bd" command.
	 */
	private void printBoard() {
		StringBuffer line;
		GameState state = Frittle.getGame().getCurrentState();
		for (byte r = 8; r > 0; r--) {
			line = new StringBuffer();
			line.append(r + "| ");
			for (char f = 'a'; f != 'i'; f++) {
				byte i = Moves.toIndex(f, r);
				if (state.getBoard()[i] == null)
					line.append("- ");
				else
					line.append(state.getBoard()[i].getChar() + " ");
			}
			Frittle.write(line.toString());
		}
		Frittle.write("------------------");
		Frittle.write(" | a b c d e f g h");
		Frittle.write("  " + state.getActivePlayer().toString() + " TO MOVE");
	}


}