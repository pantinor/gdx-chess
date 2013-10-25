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

/**
 * A <code>BookFormatException</code> is thrown when the entry in the opening
 * book cannot be parsed.
 */
public class BookFormatException extends Exception
{
    /**
     * Creates a new instance of <code>BookFormatException</code> which occured
     * at a given line.
     *
     * @param err   the error message
     * @param line  the line number where the error occured
     */
    public BookFormatException(String err, int line)
    {
        super(err + " on line " + line);
    }
}
