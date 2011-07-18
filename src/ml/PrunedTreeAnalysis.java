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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PrunedTreeAnalysis {
    public static void main(String[] args) {
	final File reftreefile = new File(args[0]);
	final File prunesplitfile = new File(args[1]);

	final File oltfile;
	final File classfile;

	String runName = args[2];
	oltfile = new File("RAxML_originalLabelledTree." + runName);
	classfile = new File("RAxML_classification." + runName);

	LN reftree = LN.parseTree(reftreefile);
	LN olt = LN.parseTree(oltfile);
	ClassifierOutput co = ClassifierOutput.read(classfile);

	String[] prunesplit = readSplit(prunesplitfile);

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

	// // original insertion position on the olt
	LN[] oip = LN.findCorrespondingBranch(oip_reftree, olt);

	String oip_branchname = oip[0].backLabel;
	// System.out.printf( "oip in olp: %s\n", oip[0].backLabel );

	for (ClassifierOutput.Res cr : co.reslist) {

	    // classifier insert position
	    LN[] cip = LN.findBranchByName(olt, cr.branch);
	    LN[] cip_reftree = LN.findCorrespondingBranch(cip, reftreepruned);

	    int[] fuck = new int[1]; // some things (like 'multiple return
				     // values') are soo painful in java ...
	    double lenOT = ClassifierLTree.getPathLenBranchToBranch(
		    oip_reftree, cip_reftree, 0.5, fuck);
	    double ndOT = fuck[0];

	    String cip_branchname = cip[0].backLabel;

	    System.out.printf("%s\t%s\t%s\t%f\t%f\n", cr.seq, oip_branchname,
		    cip_branchname, ndOT, lenOT);
	}

    }

    public static String[] readSplit(File prunesplitfile) {
	ArrayList<String> l;

	try {
	    l = new ArrayList<String>();

	    BufferedReader r = new BufferedReader(
		    new FileReader(prunesplitfile));

	    String line;
	    while ((line = r.readLine()) != null) {
		l.add(line);
	    }
	} catch (IOException x) {
	    x.printStackTrace();
	    throw new RuntimeException("bailing out");
	}

	return l.toArray(new String[l.size()]);
    }
}
