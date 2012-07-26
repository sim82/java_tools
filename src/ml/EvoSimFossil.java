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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class EvoSimFossil {
    static Random rnd;

    TreeMap<String, byte[]> seqList = new TreeMap<String, byte[]>();

    public EvoSimFossil(LN tree, byte[] fossilSeq, double scale) {
	this(tree, fossilSeq, scale, null);
    }

    public EvoSimFossil(LN tree, byte[] fossilSeq, double scale, PrintWriter bw) {

	EvoSim.SCALE = scale;

	LN[][] branches = LN.getAllBranchList(tree);

	LN[] innerstBranch = null;
	double innerstDist = 0;
	double meanBranchLen = 0.0;

	for (LN[] br : branches) {
	    assert (br[0].backLen == br[1].backLen);
	    meanBranchLen += br[0].backLen;

	    // if( br[0].data.seq == null ) {
	    // br[0].data.seq = new byte[fossilSeq.length];
	    // }
	    // if( br[1].data.seq == null ) {
	    // br[1].data.seq = new byte[fossilSeq.length];
	    // }

	    if (br[0].data.isTip || br[1].data.isTip) {
		continue;
	    }

	    double d = getShortestPath(br);
	    // System.out.printf( "d: %f\n", d );

	    if (d > innerstDist) {
		innerstBranch = br;
		innerstDist = d;
	    }

	}

	meanBranchLen /= branches.length;

	if (bw != null) {
	    String[] splitSet = LN.getSmallerSplitSet(innerstBranch);
	    Arrays.sort(splitSet);
	    bw.printf("0000\tfossil\t*NONE*\t%s\n",
		    FindMinSupport.commaSeparatedList(splitSet));
	}

	
	
	LN tip = LN.insertBranch(innerstBranch, meanBranchLen);
	tip.data.setTipName("fossil");
	tip.data.isTip = true;
	tip.data.seq = fossilSeq;
	// TreePrinter.printRaw(tip.back, System.out);

	tip.data.seq = fossilSeq;

	EvoSim.evolveRec(tip);
	// EvoSim.evolveRec( tip.next.next );

	LN[] list = LN.getAsList(tip);

	for (LN n : list) {
	    if (n.data.isTip && n.back != null) {
		seqList.put(n.data.getTipName(), n.data.seq);
	    }
	}

    }

    public static void main(String[] args) throws FileNotFoundException {

	final int BAD_LEN = Integer.parseInt(args[2]);
	final int GOOD_LEN = Integer.parseInt(args[3]);

	final boolean BAD_DATA = BAD_LEN > 0;
	final boolean GOOD_DATA = GOOD_LEN > 0;

	final boolean WITH_FOSSIL = args.length > 4 && args[4].equals("F");

	int seed;

	if (args.length > 5) {
	    seed = Integer.parseInt(args[5]);
	} else {
	    seed = 123456;
	}

	final int treeseed;
	if (args.length > 6) {
	    treeseed = Integer.parseInt(args[6]);
	} else {
	    treeseed = 123456;
	}

	final double BRANCH_SCALE;

	if (args.length > 7) {
	    BRANCH_SCALE = Double.parseDouble(args[7]);
	} else {
	    BRANCH_SCALE = 1.0;
	}

	System.out.printf("EvoSim scaling: %f\n", BRANCH_SCALE);

	rnd = new Random(seed);

	String outname = "";
	if (BAD_DATA && GOOD_DATA) {
	    outname += "both_" + BAD_LEN + "_" + GOOD_LEN;
	} else if (BAD_DATA) {
	    outname += "bad_" + BAD_LEN;
	} else if (GOOD_DATA) {
	    outname += "good_" + GOOD_LEN;
	} else {
	    assert (false);
	}

	if (WITH_FOSSIL) {
	    outname += "_F";
	}
	outname += "_" + treeseed + "_" + seed;

	byte[] fossilSeqGood = new byte[GOOD_LEN];
	randSeqBIN(fossilSeqGood);
	byte[] fossilSeqBad = new byte[BAD_LEN];
	randSeqBIN(fossilSeqBad);

	LN treeBad = LN.getNonTipNode(LN.parseTree(new File(args[0])));
	LN treeGood = LN.getNonTipNode(LN.parseTree(new File(args[1])));

	drawBranchLengths(treeGood, treeBad);

	new Random();

	PrintWriter bw = null;
	if (GOOD_DATA) {
	    bw = new PrintWriter(new File("rn_good.txt"));
	}

	EvoSimFossil esf = new EvoSimFossil(treeGood, fossilSeqGood,
		BRANCH_SCALE, bw);
	if (bw != null) {
	    bw.close();
	}

	EvoSimFossil esf_rand = new EvoSimFossil(treeBad, fossilSeqBad,
		BRANCH_SCALE);

	int nTaxa = esf.seqList.size() - 1;
	if (WITH_FOSSIL) {
	    nTaxa++;
	}

	int seqLen = 0;
	if (GOOD_DATA) {
	    seqLen += fossilSeqGood.length;

	}

	if (BAD_DATA) {
	    seqLen += fossilSeqBad.length;
	}

	String[] names = new String[nTaxa];
	String[] seqs = new String[nTaxa];
	int i = 0;

	for (Map.Entry<String, byte[]> s1 : esf.seqList.entrySet()) {
	    String name = s1.getKey();

	    if (!WITH_FOSSIL && name.equals("fossil")) {
		continue;
	    }

	    byte[] seq1 = s1.getValue();
	    byte[] seq2 = esf_rand.seqList.get(name);

	    names[i] = name;

	    seqs[i] = "";
	    if (BAD_DATA) {
		seqs[i] += new String(seq2);
	    }
	    if (GOOD_DATA) {
		seqs[i] += new String(seq1);
	    }
	    i++;

	}

	MultipleAlignment ma = new MultipleAlignment(seqLen, names, seqs);

	PrintStream ps = new PrintStream(new FileOutputStream(outname));
	ma.writePhylip(ps);

    }

    private static void drawBranchLengths(LN tree1, LN tree2) {
	LN[][] br1 = LN.getAllBranchList(tree1);
	LN[][] br2 = LN.getAllBranchList(tree2);

	// System.out.printf( "nbr: %d\n", br1.length );

	if (br1.length != br2.length) {
	    throw new RuntimeException("num branches not equal");
	}

	int[] r = randomPermutation(br1.length);
	for (int i = 0; i < br1.length; i++) {
	    br2[i][0].backLen = br1[r[i]][0].backLen;
	    br2[i][1].backLen = br1[r[i]][0].backLen;
	    // System.out.printf( "dd: %f %f\n", br2[i][0].backLen,
	    // br2[i][1].backLen );
	}

    }

    static int[] randomPermutation(int N) {
	int[] r = new int[N];
	for (int i = 0; i < N; i++) {
	    r[i] = i;
	}

	int n = N;
	while (n > 1) {
	    n--;
	    int k = rnd.nextInt(n + 1);

	    int tmp = r[k];
	    r[k] = r[n];
	    r[n] = tmp;
	}

	return r;
    }

    private static void randSeqBIN(byte[] seq) {
	// // not necessary. just fill in '0'
	// Arrays.fill(seq, (byte)'0');

	for (int i = 0; i < seq.length; i++) {
	    seq[i] = (byte) (rnd.nextBoolean() ? '1' : '0');
	}
    }

    double getShortestPath(LN[] br) {

	return Math.min(LN.shortestPathRec(br[0]), LN.shortestPathRec(br[1]));
    }

}
