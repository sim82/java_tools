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
import java.util.Set;

public class SplitAlignmentByTree {
	public static void main(String[] args) {
		File aliFile = new File( args[0] );
		File treeFile = new File( args[1] );
		
		boolean fuckholm = args.length > 2 && args[2].equals("--hmmer");
		
		MultipleAlignment ma = MultipleAlignment.loadPhylip(aliFile);
		LN t = TreeParser.parse(treeFile);
		
		Set<String> ts = LN.getTipSet(LN.getAsList(t));
		
		String[] names1 = new String[ts.size()];
		String[] seqs1 = new String[ts.size()];
		
		String[] names2 = new String[ma.nTaxon - ts.size()];
		String[] seqs2 = new String[ma.nTaxon - ts.size()];
		
		
		int i1 = 0;
		int i2 = 0;
		
		for( int i = 0; i < ma.nTaxon; i++ ) {
			if( ts.contains(ma.names[i])) {
				names1[i1] = ma.names[i];
				seqs1[i1] = ma.data[i];
				i1++;
			} else {
				names2[i2] = ma.names[i];
				seqs2[i2] = ma.data[i];
				i2++;
			}
		}
		
		MultipleAlignment ma1 = new MultipleAlignment(ma.seqLen, names1, seqs1 );
		MultipleAlignment ma2 = new MultipleAlignment(ma.seqLen, names2, seqs2 );
		
		if( !fuckholm ) {
			ma1.writePhylip(new File( aliFile.getPath() + ".in" ));
			ma2.writePhylip(new File( aliFile.getPath() + ".out" ));
		} else {
			ma1.writeFuckholm(new File( aliFile.getName() + ".in" ));
			ma2.writeFastaNogaps(new File( aliFile.getName() + ".out" ));
		
		}
		
	}
}
