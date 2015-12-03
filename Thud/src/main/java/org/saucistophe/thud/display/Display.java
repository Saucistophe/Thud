package org.saucistophe.thud.display;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_SPACE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.saucistophe.settings.SettingsHandler;
import org.saucistophe.thud.model.Coordinate;
import org.saucistophe.thud.model.Piece;
import static org.saucistophe.thud.model.Piece.DWARF;
import static org.saucistophe.thud.model.Piece.EMPTY;
import org.saucistophe.thud.model.boards.Board;
import org.saucistophe.thud.model.players.NegamaxPlayer;
import org.saucistophe.thud.model.players.Player;

/**
 A self-contained display panel, that syncs to a board and allows to control it.
 */
public class Display
{
	/**
	 The main display panel.
	 */
	private JPanel panel;

	/**
	 The model representation of the board.
	 */
	private Board board;

	private static Board lastLoadedBoard;

	/**
	 The label of each represented square.
	 */
	private PieceLabel squareLabels[][];

	/**
	 The currently selected piece's coordinate, if any.
	 */
	public PieceLabel selected;

	/**
	 The hovered square, if any.
	 */
	public PieceLabel hovered;

	private List<Coordinate> potentialVictims = null;
	private Coordinate potentialKiller = null;
	private Coordinate potentialCrimeScene = null;

	public static void main(String[] args)
	{
		// First init the settings.
		SettingsHandler.readFromFile();

		Board board = null;
		try
		{
			board = readFromFile(new File("initialPosition.thud"));

		} catch (IOException ex)
		{
			JOptionPane.showMessageDialog(null,
				"Please put an \"initialPosition.thud\" file next to the executable.",
				"Missing starting board file",
				JOptionPane.ERROR_MESSAGE);
		}
		Display display = new Display(board);
	}

	/**
	 Constructor.

	 @param board The board to display and control in this display.
	 */
	public Display(Board board)
	{
		this.board = board;
		createAndShowGui();
	}

