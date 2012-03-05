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
import java.util.Arrays;

public class BlastTreedistPruned {
    public static void main(String[] args) {
	assert (args[0].equals("--auto"));

	File reftreeFile = new File(args[1]);
	File prunesplitfile = new File(args[2]);
	final String autoprefix;
	if (args.length > 2) {
	    autoprefix = args[3];
	} else {
	    autoprefix = "";
	}

	LN reftree;
	{
	    TreeParser tpreftree = new TreeParser(reftreeFile);
	    reftree = tpreftree.parse();
	}

	double reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

	String[] prunesplit = PrunedTreeAnalysis.readSplit(prunesplitfile);

	LN reftreepruned;
	LN[] oip_reftree;
	{
	    LN tmp = LN.deepClone(reftree);

	    LN[] psbranch = LN.findBranchBySplit(tmp, prunesplit);
	    LN removenode = psbranch[1];

	    // remove the complete subtree ( = remove node psbranch[1] )
	    oip_reftree = LN.removeNode(removenode.next.back,
		    removenode.next.next.back);

	    // use a node as pseudo root that will still be part of the tree
	    // after the remove (= one from the newly created branch).
	    reftreepruned = oip_reftree[0];

	    // System.out.printf( "%s\n", reftreepruned == null );
	}

	File cwd = new File(".");

	String[] files = cwd.list(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		return name.startsWith(autoprefix);
	    }
	});

	Arrays.sort(files);

	for (String blastFile : files) {
	    BlastFile bf = new BlastFile(new File(blastFile));

	    String queryName = bf.getQueryName();
	    if (queryName == null) {
		throw new RuntimeException("BlastFile has not query seq");
	    }

	    // use branch next to the tip as insertion position
	    String bestname = bf.getSeqNames().get(0);
	    LN[] ipb = LN.findBranchByTip(reftreepruned, bestname);

	    int[] fuck = { 0, 0 };
	    double lenOT = ClassifierLTree.getPathLenBranchToBranch(
		    oip_reftree, ipb, 0.5, fuck);
	    int ndOT = fuck[0];

	    // double distUW = getPathLenTipToTip( reftree, queryName, name,
	    // true );
	    // double dist = getPathLenTipToTip( reftree, queryName, name, false
	    // );

	    // ugly: extract seq and gap from the blast file name
	    int idx0thUnderscore = blastFile.indexOf('_');
	    int idx1stUnderscore = blastFile.indexOf('_', idx0thUnderscore + 1);
	    int idx2ndUnderscore = blastFile.indexOf('_', idx1stUnderscore + 1);

	    String seq = blastFile.substring(idx1stUnderscore + 1,
		    idx2ndUnderscore);
	    String gap = blastFile.substring(idx2ndUnderscore + 1);

	    System.out.printf("%s\t%s\t%s\t%s\t%d\t%f\t%f\n", seq, gap,
		    queryName, bestname, (int) ndOT, lenOT, lenOT
			    / reftreeDiameter);

	}
    }
}
