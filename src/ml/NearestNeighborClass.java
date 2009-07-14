package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Woodstox;

public class NearestNeighborClass {
	public final static boolean VERBOSE = false;
	
	static LN reftree;
	static double reftreeDiameter; 
	
	public static void main(String[] args) {
		if( args[1].equals("--auto")) {
			String ds = args[2];
			
			File radir = new File( "/space/red_alignments");
			File rtfile = new File( args[0] );
			
			ArrayList<String[]> sgpairs = new ArrayList<String[]>();
			
			TreeParser tp = new TreeParser( rtfile );
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
			
			boolean end = false;
			for( int seq = 0; !end; seq++ ) {
				end = true;
				
				for( int gap = 10; gap <= 100; gap += 10 ) {
					
					String sps = padzero(4, seq);
					String name = ds + "_" + sps + "_" + gap;
					
					if(! new File(radir, name).isFile()) {
						name += ".gz";
						if(! new File(radir, name).isFile()) {
							continue;
						}
					}
				
					end = false;
					
					File treefile = new File( "/space/redtree/RAxML_bipartitions." + ds + ".BEST.WITH_" + sps);
					File phyfile = new File( radir, name );
					
					wellDoIt( treefile, phyfile, sps, "" + gap);
					
					
					
//					if( new File(radir, name).isFile()) {
//						String[] wwooooohhh = {sps, "" + gap, name }; 
//						sgpairs.add( wwooooohhh );
//					} else if( new File(radir, name + ".gz").isFile()) {
//						String[] uuuhhhaarrg = {sps, "" + gap, name + ".gz" }; 
//						sgpairs.add( uuuhhhaarrg );
//					}
					
				}
			}
			
			
			
			
			
		} else if(args[1].equals("--autope")) {
			
			
			String ds = args[2];
			
			String[] gaps = {"100"};
			
			if( args.length > 3 ) {
				gaps[0] = args[3];
			} 
			
			
			File radir = new File( "/space/pairedend_alignments");
			File rtfile = new File( args[0] );
			
			ArrayList<String[]> sgpairs = new ArrayList<String[]>();
			
			TreeParser tp = new TreeParser( rtfile );
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
			
			boolean end = false;
			for( int seq = 0; !end; seq++ ) {
				end = true;
				
			
				
				for( String gap : gaps ) {
					
					String sps = padzero(4, seq);
					String name = ds + "_" + sps + "_" + gap;
					
					if(! new File(radir, name).isFile()) {
						name += ".gz";
						if(! new File(radir, name).isFile()) {
							continue;
						}
					}
				
					end = false;
					
					File treefile = new File( "/space/redtree/RAxML_bipartitions." + ds + ".BEST.WITH_" + sps);
					File phyfile = new File( radir, name );
					
					wellDoIt( treefile, phyfile, sps, "" + gap);
					
					
					
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
				if( idx1st >= 0 ) {
					int idx2nd = name.indexOf('_', idx1st);
					
					if( idx2nd > idx1st ) {
						int idxdot = name.indexOf('.', idx2nd );
						
						if( idxdot > idx2nd ) {
							seq = name.substring(idx1st + 1, idx2nd );
							gap = name.substring(idx2nd + 1, idxdot );
						}
						
					}
				}
				
			}
			TreeParser tp = new TreeParser( rtfile );
			reftree = tp.parse();
			reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
			wellDoIt( treefile, phyfile, seq, gap);
		}
	}

	private static void wellDoIt( File treefile, File phyfile, String seq, String gap) {
		LN tree;
		{
			TreeParser tp = new TreeParser( treefile );
			tree = tp.parse();
		}
		
		Set<String> taxonSet = new HashSet<String>();
		
		
		{
			LN[] list = LN.getAsList(tree);
			for( LN n : list ) {
				if( n.data.isTip ) {
					taxonSet.add( n.data.getTipName());
				}
			}
		}
		
		
		
		
		MultipleAlignment ma;
		try {
			ma = MultipleAlignment.loadPhylip(GZStreamAdaptor.open(phyfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out");
		} 
		

		
		ArrayList<String> queryNames = new ArrayList<String>();
		for( String name : ma.names ) {
			if( !taxonSet.contains(name)) {
				queryNames.add( name );
			}
		}
		
//		System.out.printf( "n tree: %d, n ma: %d\n", taxonSet.size(), ma.names.length );
		
		for( String qn : queryNames ) {
			
			if( VERBOSE ) {
				System.out.printf( "query: %s\n", qn );
			}
			
			String qs = ma.getSequence(qn);
			
			int edMin = Integer.MAX_VALUE;
			int idxMin = -1;
			int nMin = 0;
			for( int i = 0; i < ma.names.length; i++ ) {
				String on = ma.names[i];
				if( qn.equals(on)) {
					continue;
				}
				
				String os = ma.getSequence(i);
				
				int ed = editDist_nogaps(qs, os);
				if( VERBOSE ) {
					System.out.printf( "(%s %d) ", on, ed );
				}
				
				if( ed < edMin ) {
					edMin = ed;
					idxMin = i;
					
					nMin = 1;
				} else if( ed == edMin ) {
					nMin++;
				}
				
			}
			
			if( VERBOSE ) {
				System.out.printf( "\nbest: %d %d %s\n", edMin, idxMin, ma.names[idxMin] );
			}

		    LN reftreePruned = LN.deepClone(reftree);
		    LN[] opb = LN.removeTaxon(reftreePruned, qn);
		
		    //String[] ois = splitmap.get(qn);
		    String nnName = ma.names[idxMin];
		    
		    if( false ) {
		    	System.out.printf( "%s\n%s\n", qs, ma.data[idxMin] );
		    }
		    
		    LN[] list = LN.getAsList(reftreePruned);
		    
		    for( LN n : list ) {
		    	if( n.data.isTip( nnName )) {
		    		
		    		int wc = 0;
		    		
		    		n = LN.getTowardsTree(n);
		    		
		    		LN[] ipb = {n, n.back};
		    		
		    		int[] fuck = {0, 0};
		    		double lenOT = ClassifierLTree.getPathLenBranchToBranch(opb, ipb, 0.5, fuck);
		            int ndOT = fuck[0];
		            if( seq != null && gap != null ) {
		            	System.out.printf( "%s\t%s\t%d\t%f\t%f\t%d\t%d\t%s\n", seq, gap, ndOT, lenOT, lenOT/reftreeDiameter, nMin, edMin, qn );
		            } else {
		            	System.out.printf( "%d %f\n", ndOT, lenOT );
		            }
		    		
		    		break;
		    	}
		    }
			
		}
	}

	private static String padzero(int nc, int n) {
		String s = "" + n;
		while( s.length() < nc ) {
			s = "0" + s;
		}
		return s;
	}

	public static int editDist(String qs, String os) {
		if( qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}
		
		int ed = 0;
		for( int i = 0; i < qs.length(); i++ ) {
			
			
			if( qs.charAt(i) != '-' && qs.charAt(i) != os.charAt(i)) {
				ed+=3;
			} else if( qs.charAt(i) == '-' ) {
				if( i <= 0 || qs.charAt(i-1) != '-' ) {
					ed+=3;
				} else {
					ed++;
				}
			}
		}
		
		return ed;
	}
	
	
	public static int editDist_affine(String qs, String os) {
		if( qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}
		
		int ed = 0;
		
		boolean qge = false;
		boolean oge = false;
		for( int i = 0; i < qs.length(); i++ ) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);
			
			boolean qg = isGapCharacter(qc);
			boolean og = isGapCharacter(oc);
			if( !true ) {
				if( !qg && qc != oc ) {
					ed += 3;
				} else if( qg ) {
					if( !qge ) {
						ed+=3;
					} else {
						ed +=1;
					}
				}
				
				
//				if( qc != oc ) {
//					
//					if( qg ) {
//						if( !qge ) {
//							ed+=3;
//						} else {
//							ed += 1;
//						}	
//					} else {
//						ed += 3;
//					}
//				} 
				
				
			} else {
				if( qc != oc ) {
					// we have a mismatch
					// check if we have (one-sided) gap
					
					if( qg ) {
						// gap in the query sequence, check if we extend an existing gap 
						if( qge ) {
							ed += 1;
						} else {
							ed += 3;
						}
					} else if( og ) {
						// d.t.o for the original sequence
						if( oge ) {
							ed += 1;
						} else {
							ed += 3;
						}
					} else {
						// no gap. use normal mismatch penalty
						ed += 3;
					}
				} else if( qg ) {
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
	
	public static int editDist_nogaps(String qs, String os) {
		if( qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}
		
		int ed = 0;
		
		for( int i = 0; i < qs.length(); i++ ) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);
			
			if( qc != '-' && oc != '-' && qc != oc ) {
				ed+=1;
			} 
		}
		
		return ed;
	}
	
	public static int editDist_withgaps(String qs, String os) {
		
//		if( true ) {
//			
//			throw new RuntimeException( "bla");
//		}
		if( qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}
		
		int ed = 0;
		
		for( int i = 0; i < qs.length(); i++ ) {
			char qc = qs.charAt(i);
			char oc = os.charAt(i);
			
			if( qc != oc ) {
				ed+=1;
			} 
		}
		
		return ed;
	}
	
//	private static int editDist(String qs, String os) {
//		if( qs.length() != os.length()) {
//			throw new RuntimeException("qs.length() != os.length()");
//		}
//		
//		int ed = 0;
//		for( int i = 0; i < qs.length(); i++ ) {
//			
//			
//			if( qs.charAt(i) != '-' && qs.charAt(i) != os.charAt(i)) {
//				ed+=4;
//			} else if( qs.charAt(i) == '-' ) {
//				if( i <= 0 || qs.charAt(i-1) != '-' ) {
//					ed+=3;
//				} else {
//					ed++;
//				}
//			}
//		}
//		
//		return ed;
//	}
	

	private static int editDist_bad(String qs, String os) {
		if( qs.length() != os.length()) {
			throw new RuntimeException("qs.length() != os.length()");
		}
		
		int ed = 0;
		for( int i = 0; i < qs.length(); i++ ) {
			
			
			if( qs.charAt(i) != os.charAt(i)) {
				ed++;
			} 
		}
		
		return ed;
	}

	private static boolean isGapCharacter(char c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}
}
