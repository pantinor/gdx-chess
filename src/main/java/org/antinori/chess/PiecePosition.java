package org.antinori.chess;

import net.sourceforge.frittle.PieceType;
import net.sourceforge.frittle.Player;

import com.badlogic.gdx.math.Vector3;

public enum PiecePosition {
	
	WHITE_PAWN (PieceType.PAWN, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_PAWN (PieceType.PAWN, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	
	WHITE_ROOK (PieceType.ROOK, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_ROOK (PieceType.ROOK, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),

	WHITE_KNIGHT (PieceType.KNIGHT, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_KNIGHT (PieceType.KNIGHT, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	
	WHITE_BISHOP (PieceType.BISHOP, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_BISHOP (PieceType.BISHOP, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	
	WHITE_QUEEN (PieceType.QUEEN, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_QUEEN (PieceType.QUEEN, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),

	WHITE_KING (PieceType.KING, Player.WHITE, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f)),
	BLACK_KING (PieceType.KING, Player.BLACK, new Vector3(1f,1f,1f), new Vector3(1f,1f,1f));
	
	private PieceType pt;
	private Player pl;
	private Vector3 originalPosition;
	private Vector3 trayPosition;

	
	private PiecePosition(PieceType pt, Player pl, Vector3 v1, Vector3 v2) {
		
		this.pt = pt;
		this.pl = pl;
		this.originalPosition = v1;
		this.trayPosition = v2;
		
	}


	public PieceType getPt() {
		return pt;
	}


	public Player getPl() {
		return pl;
	}


	public Vector3 getOriginalPosition() {
		return originalPosition;
	}


	public Vector3 getTrayPosition() {
		return trayPosition;
	}


	
	

}
