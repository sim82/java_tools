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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ml.FindMinSupport.SubseqPos;

public class FindSplit {
    private static final boolean CREATE_REDUCED_ALIGNMENTS = true;
    private static final boolean CREATE_SUBSEQ_ALIGNMENTS = true;
    private static final boolean CREATE_PRUNED_TREES = true;

    public static void main(String[] args) {
	final int set = 1604;

	final File treefile;
	final File mafile;
	if (false) {
	    treefile = new File(args[0]);
	    mafile = new File(args[1]);
	} else {
	    treefile = new File("/space/raxml/VINCENT/RAxML_bipartitions."
		    + set + ".BEST.WITH");
	    mafile = new File("/space/raxml/VINCENT/DATA/" + set);
	}

	LN tree;
	{
	    TreeParser tp = new TreeParser(treefile);
	    tree = tp.parse();
	}

	LN[][] blist = LN.getBranchList(tree);

	System.out.printf("%d branches\n", blist.length);

	int bestSmallSize = -1;
	String[] bestSplit = null;

	double bestSupport = -1;

	for (LN[] branch : blist) {
	    LN p = branch[0];
	    LN q = branch[1];
	    // System.out.printf( "------------------------\n" );
	    if (p.back != q || q.back != p) {
		throw new RuntimeException("inconsistent branch");
	    }

	    if (p.backSupport != q.backSupport) {
		throw new RuntimeException("inconsitency");
	    }

	    String[] split = LN.getSmallerSplitSet(branch);
	    System.out.printf("split: %d %s   ", split.length, p.backLabel);

	    // if( split.length <= 20 ) {
	    System.out.printf("%f %s\n", p.backSupport, join(split, " "));
	    // }
	    if (p.backSupport < 95.0) {
		continue;
	    }
	    if (split.length > bestSmallSize) {
		// System.out.printf( "new best: %d\n", split.length );
		bestSmallSize = split.length;
		bestSplit = split;
		bestSupport = p.backSupport;
	    }

	}

	System.out.printf("best: %d %f\n", bestSplit.length, bestSupport);
	System.out.printf("set: (%s)\n", join(bestSplit, " "));

	final int alignmentLength;
	try {
	    MultipleAlignment ma = MultipleAlignment.loadPhylip(GZStreamAdaptor
		    .open(mafile));

	    alignmentLength = ma.seqLen;
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException("bailing out");
	}

	for (String taxon : bestSplit) {
	    LN[] newbranch = LN.removeTaxon(tree, taxon, false);
	    if (taxon.equals("M00Clon8")) {
		System.out.printf("here!!!!!!!!!!!!!!!!!!\n");

	    }
	    if (!newbranch[0].data.isTip) {
		tree = newbranch[0];
	    } else if (!newbranch[1].data.isTip) {
		tree = newbranch[1];
	    } else {
		throw new RuntimeException(
			"branch returned by removeTaxon only contains tips");
	    }
	}

	final String alignName = "" + set;
	// final File alignmentdir = new File( "/space/raxml/VINCENT/DATA");
	final File degen_alignoutdir = new File(
		"/space/pruned_subtree/alignments");
	final File subseq_alignoutdir = new File(
		"/space/pruned_subtree/ssalignments");
	final File subseq_queryoutdir = new File("/space/pruned_subtree/query");
	final File split_outdir = new File("/space/pruned_subtree/splits");
	final File blast_outdir = new File("/space/pruned_subtree/blast");
	final int i = 0;
	final String paddi = FindMinSupport.padchar("" + 0, 0, 4);

	if (CREATE_PRUNED_TREES) {
	    File outfile = new File("/space/pruned_subtree/trees/", "" + set
		    + "_" + paddi);
	    PrintStream ps;
	    try {
		ps = new PrintStream(new BufferedOutputStream(
			new FileOutputStream(outfile)));
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException("bailing out");
	    }
	    TreePrinter.printRaw(tree, ps);
	    ps.close();
	}

	if (true) {
	    File outfile = new File(split_outdir, "" + set + "_" + paddi);
	    PrintStream ps;
	    try {
		ps = new PrintStream(new FileOutputStream(outfile));
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException("bailing out");
	    }

	    for (String taxon : bestSplit) {
		ps.println(taxon);
	    }

	    ps.close();
	}

	if (CREATE_REDUCED_ALIGNMENTS) {

	    for (int j = 10; j <= 100; j += 10) {
		final int len = (int) Math.ceil(alignmentLength * (j / 100.0));

		String subsuffix = ".gz";

		final boolean valid = createDegeneratedAlignment(mafile,
			new File(degen_alignoutdir, alignName + "_"
				+ FindMinSupport.padchar("" + i, 0, 4) + "_"
				+ j + subsuffix), bestSplit, len);

		if (!valid) {
		    break;
		}

		// System.out.printf( "%d ", j );

	    }
	}

	if (CREATE_SUBSEQ_ALIGNMENTS) {
	    // final int[] ssLenList = {250, 500};

	    final int[] ssLenList = { 500 };

	    for (final int ssLen : ssLenList) {
		SubseqPos[] sspList = { SubseqPos.START, SubseqPos.MID,
			SubseqPos.END };

		for (SubseqPos sp : sspList) {

		    String subsuffix = ".gz";
		    String posid = FindMinSupport.subseqIdent(sp);
		    File alignOutfile = new File(subseq_alignoutdir, alignName
			    + "_" + FindMinSupport.padchar("" + i, 0, 4) + "_"
			    + ssLen + posid + subsuffix);

		    File querybase = new File(subseq_queryoutdir, "" + set
			    + "_" + FindMinSupport.padchar("" + i, 0, 4) + "_");
		    String querysuffix = "_" + ssLen
			    + FindMinSupport.subseqIdent(sp);

		    createSubseqAlignment(mafile, alignOutfile,
			    querybase.getPath(), querysuffix, bestSplit, ssLen,
			    sp);
		}
	    }
	}

	if (true) {

	    try {
		MultipleAlignment ma = MultipleAlignment
			.loadPhylip(GZStreamAdaptor.open(mafile));
		PrintWriter faw = new PrintWriter(new FileWriter(new File(
			blast_outdir, "" + set + "_" + paddi)));

		Set<String> bss = new HashSet<String>(Arrays.asList(bestSplit));

		for (String name : ma.names) {
		    if (!bss.contains(name)) {

			faw.printf("> %s\n", name);
			faw.printf("%s\n",
				FindMinSupport.removeGaps(ma.getSequence(name)));
		    }

		}

		faw.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException("bailing out");
	    }

	}

    }

