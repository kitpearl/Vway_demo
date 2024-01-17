package com.cubegen.fp.loc;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LOCParserManager_Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		 LOCParserManager manager = new LOCParserManager();
		 manager.Run(
				 "E:\\MetaPlus\\Resource\\KEB\\ChangeFlow\\166\\20100819000455\\", 
				 "OAHISOG", 
				 "1.0", 
				 "TAL", 
				 "1F1299653", 
				 "BASELINE20120120", 
				 "", /*"MVS",*/
				 "", 
				 false);

	}

}
