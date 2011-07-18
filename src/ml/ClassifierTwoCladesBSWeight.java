/*
 * Copyright (C) 2010 Simon A. Berger
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ClassifierTwoCladesBSWeight {
    static class Res {
	String name;
	double wgh1;
	double wgh2;
	double wghog;

	Res(String name, double wgh1, double wgh2, double wghog) {
	    this.name = name;
	    this.wgh1 = wgh1;
	    this.wgh2 = wgh2;
	    this.wghog = wghog;
	}
    }

    public static void main(String[] args) {
	File treeFile = new File(args[0]);
	File classFile = new File(args[1]);

	String outgroup = args[2];

	LN n = TreeParser.parse(treeFile);

	LN[] sp = LN.findBranchByTip(n, outgroup);

	LN t1 = sp[0].back.next;
	LN t2 = sp[0].back.next.next;

	Set<String> nl1 = getBranchLabelSet(LN.getAsList(t1.back, false));
	Set<String> nl2 = getBranchLabelSet(LN.getAsList(t2.back, false));

	Set<String> tl1 = LN.getTipSet(LN.getAsList(t1.back, false));
	Set<String> tl2 = LN.getTipSet(LN.getAsList(t2.back, false));

	int nchecked = 0;
	int nright = 0;
	for (int i = 3; i < args.length; i++) {
	    if (tl1.contains(args[i])) {
		// throw new RuntimeException( "polarity check failed" );
		nright++;
	    }
	    nchecked++;
	}
	if (nright < nchecked - 1) {
	    throw new RuntimeException("polarity check failed");
	}

	if (false) {
	    String tl1s = "";
	    String tl2s = "";
	    for (String s : tl1) {
		tl1s += " " + s;
	    }
	    for (String s : tl2) {
		tl2s += " " + s;
	    }
	    System.out.printf("set1: %s\n", tl1s);
	    System.out.printf("set2: %s\n", tl2s);
	}
	// System.out.println( "b1:");
	// print( nl1 );
	//
	// LN.getBranchList(n)

	ClassifierOutput cf = ClassifierOutput.read(classFile);

	new ArrayList<String>();
	new ArrayList<String>();
	new ArrayList<String>();

	Map<String, Res> orm = new TreeMap<String, Res>();

	for (ClassifierOutput.Res res : cf.reslist) {
	    Res or = orm.get(res.seq);

	    if (or == null) {
		or = new Res(res.seq, 0, 0, 0);
		orm.put(res.seq, or);
	    }

	    if (nl1.contains(res.branch)) {
		or.wgh1 += res.support;
	    } else if (nl2.contains(res.branch)) {
		or.wgh2 += res.support;
	    } else {
		or.wghog += res.support;
	    }
	}

	// for( Res res : orm.values() ) {
	// System.out.printf( "%s\t%f\t%f\t%f\n", res.name, res.wgh1, res.wgh2,
	// res.wghog );
	// }

	ml.ClassifierTwoCladesBSWeight.Res[] orl = orm.values().toArray(
		new Res[orm.size()]);
	Arrays.sort(orl, new Comparator<Res>() {

	    @Override
	    public int compare(ml.ClassifierTwoCladesBSWeight.Res o1,
		    ml.ClassifierTwoCladesBSWeight.Res o2) {
		// TODO Auto-generated method stub

		int w1 = (int) o1.wgh1 - (int) o1.wgh2 - (int) o1.wghog * 10;
		int w2 = (int) o2.wgh1 - (int) o2.wgh2 - (int) o2.wghog * 10;

		if (w1 < w2) {
		    return 1;
		} else if (w1 > w2) {
		    return 0;
		} else {
		    return o1.name.compareTo(o2.name);
		}

	    }
	});

	for (Res res : orl) {
	    System.out.printf("%s\t%f\t%f\t%f\n", res.name, res.wgh1, res.wgh2,
		    res.wghog);
	}
    }

    static void print(Iterable<String> nl1) {
	for (String s : nl1) {
	    System.out.println(s);
	}

    }

    static Set<String> getBranchLabelSet(LN[] bl) {
	Set<String> s = new HashSet<String>();

	for (LN n : bl) {
	    s.add(n.backLabel);
	}

	return s;
    }
}
