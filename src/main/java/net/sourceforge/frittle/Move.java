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
 * The Move class represents a possible move in a chess game.
 * The information in an object of Move is the source and destination positions,
 * and the promotion if any.
 */
public class Move
{
    /** Source square of the move */
	public byte source;
    /** Destination square of the move */
	public byte dest;
    /** Reference to the piece that moved */
    public Piece movedPiece;
    /** Reference to the piece that was captured and removed */
    public Piece capturedPiece;
    /** Specifies the type of piece to promote to, or null in case of no promotion */
	public PieceType promotion;
    /** Whether this move changes the kingside castling rights of white */
    public boolean toggleWK;
    /** Whether this move changes the kingside castling rights of black */
    public boolean toggleBK;
    /** Whether this move changes the queenside castling rights of white */
    public boolean toggleWQ;
    /** Whether this move changes the queenside castling rights of black */
    public boolean toggleBQ;
    /** A flag that determines if the move was a castle, or null if it was a normal move */
    public Castle castle;
    /** The square that was left en passant BEFORE the move was made (useful for undoMove) */
    public byte oldEP;
    /** The square that was left en passant AFTER the move was made */
    public byte newEP;
    /** Whether the move was an en passant capture */
    public boolean viaEP;
    /** Whether this move checks the opponent forcing the next side to move to evade check */
    public boolean checking;
    /** Whether the move exposes the moving player's king to check and is thus illegal */
    public boolean illegal;

    /** An enum that determines the type of castle */
    public enum Castle { WK, WQ, BK, BQ };

    /**
     * Constructs a Move object for a non-castle move.
     *
     * @param state             the board state before the move was made
     * @param source            source square of move
     * @param dest              target square of move
     * @param promotion         type of promotion, or <code>null</code> if no promotion
	 * @param viaEP				whether the move is an en-passant capture
	 * @param newEP				the new en-passant square, or <code>-1</code> if there isn't one
     */
	public Move(GameState state, byte source, byte dest, PieceType promotion, boolean viaEP, int newEP)
    {
        // Set fields
		this.source = source;
        this.dest = dest;
        this.movedPiece = state.getBoard()[source];
        this.viaEP = viaEP;
		if(viaEP)
        {
            if(state.getActivePlayer() == Player.WHITE)
                this.capturedPiece = state.getBoard()[dest+8];
            else
                this.capturedPiece = state.getBoard()[dest-8];
        }
		else			
			this.capturedPiece = state.getBoard()[dest];
        this.promotion = promotion;
        this.castle = null;
        this.oldEP = state.getEnPassant();
        this.newEP = (byte)newEP;
        // Check if castling rights were lost
        if(state.canCastleWhiteKingside() && (source == 60 || source == 63 || dest == 63))
            this.toggleWK = true;
        if(state.canCastleWhiteQueenside() && (source == 60 || source == 56 || dest == 56))
            this.toggleWQ = true;
        if(state.canCastleBlackKingside() && (source == 4 || source == 7 || dest == 7))
            this.toggleBK = true;
        if(state.canCastleBlackQueenside() && (source == 4 || source == 0 || dest == 0))
            this.toggleBQ = true;
        // Look for check status
        state.doMove(this);
        /*if(state.isInCheck(state.getActivePlayer()))
            this.checking = true;*/
        if(state.isInCheck(state.getActivePlayer().opponent()))
            this.illegal = true;
        state.undoMove(this);
	}
	
    /**
     * Constructs a simple move (no promotions or en passant)
     *
     * @param state             the board state before the move was made
     * @param source            source square of move
     * @param dest              target square of move
     */
	public Move(GameState state, byte source, byte dest)
	{
		this(state, source, dest, null, false, -1);
	}

