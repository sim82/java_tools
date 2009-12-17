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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NearestNeighborClass {
	public final static boolean VERBOSE = false;

	static LN reftree;
	static double reftreeDiameter;
	static Random rnd = new Random();

	public static void main(String[] args) {
		if (args[1].equals("--auto")) {
			String ds = args[2];

			File radir = new File("/space/red_alignments");
			File rtfile = new File(args[0]);

			ArrayList<String[]> sgpairs = new ArrayList<String[]>();

			TreeParser tp = new TreeParser(rtfile);
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

			boolean end = false;
			for (int seq = 0; !end; seq++) {
				end = true;

				for (int gap = 10; gap <= 100; gap += 10) {

					String sps = padzero(4, seq);
					String name = ds + "_" + sps + "_" + gap;

					if (!new File(radir, name).isFile()) {
						name += ".gz";
						if (!new File(radir, name).isFile()) {
							continue;
						}
					}

					end = false;

					File treefile = new File(
							"/space/redtree/RAxML_bipartitions." + ds
									+ ".BEST.WITH_" + sps);
					File phyfile = new File(radir, name);

					wellDoIt(treefile, phyfile, sps, "" + gap);

					// if( new File(radir, name).isFile()) {
					// String[] wwooooohhh = {sps, "" + gap, name };
					// sgpairs.add( wwooooohhh );
					// } else if( new File(radir, name + ".gz").isFile()) {
					// String[] uuuhhhaarrg = {sps, "" + gap, name + ".gz" };
					// sgpairs.add( uuuhhhaarrg );
					// }

				}
			}

		} else if (args[1].equals("--autope")) {

			String ds = args[2];

			String[] gaps = { "100" };

			if (args.length > 3) {
				gaps[0] = args[3];
			}

			File radir = new File("/space/pairedend_alignments");
			File rtfile = new File(args[0]);

			ArrayList<String[]> sgpairs = new ArrayList<String[]>();

			TreeParser tp = new TreeParser(rtfile);
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

			boolean end = false;
			for (int seq = 0; !end; seq++) {
				end = true;

				for (String gap : gaps) {

					String sps = padzero(4, seq);
					String name = ds + "_" + sps + "_" + gap;

					if (!new File(radir, name).isFile()) {
						name += ".gz";
						if (!new File(radir, name).isFile()) {
							continue;
						}
					}

					end = false;

					File treefile = new File(
							"/space/redtree/RAxML_bipartitions." + ds
									+ ".BEST.WITH_" + sps);
					File phyfile = new File(radir, name);

					wellDoIt(treefile, phyfile, sps, "" + gap);

				}
			}

		} else if (args[1].equals("--autodist")) {

			String[] dp = { "200_60" };

			String ds = args[2];

			File radir = new File("/space/dist_subseq_alignments");
			File rtfile = new File(args[0]);

			ArrayList<String[]> sgpairs = new ArrayList<String[]>();

			TreeParser tp = new TreeParser(rtfile);
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
			final boolean DO_BS = !false;

			boolean end = false;
			for (int seq = 0; !end; seq++) {
				end = true;

				for (String gap : dp) {

					String sps = padzero(4, seq);
					String name = ds + "_" + sps + "_" + gap;

					if (VERBOSE) {
						System.out.printf("checking: %s\n", name);
					}

					if (!new File(radir, name).isFile()) {
						if (VERBOSE) {
							System.out.printf("not found\n");
						}
						name += ".gz";
						if (!new File(radir, name).isFile()) {
							continue;
						}
					}

					end = false;

					File treefile = new File(
							"/space/redtree/RAxML_bipartitions." + ds
									+ ".BEST.WITH_" + sps);
					File phyfile = new File(radir, name);

					if (DO_BS) {
						wellDoItBootstrap(treefile, phyfile, sps, "" + gap, 3);
					} else {
						wellDoIt(treefile, phyfile, sps, "" + gap, 3);
					}

				}
			}

		} else {

			File rtfile = new File(args[0]);
			File treefile = new File(args[1]);
			File phyfile = new File(args[2]);

			String seq = null;
			String gap = null;
			{
				String name = phyfile.getName();
				int idx1st = name.indexOf('_');
				if (idx1st >= 0) {
					int idx2nd = name.indexOf('_', idx1st);

					if (idx2nd > idx1st) {
						int idxdot = name.indexOf('.', idx2nd);

						if (idxdot > idx2nd) {
							seq = name.substring(idx1st + 1, idx2nd);
							gap = name.substring(idx2nd + 1, idxdot);
						}

					}
				}

			}
			TreeParser tp = new TreeParser(rtfile);
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
			wellDoIt(treefile, phyfile, seq, gap);
		}
	}

	private static void wellDoIt(File treefile, File phyfile, String seq,
			String gap) {
		wellDoIt(treefile, phyfile, seq, gap, -1);
	}

	private static void wellDoIt(File treefile, File phyfile, String seq,
			String gap, int removeSuffix) {
		LN tree;
		{
			TreeParser tp = new TreeParser(treefile);
			tree = tp.parse();
		}

		Set<String> taxonSet = new HashSet<String>();

		{
			LN[] list = LN.getAsList(tree);
			for (LN n : list) {
				if (n.data.isTip) {
					taxonSet.add(n.data.getTipName());
				}
			}
		}

		MultipleAlignment ma;
		try {
			ma = MultipleAlignment.loadPhylip(GZStreamAdaptor.open(phyfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("bailing out");
		}

		ArrayList<String> queryNames = new ArrayList<String>();
		Set<String> qnSet = new HashSet<String>();
		for (String name : ma.names) {
			if (!taxonSet.contains(name)) {
				queryNames.add(name);
				qnSet.add(name);
				// System.out.printf( "add query: %s\n", name );
			}
		}

		// System.out.printf( "n tree: %d, n ma: %d\n", taxonSet.size(),
		// ma.names.length );

		for (String qn : queryNames) {

			String qnOrig = qn;

			if (VERBOSE) {
				System.out.printf("query: %s\n", qn);
			}

			String qs = ma.getSequence(qn);

			int[] ngm = nonGapMap(qs);
			int[] ngm_bs = bsSample(ngm);

			int edMin = Integer.MAX_VALUE;
			int idxMin = -1;
			int nMin = 0;
			for (int i = 0; i < ma.names.length; i++) {
				String on = ma.names[i];
				if (qnSet.contains(on)) {
					// System.out.printf( "drop: %s\n", on );
					continue;
				}

				String os = ma.getSequence(i);

				int ed = editDist_explicit(qs, os, ngm_bs);
				// int ed = editDist_nogaps(qs, os);
				if (VERBOSE) {
					System.out.printf("(%s %d) ", on, ed);
				}

				if (ed < edMin) {
					edMin = ed;
					idxMin = i;

					nMin = 1;
				} else if (ed == edMin) {
					nMin++;
				}

			}

			if (VERBOSE) {
				System.out.printf("\nbest: %d %d %s\n", edMin, idxMin,
						ma.names[idxMin]);
			}

			if (removeSuffix > 0) {
				qn = qn.substring(0, qn.length() - removeSuffix);
			}

			LN reftreePruned = LN.deepClone(reftree);
			LN[] opb = LN.removeTaxon(reftreePruned, qn);

			// String[] ois = splitmap.get(qn);
			String nnName = ma.names[idxMin];

			if (false) {
				System.out.printf("%s\n%s\n", qs, ma.data[idxMin]);
			}

			LN[] list = LN.getAsList(reftreePruned);

			for (LN n : list) {
				if (n.data.isTip(nnName)) {

					int wc = 0;

					n = LN.getTowardsTree(n);

					LN[] ipb = { n, n.back };

					int[] fuck = { 0, 0 };
					double lenOT = ClassifierLTree.getPathLenBranchToBranch(
							opb, ipb, 0.5, fuck);
					int ndOT = fuck[0];
					if (seq != null && gap != null) {
						System.out.printf(
								"%s\t%s\t%d\t%f\t%f\t%d\t%d\t%s\t%s\n", seq,
								gap, ndOT, lenOT, lenOT / reftreeDiameter,
								nMin, edMin, qn, qnOrig);
					} else {
						System.out.printf("%d %f\n", ndOT, lenOT);
					}

					break;
				}
			}

		}
	}

	static void wellDoItBootstrap(File treefile, File phyfile, String seq,
			String gap, int removeSuffix) {
		LN tree;
		{
			TreeParser tp = new TreeParser(treefile);
			tree = tp.parse();
		}

		Set<String> taxonSet = new HashSet<String>();

		{
			LN[] list = LN.getAsList(tree);
			for (LN n : list) {
				if (n.data.isTip) {
					taxonSet.add(n.data.getTipName());
				}
			}
		}

		MultipleAlignment ma;
		try {
			ma = MultipleAlignment.loadPhylip(GZStreamAdaptor.open(phyfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("bailing out");
		}

		ArrayList<String> queryNames = new ArrayList<String>();
		Set<String> qnSet = new HashSet<String>();
		for (String name : ma.names) {
			if (!taxonSet.contains(name)) {
				queryNames.add(name);
				qnSet.add(name);
				// System.out.printf( "add query: %s\n", name );
			}
		}

		// System.out.printf( "n tree: %d, n ma: %d\n", taxonSet.size(),
		// ma.names.length );

		final int N_REP = 100;

		for (String qn : queryNames) {

			String qnOrig = qn;

			if (VERBOSE) {
				System.out.printf("query: %s\n", qn);
			}

			String qs = ma.getSequence(qn);

			int[] ngm = nonGapMap(qs);

			int[] dist_bs = new int[N_REP];

			if (removeSuffix > 0) {
				qn = qn.substring(0, qn.length() - removeSuffix);
			}
			LN reftreePruned = LN.deepClone(reftree);
			LN[] opb = LN.removeTaxon(reftreePruned, qn);

			for (int bs = 0; bs < N_REP; bs++) {
				int[] ngm_bs = bsSample(ngm);

				int edMin = Integer.MAX_VALUE;
				int idxMin = -1;
				int nMin = 0;
				for (int i = 0; i < ma.names.length; i++) {
					String on = ma.names[i];
					if (qnSet.contains(on)) {
						// System.out.printf( "drop: %s\n", on );
						continue;
					}

					String os = ma.getSequence(i);

					int ed = editDist_explicit(qs, os, ngm_bs);
					// int ed = editDist_nogaps(qs, os);
					if (VERBOSE) {
						System.out.printf("(%s %d) ", on, ed);
					}

					if (ed < edMin) {
						edMin = ed;
						idxMin = i;

						nMin = 1;
					} else if (ed == edMin) {
						nMin++;
					}

				}

				if (VERBOSE) {
					System.out.printf("\nbest: %d %d %s\n", edMin, idxMin,
							ma.names[idxMin]);
				}

				// String[] ois = splitmap.get(qn);
				String nnName = ma.names[idxMin];

				LN[] list = LN.getAsList(reftreePruned);

				for (LN n : list) {
					if (n.data.isTip(nnName)) {

						n = LN.getTowardsTree(n);

						LN[] ipb = { n, n.back };

						int[] fuck = { 0, 0 };
						double lenOT = ClassifierLTree
								.getPathLenBranchToBranch(opb, ipb, 0.5, fuck);

						dist_bs[bs] = fuck[0];

						// int ndOT = fuck[0];
						// if( seq != null && gap != null ) {
						// System.out.printf(
						// "%s\t%s\t%d\t%f\t%f\t%d\t%d\t%s\t%s\n", seq, gap,
						// ndOT, lenOT, lenOT/reftreeDiameter, nMin, edMin, qn,
						// qnOrig );
						// } else {
						// System.out.printf( "%d %f\n", ndOT, lenOT );
						// }

						break;
					}
				}

			}

			double ndOT = rmsd(dist_bs);
			System.out.printf("%s\t%s\t%f\t%f\t%f\t%d\t%d\t%s\t%s\n", seq, gap,
					ndOT, 0.0, 0.0, 0, 0, qn, qnOrig);
		}
	}

	private static int[] bsSample(int[] ngm) {
		int[] bs_ngm = new int[ngm.length];

		for (int i = 0; i < bs_ngm.length; i++) {
			bs_ngm[i] = ngm[rnd.nextInt(ngm.length)];
		}
		return bs_ngm;
	}

	static double rmsd(int[] dist) {
		double sum_squares = 0.0;

		for (int _d : dist) {
			double d = (double) _d;
			sum_squares += (d * d);
		}

		double mean_squares = sum_squares / dist.length;
		return Math.sqrt(mean_squares);
	}

	// private static void wellDoIt( File treefile, File phyfile, String[] seq )
	// {
	// LN tree;
	// {
	// TreeParser tp = new TreeParser( treefile );
	// tree = tp.parse();
	// }
	//		
	// Set<String> taxonSet = new HashSet<String>();
	//		
	//		
	// {
	// LN[] list = LN.getAsList(tree);
	// for( LN n : list ) {
	// if( n.data.isTip ) {
	// taxonSet.add( n.data.getTipName());
	// }
	// }
	// }
	//		
	//		
	//		
	//		
	// MultipleAlignment ma;
	// try {
	// ma = MultipleAlignment.loadPhylip(GZStreamAdaptor.open(phyfile));
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw new RuntimeException( "bailing out");
	// }
	//		
	//
	//		
	// ArrayList<String> queryNames = new ArrayList<String>();
	// for( String name : ma.names ) {
	// if( !taxonSet.contains(name)) {
	// queryNames.add( name );
	// }
	// }
	//		
	// // System.out.printf( "n tree: %d, n ma: %d\n", taxonSet.size(),
	// ma.names.length );
	//		
	// for( String qn : queryNames ) {
	//			
	// if( VERBOSE ) {
	// System.out.printf( "query: %s\n", qn );
	// }
	//			
	// String qs = ma.getSequence(qn);
	//			
	// int edMin = Integer.MAX_VALUE;
	// int idxMin = -1;
	// int nMin = 0;
	// for( int i = 0; i < ma.names.length; i++ ) {
	// String on = ma.names[i];
	// if( qn.equals(on)) {
	// continue;
	// }
	//				
	// String os = ma.getSequence(i);
	//				
	// int ed = editDist_nogaps(qs, os);
	// if( VERBOSE ) {
	// System.out.printf( "(%s %d) ", on, ed );
	// }
	//				
	// if( ed < edMin ) {
	// edMin = ed;
	// idxMin = i;
	//					
	// nMin = 1;
	// } else if( ed == edMin ) {
	// nMin++;
	// }
	//				
	// }
	//			
	// if( VERBOSE ) {
	// System.out.printf( "\nbest: %d %d %s\n", edMin, idxMin, ma.names[idxMin]
	// );
	// }
	//
	// LN reftreePruned = LN.deepClone(reftree);
	// LN[] opb = LN.removeTaxon(reftreePruned, qn);
	//		
	// //String[] ois = splitmap.get(qn);
	// String nnName = ma.names[idxMin];
	//		    
	// if( false ) {
	// System.out.printf( "%s\n%s\n", qs, ma.data[idxMin] );
	// }
	//		    
	// LN[] list = LN.getAsList(reftreePruned);
	//		    
	// for( LN n : list ) {
	// if( n.data.isTip( nnName )) {
	//		    		
	// int wc = 0;
	//		    		
	// n = LN.getTowardsTree(n);
	//		    		
	// LN[] ipb = {n, n.back};
	//		    		
	// int[] fuck = {0, 0};
	// double lenOT = ClassifierLTree.getPathLenBranchToBranch(opb, ipb, 0.5,
	// fuck);
	// int ndOT = fuck[0];
	// if( seq != null && gap != null ) {
	// System.out.printf( "%s\t%s\t%d\t%f\t%f\t%d\t%d\t%s\n", seq, gap, ndOT,
	// lenOT, lenOT/reftreeDiameter, nMin, edMin, qn );
	// } else {
	// System.out.printf( "%d %f\n", ndOT, lenOT );
	// }
	//		    		
	// break;
	// }
	// }
	//			
	// }
	// }
	//	
	private static String padzero(int nc, int n) {
		String s = "" + n;
		while (s.length() < nc) {
			s = "0" + s;
		}
		return s;
	}

	public static int editDist(String qs, String os) {
		if (qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}

		int ed = 0;
		for (int i = 0; i < qs.length(); i++) {

			if (qs.charAt(i) != '-' && qs.charAt(i) != os.charAt(i)) {
				ed += 3;
			} else if (qs.charAt(i) == '-') {
				if (i <= 0 || qs.charAt(i - 1) != '-') {
					ed += 3;
				} else {
					ed++;
				}
			}
		}

		return ed;
	}

	public static int editDist_affine(String qs, String os) {
		if (qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}

		int ed = 0;

		boolean qge = false;
		boolean oge = false;
		for (int i = 0; i < qs.length(); i++) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);

			boolean qg = isGapCharacter(qc);
			boolean og = isGapCharacter(oc);
			if (!true) {
				if (!qg && qc != oc) {
					ed += 3;
				} else if (qg) {
					if (!qge) {
						ed += 3;
					} else {
						ed += 1;
					}
				}

				// if( qc != oc ) {
				//					
				// if( qg ) {
				// if( !qge ) {
				// ed+=3;
				// } else {
				// ed += 1;
				// }
				// } else {
				// ed += 3;
				// }
				// }

			} else {
				if (qc != oc) {
					// we have a mismatch
					// check if we have (one-sided) gap

					if (qg) {
						// gap in the query sequence, check if we extend an
						// existing gap
						if (qge) {
							ed += 1;
						} else {
							ed += 3;
						}
					} else if (og) {
						// d.t.o for the original sequence
						if (oge) {
							ed += 1;
						} else {
							ed += 3;
						}
					} else {
						// no gap. use normal mismatch penalty
						ed += 3;
					}
				} else if (qg) {
					// gap vs. gap. penalize slightly
					ed += 1;
				}
			}
			// store current gap state for next position
			qge = qg;
			oge = og;

		}

		return ed;
	}

	static int[] nonGapMap(String qs) {
		int l = qs.length();
		int nng = 0;
		for (int i = 0; i < l; i++) {
			if (qs.charAt(i) != '-') {
				nng++;
			}
		}

		int[] ngm = new int[nng];

		for (int i = 0, j = 0; i < l; i++) {
			if (qs.charAt(i) != '-') {
				ngm[j] = i;
				j++;
			}
		}

		return ngm;
	}

	public static int editDist_explicit(String qs, String os, int[] pos) {
		int ed = 0;

		for (int p : pos) {
			char osc = os.charAt(p);
			if (qs.charAt(p) != osc) {
				ed++;
			}
		}

		return ed;
	}

	public static int editDist_nogaps(String qs, String os) {
		if (qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}

		int ed = 0;

		for (int i = 0; i < qs.length(); i++) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);

			if (qc != '-' && oc != '-' && qc != oc) {
				ed += 1;
			}
		}

		return ed;
	}

	public static int editDist_withgaps(String qs, String os) {

		// if( true ) {
		//			
		// throw new RuntimeException( "bla");
		// }
		if (qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}

		int ed = 0;

		for (int i = 0; i < qs.length(); i++) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);

			if (qc != oc) {
				ed += 1;
			}
		}

		return ed;
	}

	// private static int editDist(String qs, String os) {
	// if( qs.length() != os.length()) {
	// throw new RuntimeException("qs.length() != os.length()");
	// }
	//		
	// int ed = 0;
	// for( int i = 0; i < qs.length(); i++ ) {
	//			
	//			
	// if( qs.charAt(i) != '-' && qs.charAt(i) != os.charAt(i)) {
	// ed+=4;
	// } else if( qs.charAt(i) == '-' ) {
	// if( i <= 0 || qs.charAt(i-1) != '-' ) {
	// ed+=3;
	// } else {
	// ed++;
	// }
	// }
	// }
	//		
	// return ed;
	// }

	private static int editDist_bad(String qs, String os) {
		if (qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}

		int ed = 0;
		for (int i = 0; i < qs.length(); i++) {

			if (qs.charAt(i) != os.charAt(i)) {
				ed++;
			}
		}

		return ed;
	}

	private static boolean isGapCharacter(char c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}
}
