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
import java.util.Map;

public class CheapDist {
	public static void main(String[] args) {
		File classfile1 = new File( args[0]);
		File classfile2 = new File( args[1]);
		File reftreefile = new File( args[2]);
		
		
		LN reftree = LN.parseTree(reftreefile);
		
		ClassifierOutput co1 = ClassifierOutput.read(classfile1);
		ClassifierOutput co2 = ClassifierOutput.read(classfile2);
		
		Map<String,ClassifierOutput.Res> com2 = new HashMap<String, ClassifierOutput.Res>();
		
		for( ClassifierOutput.Res res : co2.reslist ) {
			if( com2.containsKey(res.seq)) {
				throw new RuntimeException("com2.containsKey(res.seq)");
			}
			
			com2.put(res.seq, res);
		}
		
		for( int i = 0; i < co1.reslist.size(); i++ ) {
			ClassifierOutput.Res r1 = co1.reslist.get(i);
		//	ClassifierOutput.Res r2 = co2.reslist.get(i);
			
			ClassifierOutput.Res r2 = com2.get(r1.seq);
			
			if( !r1.seq.equals(r2.seq)) {
				throw new RuntimeException("!r1.seq.equals(r2.seq)");
			}
			
			LN[] b1 = LN.findBranchByName(reftree, r1.branch);
			LN[] b2 = LN.findBranchByName(reftree, r2.branch);
			
			int[] fuck = new int[1]; // some things (like 'multiple return values') are soo painful in java ...
            double lenOT = ClassifierLTree.getPathLenBranchToBranch( b1, b2, 0.5, fuck);
            int ndOT = fuck[0];
			
            int td1 = BranchToTipDist.ndToNearestTip(b2[0]);
            int td2 = BranchToTipDist.ndToNearestTip(b2[1]);
            
            int td = Math.min(td1, td2);
            
            System.out.printf( "%s %s: %d %s %s %d %f %d\n", r1.seq, r2.seq, (int)r1.support, r1.branch, r2.branch, ndOT, lenOT, td);
		}
	}
}
