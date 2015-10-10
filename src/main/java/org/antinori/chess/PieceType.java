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
 * This enumuration determines the type of a piece (King, Queen, Rook, Bishop,
 * kNight or Pawn).
 */
public enum PieceType {

    PAWN {
                public char getUpperChar() {
                    return 'P';
                }

                public char getLowerChar() {
                    return 'p';
                }
            }, KNIGHT {
                public char getUpperChar() {
                    return 'N';
                }

                public char getLowerChar() {
                    return 'n';
                }
            }, BISHOP {
                public char getUpperChar() {
                    return 'B';
                }

                public char getLowerChar() {
                    return 'b';
                }
            }, ROOK {
                public char getUpperChar() {
                    return 'R';
                }

                public char getLowerChar() {
                    return 'r';
                }
            }, QUEEN {
                public char getUpperChar() {
                    return 'Q';
                }

                public char getLowerChar() {
                    return 'q';
                }
            }, KING {
                public char getUpperChar() {
                    return 'K';
                }

                public char getLowerChar() {
                    return 'k';
                }
            };

    public abstract char getUpperChar();

    public abstract char getLowerChar();

    public static PieceType convert(char c) {
        for (PieceType pt : PieceType.values()) {
            if (pt.getLowerChar() == c) {
                return pt;
            }
            if (pt.getUpperChar() == c) {
                return pt;
            }
        }
        return PAWN;
    }
}
