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
package ml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class BlastOutTreedist {

    public static void main(String[] args) {

	boolean automode = args[0].equals("--auto");

	File reftreeFile = new File(args[1]);
	LN reftree;
	{
	    TreeParser tpreftree = new TreeParser(reftreeFile);
	    reftree = tpreftree.parse();
	}

	// highest path weight in reference tree (=path with the highest sum of
	// edge weights, no necessarily the longest path)
	double reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

	if (!automode) {

	    File blastFile = new File(args[0]);
	    BlastFile bf = new BlastFile(blastFile);

	    // parse reference tree used for weighted branch difference stuff

	    // Map<String,String[]> splitmap = ClassifierLTree.parseSplits(
	    // rnFile );

	    String queryName = bf.getQueryName();
	    if (queryName == null) {
		throw new RuntimeException("BlastFile has not query seq");
	    }

	    if (!queryName.equals(bf.getSeqNames().get(0))) {
		throw new RuntimeException(
			"ooops. query sequence is not the best blast hit. bailing out");
	    }

	    String name = bf.getSeqNames().get(1);
	    double distUW = getPathLenTipToTip(reftree, queryName, name, true);
	    double dist = getPathLenTipToTip(reftree, queryName, name, false);
	    System.out.printf("%s\t%f\t%d\t%f\t%f\n", name,
		    bf.getBitscore(name), (int) distUW, dist, dist
			    / reftreeDiameter);
	} else {
	    final String autoprefix;
	    if (args.length > 2) {
		autoprefix = args[2];
	    } else {
		autoprefix = "";
	    }

	    final String autosuffix;
	    if (args.length > 3) {
		autosuffix = args[3];
	    } else {
		autosuffix = ".fa";
	    }

	    File cwd = new File(".");

	    String[] files = cwd.list(new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
		    return name.startsWith(autoprefix)
			    && name.endsWith(autosuffix);
		}
	    });

	    Arrays.sort(files);

	    for (String blastFile : files) {
		// System.out.printf( "%s:\n", blastFile );
		BlastFile bf = new BlastFile(new File(blastFile));

		// parse reference tree used for weighted branch difference
		// stuff

		// Map<String,String[]> splitmap = ClassifierLTree.parseSplits(
		// rnFile );

		String queryName = bf.getQueryName();
		if (queryName == null) {
		    throw new RuntimeException("BlastFile has not query seq");
		}

		ArrayList<String> seqNames = bf.getSeqNames();

		int hit = -1;

		for (int i = 0; i < seqNames.size(); i++) {
		    if (!seqNames.get(i).equals(queryName)) {
			hit = i;
			break;
		    }
		}

		if (hit < 0) {
		    throw new RuntimeException(
			    "could not find first blast hit!?");
		}

		// if( !queryName.equals( bf.getSeqNames().get(0))) {
		// throw new RuntimeException(
		// "ooops. query sequence is not the best blast hit. bailing out");
		// }

		LN reftreePruned = LN.deepClone(reftree);
		LN[] opb = LN.removeTaxon(reftreePruned, queryName);

		String name = bf.getSeqNames().get(hit);
		LN[] ipb = LN.findBranchByTip(reftreePruned, name);

		int[] fuck = { 0, 0 };
		double lenOT = ClassifierLTree.getPathLenBranchToBranch(opb,
			ipb, 0.5, fuck);
		int ndOT = fuck[0];

		// double distUW = getPathLenTipToTip( reftree, queryName, name,
		// true );
		// double dist = getPathLenTipToTip( reftree, queryName, name,
		// false );

		// ugly: extract seq and gap from the blast file name
		int idx1stUnderscore = blastFile.indexOf('_');
		int idx2ndUnderscore = blastFile.indexOf('_',
			idx1stUnderscore + 1);
		int idxDot = blastFile.lastIndexOf('.');

		String seq = blastFile.substring(idx1stUnderscore + 1,
			idx2ndUnderscore);
		String gap = blastFile.substring(idx2ndUnderscore + 1, idxDot);

		System.out.printf("%s\t%s\t%s\t%s\t%d\t%f\t%f\n", seq, gap,
			queryName, name, (int) ndOT, lenOT, lenOT
				/ reftreeDiameter);

	    }

	}
    }

    private static double getPathLenTipToTip(LN tree, String startName,
	    String endName, boolean unweighted) {
	LN[] list = LN.getAsList(tree);

	LN start = null;
	LN end = null;

	for (LN n : list) {
	    if (n.data.isTip) {
		String tipName = n.data.getTipName();

		if (start == null && tipName.equals(startName)) {
		    start = n;
		}
		if (end == null && tipName.equals(endName)) {
		    end = n;
		}
	    }

	    if (start != null && end != null) {
		break;
	    }
	}

	if (start == null) {
	    throw new RuntimeException("could not find node for start tip: "
		    + startName);
	}
	if (end == null) {
	    throw new RuntimeException("could not find node for end tip: "
		    + startName);
	}

	if (start == end) {
	    return 0.0;
	}

	return getPathLenNodeToTipNoBack(start.back, end, unweighted)
		+ (unweighted ? 1.0 : start.backLen);

    }

    private static double getPathLenNodeToTipNoBack(LN start, LN end,
	    boolean unweighted) {
	if (start == end) {
	    return 0.0;
	} else if (start.data.isTip) {
	    return Double.POSITIVE_INFINITY;
	} else {
	    {
		double len = getPathLenNodeToTipNoBack(start.next.back, end,
			unweighted);
		if (len < Double.POSITIVE_INFINITY) {
		    return len + (unweighted ? 1.0 : start.next.backLen);
		}
	    }
	    {
		double len = getPathLenNodeToTipNoBack(start.next.next.back,
			end, unweighted);
		if (len < Double.POSITIVE_INFINITY) {
		    return len + (unweighted ? 1.0 : start.next.next.backLen);
		}
	    }

	    return Double.POSITIVE_INFINITY;
	}

    }
}
