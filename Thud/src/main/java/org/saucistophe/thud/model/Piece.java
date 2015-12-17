package org.saucistophe.thud.model;

public enum Piece
{
	OUT("░"), EMPTY(" "), DWARF("D"), TROLL("T"), ROCK("X");

	public String text;

	private Piece(String text)
	{
		this.text = text;
	}

	public static Piece fromText(String text)
	{
		for (Piece candidateValue : values())
		{
			if (candidateValue.text.equals(text))
			{
				return candidateValue;
			}
		}
		System.err.println("Unknown piece type " + text);
		return null;
	}
}
