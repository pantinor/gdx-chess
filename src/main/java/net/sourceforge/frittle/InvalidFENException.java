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
 * This exception is raised whenever a FEN string to be parsed does not follow
 * the standard format.
 */
public class InvalidFENException extends Exception
{
    /**
     * Constructs an instance of <code>InvalidFENException</code>
     * with the invalid FEN that generated it.
     *
     * @param FEN     the input FEN string that was incorrect
     */
    public InvalidFENException(String FEN) {
        super("Cannot parse FEN: " + FEN);
    }
}
