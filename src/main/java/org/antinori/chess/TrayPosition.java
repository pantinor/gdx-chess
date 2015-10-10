package org.antinori.chess;

import com.badlogic.gdx.math.Vector3;

public class TrayPosition {

    private PieceType type;
    private Player player;
    private Vector3 position;
    private boolean taken = false;

    public TrayPosition(PieceType pt, Player pl, Vector3 v1) {

        this.type = pt;
        this.player = pl;
        this.taken = false;
        this.position = v1;

    }

    public PieceType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public Vector3 getPosition() {
        return position;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

}
