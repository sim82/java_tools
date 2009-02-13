/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


class ReductionResult {

	String taxon;
	String taxonNeighbor;

	LN nl;
	LN nr;
}

/**
 *
 * @author sim
 */
public class FindMinSupport {

	public static Random rand = new Random();

	public static ReductionResult createNThReducedTree(LN n, int num) {
		LN[] nodelist = LN.getAsList(n);

		System.out.printf("nodes: %d\n", nodelist.length);

		int nTT = 0;
		int i = 0;

        //if( true ) {
        for (LN node : nodelist) {
            int nt = numTips(node);

            if (node.data.getSupport() < 100.0) {
                continue;
            }

            if (nt == 2) {
                nTT++;


                String[] tn = getTipNames(node);

                assert (tn.length == 2);

                System.out.printf("%s %f (%s %s): %d\n", node.data, node.data.getSupport(), tn[0], tn[1], nt);

                LN tnt = LN.getTowardsNonTip(node);
                if( tnt.back.data.isTip ) {
                    throw new RuntimeException("tnt.back.data.isTip is true");
                }

                //String ret[] = new String[2];
				ReductionResult ret = null;
                //int c = 2;
                if (i == num) {
					ret = new ReductionResult();
					ret.taxon = tn[1];
					ret.taxonNeighbor = tn[0];
					ret.nl = tnt.back;
					ret.nr = tnt.next.back;

                    tnt.back.back = tnt.next.back;
                    tnt.next.back.back = tnt.back;



//                    ret[0] = tn[1];
//                    ret[1] = tn[0];
                    //return tn[1];
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

//                    ret[0] = tn[0];
//                    ret[1] = tn[1];
                    return ret;

                    //return tn[0];
                }
                i++;
            }



        }
        //}

        // find cases where a single tip has two neighboring brnaches with 100 % support
        // this is done in an extra loop to keep the sorting order compatible with the old version
		for (LN node : nodelist) {
			int nt = numTips(node);

            if( nt == 1 ) {
                LN tt = LN.getTowardsTip(node);

//                if( tt.back.data.getTipName().equals("poly914")) {
//                    System.out.printf( "poly914\n" );
//                }

                if( !tt.next.back.data.isTip && !tt.next.next.back.data.isTip && tt.next.backSupport >= 100.0 && tt.next.next.backSupport >= 100.0 ) {
                    if( i == num ) {

                        //String ret[] = new String[2];

						ReductionResult ret = new ReductionResult();
						ret.taxon = tt.back.data.getTipName();
						ret.taxonNeighbor = null;
						ret.nl = tt.next.back;
						ret.nr = tt.next.next.back;

                        // remove the current node (and the branch toward the tip) by retwiddling of the other two nodes
                        tt.next.back.back = tt.next.next.back;
                        tt.next.next.back.back = tt.next.back;

//                        ret[0] = tt.back.data.getTipName();
//                        ret[1] = null;

                        return ret;

                    }

                    i++;
                }
            }

        }
		return null;
	}

	public static void main(String[] args) {
//		String[] inlist = {"RAxML_bipartitions.125.BEST.WITH", "RAxML_bipartitions.1908.BEST.WITH", "RAxML_bipartitions.354.BEST.WITH", "RAxML_bipartitions.59.BEST.WITH", "RAxML_bipartitions.855.BEST.WITH",
//			"RAxML_bipartitions.140.BEST.WITH", "RAxML_bipartitions.2000.BEST.WITH", "RAxML_bipartitions.404.BEST.WITH", "RAxML_bipartitions.628.BEST.WITH", "RAxML_bipartitions.8.BEST.WITH",
//			"RAxML_bipartitions.150.BEST.WITH", "RAxML_bipartitions.217.BEST.WITH", "RAxML_bipartitions.500.BEST.WITH", "RAxML_bipartitions.714.BEST.WITH",
//			"RAxML_bipartitions.1604.BEST.WITH", "RAxML_bipartitions.218.BEST.WITH", "RAxML_bipartitions.53.BEST.WITH", "RAxML_bipartitions.81.BEST.WITH"};
//
//
//		for( String filename : inlist ) {
//			createReducedTrees(filename);
//		}



		//    createLeastGappySubseq("---abc-ded-f--gi-jkl", 4);

//	createReducedTrees("RAxML_bipartitions.855.BEST.WITH", "855");
//	createReducedTrees("RAxML_bipartitions.150.BEST.WITH", "150");
//	createReducedTrees("RAxML_bipartitions.628.BEST.WITH", "628");
    createReducedTrees("RAxML_bipartitions.714.BEST.WITH", "714");
        //createReducedTrees("RAxML_bipartitions.2000.BEST.WITH", "2000");
        //       createReducedTrees("RAxML_bipartitions.150.BEST.WITH", "150" );
	//createReducedTrees("RAxML_bipartitions.354.BEST.WITH", "354" );

	}

