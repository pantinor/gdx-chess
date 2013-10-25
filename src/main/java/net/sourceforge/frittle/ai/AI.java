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

import net.sourceforge.frittle.*;
import net.sourceforge.frittle.ai.book.Book;
import net.sourceforge.frittle.ai.book.BookFormatException;
import java.util.Timer;

/**
 * The chess engine uses Artificial Intelligence techniques to play the best
 * move from a given position. An <code>AI</code> object is an agent which
 * controls the actions to take when asked to find a good move to play. The
 * main techniques used are searching the game tree and caluclating a static
 * evaluation score for a given position.
 */
public class AI
{
    /** Maximum depth to which to search */
	public int searchDepth;
    /** Flag indicating whether or not to print thinking */
	public boolean showThinking;
    /** If forceMode mode is on, AI does not play or think or ponderMode */
    public boolean forceMode;
    /** If ponderMode mode is on, the AI thinks on opponent's time */
    public boolean ponderMode;
    /** The clock associated with the engine */
    public Clock clock;
    /** Hashtable containing information about previously searched nodes */
	TranspositionTable transpositionTable;
    /** Opening book */
    Book book;
    /** The current ongoing search */
    Search search;
    /** A timer used to automatically interrupt search if it is taking too long */
    Timer timer;
	
	/**
	 * Creates a new AI engine for the given Game.
	 *
	 * @param game	the Game for which this AI should think.
	 */
	public AI()
	{
		// Initialize Transposition Table and Opening Book
        try
        {
            Runtime runtime = Runtime.getRuntime();
            long start = runtime.totalMemory()-runtime.freeMemory();
            Frittle.debug("Initializing hashtable...");
            transpositionTable = new TranspositionTable(4194304);
            long afterHash = runtime.totalMemory()-runtime.freeMemory();
            int hashtableMB = (int)(afterHash-start)/(1024*1024);
            float positions = Math.round(transpositionTable.size()/100000.0f)/10.0f; // .1 precision
            Frittle.debug("Hashtable ready [" + positions + " million positions in " + hashtableMB + "MB]");
            Frittle.debug("Loading book...");
            book = new Book("tiny.book");
            long afterBook = runtime.totalMemory()-runtime.freeMemory();
            int bookMB = (int)(afterBook-afterHash)/(1024*1024);
            Frittle.debug("Book loaded [" + book.size() + " positions in " + bookMB + "MB]");
        }
        catch(java.io.FileNotFoundException e)
        {
            Frittle.debug("ERROR - Could not find book file!!!");
        }
        catch(BookFormatException e)
        {
            Frittle.debug("ERROR - " + e.getMessage());
        }
        catch(java.io.IOException e)
        {
            Frittle.debug(e.toString());
        }
        // Initialize other paramaters
		this.searchDepth = 128; // Why not? Unless 'sd' is forced
        resetModes();
        // Initially set clock to full base time of the current game
        clock = new Clock(Frittle.getGame().getClockFormat().getBaseTime());
	}

    /**
     * Resets settings for a fresh game
     */
    public void resetModes()
    {
		showThinking = true;
        forceMode = false;
        ponderMode = false;
    }

	/**
	 * Asks the AI to make a move for the current active player
	 * in the game.
	 */
	public void go()
	{
        // Just in case we were in forceMode mode, change that
        this.forceMode = false;
		// Use current GameState
		GameState state = Frittle.getGame().getCurrentState();
        // Check book
        String bookMoveStr = null;
        if(book != null)
            bookMoveStr = book.getMoveFrom(state);
        if(bookMoveStr != null)
        {
            // Ideally we tell the protocol first, but book moves generally
            // don't announce results, so its safe. On the other hand, the
            // book move may not be legal if the book was bad
            if(Frittle.getGame().doMove(bookMoveStr))
                Frittle.getProtocol().AIMove(bookMoveStr);
            else
                Frittle.getProtocol().error("Illegal move in book");

            return;
        }

        // Now to rely on searching for the best move
        long searchTime = searchTime();
        // There might already be a ponder search going on for this position
        if(search != null && search.hash() == state.hash())
        {
            // We have a match! Opponent played the move we were pondering on
            // The search is now considered real, not a ponder
            this.search.setPonder(false);
            if(search.isAlive()) // Still thinking
            {
                // tell search how much time it should take
                search.setSearchTime(searchTime); // Will exit if it realises it cannot go deeper
                // Schedule a timeout
                this.timer = new Timer();
                this.timer.schedule(new SearchTimeout(this, this.search), searchTime);
            }
            else // Pondering is over
            {
                moveNow();
            }
        }
        else // There was no ponder or the opponent did not play the expected move
        {
            // Destroy any search threads that may be going on
            destroyThreads();
            // Start a new search thread
            search = new Search(this, state, this.searchDepth, searchTime);
            search.start();
            // Schedule a timer that will interrupt the search after sometime
            this.timer = new Timer();
            this.timer.schedule(new SearchTimeout(this, this.search), searchTime);
        }
	}

