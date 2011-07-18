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
import java.util.SortedSet;
import java.util.TreeSet;

public class BranchToTipDist {
    public static void main(String[] args) {
	File classfile = new File("/space/newPNAS/RAxML_classification.PNAS");
	File oltfile = new File(
		"/space/newPNAS/RAxML_originalLabelledTree.PNAS");

	ClassifierOutput co = ClassifierOutput.read(classfile);

	SortedSet<String> branchSet = new TreeSet<String>();

	LN olt = LN.parseTree(oltfile);

	for (ClassifierOutput.Res res : co.reslist) {
	    branchSet.add(res.branch);

	    LN[] branch = LN.findBranchByName(olt, res.branch);
	    int d1 = ndToNearestTip(branch[0]);
	    int d2 = ndToNearestTip(branch[1]);

	    int mind = Math.min(d1, d2);
	    System.out.printf("%s %s\t%d\t%d\n", res.seq, res.branch,
		    (int) res.support, mind);
	}

    }

    public static int ndToNearestTip(LN n) {
	if (n.data.isTip) {
	    return 0;
	} else {
	    int d1 = ndToNearestTip(n.next.back);
	    int d2 = ndToNearestTip(n.next.next.back);

	    return Math.min(d1, d2) + 1;
	}
    }
}
