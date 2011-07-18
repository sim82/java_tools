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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class BlastOutTreedistTabformat {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		File tabfile = new File( args[0] );
		
		
		
        File reftreeFile = new File( args[1] );
        LN reftree;
        {
            TreeParser tpreftree = new TreeParser(reftreeFile);
            reftree = tpreftree.parse();
        }

        // highest path weight in reference tree (=path with the highest sum of edge weights, no necessarily the longest path)
        double reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

        
      
   
   
		BufferedReader r = new BufferedReader(new FileReader(tabfile));
		
		String line = null;
		Set<String> prevNames = new HashSet<String>();
		String lastTaxon = null;
		double bestScore = -1;
		while( (line = r.readLine()) != null ) {
			String[] token = line.split("\\s+");
//			System.out.printf( "%s %s: %s\n", token[0], token[1], token[11] );
			String taxon = token[0];
			String otherTaxon = token[1];
			if( taxon.equals(otherTaxon )) {
				// ignore self hits
				continue;
			}
			
			double bitscore = Double.parseDouble(token[11]);
			
			if( lastTaxon == null || !lastTaxon.equals(taxon)) {
				if( prevNames.contains(taxon)) {
					throw new RuntimeException( "order in input file fschked up");
				}

				
				
				prevNames.add( taxon );
				lastTaxon = taxon;
				bestScore = -1;
				

				LN reftreePruned = LN.deepClone(reftree);
		        LN[] opb = LN.removeTaxon(reftreePruned, taxon); 
		        
	        	
	        	LN[] ipb = LN.findBranchByTip( reftreePruned, otherTaxon );
	        	
	        	
	        	int[] fuck = {0, 0};
	    		double lenOT = ClassifierLTree.getPathLenBranchToBranch(opb, ipb, 0.5, fuck);
	            int ndOT = fuck[0];
	        	
	        	//double distUW = getPathLenTipToTip( reftree, queryName, name, true );
				//double dist = getPathLenTipToTip( reftree, queryName, name, false );
				
				String seq = "XXXX";
				String gap = "XXX";
				
				System.out.printf( "%s\t%s\t%s\t%s\t%d\t%f\t%f\n", seq, gap, taxon, otherTaxon, (int)ndOT, lenOT, lenOT / reftreeDiameter );
		    
				
				
			}
			
			if( bestScore < 0 ) {
				bestScore = bitscore;
			} else {
				if( bestScore < bitscore ) {
					throw new RuntimeException( "order in input file fschked up (scores)");
				}
			}
			
		}
		

	}

	static double getPathLenTipToTip(LN tree, String startName, String endName, boolean unweighted) {
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
