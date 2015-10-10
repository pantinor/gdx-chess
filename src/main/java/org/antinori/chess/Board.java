package org.antinori.chess;


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.UBJsonReader;

public class Board {
	
	public AssetManager assets;
	public ModelInstance skydome;
	public ModelInstance floor;
	public ModelInstance subfloor;

	
	String[] COORD_X = { "a", "b", "c", "d", "e", "f", "g", "h" };
	
	private List<Cube> cubes;
	public List<Piece> pieces;
	
	public static final float MODEL_HEIGHT = 2.55f;
	public static final Vector3 X_AXIS = new Vector3(1, 0, 0);
	public static final Vector3 Y_AXIS = new Vector3(0, 1, 0);
	public static final Vector3 Z_AXIS = new Vector3(0, 0, 1);
	
	public static Model pawnModel;
	public static Model bishopModel;
	public static Model knightWhiteModel;
	public static Model knightBlackModel;
	public static Model rookModel;
	public static Model queenModel;
	public static Model kingModel;
	
	List<TrayPosition> whiteTray = new ArrayList<TrayPosition>();
	List<TrayPosition> blackTray = new ArrayList<TrayPosition>();

	//public static final Texture darkTexture = new Texture(Gdx.files.classpath("data/wooddark0.jpg"), Format.RGB565, true);
	//public static final Texture lightTexture = new Texture(Gdx.files.classpath("data/woodlight0.jpg"), Format.RGB565, true);
	
