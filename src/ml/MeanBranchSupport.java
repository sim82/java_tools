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

public class MeanBranchSupport {
	public static void main(String[] args) {
		File infile = new File( args[0] );
		
		final TreeParser tp = new TreeParser(infile);
		LN n = tp.parse();
		
		//LN[] nodelist = LN.getAsList(n);
		
		LN[][] bl = LN.getBranchList(n);
		
		double sum = 0.0;
		double num = 0.0;
		
		for( LN[] br : bl ) {
			sum += br[0].backSupport;
			num += 1.0;
		}
		
		
		System.out.printf( "mean bs support: %f\n", sum / num );
	}
}
