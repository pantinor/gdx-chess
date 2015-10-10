package org.antinori.chess;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;

public class Piece {

    private PieceType type;
    private Player player;
    private ModelInstance instance;
    private Model model;
    private Vector3 pos;
    private boolean placed = false;

    public Piece(PieceType t, Player w, Model model, Vector3 pos) {
        this.player = w;
        this.model = model;
        this.type = t;
        this.instance = new ModelInstance(model);

        if (w == Player.BLACK) {
            //this.instance.materials.get(0).set(TextureAttribute.createDiffuse(Board.darkTexture));
            this.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.DARK_GRAY));
        } else {
            //this.instance.materials.get(0).set(TextureAttribute.createDiffuse(Board.lightTexture));
            this.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.LIGHT_GRAY));
        }

        setPos(pos);

        this.instance.calculateTransforms();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", type, player, placed);
    }

    public PieceType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public ModelInstance getInstance() {
        return instance;
    }

    public Model getModel() {
        return model;
    }

    public Vector3 getPos() {
        return pos;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setInstance(ModelInstance instance) {
        this.instance = instance;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void setPos(Vector3 pos) {
        this.pos = new Vector3(pos.x, Board.MODEL_HEIGHT, pos.z);
        this.instance.transform.setToTranslation(this.pos);
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

}