	public static void createReducedTrees(String filename, String alignName) {
		File basedir = new File("/space/raxml/VINCENT/");
		File alignmentdir = new File("/space/raxml/VINCENT/DATA");

		File outdir = new File("/space/redtree");
		File degen_alignoutdir = new File("/space/degen_alignments");
		File subseq_alignoutdir = new File("/space/subseq_alignments");

        PrintStream realNeighborFile;
        PrintStream numberToTaxonFile;
        try {
            realNeighborFile = new PrintStream(new FileOutputStream(new File(degen_alignoutdir, "real_neighbors_" + alignName + ".txt")));
            numberToTaxonFile = new PrintStream(new FileOutputStream(new File(degen_alignoutdir, "number_to_taxon_" + alignName + ".txt")));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FindMinSupport.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("bailing out");
        }

		for (int i = 0;; i++) {
			File f = new File(basedir, filename);

			TreeParser tp = new TreeParser(f);


			LN n = tp.parse();

			//String[] taxonAN = createNThReducedTree(n, i);

			ReductionResult res = createNThReducedTree(n, i);

            

            

			if (res == null) {
				System.out.printf("finished after %d trees\n", i);
				break;
			}
			String taxon = res.taxon;
            

            numberToTaxonFile.printf( "%s\t%s\n", padchar("" + i, '0', 4), taxon );




			try {
				File outfile = new File(outdir, filename + "_" + padchar("" + i, '0', 4));

				PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
				TreePrinter.printRaw(n, ps);
				ps.close();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(FindMinSupport.class.getName()).log(Level.SEVERE, null, ex);
			}
			System.out.printf("dropped taxon: %s\n", taxon);


			for (int j = 0; j < 50; j += 10) {
				createDegeneratedAlignment(new File(alignmentdir, alignName), new File(degen_alignoutdir, alignName + "_" + padchar("" + i, '0', 4) + "_" + j), taxon, j);
			}
//
//			int ssLen = 500;
//			createSubseqAlignment(new File(alignmentdir, alignName), new File(subseq_alignoutdir, alignName + "_" + padchar("" + i, '0', 4) + "_" + ssLen), taxon, ssLen);


			
			// find the split induced by the tip position, to identify the insertion position in the reduced tree
			String[] ssl; // smaller split list
			{
				LN[] ll = LN.getAsList(res.nl, false);
				LN[] lr = LN.getAsList(res.nr, false);

				Set<String> sl = LN.getTipSet(ll);
				Set<String> sr = LN.getTipSet(lr);

				Set<String> smallset = (sl.size() <= sr.size()) ? sl : sr;

				ssl = new String[smallset.size()];
				ssl = smallset.toArray(ssl);
				Arrays.sort(ssl);
			}
				
			
			if( res.taxonNeighbor != null ) {
                realNeighborFile.println( padchar("" + i, '0', 4) + "\t" + taxon + "\t" + res.taxonNeighbor + "\t" + commaSeparatedList(ssl));
            } else {
                realNeighborFile.println( padchar("" + i, '0', 4) + "\t" + taxon + "\t" + "*NONE*" + "\t" + commaSeparatedList(ssl));
            }

		}

        realNeighborFile.close();
        numberToTaxonFile.close();
	//System.out.printf( "nTT: %d\n", nTT );
	}

	private static String commaSeparatedList(String[] ssl) {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < ssl.length; i++ ) {
			sb.append(ssl[i]);
			if( i != ssl.length - 1 ) {
				sb.append(",");
			}
		}

