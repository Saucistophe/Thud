package org.saucistophe.thud.model.players;

import java.util.List;
import static org.saucistophe.thud.model.Piece.DWARF;
import static org.saucistophe.thud.model.Piece.TROLL;
import org.saucistophe.thud.model.boards.Board;
import static org.saucistophe.thud.model.boards.Board.INFINITY;

/**
 A Negamax implementation of the Minimax algorithm.
 */
public class NegamaxPlayer extends Player
{
	public static final int MAX_DEPTH = 3;

	/**
	 Makes the best move possible.
	 */
	private Board bestMove = null;

	public NegamaxPlayer(Board board)
	{
		super(board);
	}

	// TODO depends of the board type.
	@Override
	public int evaluate(Board evaluatedBoard)
	{
		int result;

		int materialFactor = 5;
		int materialRatio = 10;
		int dwarfClusteringRatio = 2;
		int trollClusteringRatio = -1;
		int absoluteVictoryBonus = 1000;

		int numberOfTrolls = evaluatedBoard.numberOf(TROLL);
		int numberOfDwarves = evaluatedBoard.numberOf(DWARF);

		// Pure material advantage
		int materialAdvantage = materialFactor * (numberOfDwarves - materialRatio * numberOfTrolls);
		result = materialAdvantage * materialRatio;

		// Clustering values;
	/*	int dwarfClusteringValue = board.getPiecesStream(DWARF).mapToInt(dwarf -> board.getNearby(DWARF, dwarf.width, dwarf.height).size()).sum();
		int trollClusteringValue = board.getPiecesStream(TROLL).mapToInt(troll -> board.getNearby(TROLL, troll.width, troll.height).size()).sum();
		result += dwarfClusteringRatio * dwarfClusteringValue + trollClusteringRatio * trollClusteringValue;*/

		// If there's no piece left, add a big malus/bonus.
		if (numberOfTrolls == 0)
		{
			result += absoluteVictoryBonus;
		}
		else if (numberOfDwarves == 0)
		{
			result -= absoluteVictoryBonus;
		}

		return evaluatedBoard.dwarvesTurn ? result : -result;
	}

	@Override
	public void makeBestMove()
	{
		bestMove = null;
		negaMax(board, -INFINITY, INFINITY, 0);

		// Copy its data, and switch turns.
		board.squares = bestMove.squares;
		board.dwarvesTurn = bestMove.dwarvesTurn;
	}

	/**
	 Alpha-Beta Negamax algorithm. Go check on wikipedia!

	 @param evaluatedBoard The board currently evaluated.
	 @param alpha Alpha parameter, to cut some leaves on the multiverse tree.
	 @param beta Alpha parameter, to cut even more leaves on the multiverse tree.
	 @param depth The current depth of the reflexion.

	 @return The best you can do with the worst the other player can do with the best you can do with...
	 */
	public int negaMax(Board evaluatedBoard, int alpha, int beta, int depth)
	{
		// If it's a leaf, evaluate.
		if (depth == MAX_DEPTH || evaluatedBoard.numberOf(DWARF) == 0 || evaluatedBoard.numberOf(TROLL) == 0)
		{
			return evaluate(evaluatedBoard);
		}

		List<Board> childrenBoards = evaluatedBoard.childrenBoards();
		/*Map<Board, Integer> boardValues = new HashMap<>();
		for (Board childrenBoard : childrenBoards)
		{
			boardValues.put(childrenBoard, evaluate(childrenBoard));
		}

		// Sort those boards with the evaluation method, to evaluate the most interesting moves.
		Collections.sort(childrenBoards, (board1, board2) -> Integer.compare(boardValues.get(board2), boardValues.get(board1)));
		// Limit to the first ten moves.
		childrenBoards = childrenBoards.subList(0, 10);*/

		// Look for the best value on children boards.
		int bestValue = -INFINITY;
		for (Board childBoard : childrenBoards)
		{
			// Get the score of the child. The negamax, a specific implementation of the Minimax, requires switching and inverting values here.
			int score = -negaMax(childBoard, -beta, -alpha, depth + 1);

			if (score > bestValue)
			{
				bestValue = score;
				// If we're at depth zero keep track of the best board.
				if (depth == 0)
				{
					bestMove = childBoard;
				}
			}

			// Alpha-beta pruning
			alpha = Math.max(alpha, score);
			if (alpha >= beta)
			{
				break;
			}
		}

		return bestValue;
	}
}
