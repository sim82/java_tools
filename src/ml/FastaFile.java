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