	public Board() {
		
		assets = new AssetManager(new ClasspathFileHandleResolver());
		assets.load("skydome.g3db", Model.class);
		assets.load("floor.jpg", Texture.class);
		assets.update(2000);
		
		skydome = new ModelInstance(assets.get("skydome.g3db", Model.class));

		
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		MeshPartBuilder part = builder.part("floor", GL30.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal, new Material());
		part.circle(60, 40, 0, 0, 0, 0, 1, 0);
		Model floorModel = builder.end();
		floorModel.materials.get(0).set(TextureAttribute.createDiffuse(assets.get("floor.jpg", Texture.class)));
		floor = new ModelInstance(floorModel);
		
		Model sf = builder.createBox(500, 2, 500, new Material(ColorAttribute.createDiffuse(Color.BLACK)), Usage.Position | Usage.Normal);
		subfloor = new ModelInstance(sf, new Vector3(0,-2,0));
		
		cubes = new ArrayList<Cube>();
		
		ModelBuilder modelBuilder = new ModelBuilder();
		
		for (int x=0;x<8;x++) {
			for (int z=0;z<8;z++) {
				
				Color color = Color.BLUE;
				if ((x + z) % 2 == 1) {
					color = new Color(Color.rgb888(80, 124, 224));
				} else {
					color = new Color(Color.rgb888(167, 191, 246));
				}
				
				String coord = COORD_X[z] + (x+1);
				
				Vector3 pos = new Vector3(x*5+2.5f-20, 0, z*5+2.5f-20);
				Cube cube = new Cube(modelBuilder, color, pos, coord);
				cubes.add(cube);
			}
		}
		
		//convert the collada dae format to the g3db format (do not use the obj format)
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/pawn.dae ./pawn.g3db
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/rook.dae ./rook.g3db
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/knight.dae ./knight.g3db
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/bishop.dae ./bishop.g3db
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/king.dae ./king.g3db
		//C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/queen.dae ./queen.g3db

		ModelLoader gloader = new G3dModelLoader(new UBJsonReader(), new ClasspathFileHandleResolver());
		
		pawnModel = gloader.loadModel(Gdx.files.classpath("meshes/pawn.g3db"));
		bishopModel = gloader.loadModel(Gdx.files.classpath("meshes/bishop.g3db"));
		
		knightWhiteModel = gloader.loadModel(Gdx.files.classpath("meshes/knight.g3db"));
		knightBlackModel = gloader.loadModel(Gdx.files.classpath("meshes/knight-black.g3db"));
		//knightModel.nodes.get(0).rotation.set(Y_AXIS, 180);


		rookModel = gloader.loadModel(Gdx.files.classpath("meshes/rook.g3db"));
		queenModel = gloader.loadModel(Gdx.files.classpath("meshes/queen.g3db"));
		kingModel = gloader.loadModel(Gdx.files.classpath("meshes/king.g3db"));
		
		//darkTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		//lightTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		
		
		pieces = new ArrayList<Piece>();
		

		// Pawns
		for (int c = 0; c < 8; c++) {
			pieces.add(new Piece(PieceType.PAWN, Player.WHITE, pawnModel, Vector3.Zero)); // White
			pieces.add(new Piece(PieceType.PAWN, Player.BLACK, pawnModel, Vector3.Zero)); // Black
		}
		
		
		// Other White pieces
		pieces.add(new Piece(PieceType.ROOK, Player.WHITE, rookModel, 			Vector3.Zero));
		pieces.add(new Piece(PieceType.KNIGHT, Player.WHITE, knightWhiteModel, 	Vector3.Zero));
		pieces.add(new Piece(PieceType.BISHOP, Player.WHITE, bishopModel,		Vector3.Zero));
		pieces.add(new Piece(PieceType.QUEEN, Player.WHITE, queenModel,			Vector3.Zero));
		pieces.add(new Piece(PieceType.KING, Player.WHITE, kingModel, 			Vector3.Zero));
		pieces.add(new Piece(PieceType.BISHOP, Player.WHITE, bishopModel, 		Vector3.Zero));
		pieces.add(new Piece(PieceType.KNIGHT, Player.WHITE, knightWhiteModel, 	Vector3.Zero));
		pieces.add(new Piece(PieceType.ROOK, Player.WHITE, rookModel, 			Vector3.Zero));

		
		// Other Black pieces
		pieces.add(new Piece(PieceType.ROOK, Player.BLACK, rookModel, 			Vector3.Zero));
		pieces.add(new Piece(PieceType.KNIGHT, Player.BLACK, knightBlackModel, 	Vector3.Zero));
		pieces.add(new Piece(PieceType.BISHOP, Player.BLACK, bishopModel,		Vector3.Zero));
		pieces.add(new Piece(PieceType.QUEEN, Player.BLACK, queenModel,			Vector3.Zero));
		pieces.add(new Piece(PieceType.KING, Player.BLACK, kingModel, 			Vector3.Zero));
		pieces.add(new Piece(PieceType.BISHOP, Player.BLACK, bishopModel, 		Vector3.Zero));
		pieces.add(new Piece(PieceType.KNIGHT, Player.BLACK, knightBlackModel, 	Vector3.Zero));
		pieces.add(new Piece(PieceType.ROOK, Player.BLACK, rookModel, 			Vector3.Zero));	
		
		
		float factor1 = 2.5f - 20;
		float factor2 = -7.5f - 20;
		float factor3 = 45 - 20;
		
		//set tray positions
		for (int c = 0; c < 8; c++) {
			whiteTray.add(new TrayPosition(PieceType.PAWN, Player.WHITE, new Vector3(2.5f*c-20, MODEL_HEIGHT, factor2 + 2.5f)));
			blackTray.add(new TrayPosition(PieceType.PAWN, Player.BLACK, new Vector3(2.5f*c-20, MODEL_HEIGHT, factor3 - 2.5f)));
		}
		
		whiteTray.add(new TrayPosition(PieceType.ROOK, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.ROOK, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.KNIGHT, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.KNIGHT, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.BISHOP, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.BISHOP, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.QUEEN, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		whiteTray.add(new TrayPosition(PieceType.KING, Player.WHITE, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor2)));
		
		factor1 = 2.5f - 20;
		
		blackTray.add(new TrayPosition(PieceType.ROOK, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.ROOK, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.KNIGHT, Player.BLACK,	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.KNIGHT, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.BISHOP, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.BISHOP, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.QUEEN, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));
		blackTray.add(new TrayPosition(PieceType.KING, Player.BLACK, 	new Vector3(factor1 += 2.5f, MODEL_HEIGHT, factor3)));

		
	}

	public List<Cube> getCubes() {
		return cubes;
	}

	public List<Piece> getPieces() {
		return pieces;
	}
	
	public Cube getCube(String coord) {
		Cube c = null;
		for (Cube cube : cubes) {
			if (cube.getCoordinate().equals(coord)) c = cube;
		}
		return c;
	}
	
	public Piece getPiece(PieceType pt, Player pl) {
		Piece piece = null;		
		for (Piece p : pieces) {
			if (p.getType() == pt && p.getPlayer() == pl && !p.isPlaced()) {
				piece = p;
				break;
			}
		}
		return piece;
	}
	
	public Piece getPiece(char c) {
		PieceType pt = PieceType.convert(c);
		Player pl = (Character.isUpperCase(c)?Player.WHITE:Player.BLACK);
		return getPiece(pt,pl);
	}
	
	public boolean isSamePosition(Vector3 v1, Vector3 v2) {
		if (v1 == null || v2 == null) return false;
		if (NumberUtils.floatToIntBits(v1.x) != NumberUtils.floatToIntBits(v2.x)) return false;
		if (NumberUtils.floatToIntBits(v1.z) != NumberUtils.floatToIntBits(v2.z)) return false;
		return true;
	}


	
	class ClasspathFileHandleResolver implements FileHandleResolver {
		public FileHandle resolve (String fileName) {
			return Gdx.files.classpath(fileName);
		}
	}
	
	public void reset() {
		for (Piece p: getPieces()) {
			p.setPlaced(false);
		}
		
		for (TrayPosition tp : whiteTray) {
			tp.setTaken(false);
		}
		
		for (TrayPosition tp : blackTray) {
			tp.setTaken(false);
		}
	}
	
	
	public void placeInTray(Piece p) {
		
		List<TrayPosition> tray = (p.getPlayer() == Player.WHITE?whiteTray:blackTray);
		for (TrayPosition tp : tray) {
			if (p.getType() == tp.getType() && !tp.isTaken()) {
				tp.setTaken(true);
				p.setPos(tp.getPosition());
			}
		}
		
	}

	
	
}
