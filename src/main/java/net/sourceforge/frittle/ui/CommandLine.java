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

package net.sourceforge.frittle.ui;

import net.sourceforge.frittle.*;
import net.sourceforge.frittle.ai.Eval;
import java.util.regex.Matcher;

/**
 * The console protocol implements a format of communication with command line
 * interfaces like DOS or Shell.
 */
public class CommandLine extends CommunicationProtocol
{

    /**
     * Process a command from the input stream and do something about it.
     *
     * @param   input     the command string from the input stream
     */
    public void processCommand(String input)
    {
        if( input.equals("help") )
        {
            String help = "Frittle " + Frittle.version + " Help\n" +
                    "bd.....................Display current position as a board\n" +
                    "fen....................Display current position in FEN\n" +
                    "eval...................Show AI evaluation of current position\n" +
                    "history................Show move history\n" +
                    "MOVE...................Play a move, where MOVE is in coordinate\n" +
                    "                       notation. (eg. e2e4, e1g1 or d7d8q)\n" +
                    "go.....................Tells the engine to play the next move. This\n" +
                    "                       will switch the AI to the player on move\n" +
                    "force..................AI will not play moves or think at all   \n" +
                    "undo...................Undo previous move\n" +
                    "resign.................Resign as the player on move\n" +
                    "debug..................Toggle DEBUG mode\n" +
                    "post...................Show what AI is thinking when on a move\n" +
                    "nopost.................Don't show what AI is thinking when on a move\n" +
                    "hard...................Switch to hard mode. AI will ponder on opponent's move\n" +
                    "easy...................Switch to easy mode. AI will think only on its own move\n" +
                    "sd DEPTH...............Sets AI search depth to DEPTH\n" +
                    "level X Y[:S] Z........Sets the clock type of this game to X moves per\n" +
                    "                       session, with base time of Y minutes, S seconds which\n" +
                    "                       increments by Z seconds after each move\n" +
                    "?......................Interrupt search and Move Now!\n" +
                    "moves..................Displays a list of legal moves from current position\n" +
                    "new....................Starts a new game with white on move\n" +
                    "setboard FEN...........Sets current position to that represented by FEN,\n" +
                    "                       which is a string in Forsyth-Edward notation\n" +
                    "perft DEPTH............Runs a performance test to depth DEPTH\n" +
                    "quit...................Exits Frittle";

            Frittle.write(help);
        }
        else if( input.equals("bd") )
		{
			printBoard();
		}
        else if( input.equals("eval") )
        {
            Frittle.write("Evaluation: "+(float)Eval.evaluate(Frittle.getGame().getCurrentState())/100);
        }
		else if( input.equals("fen") )
		{
			Frittle.write(Frittle.getGame().getCurrentState().toFEN() + 
                    " " + Frittle.getGame().getReversiblePliesCount() +
                    " " + Frittle.getGame().getMoveNumber());
		}
        else if ( input.equals("history") )
        {
            Frittle.write(Frittle.getGame().getMoveHistoryString());
        }
		else if( input.equals("xboard") )
		{
			Frittle.setProtocol(new XBoard());
		}
		else if( input.equals("resign") )
		{
			Frittle.getGame().resign();
		}
		else if( input.equals("go") )
		{
            // First check if the game is still on
            if(Frittle.getGame().isGameOver())
                this.error("Game Over");
            // If so, ask the AI to make a move
            else
            {
                Frittle.debug("Frittle is playing " + (Frittle.getGame().getCurrentState().getActivePlayer()));
                Frittle.getAI().go();
            }
		}
		else if( input.equals("force") )
		{
            Frittle.debug("Frittle is inactive");
            Frittle.getAI().destroyThreads();
			Frittle.getAI().forceMode = true;
		}
		else if( input.equals("undo") )
		{
			Frittle.getGame().undo();
            Frittle.debug("Reversed one move");
		}
		else if( input.equals("remove") )
		{
			Frittle.getGame().undo();
			Frittle.getGame().undo();
            Frittle.debug("Reversed two moves");
		}
		else if( input.startsWith("debug") )
		{
			Frittle.debugMode = input.substring(6).equals("on");
		}
		else if( input.equals("post") )
		{
			Frittle.getAI().showThinking = true;
            Frittle.debug("Thinking on");
		}
		else if( input.equals("nopost") )
		{
			Frittle.getAI().showThinking = false;
            Frittle.debug("Thinking off");
		}
        else if ( input.equals("hard") )
        {
            Frittle.getAI().ponderMode = true;
            Frittle.debug("Pondering on");
        }
        else if ( input.equals("easy") )
        {
            Frittle.getAI().ponderMode = false;
            Frittle.debug("Pondering off");
        }
        else if( input.equals("moves") )
        {
            StringBuffer movesStr = new StringBuffer("");
            MoveList moveList = Frittle.getGame().getLegalMoves();
            if(moveList.isEmpty())
            {
                movesStr.append("There are no legal moves from current position");
            }
            else
            {
                movesStr.append("Legal moves: ");
                for(Move move : moveList)
                    movesStr.append(move.toString() + ". ");
            }
            Frittle.write(movesStr.toString());
        }
        else if( input.startsWith("setboard") )
        {
            String FEN = input.substring(9); // After "setboard "
            try
            {
                Frittle.getAI().destroyThreads();
                GameState state = new GameState(FEN);
                Frittle.getGame().setState(state);
                Frittle.debug("OK");
            }
            catch(InvalidFENException e)
            {
                this.error(e.getMessage());
            }
        }
        else if( input.startsWith("sd") )
        {
            try
            {
                int depth = Integer.parseInt(input.substring(3)); // After "sd "
                Frittle.getAI().searchDepth = depth;
                Frittle.debug("OK");
            }
            catch(NumberFormatException e)
            {
                this.error("Invalid Search Depth");
            }
        }
        else if( input.startsWith("level") )
        {
            Matcher matcher = timeControlsPattern.matcher(input.substring(6));
            if(matcher.matches())
            {
                try
                {
                    // Get moves per session (0 if not tournament mode)
                    int movesPerSession = Integer.parseInt(matcher.group(1));
                    // Get base time in minutes
                    long baseTime = Long.parseLong(matcher.group(2)) * 60 * 1000;
                    // Add seconds component, if any
                    if(matcher.group(3) != null)
                    {
                        baseTime += Long.parseLong(matcher.group(3)) * 1000;
                    }
                    // Get move increment from seconds
                    long increment = Long.parseLong(matcher.group(4)) * 1000;
                    // Set clock format on game
                    if(movesPerSession > 0) // Tournament format
                        Frittle.getGame().setClockFormat(new ClockFormat(movesPerSession, baseTime));
                    else // Fischer format
                        Frittle.getGame().setClockFormat(new ClockFormat(baseTime, increment));
                    // Reset AI clock
                    Frittle.getAI().clock.set(baseTime);
                    Frittle.debug("OK");
                    return;
                }
                catch(NumberFormatException e)
                {
                }
            }
            else
                error("Invalid format. Type 'help' for assistance on commands.");
        }
        else if ( input.startsWith("time") )
        {
            try
            {
                // the time X commands sends X centiseconds
                long time = Long.parseLong(input.substring(5)) * 10;
                Frittle.getAI().clock.set(time);
            }
            catch(NumberFormatException e)
            {
                error("Invalid format. Type 'help' for assistance on commands.");
            }
        }
        else if ( input.equals("?") )
        {
            Frittle.getAI().moveNow();
        }
        else if( input.startsWith("perft") )
        {
            Perft p = new Perft(Frittle.getGame().getCurrentState());
            p.test(Integer.parseInt(input.substring(6)));
        }
		else if(coordinateMovePattern.matcher(input).matches())// Try to parse the move and apply it
		{
            if(Frittle.getGame().doMove(input))
            {
                // The move was made, success!
                // If not in forceMode mode and the game is still on, let the AI play the next move
                if(Frittle.getAI().forceMode == false && Frittle.getGame().isGameOver() == false)
                    Frittle.getAI().go();
            }
            else
            {
                // Illegal move
                Frittle.write("Illegal Move. Type 'moves' for a list of legal moves.");
            }
		}
        else
        {
            error("Unknown command");
        }
    }
    
