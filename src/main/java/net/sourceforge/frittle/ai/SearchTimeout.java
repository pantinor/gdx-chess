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

/**
 * This task can be scheduled by a Timer so that the AI's search thread can
 * be interrupted and a move will be performed
 */
public class SearchTimeout extends java.util.TimerTask
{
    /** Reference to the AI object that is searching */
    private AI ai;
    /** Reference to the the Search object that needs to be stopped */
    private Search search;

    /**
     * Creates a new SearchTimeout task for the given search by the given AI
     *
     * @param   ai      the AI that is searching
     * @param   search  the Search that needs to be stopped
     */
    public SearchTimeout(AI ai, Search search)
    {
        this.ai = ai;
        this.search = search;
    }

    /**
     * Executes the task
     */
    public void run()
    {
        // Tell the AI to move now ONLY if it's current search thread
        // is the same that this task is meant to stop
        if(ai.search == this.search)
        {
            ai.moveNow();
        }
    }
}