		return sb.toString();
	}

	
	private static String createLeastGappySubseq(String seq, int length) {
		int[] nm = getNonGapCharacterMap(seq);
		if (nm.length < length) {
			throw new RuntimeException("less than " + length + " non-gap characters in sequence");
		}

		int maxStartPos = nm.length - length + 1;

		int[] gapsByStartpos = new int[maxStartPos];

		int minGaps = Integer.MAX_VALUE;

		for (int i = 0; i < maxStartPos; i++) {
			int numGaps = 0;


			for (int j = i; j < i + length - 1; j++) {
				numGaps += nm[j + 1] - nm[j] - 1;
			}

			gapsByStartpos[i] = numGaps;

			minGaps = Math.min(minGaps, numGaps);
		//System.out.printf( "start pos: %d: %d\n", i, numGaps );
		}

		if (minGaps == Integer.MAX_VALUE) {
			throw new RuntimeException("could not find any start position with less than infinite gaps (which should not be possible ...)");
		}



		ArrayList<Integer> minPosList = new ArrayList<Integer>();

		for (int i = 0; i < maxStartPos; i++) {
			if (gapsByStartpos[i] == minGaps) {
				minPosList.add(i);
			}
		}

		System.out.printf("best possible subseq has %d gaps (%d alternatives)\n", minGaps, minPosList.size());
		assert (minPosList.size() >= 1);

		int sp = minPosList.get(rand.nextInt(minPosList.size()));
		int ep = sp + length - 1;

		int rsp = nm[sp];
		int rep = nm[ep] + 1;


		String sub = repchar('-', rsp) + seq.substring(rsp, rep) + repchar('-', seq.length() - rep);
		System.out.printf("'%s' =>\n'%s'\n", seq, sub);

		return sub;
	}

	private static String repchar(char c, int n) {
		StringBuffer sb = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			sb.append(c);
		}

		return sb.toString();
	}

	private static int[] getNonGapCharacterMap(String seq) {
		int num = 0;
		for (int i = 0; i < seq.length(); i++) {
			if (!isGapCharacter(seq.charAt(i))) {
				num++;
			}
		}

		int[] map = new int[num];
		num = 0;

		for (int i = 0; i < seq.length(); i++) {
			if (!isGapCharacter(seq.charAt(i))) {
				map[num++] = i;
			}
		}
		return map;
	}

	private static void createDegeneratedAlignment(File infile, File outfile, String taxon, int dp) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);
		String seq = ma.getSequence(taxon);

		String degseq = createDegeneratedSequence(seq, dp / 100.0f);
		ma.replaceSequence(taxon, degseq);

		ma.writePhylip(outfile);
	}

	private static String createDegeneratedSequence(String seq, float f) {
		int[] ngm = getNonGapCharacterMap(seq);
		int ngmsize = ngm.length;
		char[] sa = seq.toCharArray();

		int numgaps = (int) (ngm.length * f);



		for (int i = 0; i < numgaps; i++) {
			int n = rand.nextInt(ngmsize);

			int r = ngm[n];
			sa[r] = '-';

			ngm[n] = ngm[ngmsize - 1];
			ngmsize--;
		}

		return new String(sa);
	}

	private static void createSubseqAlignment(File infile, File outfile, String taxon, int length) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(infile);
		String seq = ma.getSequence(taxon);

		String degseq = createLeastGappySubseq(seq, length);
		ma.replaceSequence(taxon, degseq);

		ma.writePhylip(outfile);
	}

	

	static int numTips(LN n) {
		if (n.data.isTip) {
			return 0;
		} else {
			LN start = n;
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

	
//	static LN getTowardsTree( LN n ) {
//		if( !n.data.isTip ) {
//			throw new RuntimeException( "parameter is not a tip" );
//		}
//
//		for( int i = 0; i < 3; i++, n = n.next ) {
//			if( n.back != null ) {
//				return n;
//			}
//		}
//
//		throw new RuntimeException( "at tip: could not find LN pointing towards the tree" );
//	}

	static String[] getTipNames(LN n) {
		ArrayList<String> tipnames = new ArrayList<String>();

		LN start = n;
		LN cur = n.next;
		int nTips = 0;

		while (cur != start) {
			if (cur.back.data.isTip) {
				tipnames.add(cur.back.data.getTipName());
			}
			cur = cur.next;
		}

		String[] ra = new String[tipnames.size()];


		for (int i = 0; i < tipnames.size(); i++) {
			ra[i] = tipnames.get(i);
		}

		return ra;
	}

	private static boolean isGapCharacter(char c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}

	private static String padchar(String string, char c, int num) {
		while (string.length() < num) {
			string = c + string;
		}
		return string;
	}
}