    /**
     * Notify the user of the move made by the engine.
     *
     * @param   moveStr        the move made by the AI in co-ordinate notation
     */
    public void AIMove(String moveStr)
    {
        Frittle.write("My move is: " + moveStr);
    }

    /**
     * Show AI thoughts to the user
     *
     * @param   depth       the deeepest ply that the normal search reached
     * @param   sdepth      the deeepest ply that the selective search reached
     * @param   score       the score of evaluation of the principal variation
     * @param   nodes       the number of nodes searched
     * @param   evals       the number of nodes evaluated
     * @param   msec        the time required, in milliseconds
     * @param   pv          the principal variation of the search
     */
    public void showThinking(int depth, int sdepth, int score, int msec, int nodes, int evals, String pv)
    {
        // Get time in seconds
        float seconds = (float)msec/1000;
        // Build string for score. Separate sign and magnitude.
        String scoreStr;
        if(score >= 0)
            scoreStr = "+";
        else // score is negative
        {
            scoreStr = "-";
            score = -score; // We want magnitude
        }
        if(score > Eval.checkmateThreshold)
            scoreStr = scoreStr + "M" + (Eval.INFINITY - score)/100;
        else
            scoreStr = scoreStr + (float)score/100;
        Frittle.write("["+ depth + "/" + sdepth + "]\tscore=" + scoreStr + "\t" + pv);
        Frittle.write(seconds + "s\tnodes=" + nodes + "\tnps=" + nodes/seconds + "\tevals=" +
                 evals + "\n");
    }


    /**
     * Notify the user of some message
     *
     * @param   msg     the message to convey to the user
     */
    public void notify(String msg)
    {
        Frittle.write("Note: " + msg);
    }

    /**
     * Notify the user of some error
     *
     * @param   err     the error message to convey to the user
     */
    public void error(String err)
    {
        Frittle.write("Error: " + err);
    }

    /**
	 * Prints an ASCII equivalent of the current state of the board
	 * to standard output. Generated by the "bd" command.
	 */
	private void printBoard()
	{
        StringBuffer line;
        GameState state = Frittle.getGame().getCurrentState();
		for(byte r=8; r>0; r--)
		{
            line = new StringBuffer();
			line.append(r+"| ");
			for(char f='a'; f != 'i'; f++)
			{
				byte i = Moves.toIndex(f,r);
				if(state.getBoard()[i]==null)
					line.append("- ");
				else
					line.append(state.getBoard()[i].getChar()+" ");
			}
			Frittle.write(line.toString());
		}
		Frittle.write("------------------");
		Frittle.write(" | a b c d e f g h");
        Frittle.write("  " + state.getActivePlayer().toString() + " TO MOVE");
	}
}
