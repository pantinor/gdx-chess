package org.antinori.chess;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Board {
	
	public static final int BOARD_WIDTH = 8;
	public static final int BOARD_HEIGHT = 8;

	int width = BOARD_WIDTH;
	int height = BOARD_HEIGHT;

	private Cube[][] cubes;
	public Piece[][] pieces;
	
	public Board() {
		
		cubes = new Cube[width][height];
		
		ModelBuilder modelBuilder = new ModelBuilder();
		
		for (int x = 0;x<8;x++) {
			for (int z = 0;z<8;z++) {
				
				Color color = Color.BLUE;
				if ((x + z) % 2 == 1) {
					color = new Color(Color.rgb888(80, 124, 224));
				} else {
					color = new Color(Color.rgb888(167, 191, 246));
				}
				
				Vector3 pos = new Vector3(x*5f+2.5f, 0f, z*5f +2.5f);
				
				Cube cube = new Cube(modelBuilder, color, pos, x, z);
				this.cubes[x][z]= cube;


			}
		}
		
		
		ModelLoader loader = new ObjLoader();
		
		Model pawnModel = loader.loadModel(Gdx.files.classpath("meshes/sphere.obj"));
		Model bishopModel = loader.loadModel(Gdx.files.classpath("meshes/circle.obj"));
		Model knightModel = loader.loadModel(Gdx.files.classpath("meshes/circle.obj"));
		Model rookModel = loader.loadModel(Gdx.files.classpath("meshes/circle.obj"));
		Model queenModel = loader.loadModel(Gdx.files.classpath("meshes/circle.obj"));
		Model kingModel = loader.loadModel(Gdx.files.classpath("meshes/circle.obj"));
		
		pieces = new Piece[width][height];

		// Pawns
		for (int c = 0; c < 8; c++) {
			pieces[c][1] = new Piece(PieceType.PAWN, true, pawnModel); // White
			pieces[c][6] = new Piece(PieceType.PAWN, false, pawnModel); // Black
		}
		
		// Other White pieces
		pieces[2][0] = new Piece(PieceType.BISHOP, true, bishopModel);
		pieces[1][0] = new Piece(PieceType.KNIGHT, true, knightModel);
		pieces[0][0] = new Piece(PieceType.ROOK, true, rookModel);
		pieces[5][0] = new Piece(PieceType.BISHOP, true, bishopModel);
		pieces[6][0] = new Piece(PieceType.KNIGHT, true, knightModel);
		pieces[7][0] = new Piece(PieceType.ROOK, true, rookModel);
		pieces[3][0] = new Piece(PieceType.QUEEN, true, queenModel);
		pieces[4][0] = new Piece(PieceType.KING, true, kingModel);
		
		// Other Black pieces
		pieces[2][7] = new Piece(PieceType.BISHOP, false, bishopModel);
		pieces[1][7] = new Piece(PieceType.KNIGHT, false, knightModel);
		pieces[0][7] = new Piece(PieceType.ROOK, false, rookModel);
		pieces[5][7] = new Piece(PieceType.BISHOP, false, bishopModel);
		pieces[6][7] = new Piece(PieceType.KNIGHT, false, knightModel);
		pieces[7][7] = new Piece(PieceType.ROOK, false, rookModel);
		pieces[3][7] = new Piece(PieceType.QUEEN, false, queenModel);
		pieces[4][7] = new Piece(PieceType.KING, false, kingModel);
		
		
		
		for (int c = 0; c < 8; c++) {
			for (int r = 0; r < 8; r++) {
				if (pieces[c][r] != null) {
					pieces[c][r].instance.transform.setToTranslation(r*5+2.5f, 3f, c*5 +2.5f);
				}
			}
		}

	}

	public Cube[][] getCubes() {
		return cubes;
	}

	public Piece[][] getPieces() {
		return pieces;
	}




	
	
}
