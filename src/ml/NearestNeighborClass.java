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
	public static void main(String[] args) {
		if( args[1].equals("--auto")) {
			String ds = args[2];
			
			File radir = new File( "/space/red_alignments");
			File rtfile = new File( args[0] );
			
			ArrayList<String[]> sgpairs = new ArrayList<String[]>();
			
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
					
					wellDoIt(rtfile, treefile, phyfile, sps, "" + gap);
					
					
					
//					if( new File(radir, name).isFile()) {
//						String[] wwooooohhh = {sps, "" + gap, name }; 
//						sgpairs.add( wwooooohhh );
//					} else if( new File(radir, name + ".gz").isFile()) {
//						String[] uuuhhhaarrg = {sps, "" + gap, name + ".gz" }; 
//						sgpairs.add( uuuhhhaarrg );
//					}
					
				}
			}
			
			
			
			
			
		} else {
		
			
			File rtfile = new File(args[0]);
			File treefile = new File(args[1]);
			File phyfile = new File(args[2]);
			
			
			
			wellDoIt(rtfile, treefile, phyfile, null, null);
		}
	}

	private static void wellDoIt(File rtfile, File treefile, File phyfile, String seq, String gap) {
		LN tree;
		{
			TreeParser tp = new TreeParser( treefile );
			tree = tp.parse();
		}
		
		Set<String> taxonSet = new HashSet<String>();
		
		LN reftree;
		{
			TreeParser tp = new TreeParser( rtfile );
			reftree = tp.parse();
			
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
//			System.out.printf( "query: %s\n", qn );
			String qs = ma.getSequence(qn);
			
			int edMin = Integer.MAX_VALUE;
			int idxMin = -1;
			
			for( int i = 0; i < ma.names.length; i++ ) {
				String on = ma.names[i];
				if( qn.equals(on)) {
					continue;
				}
				
				String os = ma.getSequence(i);
				
				int ed = editDist( qs, os );
			
				if( ed < edMin ) {
					edMin = ed;
					idxMin = i;
				}
				
			}
			
			
			//System.out.printf( "best: %d %d %s\n", edMin, idxMin, ma.names[idxMin] );
			

		    LN reftreePruned = LN.deepClone(reftree);
		    LN[] opb = LN.removeTaxon(reftreePruned, qn);
		
		    //String[] ois = splitmap.get(qn);
		    String nnName = ma.names[idxMin];
		    
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
		            	System.out.printf( "%s\t%s\t%d\t%f\n", seq, gap, ndOT, lenOT );
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

	private static int editDist(String qs, String os) {
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
	
	
}