    private static boolean createDegeneratedAlignment(File infile,
	    File outfile, String[] tlist, int len) {

	final MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);

	boolean valid = false;
	for (String taxon : tlist) {
	    final String seq = ma.getSequence(taxon);

	    final String degseq = FindMinSupport.createDegeneratedSequence(seq,
		    len);
	    if (degseq != null) {
		valid = true;
		ma.replaceSequence(taxon, degseq);
	    }
	}
	ma.writePhylip(outfile);
	return valid;
    }

    static String join(String[] list, String sep) {
	String out = "";

	for (int i = 0; i < list.length; i++) {
	    out += list[i];

	    if (i != list.length - 1) {
		out += sep;
	    }
	}

	return out;
    }

    private static void createSubseqAlignment(File infile, File outfile,
	    String queryBase, String querySuffix, String[] tlist, int length,
	    SubseqPos pos) {

	final MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);

	for (int i = 0; i < tlist.length; i++) {
	    String taxon = tlist[i];

	    final String seq = ma.getSequence(taxon);

	    final String degseq;

	    switch (pos) {
	    case LEAST_GAPPY_RANDOM:
		degseq = FindMinSupport.createLeastGappySubseq(seq, length);
		break;

	    case START:
		degseq = FindMinSupport.createStartSubseq(seq, length);
		break;

	    case MID:
		degseq = FindMinSupport.createMidSubseq(seq, length);
		break;

	    case END:
		degseq = FindMinSupport.createEndSubseq(seq, length);
		break;

	    default:
		throw new RuntimeException("unhandled case");
	    }

	    ma.replaceSequence(taxon, degseq);

	    PrintWriter pw;
	    try {
		pw = new PrintWriter(queryBase
			+ FindMinSupport.padchar("" + i, '0', 4) + querySuffix);
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException("bailing out");
	    }
	    pw.printf("> %s\n", taxon);
	    pw.printf("%s\n", FindMinSupport.removeGaps(degseq));
	    pw.close();

	}

	ma.writePhylip(outfile);

    }

}
