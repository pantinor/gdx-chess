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
import java.util.Stack;


/**
 * A search from a given position includes examining the entire game tree
 * using a negamax algorithm with alpha-beta pruning and other techniques such
 * as quiescence search, transposition tables, etc.
 *
 * A search is performed in its own thread so that it does not block user input
 * and can be interrupted anytime. This also allows the AI to ponder on the
 * opponent's time while being ever-ready to accept the opponent's move from
 * the input stream and interrupt the search.
 */
public class Search extends Thread
{
    /** The AI object that initiated this search. */
    private AI ai;
    /** A local copy of the game state to search (might be modified during search) */
    private GameState state;
    /** A hash of the stte to search */
    private long hash;
    /** Whether the search is a ponder */
    private boolean ponder;
    /** The current depth being searched */
    private int depth;
    /** The maximum depth to search */
    private int maxDepth;
    /** The approx time to use for this move */
    private long searchTime;
    /** The time the search started */
    private long startTime;
    /** The time taken to search the last depth */
    private long lastIterationTime;
    /** The best move searched so far */
    private String bestMoveSoFar;
    /** The score of the best move searched so far */
    private int bestScoreSoFar;
    /** Counts the number of nodes in the search */
	private int nodeCount;
    /** Counts the number of evaluations performed in the search */
	private int evalCount;
    /** Keeps track of the deepest ply reached in a normal search */
	private int deepestPly;
    /** The current variation being considered */
    private Stack<Move> currentVariation;
    /** The killer moves (used for move ordering) */
    private short[][] killer;

    /** A flag signaling that the hashtable entry is invalid */
    private static final byte INVALID = 0;
    /** A flag signalling that the hashtable entry is an ALL NODE */
    private static final byte ALL_NODE = 1;
    /** A flag signaling that the hashtable entry has an exact score */
    private static final byte PV_NODE = 2;
    /** A flag signalling that the hashtable entry is a CUT NODE */
    private static final byte CUT_NODE = 3;

    /**
     * Initializes a new search. Creates a local copy of the given state
     *
     * @param   ai          the AI object that initialized the search
     * @param   state       the state to search (it will be copied)
     * @param   maxDepth    the maximum depth to which to search if not interrupted
     * @param   searchTime  the approximate amount of time to use for the search (in milliseconds)
     */
    public Search(AI ai, GameState state, int maxDepth, long searchTime)
    {
        super("Search");
        // Create a copy of the game state so that in case of interruption the
        // original state (of Frittle.getGame()) is not left mangled
        this.state = new GameState(state);
        this.hash = state.hash();
        this.bestScoreSoFar = Eval.evaluate(state);
        if(ai.transpositionTable.exists(hash))
            this.bestMoveSoFar = Move.hashToString(ai.transpositionTable.getBestMoveHash(hash));
        // Initialize fields
        this.ai = ai;
        this.maxDepth = maxDepth;
        this.searchTime = searchTime;
		this.nodeCount = 0;
		this.deepestPly = 0;
		this.evalCount = 0;
        this.currentVariation = new Stack<Move>();
        currentVariation.ensureCapacity(maxDepth);
    }

