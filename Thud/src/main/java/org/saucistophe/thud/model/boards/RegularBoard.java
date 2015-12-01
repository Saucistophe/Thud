package org.saucistophe.thud.model.boards;

import java.util.ArrayList;
import java.util.List;
import org.saucistophe.thud.model.Coordinate;
import org.saucistophe.thud.model.Piece;
import static org.saucistophe.thud.model.Piece.DWARF;
import static org.saucistophe.thud.model.Piece.EMPTY;
import static org.saucistophe.thud.model.Piece.TROLL;

/**

 */
public class RegularBoard extends Board
{
	@Override
	public List<Coordinate> validMoves(int x, int y, List<Coordinate> trollShovings)
	{
		// TODO: Subclass the board to handle KVT here.
		List<Coordinate> result = new ArrayList<>();

		// If the piece is a dwarf.
		if (dwarvesTurn && squares[x][y] == DWARF)
		{
			// For each possible direction:
			for (Coordinate direction : Coordinate.directions)
			{
				// While the ground is empty, we add the square and check further.
				boolean blocked = false;
				boolean blockedByTroll = false;
				int distanceToTroll = 0, candidateX = 0, candidateY = 0;
				for (int distance = 1; !blocked; distance++)
				{
					candidateX = x + distance * direction.width;
					candidateY = y + distance * direction.height;
					// Check if the destination is inside the bounds of the game, and free.
					if (isInsideBounds(candidateX, candidateY))
					{
						Piece candidateDestination = squares[candidateX][candidateY];
						if (candidateDestination == EMPTY)
						{
							result.add(new Coordinate(candidateX, candidateY));
						}
						else
						{
							blocked = true;
							// Take note if a troll is blocking the way.
							blockedByTroll = candidateDestination == TROLL;
							distanceToTroll = distance;
						}
					}
					else
					{
						blocked = true;
					}
				}

				// If no troll blocks the way, we're done.
				if (!blockedByTroll)
				{
					continue;
				}

				// If the next square is taken by a troll, check if it can be hit by hurling.
				// I.e., if there is enough drwaves behind the moving dwarf to land on the troll.
				int dwarvesInARow = 0;
				int x2 = x;
				int y2 = y;
				do
				{
					x2 -= direction.width;
					y2 -= direction.height;
					dwarvesInARow++;
				} while (dwarvesInARow < distanceToTroll && isInsideBounds(x2, y2) && squares[x2][y2] == DWARF);

				// If there are enough dwarves, bingo, add the troll's location to the possible moves.
				if (dwarvesInARow >= distanceToTroll)
				{
					result.add(new Coordinate(candidateX, candidateY));
				}
			}
		}

		// If the piece is a troll:
		if (!dwarvesTurn && squares[x][y] == TROLL)
		{
			// For each possible direction:
			for (Coordinate direction : Coordinate.directions)
			{
				// First, the troll can move to any adjacent and empty square.
				// While the ground is empty, we add the square and check
				// further.
				int newX = x + direction.width;
				int newY = y + direction.height;
				if (isInsideBounds(newX, newY) && squares[newX][newY] == EMPTY)
				{
					result.add(new Coordinate(newX, newY));
					// Find out if it's a shove, even at distance of 1:
					int oppositeSquareX = x - direction.width;
					int oppositeSquareY = y - direction.height;
					if (trollShovings != null && isInsideBounds(oppositeSquareX, oppositeSquareY) && squares[oppositeSquareX][oppositeSquareY] == TROLL)
					{
						trollShovings.add(new Coordinate(newX, newY));
					}
				}
				else
				{
					// If not, the troll is blocked, he won't go further.
					continue;
				}

				boolean blocked = false;
				// Then, check for each distance ahead, if the corresponding place is empty, and can be reached by shoving.
				for (int distance = 1; !blocked; distance++)
				{
					int candidateX = x + (distance + 1) * direction.width;
					int candidateY = y + (distance + 1) * direction.height;
					int shovingTrollX = x - distance * direction.width;
					int shovingTrollY = y - distance * direction.height;

					// Check if nothing blocks the view, and there's a backing troll.
					blocked = !isInsideBounds(candidateX, candidateY)
						|| squares[candidateX][candidateY] != EMPTY
						|| !isInsideBounds(shovingTrollX, shovingTrollY)
						|| squares[shovingTrollX][shovingTrollY] != TROLL;

					if (blocked)
					{
						break;
					}

					// If there is a clear view to the destination, and it's near dwarves, it's a go.
					if (!blocked && isNearADwarf(candidateX, candidateY))
					{
						Coordinate shovingMove = new Coordinate(candidateX, candidateY);
						result.add(shovingMove);
						// Mark the move as a troll shoving, if requested.
						if (trollShovings != null)
						{
							trollShovings.add(shovingMove);
						}
					}
				}
			}
		}
		return result;
	}
}
