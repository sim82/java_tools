package ml;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

class MatrixIdx {
    final int w, h;
    final int n;

    MatrixIdx(int w, int h) {
	this.w = w;
	this.h = h;

	n = w * h;
    }

    int i(int x, int y) {
	return x + y * w;
    }

}

public class PairwiseBranchDist {
    public static void main(String[] args) {
	final File treeFile;

	if (args.length > 1) {
	    treeFile = new File(args[0]);
	} else {
	    treeFile = new File("/scratch/rob_new/new.tree");
	}

	LN tree = TreeParser.parse(treeFile);

	System.out.printf("starting: %d %d %d %d\n", tree.data.serial,
		tree.back.data.serial, tree.next.back.data.serial,
		tree.next.next.back.data.serial);
	LN[][] bra = LN.getAllBranchList3(tree);
	LN[] nl = LN.getAsList(tree);
	System.out.printf("%d %d\n", nl.length, bra.length);

	if (false) {
	    for (int i = 0; i < bra.length; i++) {
		System.out.printf("%d %d\n", bra[i][0].data.serial,
			bra[i][1].data.serial);
	    }
	    for (int i = 0; i < nl.length; i++) {
		System.out.printf("nl: %d\n", nl[i].data.serial);

	    }
	}
	// int numNull = 0;
	// for( int i = 0; i < bra.length; i++ ) {
	// if( bra[i][0].backLen == 0 ) {
	// System.out.printf( "back len == 0\n" );
	// numNull++;
	// }
	//
	//
	// }
	// System.out.printf( "brnaches with zero length: %d\n", numNull );
	// System.out.printf( "%d %d %d\n", br.length, bra.length, bra2.length
	// );
	Random rnd = new Random();

	// Fisher/Yates shuffle
	for (int n = bra.length - 1; n >= 1; n--) {
	    int j = rnd.nextInt(n);
	    LN[] t = bra[j];
	    bra[j] = bra[n];
	    bra[n] = t;
	}

	final int N = 200;

	LN[][] sb = Arrays.copyOfRange(bra, 0, N);

	// pwdBrute( tree, sb );
	// pwdNew( tree, sb );
	pwdBrute(tree, sb);
	pwdNew(tree, sb);

	long t1 = System.currentTimeMillis();
	double pwdb = pwdBrute(tree, sb);
	long t2 = System.currentTimeMillis();

	double pwd = pwdNew(tree, sb);
	long t3 = System.currentTimeMillis();
	System.out.printf("brute: %f %f\n", pwdb, pwd);

	System.out.printf("time: %d %d\n", t2 - t1, t3 - t2);

	Arrays.sort(dsBrute);
	Arrays.sort(dsNew);

	for (int i = 0; i < dsBrute.length; i++) {
	    double d = dsNew[i] - dsBrute[i];

	    if (Math.abs(d) > 1e-7) {
		System.out.printf("diff: %f %f %e\n", dsNew[i], dsBrute[i], d);
	    }
	}

    }

    static double[] dsBrute;

    private static double pwdBrute(LN tree, LN[][] sb) {
	int[] nd = new int[1];
	double bdsum = 0.0;

	dsBrute = new double[sb.length * (sb.length - 1) / 2];

	int n = 0;
	for (int i = 0; i < sb.length; i++) {
	    for (int j = 0; j < i; j++) {
		nd[0] = 0;
		double bd = ClassifierLTree.getPathLenBranchToBranch(sb[i],
			sb[j], 0.5, nd);
		bdsum += bd;
		dsBrute[n] = bd;
		n++;

	    }
	}

	return bdsum;
    }

    static double[] dsNew;

