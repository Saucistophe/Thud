package org.saucistophe.thud.model.players;

import org.saucistophe.thud.model.boards.Board;

public abstract class Player
{
	public Player(Board board)
	{
		this.board = board;
	}

	public Board board;

	public abstract void makeBestMove();

	/**
	 Evaluates the current state of the board, in regard to which side is
	 playing. Useful for the Min-max algorithm, this function basically checks
	 how much the player is in a kickarse position, with simple criterions.

	 @param evaluatedBoard The board to evaluate.
	 @return An arbitrary quality for the current game.
	 */
	public abstract int evaluate(Board evaluatedBoard);
}
