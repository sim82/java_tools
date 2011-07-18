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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author sim
 */
public class FindMinSupport {
    static class ReductionResult {

	String taxon;
	String taxonNeighbor;

	LN nl;
	LN nr;
    }

    private static boolean CREATE_REDUCED_ALIGNMENTS = !true;
    private static boolean CREATE_SUBSEQ_ALIGNMENTS = !true;
    private static boolean CREATE_REDUCED_TREES = !true;
    private static boolean CREATE_MAPPING_FILES = true;
    private static final boolean CREATE_PAIRED_END_ALIGNMENTS = !true;
    private static final boolean CREATE_DIST_SUBSEQ_ALIGNMENTS = true;

    // try to keep the random gap generation reproducible
    public static final Random rand = new Random(12345);
    // private static final int START_TREE_IDX = 97;
    // private static final int START_TREE_IDX = 65;
    private static final int START_TREE_IDX = 0;

    public static ReductionResult createNThReducedTree(LN n, int num) {
	final LN[] nodelist = LN.getAsList(n);

	System.out.printf("nodes: %d\n", nodelist.length);

	int i = 0;

	// if( true ) {
	for (final LN node : nodelist) {
	    final int nt = numTips(node);

	    // System.out.printf( "support: %f\n", node.data.getSupport());

	    if (node.data.getSupport() < 100.0) {
		continue;
	    }

	    if (nt == 2) {
		final String[] tn = getTipNames(node);

		assert (tn.length == 2);

		// System.out.printf("%s %f (%s %s): %d\n", node.data,
		// node.data.getSupport(), tn[0], tn[1], nt);

		final LN tnt = LN.getTowardsNonTip(node);
		if (tnt.back.data.isTip) {
		    throw new RuntimeException("tnt.back.data.isTip is true");
		}

		// String ret[] = new String[2];
		ReductionResult ret = null;
		// int c = 2;
		if (i == num) {
		    ret = new ReductionResult();
		    ret.taxon = tn[1];
		    ret.taxonNeighbor = tn[0];
		    ret.nl = tnt.back;
		    ret.nr = tnt.next.back;

		    tnt.back.back = tnt.next.back;
		    tnt.next.back.back = tnt.back;

		    // ret[0] = tn[1];
		    // ret[1] = tn[0];
		    // return tn[1];
		    return ret;
		}
		i++;
		if (i == num) {
		    ret = new ReductionResult();
		    ret.taxon = tn[0];
		    ret.taxonNeighbor = tn[1];
		    ret.nl = tnt.back;
		    ret.nr = tnt.next.next.back;

		    tnt.back.back = tnt.next.next.back;
		    tnt.next.next.back.back = tnt.back;

		    // ret[0] = tn[0];
		    // ret[1] = tn[1];
		    return ret;

		    // return tn[0];
		}
		i++;
	    }

	}
	// }

	// find cases where a single tip has two neighboring brnaches with 100 %
	// support
	// this is done in an extra loop to keep the sorting order compatible
	// with the old version
	for (final LN node : nodelist) {
	    final int nt = numTips(node);

	    if (nt == 1) {
		final LN tt = LN.getTowardsTip(node);

		// if( tt.back.data.getTipName().equals("poly914")) {
		// System.out.printf( "poly914\n" );
		// }

		if (!tt.next.back.data.isTip && !tt.next.next.back.data.isTip
			&& tt.next.backSupport >= 100.0
			&& tt.next.next.backSupport >= 100.0) {
		    if (i == num) {

			// String ret[] = new String[2];

			final ReductionResult ret = new ReductionResult();
			ret.taxon = tt.back.data.getTipName();
			ret.taxonNeighbor = null;
			ret.nl = tt.next.back;
			ret.nr = tt.next.next.back;

			// remove the current node (and the branch toward the
			// tip) by retwiddling of the other two nodes
			tt.next.back.back = tt.next.next.back;
			tt.next.next.back.back = tt.next.back;

			// ret[0] = tt.back.data.getTipName();
			// ret[1] = null;

			return ret;

		    }

		    i++;
		}
	    }

	}

	//
	//
	// baaaaaad: duplicate code:
	// prune tips with support in range [75,100[ (done extra to keep sorting
	// order compatible)
	//
	//

	for (final LN node : nodelist) {
	    final int nt = numTips(node);

	    if (node.data.getSupport() >= 100 || node.data.getSupport() < 75) {
		continue;
	    }

	    if (nt == 2) {
		final String[] tn = getTipNames(node);

		assert (tn.length == 2);

		// System.out.printf("%s %f (%s %s): %d\n", node.data,
		// node.data.getSupport(), tn[0], tn[1], nt);

		final LN tnt = LN.getTowardsNonTip(node);
		if (tnt.back.data.isTip) {
		    throw new RuntimeException("tnt.back.data.isTip is true");
		}

		// String ret[] = new String[2];
		ReductionResult ret = null;
		// int c = 2;
		if (i == num) {
		    ret = new ReductionResult();
		    ret.taxon = tn[1];
		    ret.taxonNeighbor = tn[0];
		    ret.nl = tnt.back;
		    ret.nr = tnt.next.back;

		    tnt.back.back = tnt.next.back;
		    tnt.next.back.back = tnt.back;

		    // ret[0] = tn[1];
		    // ret[1] = tn[0];
		    // return tn[1];
		    return ret;
		}
		i++;
		if (i == num) {
		    ret = new ReductionResult();
		    ret.taxon = tn[0];
		    ret.taxonNeighbor = tn[1];
		    ret.nl = tnt.back;
		    ret.nr = tnt.next.next.back;

		    tnt.back.back = tnt.next.next.back;
		    tnt.next.next.back.back = tnt.back;

		    // ret[0] = tn[0];
		    // ret[1] = tn[1];
		    return ret;

		    // return tn[0];
		}
		i++;
	    }

	}
	// }

	// find cases where a single tip has two neighboring brnaches with 100 %
	// support
	// this is done in an extra loop to keep the sorting order compatible
	// with the old version
	for (final LN node : nodelist) {
	    final int nt = numTips(node);

	    if (nt == 1) {
		final LN tt = LN.getTowardsTip(node);

		// if( tt.back.data.getTipName().equals("poly914")) {
		// System.out.printf( "poly914\n" );
		// }

		if (!tt.next.back.data.isTip && !tt.next.next.back.data.isTip
			&& tt.next.backSupport >= 75.0
			&& tt.next.next.backSupport >= 75.0
			&& tt.next.backSupport < 100.0
			&& tt.next.next.backSupport < 100.0) {
		    if (i == num) {

			// String ret[] = new String[2];

			final ReductionResult ret = new ReductionResult();
			ret.taxon = tt.back.data.getTipName();
			ret.taxonNeighbor = null;
			ret.nl = tt.next.back;
			ret.nr = tt.next.next.back;

			// remove the current node (and the branch toward the
			// tip) by retwiddling of the other two nodes
			tt.next.back.back = tt.next.next.back;
			tt.next.next.back.back = tt.next.back;

			// ret[0] = tt.back.data.getTipName();
			// ret[1] = null;

			return ret;

		    }

		    i++;
		}
	    }

	}

	return null;
    }

