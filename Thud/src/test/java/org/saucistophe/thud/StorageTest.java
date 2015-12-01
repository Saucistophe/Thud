package org.saucistophe.thud;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.saucistophe.thud.model.boards.Board;

/**

*/
public class StorageTest {

	@Test
	public void storeTest() throws IOException
	{
		Board board = Board.readFromFile(new File("initialPosition.thud"));
		File outFile = board.writeQuick();
		Board.readFromFile(outFile);
	}
}