    /**
     * Constructs a Move object for a castling move.
     *
     * @param state         the board state before the move was made
     * @param castle        the type of castle (color and kingside/queenside)
     */
    public Move(GameState state, Castle castle)
    {
        this.castle = castle;
        // Set flags to change castling rights
        // Also set source and dest for compatibility with toString()
        switch(castle)
        {
            case WK:
                toggleWK = true;
                if(state.canCastleWhiteQueenside())
                    toggleWQ = true;
                source = 60;
                dest = 62;
                break;
            case WQ:
                toggleWQ = true;
                if(state.canCastleWhiteKingside())
                    toggleWK = true;
                source = 60;
                dest = 58;
                break;
            case BK:
                toggleBK = true;
                if(state.canCastleBlackQueenside())
                    toggleBQ = true;
                source = 4;
                dest = 6;
                break;
            case BQ:
                toggleBQ = true;
                if(state.canCastleBlackKingside())
                    toggleBK = true;
                source = 4;
                dest = 2;
                break;
        }
        this.movedPiece = state.getBoard()[source];
        this.oldEP = state.getEnPassant();
        this.newEP = -1;
    }

    /**
     * Generates a short hash value for the move (used for storage purposes)
     * @return  a 16-bit hash value (actually uses only 15 bits)
     */
    public short hash()
    {
        int hash = 0;
        // Append source (6 bits)
        hash |= source;
        // Shift left 6 bits to make space for dest
        hash <<= 6;
        // Append dest (6 bits)
        hash |= dest;
        // Shift left 3 bits to make space for promotion
        hash <<= 3;
        // If no promotion, the bits are 000
        // Otherwise they are the ordinal of the piecetype
        // i.e. 1-N,2-B,3-R,4-Q
        if(promotion != null)
            hash |= promotion.ordinal();
        return (short)hash;        
    }

    /**
     * Converts a 16-bit move hash value to a string representation in
     * co-ordinate notation
     * @param   hash    the 16-bit hash value
     * @return  the string representation of the move in co-ordinate notation
     * @see     hash()
     */
    public static String hashToString(short hash)
    {
        if(hash == 0) // invalid move
            return null;
        // Get 3 bits for promotional piece type
        int ipromo = hash & 7;
        PieceType promotion = (ipromo == 0) ? null : PieceType.values()[ipromo];
        // Shift right 3 bits to get dest part in front
        hash >>= 3;
        // Get the 6 bits of the dest
        int dest = hash & 63;
        // Shift right 6 bits to get the source part in front
        hash >>= 6;
        // Get the 6 bits of the source
        int source = hash & 63;
        String moveStr = Moves.toCoOrdinate((byte)source) +
                 Moves.toCoOrdinate((byte)dest);
        if(promotion != null)
            moveStr = moveStr + promotion.getLowerChar() ;
        return moveStr;

    }

	/**
	 * Converts the Move object into co-ordinate notation
	 *
	 * @return	the move in co-ordinate notation
	 */
	@Override public String toString()
    {
        String str = Moves.toCoOrdinate(source)+Moves.toCoOrdinate(dest);
        if(this.promotion != null)
            str = str + this.promotion.getLowerChar();
        return str;
	}

    /**
     * Indicates whether this move was a capture
     * @return  <code>true</code> if the move was a capture, <code>false</code> if not
     */
    public boolean isCapture()
    {
        return (capturedPiece != null);
    }
    
    /**
     * Indicates whether this move was a castle
     * @return  <code>true</code> if the move was a castle, <code>false</code> if not
     */
    public boolean isCastle()
    {
        return (castle != null);
    }

    /**
     * Indicates whether this move was a promotion
     * @return  <code>true</code> if the move was a promotion, <code>false</code> if not
     */
    public boolean isPromotion()
    {
        return (promotion != null);
    }

    /**
     * Indicates whether the move is legal as far as exposing the moving player's
     * king to check goes.
     *
     * The move should be legal by the rules of chess in terms
     * of the piece moving from source to destination square because the move
     * generator will construct only legal <code>Move</code> objects. It is the
     * programmer's responsibility to make sure that <code>doMove()</code> and
     * <code>undoMove()</code> is called on the correct <code>GameState</code>
     * and <code>Move</code> object pair.
     *
     * @return  <code>false</code> if the move exposes the moving player's king to check
     */
    public boolean isLegal()
    {
        return !illegal;
    }

