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
 * This class is used to conduct a performance test on the move generation
 * process.
 */
public class Perft {
    private long nodes;
    private long captures;
    private long checks;
    private long enPassant;
    private long castles;
    private long promotions;
    private GameState state;

    public Perft(GameState state)
    {
        this.state = state;
    }

    public void test(int depth)
    {
        nodes = 0;
        checks = 0;
        captures = 0;
        enPassant = 0;
        castles = 0;
        promotions = 0;
        long start = new java.util.Date().getTime();
        crawl(state, depth);
        long end = new java.util.Date().getTime();
        Frittle.write("nodes="+ nodes + ", captures=" + captures + ", ep=" + enPassant 
             + ", castles=" + castles + ", promotions=" + promotions + ", checks=" + checks);
        float time = (float)(end-start)/1000;
        Frittle.write("time=" + time + ", nps=" + (float)nodes/time);
    }

    private void crawl(GameState state, int depth)
    {
        if(depth == 0)
        {
            return;
        }
        else
        {
            MoveList moves = state.generateMoves();
            for(Move move : moves)
            {
                state.doMove(move);
                //Frittle.write(move.toString());
                //Frittle.write(state.toFEN());
                if(depth == 1)
                {
                    nodes++;
                    if(move.capturedPiece != null)
                        captures++;
                    if(move.viaEP)
                        enPassant++;
                    if(move.castle != null)
                        castles++;
                    if(move.promotion != null)
                        promotions++;
                    if(state.isInCheck(state.getActivePlayer()))
                        checks++;
                }
                crawl(state,depth-1);
                state.undoMove(move);
            }
        }
    }
}