    // @todo separate search of root node
    /**
     * Begin the search. This method is implcitly called by the JVM after
     * the AI calls Search.start()     *
     */
    @Override public void run()
    {
		// Find the best move by iterative deepening
		this.startTime = System.currentTimeMillis();
        this.lastIterationTime = 0;
		depth = 1;
        try
        {
            // Initialize killer moves register
            killer = new short[maxDepth+4][2]; // Keep some extra space in case of extensions
            do
            {
                PrincipalVariation pv = new PrincipalVariation();
                // Search the node
                this.bestScoreSoFar = search(-Eval.INFINITY, Eval.INFINITY, depth, pv);
                    //MTD(f)://this.bestScoreSoFar = MTD(this.bestScoreSoFar, depth, pv);
                // Get the best move from the hashtable
                // This HAS to exist because if search() completed then the last
                // entry made in the hashtable has to be of the root node which WILL
                // have a best move (if not we are screwed)
                short bestMoveHash = pv.move;
                this.bestMoveSoFar = Move.hashToString(bestMoveHash);
                // Calcualte time in milliseconds
                this.lastIterationTime = System.currentTimeMillis() - this.startTime;
                // Show thinking
                if(ai.showThinking && this.deepestPly > 0)
                {
                    // Get Principal Variation as a string
                    String movStr, pvStr = new String();
                    while(pv != null && pv.move != 0)
                    {
                        movStr = Move.hashToString(pv.move);
                        pvStr = pvStr + movStr + " ";
                        pv = pv.subPV;
                    }
                    // Give the protocol information about this particular search
                    Frittle.getProtocol().showThinking(depth, deepestPly,
                            bestScoreSoFar, (int)this.lastIterationTime, nodeCount, evalCount, pvStr);
                }
                // In case we found a move that is mate in N, then no need to
                // search deeper > N
                if(Math.abs(bestScoreSoFar) > Eval.checkmateThreshold)
                {
                    break;
                }
                // Smart time control
                if(canSearchDeeper() == false)
                    break;
                // Increment depth for next iteration
                depth++;
            } while (depth <= this.maxDepth);
            // If a real search completes normally, then we move now
            this.ai.moveNow();
        }
        catch(InterruptedException e)
        {
            // The search might be interrupted because a hard timeout occurred
            // or because the user chose to MOVE NOW with the '?' command
            // In either case, the moveNow() method will already have been called
            // so we just finish this thread normally
        }
    }

    /**
     * Performs a negamax search on the given state to find the best score.
     * The search is actually a principal-variation alpha-beta search.
     * For implementation details of PVS see:
     * http://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=255640&t=26974
     *
     * @param alpha     the lower bound in alpha-beta search
     * @param beta      the upper bound in alpha-beta search
     * @param depth     the number of plies in the game tree below this node
     * @param pv        the principal variation of the node
     * @return          the score of the search of the state in centipawns
     * @exception InterruptedException  if the user or timer interrupts the search
     */
    private int search(int alpha, int beta, int depth, PrincipalVariation pv) throws InterruptedException
    {
        // Check for interruption
        if(isInterrupted())
            throw new InterruptedException();
        // Increment counters each time we crawl through a node
        nodeCount++;
        int ply = currentVariation.size();
        deepestPly = Math.max(deepestPly, ply);
        // Check transposition table if this has already been searched enough
        // And even if it has not, at least try to get a best move
        long stateHash = state.hash();
        short bestMoveHash = 0; // By default assume no hash move found
        if(ai.transpositionTable.exists(stateHash)) // Found a corresponding entry
        {
            // At least store the best move
            bestMoveHash = ai.transpositionTable.getBestMoveHash(stateHash);
            // Now check if the score is enough to return directly
            if(ai.transpositionTable.getDepth(stateHash) >= depth)
            {
                int hashScore = ai.transpositionTable.getScore(stateHash);
                int flag = ai.transpositionTable.getFlag(stateHash);
                if((flag==PV_NODE) || (flag==ALL_NODE && hashScore <= alpha) || (flag==CUT_NODE && hashScore >= beta))
                {
                    pv.move = bestMoveHash;
                    return hashScore;
                }
            }
        }
        // At this point, the TT entry was not good enough to return
        // but we might have got a hashMove if there was a matching entry

        // Initialize some variables
        int score, bestScore;
        PrincipalVariation subPV = new PrincipalVariation();

        // If depth is 0, perform static eval (or quiescence search)
        if(depth <= 0)
        {
            score = quiescenceSearch(alpha, beta);
            ai.transpositionTable.store(stateHash, depth, score, PV_NODE, bestMoveHash);
            return score;
        }

        // Now, generate all legal moves from this point
        MoveList moveList = state.generateMoves();

        // Make sure there is at least one legal move
        if(moveList.isEmpty())
        {
            // This is mate state.
            // However, stalemate is also possible, in which case score should
            // not be INIFINITY but rather 0 for draw.
            if(state.isInCheck(state.getActivePlayer()))
                bestScore = -Eval.INFINITY; // Checkmate
            else
                bestScore = 0; // Stalemate score
            // Store exact score with large depth so that on every other request
            // to this item from the hashtable, we return early
            ai.transpositionTable.store(stateHash, 100, bestScore, PV_NODE, bestMoveHash);
            return bestScore;
        }

        // Order moves with best at the top
        orderMoves(moveList, ply, bestMoveHash);

        /* Now time to search child nodes of the search tree */

        // By default the flag for this state is that it will be an alpha cut-off
        // If an exact score is found or beta cut-off is made, flag is changed
        int flag = ALL_NODE;
        short moveHash;
        // Initialize variables used in the search
        bestScore = -Eval.INFINITY;
        // Control variable for whether or not to perform a zero-window PVS
        boolean zwSearch = false; // This should be false only for the first move
        // Now search each move recursively
        for(Move move : moveList)
        {
            moveHash = move.hash();
            state.doMove(move);
            currentVariation.push(move);
            if(zwSearch)
            {
                score = -search(-alpha-1, -alpha, depth-1, subPV);  // Zero-window search
                if(score > alpha && score < beta)
                    score = -search(-beta, -alpha, depth-1, subPV); // Re-search with full window
            }
            else
            {
                score = -search(-beta, -alpha, depth-1, subPV); // Full window search on first move
            }
            currentVariation.pop();
            state.undoMove(move);
            if(score > Eval.checkmateThreshold)
            {
                score-=100; // The difference from INFINITY can give mate in N value
            }
            if(score >= beta) // Beta cut-off
            {
                // Save killer move if possible
                if(move.isCapture() == false && move.promotion == null &&
                        killer[ply][0] != moveHash && killer[ply][1] != moveHash)
                {
                    killer[ply][0] = killer[ply][1];
                    killer[ply][1] = moveHash;
                }
                // Store in hashtable
                ai.transpositionTable.store(stateHash, depth, score, CUT_NODE, bestMoveHash);
                return score; // Fail-soft beta cut-off
            }
            if(score > bestScore) // Better than before
            {
                bestScore = score;
                bestMoveHash = moveHash;
                if(score > alpha) // Better than alpha even
                {
                    alpha = score; // Raise alpha
                    flag = PV_NODE; // since alpha < score < beta
                    pv.move  = bestMoveHash; // Set best move for this variation
                    pv.subPV = new PrincipalVariation(subPV); // Copy sub-pv of best move
                }
            }
            zwSearch = true; // So that all moves after the first will be tried with zero window
        }
        // At this point there was no beta cut-off, so the node is either
        // a PV-node or an All-node
        // The 'flag' has the correct value
        ai.transpositionTable.store(stateHash, depth, bestScore, flag, bestMoveHash);
        return bestScore;
    }
    