	/**
	 GUI Setup.
	 */
	private void createAndShowGui()
	{
		// Create a blank grid.
		JFrame frame = new JFrame();
		panel = new JPanel();

		// The panel will react to the spacebar.
		panel.setFocusable(true);
		panel.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent ke)
			{
				super.keyTyped(ke);
				// Playing a move with the AI.
				if (ke.getKeyCode() == VK_SPACE)
				{
					Player player = new NegamaxPlayer(board);
					player.makeBestMove();

					// Clean all selections.
					potentialKiller = null;
					potentialVictims = null;
					potentialCrimeScene = null;
					selected = null;

					// Update the display.
					update();
				}
			}
		});

		setupSquares();
		update();

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		{
			JMenuItem settingsItem = new JMenuItem("New Game");
			settingsItem.addActionListener(e
				->
				{
					board.set(lastLoadedBoard);
					update();
			});
			fileMenu.add(settingsItem);
		}
		menuBar.add(fileMenu);

		JMenu toolsMenu = new JMenu("Options");
		{
			JMenuItem settingsItem = new JMenuItem("Settings");
			settingsItem.addActionListener(e
				->
				{
					SettingsHandler.showSettingsDialog();
			});
			toolsMenu.add(settingsItem);
		}
		menuBar.add(toolsMenu);

		frame.setJMenuBar(menuBar);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Refresh the display.
		update();
	}

	/**
	 Refreshes the display (Changes the text in the boxes and the boxes colors.)
	 */
	public void update()
	{
		updatePossibleMoves(selected, hovered);

		// Refresh labels
		for (int i = 0; i < board.getWidth(); i++)
		{
			for (int j = 0; j < board.getHeight(); j++)
			{
				squareLabels[i][j].refresh(board, this);
			}
		}

		// Write the playing side on the top left corner.
		squareLabels[0][0].setText(board.dwarvesTurn ? "D" : "T");
	}

	/**
	 Updates the board to highlight possible moves from the first possible movable piece.

	 @param candidateOrigins The parts of the board that are susceptible to move, sorted by order of priority.
	 */
	public void updatePossibleMoves(PieceLabel... candidateOrigins)
	{
		PieceLabel origin = null;

		// Find the first non-null given origin.
		for (PieceLabel candidateOrigin : candidateOrigins)
		{
			if (candidateOrigin != null)
			{
				origin = candidateOrigin;
				break;
			}
		}

		List<Coordinate> validMoves = origin == null
			? new ArrayList<>()
			: board.validMoves(origin.x, origin.y, null);

		// Set the relevant flags in the label.
		for (int i = 0; i < board.getWidth(); i++)
		{
			for (int j = 0; j < board.getHeight(); j++)
			{
				squareLabels[i][j].possibleMove = validMoves.contains(new Coordinate(i, j));
				squareLabels[i][j].possibleVictim = potentialVictims != null && potentialVictims.contains(new Coordinate(i, j));
			}
		}
	}

	private void setupSquares()
	{
		panel.setLayout(new GridLayout(board.getHeight(), board.getWidth()));

		squareLabels = new PieceLabel[board.getWidth()][board.getHeight()];
		// Here, we fill the board with labels.
		for (int j = 0; j < board.getHeight(); j++)
		{
			for (int i = 0; i < board.getWidth(); i++)
			{
				squareLabels[i][j] = new PieceLabel(i, j);
				squareLabels[i][j].addMouseListener(new MyMouseListener(i, j));
				squareLabels[i][j].refresh(board, this);
				panel.add(squareLabels[i][j]);
			}
		}
	}

	/**
	 Custom listener that highlights possible moves and handles
	 selection/deselection.
	 */
	private class MyMouseListener extends MouseAdapter
	{
		private final int x;
		private final int y;

		public MyMouseListener(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public void mouseEntered(MouseEvent me)
		{
			super.mouseMoved(me);
			hovered = (PieceLabel) me.getComponent();
			update();
		}

		@Override
		public void mouseExited(MouseEvent me)
		{
			hovered = null;
			update();
		}

		@Override
		public void mousePressed(MouseEvent arg0)
		{
			// There are two cases, depending on wether a piece has been selected:
			if (selected == null)
			{
				// Try to select the clicked coordinate.
				if (board.dwarvesTurn && board.squares[x][y] == Piece.DWARF
					|| !board.dwarvesTurn && board.squares[x][y] == Piece.TROLL)
				{
					selected = squareLabels[x][y];
				}
			}
			else // Dwarf selection state
			{
				if (!board.dwarvesTurn
					&& potentialKiller != null
					&& potentialVictims.contains(new Coordinate(x, y)))
				{
					// Efect the move, kill the victim, and deselect.
					board.move(potentialKiller.width, potentialKiller.height, potentialCrimeScene.width, potentialCrimeScene.height);
					board.squares[x][y] = EMPTY;

					potentialKiller = null;
					potentialVictims = null;
					potentialCrimeScene = null;
					selected = null;
				}
				else
				{
					List<Coordinate> trollShovings = new ArrayList<>();

					boolean wrongVictim = potentialKiller != null && !potentialVictims.contains(new Coordinate(x, y));
					boolean wrongMove = !board.validMoves(selected.x, selected.y, trollShovings).contains(new Coordinate(x, y));

					// If there was a wrong selected target among the dwarves victim, the move is invalid, deselect and start again.
					if (wrongVictim || wrongMove)
					{
						selected = null;
						potentialKiller = null;
						potentialVictims = null;
						potentialCrimeScene = null;
					}
					else // If there's already a selection, we check if the selected move is valid:
					// If it's a troll, and there is several possible dwarven victims (i.e. on a simple move among several dwarves) prompt for the dwarf to kill.
					{
						if (board.squares[selected.x][selected.y] == Piece.TROLL)
						{
							if (trollShovings.contains(new Coordinate(x, y)))
							{
								// In case of troll shoving, kill all nearbydwarves, for simplicity.
								for (Coordinate victim : board.getNearby(DWARF, x, y))
								{
									board.squares[victim.width][victim.height] = EMPTY;
								}
								board.move(selected.x, selected.y, x, y);
								selected = null;
							}
							else
							{
								// If it's a simple move, ask which dwarf to kill, if any: Switch to kill mode.
								List<Coordinate> nearbyDwarves = board.getNearby(DWARF, x, y);

								if (!nearbyDwarves.isEmpty())
								{
									potentialCrimeScene = new Coordinate(x, y);
									potentialVictims = nearbyDwarves;
									potentialKiller = new Coordinate(selected.x, selected.y);
								}
								else
								{
									// No nearby dwaves, simple move!
									board.move(selected.x, selected.y, x, y);
									selected = null;
								}
							}
						}
						// If it's a dwarf, effect the move.
						else
						{
							board.move(selected.x, selected.y, x, y);
							selected = null;
						}
					}
				}
			}

			// Refresh the display, whatever was clicked.
			update();
		}
	}

	/**
	Loads a board file and, if successful, saves it as the "new game" initial configuration.

	@param inputFile The board file to read.
	@return The read board.
	@throws IOException In case of unopenable file.
	*/
	public static Board readFromFile(File inputFile) throws IOException
	{
		Board result = Board.readFromFile(inputFile);
		lastLoadedBoard = result.cloneBoard();
		return result;
	}
}
