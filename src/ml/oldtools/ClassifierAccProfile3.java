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
package ml.oldtools;

import java.io.File;

import ml.ArrayIndex;
import ml.BucketMeans;
import ml.CSVFile;
import ml.MultipleAlignment;

public class ClassifierAccProfile3 {
	// this program generates an accuracy profile over the SEQ NN output
	public static void main(String[] args) {
		int[] coverage = null;
		int[] cdist = null;
		boolean lenVsDist = args.length > 0 && args[args.length-1].equals("--sp");
		boolean lenVsDistBM = args.length > 0 && args[args.length-1].equals("--bm");
		
		
		File nnFile = new File( args[1] );
		
		String aliTemp = args[0];
		
		ArrayIndex rnsIdIdx;
		String[] rnsIds;
		String[] rnsSplits;
		if( args.length > 2 && !args[2].startsWith("--")) {
			CSVFile rns = CSVFile.load(new File( args[2]), "SSSS");
			
			rnsIds = rns.getString(0);
			rnsSplits = rns.getString(3);
			rnsIdIdx = new ArrayIndex(rnsIds);
		} else {
			rnsIds = rnsSplits = null;
			rnsIdIdx = null;
		}
		final BucketMeans bm;
		if( lenVsDistBM ) {
			bm = new BucketMeans(40, 400, 40);
		} else {
			bm = null;
		}
		
		CSVFile nn = CSVFile.load(nnFile, "SSIDDSSSS" );
		//0057    200_60  0       0.000000        0.000000        1       1077    MarCl353        MarCl353_11
		String[] ids = nn.getString(0);
		int[] dists = nn.getInteger(2);
		String[] tnames = nn.getString(8);
		
		String id = null;
		MultipleAlignment ma = null;
		for( int i = 0; i < ids.length; i++ ) {
			//System.out.printf( "file %d\n", k );
			
			// filter out all non inner-QS
			if( ids[i] != null && rnsIdIdx != null ) {
				//get split-set for current seq id (ids[k]) 
				String rn = rnsSplits[rnsIdIdx.getIdx(ids[i])];
				
				// only execute loop if there is more than one element in the split list (=inner QS)
				if( rn.indexOf(',') < 0 ) {
					continue;
				}
			}
			
			if( id == null || !id.equals(ids[i])) {
				id = ids[i];
				ma = MultipleAlignment.loadPhylip( new File(aliTemp.replaceAll("\\%id\\%", id)));
			}
			
			String name = tnames[i];
			String seq = ma.getSequence(name);
			int dist = dists[i];
			
			if( coverage == null ) {
				coverage = new int[seq.length()];
				cdist = new int[seq.length()];
			} else if( coverage.length != seq.length()) {
				throw new RuntimeException( "sequence in ml has wrong length" );
			}

			
			int nChar = 0;
			for( int j = 0; j < coverage.length; j++ ) {
				char c = Character.toUpperCase(seq.charAt(j));
				if( !isGap(c) ) {
					nChar++;
					coverage[j]++;
					cdist[j] += dist; 
				}
			}
			if( lenVsDist ) {
				System.out.printf( "%d %d\n", nChar, dist );
			}
			if( bm != null ) {
				bm.add(nChar, dist);
			}
		}
		
		if( !lenVsDist && !lenVsDistBM ) {
			for( int i = 0; i < coverage.length; i++ ) {
				//coverage[i] = Math.max( coverage[i], 1 ); // add pseudocount of 1
				if( coverage[i] > 0 ) {
					System.out.printf( "%d %f\n", coverage[i], (float)cdist[i] / coverage[i] );
				} else {
					System.out.printf( "0 %f\n", Double.POSITIVE_INFINITY );
				}
			}
		}
		
		if( bm != null ) {
			bm.print();
		}
	}

	private static boolean isGap(char c) {
		
		return c == '-' || c == 'N';
	}
}
