package org.antinori.chess;


import net.sourceforge.frittle.Frittle;
import net.sourceforge.frittle.Game;
import net.sourceforge.frittle.GameState;
import net.sourceforge.frittle.Move;
import net.sourceforge.frittle.MoveList;
import net.sourceforge.frittle.Moves;
import net.sourceforge.frittle.PieceType;
import net.sourceforge.frittle.Player;
import net.sourceforge.frittle.ai.AI;
import net.sourceforge.frittle.ai.Eval;
import net.sourceforge.frittle.ui.XBoard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Main extends SimpleGame {
		
    Frittle frittle;

	public Environment environment;
	
	public PerspectiveCamera cam;
	
	public static final float CAMERA_HEIGHT = 30f;
	
	public static final Vector3 startCameraPosition = new Vector3(-10f, CAMERA_HEIGHT, 20f);
	public static final Vector3 leftCameraPosition = new Vector3(20f, 20f, -10f);
	public static final Vector3 rightCameraPosition = new Vector3(20f, 20f, 50f);
	
	private Vector3 cameraPosition = startCameraPosition;
	
	private float lightPosition = 0;
	private Vector3 lightCenter = new Vector3(20f, 20f, 20f);
	private float radiusA = 13f;
	private float radiusB = 13f;
	private ModelInstance circlingLight;
	
	public Board board;
	
	Cube lastSelectedTile = null;
	MoveList moveList = null;
	
	
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
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(cameraPosition);
		cam.lookAt(20, 0, 20);

		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();
		
		board = new Board();
		
		
//		ModelBuilder modelBuilder = new ModelBuilder();
//		Model sphere = modelBuilder.createSphere(2f, 2f, 2f, 20, 20, new Material(), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
//		circlingLight = new ModelInstance(sphere);
//		
//		createAxes();
		
		
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
		
		
//		lightPosition += delta * 1.0f;
//		float lx = (float) (radiusA * Math.cos(lightPosition));
//		float ly = (float) (radiusB * Math.sin(lightPosition));
//		Vector3 lightVector = new Vector3(lx, 0, ly).add(lightCenter);
//		circlingLight.transform.setToTranslation(lightVector);
//		
//		modelBatch.render(circlingLight, environment);
		
		
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
		
		for (Piece p : board.getPieces()) {
			modelBatch.render(p.getInstance(), environment);			
		}
		
        //modelBatch.render(axesInstance);

		modelBatch.end();
		

		
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		
		Ray pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
		
		Cube[][] cubes = board.getCubes();
		
		for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_HEIGHT; j++) {
				cubes[i][j].resetColor();;
			}
		}
				
		outer: for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_HEIGHT; j++) {
				Cube cube = cubes[i][j];
				if (Intersector.intersectRayBoundsFast(pickRay, cube.getBoundingBox())) {
					lastSelectedTile = cube;
					lastSelectedTile.highlight();
					break outer;
				}
			}
		}
		
		String coord = lastSelectedTile.getCoordinate();
		
		Player active = Frittle.getGame().getCurrentState().getActivePlayer().opponent();
		
		if (active == Player.WHITE) {
			if (moveList != null) moveList.clear();
			return false;
		}
			
		//check if last selected tile is in the move destination
		boolean moved = false;
		if (moveList != null && !moveList.isEmpty()) {
			for (Move m : moveList) {
				if (m.toString().endsWith(coord)) {
					if (move(m)) {
						moved = true;
						moveList.clear();
						break;
					}
				}
			}
		}
		
        
		if (!moved) {
	        moveList = Frittle.getGame().getLegalMoves();
			if (!moveList.isEmpty()) {
				for (Move move : moveList) {
					if (move.toString().startsWith(coord)) {
						Cube c = board.getCube(Moves.toCoOrdinate(move.dest));
						c.changeColor(Color.GREEN);
					}
				}
			}	
		}
		
		//printBoard();
		movePiecesToCurrentGameState();
		
        Frittle.write("Evaluation: "+(float)Eval.evaluate(Frittle.getGame().getCurrentState())/100);

		
		return false;
	}
	
	public boolean move(Move move) {
		boolean moved = false;
		if (Frittle.getGame().doMove(move.toString())) {
			if (Frittle.getAI().forceMode == false && Frittle.getGame().isGameOver() == false) {
				Frittle.getAI().go();
				moved = true;
			}
		}
		return moved;
	}
	
	@Override
	public boolean keyDown (int keycode) {
				
		if (keycode == Input.Keys.NUM_1) {
			cam.position.set(startCameraPosition);
			cam.lookAt(20f, 5f, 20f);
			return false;
		}
		
		if (keycode == Input.Keys.NUM_2) {
			cam.position.set(leftCameraPosition);
			cam.lookAt(20f, 5f, 20f);
			return false;
		}
		
		if (keycode == Input.Keys.NUM_3) {
			cam.position.set(rightCameraPosition);
			cam.lookAt(20f, 5f, 20f);
			return false;
		}

		if (keycode == Input.Keys.RIGHT) 
			cameraPosition.z += 1f;
		if (keycode == Input.Keys.UP) 
			cameraPosition.x += 1f;
		if (keycode == Input.Keys.LEFT) 
			cameraPosition.z -= 1f;
		if (keycode == Input.Keys.DOWN) 
			cameraPosition.x -= 1f;

		cam.position.set(cameraPosition);

		return false;
	}



	@Override
	public boolean scrolled (int amount) {
		float scrollFactor = -0.1f;
		cam.translate(new Vector3(cam.direction).scl(amount * scrollFactor * 10f));
		cameraPosition = cam.position;
		//System.out.println(cameraPosition);
		return false;
	}
	
	
	
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
	 * Prints an ASCII equivalent of the current state of the board to standard output.
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
	
	private void movePiecesToCurrentGameState() {
		
		for (Piece p: board.getPieces()) {
			p.setPlaced(false);
		}
		
		GameState state = Frittle.getGame().getCurrentState();
		for (byte r = 8; r > 0; r--) {
			for (char f = 'a'; f != 'i'; f++) {
				byte i = Moves.toIndex(f, r);
				if (state.getBoard()[i] != null) {
					PieceType pt = state.getBoard()[i].getType();
					Player pl = state.getBoard()[i].getPlayer();
					
					Piece piece = board.getPiece(pt, pl);
					piece.setPlaced(true);
					
					String coord = "" + f + r;
					Cube cube = board.getCube(coord);
					piece.setPos(cube.getPos());
				}
			}
		}
		
		//now place any captured pieces on the side
		for (Piece p : board.getPieces()) {
			if (!p.isPlaced()) {
				p.setPos(new Vector3(5f, 3f, -5f));
			}
		}

	}
	
	


}