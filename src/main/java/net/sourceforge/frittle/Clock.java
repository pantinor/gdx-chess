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
 * An active clock should be continously counting down from a given value.
 * To reflect such actions, the Clock class stores the initial (or forcibly set)
 * value of the clock and the time that the clock was set, so that at any time
 * the time left can be retreived.
 *
 * All time is handled in milliseconds
 */
public class Clock
{
    /** The time the clock was set */
    private long setTime;

    /** The amount of time set on the clock */
    private long amount;

    /**
     * Creates a new clock and sets it to the given amount
     * at the instance of creation
     * @param   amount      the amount of time to set on the clock in milliseconds
     */
    public Clock(long amount)
    {
        this.setTime = System.currentTimeMillis();
        this.amount = amount;
    }

    /**
     * Manually sets the clock to the given amount at the instance of
     * calling the method
     * @param   amount      the amount of time to set on the clock in milliseconds
     */
    public void set(long amount)
    {
        this.setTime = System.currentTimeMillis();
        this.amount = amount;
    }

    /**
     * Increments the amount on the clock by the given value. This does not
     * affect the set time, but merely updates the stored amount
     * @param   amount      the amount of time to increment in milliseconds
     */
    public void increment(long amount)
    {
        this.amount += amount;
    }

    /**
     * The amount of time left on this clock. A negative value indicates that
     * the clock has already expired.
     * @return  the amount of time left in milliseconds
     */
    public long left()
    {
        return this.amount - (System.currentTimeMillis() - this.setTime);
    }
}
