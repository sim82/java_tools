package ml;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class SplitAlignmentRandom {
    public static void main(String[] args) {
	MultipleAlignment ma = MultipleAlignment.loadPhylip(new File(args[0]));

	final String name1, name2;

	if (args.length >= 3) {
	    name1 = args[1];
	    name2 = args[2];
	} else {
	    name1 = "1";
	    name2 = "2";
	}

	String[] snames = ma.names.clone();
	Arrays.sort(snames);

	Random rnd = new Random(12345);

	// Fisher/Yates shuffle
	for (int n = snames.length - 1; n >= 1; n--) {
	    int j = rnd.nextInt(n);
	    String t = snames[j];
	    snames[j] = snames[n];
	    snames[n] = t;
	}

	int mid = snames.length / 2;

	ma.writeSpecial(new File(name1), snames, 0, mid);
	ma.writeSpecial(new File(name2), snames, mid, ma.nTaxon);
    }
}
