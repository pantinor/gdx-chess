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

import net.sourceforge.frittle.Frittle;
import java.util.regex.Pattern;

/**
 * A protocol specifies certain read/write actions for communicating
 * messages to and from the user. Different protocls can implement these
 * actions in their formats.
 */
public abstract class CommunicationProtocol {

    /**
     * Process a command from the input stream and do something about it.
     *
     * @param   command     the command string from the input stream
     */
    abstract public void processCommand(String command);

    /**
     * Notify the user of the move made by the engine.
     *
     * @param   moveStr        the move made by the AI in co-ordinate notation
     */
    abstract public void AIMove(String moveStr);

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
    abstract public void showThinking(int depth, int sdepth, int score, int msec, int nodes, int evals, String pv);

    /**
     * Notify the user of some message
     *
     * @param   msg     the message to convey to the user
     */
    abstract public void notify(String msg);

    /**
     * Notify the user of some error
     *
     * @param   err     the error message to convey to the user
     */
    abstract public void error(String err);


    /**
     * A regular expression for a move in coordinate notation (eg. e2e4 or g7g8q)
     * Groups:
     * 1 => file of source square
     * 2 => rank of source square
     * 3 => file of target square
     * 4 => rank of target square
     * 5 => character code of promotional piece, if any
     */
    protected static Pattern coordinateMovePattern =
            Pattern.compile("([a-h][1-8])([a-h][1-8])([qrnbQRNB])?");

    /**
     * A regular expression for specifying time formats
     * Groups:
     * 1 => Moves per session
     * 2 => Base time minutes
     * 3 => Base time seconds, if any
     * 4 => Increment time in seconds
     */
    protected static Pattern timeControlsPattern =
            Pattern.compile("(\\d+) (\\d+)(?:\\:(\\d{2}))? (\\d+)");
}
