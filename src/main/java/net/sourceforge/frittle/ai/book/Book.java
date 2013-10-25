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

package net.sourceforge.frittle.ai.book;

import net.sourceforge.frittle.GameState;
import net.sourceforge.frittle.InvalidFENException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A book is a hashtable of known states and probably moves
 * from that state. This is most effectively used in openings and is
 * therefore also sometimes referred to as the Opening Book.
 */
public class Book
{
    /**
	 * The hashtable used to store the book.
     * The key is a 64-bit zobrist-hash for the game state (wrapped in a Long)
     * and the value is a vector of possible weighted moves
	 */
	private Hashtable<Long,Vector<BookMove>> hashtable;
    
    /**
     * Regular expression for weighted move entries in the book
     */
    Pattern weightedMovePattern = Pattern.compile("([a-h][1-8])([a-h][1-8])([qrnbQRNB])?\\{(\\d+)\\}");

    /**
     * Creates a book using the given book file
     *
     * @throws IOException              in case file cannot be read properly
     * @throws BookFormatException      if any string in the book is invalid
     */
    public Book(String filename) throws IOException, BookFormatException
    {
        // Initialize hashtable
        hashtable = new Hashtable<Long,Vector<BookMove>>(120000);
        // Initialize reader
        InputStream bookStream = getClass().getResourceAsStream(filename);
        if(bookStream == null)
            throw new FileNotFoundException();
        BufferedReader br = new BufferedReader(new InputStreamReader(bookStream));
        for(int line = 1; br.ready(); line+=2)
        {
            // For each line
            String FEN = br.readLine();
            // Check if end of file
            if(FEN.equals("#END#"))
            {
                break;
            }

            // Now try to find which position this entry is for
            GameState state;
            try
            {
                state = new GameState(FEN);
            }
            catch(InvalidFENException e)
            {
                throw new BookFormatException(e.getMessage(), line);
            }

            // Now get the moves from that position
            String moves = br.readLine();
            Vector<BookMove> weightedMoves = new Vector<BookMove>();
            Matcher matcher = weightedMovePattern.matcher(moves);
            // Try to search for moves such as e2e4{1234}
            while(matcher.find())
            {
                // Get the move object from the string notation
                String moveStr;
                if(matcher.group(3) != null) // with promotion
                    moveStr = matcher.group().substring(0, 5);
                else                        // no promotion
                    moveStr = matcher.group().substring(0,4);
                // Get the weight
                int weight = Integer.parseInt(matcher.group(4));
                if(weight == 0) // Bad move
                    continue;
                BookMove bookMove = new BookMove(moveStr, weight);
                weightedMoves.addElement(bookMove);
            }

            // If we reached here without exceptions, it means we have a
            // Vector of moves from the current state
            hashtable.put(new Long(state.hash()), weightedMoves);
        }
        br.close();
    }

    /**
     * Searches the book for a possible move from given state and returns
     * one if found.
     *
     * @param   state   the <code>GameState</code> to search in the book
     * @return  a <code>Move</code> in string notation if a possible move was found; <code>null</code> otherwise
     */
    public String getMoveFrom(GameState state)
    {
        // Try to retreive an entry for the given state
        Vector<BookMove> weightedMoves = hashtable.get(new Long(state.hash()));
        if(weightedMoves == null || weightedMoves.size() == 0)
            return null;

        // If we got a vector of weighted moves, randomize a bit and return one

        // Randomization Algorithm:
        // 1. Sum up all weights
        int sumWeights = 0;
        for(int i=0; i<weightedMoves.size(); i++)
            sumWeights += weightedMoves.elementAt(i).weight;
        // 2. Generate a random number between 0 and sumWeights
        int random = (int)(Math.random() * sumWeights);
        // 3. Initialize x = 0
        int x = 0;
        // Foreach weighted move, check if the random number lies between
        // x and x+weight. Increment x by the weight if not.
        for(int i=0; i<weightedMoves.size(); i++)
        {
            if(random < (x+weightedMoves.elementAt(i).weight))
                return weightedMoves.elementAt(i).moveStr;
            else
                x = x + weightedMoves.elementAt(i).weight;
        }
        // Technically we shouldn't reach this point here
        throw new RuntimeException("Unexpected Error by book randomizer");
    }

    /**
     * Get the number of elements in the book
     * @return  the no. of entries in the book
     */
    public int size()
    {
        return hashtable.size();
    }
}

/**
 * A move stored in the opening book.
 */
class BookMove
{
    /** The relative weight of this move.  */
    public int weight;
    /** The string representation of the move */
    public String moveStr;

    /**
     * Sets up a new move with given parameters
     * @param   moveStr     a string form of the move (eg.e2e4)
     * @param   weight      relative weight for this move
     */
    public BookMove(String  moveStr, int weight)
    {
        this.moveStr = moveStr;
        this.weight = weight;
    }
}