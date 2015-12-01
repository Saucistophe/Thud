package org.saucistophe.thud.model;

import java.awt.Dimension;

/**
 A convenient class that implements a 2D integer vector, and makes it Comparable, so
 that it can be used in a Set.
 */
public class Coordinate extends Dimension implements Comparable<Coordinate>
{
	private static final long serialVersionUID = 1L;

	public static final Coordinate[] directions =
	{
		new Coordinate(1, 0),
		new Coordinate(1, -1),
		new Coordinate(0, -1),
		new Coordinate(-1, -1),
		new Coordinate(-1, 0),
		new Coordinate(-1, 1),
		new Coordinate(0, 1),
		new Coordinate(1, 1)
	};

	public Coordinate(int i, int j)
	{
		width = i;
		height = j;
	}

	@Override
	public int compareTo(Coordinate o)
	{
		return (15 * (width - o.width) + (height - o.height));
	}
}
