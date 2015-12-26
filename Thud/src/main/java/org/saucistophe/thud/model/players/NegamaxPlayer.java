package org.saucistophe.thud.model.players;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.saucistophe.annotations.SettingsField;
import org.saucistophe.math.genetics.BasicNaturalSelection;
import static org.saucistophe.math.genetics.BasicNaturalSelection.MIXING_FACTOR;
import org.saucistophe.math.genetics.Individual;
import org.saucistophe.stats.ImprovedRandom;
import static org.saucistophe.thud.model.Piece.DWARF;
import static org.saucistophe.thud.model.Piece.TROLL;
import org.saucistophe.thud.model.boards.Board;
import static org.saucistophe.thud.model.boards.Board.INFINITY;

/**
 A Negamax implementation of the Minimax algorithm.
 */
public class NegamaxPlayer extends Player
{
	public static ImprovedRandom random = new ImprovedRandom();

	@SettingsField(category = "AI", name = "Processing depth", minValue = 1, maxValue = 6)
	public static int MAX_DEPTH = 3;

	// Calibration values.
	int dwarfMaterialRatio = 13;
	int trollMaterialRatio = 15;
	int dwarfClusteringRatio = 0;
	int trollClusteringRatio = 2;
	int dwarfMobilityRatio = 0;
	int trollMobilityRatio = 1;
	int absoluteVictoryBonus = 172;

	private static Board initialTestBoard = null;

