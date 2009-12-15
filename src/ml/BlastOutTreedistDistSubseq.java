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

import util.StringTools;


public class BlastOutTreedistDistSubseq {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		String inTemp = args[0];
		
		
		
        File reftreeFile = new File( args[1] );
        LN reftree;
        {
            TreeParser tpreftree = new TreeParser(reftreeFile);
            reftree = tpreftree.parse();
        }

        // highest path weight in reference tree (=path with the highest sum of edge weights, no necessarily the longest path)
        double reftreeDiameter = ClassifierLTree.treeDiameter(reftree);

        
      
   
   
        for( int i = 0;; i++ ) {
        	File infile = new File( inTemp.replaceAll( "\\%id\\%", StringTools.padchar("" + i, 0, 4)));
        	if( !infile.isFile() || !infile.canRead() ) {
        		break;
        	}
        	
        	BufferedReader r = new BufferedReader(new FileReader(infile));
    		
    		String line = null;
    		
    		while( (line = r.readLine()) != null ) {
    			String[] token = line.split("\\s+");
//    			System.out.printf( "%s %s: %s\n", token[0], token[1], token[11] );
    			String taxonDecorated = token[0];
    			String taxon = taxonDecorated.substring(0, taxonDecorated.length() - 3 );
    			
    			String otherTaxon = token[1];
    			
    			    				

				LN reftreePruned = LN.deepClone(reftree);
				LN[] opb = LN.removeTaxon(reftreePruned, taxon); 
    		        
    	        	
    			LN[] ipb = LN.findBranchByTip( reftreePruned, otherTaxon );
    	        	
    	        	
    			int[] fuck = {0, 0};
    			double lenOT = ClassifierLTree.getPathLenBranchToBranch(opb, ipb, 0.5, fuck);
    			int ndOT = fuck[0];
    				
    			String seq = "XXXX";
    			String gap = "XXX";
    				
    			System.out.printf( "%s\t%s\t%s\t%s\t%d\t%f\t%f\n", seq, gap, taxonDecorated, otherTaxon, (int)ndOT, lenOT, lenOT / reftreeDiameter );
    		    
    		} 	
		}
	}
}
