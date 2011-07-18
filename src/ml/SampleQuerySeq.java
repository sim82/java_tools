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
import java.util.Random;
import java.util.Set;

public class SampleQuerySeq {
	public static void main(String[] args) {
		File af = new File( args[0] ); 
		File tf = new File( args[1] );
		
		
		MultipleAlignment ml = MultipleAlignment.loadPhylip(af);
		LN t = LN.parseTree(tf);
		
		Set<String> ts = LN.getTipSet(LN.getAsList(t));
		
		ArrayList<String> ans = new ArrayList<String>();
		
		for( String name : ml.names ) {
			if( !ts.contains(name)) {
				ans.add(name);
			}
		}
		Random rand = new Random();
		
		final int N = 100;
		
		String[] oname = new String[N];
		
		for( int i = 0; i < N; i++ ) {
			assert( ans.size() > 0 );
			int r = rand.nextInt(ans.size()); 
			
			oname[i] = ans.get(r);
			ans.set(r, ans.get(ans.size() - 1));
			ans.remove(ans.size() - 1);
		}
		final int N_TAXA = ts.size() + N;
		
		String[] names = new String[N_TAXA]; 
		String[] seqs = new String[N_TAXA];
		
		int i = 0;
		
		for( String name : ts ) {
			names[i] = name;
			seqs[i] = ml.getSequence(name);
			i++;
		}
		
		for( int j = 0; j < N; j++ ) {
			names[i] = oname[j];
			seqs[i] = ml.getSequence(oname[j]);
			i++;
		}
		
		MultipleAlignment oml = new MultipleAlignment( ml.seqLen, names, seqs);
		
		oml.writePhylip(System.out);
	}
}
