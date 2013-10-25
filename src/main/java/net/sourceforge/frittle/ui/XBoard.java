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
import java.util.regex.Matcher;

/**
 * The XBoard protocol implements a format of communication with XBoard/WinBoard
 * interfaces. The protocol follows the Chess Engine Communication Protocol
 * [http://www.tim-mann.org/xboard/engine-intf.html] written by Tim Mann.
 */
public class XBoard extends CommunicationProtocol {
	/**
	 * Process a command from the input stream and do something about it.
	 * 
	 * @param input
	 *            the command string from the input stream
	 */
	public void processCommand(String input) {
		if (input.equals("protover 2")) {
			Frittle.write("feature myname=\"Frittle " + Frittle.version + "\" setboard=1 analyze=0 variants=\"normal\" colors=0 debug=1 done=1");
		} else if (input.equals("resign")) {
			Frittle.getGame().resign();
		} else if (input.equals("go")) {
			// First check if the game is still on
			if (Frittle.getGame().isGameOver())
				this.error("Game Over");
			// If so, ask the AI to make a move
			else {
				Frittle.debug("Frittle is playing " + (Frittle.getGame().getCurrentState().getActivePlayer()));
				Frittle.getAI().go();
			}
		} else if (input.equals("force")) {
			Frittle.debug("Frittle is inactive");
			Frittle.getAI().destroyThreads();
			Frittle.getAI().forceMode = true;
		} else if (input.equals("undo")) {
			Frittle.getGame().undo();
			Frittle.debug("Reversed one move");
		} else if (input.equals("remove")) {
			Frittle.getGame().undo();
			Frittle.getGame().undo();
			Frittle.debug("Reversed two moves");
		} else if (input.startsWith("debug")) {
			Frittle.debugMode = input.substring(6).equals("on");
		} else if (input.equals("post")) {
			Frittle.getAI().showThinking = true;
			Frittle.debug("Thinking on");
		} else if (input.equals("nopost")) {
			Frittle.getAI().showThinking = false;
			Frittle.debug("Thinking off");
		} else if (input.equals("hard")) {
			Frittle.getAI().ponderMode = true;
			Frittle.debug("Pondering on");
		} else if (input.equals("easy")) {
			Frittle.getAI().ponderMode = false;
			Frittle.debug("Pondering off");
		} else if (input.startsWith("setboard")) {
			String FEN = input.substring(9); // After "setboard "
			try {
				Frittle.getAI().destroyThreads();
				GameState state = new GameState(FEN);
				Frittle.getGame().setState(state);
				Frittle.debug("OK");
			} catch (InvalidFENException e) {
				this.error(e.getMessage());
			}
		} else if (input.startsWith("sd")) {
			try {
				int depth = Integer.parseInt(input.substring(3)); // After "sd "
				Frittle.getAI().searchDepth = depth;
				Frittle.debug("OK");
			} catch (NumberFormatException e) {
				this.error("Invalid Search Depth");
			}
		} else if (input.startsWith("level")) {
			Matcher matcher = timeControlsPattern.matcher(input.substring(6));
			if (matcher.matches()) {
				try {
					// Get moves per session (0 if not tournament mode)
					int movesPerSession = Integer.parseInt(matcher.group(1));
					// Get base time in minutes
					long baseTime = Long.parseLong(matcher.group(2)) * 60 * 1000;
					// Add seconds component, if any
					if (matcher.group(3) != null) {
						baseTime += Long.parseLong(matcher.group(3)) * 1000;
					}
					// Get move increment from seconds
					long increment = Long.parseLong(matcher.group(4)) * 1000;
					// Set clock format on game
					if (movesPerSession > 0) // Tournament format
						Frittle.getGame().setClockFormat(new ClockFormat(movesPerSession, baseTime));
					else
						// Fischer format
						Frittle.getGame().setClockFormat(new ClockFormat(baseTime, increment));
					// Reset AI clock
					Frittle.getAI().clock.set(baseTime);
					Frittle.debug("OK");
				} catch (NumberFormatException e) {
				}
			}
		} else if (input.startsWith("time")) {
			try {
				// the time X commands sends X centiseconds
				long time = Long.parseLong(input.substring(5)) * 10;
				Frittle.getAI().clock.set(time);
			} catch (NumberFormatException e) {
			}
		} else if (input.equals("?")) {
			Frittle.getAI().moveNow();
		} else if (input.startsWith("perft")) {
			Perft p = new Perft(Frittle.getGame().getCurrentState());
			p.test(Integer.parseInt(input.substring(6)));
		} else if (coordinateMovePattern.matcher(input).matches())// Try to
																	// parse the
																	// move and
																	// apply it
		{
			if (Frittle.getGame().doMove(input)) {
				// The move was made, success!
				// If not in forceMode mode and the game is still on, let the AI
				// play the next move
				if (Frittle.getAI().forceMode == false && Frittle.getGame().isGameOver() == false)
					Frittle.getAI().go();
			} else {
				// Illegal move
				Frittle.write("Illegal Move");
			}
		} else {
			// Do nothing if errornous command was made in XBoard mode
		}
	}

	/**
	 * Notify the user of the move made by the engine.
	 * 
	 * @param moveStr
	 *            the move made by the AI in co-ordinate notation
	 */
	public void AIMove(String moveStr) {
		Frittle.write("move " + moveStr);
	}

	/**
	 * Show AI thoughts to the user
	 * 
	 * @param depth
	 *            the deeepest ply that the normal search reached
	 * @param sdepth
	 *            the deeepest ply that the selective search reached
	 * @param score
	 *            the score of evaluation of the principal variation
	 * @param nodes
	 *            the number of nodes searched
	 * @param evals
	 *            the number of nodes evaluated
	 * @param msec
	 *            the time required, in milliseconds
	 * @param pv
	 *            the principal variation of the search
	 */
	public void showThinking(int depth, int sdepth, int score, int msec, int nodes, int evals, String pv) {
		Frittle.write(depth + " " + score + " " + (msec / 10) + " " + nodes + " " + pv);
	}

	/**
	 * Notify the user of some message
	 * 
	 * @param msg
	 *            the message to convey to the user
	 */
	public void notify(String msg) {
		Frittle.write("telluser " + msg);
	}

	/**
	 * Notify the user of some error
	 * 
	 * @param err
	 *            the error message to convey to the user
	 */
	public void error(String err) {
		Frittle.write("tellusererror " + err);
	}
}