    /**
     * Get the most efficient move that can capture at square x. The most
     * efficient maeans that it will find the least valuable attacker to
     * make the capture. Note that this method does not take into account
     * (1) castling rights - because in recapture calculations we will never
     * go down a variation where castling is the last thing we do
     * (2) promotion - because if a pawn can capture on the last rank and it is
     * recaptured, we cant to consider that we recaptured a 'pawn' and not a
     * 'queen'.
     * (3) en passant info - because in exchange evaluation this is never
     * required.
     * @param state     the original state of the board
     * @param x         the square to capture at
     * @return  the best Move if found, or else null
     */
    public static Move getBestCapture(GameState state, byte x)
    {
        Move move; Piece p;
        Piece[] board = state.getBoard();
        Player player = state.getActivePlayer();
        // If there is a no piece or a friendly piece on the target square,
        //  there can't be any move to capture at target
        if(board[x] == null || board[x].getPlayer() == player)
            return null;
        // First find a pawn
        switch(player)
        {
            case WHITE:
                // We have to find a white pawn one step diagonally to the bottom
                // rank. For that, we use the attack vector of black pawns (southwards)
                for(byte s : Moves.pawnBX[x])
                {
                    p = board[s];
                    if(p==null)
                        continue;
                    if(p.getType() == PieceType.PAWN && p.getPlayer() == Player.WHITE)
                    {
                        move = new Move(state, s, x);
                        if(move.isLegal())
                            return move;
                    }
                }
                break;
            case BLACK:
                // We have to find a black pawn one step diagonally to the top
                // rank. For that, we use the attack vector of white pawns (northwards)
                for(byte s : Moves.pawnWX[x])
                {
                    p = board[s];
                    if(p==null)
                        continue;
                    if(p.getType() == PieceType.PAWN && p.getPlayer() == Player.BLACK)
                    {
                        move = new Move(state, s, x);
                        if(move.isLegal())
                            return move;
                    }
                }
                break;
        }
        // Now look for a knight
        for(byte s : Moves.knight[x])
        {
            p = board[s];
            if(p==null)
                continue;
            if(p.getType() == PieceType.KNIGHT && p.getPlayer() == player)
            {
                move = new Move(state, s, x);
                if(move.isLegal())
                    return move;
            }
        }
        // Now look for a bishop
        for(byte[][] ray : Moves.bishop)
        {
            for(byte s : ray[x])
            {
                p = board[s];
                if(p == null)
                    continue;
                if(p.getType() == PieceType.BISHOP && p.getPlayer() == player)
                {
                    move = new Move(state, s, x);
                    if(move.isLegal())
                        return move;
                }
                // At this point p is not null and either p is not the required piece,
                // or p is required piece but move is not legal, so the ray is blocked
                break;
            }
        }
        // Now look for a rook
        for(byte[][] ray : Moves.rook)
        {
            for(byte s : ray[x])
            {
                p = board[s];
                if(p == null)
                    continue;
                if(p.getType() == PieceType.ROOK && p.getPlayer() == player)
                {
                    move = new Move(state, s, x);
                    if(move.isLegal())
                        return move;
                }
                // At this point p is not null and either p is not the required piece,
                // or p is required piece but move is not legal, so the ray is blocked
                break;
            }
        }
        // Now look for a queen
        for(byte[][] ray : Moves.queen)
        {
            for(byte s : ray[x])
            {
                p = board[s];
                if(p == null)
                    continue;
                if(p.getType() == PieceType.QUEEN && p.getPlayer() == player)
                {
                    move = new Move(state, s, x);
                    if(move.isLegal())
                        return move;
                }
                // At this point p is not null and either p is not the required piece,
                // or p is required piece but move is not legal, so the ray is blocked
                break;
            }
        }
        // Finally look for a king
        for(byte s : Moves.king[x])
        {
            p = board[s];
            if(p==null)
                continue;
            if(p.getType() == PieceType.KING && p.getPlayer() == player)
            {
                move = new Move(state, s, x);
                if(move.isLegal())
                        return move;
            }
        }
        // If we havn't returned yet it means no good attacker was found
        return null;
    }

