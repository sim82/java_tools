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

public class FindMostInnerNode {
	public static void main(String[] args) {
		File infile = new File(args[0]);
		
		final TreeParser tp = new TreeParser(infile);
		
		
		
		final LN n = tp.parse();
		LN[] list = LN.getAsList(n);
		
	
		double maxd = -1.0;
		LN maxln = null;
		
		for( LN ln : list ) {
			if( ln.back != null && ln.data.isTip) {
				double lp = LN.shortestPath(ln);
				//System.out.printf( "%d %f\n", ln.data.serial, lp );
				
				if( lp > maxd ) {
					maxd = lp;
					maxln = ln;
				}
			}
		}
		
		System.out.printf( "max: %f %s\n", maxd, maxln.data.getTipName() );
		
//		LN[] nodeList = LN.getAsList(n);
//		int i = 0;
//		for( LN node : nodeList ) {
//			if( !node.data.isTip ) {
//				continue;
//			}
//			
//			if( node.data.getTipSerial() == maxln.data.getTipSerial()) {
//				System.out.printf( "idx: %d\n", i );
//				break;
//			}
//			
//			i++;
//		}
		
	}
}
