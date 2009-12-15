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
import java.util.ArrayList;

public class TreeCompare {
	public static void main(String[] args) {
//		File treeFile = new File( "/space/raxml/VINCENT/RAxML_bipartitions.855.BEST.WITH" );
		File treeFile = new File( "/home/sim/mnt_ibc/tmp/RAxML_originalLabelledTree.855_0000" );
		TreeParser tp = new TreeParser( treeFile );
		LN n = tp.parse();
		
		int nTips = tp.getNTips();
		
		
		LN[][] branches = getAllBranches(n);
		
		
		for( LN[] b : branches ) {
			
			
			System.out.printf( "%d %d %s %s\n", b[0].data.serial, b[1].data.serial, b[0].backLabel, b[1].backLabel);
		}
	}
	
	
	public static LN[][] getAllBranches( LN n ) {
		ArrayList<LN[]>branches = new ArrayList<LN[]>();
		
		getAllBranchesRec(n, true, branches);
		
		return branches.toArray(new LN[branches.size()][]);
	}
	
	
	public static void getAllBranchesRec(LN n, boolean back, ArrayList<LN[]>branches) {
		if( back ) {
			if( n.back != null ) {
				LN[] b = {n, n.back};
				branches.add( b );
				
				getAllBranchesRec(n.back, false, branches);
			}
		}
		if( n.next.back != null ) {
			LN[] b = {n.next, n.next.back};
			branches.add( b );
			
			getAllBranchesRec(n.next.back, false, branches);
		}
		if( n.next.next.back != null ) {
			LN[] b = {n.next.next, n.next.next.back};
			branches.add( b );
			
			getAllBranchesRec(n.next.next.back, false, branches);
		}
		
		
	}
}
