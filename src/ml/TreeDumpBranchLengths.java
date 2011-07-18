package ml;

import java.io.File;

public class TreeDumpBranchLengths {

    public static void main(String[] args) {
	File f = new File(args[0]);

	LN n = TreeParser.parse(f);

	LN[][] brl = LN.getAllBranchList3(n);

	double[] brlen = new double[brl.length];

	double min = Double.POSITIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;

	int i = 0;
	for (LN[] br : brl) {
	    brlen[i] = br[0].backLen;
	    min = Math.min(min, brlen[i]);
	    max = Math.max(max, brlen[i]);
	    i++;
	}
	System.out.printf("minmax: %f %f\n", min, max);
	Histogram<Integer> hist = new Histogram<Integer>();

	final int nbins = 20;

	for (double l : brlen) {
	    hist.add(Histogram.bin(l, min, max, nbins));

	}

	hist.printPoser(min, max, nbins);

    }
}