    /**
     * Performs a Quiescence search on the given node.
     *
     * @param state     the game state to search
     * @param alpha     the lower bound in alpha-beta search
     * @param beta      the upper bound in alpha-beta search
     * @return  the score of the quiescence search in centipawns
     */
    private int quiescenceSearch(int alpha, int beta)
    {
        // Increment counters
        this.nodeCount++;
        this.evalCount++;
        deepestPly = Math.max(deepestPly, currentVariation.size());

        // Perform static evaluation of this state
        int staticEval = Eval.evaluate(state);

        // If the static evaluation itself exceeded beta, return
        if(staticEval > beta)
            return staticEval;

        // Is this score improving alpha?
        if(staticEval > alpha)
            alpha = staticEval;

        // Now look through non-quiet moves
        int score, bestScore = staticEval;
        MoveList captures = state.generateCaptures();
        orderCaptures(captures);
        for(Move move : captures)
        {
            // Make the capture and recursively perform a quiescence search
            state.doMove(move);
            currentVariation.push(move);
            score = -quiescenceSearch(-beta, -alpha);
            currentVariation.pop();
            state.undoMove(move);
            // Look for beta cut-off
            if(score > beta)
            {
                return score;
            }
            // Improve best score or alpha if possible
            if(score > bestScore)
            {
                bestScore = score;
                if(score > alpha)
                {
                    alpha = score;
                }
            }
        }
        return bestScore;
    }

