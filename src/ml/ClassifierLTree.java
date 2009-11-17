/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ml;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassifierLTree {
	/**
	 *
	 * @author sim
	 */
	static boolean DO_PRUNE = true;
	static class TreeWithSplitCache {
		LN tree;	
		Map<String, LN[]> splitMap = new HashMap<String, LN[]>();
		Map<String, LN[]> branchMap = new HashMap<String, LN[]>();
		Map<String, String[]> branchSplitMap = new HashMap<String, String[]>();
		
		
		public TreeWithSplitCache( LN tree ) {
			this.tree = tree;
		}
		
		LN[] getBranchBySplit( String[] split ) {
			String qs = "";
		
			//Arrays.sort(split);
			
			for( String s : split ) {
				qs += s + ":";
			}
			
			LN[] branch = splitMap.get(qs);
			
			if( branch == null ) {
				branch = LN.findBranchBySplit(tree, split);
				splitMap.put( qs, branch );
			}
			
			return branch;
		}
		
		LN[] getBranchByName( String name ) {
			LN[] branch = branchMap.get(name);
			
			
			if( branch == null ) {
				branch = LN.findBranchByName(tree, name);
				branchMap.put(name, branch);
			}
			
			return branch;
		}

		public String[] getSplitByBranchName(String branch) {
			//System.out.printf( "%s\n", branch );
			
			String[] insertSplit = branchSplitMap.get(branch);
			
			if( insertSplit == null ) {
				//LN[] insertBranch = LN.findBranchByName( oltree, branch );
				LN[] insertBranch = getBranchByName( branch );
	
				
				// FIXME: there now is a method in Ln to do this
				
				LN[] ll = LN.getAsList(insertBranch[0], false);
				LN[] lr = LN.getAsList(insertBranch[1], false);
	
				Set<String> sl = LN.getTipSet(ll);
				Set<String> sr = LN.getTipSet(lr);
	
				Set<String> smallset = (sl.size() <= sr.size()) ? sl : sr;
	
				insertSplit = new String[smallset.size()];
				insertSplit = smallset.toArray(insertSplit);
				Arrays.sort(insertSplit);
				
				branchSplitMap.put( branch, insertSplit);
			} else {
				//System.out.printf( "hit\n" );
			}
			return insertSplit;
		}
	}
	
	class PrunedReftree {
		String taxon;
		
		LN[] obp;
		TreeWithSplitCache tsc;
		
		
		PrunedReftree( LN origTree, String taxon ) {
			this.taxon = taxon;
			
			LN tree = LN.deepClone(origTree);
			
			if( DO_PRUNE ) {
				obp = LN.removeTaxon(tree, taxon,false);
				tree = obp[0];
			} else {
				// horrible hack:
				obp = LN.findBranchBySplit(tree, splitmap.get( taxon ));
				assert( obp != null );
			}
			tsc = new TreeWithSplitCache(tree);
			
		}
		
		
		LN[] getOBP() {
			return obp;
		}
		
		TreeWithSplitCache getTSC() {
			return tsc;
		}
	}
	
	static class TaxonSMap {
		HashMap<String,int[]> map = new HashMap<String, int[]>();
		
		TaxonSMap( File file ) {
			try {
				BufferedReader r = new BufferedReader( new FileReader(file));
				
				String line = null;
				
				while((line = r.readLine()) != null ) {
					StringTokenizer ts = new StringTokenizer(line);
					
					String taxon = ts.nextToken();
					
					
					int nToken = ts.countTokens();
					
					int[] nm = new int[nToken];
					int i = 0;
					while( ts.hasMoreTokens() ) {
						String t = ts.nextToken();
						
						nm[i] = Integer.parseInt(t);
						i++;
					}
					
					map.put( taxon, nm );
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				throw new RuntimeException( "bailing out" );
			}
		}
		
		int[] getMap( String taxon ) {
			int[] m = map.get(taxon);
			
			if( m == null ) {
				throw new RuntimeException( "taxon not found in smap: " + taxon );
			}
			
			return m;
		}
		int maxLen() {
			int max = 0;
			
			for( int[] smap : map.values()) {
				max = Math.max(max, smap[smap.length-1]);
			}
			
			return max + 1;
		}
	}

	//final double oltDiameter;
	final double reftreeDiameter;
	final LN	reftree;
	//final LN	oltree;
	final Map<String,String> rnm;
	final Map<String,String[]> splitmap;
	
	static TaxonSMap smap = null;

	
    public static void main( String[] args ) throws FileNotFoundException, UnsupportedEncodingException {

    	
    	if( args[0].equals("--auto") || args[0].equals("--autoc")) {
    
    		// auto mode: convert all raxml output files in this directory
    		// usage:
    		// <CMD> --auto <real neighbor file> <reftree file>
    		// automode will work on all RAxML_classification* files in the cwd
    		// Output files are written to the subdir './ext'
    		
    		final boolean cutsuffix = args[0].equals("--autoc");
    		
    		File rnFile = new File( args[1] );
    		File reftreeFile = new File( args[2] );
    		
    		if( args.length > 3 ) {
    			smap = new TaxonSMap(new File(args[3]));
    		}
    		
    		ClassifierLTree clt = new ClassifierLTree(reftreeFile, rnFile );
    		
	        File cwd = new File( "." );
	        
	        
	        
	        File[] classFiles = cwd.listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("RAxML_classification." ); 
				} });
	        
	        
	        for( File classFile : classFiles ) {
	        	String suffix = classFile.getName().substring(21);
	        	System.out.printf( "cf: %s\n", classFile );
	        	File oltFile;
	        	
	        	{
	        		oltFile = new File( classFile.getParent(), "RAxML_originalLabelledTree." + suffix );
	        		
	        		if( !oltFile.canRead() ) {
	        			oltFile = new File( classFile.getParent(), "RAxML_originalLabelledTree." + suffix + ".gz" );
	        		}
	        		
	        		if( !oltFile.canRead() ) {
	        			throw new RuntimeException( "cannot find olt file (with and without .gz suffix): " + oltFile);
	        		}
	        	}
	        	
	        	//System.out.printf( "file: %s\n", classFile.getName());
	        	
	        	File outDir = new File( classFile.getParent(), "ext/" );
	
	        	if( !outDir.exists() ) {
	        		outDir.mkdir();
	        	}
	        	
				PrintStream out = new PrintStream( new BufferedOutputStream(new FileOutputStream(new File(outDir, classFile.getName() ))));
	        	try {
	        		clt.output( out, classFile, oltFile, cutsuffix );
	        	} catch( RuntimeException x ) {
	        		x.printStackTrace();
	        		System.out.printf( "error thrown at: %s %s\n", classFile.getName(), oltFile.getName() );
	        		throw new RuntimeException( "bailing out finally" );
	        	}
	        	out.close();						
		    	
	        }
	
    	} else {
	    	
    		// WARNING: setting 'no prune' mode!
    		ClassifierLTree.DO_PRUNE = false;
    		
    		// classic mode
    		
	        File dir = new File( args[0] );
	        File rnFile = new File( args[1] );
	
	        File oltFile = new File( dir, "RAxML_originalLabelledTree." + args[2] );
	        File reftreeFile = new File( args[3] );
	
	        ClassifierLTree clt = new ClassifierLTree(reftreeFile, rnFile );
	        
	        
	        File classFile = new File( dir, "RAxML_classification." + args[2] );
	
	             
	        clt.output( System.out, classFile, oltFile, false );
    	}
    }


	
	
	public ClassifierLTree( File reftreeFile, File rnFile ) {
		rnm = parseRealNeighbors( rnFile );
		splitmap = parseSplits( rnFile );
		
        
        // parse reference tree used for weighted branch difference stuff
        
        {
            TreeParser tpreftree = new TreeParser(reftreeFile);
            reftree = tpreftree.parse();
        }

// highest path weight in reference tree (=path with the highest sum of edge weights, no necessarily the longest path)
        reftreeDiameter = treeDiameter(reftree);

        // some 'anal' tests for the deep-clone stuff
//        if( false )
//        {
//            LN[] list1 = LN.getAsList(n);
//            LN nn = LN.deepClone(n);
//
//            LN[] list2 = LN.getAsList(n);
//            LN[] list3 = LN.getAsList(nn);
//
//            System.out.printf( "cmp: %s %s %s %s %s %s\n", LN.cmpLNList( list1, list2 ), LN.cmpLNList( list1, list3 ), LN.cmpLNList( list2, list3 ), LN.cmpLNListObjectIdentity( list1, list2 ), LN.cmpLNListObjectIdentity( list1, list3 ), LN.cmpLNListObjectIdentity( list2, list3 ));
//            System.out.printf( "sym: %s %s\n", LN.checkLinkSymmetry(n), LN.checkLinkSymmetry(nn) );
//        }

	}
    
	TreeWithSplitCache oltsc = null;
	File oltfile_cached = null;
