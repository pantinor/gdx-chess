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

import net.sourceforge.frittle.*;

/**
 * A static evaluation is an analysis of the game at any state (position) and
 * the result of this evaluation is an integer value in the unit 'centipawns',
 * where 100 centipawns = value of a pawn on the board. The value is always
 * relative to the side to move. For example, at the beginning of the game, the
 * score is always 0. If it is white to move and mate in 3, then score is around
 * INFINITY. If it is black to move with forced mate then it is -INFINITY.
 *
 * This class provides static methods for evaluation of a position.
 */
public class Eval
{
    /**
     * Privatize constructor to prevent instantiation.
     */
    private Eval(){}
    
    /**
     * Calculates the static evaluation for a given position relative to the
     * side to move.
     *
     * @param   state   the state to evaluate
     * @return  the score of the position in centipawns
     */
    public static int evaluate(GameState state)
    {
        int[] score = new int[2]; // [W|B]
		int[] material = new int[2]; // [W|B]
        int[] positional = new int[2]; // [W|B]
        int[] endgamePos = new int[2]; // [W|B]
        int[][] pieceCount = new int[2][6]; // [W|B] and [P|N|B|R|Q|K]
        int[][][] pawns = new int[2][16][3]; // [W|B][n][file|relative_rank]
        // Relative rank means for black the ranks are reversed, so initial rank for pawn is always 1
        // Iterate through the board and get material values,
        // positional scores and piece counts
        Piece p = null;
        int player, opponent, type, n;
        final int FILE = 0, RANK = 1;
		for(int x=0; x<64; x++)
		{
            p = state.getBoard()[x];
            if(p == null)
                continue;
            player = p.getPlayer().ordinal();
            type = p.getType().ordinal();
            // Now get the material value
            material[player] += pieceValue[type];
            positional[player] += piecePositionalScore[player][type][x];
            endgamePos[player] += endgamePositionalScore[player][type][x];
            pieceCount[player][type]++;
            if(type==0) // Pawn
            {
                n = pieceCount[player][0]-1; // Index for storing pawn data
                pawns[player][n][FILE] = Moves.toFileIndex(x);
                pawns[player][n][RANK] = Moves.toRankIndex(x);
                if(player == 1) // reverse ranks for black pawns
                    pawns[player][n][RANK] = 7-pawns[player][n][RANK];
            }
        }
        // See if either side should bring their kings and pawns forward
        for(player=0, opponent=1; player<2; player++,opponent--)
        {
            // Add material value
            score[player] += material[player];

            // Add material ratio so that exchanges are favoured when at an advantage
            //score[player] += (100*score[player])/score[opponent];

            // Now give positional score based on whether it is middle game or endgame

            // Endgame is when opponent has no queen and either at most one rook
            // or at most two of the bishop/knight pieces.
            if( pieceCount[opponent][4] == 0 &&
                    ( (pieceCount[opponent][3] <= 1) ||
                      (pieceCount[opponent][1]+pieceCount[opponent][2] <= 2) ) )
            {
                score[player] += endgamePos[player];
                // @todo improve endgame analysis other than just positional scoring
            }
            else
            {
                score[player] += positional[player];
            }

            // Award bishop pairs
            if(pieceCount[player][2] >= 2)
                score[player] += 50;

            // Analyze pawn structure
            boolean doubled, isolated, passed, chained;
            int[] p1, p2;
            for(int i=0; i<pieceCount[player][0]; i++)
            {
                p1 = pawns[player][i]; // p1 is the ith pawn of player
                doubled = false;
                isolated = true;
                passed = true;
                chained = false;
                for(int j=0; j<pieceCount[player][0]; j++) // Friendly pawns
                {
                    p2 = pawns[player][j]; // p2 is the jth pawn of player
                    if(i==j)
                        continue; // don't refer to the same pawn

                    if(p1[FILE]==p2[FILE] && p1[RANK]<p2[RANK]) // p1 is doubled behind p2
                        doubled = true;
                    else if( Math.abs(p1[FILE]-p2[FILE])==1 ) // p2 is on adjacent file
                    {
                        if(p1[RANK]-p2[RANK] >= 0) // p2 is behind p1
                            isolated = false;
                        if(p1[RANK]-p2[RANK]==1) // p1 is immediately defended by p2
                            chained = true;
                    }
                } // end foreach pawn j
                for(int j=0; j<pieceCount[opponent][0]; j++)
                {
                    p2 = pawns[opponent][j]; // p2 is the jth pawn of the opponent
                    if(  Math.abs(p1[FILE]-p2[FILE])==1 ) // is on adjacent file
                    {
                        if((7-p2[RANK]) > p1[RANK]) // is ahead
                            passed = false;
                    }
                } // end foreach pawn j
                // A doubled pawn cannot be counted passed even if no enemies on adjacent files
                if(doubled == true)
                    passed = false;
                // Now give bonuses or penalties
                if(doubled)
                    score[player] -= 20;
                if(isolated)
                    score[player] -= 20;
                if(passed)
                    score[player] += 60;
                if(chained)
                    score[player] += 30;
            } // end foreach pawn i

        } // end foreach player (playerwise evaluation)

        // Return as per the point of view of the player to move
        if(state.getActivePlayer()==Player.WHITE)
            return score[0]-score[1];
        else
            return score[1]-score[0];
    }