    public static void main(String[] args) {
	// createLeastGappySubseq("---abc-ded-f--gi-jkl", 4);

	// createReducedTrees("RAxML_bipartitions.140.BEST.WITH", "140");
	// createReducedTrees("RAxML_bipartitions.855.BEST.WITH", "855");
	createReducedTrees("RAxML_bipartitions.150.BEST.WITH", "150");
	// createReducedTrees("RAxML_bipartitions.218.BEST.WITH", "218");
	// createReducedTrees("RAxML_bipartitions.500.BEST.WITH", "500");
	// createReducedTrees("RAxML_bipartitions.628.BEST.WITH", "628");
	// createReducedTrees("RAxML_bipartitions.714.BEST.WITH", "714");
	createReducedTrees("RAxML_bipartitions.1604.BEST.WITH", "1604");
	// createReducedTrees("RAxML_bipartitions.1605.BEST.WITH", "1605");
	// createReducedTrees("RAxML_bipartitions.2000.BEST.WITH", "2000");
	// createReducedTrees("RAxML_bipartitions.150.BEST.WITH", "150" );
	// createReducedTrees("RAxML_bipartitions.354.BEST.WITH", "354" );

    }

    public static void createReducedTrees(String filename, String alignName) {
	final File basedir = new File("/space/raxml/VINCENT/");
	final File alignmentdir = new File("/space/raxml/VINCENT/DATA");

	final File outdir = new File("/space/redtree_testing");
	final File degen_alignoutdir = new File("/space/red_alignments_testing");
	final File subseq_alignoutdir = new File(
		"/space/subseq_alignments_testing");
	final File subseq_queryoutdir = new File("/space/subseq_query_testing");
	final File pairedend_alignoutdir = new File(
		"/space/pairedend_alignments_testing");
	final File pairedend_queryoutdir = new File(
		"/space/pairedend_query_testing");
	final File dist_subseq_alignoutdir = new File(
		"/space/dist_subseq_alignments_testing");
	;

	final MultipleAlignment pairedend_all;

	MultipleAlignment.USE_SHITTY_LOADER = !true;
	if (CREATE_PAIRED_END_ALIGNMENTS) {
	    pairedend_all = MultipleAlignment.loadPhylip(new File(alignmentdir,
		    alignName));
	}

	final MultipleAlignment inputAlignment = MultipleAlignment
		.loadPhylip(new File(alignmentdir, alignName));

	final int alignmentLength = inputAlignment.seqLen;

	PrintStream realNeighborFile;
	PrintStream numberToTaxonFile;

	if (CREATE_MAPPING_FILES) {
	    try {
		realNeighborFile = new PrintStream(new FileOutputStream(
			new File(degen_alignoutdir, "real_neighbors_"
				+ alignName + ".txt")));
		numberToTaxonFile = new PrintStream(new FileOutputStream(
			new File(degen_alignoutdir, "number_to_taxon_"
				+ alignName + ".txt")));
	    } catch (final FileNotFoundException ex) {
		Logger.getLogger(FindMinSupport.class.getName()).log(
			Level.SEVERE, null, ex);
		throw new RuntimeException("bailing out");
	    }
	} else {
	    realNeighborFile = null;
	    numberToTaxonFile = null;
	}

	for (int i = START_TREE_IDX;; i++) {
	    final File f = new File(basedir, filename);

	    final TreeParser tp = new TreeParser(f);

	    final LN n = tp.parse();

	    // String[] taxonAN = createNThReducedTree(n, i);

	    final ReductionResult res = createNThReducedTree(n, i);

	    if (res == null) {
		System.out.printf("finished after %d trees\n", i);
		break;
	    }
	    final String taxon = res.taxon;

	    if (numberToTaxonFile != null) {
		numberToTaxonFile.printf("%s\t%s\n", padchar("" + i, 0, 4),
			taxon);
	    }

	    if (CREATE_REDUCED_TREES) {

		try {
		    final File outfile = new File(outdir, filename + "_"
			    + padchar("" + i, 0, 4));

		    final PrintStream ps = new PrintStream(
			    new BufferedOutputStream(new FileOutputStream(
				    outfile)));
		    TreePrinter.printRaw(n, ps);
		    ps.close();
		} catch (final FileNotFoundException ex) {
		    Logger.getLogger(FindMinSupport.class.getName()).log(
			    Level.SEVERE, null, ex);
		}
	    }
	    System.out.printf("dropped taxon: %s\n", taxon);

	    if (CREATE_REDUCED_ALIGNMENTS) {
		for (int j = 10; j <= 100; j += 10) {
		    final int len = (int) Math.ceil(alignmentLength
			    * (j / 100.0));

		    String subsuffix = ".gz";

		    final boolean valid = createDegeneratedAlignment(new File(
			    alignmentdir, alignName),
			    new File(degen_alignoutdir, alignName + "_"
				    + padchar("" + i, 0, 4) + "_" + j
				    + subsuffix), taxon, len);

		    if (!valid) {
			break;
		    }

		    System.out.printf("%d ", j);

		}
	    }
	    System.out.println();

	    if (CREATE_SUBSEQ_ALIGNMENTS) {
		// final int[] ssLenList = {250, 500};

		final int[] ssLenList = { 500 };

		for (final int ssLen : ssLenList) {
		    for (SubseqPos sp : SubseqPos.values()) {

			String subsuffix = ".gz";
			String posid = subseqIdent(sp);
			File alignOutfile = new File(subseq_alignoutdir,
				alignName + "_" + padchar("" + i, 0, 4) + "_"
					+ ssLen + posid + subsuffix);

			final String qs = createSubseqAlignment(new File(
				alignmentdir, alignName), alignOutfile, taxon,
				ssLen, sp);

			try {
			    File qf = new File(subseq_queryoutdir, alignName
				    + "_" + padchar("" + i, 0, 4) + "_" + ssLen
				    + posid + ".fa");
			    PrintWriter pw = new PrintWriter(new FileWriter(qf));
			    pw.printf("> %s\n", taxon);
			    pw.printf("%s\n", qs);
			    pw.close();
			} catch (IOException x) {
			    x.printStackTrace();
			    throw new RuntimeException("bailing out");
			}
		    }
		}
	    }

	    if (CREATE_PAIRED_END_ALIGNMENTS) {
		// final int[] ssLenList = {250, 500};

		final int ssLen = 100;

		String subsuffix = ".gz";

		File alignOutfile = new File(pairedend_alignoutdir, alignName
			+ "_" + padchar("" + i, 0, 4) + "_" + ssLen + subsuffix);

		final String qs = createPairedEndAlignment(new File(
			alignmentdir, alignName), alignOutfile, taxon, ssLen);

		pairedend_all.replaceSequence(taxon, qs);

		try {
		    File qf = new File(pairedend_queryoutdir, alignName + "_"
			    + padchar("" + i, 0, 4) + "_" + ssLen + ".fa");
		    PrintWriter pw = new PrintWriter(new FileWriter(qf));
		    pw.printf("> %s\n", taxon);
		    pw.printf("%s\n", qs);
		    pw.close();
		} catch (IOException x) {
		    x.printStackTrace();
		    throw new RuntimeException("bailing out");
		}
	    }

	    if (CREATE_DIST_SUBSEQ_ALIGNMENTS) {
		final int meanLen = 100;
		final int sd = 0;
		final int nSamples = 20;
		final int minLen = 100;
		final int maxLen = 100;

		File alignOutfile = new File(dist_subseq_alignoutdir, alignName
			+ "_" + padchar("" + i, 0, 4) + "_" + meanLen + "_"
			+ sd);

		// final String qs = createSubseqAlignment(new
		// File(alignmentdir, alignName), alignOutfile, taxon, len, sp);

		final MultipleAlignment ma = MultipleAlignment
			.loadPhylip(new File(alignmentdir, alignName));
		ma.getSequence(taxon);

		int nseqs = ma.nTaxon - 1 + nSamples;

		String[] names = new String[nseqs];
		String[] seqs = new String[nseqs];
		int outptr = 0;

		String qs = null;
		for (int j = 0; j < ma.nTaxon; j++) {
		    String cn = ma.names[j];
		    String cs = ma.data[j];

		    if (!taxon.equals(cn)) {
			names[outptr] = cn;
			seqs[outptr] = cs;
			outptr++;
		    } else {
			assert (qs == null);
			qs = cs;
		    }
		}

		assert (qs != null);
		assert (outptr == ma.nTaxon - 1);

		for (int j = 0; j < nSamples; j++) {
		    double gr = rand.nextGaussian();
		    final int len = Math.min(
			    maxLen,
			    Math.max(minLen,
				    (int) Math.round(gr * sd + meanLen)));

		    names[outptr] = taxon + "_" + padchar("" + j, 0, 2);
		    seqs[outptr] = createRandomSubseq(qs, len);

		    outptr++;
		}

		(new MultipleAlignment(ma.seqLen, names, seqs))
			.writePhylip(alignOutfile);

	    }

	    if (realNeighborFile != null) {
		// find the split induced by the tip position, to identify the
		// insertion position in the reduced tree
		String[] ssl; // smaller split list
		{
		    final LN[] ll = LN.getAsList(res.nl, false);
		    final LN[] lr = LN.getAsList(res.nr, false);

		    final Set<String> sl = LN.getTipSet(ll);
		    final Set<String> sr = LN.getTipSet(lr);

		    final Set<String> smallset = (sl.size() <= sr.size()) ? sl
			    : sr;

		    ssl = new String[smallset.size()];
		    ssl = smallset.toArray(ssl);
		    Arrays.sort(ssl);

		}

		if (res.taxonNeighbor != null) {
		    realNeighborFile.println(padchar("" + i, 0, 4) + "\t"
			    + taxon + "\t" + res.taxonNeighbor + "\t"
			    + commaSeparatedList(ssl));
		} else {
		    realNeighborFile.println(padchar("" + i, 0, 4) + "\t"
			    + taxon + "\t" + "*NONE*" + "\t"
			    + commaSeparatedList(ssl));
		}
	    }
	}

	if (CREATE_PAIRED_END_ALIGNMENTS) {
	    pairedend_all.writePhylip(new File(pairedend_alignoutdir, alignName
		    + "_all"));
	}

	if (realNeighborFile != null) {
	    realNeighborFile.close();
	}
	if (numberToTaxonFile != null) {
	    numberToTaxonFile.close();
	}
	// System.out.printf( "nTT: %d\n", nTT );
    }

