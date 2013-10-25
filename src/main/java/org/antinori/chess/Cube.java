package org.antinori.chess;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Cube {

	private int block_x;
	private int block_y;
	private Model model;
	private ModelInstance instance;
	private BoundingBox boundingBox;
	private Vector3 pos;
	private Color color; 
	//for debugging
	private ModelInstance outline;

	public Cube(ModelBuilder modelBuilder, Color color, Vector3 pos, int x, int y) {
		
		this.block_x = x;
		this.block_y = y;
		this.pos = pos;
		this.color = color;
		
		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(color)), Usage.Position | Usage.Normal);
		instance = new ModelInstance(model, pos);
		
		float h = 2.5f;
		Vector3[] vertices = new Vector3[4];
		vertices[0] = new Vector3(pos.x-2.5f,h,pos.z-2.5f);
		vertices[1] = new Vector3(pos.x-2.5f,h,pos.z+2.5f);
		vertices[2] = new Vector3(pos.x+2.5f,h,pos.z+2.5f);
		vertices[3] = new Vector3(pos.x+2.5f,h,pos.z-2.5f);
		
		boundingBox = new BoundingBox();
		boundingBox.set(vertices);
				
		//outline = createBoxOutline(vertices);
		
	}
	

    
	private ModelInstance createBoxOutline(Vector3[] v) {

		Model model = null;

		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		
		MeshPartBuilder builder = modelBuilder.part("box", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		
		builder.setColor(Color.PINK);
		
		float y = v[0].y;
		
		builder.line(v[0].x,y,v[0].z,v[1].x,y,v[1].z);
		builder.line(v[1].x,y,v[1].z,v[2].x,y,v[2].z);
		builder.line(v[2].x,y,v[2].z,v[3].x,y,v[3].z);
		builder.line(v[3].x,y,v[3].z,v[0].x,y,v[0].z);

		model = modelBuilder.end();
		return new ModelInstance(model);
	}

	public int getBlock_x() {
		return block_x;
	}

	public int getBlock_y() {
		return block_y;
	}

	public Model getModel() {
		return model;
	}

	public ModelInstance getInstance() {
		return instance;
	}

	public Vector3 getPos() {
		return pos;
	}

	public void setBlock_x(int block_x) {
		this.block_x = block_x;
	}

	public void setBlock_y(int block_y) {
		this.block_y = block_y;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public void setInstance(ModelInstance instance) {
		this.instance = instance;
	}

	public void setPos(Vector3 pos) {
		this.pos = pos;
	}
	
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public Color getColor() {
		return color;
	}

	public ModelInstance getOutline() {
		return outline;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setOutline(ModelInstance outline) {
		this.outline = outline;
	}


	public void changeColor(boolean flag) {
		if (flag) {
			this.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
		} else {
			this.instance.materials.get(0).set(ColorAttribute.createDiffuse(color));		
		}
	}
	
	public boolean isInRegion(int dx, int dz) {
		
		int a = block_x*5+5;
		int b = block_y*5+5;
		
		System.out.println("dx:"+dx+" dz:"+dz+ " a:"+a+" b:"+b);
		
		boolean within = false;
		if (dx <= a && dz <= b) {
			within = true;
		}
		
		return within;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Cube)) 
			return false;
		
		Cube tmp = (Cube)obj;
		if (tmp.block_x == this.block_x && tmp.block_y == this.block_y) 
			return true;
		
		return false;
	}


}
