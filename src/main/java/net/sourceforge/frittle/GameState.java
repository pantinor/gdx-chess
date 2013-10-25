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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This is the state of the chess game at any point in the game.
 * The GameState contains the following information about the game:
 * <ul>
 * <li>Position of each piece on the board (with color)</li>
 * <li>Active player (WHITE or BLACK)</li>
 * <li>Whether active player is in check</li>
 * <li>Whether each player is allowed to castle kingside/queenside</li>
 * <li>En Passant square, if any</li>
 * <li>Halfmove clock (No. of plys since the last pawn move or capture)</li>
 * <li>Move number</li>
 * <li>Evaulation object (used only by AI)</li>
 * </ul>
 */
public class GameState
{
    /** A 64-array of squares on the board and the piece which is on the square */
	private Piece[] board;
    /** The side to move */
    private Player activePlayer;
    /** Castling rights */
    private boolean castleWhiteKingside;
    private boolean castleWhiteQueenside;
    private boolean castleBlackKingside;
    private boolean castleBlackQueenside;
    /** The square for en passant, if any */
    private byte enPassant;
    /** The location of the white king */
    private byte whiteKing;
    /** The location of the black king */
    private byte blackKing;


    /**
      * Regular expression for a FEN string
      * Groups:
      * 1 = board pattern
      * 2 = 'w' or 'b' for active player
      * 3 = 'K' if white can castle kingside
      * 4 = 'Q' if white can castle queenside
      * 5 = 'k' if black can castle kingside
      * 6 = 'q' if black can castle queenside
      * 7 = coordinate of en passant square, or else null
      * 8 = half move clock since last pawn move or capture
      * 9 = full move number
      */
     private static Pattern FENPattern = Pattern.compile("([pnbrqkPNBRQK\\-\\/1-8]+) ([wb]) (K)?(Q)?(k)?(q)?\\-? ([a-h][1-8])?\\-?(?: (\\d{1,2}) (\\d{1,2}))?");

	
	/**
	 * Create a new GameState that assumes default initial position of the board.
	 */
	public GameState()
	{
		// The default board
		Piece[] defaultBoard = {
			new Piece(PieceType.ROOK, Player.BLACK), new Piece(PieceType.KNIGHT, Player.BLACK), new Piece(PieceType.BISHOP, Player.BLACK), new Piece(PieceType.QUEEN, Player.BLACK), new Piece(PieceType.KING, Player.BLACK), new Piece(PieceType.BISHOP, Player.BLACK), new Piece(PieceType.KNIGHT, Player.BLACK), new Piece(PieceType.ROOK, Player.BLACK),
			new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK), new Piece(PieceType.PAWN, Player.BLACK),
			null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, 
			new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE), new Piece(PieceType.PAWN, Player.WHITE),
			new Piece(PieceType.ROOK, Player.WHITE), new Piece(PieceType.KNIGHT, Player.WHITE), new Piece(PieceType.BISHOP, Player.WHITE), new Piece(PieceType.QUEEN, Player.WHITE), new Piece(PieceType.KING, Player.WHITE), new Piece(PieceType.BISHOP, Player.WHITE), new Piece(PieceType.KNIGHT, Player.WHITE), new Piece(PieceType.ROOK, Player.WHITE)
		};
		
		// Copy this into the board array
		board = new Piece[64];
		System.arraycopy(defaultBoard,0,board,0,64);
		
		// Initialize values for other fields
		activePlayer = Player.WHITE;
		castleWhiteKingside = true;
		castleWhiteQueenside = true;
		castleBlackKingside = true;
		castleBlackQueenside = true;
		enPassant = -1;
        whiteKing = 60;
        blackKing = 4;
	}
	
	/**
	 * Creates a copy of another GameState.
	 *
	 * @param state 	the original gamestate to copy
	 */
	public GameState(GameState state)
	{
		// Copy the board array, including piece objects
		this.board = new Piece[64];
		for(int i=0; i<64; i++)
        {
            if(state.board[i]!=null)
                this.board[i] = new Piece(state.board[i].getType(),state.board[i].getPlayer());
        }
		// Copy information
		this.activePlayer = state.activePlayer;
		this.castleWhiteKingside = state.castleWhiteKingside;
		this.castleWhiteQueenside = state.castleWhiteQueenside;
		this.castleBlackKingside = state.castleBlackKingside;
		this.castleBlackQueenside = state.castleBlackQueenside;
		this.enPassant = state.enPassant;
        this.whiteKing = state.whiteKing;
        this.blackKing = state.blackKing;
	}

     /**
      * Constructs a new GameState object from the given FEN string.
      *
      * @param  FEN     the input string in Forsyth-Edward Notation
      */
     public GameState(String FEN) throws InvalidFENException
     {
         Matcher matcher = FENPattern.matcher(FEN);
         if(matcher.matches())
         {
             this.board = new Piece[64];
             String b = matcher.group(1);
             int i=0;   // board index
             int x;     // string index
             char c;    // character at x
             for(x=0; x<b.length(); x++)
             {
                 // Take each character of the FEN board at a time
                 c = b.charAt(x);
                 if(c == '/')
                     continue; // We don't need to parse a slash, it should occur at the end of a line only
                 else if(Character.isDigit(c))
                 {
                     // If it is a number, we shift our board index by that much
                     // because there will be k-many null squares
                     int k = Character.getNumericValue(c);
                     for(int j=0; j<k; j++)
                         i++;
                 }
                 else
                 {
                     // If we find a piece character, we add it to the board
                     switch(c)
                     {
                         case 'p':
                             this.board[i] = new Piece(PieceType.PAWN,Player.BLACK);
                             break;
                         case 'n':
                             this.board[i] = new Piece(PieceType.KNIGHT,Player.BLACK);
                             break;
                         case 'b':
                             this.board[i] = new Piece(PieceType.BISHOP,Player.BLACK);
                             break;
                         case 'r':
                             this.board[i] = new Piece(PieceType.ROOK,Player.BLACK);
                             break;
                         case 'q':
                             this.board[i] = new Piece(PieceType.QUEEN,Player.BLACK);
                             break;
                         case 'k':
                             this.board[i] = new Piece(PieceType.KING,Player.BLACK);
                             this.blackKing = (byte)i;
                             break;
                         case 'P':
                             this.board[i] = new Piece(PieceType.PAWN,Player.WHITE);
                             break;
                         case 'N':
                             this.board[i] = new Piece(PieceType.KNIGHT,Player.WHITE);
                             break;
                         case 'B':
                             this.board[i] = new Piece(PieceType.BISHOP,Player.WHITE);
                             break;
                         case 'R':
                             this.board[i] = new Piece(PieceType.ROOK,Player.WHITE);
                             break;
                         case 'Q':
                             this.board[i] = new Piece(PieceType.QUEEN,Player.WHITE);
                             break;
                         case 'K':
                             this.board[i] = new Piece(PieceType.KING,Player.WHITE);
                             this.whiteKing = (byte)i;
                             break;
                     }
                     // Incremement i for the next iteration
                     i++;
                 }
             }
             // Assert board was constructed correctly
             if(i!=64)
                 throw new InvalidFENException(FEN);

             // Now we move on

             // Active player
             switch(matcher.group(2).charAt(0))
             {
                 case 'w':
                     this.activePlayer = Player.WHITE;
                     break;
                 case 'b':
                     this.activePlayer = Player.BLACK;
                     break;
             }

             // Castling
             if(matcher.group(3) != null && matcher.group(3).charAt(0)=='K')
                 this.castleWhiteKingside = true;
             else
                 this.castleWhiteKingside = false;

             if(matcher.group(4) != null && matcher.group(4).charAt(0)=='Q')
                 this.castleWhiteQueenside = true;
             else
                 this.castleWhiteQueenside = false;

             if(matcher.group(5) != null && matcher.group(5).charAt(0)=='k')
                 this.castleBlackKingside = true;
             else
                 this.castleBlackKingside = false;

             if(matcher.group(6) != null && matcher.group(6).charAt(0)=='q')
                 this.castleBlackQueenside = true;
             else
                 this.castleBlackQueenside = false;

             // En Passant
             if(matcher.group(7) != null)
                 this.enPassant = Moves.toIndex(matcher.group(7));
             else
                 this.enPassant = -1;
             
         }
         else
         {
             throw new InvalidFENException(FEN);
         }

     }

     /**
      * Hashes the current board position and game state using the Zobrist method.
      * @return     the 64-bit long Zobrist hash key
      */
     public long hash()
     {
         return Zobrist.hash(this);
     }
	
	/**
     * Makes a move on the state. The properties of the state are thus changed.
     *
     * @param move  the move to make
     */
    public void doMove(Move move)
    {
        if(move != null)
        {
            // Shift positions of pieces on board
            if(move.castle == null)
            {
                board[move.dest] = board[move.source];
                board[move.source] = null;
                // If there was a promotion, change the piece type
                if(move.promotion != null)
                    board[move.dest].type = move.promotion;
                // If the capture was via en passant, remove that piece
                if(move.viaEP)
                {
                    if(activePlayer == Player.WHITE)
                        board[move.dest+8] = null;
                    else
                        board[move.dest-8] = null;
                }
            }
            else
            {
                switch(move.castle)
                {
                    case WK:
                        board[62] = board[60];
                        board[60] = null;
                        board[61] = board[63];
                        board[63] = null;
                        break;
                    case WQ:
                        board[58] = board[60];
                        board[60] = null;
                        board[59] = board[56];
                        board[56] = null;
                        break;
                    case BK:
                        board[6] = board[4];
                        board[4] = null;
                        board[5] = board[7];
                        board[7] = null;
                        break;
                    case BQ:
                        board[2] = board[4];
                        board[4] = null;
                        board[3] = board[0];
                        board[0] = null;
                        break;
                }
            }
            // Toggle castling rights
            if(move.toggleWK)
                castleWhiteKingside = !castleWhiteKingside;
            if(move.toggleWQ)
                castleWhiteQueenside = !castleWhiteQueenside;
            if(move.toggleBK)
                castleBlackKingside = !castleBlackKingside;
            if(move.toggleBQ)
                castleBlackQueenside = !castleBlackQueenside;

            // Remember en passant square
            enPassant = move.newEP;

            // Relocate king
            if(move.movedPiece.getType() == PieceType.KING)
            {
                if(activePlayer == Player.WHITE)
                    whiteKing = move.dest;
                else
                    blackKing = move.dest;
            }
        } // endif move is not null
        
        // Change active player
        activePlayer = activePlayer.opponent();
    }

    /**
     * Unmakes a previously made move on this state.
     *
     * @param move  the move to unmake
     */
    public void undoMove(Move move)
    {
        if(move != null)
        {
            // Switch back pieces from the squares
            if(move.castle == null)
            {
                // If there was a promotion, undo it
                if(move.promotion != null)
                    board[move.dest].type = PieceType.PAWN;
                // Undo position shift
                board[move.source] = board[move.dest];
                if(move.viaEP)
                {
                    board[move.dest] = null;
                    if(activePlayer.opponent() == Player.WHITE) // White did the en passant capture
                        board[move.dest+8] = move.capturedPiece;
                    else
                        board[move.dest-8] = move.capturedPiece;
                }
                else
                    board[move.dest] = move.capturedPiece;
            }
            else
            {
                switch(move.castle)
                {
                    case WK:
                        board[60] = board[62];
                        board[62] = null;
                        board[63] = board[61];
                        board[61] = null;
                        break;
                    case WQ:
                        board[60] = board[58];
                        board[58] = null;
                        board[56] = board[59];
                        board[59] = null;
                        break;
                    case BK:
                        board[4] = board[6];
                        board[6] = null;
                        board[7] = board[5];
                        board[5] = null;
                        break;
                    case BQ:
                        board[4] = board[2];
                        board[2] = null;
                        board[0] = board[3];
                        board[3] = null;
                        break;
                }
            }
            // Toggle castling rights
            if(move.toggleWK)
                castleWhiteKingside = !castleWhiteKingside;
            if(move.toggleWQ)
                castleWhiteQueenside = !castleWhiteQueenside;
            if(move.toggleBK)
                castleBlackKingside = !castleBlackKingside;
            if(move.toggleBQ)
                castleBlackQueenside = !castleBlackQueenside;

            // Remember the old en passant square
            enPassant = move.oldEP;

            // Relocate king back
            if(move.movedPiece.getType() == PieceType.KING)
            {
                // Note: use opponent() because after the move was made the
                // active player changes
                if(activePlayer.opponent() == Player.WHITE)
                    whiteKing = move.source;
                else
                    blackKing = move.source;
            }

        } // endif move is not null
        
        // Change active player
        activePlayer = activePlayer.opponent();
    }

    /**
     * Genrates a list of legal moves from the current state.
     *
     * @return  a list of legal moves
     */
    public MoveList generateMoves()
    {
        MoveList moves = new MoveList();
        for(byte s=0; s<64; s++ )
        {
            if(board[s] == null || board[s].getPlayer() != activePlayer)
                continue;
            switch(board[s].getType())
            {
                case KING:
                    for(byte d : Moves.king[s])
                    {
                        if(board[d] == null || board[d].getPlayer() != activePlayer)
                        {
                            moves.add(new Move(this, s, d));
                        }
                    }
                    break;
                case KNIGHT:
                    for(byte d : Moves.knight[s])
                    {
                        if(board[d] == null || board[d].getPlayer() != activePlayer)
                            moves.add(new Move(this, s, d));
                    }
                    break;
                case BISHOP:
                    for(byte[][] ray : Moves.bishop)
                    {
                        for(byte d : ray[s])
                        {
                            if(board[d] == null)
                                moves.add(new Move(this, s, d));
                            else
                            {
                                if(board[d].getPlayer() != activePlayer)
                                {
                                    moves.add(new Move(this, s, d));
                                }
                                break;
                            }
                        }
                    }
                    break;
                case ROOK:
                    for(byte[][] ray : Moves.rook)
                    {
                        for(byte d : ray[s])
                        {
                            if(board[d] == null)
                            {
                                moves.add(new Move(this, s, d));
                            }
                            else
                            {
                                if(board[d].getPlayer() != activePlayer)
                                {
                                    moves.add(new Move(this, s, d));
                                }
                                break;
                            }
                        }
                    }
                    break;
                case QUEEN:
                    for(byte[][] ray : Moves.queen)
                    {
                        for(byte d : ray[s])
                        {
                            if(board[d] == null)
                            {
                                moves.add(new Move(this, s, d));
                            }
                            else
                            {
                                if(board[d].getPlayer() != activePlayer)
                                {
                                    moves.add(new Move(this, s, d));
                                }
                                break;
                            }
                        }
                    }
                    break;
                case PAWN:
                    switch(activePlayer)
                    {
                        case WHITE:
                            for(byte d : Moves.pawnW[s])
                            {
                                if(board[d] == null)
                                {
                                    if(Moves.toRank(s) == 2 && Moves.toRank(d) == 4)
                                    {
                                        if(board[d+8]==null)
                                        {
                                            moves.add(new Move(this, s, d, null, false, d+8));
                                        }
                                    }
                                    else if(Moves.toRank(s) == 7)
                                    {
                                        moves.add(new Move(this, s, d, PieceType.QUEEN,  false, -1));
                                        moves.add(new Move(this, s, d, PieceType.ROOK,   false, -1));
                                        moves.add(new Move(this, s, d, PieceType.BISHOP, false, -1));
                                        moves.add(new Move(this, s, d, PieceType.KNIGHT, false, -1));
                                    }
                                    else
                                        moves.add(new Move(this, s, d));
                                }
                            }
                            for(byte d: Moves.pawnWX[s])
                            {
                                if(board[d] != null && board[d].getPlayer() != activePlayer)
                                {
                                    if(Moves.toRank(s) == 7)
                                    {
                                        moves.add(new Move(this, s, d, PieceType.QUEEN,  false, -1));
                                        moves.add(new Move(this, s, d, PieceType.ROOK,   false, -1));
                                        moves.add(new Move(this, s, d, PieceType.BISHOP, false, -1));
                                        moves.add(new Move(this, s, d, PieceType.KNIGHT, false, -1));
                                    }
                                    else
                                        moves.add(new Move(this, s, d));
                                }
                                else if(d == enPassant)
                                {
                                    moves.add(new Move(this, s, d, null, true, -1));
                                }
                            }
                            break;
                        case BLACK:
                            for(byte d : Moves.pawnB[s])
                            {
                                if(board[d] == null)
                                {
                                    if(Moves.toRank(s) == 7 && Moves.toRank(d) == 5)
                                    {
                                        if(board[d-8]==null)
                                        {
                                            moves.add(new Move(this, s, d, null, false, d-8));
                                        }
                                    }
                                    else if(Moves.toRank(s) == 2)
                                    {
                                        moves.add(new Move(this, s, d, PieceType.QUEEN,  false, -1));
                                        moves.add(new Move(this, s, d, PieceType.ROOK,   false, -1));
                                        moves.add(new Move(this, s, d, PieceType.BISHOP, false, -1));
                                        moves.add(new Move(this, s, d, PieceType.KNIGHT, false, -1));
                                    }
                                    else
                                        moves.add(new Move(this, s, d));
                                }
                            }
                            for(byte d: Moves.pawnBX[s])
                            {
                                if(board[d] != null && board[d].getPlayer() != activePlayer)
                                {
                                    if(Moves.toRank(s) == 2)
                                    {
                                        moves.add(new Move(this, s, d, PieceType.QUEEN,  false, -1));
                                        moves.add(new Move(this, s, d, PieceType.ROOK,   false, -1));
                                        moves.add(new Move(this, s, d, PieceType.BISHOP, false, -1));
                                        moves.add(new Move(this, s, d, PieceType.KNIGHT, false, -1));
                                    }
                                    else
                                        moves.add(new Move(this, s, d));
                                }
                                else if(d == enPassant)
                                {
                                    moves.add(new Move(this, s, d, null, true, -1));
                                }
                            }
                            break;
                    }
                    break;
            }
        }
        switch(activePlayer)
        {
            case WHITE:
                if(castleWhiteKingside &&!isUnderAttack(60) &&
                        board[61]==null && !isUnderAttack(61) &&
                        board[62]==null && !isUnderAttack(62) )
                {
                    moves.add(new Move(this, Move.Castle.WK));
                }
                if(castleWhiteQueenside &&!isUnderAttack(60) &&
                        board[59]==null && !isUnderAttack(59) &&
                        board[58]==null && !isUnderAttack(58) && board[57] == null )
                {
                    moves.add(new Move(this, Move.Castle.WQ));
                }
                break;
            case BLACK:
                if(castleBlackKingside &&!isUnderAttack(4) &&
                        board[5]==null && !isUnderAttack(5) &&
                        board[6]==null && !isUnderAttack(6) )
                {
                    moves.add(new Move(this, Move.Castle.BK));
                }
                if(castleBlackQueenside &&!isUnderAttack(4) &&
                        board[3]==null && !isUnderAttack(3) &&
                        board[2]==null && !isUnderAttack(2) && board[1] == null )
                {
                    moves.add(new Move(this, Move.Castle.BQ));
                }
                break; 
        }
        return moves;
    }

    /**
     * Generates a list of legal captures from the current state
     * @return  the list of moves that are captures
     */
    public MoveList generateCaptures()
    {
        MoveList allMoves = generateMoves();
        MoveList captures = new MoveList();
        for(Move move : allMoves)
        {
            if(move.isCapture())
                captures.add(move);
        }
        return captures;
    }

	/**
	 * Determines whether a square is defended by the active player
	 *
	 * @param	sq			the target square to check
	 * @return	whether or not the square is defended
	 */
	public boolean isDefended(byte sq)
	{
		return isCoveredBy(this.getActivePlayer(),sq);
	}
	
	/**
	 * Determines whether a square is under attack by the inactive player (opponent)
	 *
	 * @param	sq			the target square to check
	 * @return	whether or not the square is under attack
	 */
	public boolean isUnderAttack(int sq)
	{
		return isCoveredBy(this.getActivePlayer().opponent(),sq);
	}

	/**
	 * Checks whether the king of the given player is left in check.
     * This method is used privately by the constructor from a move to see
     * if active player is in check or moved player is in check (illegal move).
	 *
	 * @param player		the player for whom to check
	 * @return              whether the king of the given player is in check
	 */
	public boolean isInCheck(Player player)
	{
		return this.isCoveredBy(player.opponent(), findKing(player));
	}
	
	/**
	 * Determines whether a square is covered by given player.
	 *
	 * @param	player      the player who might cover the area
	 * @param	sq			the target square to check
	 * @return	whether or not the square is covered
	 */
	public boolean isCoveredBy(Player player, int sq)
	{
		byte i,j,x; Piece p; byte[][] ray;
		// First check for kings
		for(i=0; i<Moves.king[sq].length; i++)
		{
			x = Moves.king[sq][i];
			p = this.getBoard()[x];
			if(p != null && p.getType() == PieceType.KING && p.getPlayer() == player)
				return true;
		}
		// Check knights
		for(i=0; i<Moves.knight[sq].length; i++)
		{
			x = Moves.knight[sq][i];
			p = this.getBoard()[x];
			if(p != null && p.getType() == PieceType.KNIGHT && p.getPlayer() == player)
				return true;
		}
		// Check diagonals
		for(j=0; j<4; j++)
		{
			ray = Moves.bishop[j];
			for(i=0; i<ray[sq].length; i++)
			{
				x = ray[sq][i];
				p = this.getBoard()[x];
				if(p != null)
				{
					if((p.getType() == PieceType.BISHOP || p.getType() == PieceType.QUEEN) && p.getPlayer() == player)
						return true;
					else // There is some other piece in the way in the ray
						break;
				}
			}
		}
		// Check straights		
		for(j=0; j<4; j++)
		{
			ray = Moves.rook[j];
			for(i=0; i<ray[sq].length; i++)
			{
				x = ray[sq][i];
				p = this.getBoard()[x];
				if(p != null)
				{
					if((p.getType() == PieceType.ROOK || p.getType() == PieceType.QUEEN) && p.getPlayer() == player)
						return true;
					else // There is some other piece in the way in the ray
						break;
				}
			}
		}
		// Check pawn
		if(player == Player.WHITE)
		{
			for(i=0; i<Moves.pawnBX[sq].length; i++) // We use the move array of opposite color to reverse direction
			{
				x = Moves.pawnBX[sq][i];
				p = this.getBoard()[x];
				if(p != null && p.getType() == PieceType.PAWN && p.getPlayer() == player)
					return true;
			}
		} // Black
		else
		{
			for(i=0; i<Moves.pawnWX[sq].length; i++) // We use the move array of opposite color to reverse direction
			{
				x = Moves.pawnWX[sq][i];
				p = this.getBoard()[x];
				if(p != null && p.getType() == PieceType.PAWN && p.getPlayer() == player)
					return true;
			}
		}			
		return false;
	}
	
	/**
	 * Equates two distinct instances of GameState to see if they
	 * represent the same state of the board and game
	 *
	 * @param	obj		the other GameState object
     * @return  true if the GameState objects are equal, false otherwise
	 */
	public boolean equals(Object obj)
	{	
		GameState other = (GameState)obj;
		// Check board positions
		Piece p1, p2;
		for(int i=0; i<64; i++)
		{
			p1 = this.getBoard()[i];
			p2 = other.getBoard()[i];
			// First check if both are empty
			if( p1 == null && p2 == null )
				continue;
			// Check if one is empty
			if( (p1 == null) || (p2 == null)  ) 
				return false;
			// At this point there is a piece in both p1 and p2. Now lets see if it is the same piece
			if( (p1.getPlayer() != p2.getPlayer()) || (p1.getType() != p2.getType()) )
				return false;
		}
		
		// Check castling rights
		if( this.canCastleWhiteKingside() != other.canCastleWhiteKingside() || this.canCastleWhiteQueenside() != other.canCastleWhiteQueenside()
		 || this.canCastleBlackKingside() != other.canCastleBlackKingside() || this.canCastleBlackQueenside() != other.canCastleBlackQueenside() )
			return false;
			
		// Check en passant
		if( this.getEnPassant() != other.getEnPassant() )
			return false;
		
		// At this point everything checks out
		return true;
	}
	
	/**
	 * Generates a FEN equivalent of the given state.
	 *
	 * @return 		the FEN equivalent string
	 */
	public String toFEN()
	{
		StringBuffer FEN = new StringBuffer(70);
		int i,j,k,e;
		for(i=0; i<8; i++)
		{
			e = 0; // No. of empty squares
			for(j=0; j<8; j++)
			{
				k = i*8+j; // Array index
				if(getBoard()[k] != null)
				{
					if( e > 0 )
					{
						FEN.append(e);
						e = 0;
					}
					FEN.append(getBoard()[k].getChar());
				}
				else
				{
					e++;
				}
			}
			if( e > 0 )
			{
				FEN.append(e);
			}
			FEN.append('/');
		}
		FEN.deleteCharAt(FEN.length()-1); // to remove the trailing '/'
		FEN.append(' ');
		FEN.append(this.getActivePlayer()==Player.WHITE ? 'w' : 'b');
		FEN.append(' ');
		if(this.canCastleWhiteKingside())
			FEN.append('K');
		if(this.canCastleWhiteQueenside())
			FEN.append('Q');
		if(this.canCastleBlackKingside())
			FEN.append('k');
		if(this.canCastleBlackQueenside())
			FEN.append('q');
        if(FEN.charAt(FEN.length()-1)==' ') // No castling symbol was added
            FEN.append('-');
		FEN.append(' ');
		FEN.append(this.getEnPassant() == -1 ? '-' : Moves.toCoOrdinate(this.getEnPassant()));
		return FEN.toString();
	}

    /**
     * Information about the current board and positions of each piece.
     * The board is 8x8 but this array is a linear array of 64.
     * To convert conventional co-ordinates (eg. e4) to array index,
     * use following formula: (8-rank)*8 + (file-1)... therefore a8=0,h1=63.
     * If there is no piece at a particular position, the value is NULL.
     * @return the board
     */
    public Piece[] getBoard() {
        return board;
    }

    /**
     * Active player. The player whose turn it is next.
     * Whether WHITE or BLACK.
     * @return the active player
     */
    public Player getActivePlayer() {
        return activePlayer;
    }

    /**
     * Kingside-Castling rights of White.
     * @return whether white can castle kingside
     */
    public boolean canCastleWhiteKingside() {
        return castleWhiteKingside;
    }

    /**
     * Queenside-Castling rights of White.
     * @return whether white can castle queenside
     */
    public boolean canCastleWhiteQueenside() {
        return castleWhiteQueenside;
    }

    /**
     * Kingside-Castling rights of Black.
     * @return whether black can castle kingside
     */
    public boolean canCastleBlackKingside() {
        return castleBlackKingside;
    }

    /**
     * Queenside-Castling rights of Black.
     * @return Whether black can castle queenside
     */
    public boolean canCastleBlackQueenside() {
        return castleBlackQueenside;
    }

    /**
     * En Passant square, if any.
     * Position of the square that can be captured en passant, if there is one.
     * The value is -1 if there is no such position.
     * @return the enPassant square
     */
    public byte getEnPassant() {
        return enPassant;
    }

    /**
     * Locate the king of the given player
     * @return  the square where the king of the given player is located
     */
    public byte findKing(Player player)
    {
        if(player == Player.WHITE)
            return whiteKing;
        else
            return blackKing;
    }
}
	