package org.saucistophe.thud;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.saucistophe.thud.model.boards.Board;

/**

 */
public class StorageTest
{

	@Test
	public void storeTest() throws IOException
	{
		Board board = Board.readFromFile(new File(StorageTest.class.getClassLoader().getResource("initialBoard.thud").getFile()));

		// Simply drop the file, hpefully without errors.
		File outFile = board.writeQuick();
		Board.readFromFile(outFile);

		// Clean up.
		outFile.deleteOnExit();
	}
}
