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

package net.sourceforge.frittle.ai;

import net.sourceforge.frittle.GameState;
import net.sourceforge.frittle.Move;

/**
 * A Transposition Table is a hashtable of previously evaluated positions
 * and information about their evaluation.
 */
public class TranspositionTable
{
    /** The hash values of the game state to which the entry corresponds */
    private long[] stateHash;
    /** The evaluation score for the state to which it corresponds */
    private short[] score;
    /** The depth to which the state to which the entry corresponds has been searched */
    private byte[] depth;
    /** A flag indicating whether the score is exact, a bound or invalid */
    private byte[] flag;
    /** A hash value for the best move. Short.MAX_VALUE is used for null values */
    private short[] bestMoveHash;

    /** The number of items in the hashtable  */
    private int capacity;

	
	/**
	 * Construct a new TranspositionTable of the given capacity.
	 *
	 * @param	capacity	the size of the transposition table
	 */
	public TranspositionTable(int capacity)
	{
        this.capacity = capacity;
        this.stateHash = new long[capacity];
        this.score = new short[capacity];
        this.depth = new byte[capacity];
        this.flag = new byte[capacity];
        this.bestMoveHash = new short[capacity];
	}
	
	/**
	 * Store an evaluated GameState into the transposition table.
	 *
	 * @param	hash            the 64-bit hash of the game state to store
     * @param   depth           the depth to which this state has been search
     * @param   score           the score of the evaluation / search
     * @param   flag            the type of evaluation
     * @param   bestMoveHash    the 16-bit hash of the best move from this state
	 */
	public void store(long stateHash, int depth, int score, int flag, short bestMoveHash)
	{
        int index = index(stateHash);
        this.stateHash[index] = stateHash;
        this.depth[index] = (byte)depth;
        this.score[index] = (short)score;
        this.flag[index] = (byte)flag;
        this.bestMoveHash[index] = bestMoveHash;
	}

    /**
     * Whether or not the entry for the given state exists in the hashtable
     * @param hash  the 64-bit hash of the state
     * @return  <code>true</code> if a valid entry exists in the hashtable, <code>false</code> if not
     */
    public boolean exists(long hash)
    {
        return (stateHash[index(hash)]==hash);
    }

    /**
     * Returns the score for the given state
     * @param hash  the 64-bit hash of the state
     * @return  the score in centipawns
     */
    public short getScore(long hash) {
        return score[index(hash)];
    }

    /**
     * Returns the depth to which the given state was searched
     * @param hash  the 64-bit hash of the state
     * @return the depth in plies
     */
    public byte getDepth(long hash) {
        return depth[index(hash)];
    }

    /**
     * Returns the flag of the node type
     * @param hash  the 64-bit hash of the state
     * @return a flag for whether the node was searched exactly or a bound is stored
     */
    public byte getFlag(long hash) {
        return flag[index(hash)];
    }

    /**
     * Returns the hash of the best move from the given state
     * @param hash  the 64-bit hash of the state
     * @return  the 16-bit hash of the best move
     */
    public short getBestMoveHash(long hash) {
        return bestMoveHash[index(hash)];
    }
	
	/**
	 * Get the number of elements in the transposition table.
	 *
	 * @return	the size of the table
	 */
	public int size()
	{
		return this.capacity;
	}

    /**
     * Convert hash code into appropriate index for table by using the modulo function.
     * @param hash      the hash code of the game state
     * @return          the index in the table (array)
     */
    private int index(long hash)
    {
        return (int)(Math.abs(hash) % this.capacity);
    }
		
}


