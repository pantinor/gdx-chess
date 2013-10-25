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

import java.util.Stack;

/**
 * Game is an active chess game in play.
 * This class contains information about the current GameState
 * and handles logic of the game such as winning, losing, draws, etc.
 * and also provides communication between the Players to make
 * moves and offer draws, resign, etc.
 */
public class Game
{
    /** The current state of the game */
    private GameState currentState;

    /** A list of legal moves from the current position */
    private MoveList legalMoves;

	/** A stack of moves (plies) of the history of the game */
	private Stack<Move> moveStack;

    /** The number of half moves (plies) since the last pawn move or capture */
    private int reversiblePliesCount;

    /** The move number (initially 1, then increments after each of black's play) */
    private int moveNumber;

	/** Status of the game. Whether it is over or ongoing. */
	private boolean gameOver;

    /** The time controls for this game */
    private ClockFormat clockFormat;

	/**
	 * Creates a new chess Game from the default position.
	 */
	public Game()
	{
        setState(new GameState());
	}


    /**
     * Set the current position of the board manually. This will clear
     * the stack so no undo will be possible. Also, there is a chance that this
     * state might be illegal (eg. no kings or more than one king a side)
     *
     * @param   state       the new game state to set
     */
    public void setState(GameState state)
    {
		currentState = state;
        legalMoves = currentState.generateMoves();
		moveStack = new Stack<Move>();
        reversiblePliesCount = 0;
        moveNumber = 1;
		gameOver = false;
        // By default set clock format to 40 moves in 20 minutes
        clockFormat = new ClockFormat(40,(long)(20*60*1000));
    }

	/**
     * Get a reference to the current state of the board
     *
	 * @return	the current GameState object
	 */
	public GameState getCurrentState()
	{
		return this.currentState;
	}

    /**
     * The time controls for this game.
     * @return the clockFormat
     */
    public ClockFormat getClockFormat()
    {
        return clockFormat;
    }

    /**
     * Change the time controls for this game.
     * @param clockFormat   the clock format to set
     */
    public void setClockFormat(ClockFormat clockFormat)
    {
        this.clockFormat = clockFormat;
    }

	/**
     * Get a list of all legal moves from the current state of the board
     *
	 * @return	a vector of legal moves from this position
	 */
	public MoveList getLegalMoves()
	{
		return this.gameOver ? new MoveList() : this.legalMoves;
	}

    /**
     * The move history in string format (eg. 1. e2e4 e7e5 2. b1c3)
     *
     * @return  the move history as a String
     */
    public String getMoveHistoryString()
    {
        StringBuffer sb = new StringBuffer();
        Move move;
        int moveNum=0, i=0;
        for(i=0; i<moveStack.size(); i++)
        {
            move = moveStack.elementAt(i);
            if(i % 2 == 0)
            {
                ++moveNum;
                sb.append(moveNum + ". ");
            }
            sb.append(move.toString() + " ");
        }
        return sb.toString();
    }

    /**
     * Gets a reference to the current move history stack
     *
     * @return  the move history stack
     */
    Stack<Move> getMoveStack()
    {
        return this.moveStack;
    }

    /**
     * The number of halfmoves since the last capture or pawn promotion
     * @return  the half move count
     */
     public int getReversiblePliesCount()
     {
         return this.reversiblePliesCount;
     }
     /**
      * The current move number in the game. Increments each time black plays.
      * Initially (for new game or when board is manually set) it is 1.
      * @return     the move number
      */
     public int getMoveNumber()
     {
         return this.moveNumber;
     }

	/**
	 * Performs a move and changes the current state of the game.
	 *
	 * @param	notation                the move in notation (eg. e2e4)
     * @return  true if move was legal and was performed, false if move was
     * illegal and hence not performed
	 */
	public boolean doMove(String notation)
	{
        // You can't move if the game is over
		if(this.gameOver)
            return false;

        // Now test if the move to do is right
        for(Move move : legalMoves)
        {
            if(move.toString().equals(notation)) // We have a match! The move is legal
            {
                // Update current state and move history
                this.moveStack.push(move);
                currentState.doMove(move);
                // Update legal moves list
                legalMoves = currentState.generateMoves();
                // Increment move number if after the move white is set to play
                if(currentState.getActivePlayer() == Player.WHITE)
                {
                    moveNumber++;
                }
                // Increment reversible move counter if move was not a capture or pawn push
                if(move.capturedPiece == null && currentState.getBoard()[move.dest].getType() != PieceType.PAWN)
                {
                    reversiblePliesCount++;
                    // @todo change this into claimDraw() and offerDraw()
                    // If 50 such moves (100 plies) have occurred, its a draw
                    if(reversiblePliesCount >= 100)
                    {
                        declareResult("1/2-1/2 {50-move rule}");
                    }
                }
                // Otherwise reset to zero
                else
                {
                    reversiblePliesCount = 0;
                }
                // Check if there are any moves further, or else the game is over
                if(legalMoves.isEmpty())
                {
                    if(currentState.isInCheck(currentState.getActivePlayer()))
                    {
                        if(currentState.getActivePlayer()==Player.WHITE)
                            declareResult("0-1 {Black mates}");
                        else
                            declareResult("1-0 {White mates}");
                    }
                    else
                    {
                        declareResult("1/2-1/2 {Stalemate}");
                    }
                }
                // Exit the method
                return true;
            }
        }
        // If the method did not exit, then the move was not legal
        return false;
	}

	/**
	 * Undoes the previous ply.
	 */
	public void undo()
	{
		if(this.moveStack.size() > 0)
			currentState.undoMove(this.moveStack.pop());
        legalMoves = currentState.generateMoves();
        this.gameOver = false; // Undo means you wanna play again
	}

	/**
	 * Called when the active player resigns and the opponent wins the game.
	 */
	public void resign()
	{
		if(getCurrentState().getActivePlayer() == Player.WHITE)
			declareResult("0-1 {White resigns}");
		else
			declareResult("1-0 {Black resigns}");
	}

	/**
	 * Declares the result of the game to the output.
	 * Note: This does not delete the object and therefore the
	 * interface should make necessary arrangements to end the game
	 * and dissalow further moves.
	 */
	private void declareResult(String r)
	{
		this.gameOver = true;
		Frittle.write(r);
	}

    /**
     * Checks if the game's result has been declared already
     *
     * @return <code>true</code> if the game is over, <code>false</code> if the
     * game is still on
     */
    public boolean isGameOver() {
        return gameOver;
    }
}