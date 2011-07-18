package ml;

import java.io.File;


public class TreeRewrite {

	public static void main(String[] args) {
		LN n = TreeParser.parse(new File(args[0]));
		
		TreePrinter.printRaw(n, System.out);
	}
}
