package ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FastaFile {
	class Entry {
		String name;
		String data;
	}
	
	
	ArrayList<Entry>entries = new ArrayList<Entry>();
	
	
	FastaFile parse( BufferedReader r ) {
		
		FastaFile ff = new FastaFile();
		

		
		try {
			
			String line;
			
			Entry curEntry = null;
			
			while( (line = r.readLine()) != null ) {
				if( line.startsWith(">")) {
					curEntry = new Entry();
					
					StringTokenizer ts = new StringTokenizer(line);
					ts.nextToken();
					
					curEntry.name = ts.nextToken();
					
					ff.entries.add( curEntry );
				} else {
					StringTokenizer ts = new StringTokenizer(line);
					while( ts.hasMoreTokens() ) {
						curEntry.data += ts.nextToken();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("bailing out.");
		}
		
		return ff; 
	}
	

	ArrayList<Entry>getEntries() {
		return entries;
	}
	
}
