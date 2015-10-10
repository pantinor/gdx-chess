/*
 Frittle - Chess Engine for WinBoard/XBoard [http://frittle.sourceforge.net]
 Copyright (C) 2009 Rohan Padhye <verminox@gmail.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.antinori.chess;

/**
 * Color is the enumuration whose value determines whether the player is playing
 * White or Black in the chess game.
 */
public enum Player {

    WHITE,
    BLACK;

    /**
     * Gets the opponent player.
     *
     * @return WHITE if the player is Black, or BLACK if the player is White.
     */
    public Player opponent() {
        if (this == WHITE) {
            return BLACK;
        } else {
            return WHITE;
        }
    }
}
