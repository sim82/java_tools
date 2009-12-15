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

import java.io.File;
import java.util.HashMap;

public class NearestNeighborClassPhylip {
	public static void main(String[] args) {
		File refFile = new File( args[0] );
		File queryFile = new File( args[1] );
		
		
		MultipleAlignment refMa = MultipleAlignment.loadPhylip(refFile);
		MultipleAlignment queryMa = MultipleAlignment.loadPhylip(queryFile);
		
		HashMap<String, Integer> refMaMap = refMa.nameMap;
		for( int j = 0; j < queryMa.nTaxon; j++ ) {
			String qname = queryMa.names[j];
			//System.out.printf( "query: %s\n", qname );
			if( refMaMap.containsKey(qname)) {
				continue;
			}
			
			String qseq = queryMa.getSequence(qname);
			
			int minDist = Integer.MAX_VALUE;
			String bestNeighbor = null;
			
			for( int i = 0; i < refMa.nTaxon; i++ ) {
				String rseq = refMa.getSequence(i);
				
				
				int dist = NearestNeighborClass.editDist_nogaps(qseq, rseq);
				if( dist < minDist ) {
					minDist = dist;
					bestNeighbor = refMa.names[i];
				}
			}
			
			System.out.printf( "%s %s %d\n", qname, bestNeighbor, minDist );
			
		}
	}
}
