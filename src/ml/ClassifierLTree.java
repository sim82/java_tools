/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sim
 */
public class ClassifierLTree {
    public static void main( String[] args ) {

        File dir = new File( args[0] );

        File rnFile = new File( args[1] );

        Map<String,String> rnm = parseRealNeighbors( rnFile );
		Map<String,String[]> splitmap = parseSplits( rnFile );

        File oltFile = new File( dir, "RAxML_originalLabelledTree." + args[2] );
        File classFile = new File( dir, "RAxML_classification." + args[2] );
        File originalFile = new File( dir, args[3] );

        TreeParser tp = new TreeParser(oltFile);
        LN n = tp.parse();
        LN[] lnl = LN.getAsList(n);
        try {

            BufferedReader r = new BufferedReader(new FileReader(classFile));


            String line;

            while( ( line = r.readLine() ) != null ) {
                try {
                    StringTokenizer ts = new StringTokenizer(line);

                    String seq = ts.nextToken();
                    String branch = ts.nextToken();
                    String supports = ts.nextToken();

                    int support = Integer.parseInt(supports);

					System.out.printf( "seq: '%s'\n", seq );

                    


//					if( false ) {
//						String realNeighbor = rnm.get(seq);
//						assert( realNeighbor != null );
//
//						LN rnTip = findTip( lnl, realNeighbor );
//
//						if( rnTip == null ) {
//							throw new RuntimeException("could not find LN for tip '" + realNeighbor  + "'");
//						}
//
//						double len = getPathLenToNamedBranch(rnTip, branch);
//
//
//
//	//                    LN neighborTip = findTipWithNamedBranch( lnl, branch );
//	//
//	//                    System.out.printf( "%s %s %d %s %s %f\n", seq, branch, support, realNeighbor, neighborTip.data.getTipName(), len );
//						System.out.printf( "%s %s %d %s %f\n", seq, branch, support, realNeighbor, len );
//					} else {
                    String[] split = splitmap.get(seq);
                    LN[] realBranch = LN.findBranchBySplit(n, split);


                    double lenOT;
                    {
                        TreeParser tpo = new TreeParser(oltFile);
                        LN origTree = tpo.parse();
                        
                        // original position branch
                        LN[] opb = origTree.removeTaxon(origTree, seq);

                        // origTree can never be removed by removeTaxon so it's ok to use it as pseudo root
                        LN[] ipb = LN.findBranchBySplit(origTree, split);

                        double lenOT0 = getBranchBranchDistance(opb[0], ipb);
                        double lenOT1 = getBranchBranchDistance(opb[1], ipb);

                        lenOT = Math.max( lenOT0, lenOT1 );
                        
                    }
                    // get path len between real position and current insertion position
                    double len = getPathLenToNamedBranch(realBranch[0], branch, false);
                    if( len < 0 ) {
                        len = getPathLenToNamedBranch(realBranch[1], branch, false);
                    }


                    System.out.printf( "%s %s %s %d %f %f\n", seq, branch, realBranch[0].backLabel, support, len, lenOT );
                    //System.out.printf( "branch: %s '%s' '%s'\n", b[0].backLabel, b[0].data.isTip ? b[0].data.getTipName() : "*NOTIP*", b[1].data.isTip ? b[1].data.getTipName() : "*NOTIP*");

//					}

                } catch (NoSuchElementException x) {
                    System.out.printf( "bad line in raxml classifier output: " + line );
                    x.printStackTrace();

                    throw new RuntimeException( "bailing out" );

                }
                
            }

        } catch (IOException ex) {
            Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("bailing out");
        }



    }

    private static LN[] expandToAllNodes(LN[] lnl) {
        LN[] lnlx = new LN[lnl.length * 3];

        int pos = 0;
        for( LN ln : lnl ) {
            lnlx[pos++] = ln;
            lnlx[pos++] = ln.next;
            lnlx[pos++] = ln.next.next;
        }

        return lnlx;
    }

    private static LN findTipWithNamedBranch(LN[] lnl, String branch) {
        System.out.printf( "find: %s %d\n", branch, lnl.length );

        LN[] lnlx = expandToAllNodes( lnl );

        for( LN ln : lnlx ) {
            if( ln.back != null ) {
                System.out.printf( "(%s %s)\n", ln.backLabel, ln.data.isTip );
            }

            if( ln.back!= null && ln.data.isTip && ln.backLabel.equals(branch)) {
                return ln;
            }
        }
        System.out.printf( "\n" );
        return null;
    }

    static boolean branchEquals( LN[] b1, LN[] b2 ) {
        return (b1[0].data.serial == b2[0].data.serial && b1[1].data.serial == b2[1].data.serial) ||
               (b1[0].data.serial == b2[1].data.serial && b1[1].data.serial == b2[0].data.serial);
    }