//	File oltFile_cached = null;
//	LN oltree_cached;
//	double oltDiameter_cached;
	
    private void output(PrintStream out, File classFile, File oltFile, boolean cutsuffix) {
    	// parse 'original labelled tree'
//    	LN oltree;
//    	double oltDiameter;
//    	if( oltFile_cached != null && oltFile_cached.equals(oltFile)) {
//    		oltree = oltree_cached;
//    		oltDiameter = oltDiameter_cached;
//    		System.out.printf( "hit\n" );
//    	} else {
//    		System.out.printf( "miss\n" );
//	        {
//	            TreeParser tp = new TreeParser(oltFile);
//	            oltree = tp.parse();
//	        }
//	        LN[] lnl = LN.getAsList(oltree);
//	
//	        oltDiameter = treeDiameter(oltree);
//	        
//	        oltFile_cached = oltFile;
//	        oltree_cached = oltree;
//	        oltDiameter_cached = oltDiameter;
//    	} 
    	

    	//LN oltree;
    	
//    	double oltDiameter;
    	if( true || oltsc == null || !oltfile_cached.equals(oltFile))
        {
    		oltfile_cached = oltFile;
    		
            TreeParser tp = new TreeParser(oltFile);
            LN oltree = tp.parse();
            oltsc = new TreeWithSplitCache(oltree);
        }
//        LN[] lnl = LN.getAsList(oltree);

//        oltDiameter = treeDiameter(oltree);

    	try {
        	
            BufferedReader r = new BufferedReader(new FileReader(classFile));


            String line;

            PrunedReftree prt = null;
            
            while( ( line = r.readLine() ) != null ) {
                
		

                try {
                    // parse line from raxml classification output
                    StringTokenizer ts = new StringTokenizer(line);

                    // the name of the classified taxon
                    String seq = ts.nextToken();
                    
                    
                    
                    String seqOrig = seq;
                    
                    int seqSubIdx = -1; 
                    
                    int atIdx = seq.indexOf( '@' );
                    if( atIdx > 0 ) {
                    	seq = seq.substring(0, atIdx);
                    	seqSubIdx = Integer.parseInt(seqOrig.substring( atIdx + 1 ));
                    }
                   
                    // this is really getting ugly: misuse the strange 'seqSubIdx' stuff to  allow for 
                    // the removal of arbitrary suffixes connected by an underscore ...
                    // ... I'm using using ugly code here to honor the conceptual ugliness us this feature.
                    final int usIdx;
                    if( cutsuffix && (usIdx = seq.lastIndexOf("_")) > 0 ) {
                    	seq = seq.substring(0, usIdx);
                    	seqSubIdx = Integer.parseInt(seqOrig.substring( usIdx + 1 ));
                    	
                    }
              
                    
                    if( prt == null || !prt.taxon.equals(seq)) {
                    	prt = new PrunedReftree(reftree, seq);
                    }
                    
                    // name of the branch, the classifier has put the taxon in (= current insertion position)
                    String branch = ts.nextToken();

                    // boostrap support of this classification
                    String supports = ts.nextToken();
                    int support = Integer.parseInt(supports);

//////					System.out.printf( "seq: '%s'\n", seq );


                    // get the split that identifies the original insertion position
                    String[] split = splitmap.get(seq);
                    //LN[] realBranch = LN.findBranchBySplit(oltree, split);

                    LN[] realBranch = oltsc.getBranchBySplit(split);
					// find the split that identifies the current insertion position
					String[] insertSplit;
					{
						insertSplit = oltsc.getSplitByBranchName( branch );
						
					}

                    // get the weighted path length between current and original insertion position
                    // in the reference tree
                    double lenOT;
                    int ndOT;
                    //double lenOTalt;
                    {

						//System.out.printf( "diameter: %f\n", reftreeDiameter );

                    	
                    	
                        // original position branch
                        //LN reftreePruned = LN.deepClone(reftree);

                        
                        // this call has two important effects:
                        // - remove the current taxon from the reference tree (copy), so that its topology
                        //   resembles the pruned tree from the current classification
                        // - return the branch from the reference tree that corresponds to the original taxon position
                        //LN[] opb = LN.removeTaxon(reftreePruned, seq, false);
                        //reftreePruned = opb[0];

                        // identify the current insertion position from the pruned tree in the
                        // reference tree. The position is identified by the split set (or how ever this thin is called)
                        //long time1 = System.currentTimeMillis();
                        // the LN referenced by reftreePruned can (!?) never be removed by removeTaxon so it's ok to use it as pseudo root
                        
                //        System.out.printf( "split: " );
//                        for( String name : insertSplit ) {
//                        	System.out.printf( "%s ", name );
//                        }
//                        
//                        System.out.println();
                        
                        //LN[] ipb = LN.findBranchBySplit(reftreePruned, insertSplit);
                    	LN[] ipb = prt.getTSC().getBranchBySplit(insertSplit);
                        //System.out.printf( "time: %d\n", System.currentTimeMillis() - time1 );

                        // calculate weighted path length between original and current insertion branches
                        // the lengths of the current and original branches contribute half of their weight.
                        // if the branches are identical the path has zero length

                        int[] fuck = new int[1]; // some things (like 'multiple return values') are soo painful in java ...
                        lenOT = getPathLenBranchToBranch(prt.getOBP(), ipb, 0.5, fuck);
                        ndOT = fuck[0];
//                        {
//                        	double lenOT1alt = getPathLenToBranch(opb[0], ipb);
//                        	double lenOT2alt = getPathLenToBranch(opb[1], ipb);
//                        	
//                        	lenOT = Math.min(lenOT1alt, lenOT2alt);// + opb[0].backLen;
//                        	
//                        }

                       // lenOT += opb[0].backLen;
                        
                    }

                    boolean PRINT_LEGACY_STUFF = false;
                    
                    
                    if( !PRINT_LEGACY_STUFF ) {
                    	
                    	if( smap != null ) {
                    		
                    		if( seqSubIdx >= 0 ) {
	                    		int[] nm = smap.getMap(seq);
	                    		seqSubIdx = nm[seqSubIdx];
                    		}
                    		out.printf( "%s\t%s\t%s\t%d\t%d\t%f\t%f\t%f\t%d\n", seqOrig, branch, realBranch[0].backLabel, support, ndOT, lenOT, lenOT / reftreeDiameter, reftreeDiameter/*, oltDiameter */, seqSubIdx);
                    	} else {
                    		out.printf( "%s\t%s\t%s\t%d\t%d\t%f\t%f\t%f\n", seqOrig, branch, realBranch[0].backLabel, support, ndOT, lenOT, lenOT / reftreeDiameter, reftreeDiameter/*, oltDiameter */);
                           	
                    		
                    	}
                    	
                    	
                    } else {
                    	
	                    // for comparison: get the path length in the (possibly unweighted) pruned tree
	                    // get path len between real position and current insertion position
	                    
	                    
	                    double len = getPathLenToNamedBranch(realBranch[0], branch, false);
	                    if( len < 0 ) {
	                        len = getPathLenToNamedBranch(realBranch[1], branch, false);
	                    }
	                    //len += realBranch[0].backLen;
	                    
	                    
	                    int lenUW = getUnweightedPathLenToNamedBranch(realBranch[0], branch, false);
	                    if( lenUW == Integer.MAX_VALUE ) {
	                        lenUW = getUnweightedPathLenToNamedBranch(realBranch[1], branch, false);
	                    }
	
	
	    //                System.out.printf( "%s %s %s %d %d %f %f %f (%f %f)\n", seq, branch, realBranch[0].backLabel, support, lenUW, len, lenOT, lenOT / reftreeDiameter, reftreeDiameter, oltDiameter );
	                    out.printf( "%s\t%s\t%s\t%d\t%d\t%f\t%f\t%f\t%f\n", seq, branch, realBranch[0].backLabel, support, lenUW, len, lenOT, lenOT / reftreeDiameter, reftreeDiameter/*, oltDiameter*/ );
                    }

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

    static boolean belongsToBranch( LN n, LN[] b ) {
    	return (n.data.serial == b[0].data.serial) || (n.data.serial == b[1].data.serial );
    }
    
    static boolean branchEquals( LN[] b1, LN[] b2 ) {
        return (b1[0].data.serial == b2[0].data.serial && b1[1].data.serial == b2[1].data.serial) ||
               (b1[0].data.serial == b2[1].data.serial && b1[1].data.serial == b2[0].data.serial);
    }
    
    
    private static double getPathLenToBranch(LN n, LN[] b, int[] nd ) {
		if( n == null ) {
			nd[0] = -1;
			return Double.POSITIVE_INFINITY;
		}

//		System.out.printf( "bbd: %d (%d %d)\n", n.data.serial, b[0].data.serial, b[1].data.serial );

        if( belongsToBranch( n, b ) ) {
        	nd[0] = 0;
            return 0.0;
        } else {
        	{
        		double len = getPathLenToBranch(n.next.back, b, nd);
        		if( len < Double.POSITIVE_INFINITY ) {
        			nd[0]++;
        			return len + n.next.backLen;
        		} 
        	}
        	{
                double len = getPathLenToBranch(n.next.next.back, b, nd);

                if( len < Double.POSITIVE_INFINITY ) {
                	nd[0]++;
                    return len + n.next.next.backLen;
                }
            }
        	nd[0] = -1;
            return Double.POSITIVE_INFINITY;

        }
    }

    static double getPathLenBranchToBranch(LN n[], LN[] b, double seScale, int[] nodedist ) {
    	assert( n[0].backLen == n[1].backLen);
    	assert( b[0].backLen == b[1].backLen);
    	
    	if( branchEquals(n, b)) {
    		if( nodedist != null ) {
    			nodedist[0] = 0;
    		}
    		return 0.0;
    	}
    	
    	
    	int[] nd1 = new int[1];
    	int[] nd2 = new int[1];
    	
    	double len1 = getPathLenToBranch(n[0], b, nd1);
    	double len2 = getPathLenToBranch(n[1], b, nd2);
    	
    	
    	
    	double len;
    	if( len1 <= len2 ) {
    		len = len1;
    		if( nodedist != null ) {
    			nodedist[0] = nd1[0] + 1;
    		}
    	} else {
    		len = len2;
    		if( nodedist != null ) {
    			nodedist[0] = nd2[0] + 1;
    		}
    	}
    	
    	len += (n[0].backLen + b[0].backLen) * seScale;
//    	
    	return len;
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

	static Map<String, String[]> parseSplits(File rnFile) {
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
//    public static double getPathLenToNamedBranch( LN node, String name, boolean back ) {
//
//        if( node.backLabel.equals(name)) {
//            return 0.0;
//        }
//        if( back && node.back != null ) {
//            double len = getPathLenToNamedBranch(node.back, name, false);
//
//            if( len >= 0 ) {
//                return len + node.backLen;
//            }
//        }
//        if( node.next.back != null ) {
//            double len = getPathLenToNamedBranch(node.next.back, name, false);
//
//            if( len >= 0 ) {
//                return len + node.next.backLen;
//            }
//        }
//        if( node.next.next.back != null ) {
//            double len = getPathLenToNamedBranch(node.next.next.back, name, false);
//
//            if( len >= 0 ) {
//                return len + node.next.next.backLen;
//            }
//        }
//
//        return -1;
//
//    }

    public static double getPathLenToNamedBranch( LN node, String name, boolean back ) {

        if( back ) {
            throw new RuntimeException( "the 'back' flag is not supported for this opperation.bailing out." );
        }


//        if( node.backLabel.equals(name)) {
//            return 0.0;
//        }
        
        
        if( LN.hasOutgoingBranchLabel( node, name )) {
        	return 0.0;
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

    public static int getUnweightedPathLenToNamedBranch( LN node, String name, boolean back ) {

        if( back ) {
            throw new RuntimeException( "the 'back' flag is not supported for this opperation.bailing out." );
        }


        if( node.backLabel.equals(name)) {
            return 0;
        }

        if( node.next.back != null ) {
            int len = getUnweightedPathLenToNamedBranch(node.next.back, name, false);

            if( len < Integer.MAX_VALUE ) {
                return len + 1;
            }
        }
        if( node.next.next.back != null ) {
            int len = getUnweightedPathLenToNamedBranch(node.next.next.back, name, false);

            if( len < Integer.MAX_VALUE ) {
                return len + 1;
            }
        }

        return Integer.MAX_VALUE;

    }

	public static double treeDiameter( LN n ) {
		LN[] list = LN.getAsList(n);

		int cnt = 0;
		double longestPath = 0.0;


		for( LN node : list ) {
			if( node.data.isTip && node.back != null ) {
				cnt++;

				longestPath = Math.max( longestPath, LN.longestPath(node));
			}
		}


		//System.out.printf( "cnt: %d\n", cnt );

		return longestPath;
	}

    

}
