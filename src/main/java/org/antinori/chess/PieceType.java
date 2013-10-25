package org.antinori.chess;

public enum PieceType {
		
	PAWN("Pa","P"), 
	ROOK("Ro","R"), 
	KNIGHT("Kn","N"), 
	BISHOP("Bi","B"), 
	QUEEN("Qu","Q"), 
	KING("Ki","K");

	public String name;
	public String type;

	private PieceType(String name, String type) {
		this.name = name;
		this.type = type;
	}

}
