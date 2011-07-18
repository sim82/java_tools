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
import java.util.ArrayList;

import ml.MultipleAlignment;

public class AlignmentFumbelizer {

	public static void main(String[] args) {
		File raFile = new File( args[0] );
		
		MultipleAlignment ra = MultipleAlignment.loadPhylip(raFile);
	
		ArrayList<String> fumblenames = new ArrayList<String>();
		ArrayList<String> fumbleseqs = new ArrayList<String>();
		
		for( int i = 1; i < args.length; i++ ) {
			MultipleAlignment a = MultipleAlignment.loadFasta( new File(args[i]));
			
			
			
			String fname = a.names[0];
			String refseq = ra.getSequence(fname);
			String cmpseq = a.data[0];
			String sname = a.names[1];
			
			String sseq;
			if( refseq.length() == cmpseq.length() ) {
				sseq = a.data[1];
			} else {
				// fumbelize
				
//				if( refseq.length() > cmpseq.length() ) {
//					throw new RuntimeException( "refseq.length() > cmpseq.length()" );
//				}
				
//				System.out.printf( "%s\n", refseq );
//				System.out.printf( "%s\n", cmpseq );
				
				
				sseq = "";
				int ngaps = 0;
				for( int rc = 0; rc < refseq.length(); rc++ ) {
					char rchar = Character.toUpperCase(refseq.charAt(rc));
					
					char cchar;
					
					while( (cchar = Character.toUpperCase(cmpseq.charAt(rc+ngaps))) != rchar ) {
						
						if( cchar == '-') {
							ngaps++;
						} else if( rchar == '-' ) {
							// gap removed!?
							ngaps--;
							sseq+="-";
							break;
						} else {
							throw new RuntimeException( "bad change in cmpseq!? " + cchar + " " + rchar + " " + rc + " " + ngaps );	
						}
					}
					
					
					sseq += a.data[1].charAt(rc+ngaps);
				}
				//System.out.printf( "sname: %s %s %d %d '%s'\n", sname, args[i], refseq.length(), cmpseq.length(), cmpseq );
//				throw new RuntimeException( "implement" );
			}
			
			ra.replaceSequence(sname, sseq);
			
//			fumblenames.add(sname);
//			fumbleseqs.add(sseq);
		}
		
		
		String[] newnames = new String[ra.names.length + fumblenames.size()];
		String[] newseqs = new String[ra.data.length + fumbleseqs.size()];
		
		System.arraycopy(ra.names, 0, newnames, 0, ra.names.length);
		System.arraycopy(ra.data, 0, newseqs, 0, ra.data.length);
		for( int i = 0; i < fumblenames.size(); i++ ) {
			newnames[i + ra.names.length] = fumblenames.get(i);
			newseqs[i + ra.data.length] = fumbleseqs.get(i);
		}
		
		
//		MultipleAlignment oa = new MultipleAlignment(ra.seqLen, newnames, newseqs );
		
		ra.writePhylip(System.out);
	}
}
