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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.management.RuntimeErrorException;

public class FastaFile {
	static class Entry {
		String name;
		String data;
	}
	
	
	ArrayList<Entry>entries = new ArrayList<Entry>();
	
	static FastaFile parse( File f ) {
		try {
			return FastaFile.parse(new BufferedReader(new FileReader(f)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out." );
		}
	}
	static FastaFile parse( BufferedReader r ) {
		
		FastaFile ff = new FastaFile();
		

		
		try {
			
			String line;
			
			Entry curEntry = null;
			
			while( (line = r.readLine()) != null ) {
				if( line.startsWith(">")) {
					
					
					curEntry = new Entry();
									
					
					int start = 1;
					
					while( Character.isWhitespace(line.charAt(start)) ) {
						start++;
					}
					int end = start;
					while( !Character.isWhitespace(line.charAt(end)) ) {
						end++;
					}
					
					curEntry.name = line.substring(start, end);
					curEntry.data = "";
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