    // Infinity constant
    public static final short INFINITY = 30000; //Short.MAX_VALUE;
    // Constants used in evaluation.
    public static final int pawnValue    = 100;
    public static final int knightValue  = 300;
    public static final int bishopValue  = 300;
    public static final int rookValue    = 500;
    public static final int queenValue   = 900;
    // kingValue should be just high enough to be most valuable in MVV/LVA
    // and low enough to not distort the material ratio
    public static final int kingValue    = 1000; 
    // Threshold gives a lower bound above which values indicate checkmate
    public static final int checkmateThreshold = kingValue+9*queenValue+2*rookValue+2*bishopValue+2*knightValue;
    // An alias to the above arrays but ordinal indexed
    public static final int[] pawnWPositionalScore = {
         0,  0,  0,  0,  0,  0,  0,  0,
        80, 80, 80, 80, 80, 80, 80, 80,
        40, 40, 40, 50, 50, 40, 40, 40,
        10, 15, 20, 25, 25, 10, 15, 10,
         5,  0, 10, 20, 20, 10,  0,  5,
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };
    public static final int[] pawnBPositionalScore = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0,
         5,  0, 10, 20, 20, 10,  0,  5,
        10, 15, 20, 25, 25, 10, 15, 10,
        40, 40, 40, 50, 50, 40, 40, 40,
        80, 80, 80, 80, 80, 80, 80, 80,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    public static final int[] pawnWEndgameScore = {
         0,  0,  0,  0,  0,  0,  0,  0,
        95, 95, 95, 95, 95, 95, 95, 95,
        65, 65, 65, 65, 65, 65, 65, 65,
        40, 40, 40, 40, 40, 40, 40, 40,
        10, 10, 20, 25, 25, 20, 10, 10,
        -5, -5, -5, -5, -5, -5, -5, -5,
       -10,-10,-10,-10,-10,-10,-10,-10,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    public static final int[] pawnBEndgameScore = {
         0,  0,  0,  0,  0,  0,  0,  0,
       -10,-10,-10,-10,-10,-10,-10,-10,
        -5, -5, -5, -5, -5, -5, -5, -5,
        10, 10, 20, 25, 25, 20, 10, 10,
        40, 40, 40, 40, 40, 40, 40, 40,
        65, 65, 65, 65, 65, 65, 65, 65,
        95, 95, 95, 95, 95, 95, 95, 95,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    public static final int[] knightPositionalScore = {
        -50,-30,-30,-30,-30,-30,-30,-50,
        -20,-20,  0,  5,  5,  0,-20,-20,
        -30,  5, 25, 20, 20, 25,  5,-30,
        -30,  5, 20, 35, 35, 15,  5,-30,
        -30,  5, 20, 35, 35, 20,  5,-30,
        -30,  5, 25, 20, 20, 25,  5,-30,
        -20,-20,  0,  5,  5,  0,-20,-20,
        -50,-30,-30,-30,-30,-30,-30,-50
    };
    public static final int[] bishopPositionalScore = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  0, 10, 15, 15, 10,  0,-10,
        -10,  0, 10, 15, 15, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };
    public static final int[] rookWPositionalScore = {
        10, 10, 10, 10, 10, 10, 10, 10,
        10, 20, 20, 20, 20, 20, 20, 10,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  3,  4,  5,  5,  4,  3,  0
    };
    public static final int[] rookBPositionalScore = {
         0,  3,  4,  5,  5,  4,  3,  0,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        10, 20, 20, 20, 20, 20, 20, 10,
        10, 10, 10, 10, 10, 10, 10, 10
    };
    public static final int[] queenPositionalScore = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
         -5,  0,  5,  5,  5,  5,  0, -5,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };
   public static final int[] kingWPositionalScore = { // Middlegame only
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };
    public static final int[] kingBPositionalScore = { // Middlegame only
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30
    };
    public static final int[] kingWEndgameScore = { // Endgame only
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,-30,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };
    public static final int[] kingBEndgameScore = { // Endgame only
        -50,-30,-30,-30,-30,-30,-30,-50,
        -30,-30,  0,  0,  0,-30,-30,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -50,-40,-30,-20,-20,-30,-40,-50
    };
    // Convinience aliases for arrays (so as to index them with enum ordinals without
    // resorting to switch-case)
    public static final int[] pieceValue = {
        pawnValue, knightValue, bishopValue, rookValue, queenValue, kingValue
    };
    // Following array should be accessed like [Color][Piece][Position]
    public static final int[][][] piecePositionalScore = {
        {
            pawnWPositionalScore, knightPositionalScore, bishopPositionalScore,
            rookWPositionalScore, queenPositionalScore, kingWPositionalScore
        },
        {
            pawnBPositionalScore, knightPositionalScore, bishopPositionalScore,
            rookBPositionalScore, queenPositionalScore, kingBPositionalScore
        }
    };
    // In the endgames rooks and kings get to the attack and pawns push forward
    public static final int[][][] endgamePositionalScore = {
        {
            pawnWEndgameScore, knightPositionalScore, bishopPositionalScore,
            queenPositionalScore, queenPositionalScore, kingWEndgameScore
        },
        {
            pawnBEndgameScore, knightPositionalScore, bishopPositionalScore,
            queenPositionalScore, queenPositionalScore, kingBEndgameScore
        }
    };

}