	/**
     * Forces the AI the best move it has found so far from its search.
     * If there is no ongoing search this method will do nothing.
     */
    public synchronized void moveNow()
    {
        if(search != null && !search.isPonder()) // Ponder searches aren't active unless we go()
        {
            // What score can we expect to achieve?
            int score = search.getBestScoreSoFar();
            if(score < -Eval.checkmateThreshold)
            {
                // Resign gracefully
                resign();
                // Terminate the search thread and pending timer tasks
                this.destroyThreads();
            }
            else
            {
                // Find best move
                String bestMove = search.getBestMoveSoFar();
                // Respond
                if( bestMove != null )
                {
                    // Write before doMove() otherwise results are declared before "move xyz" for checkmates, etc.
                    Frittle.getProtocol().AIMove(bestMove);
                    Frittle.getGame().doMove(bestMove); // This might return false if user interrupted game state
                }
                else
                {
                    Frittle.getProtocol().error("Nothing to play");
                }
                // Terminate the search thread and pending timer tasks
                this.destroyThreads();
                // If pondering is on, start pondering
                if(ponderMode)
                    ponder();
            }

        }
    }

    /**
     * Think on the opponent's time. Try to calculate what will happen if
     * the opponent plays the expected reply (retreived from the principal
     * variation of the previous search through the hashtable).
     */
    private void ponder()
    {
        // If AI made the move, it is reflected in the main state
		GameState state = Frittle.getGame().getCurrentState();
        // Do we have an expected reply?
        long stateHash = state.hash();

        if( transpositionTable.exists(stateHash)==false )
            return; // No hashtable entry, cannot ponderMode
        short replyHash = transpositionTable.getBestMoveHash(stateHash);
        Move expectedReply = null;
        for(Move reply : state.generateMoves())
        {
            if(reply.hash()==replyHash)
            {
                expectedReply = reply;
                break;
            }
        }
        if(expectedReply==null) // Expected reply not possible?
            return;

        // Now here we have an expected reply, so lets temporarily make the move
        state.doMove(expectedReply); // has to be legal

        // It's possible that we have a book move, then don't ponderMode
        if(book != null && book.getMoveFrom(state) != null) // We have a book move
        {
            // Fisrt safely undo the move on the state then exit
            state.undoMove(expectedReply);
            return;
        }

        // OK, so here our 'state' variable has been deformed to after
        // the expected reply. Now we create a search thread that will copy this
        // state and start thinking.
        this.search = new Search(this, state, this.searchDepth, -1);
        this.search.setPonder(true);
        this.search.start();
        // No timer is required, the more time the opponent takes the better

        // Safely undo the move on the current state
        state.undoMove(expectedReply);

        // Announce it
        Frittle.debug("Pondering on " + expectedReply.toString());
        // That's about it in the pondering business...!

    }

    /**
     * Estimates an amount of time to spend on thinking about the next move.
     * @return  the amount of time to spend on thinking about the next move in
     *          milliseconds.
     */
    private long searchTime()
    {
        int movesPerSession = Frittle.getGame().getClockFormat().getMovesPerSession();
        if(movesPerSession > 0) // Tournament time
        {
            // No. of moves elapsed = Move number - 1 (because move number is for next move)
            int elapsedMoves = Frittle.getGame().getMoveNumber()-1;
            int movesLeftInSession = movesPerSession - (elapsedMoves % movesPerSession);
            long timeLeft = this.clock.left();
            // Use a formula that will decay with move number and
            // at the last move will take up the entire time
            long timeToUse = (timeLeft*2) / (movesLeftInSession+1);
            return timeToUse;
        }
        else // Fischer time
        {
            long increment = Frittle.getGame().getClockFormat().getIncrement();
            long baseTimeLeft = this.clock.left();
            // Use full increment time plus a residual fraction of base time
            // (which will be left on the clock right now)
            long amountToUseFromBase = baseTimeLeft / 20;
            long timeToUse = amountToUseFromBase + increment;
            // It's possible that because of a large increment and small base
            // value timeToUse is more than the time left. In that case,
            // we do not consider we have increments and only use part of the base
            if(timeToUse < baseTimeLeft)
                return timeToUse;
            else
                return amountToUseFromBase;
        }
    }

    /**
     * Forfeit the current game. To be used instead of moveNow() when appropriate.
     */
    public void resign()
    {
        // Terminate the search thread and pending timer tasks
        this.destroyThreads();
        // Signal the resignation
        Frittle.getGame().resign();        
    }    

    /**
     * When the AI object is destroyed, the associated search thread should
     * also be terminated.
     */
    @Override public void finalize()
    {
        destroyThreads();
    }

    /**
     * Terminates any threads that might have been started
     */
    public void destroyThreads()
    {
        // Destroy search thread
        if(this.search != null)
        {
            this.search.interrupt();
            this.search = null;
        }
        // Cancel all pending tasks from this timer
        if(this.timer != null)
        {
            this.timer.cancel();
            this.timer = null;
        }
    }
}