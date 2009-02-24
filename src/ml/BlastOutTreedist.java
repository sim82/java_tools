package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import cern.colt.function.DoubleProcedure;
import cern.colt.matrix.linalg.Blas;

class BlastFile {
	ArrayList<String>	seqNames = new ArrayList<String>();
	Map<String, Double> bitscoreMap = new HashMap<String, Double>();
	private String queryName;
	
	
	
	public BlastFile( File file ) {
		
		try {
			BufferedReader r = new BufferedReader( new FileReader(file) );
			
			
			
			while( true ) { 
				String line = r.readLine();
			
				if( line == null ) {
					throw new RuntimeException( "eof while looking for start of bitscore section");
				}
				
				if( line.startsWith( "Query=" ) ) {
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken();
					this.queryName = st.nextToken();
				} else if( line.startsWith("Sequences producing significant alignments:") ) {
					r.readLine();
					break;
				}
			}
			
			
			while( true ) {
				String line = r.readLine();

				if( line == null ) {
					throw new RuntimeException( "eof while still in bitscore section");
				}
				
				if( line.length() == 0 ) {
					System.out.printf( "end of score section\n" );
					break;
				}
				
				
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken();
				String bitscoreS = st.nextToken();
				
				double bitscore = Double.parseDouble(bitscoreS);
			
				bitscoreMap.put( name, bitscore);
				seqNames.add( name );
			}
			
			
			r.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw new RuntimeException( "bailing out" );
		}
		
		
	}
	
	ArrayList<String>getSeqNames() {
		return seqNames;
	}
	
	double getBitscore( String seqName ) {
		Double s = bitscoreMap.get(seqName);
		
		if( s != null ) {
			return s.doubleValue();
		} else {
			throw new RuntimeException( "seqName not in blastfile: '" + seqName + "'" );
			//return s.POSITIVE_INFINITY; 
		}
		
	}
	
	String getQueryName() {
		return queryName;
	}
	
}

public class BlastOutTreedist {

	public static void main(String[] args) {
		File blastFile = new File( args[0] );
        File reftreeFile = new File( args[1] );
		
		BlastFile bf = new BlastFile(blastFile);
		
		// parse reference tree used for weighted branch difference stuff
        LN reftree;
        {
            TreeParser tpreftree = new TreeParser(reftreeFile);
            reftree = tpreftree.parse();
        }

        // highest path weight in reference tree (=path with the highest sum of edge weights, no necessarily the longest path)
        double reftreeDiameter = ClassifierLTree.treeDiameter(reftree);
		
        //Map<String,String[]> splitmap = ClassifierLTree.parseSplits( rnFile );
        
        String queryName = bf.getQueryName();
        if( queryName == null ) {
        	throw new RuntimeException( "BlastFile has not query seq" );
        }
        
        
		for( String name : bf.getSeqNames() ) {
			//System.out.printf( "%s => %f\n", name, bf.getBitscore(name));
			
			double distUW = getPathLenTipToTip( reftree, queryName, name, true );
			double dist = getPathLenTipToTip( reftree, queryName, name, false );
			System.out.printf( "%s\t%f\t%d\t%f\t%f\n", name, bf.getBitscore(name), (int)distUW, dist, dist / reftreeDiameter );
		}
		
	}

	private static double getPathLenTipToTip(LN tree, String startName, String endName, boolean unweighted) {
		LN[] list = LN.getAsList(tree);
		
		LN start = null;
		LN end = null;
		
		for( LN n : list ) {
			if( n.data.isTip ) {
				String tipName = n.data.getTipName();
				
				if( start == null && tipName.equals(startName) ) {
					start = n;
				} 
				if( end == null && tipName.equals(endName) ) {
					end = n;
				}
			}
			
			if( start != null && end != null ) {
				break;
			}
		}

		if( start == null ) {
			throw new RuntimeException( "could not find node for start tip: " + startName );
		}
		if( end == null ) {
			throw new RuntimeException( "could not find node for end tip: " + startName );
		}
		
		if( start == end ) {
			return 0.0;
		}
		
		return getPathLenNodeToTipNoBack(start.back, end, unweighted) + (unweighted ? 1.0 : start.backLen);
		
	}

	private static double getPathLenNodeToTipNoBack(LN start, LN end, boolean unweighted) {
		if( start == end ) {
			return 0.0;
		} else if( start.data.isTip ) {
			return Double.POSITIVE_INFINITY;
		} else {
			{
				double len = getPathLenNodeToTipNoBack(start.next.back, end, unweighted);
				if( len < Double.POSITIVE_INFINITY ) {
					return len + (unweighted ? 1.0 : start.next.backLen);
				}
			}
			{
				double len = getPathLenNodeToTipNoBack(start.next.next.back, end, unweighted);
				if( len < Double.POSITIVE_INFINITY ) {
					return len + (unweighted ? 1.0 : start.next.next.backLen);
				}
			}
			
			return Double.POSITIVE_INFINITY;
		}
		
	}
}
