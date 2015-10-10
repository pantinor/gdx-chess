package org.antinori.chess;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngineThreaded;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
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
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Main extends SimpleGame implements SearchObserver {

    Evaluator evaluator;
    SearchEngineThreaded engine;
    SearchParameters searchParameters;
    int opponentDefaultIndex = 1;
    String timeString[] = {"1 second", "2 seconds", "5 seconds", "15 seconds", "30 seconds", "60 seconds"};
    int timeValues[] = {1000, 2000, 5000, 15000, 30000, 60000};
    int timeDefaultIndex = 0;
    String eloString[] = {"ELO 1000", "ELO 1100", "ELO 1200", "ELO 1300", "ELO 1400", "ELO 1500", "ELO 1600", "ELO 1700", "ELO 1800", "ELO 1900", "ELO 2000", "ELO 2100"};
    int eloValues[] = {1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100};
    int eloDefaultIndex = 11;

    String opponentString[] = {"Computer Whites", "Computer Blacks", "Human vs Human", "Computer vs Computer"};
    boolean userToMove = false;

    public Environment environment;
    DirectionalShadowLight shadowLight;

    public Board board;

    protected TextButton undoButton;

    Cube lastSelectedTile = null;
    String lastSelectedPieceCoord;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Chess";
        cfg.width = 1280;
        cfg.height = 768;
        new LwjglApplication(new Main(), cfg);
    }

    @Override
    public void init() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

//		environment.add(
//			(shadowLight = new DirectionalShadowLight(2048, 2048, 100, 100, 1, 100)).set(0.8f, 0.8f, 0.8f, -1f, -.5f, -.5f)
//		);
//		environment.shadowMap = shadowLight;
//		shadowBatch = new ModelBatch(new DepthShaderProvider());
        modelBatch = new ModelBatch();

        addUI();

        board = new Board();

        //createAxes();
        Config config = new Config();
        config.setTranspositionTableSize(8);
        engine = new SearchEngineThreaded(config);
        evaluator = new CompleteEvaluator(config);
        searchParameters = new SearchParameters();
        searchParameters.setMoveTime(timeValues[timeDefaultIndex]);
        engine.setObserver(this);

        userToMove = true; //White starts, black is computer

        movePiecesToCurrentGameState();

    }

    @Override
    public void draw(float delta) {

        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);

        modelBatch.render(board.skydome, environment);
        modelBatch.render(board.floor, environment);
        modelBatch.render(board.subfloor, environment);

        //shadowLight.begin(cam);
        //shadowBatch.begin(shadowLight.getCamera());
        for (Cube cube : board.getCubes()) {
            modelBatch.render(cube.getInstance(), environment);
            //shadowBatch.render(cube.getInstance());
            //modelBatch.render(cube.getOutline(), environment);
        }

        for (Piece p : board.getPieces()) {
            modelBatch.render(p.getInstance(), environment);
            //shadowBatch.render(p.getInstance());
        }

        //shadowBatch.end();
        //shadowLight.end();
        //modelBatch.render(axesInstance);
        modelBatch.end();

    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {

        if (!userToMove) {
            return false;
        }

        Ray pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());

        for (Cube cube : board.getCubes()) {
            cube.resetColor();
        }

        lastSelectedTile = null;

        for (Cube cube : board.getCubes()) {
            if (Intersector.intersectRayBoundsFast(pickRay, cube.getBoundingBox())) {
                lastSelectedTile = cube;
                lastSelectedTile.highlight();
                break;
            }
        }

        if (lastSelectedTile == null) {
            return false;
        }

        String coord = lastSelectedTile.getCoordinate();
        //System.out.println("lastSelectedTile: "+coord);

        boolean moved = false;
        if (lastSelectedPieceCoord != null && move(lastSelectedPieceCoord + coord)) {
            moved = true;
            lastSelectedPieceCoord = null;
        }

        if (!moved) {
            //highlight the legal moves for this select piece
            lastSelectedPieceCoord = coord;
            int[] legalMoves = new int[128];
            int count = engine.getBoard().getLegalMoves(legalMoves);
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    String to = BitboardUtils.index2Algebraic(Move.getToIndex(legalMoves[i]));
                    String from = BitboardUtils.index2Algebraic(Move.getFromIndex(legalMoves[i]));
                    if (!from.equals(coord)) {
                        continue;
                    }
                    Cube c = board.getCube(to);
                    c.changeColor(Color.GREEN);
                }
            }
        }

        return false;
    }

    private boolean move(String notation) {
        int move = Move.getFromString(engine.getBoard(), notation, false);
        if (engine.getBoard().isMoveLegal(move)) {
            engine.getBoard().doMove(move);
            checkUserToMove();
            return true;
        }
        return false;
    }

    public void bestMove(int bestMove, int ponder) {

        if (userToMove) {
            return;
        }

        Cube c1 = board.getCube(BitboardUtils.index2Algebraic(Move.getToIndex(bestMove)));
        c1.changeColor(Color.PINK);
        Cube c2 = board.getCube(BitboardUtils.index2Algebraic(Move.getFromIndex(bestMove)));
        c2.changeColor(Color.PINK);

        engine.getBoard().doMove(bestMove);
        checkUserToMove();

    }

    private void checkUserToMove() {

        userToMove = false;

        if (engine.getBoard().getTurn()) {
            userToMove = true;
        }

        if (engine.getBoard().isEndGame() == 0) {
            engine.go(searchParameters);
        }

        movePiecesToCurrentGameState();
    }

    public void info(SearchStatusInfo info) {

    }

    private void movePiecesToCurrentGameState() {

        board.reset();

        //place pieces on the board
        for (String coord : BitboardUtils.squareNames) {
            char c = engine.getBoard().getPieceAt(BitboardUtils.algebraic2Square(coord));
            if (c == '.') {
                continue;
            }
            Piece piece = board.getPiece(c);
            if (piece != null) {
                piece.setPlaced(true);
                Cube cube = board.getCube(coord);
                piece.setPos(cube.getPos());
            }
        }

        //now place any captured pieces on the side
        for (Piece p : board.getPieces()) {
            if (!p.isPlaced()) {
                board.placeInTray(p);
            }
        }

    }

    protected void addUI() {
        undoButton = new TextButton("Undo Last Move", skin);
        undoButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                engine.stop();
                engine.getBoard().undoMove();
                checkUserToMove();

            }
        });
        undoButton.setPosition(hudWidth - undoButton.getWidth(), 0);
        hud.addActor(undoButton);
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
        MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }
        // axes
        builder = modelBuilder.part("axes", GL30.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 100, 0, 0);
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 100, 0);
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 100);
        axesModel = modelBuilder.end();
        axesInstance = new ModelInstance(axesModel);
    }

}
