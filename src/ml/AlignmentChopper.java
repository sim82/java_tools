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

public class AlignmentChopper {
	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(new File( args[0] ));
		
		int len = ma.seqLen;
		int maxLen = len / 2;
		
		for( String name : ma.names ) {
			char[] seq = ma.getSequence(name).toCharArray();
			
			char[] newseq = new char[seq.length];
			int n = 0;
			for( int i = 0; i < seq.length; i++ ) {
				char c = seq[i];
				if( !isGapCharacter(c) ) {
					if( n > maxLen ) {
						c = '-';
					}
					
					n++;
				}
				
				newseq[i] = c;
			}
			
			ma.replaceSequence(name, new String(newseq));
		}
		
		ma.writePhylip(System.out);
		
	}
	
	
	private static boolean isGapCharacter(char c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}
}