    /**
     * Sorts moves in the following order:
     * <ol>
     * <li>PV-move (from transposition table)</li>
     * <li>Winning capture</li>
     * <li>Queen promotion</li>
     * <li>Good capture</li>
     * <li>Killer move</li>
     * <li>Castle</li>
     * <li>Losing capture</li>
     * <li>Minor promotion</li>
     * <li>Other</li>
     * </ol>
     * @param moves         list of moves
     * @param ply           the ply at which the move list was generated
     * @param bestMoveHash  the hash of the move from the transposition table, if any
     */
    private void orderMoves(MoveList moves, int ply, short bestMoveHash)
    {
        // Initialize buckets
        MoveList pvMove =           new MoveList();
        MoveList winningCaptures =  new MoveList();
        MoveList queenPromotions =  new MoveList();
        MoveList goodCaptures =     new MoveList();
        MoveList killers =          new MoveList();
        MoveList castles =          new MoveList();
        MoveList losingCaptures =   new MoveList();
        MoveList minorPromotions =  new MoveList();
        MoveList otherMoves =       new MoveList();

        // Go through each move, and put it in the correct bucket
        for(Move move : moves)
        {
            short moveHash = move.hash();
            if(moveHash==bestMoveHash)
            {
                pvMove.add(move);
            }
            else if(move.isCapture() &&
               Eval.pieceValue[move.movedPiece.getType().ordinal()] <
               Eval.pieceValue[move.capturedPiece.getType().ordinal()])
            {
                winningCaptures.add(move);
            }
            else if(move.promotion == PieceType.QUEEN) 
            {
                queenPromotions.add(move);
            }
            else if(move.isCapture() &&
               Eval.pieceValue[move.movedPiece.getType().ordinal()] ==
               Eval.pieceValue[move.capturedPiece.getType().ordinal()])
            {
                goodCaptures.add(move);
            }
            else if(killer[ply][0] == moveHash || killer[ply][1] == moveHash)
            {
                killers.add(move);
            }
            else if(move.isCastle())
            {
                castles.add(move);
            }
            else if(move.isCapture())
            {
                losingCaptures.add(move);
            }
            else if(move.isPromotion())
            {
                minorPromotions.add(move);
            }
            else
            {
                otherMoves.add(move);
            }
        }
                orderCaptures(winningCaptures);

        // Clear the move list and concatenate buckets to form the ordered list
        moves.clear();
        moves.addAll(pvMove);
        moves.addAll(winningCaptures);
        moves.addAll(queenPromotions);
        moves.addAll(goodCaptures);
        moves.addAll(killers);
        moves.addAll(castles);
        moves.addAll(losingCaptures);
        moves.addAll(minorPromotions);
        moves.addAll(otherMoves);

    }

    /**
     * Sorts captures using the MVV/LVA technique
     * @param captures      the list of capture moves
     */
    private void orderCaptures(MoveList captures)
    {
        // Initialie buckets
        MoveList buckets[] = new MoveList[6];

        // Bucketize and sort according to least valuable attacker
        for(int i=0; i<6; i++)
            buckets[i] = new MoveList();
        for(Move capture : captures)
            buckets[capture.movedPiece.getType().ordinal()].add(capture);
        captures.clear();
        for(int i=0; i<6; i++)
            captures.addAll(buckets[i]);
        // Bucketize and sort according to most valuable victim
        for(int i=0; i<6; i++)
            buckets[i] = new MoveList();
        for(Move capture : captures)
            buckets[capture.capturedPiece.getType().ordinal()].add(capture);
        captures.clear();
        for(int i=5; i>=0; i--)
            captures.addAll(buckets[i]);
    }