    private static double pwdNew(LN tree, LN[][] sb) {
	double sum = 0.0;
	int npairs = 0;

	LN[] nl = LN.getAsList(tree);
	for (int i = 0; i < nl.length; i++) {
	    nl[i].data.localSerial = i;
	}

	final int nNodes = nl.length;
	final int nBranches = sb.length;

	MatrixIdx mi = new MatrixIdx(nBranches, nNodes);
	MatrixIdx bbi = new MatrixIdx(nBranches, nBranches);

	// BitSet bbbs = new BitSet(bbi.n);

	boolean[] bbbs = new boolean[bbi.n];

	double d[] = new double[mi.n];
	Arrays.fill(d, -1);
	LinkedList<LN>[] qs = new LinkedList[nBranches];

	dsNew = new double[sb.length * (sb.length - 1) / 2];
	int nds = 0;

	for (int i = 0; i < qs.length; i++) {
	    qs[i] = new LinkedList<LN>();
	    qs[i].add(sb[i][0]);
	    qs[i].add(sb[i][1]);
	    d[mi.i(i, sb[i][0].data.localSerial)] = sb[i][0].backLen * 0.5;
	    d[mi.i(i, sb[i][1].data.localSerial)] = sb[i][0].backLen * 0.5;

	    // check for early hits == branches directly next to each other
	    for (int j = 0; j < 1; j++) {
		int oser = sb[i][j].data.localSerial;

		for (int k = 0; k < qs.length; k++) {
		    if (k != i && !bbbs[bbi.i(k, i)]) {
			if (d[mi.i(k, oser)] > 0) {
			    // if( false ) {
			    // System.out.printf( "dist: %d %d = %.4f\n", i, k,
			    // d[mi.i(k, oser)] + d[mi.i(i, oser)] );
			    // }

			    // System.out.printf( "early hit\n" );
			    double bd = d[mi.i(k, oser)] + d[mi.i(i, oser)];
			    sum += bd;
			    dsNew[nds] = bd;
			    nds++;
			    bbbs[bbi.i(i, k)] = true;
			    bbbs[bbi.i(k, i)] = true;
			    npairs++;
			}
		    }
		}
	    }
	}

	boolean done = false;

	while (!done) {
	    boolean allEmpty = true;
	    for (int i = 0; i < qs.length; i++) {
		if (qs[i].isEmpty()) {
		    System.out.printf("queue empty: %d\n", i);
		    continue;
		}

		// if( false )
		// {
		// System.out.printf( "q: (%d) -> ", i );
		// for( LN qe : qs[i]) {
		// System.out.printf( "%d ", qe.data.serial );
		// }
		// System.out.printf( "\n" );
		//
		// }

		allEmpty = false;
		LN n = qs[i].removeFirst();
		int ser = n.data.localSerial;

		double cd = d[mi.i(i, ser)];

		if (cd < 0.0) {
		    throw new RuntimeException("cd <= 0.0");
		}

		LN[] ns = { n.next, n.next.next };
		for (LN on : ns) {
		    if (on.back == null) {
			continue;
		    }
		    int oser = on.back.data.localSerial;

		    qs[i].add(on.back);
		    d[mi.i(i, oser)] = cd + on.backLen;

		    // search for hits = other branches that have already
		    // calculated their
		    // distance to the current node (oser)
		    for (int k = 0; k < qs.length; k++) {
			if (k != i && !bbbs[bbi.i(i, k)]) {
			    if (d[mi.i(k, oser)] > 0) {
				// if( false ) {
				// System.out.printf( "dist: %d %d = %.4f\n", i,
				// k, d[mi.i(k, oser)] + d[mi.i(i, oser)] );
				// }

				// System.out.printf( "early hit\n" );
				double bd = d[mi.i(k, oser)] + d[mi.i(i, oser)];
				sum += bd;
				dsNew[nds] = bd;
				nds++;

				bbbs[bbi.i(i, k)] = true;
				bbbs[bbi.i(k, i)] = true;
				npairs++;
			    }
			}
		    }
		}

	    }
	    // if( false ) {
	    // System.out.printf( "npairs: %d %d\n", npairs,
	    // 0/*bbbs.cardinality()*/ );
	    // }

	    done = allEmpty || (npairs * 2 == (nBranches * (nBranches - 1)));
	}

	// if( false ) {
	// for( int i = 0; i < nNodes; i++ ) {
	// System.out.printf( "(%d)", i );
	// for( int j = 0; j < nBranches; j++ ) {
	// System.out.printf( "\t%.2f", d[mi.i(j,i)] );
	// }
	// System.out.printf( "\n" );
	// }
	// }

	return sum;
    }
}
