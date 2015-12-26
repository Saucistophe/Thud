package org.saucistophe.thud.model.players;

import java.util.function.Consumer;
import org.saucistophe.math.genetics.Individual;
import org.saucistophe.thud.model.boards.Board;

public abstract class Player extends Individual
{
	/**
	 A callback to display or handle the reflection progress. Can be null.
	 It must handle percentages (values 0-100).
	 */
	public Consumer<Integer> progressCallback = null;

	/**
	 @param evaluatedBoard The board to evaluate.
	 @return The board after making its move.
	 */
	public abstract Board makeBestMove(Board evaluatedBoard);

	/**
	 Evaluates the current state of the board, in regard to which side is
	 playing. Useful for the Min-max algorithm, this function basically checks
	 how much the player is in a kickarse position, with simple criterions.

	 @param evaluatedBoard The board to evaluate.
	 @return An arbitrary quality for the current game.
	 */
	public abstract int evaluate(Board evaluatedBoard);
}
