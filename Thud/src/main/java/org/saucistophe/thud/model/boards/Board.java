package org.saucistophe.thud.model.boards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.saucistophe.thud.model.Coordinate;
import org.saucistophe.thud.model.Piece;
import static org.saucistophe.thud.model.Piece.DWARF;
import static org.saucistophe.thud.model.Piece.EMPTY;
import static org.saucistophe.thud.model.Piece.OUT;
import static org.saucistophe.thud.model.Piece.TROLL;

/**
 The board corresponds to a state of the game, and contains an 2D array of
 pieces. This class contains AI methods and everything pertaining to game
 moves. Kind of a god object, but hey, what the heck.
 */
public abstract class Board implements Cloneable
{
	/**
	 The value considered as a maximum value for score evaluation.
	 */
	public static int INFINITY = Integer.MAX_VALUE - 5;

	/**
	 This class' logger.
	 */
	public static Logger LOGGER = Logger.getLogger(Board.class.getName());

	/**
	 The grid of pieces.
	 */
	public Piece squares[][];

	/**
	 The current playing side, true if the dwarves are playing.
	 */
	public boolean dwarvesTurn;

	private static List<Coordinate> piecesCache = null;

	/**
	 Returns a set containing each possible move for the specified piece.

	 @param x The X location of the piece to move.
	 @param y The Y location of the piece to move.
	 @param trollShovings A list of the troll shoving move, that will be filled by this method.
	 @return a set of the possible move. This set contains each move only once.
	 */
	public abstract List<Coordinate> validMoves(int x, int y, List<Coordinate> trollShovings);

	public Board()
	{
	}

	/**
	 Clones the current board.

	 @return A board, identical to this one.
	 */
	public Board cloneBoard()
	{
		Board result = null;
		try
		{
			result = (Board) this.clone();
			result.set(this);
		} catch (CloneNotSupportedException ex)
		{
			Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
		}

		return result;
	}

	public void set(Board thatBoard)
	{
		this.squares = Arrays.stream(thatBoard.squares).map(x -> x.clone()).toArray(Piece[][]::new);
		this.dwarvesTurn = thatBoard.dwarvesTurn;
	}

	/**
	 @return The width of the board, as infered from the arrays lengths.
	 */
	public int getWidth()
	{
		return squares.length;
	}

	/**
	 @return The height of the board, as infered from the arrays lengths.
	 */
	public int getHeight()
	{
		return squares[0].length;
	}

	/**
	 Returns true if the specified square is near a dwarf.

	 @param x The X location of the square to check.
	 @param y
	 the Y location of the square to check.
	 @return <b>true</b> if the specified place is near a dwarf.
	 */
	public boolean isNearADwarf(int x, int y)
	{
		return !getNearby(DWARF, x, y).isEmpty();
	}

	/**
	 @param x The X location of the square to check.
	 @param y The Y location of the square to check.
	 @return True if the specified square is inside the board.
	 */
	public boolean isInsideBounds(int x, int y)
	{
		// If the square is out of the physical board's dimensions:
		if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
		{
			return false;
		}
		else
		{
			// If the square is marked as out.
			return squares[x][y] != OUT;
		}
	}

	/**
	 @param coordinate The location of the square to check.
	 @return True if the specified square is inside the board.
	 */
	public boolean isInsideBounds(Coordinate coordinate)
	{
		return isInsideBounds(coordinate.width, coordinate.height);
	}

	/**
	 Moves a piece to a square.

	 @param x1 The X location of the piece to move.
	 @param y1 The Y location of the piece to move.
	 @param x2 The X location of the destination square.
	 @param y2 The Y location of the destination square.
	 @return The list of potential target, for troll players.
	 */
	public List<Coordinate> move(int x1, int y1, int x2, int y2)
	{
		boolean isTroll = squares[x1][y1] == TROLL;

		squares[x2][y2] = squares[x1][y1];
		squares[x1][y1] = EMPTY;

		// Change the turn.
		dwarvesTurn = !dwarvesTurn;

		// We check if a (or several) dwarf is captured.
		if (isTroll)
		{
			return getNearby(DWARF, x2, y2);
		}
		return null;
	}

	/**
	 @param pieceType The piece type to look for.
	 @param x The X location of the square to check.
	 @param y The Y location of the square to check.
	 @return The list of dwarves near the given square.
	 */
	public List<Coordinate> getNearby(Piece pieceType, int x, int y)
	{
		return Arrays.stream(Coordinate.directions)
			.map(direction -> new Coordinate(x + direction.width, y + direction.height))
			// Must be on the board
			.filter(this::isInsideBounds)
			// Must be a dwarf
			.filter(square -> getPiece(square) == pieceType)
			.collect(Collectors.toList());
	}

	/**
	 Counts the number of the choosen piece on the board.

	 @param piece The chosen type of piece
	 @return The number of occurences of this piece on the board.
	 */
	public int numberOf(Piece piece)
	{
		return (int) getPiecesStream(piece).count();
	}

