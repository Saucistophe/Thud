package org.saucistophe.thud.display;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import org.saucistophe.annotations.SettingsField;
import org.saucistophe.thud.model.Coordinate;
import org.saucistophe.thud.model.Piece;
import static org.saucistophe.thud.model.Piece.OUT;
import org.saucistophe.thud.model.boards.Board;

/**

 */
public class PieceLabel extends JLabel
{
	public int x;
	public int y;

	public boolean selected = false;
	public boolean possibleMove = false;
	public boolean possibleVictim = false;

	@SettingsField(category = "Display", name = "Font Size")
	public static int fontSize = 25;

	private static Font font = new Font("Arial", Font.BOLD, fontSize);

	// The colors used for squares, inside and outside the board
	private static final Color OUTSIDE_COLOR_1 = new Color(0x5A5A5A);
	private static final Color OUTSIDE_COLOR_2 = new Color(0x5A5A5A);
	private static final Color INSIDE_COLOR_1 = new Color(0xFFFFFF);
	private static final Color INSIDE_COLOR_2 = new Color(0xCCCCCC);
	private static final Color SELECTABLE_COLOR_1 = new Color(0xCCFFFF);
	private static final Color SELECTABLE_COLOR_2 = new Color(0xCCFFFF);
	private static final Color SELECTED_COLOR_1 = new Color(0x88FF88);
	private static final Color SELECTED_COLOR_2 = new Color(0x66CC66);

	public PieceLabel(int x, int y)
	{
		super("☹", JLabel.CENTER);

		this.x = x;
		this.y = y;

		// The topleft corner is white.
		if (x == 0 && y == 0)
		{
			setForeground(Color.white);
		}

		setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		setFont(font);
		setOpaque(true);
		setPreferredSize(new Coordinate(fontSize, fontSize));
	}

	public void refresh(Board board, Display display)
	{
		// Reset the font size.
		if (font.getSize() != fontSize)
		{
			font = new Font(font.getName(), font.getStyle(), fontSize);
		}
		setFont(font);

		boolean white = (x + y) % 2 == 0;

		// Fill in the square's text value.
		if (board.squares[x][y] != Piece.OUT)
		{
			setText(board.squares[x][y].text);
		}
		else
		{
			setText("");
		}

		// Set the background color.
		if (board.squares[x][y] == OUT)
		{
			setBackground(white ? OUTSIDE_COLOR_1 : OUTSIDE_COLOR_2);
		}
		else
		{
			setBackground(white ? INSIDE_COLOR_1 : INSIDE_COLOR_2);
		}

		// By order of precedence:
		// Selected
		if (this == display.selected)
		{
			setBackground((x + y) % 2 != 0 ? SELECTED_COLOR_1 : SELECTED_COLOR_2);
		}
		// Hovered
		else if (this == display.hovered && board.squares[x][y] != OUT)
		{
			setBackground((x + y) % 2 != 0 ? SELECTABLE_COLOR_1 : SELECTABLE_COLOR_2);
		}
		// Possible victim
		else if (possibleVictim)
		{
			setBackground(new Color(255, 255, getBackground().getBlue() * 7 / 10));
		}
		// Possible move.
		else if (possibleMove)
		{
			setBackground(new Color(255, getBackground().getBlue() * 9 / 10, getBackground().getGreen() * 9 / 10));
		}
	}
}
