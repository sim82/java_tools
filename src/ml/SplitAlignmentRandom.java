package ml;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class SplitAlignmentRandom {
	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(new File(args[0]));
		
		String[] snames = ma.names.clone();
		
		Random rnd = new Random();
		
		// Fisher/Yates shuffle
		for( int n = snames.length - 1; n >= 1; n-- ) {
			int j = rnd.nextInt(n);
			String t = snames[j];
			snames[j] = snames[n];
			snames[n] = t;
		}
		
		
		
		int mid = snames.length / 2;
		
		ma.writeSpecial( new File("1"), snames, 0, mid );
		ma.writeSpecial( new File("2"), snames, mid, ma.nTaxon );
	}
}
