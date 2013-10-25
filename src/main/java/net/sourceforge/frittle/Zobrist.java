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
 * This static class provides a method of generating a 64-bit hash code
 * for a GameState object.
 */
public class Zobrist
{
	/**
	 * The set of random integers (bit strings) for the hash function
	 */
	private static long  bitStrings[];
	
	// Initialize bitstrings
	static
	{
		bitStrings = new long [907];
		java.util.Random random = new java.util.Random();
		for(int i=0; i<907; i++)
		{
			bitStrings[i] = random.nextLong();
		}
	}
	
	
	/**
	 * Static method that generates a zobrist hash from a GameState using the
	 * bitString set generated statically above.
	 *
	 * @param 	state 		the GameState from whom to construct a key
	 * @return	the hash code
	 */
	public static long hash(GameState state)
	{
		// Initialize hash
		long hash = 0;
		// First the board position
		// There are 832 bitStrings for this part
		// 13 different ways each square can be
		Piece p;
		int i,j=0,k;
		for(i=0; i<64; i++)
		{
			p = state.getBoard()[i];
			if(p == null)
			{
				j = 0;
			}
			else
			{
				switch(p.getPlayer())
				{
					case WHITE:
						switch(p.getType())
						{
							case PAWN:
								j = 1;
							break;
							case KNIGHT:
								j = 2;
							break;
							case BISHOP:
								j = 3;
							break;
							case ROOK:
								j = 4;
							break;
							case QUEEN:
								j = 5;
							break;
							case KING:
								j = 6;
							break;
						}
					break;
					case BLACK:						
						switch(p.getType())
						{
							case PAWN:
								j = 7;
							break;
							case KNIGHT:
								j = 8;
							break;
							case BISHOP:
								j = 9;
							break;
							case ROOK:
								j = 10;
							break;
							case QUEEN:
								j = 11;
							break;
							case KING:
								j = 12;
							break;
						}
					break;
				}
			}
			// Now we have 0 <= i < 64 and 0 <= j < 13
			// We need k such that 0 <= k < 832
			// therefore,
			k = i*13 + j;
			// Now XOR the corresponding bitString to the hash
			hash = hash ^ bitStrings[k];
		}
		// Now we see the other components
		if( state.getActivePlayer() == Player.WHITE )
			hash = hash ^ bitStrings[832];
		else
			hash = hash ^ bitStrings[833];
		if( state.canCastleWhiteKingside() )
			hash = hash ^ bitStrings[834];
		else
			hash = hash ^ bitStrings[835];
		if( state.canCastleBlackKingside() )
			hash = hash ^ bitStrings[836];
		else
			hash = hash ^ bitStrings[837];
		if( state.canCastleWhiteQueenside() )
			hash = hash ^ bitStrings[838];
		else
			hash = hash ^ bitStrings[839];
		if( state.canCastleBlackQueenside() )
			hash = hash ^ bitStrings[840];
		else
			hash = hash ^ bitStrings[841];
		if( state.getEnPassant() == -1 )
			hash = hash ^ bitStrings[842];
		else
			hash = hash ^ bitStrings[843+state.getEnPassant()];
		// That's it! We're done..
		return hash;
	}

   
}