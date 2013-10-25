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

import net.sourceforge.frittle.ai.AI;
import net.sourceforge.frittle.ui.CommandLine;
import net.sourceforge.frittle.ui.CommunicationProtocol;

import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Frittle is the static Main class that contains the
 * components of the chess engine.
 *
 * @author	Rohan Padhye
 * @version 0.5
 */
public class Frittle
{
    /** The current version **/
    public static final String version = "1.0";

	/** The Game object that contains GameStates and Game Logic. */
	private static Game game;

	/** The brain of the engine. Artificial Intelligence. */
	private static AI ai;

    /** The communication protocol that is being used to talk to the user. */
    private static CommunicationProtocol protocol;

    /** Debug mode flag. True if debugMode mode is on */
    public static boolean debugMode;

	/**
	 * This is the method that is invoked when the program is
	 * run from the commandline via the JVM.
	 *
	 * @param args	the command line arguments, if any
	 */
	public static void main(String args[]) throws java.io.IOException
	{
        // Parse arguments
        for(int i=0; i < args.length; i++)
        {
            if(args[i].equals("-debug"))
            {
                debugMode = true;
            }
        }

        // Initialize IOStreams
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
        // Welcome message
        Frittle.write("Welcome to Frittle " + version);
		// Create new game and restart AI engine
		game = new Game();
		ai = new AI();
        Frittle.debug("Frittle is "    + (ai.forceMode?"inactive":"playing BLACK"));
        Frittle.debug("Pondering " + (ai.ponderMode?"on":"off"));
        Frittle.debug("Thinking "  + (ai.showThinking?"on":"off"));
        Frittle.write("Type 'help' for a list of commands.");
        // By default, use the console protocol
        protocol = new CommandLine();
		String input;
		do
		{
			try
			{
				input = br.readLine();
				//log.println(input); // Debug

                // First check for standard input that the protocol will not handle
                if(input.equals("new"))
                {
                    // Start a new game
                    ai.destroyThreads();
                    ai.resetModes();
                    game = new Game();
                    Frittle.debug("New game started");
                }
                else if(input.equals("quit"))
                {
                    break;
                }
                else
                {
                    protocol.processCommand(input);
                }
			}
            catch(Exception e)
            {
                e.printStackTrace();
            }
		} while (true);
        ai.destroyThreads();
	}

	/**
	 * Writes a line to standard output
	 *
	 * @param message		the string to write
	 */
	public static void write(String message)
	{
		System.out.println(message);
		System.out.flush();
	}

    /**
     * Write a line to standard debugMode output if in debugMode mode
     *
     * @param   msg     the message to write as debugMode
     */
    public static void debug(String msg)
    {
        if(debugMode)
            write("# " + msg);
    }

    /**
     * Get a reference to the current Game.
     *
     * @return the game
     */
    public static Game getGame() {
        return game;
    }

	/**
	 * Get a reference to the current AI.
	 *
	 * @return		the AI object
	 */
	public static AI getAI()
	{
		return ai;
	}

    /**
     * Get a reference to the currently active communication protocol.
     *
     * @return the communication protocol
     */
    public static CommunicationProtocol getProtocol() {
        return protocol;
    }

    /**
     * Change the current communication protocol to something else
     *
     * @param newProtocol the communication protocol to set
     */
    public static void setProtocol(CommunicationProtocol newProtocol) {
        protocol = newProtocol;
    }
    

	public static void setGame(Game game) {
		Frittle.game = game;
	}

	public static void setAi(AI ai) {
		Frittle.ai = ai;
	}

	public static void setDebugMode(boolean debugMode) {
		Frittle.debugMode = debugMode;
	}

	

}