    /// The gain from recapturing at given square (not used currently)
    private int recaptureScore(int square)
    {
        if(currentVariation.size() > deepestPly)
            deepestPly = currentVariation.size();
        // If we can't recapture, then we return 0. Otherwise we return
        // a positive number that denotes how much we gain by recapturing
        int score = 0;
        // Find the move that can recapture with the least valuable attacker
        Move recaptureMove = Move.getBestCapture(state, (byte)square);
        if(recaptureMove == null)
        {
            // There is no legal recapture, so return 0
            return score;
        }
        // Now try the recapture
        state.doMove(recaptureMove);
        currentVariation.push(recaptureMove);
        int scoreAfterOpponentRecaptures = -recaptureScore(square);
        currentVariation.pop();
        state.undoMove(recaptureMove);
        if(scoreAfterOpponentRecaptures >= 0)
        {
            // We only consider the recapture if the opponent's recapture doesn't
            // screw us over
            int gainedMaterialValue = Eval.pieceValue[recaptureMove.capturedPiece.getType().ordinal()];
            score = gainedMaterialValue + scoreAfterOpponentRecaptures;
        }
        return score;
    }

    

    /**
     * Performs a MTD(f) search on the root node and returns the score
     *
     * @param f         the 'first guess' as to what the score might be
     * @param depth     the depth to which to search
     * @param pv        the principal variation of the node
     * @return          the result of this search in centipawns
     * @throws InterruptedException if search is interrupted by the user or timeout occurs
     */
    private int MTD(int f, int depth, PrincipalVariation pv) throws InterruptedException
    {
        int g = f;
        int uppperBound =  Eval.checkmateThreshold;
        int lowerBound = -Eval.checkmateThreshold;
        int beta = g;
        while(lowerBound < uppperBound)
        {
            if(g == lowerBound)
                beta = g+1;
            else
                beta = g;
            g = search(beta-1,beta,depth, pv);
            if(g < beta)
                uppperBound = g;
            else
                lowerBound = g;
        }
        return g;
    }

    /**
     * The best move searched so far
     * @return      the co-ordinate notation for the best move found yet
     */
    public String getBestMoveSoFar()
    {
        return this.bestMoveSoFar;
    }

    /**
     * The score of the best move searched so far
     * @return      the score for the best move found yet
     */
    public int getBestScoreSoFar()
    {
        return this.bestScoreSoFar;
    }

    /**
     * @return Whether the search is a ponde
     */
    public boolean isPonder() {
        return ponder;
    }

    /**
     * Change the nature of the search (ponder or real)
     * @param ponder whether the search is a ponder
     */
    public void setPonder(boolean ponder) {
        this.ponder = ponder;
    }

    /**
     * The hash code of the search is same as the hash code for the game state,
     * because a search on the same state will yield the same results.
     * @return  the 64-bit zobrist hash key
     */
    public long hash()
    {
        return this.hash;
    }

    /**
     * Set the search time for this move
     * @param   searchTime  the approximate amount of time to search (in milliseconds)
     */
    public void setSearchTime(long searchTime)
    {
        // Set approx search time
        this.searchTime = searchTime;
        // Find out how much time has been used already
        if(canSearchDeeper() == false)
            ai.moveNow();
    }

    /**
     * Calculates if there is enough time left to continue the search
     * @return      true if there is enough time left to go on deeper, false if not
     */
    public boolean canSearchDeeper()
    {
        // Estimated Branch Factor (NEEDS TUNING)
        final int branchFactor = 6;
        if(ponder == false) // Not a ponder
        {
            // If it is lesser than a constant branch factor (UPDATE REGULARLY)
            // then we won't be able to perform a deeper search, so exit
            if(this.lastIterationTime * branchFactor > this.searchTime)
                return false;
        }
        return true;
    }
}

/**
 * Collecting the Principal Variation during the search allows us to return
 * the entire PV-string without any abrubpt cuts due to hashtable overwrites. *
 * The overhead in passing these objects during searches and copying entire PVs
 * when alpha is raised is minimal compared to the overall search.
 */
class PrincipalVariation
{
    /** The 16-bit hash of the move that seems best to make */
    short move;
    /** A reference to the PV after the above move is played */
    PrincipalVariation subPV;

    /** Default constructor */
    PrincipalVariation(){}

    /**
     * Copies the entire Principal Variation (down to the depths). Quite a bit
     * of memory allocation is required here.
     * @param pv    the principal variation to copy
     */
    PrincipalVariation(PrincipalVariation pv)
    {
        this.move = pv.move; // Copy move hash by value
        if(pv.subPV != null) // Copy sub-pv down to the tips
            this.subPV = new PrincipalVariation(pv.subPV);
    }
}
