package ml;

import java.io.File;

public class ReadWriteTree {
	public static void main(String[] args) {
		File treefile = new File( args[0] );
		
		LN t = TreeParser.parse(treefile);
		
		TreePrinter.printRaw(t, System.out);
		
	}
}