    /**
     * Parses a move in Standard Algebraic notation and returns a Move object.
     *
     * @param   state       the GameState before the move is attempted
     * @param   string      the input string in standard algebraic notation
     * @return  the Move object formed from the SAN notation
     * @throws  IllegalMoveException    if the string is not in SAN
     */
    /*public static Move parseSAN(GameState state, String string) throws IllegalMoveException
    {
        Matcher matcher = SANPattern.matcher(string);
        if(!matcher.matches())
        {
            throw new IllegalMoveException();
        }
        // Now parse the retreived groups from the regex
        byte source = -1;
        PieceType promotion = null;
        // First get the destination square and the type of piece
        byte dest   = GameState.toIndex(matcher.group(5).charAt(0),Byte.parseByte(matcher.group(6)));
        PieceType type = null;
        if(matcher.group(1) == null)
        {
            type = PieceType.PAWN;
        }
        else
        {
            switch(matcher.group(1).charAt(0))
            {
                case 'K':
                    type = PieceType.KING;
                    break;
                case 'Q':
                    type = PieceType.QUEEN;
                    break;
                case 'R':
                    type = PieceType.ROOK;
                    break;
                case 'B':
                    type = PieceType.BISHOP;
                    break;
                case 'N':
                    type = PieceType.KNIGHT;
                    break;
            }
        }
        java.util.Vector<Byte> sources = new java.util.Vector<Byte>();
        int i,j;
        byte[][] ray;
        Piece piece;
        switch(type)
        {
            case KING:
                for(i=0; i<Moves.king[dest].length; i++)
                {
                    piece = state.getBoard()[Moves.king[dest][i]];
                    if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.KING)
                        sources.addElement(new Byte(Moves.king[dest][i]));
                }
                break;
            case KNIGHT:
                for(i=0; i<Moves.knight[dest].length; i++)
                {
                    piece = state.getBoard()[Moves.knight[dest][i]];
                    if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.KNIGHT)
                        sources.addElement(new Byte(Moves.knight[dest][i]));
                }
                break;
            case BISHOP:
                for(j=0; j<Moves.diagonals.length; j++)
                {
                    ray = Moves.diagonals[j];
                    for(i=0; i<ray[dest].length; i++)
                    {
                        piece = state.getBoard()[ray[dest][i]];
                        if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.BISHOP)
                            sources.addElement(new Byte(ray[dest][i]));
                    }
                }
                break;
            case ROOK:
                for(j=0; j<Moves.straights.length; j++)
                {
                    ray = Moves.straights[j];
                    for(i=0; i<ray[dest].length; i++)
                    {
                        piece = state.getBoard()[ray[dest][i]];
                        if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.ROOK)
                            sources.addElement(new Byte(ray[dest][i]));
                    }
                }
                break;
            case QUEEN:
                for(j=0; j<Moves.diagonals.length; j++)
                {
                    ray = Moves.diagonals[j];
                    for(i=0; i<ray[dest].length; i++)
                    {
                        piece = state.getBoard()[ray[dest][i]];
                        if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.QUEEN)
                            sources.addElement(new Byte(ray[dest][i]));
                    }
                }
                for(j=0; j<Moves.straights.length; j++)
                {
                    ray = Moves.straights[j];
                    for(i=0; i<ray[dest].length; i++)
                    {
                        piece = state.getBoard()[ray[dest][i]];
                        if(piece.getPlayer() == state.getActivePlayer() && piece.getType()==PieceType.QUEEN)
                            sources.addElement(new Byte(ray[dest][i]));
                    }
                }
                break;
            case PAWN:
                break;
        }

        return new Move(source, dest, promotion);
    }*/
}
		
		
		
		