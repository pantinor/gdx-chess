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

package net.sourceforge.frittle;

/**
 * The Piece class represents a chess entity on the board.
 */
public class Piece
{
	
	/**
	 * The owner of the piece, WHITE or BLACK.
	 */
	protected Player player;
	
	/**
	 * The type of the piece.
	 */
	protected PieceType type;
	
	/**
	 * Creates a new piece on the board of given color
	 * and given type on the given position
	 *
	 * @param type		the type of the piece (King, Queen, Rook, Bishop, kNight or Pawn)
	 * @param player	the owner of the piece (White or Black)
	 */
	public Piece(PieceType type, Player player)
	{
		this.type = type;
		this.player = player;
	}
	
	/**
	 * @return 	the player who owns the piece
	 *
	 */
	public Player getPlayer()
	{
		return this.player;
	}
	
	/**
	 * @return 	the type of the piece.
	 *
	 */
	public PieceType getType()
	{
		return this.type;
	}
	
	/**
	 * Generates a character code of the piece.
	 * White is in uppercase (eg. Q) and Black is in lowercase (eg. q)
	 *
	 * @return		the case sensitive character code for the piece
	 */
	public char getChar()
    {
        switch(this.player)
        {
            case WHITE:
                return getUpperChar();
            case BLACK:
                return getLowerChar();
        }
        return '-';
    }
	
	/**
	 * Generates the character code of the piece in uppercase.
	 *
	 * @return		the character code for the piece (in uppercase)
	 */
	public char getUpperChar()
    {
        return this.type.getUpperChar();
    }

    /**
	 * Generates the character code of the piece in lowercase.
	 *
	 * @return		the character code for the piece (in lowercase)
	 */
	public char getLowerChar()
    {
        return this.type.getLowerChar();
    }
}
	