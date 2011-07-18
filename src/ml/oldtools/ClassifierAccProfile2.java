/*
 * Copyright (C) 2009 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */
package ml.oldtools;

import java.io.File;
import java.util.ArrayList;

import ml.ArrayIndex;
import ml.BucketMeans;
import ml.CSVFile;
import ml.FindMinSupport;
import ml.MultipleAlignment;

public class ClassifierAccProfile2 {
    public static void main(String[] args) {
	int[] coverage = null;
	int[] cdist = null;

	final String[] ids;
	final File[] alignFiles;
	final File[] clsltFiles;
	final CSVFile rns;
	boolean lenVsDist = false;
	boolean lenVsDistBM = false;

	if (args[0].startsWith("--auto")) {
	    String aliTemp = args[1];
	    String clsltTemp = args[2];

	    ArrayList<File> alil = new ArrayList<File>();
	    ArrayList<File> clsl = new ArrayList<File>();
	    ArrayList<String> idl = new ArrayList<String>();

	    for (int i = 0;; i++) {
		String pi = FindMinSupport.padchar("" + i, 0, 4);
		File ali = new File(aliTemp.replaceAll("\\%id\\%", pi));
		File cls = new File(clsltTemp.replaceAll("\\%id\\%", pi));

		// System.out.printf( "test %s %s\n", ali.getPath(),
		// ali.canRead() );
		// System.out.printf( "test %s %s\n", cls.getPath(),
		// cls.canRead() );
		if (ali.canRead() && cls.canRead()) {
		    alil.add(ali);
		    clsl.add(cls);
		    idl.add(pi);
		} else {
		    break;
		}
	    }
	    alignFiles = alil.toArray(new File[alil.size()]);
	    clsltFiles = clsl.toArray(new File[clsl.size()]);
	    ids = idl.toArray(new String[idl.size()]);

	    if (args.length > 3) {
		File rnFile = new File(args[3]);
		rns = CSVFile.load(rnFile, "SSSS");
	    } else {
		rns = null;
	    }

	    lenVsDist = args[0].equals("--autosp");
	    lenVsDistBM = args[0].equals("--autobm");

	} else {
	    File[] t1 = { new File(args[0]) };
	    File[] t2 = { new File(args[1]) };
	    String[] t3 = { null };
	    alignFiles = t1;
	    clsltFiles = t2;
	    ids = t3;
	    rns = null;
	}

	final BucketMeans bm;
	if (lenVsDistBM) {
	    bm = new BucketMeans(40, 400, 40);
	} else {
	    bm = null;
	}

	ArrayIndex rnsIdIdx;
	String[] rnsIds;
	String[] rnsSplits;
	if (rns != null) {
	    rnsIds = rns.getString(0);
	    rnsSplits = rns.getString(3);
	    rnsIdIdx = new ArrayIndex(rnsIds);
	} else {
	    rnsIds = rnsSplits = null;
	    rnsIdIdx = null;
	}

	for (int k = 0; k < alignFiles.length; k++) {
	    // System.out.printf( "file %d\n", k );
	    File alignFile = alignFiles[k];
	    File clsltFile = clsltFiles[k];

	    // filter out all non inner-QS
	    if (ids[k] != null && rnsIdIdx != null) {
		// get split-set for current seq id (ids[k])
		String rn = rnsSplits[rnsIdIdx.getIdx(ids[k])];

		// only execute loop if there is more than one element in the
		// split list (=inner QS)
		if (rn.indexOf(',') < 0) {
		    continue;
		}
	    }

	    MultipleAlignment ma = MultipleAlignment.loadPhylip(alignFile);

	    CSVFile cls = CSVFile.load(clsltFile, "SSSIIDDD");

	    String names[] = cls.getString(0);
	    int dists[] = cls.getInteger(4);

	    for (int i = 0; i < names.length; i++) {
		String name = names[i];
		String seq = ma.getSequence(name);
		int dist = dists[i];

		if (coverage == null) {
		    coverage = new int[seq.length()];
		    cdist = new int[seq.length()];
		} else if (coverage.length != seq.length()) {
		    throw new RuntimeException(
			    "sequence in ml has wrong length");
		}

		int nChar = 0;

		for (int j = 0; j < coverage.length; j++) {
		    char c = Character.toUpperCase(seq.charAt(j));
		    if (!isGap(c)) {
			nChar++;
			coverage[j]++;
			cdist[j] += dist;
		    }
		}

		if (bm != null) {
		    bm.add(nChar, dist);
		}
		if (lenVsDist) {
		    System.out.printf("%d %d\n", nChar, dist);
		}
	    }
	}

	if (!lenVsDist && !lenVsDistBM) {

	    for (int i = 0; i < coverage.length; i++) {
		// coverage[i] = Math.max( coverage[i], 1 ); // add pseudocount
		// of 1
		if (coverage[i] > 0) {
		    System.out.printf("%d %f\n", coverage[i], (float) cdist[i]
			    / coverage[i]);
		} else {
		    System.out.printf("0 %f\n", Double.POSITIVE_INFINITY);
		}
	    }
	}

	if (bm != null) {
	    bm.print();
	}
    }

    private static boolean isGap(char c) {

	return c == '-' || c == 'N';
    }
}
