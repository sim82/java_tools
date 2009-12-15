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

public class Test {
	public static void main(String[] args) {
		LN tree = LN.parseTree(new File("/space/raxml/VINCENT/RAxML_bipartitions.1604.BEST.WITH"));
		
		
		LN[] list = LN.getAsList(tree);
		
		for( LN n : list ) {
			
			if( n.data.isTip("M00Clon8") ) {
				n = LN.getTowardsTree(n);
				
				System.out.printf( "%f %f %f\n", n.backSupport, n.back.next.backSupport, n.back.next.next.backSupport );
				break;
			}
		}
	}
}