	/**
	 @return A list of the possible boards after moving.
	 */
	public List<Board> childrenBoards()
	{// TODO : for NegaScout, order moves to get the more interesting first.
		List<Board> result = new ArrayList<>();
		Stream<Coordinate> piecesToMove = getPiecesStream(dwarvesTurn ? DWARF : TROLL);

		// Iterate over pieces
		piecesToMove.forEach(pieceToMove
			->
			{
				// Iterate over the piece's possible destinations
				List<Coordinate> trollShovings = null;
				if (!dwarvesTurn)
				{
					trollShovings = new ArrayList<>();
				}

				List<Coordinate> possibleMoves = validMoves(pieceToMove.width, pieceToMove.height, trollShovings);
				for (Coordinate destination : possibleMoves)
				{
					// Create an imaginary board from the move.
					Board temporaryBoard = this.cloneBoard();
					List<Coordinate> dwarvesVictim = temporaryBoard.move(pieceToMove.width, pieceToMove.height, destination.width, destination.height);

					// If there is no dwarf victim, simply effect the move.
					if (dwarvesVictim == null || dwarvesVictim.isEmpty())
					{
						result.add(temporaryBoard);
					}
					else // If it's a troll shoving, for simplicity, kill all the dwarves.
					if (trollShovings != null && trollShovings.contains(destination))
					{
						for (Coordinate victim : dwarvesVictim)
						{
							temporaryBoard.squares[victim.width][victim.height] = EMPTY;
						}
						result.add(temporaryBoard);
					}
					// If not, only one victim can be made, create a board for each one.
					else
					{
						for (Coordinate victim : dwarvesVictim)
						{
							Board victimTemporaryBoard = temporaryBoard.cloneBoard();
							victimTemporaryBoard.squares[victim.width][victim.height] = EMPTY;
							result.add(victimTemporaryBoard);
						}
					}
				}
		});
		return result;
	}

	/**
	 Returns a list of the pieces of the given type.

	 @param type The type to get, DWARF or TROLL.
	 @return A list of the coordinates of the pieces.
	 */
	public Stream<Coordinate> getPiecesStream(Piece type)
	{
		return piecesCache.stream()
			.filter(coordinate -> squares[coordinate.width][coordinate.height] == type);
	}

	/**
	 Writes the board quickly to a new file.

	 @return The file where it is saved.
	 */
	public File writeQuick()
	{
		File tempFile = new File(this.hashCode() + ".thud");
		try
		{
			writeToFile(tempFile);
		} catch (FileNotFoundException | UnsupportedEncodingException ex)
		{
			Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
		}
		return tempFile;
	}

	/**
	 Writes the board to a file, in a quite readable format.

	 @param outputFile The file to write to.
	 @throws FileNotFoundException        If the file does not exist and can't be created.
	 @throws UnsupportedEncodingException If UTF-8 is not supported.
	 */
	public void writeToFile(File outputFile) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");

		// Iterate on squares
		for (int j = 0; j < getHeight(); j++)
		{
			// Skip the firt line feed
			if (j != 0)
			{
				writer.println();
			}

			for (int i = 0; i < getWidth(); i++)
			{
				// Write the playing side on the top left corner.
				if (i == 0 && j == 0)
				{
					writer.print(dwarvesTurn ? "D" : "T");
				}
				else
				{
					// Otherwise, print the piece's code.
					writer.print(squares[i][j].text);
				}
			}
		}

		writer.close();
	}

	/**
	 Reads a board from a thud! file.

	 @param inputFile The file to read.
	 @return The stored board.

	 @throws IOException In case of problems when acessing or reading the file.
	 */
	public static Board readFromFile(File inputFile) throws IOException
	{
		// First check the longest line in the file.
		int longestLine = Files.lines(inputFile.toPath()).mapToInt(String::length).max().getAsInt();
		// Also get the number of lines.
		int numberOfLines = (int) Files.lines(inputFile.toPath()).count();

		// Create the relevant square board.
		Piece squares[][] = new Piece[longestLine][numberOfLines];

		// TODO add something to decide which class.
		Board board = new RegularBoard();
		try (Stream<String> lines = Files.lines(inputFile.toPath()))
		{
			int lineNumber = 0;
			// For each line:
			for (String line : (Iterable<String>) lines::iterator)
			{
				// For each character:
				int charNumber = 0;
				for (char c : line.toCharArray())
				{
					// Turn the character to a piece.
					squares[charNumber][lineNumber] = Piece.fromText("" + c);
					charNumber++;
				}
				lineNumber++;
			}
		}

		// Change the top-left corner to the playing side.
		Piece playingSide = squares[0][0];
		squares[0][0] = OUT;

		// Set the board's attributes.
		board.dwarvesTurn = playingSide != TROLL;
		board.squares = squares;

		piecesCache = new ArrayList<>();
		for (int i = 0; i < board.getWidth(); i++)
		{
			for (int j = 0; j < board.getHeight(); j++)
			{
				if (board.squares[i][j] != OUT)
				{
					piecesCache.add(new Coordinate(i, j));
				}
			}
		}

		return board;
	}

	/**
	 Conveniency method for getting a square's piece from its coordinate.

	 @param coordinate The square's coordinates
	 @return The corresponding piece.
	 */
	public Piece getPiece(Coordinate coordinate)
	{
		return squares[coordinate.width][coordinate.height];
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final Board other = (Board) obj;
		if (!Arrays.deepEquals(this.squares, other.squares))
		{
			return false;
		}
		return this.dwarvesTurn == other.dwarvesTurn;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 73 * hash + Arrays.deepHashCode(this.squares);
		hash = 73 * hash + (this.dwarvesTurn ? 1 : 0);
		return hash;
	}
}
