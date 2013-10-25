package org.antinori.chess;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Piece {
	
	String[] COORD_X = { "a", "b", "c", "d", "e", "f", "g", "h" };
	
	public PieceType type;
	public boolean white;
	public ModelInstance instance;
	public Model model;

	public Piece(PieceType t, boolean w, Model model) {
		this.white = w;
		this.model = model;
		this.type = t;
		this.instance = new ModelInstance(model);
	}

	@Override
	public String toString() {
		return String.format("%s %s", type, white);
	}
	
	

}