    private static double getBranchBranchDistance(LN n, LN[] b) {
        if( n.data.serial == b[0].data.serial || n.data.serial == b[1].data.serial ) {
            return 0.0;
        } else {
            double len = getBranchBranchDistance(n.next.back, b);
            if( len >= 0 ) {
                return len + n.next.backLen;
            } else {
                len = getBranchBranchDistance(n.next.next.back, b);

                if( len >= 0 ) {
                    return len + n.next.next.backLen;
                }
            }

            return -1;

        }
    }

    private static Map<String, String> parseRealNeighbors(File rnFile) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(rnFile));

            Map<String,String> map = new HashMap<String, String>();


            String line;

            while( (line = r.readLine()) != null ) {


                try {
                    StringTokenizer st = new StringTokenizer(line);
					String seq = st.nextToken();
                    String k = st.nextToken();
                    String v = st.nextToken();

                    map.put(k,v);
                } catch( NoSuchElementException x ) {

                    System.out.printf( "bad line in tsv file: " + line );
                    x.printStackTrace();
                    throw new RuntimeException("bailing out");
                }
            }

            r.close();

//			for( Map.Entry<String,String> e : map.entrySet() ) {
//				System.out.printf( "rnm: '%s' => '%s'\n", e.getKey(), e.getValue() );
//
//			}

            return map;

        } catch (IOException ex) {
            Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);

            throw new RuntimeException( "bailing out");
        }
    }

	private static Map<String, String[]> parseSplits(File rnFile) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(rnFile));

            Map<String,String[]> map = new HashMap<String, String[]>();


            String line;

            while( (line = r.readLine()) != null ) {


                try {
                    StringTokenizer st = new StringTokenizer(line);
					String seq = st.nextToken();
                    String k = st.nextToken();
					st.nextToken(); // skip the 'real neighbor'
                    String v = st.nextToken();


					//
					// parse the 'split list' (comma separated list of tips names)
					//

					StringTokenizer st2 = new StringTokenizer(v, ",");
					String tip;
					
					int num = st2.countTokens();
					if( num < 1 ) {
						throw new RuntimeException("could not parse split list from real neighbor file");
					}

					String[] split = new String[num];


//					System.out.printf( "split: %s\n", k );
					for( int i = 0; i < num; i++ ) {
						split[i] = st2.nextToken();
//						System.out.printf( " '%s'", split[i] );
					}
					//split[num] = k;
//					System.out.println();
					
                    map.put(k,split);
                } catch( NoSuchElementException x ) {

                    System.out.printf( "bad line in tsv file: " + line );
                    x.printStackTrace();
                    throw new RuntimeException("bailing out");
                }
            }

            r.close();

//			for( Map.Entry<String,String> e : map.entrySet() ) {
//				System.out.printf( "rnm: '%s' => '%s'\n", e.getKey(), e.getValue() );
//
//			}

            return map;

        } catch (IOException ex) {
            Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);

            throw new RuntimeException( "bailing out");
        }
    }

//    public static LN findTip( LN start, String name ) {
//        if( start.data.isTip ) {
//
//            if(start.data.getTipName().equals(name)) {
//                return getTowardsTree(start);
//            } else {
//                return null;
//            }
//
//
//        } else {
//            LN r = findTip(start.next.back, name);
//            if( r != null ) {
//                return r;
//            }
//
//            return findTip(start.next.next.back, name);
//        }
//    }


    public static LN findTip( LN[] list, String name ) {
        //System.out.printf( "list size: %d\n", list.length );
        for( LN ln : list ) {
//            if( ln.data.isTip) {
//                System.out.printf( "tip: %s\n", ln.data.getTipName() );
//            }

            if( ln.data.isTip && ln.data.getTipName().equals(name)) {
                return LN.getTowardsTree(ln);
            }
        }

        return null;
    }

    public static double getPathLenToNamedBranch( LN node, String name) {
        return getPathLenToNamedBranch(node, name, true);
    }
    public static double getPathLenToNamedBranch( LN node, String name, boolean back ) {
        
        if( node.backLabel.equals(name)) {
            return 0.0;
        }
        if( back && node.back != null ) {
            double len = getPathLenToNamedBranch(node.back, name, false);

            if( len >= 0 ) {
                return len + node.backLen;
            }
        }
        if( node.next.back != null ) {
            double len = getPathLenToNamedBranch(node.next.back, name, false);

            if( len >= 0 ) {
                return len + node.next.backLen;
            }
        }
        if( node.next.next.back != null ) {
            double len = getPathLenToNamedBranch(node.next.next.back, name, false);

            if( len >= 0 ) {
                return len + node.next.next.backLen;
            }
        }

        return -1;

    }

    
}