    public static String commaSeparatedList(String[] ssl) {
	final StringBuilder sb = new StringBuilder();
	for (int i = 0; i < ssl.length; i++) {
	    sb.append(ssl[i]);
	    if (i != ssl.length - 1) {
		sb.append(",");
	    }
	}

	return sb.toString();
    }

    static String createStartSubseq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	if (nm.length < length) {
	    throw new RuntimeException("less than " + length
		    + " non-gap characters in sequence");
	}

	final int sp = 0;
	final int ep = length - 1;

	return genAlignedSubseq(seq, nm, sp, ep);
    }

    static String createMidSubseq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	if (nm.length < length) {
	    throw new RuntimeException("less than " + length
		    + " non-gap characters in sequence");
	}

	final int pregap = (nm.length - length) / 2;

	final int sp = pregap;
	final int ep = pregap + length - 1;

	return genAlignedSubseq(seq, nm, sp, ep);
    }

    static String createEndSubseq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	if (nm.length < length) {
	    throw new RuntimeException("less than " + length
		    + " non-gap characters in sequence");
	}

	final int pregap = nm.length - length;

	final int sp = pregap;
	final int ep = pregap + length - 1;

	return genAlignedSubseq(seq, nm, sp, ep);
    }

    static String createRandomSubseq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	if (nm.length < length) {
	    System.err.printf("bad seq: '%s'\n", seq);
	    throw new RuntimeException("less than " + length
		    + " non-gap characters in sequence");
	}

	final int maxStartPos = nm.length - length + 1;

	final int sp = rand.nextInt(maxStartPos);
	final int ep = sp + length - 1;

	return genAlignedSubseq(seq, nm, sp, ep);
    }

    static String createRandomSubseqClamped(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	length = Math.min(length, nm.length);

	final int maxStartPos = nm.length - length + 1;

	final int sp = rand.nextInt(maxStartPos);
	final int ep = sp + length - 1;

	return genAlignedSubseq(seq, nm, sp, ep);
    }

    static String createLeastGappySubseq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);
	if (nm.length < length) {
	    throw new RuntimeException("less than " + length
		    + " non-gap characters in sequence");
	}

	final int maxStartPos = nm.length - length + 1;

	final int[] gapsByStartpos = new int[maxStartPos];

	int minGaps = Integer.MAX_VALUE;

	for (int i = 0; i < maxStartPos; i++) {
	    int numGaps = 0;

	    for (int j = i; j < i + length - 1; j++) {
		numGaps += nm[j + 1] - nm[j] - 1;
	    }

	    gapsByStartpos[i] = numGaps;

	    minGaps = Math.min(minGaps, numGaps);
	    // System.out.printf( "start pos: %d: %d\n", i, numGaps );
	}

	if (minGaps == Integer.MAX_VALUE) {
	    throw new RuntimeException(
		    "could not find any start position with less than infinite gaps (which should not be possible ...)");
	}

	final ArrayList<Integer> minPosList = new ArrayList<Integer>();

	for (int i = 0; i < maxStartPos; i++) {
	    if (gapsByStartpos[i] == minGaps) {
		minPosList.add(i);
	    }
	}

	System.out.printf(
		"best possible subseq has %d gaps (%d alternatives)\n",
		minGaps, minPosList.size());
	assert (minPosList.size() >= 1);

	final int sp = minPosList.get(rand.nextInt(minPosList.size()));
	final int ep = sp + length - 1;

	final String sub = genAlignedSubseq(seq, nm, sp, ep);
	System.out.printf("'%s' =>\n'%s'\n", seq, sub);

	return sub;
    }

    private static String genAlignedSubseq(String seq, final int[] nm,
	    final int sp, final int ep) {
	final int rsp = nm[sp];
	final int rep = nm[ep] + 1;

	final String sub = repchar('-', rsp) + seq.substring(rsp, rep)
		+ repchar('-', seq.length() - rep);
	return sub;
    }

    private static String repchar(char c, int n) {
	final StringBuffer sb = new StringBuffer(n);
	for (int i = 0; i < n; i++) {
	    sb.append(c);
	}

	return sb.toString();
    }

    static int[] getNonGapCharacterMap(String seq) {
	int num = 0;
	for (int i = 0; i < seq.length(); i++) {
	    if (!isGapCharacter(seq.charAt(i))) {
		num++;
	    }
	}

	final int[] map = new int[num];
	num = 0;

	for (int i = 0; i < seq.length(); i++) {
	    if (!isGapCharacter(seq.charAt(i))) {
		map[num++] = i;
	    }
	}
	return map;
    }

    private static boolean createDegeneratedAlignment(File infile,
	    File outfile, String taxon, int len) {
	final MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);
	final String seq = ma.getSequence(taxon);

	final String degseq = createDegeneratedSequence(seq, len);
	if (degseq == null) {
	    return false;
	} else {
	    ma.replaceSequence(taxon, degseq);

	    ma.writePhylip(outfile);
	    return true;
	}
    }

    static String createDegeneratedSequence(String seq, int len) {
	final int[] ngm = getNonGapCharacterMap(seq);
	int ngmsize = ngm.length;

	if (ngmsize < len) {
	    return null;
	}

	final char[] sa = seq.toCharArray();

	final int numgaps = ngmsize - len;

	for (int i = 0; i < numgaps; i++) {
	    final int n = rand.nextInt(ngmsize);

	    final int r = ngm[n];
	    sa[r] = '-';

	    ngm[n] = ngm[ngmsize - 1];
	    ngmsize--;
	}

	return new String(sa);
    }

    enum SubseqPos {
	LEAST_GAPPY_RANDOM, START, END, MID, RANDOM
    }

    static String subseqIdent(SubseqPos pos) {
	switch (pos) {
	case LEAST_GAPPY_RANDOM:
	    return "l";

	case START:
	    return "s";

	case MID:
	    return "m";

	case END:
	    return "e";

	default:
	    throw new RuntimeException("unhandled case");
	}
    }

    private static String createSubseqAlignment(File infile, File outfile,
	    String taxon, int length, SubseqPos pos) {

	final MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);
	final String seq = ma.getSequence(taxon);

	final String degseq;

	switch (pos) {
	case LEAST_GAPPY_RANDOM:
	    degseq = createLeastGappySubseq(seq, length);
	    break;

	case RANDOM:
	    degseq = createRandomSubseq(seq, length);
	    break;

	case START:
	    degseq = createStartSubseq(seq, length);
	    break;

	case MID:
	    degseq = createMidSubseq(seq, length);
	    break;

	case END:
	    degseq = createEndSubseq(seq, length);
	    break;

	default:
	    throw new RuntimeException("unhandled case");
	}

	ma.replaceSequence(taxon, degseq);

	ma.writePhylip(outfile);

	return removeGaps(degseq);
    }

    static String createPairedEndSeq(String seq, int length) {
	final int[] nm = getNonGapCharacterMap(seq);

	int length_2 = length * 2;

	if (nm.length < length_2) {
	    throw new RuntimeException("less than " + length
		    + " * 2 non-gap characters in sequence");
	}

	final int sp1 = 0;
	final int ep1 = length - 1;

	final int pregap = nm.length - length;

	final int sp2 = pregap;
	final int ep2 = pregap + length - 1;

	final int rsp1 = nm[sp1];
	final int rep1 = nm[ep1] + 1;

	final int rsp2 = nm[sp2];
	final int rep2 = nm[ep2] + 1;

	final String sub = repchar('-', rsp1) + seq.substring(rsp1, rep1)
		+ repchar('-', rsp2 - rep1) + seq.substring(rsp2, rep2)
		+ repchar('-', seq.length() - rep2);
	return sub;
    }

    private static String createPairedEndAlignment(File infile, File outfile,
	    String taxon, int length) {

	final MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);
	final String seq = ma.getSequence(taxon);

	final String degseq = createPairedEndSeq(seq, length);

	ma.replaceSequence(taxon, degseq);

	ma.writePhylip(outfile);

	return removeGaps(degseq);
    }

    public static String removeGaps(String degseq) {
	int nGaps = 0;

	for (int i = 0; i < degseq.length(); i++) {
	    if (isGap(degseq.charAt(i))) {
		nGaps++;
	    }
	}

	final int nng = degseq.length() - nGaps;

	final char[] out = new char[nng];
	for (int i = 0, j = 0; i < nng; i++, j++) {

	    while (isGap(degseq.charAt(j))) {
		j++;
	    }
	    out[i] = degseq.charAt(j);
	}

	return new String(out);
    }

    private static boolean isGap(char c) {
	return c == '-' || c == '?';
    }

    static int numTips(LN n) {
	if (n.data.isTip) {
	    return 0;
	} else {
	    final LN start = n;
	    LN cur = n.next;
	    int nTips = 0;

	    while (cur != start) {
		if (cur.back.data.isTip) {
		    nTips++;
		}
		cur = cur.next;
	    }

	    return nTips;
	}
    }

    // static LN getTowardsTree( LN n ) {
    // if( !n.data.isTip ) {
    // throw new RuntimeException( "parameter is not a tip" );
    // }
    //
    // for( int i = 0; i < 3; i++, n = n.next ) {
    // if( n.back != null ) {
    // return n;
    // }
    // }
    //
    // throw new RuntimeException(
    // "at tip: could not find LN pointing towards the tree" );
    // }

    static String[] getTipNames(LN n) {
	final ArrayList<String> tipnames = new ArrayList<String>();

	final LN start = n;
	LN cur = n.next;
	while (cur != start) {
	    if (cur.back.data.isTip) {
		tipnames.add(cur.back.data.getTipName());
	    }
	    cur = cur.next;
	}

	final String[] ra = new String[tipnames.size()];

	for (int i = 0; i < tipnames.size(); i++) {
	    ra[i] = tipnames.get(i);
	}

	return ra;
    }

    static boolean isGapCharacter(char c) {
	return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
    }

    public static String padchar(String input_string, int with_digit, int to_len) {
	while (input_string.length() < to_len) {
	    input_string = with_digit + input_string;
	}
	return input_string;
    }
}