	static
	{
		try
		{
			initialTestBoard = Board.readFromStream(NegamaxPlayer.class.getClassLoader().getResourceAsStream("micro.thud"));
		} catch (IOException ex)
		{
			Logger.getLogger(NegamaxPlayer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 Calibrates the negamax player, using basic natural selection algorithms.
	 */
	public static void calibrate()
	{
		BasicNaturalSelection geneticAlgorithm = new BasicNaturalSelection();
		geneticAlgorithm.individuals = new ArrayList<>();

		for (int i = 0; i < MIXING_FACTOR * (MIXING_FACTOR + 2); i++)
		{
			NegamaxPlayer player = new NegamaxPlayer();
			player.randomize();
			geneticAlgorithm.individuals.add(player);
		}

		for (int i = 0; i < 1000; i++)
		{
			System.out.println("Iteration " + i);
			geneticAlgorithm.iterate();
		}
	}

	// TODO depends of the board type.
	@Override
	public int evaluate(Board evaluatedBoard)
	{
		int result;

		int numberOfDwarves = evaluatedBoard.numberOf(DWARF);
		int numberOfTrolls = evaluatedBoard.numberOf(TROLL);

		// Pure material advantage
		int materialAdvantage = dwarfMaterialRatio * numberOfDwarves - trollMaterialRatio * numberOfTrolls;
		result = materialAdvantage;

		// Clustering values;
		int dwarfClusteringValue = evaluatedBoard.getPiecesStream(DWARF).mapToInt(dwarf -> evaluatedBoard.getNearby(DWARF, dwarf.width, dwarf.height).size()).sum();
		int trollClusteringValue = evaluatedBoard.getPiecesStream(TROLL).mapToInt(troll -> evaluatedBoard.getNearby(TROLL, troll.width, troll.height).size()).sum();
		result += dwarfClusteringRatio * dwarfClusteringValue - trollClusteringRatio * trollClusteringValue;

		// Mobility
		int dwarfMobilityValue = 0, trollMobilityValue = 0;

		evaluatedBoard.getPiecesStream(DWARF)
			.map(pieceToMove -> evaluatedBoard.validMoves(pieceToMove.width, pieceToMove.height, null))
			.mapToInt(List::size)
			.sum();

		trollMobilityValue = evaluatedBoard.getPiecesStream(TROLL)
			.map(pieceToMove -> evaluatedBoard.validMoves(pieceToMove.width, pieceToMove.height, null))
			.mapToInt(List::size)
			.sum();

		result += dwarfMobilityRatio * dwarfMobilityValue - trollMobilityRatio * trollMobilityValue;

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
	public Board makeBestMove(Board evaluatedBoard)
	{
		Board bestMove = evaluatedBoard.cloneBoard();
		negaMax(evaluatedBoard, bestMove, -INFINITY, INFINITY, 0);

		// Copy its data, and switch turns.
		evaluatedBoard.squares = bestMove.squares;
		evaluatedBoard.dwarvesTurn = bestMove.dwarvesTurn;

		return bestMove;
	}

	/**
	 Alpha-Beta Negamax algorithm. Go check on wikipedia!

	 @param evaluatedBoard The board currently evaluated.
	 @param resultBoard (Optional) The board that will store the result of the best move.
	 @param alpha Alpha parameter, to cut some leaves on the multiverse tree.
	 @param beta Alpha parameter, to cut even more leaves on the multiverse tree.
	 @param depth The current depth of the reflexion.

	 @return The best you can do with the worst the other player can do with the best you can do with...
	 */
	public int negaMax(Board evaluatedBoard, Board resultBoard, int alpha, int beta, int depth)
	{
		// If it's a leaf, evaluate.
		if (depth == MAX_DEPTH || evaluatedBoard.numberOf(DWARF) == 0 || evaluatedBoard.numberOf(TROLL) == 0)
		{
			return evaluate(evaluatedBoard);
		}

		List<Board> childrenBoards = evaluatedBoard.childrenBoards();
		/* Map<Board, Integer> boardValues = new HashMap<>();
		 for (Board childrenBoard : childrenBoards)
		 {
		 boardValues.put(childrenBoard, evaluate(childrenBoard));
		 }

		 // Sort those boards with the evaluation method, to evaluate the most interesting moves.
		 Collections.sort(childrenBoards, (board1, board2) -> Integer.compare(boardValues.get(board2), boardValues.get(board1))); */

		// Look for the best value on children boards.
		int boardIndex;
		int bestValue = -INFINITY;
		for (boardIndex = 0; boardIndex < childrenBoards.size(); boardIndex++)
		{
			// Update the progress callback, if any.
			if (depth == 0 && progressCallback != null)
			{
				progressCallback.accept(100 * boardIndex / childrenBoards.size());
			}

			Board childBoard = childrenBoards.get(boardIndex);
			// Get the score of the child. The negamax, a specific implementation of the Minimax, requires switching and inverting values here.
			int score = -negaMax(childBoard, null, -beta, -alpha, depth + 1);

			if (score > bestValue)
			{
				bestValue = score;
				// If we're at depth zero, keep track of the best board.
				if (depth == 0)
				{
					if (resultBoard != null)
					{
						resultBoard.set(childBoard);
					}
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

	@Override
	public void randomize()
	{
		dwarfMaterialRatio = 10 + random.nextInt(10);
		trollMaterialRatio = 10 + random.nextInt(10);
		dwarfClusteringRatio = 0 + random.nextInt(6);
		trollClusteringRatio = 0 + random.nextInt(6);
		dwarfMobilityRatio = -2 + random.nextInt(8);
		trollMobilityRatio = -2 + random.nextInt(8);
		absoluteVictoryBonus = 80 + random.nextInt(100);
	}

	@Override
	public Individual makeChild(Individual otherIndividual)
	{
		NegamaxPlayer otherNegamaxPlayer = (NegamaxPlayer) otherIndividual;
		NegamaxPlayer child = new NegamaxPlayer();
		child.dwarfMaterialRatio = (dwarfMaterialRatio + otherNegamaxPlayer.dwarfMaterialRatio) / 2;
		child.trollMaterialRatio = (trollMaterialRatio + otherNegamaxPlayer.trollMaterialRatio) / 2;
		child.dwarfClusteringRatio = (dwarfClusteringRatio + otherNegamaxPlayer.dwarfClusteringRatio) / 2;
		child.trollClusteringRatio = (trollClusteringRatio + otherNegamaxPlayer.trollClusteringRatio) / 2;
		child.dwarfMobilityRatio = (dwarfMobilityRatio + otherNegamaxPlayer.dwarfMobilityRatio) / 2;
		child.trollMobilityRatio = (trollMobilityRatio + otherNegamaxPlayer.trollMobilityRatio) / 2;
		child.absoluteVictoryBonus = (absoluteVictoryBonus + otherNegamaxPlayer.absoluteVictoryBonus) / 2;

		return child;
	}

	@Override
	public void fight(Individual otherIndividual)
	{
		NegamaxPlayer otherNegamaxPlayer = (NegamaxPlayer) otherIndividual;

		// Get the test ground.
		Board testBoard = initialTestBoard.cloneBoard();

		// Fight until a side wins, or a max number of turns has elapsed.
		boolean myTurn = true;
		int iteration = 0;
		while (testBoard.numberOf(DWARF) > 0 && testBoard.numberOf(TROLL) > 0 && iteration++ < 1000)
		{
			if (myTurn)
			{
				makeBestMove(testBoard);
			}
			else
			{
				otherNegamaxPlayer.makeBestMove(testBoard);
			}
			myTurn = !myTurn;
		}

		// Update the fitnesses of the players.
		// Since the dwarves play first, the first player, me, scores the dzqrf side.
		int score = testBoard.numberOf(DWARF) - 4 * testBoard.numberOf(TROLL);
		fitness += score;
		otherNegamaxPlayer.fitness -= score;
	}

	@Override
	public String toString()
	{
		return dwarfMaterialRatio + ", "
			+ trollMaterialRatio + ", "
			+ dwarfClusteringRatio + ", "
			+ trollClusteringRatio + ", "
			+ dwarfMobilityRatio + ", "
			+ trollMobilityRatio + ", "
			+ absoluteVictoryBonus + ", (" + fitness + ")";
	}
}
