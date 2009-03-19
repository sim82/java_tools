package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BlastFile {
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
				//	System.out.printf( "end of score section\n" );
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
