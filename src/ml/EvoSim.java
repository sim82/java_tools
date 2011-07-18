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
import java.util.Arrays;
import java.util.Random;

public class EvoSim {
	static Random rnd = new Random(12345);
	
	static final boolean MORPH = false;
	
	static double SCALE = 1.0;
	
	public static void main(String[] args) {
		// input (template) tree
		File treeFile = new File( args[0] );
		
		
		LN tree = LN.parseTree(treeFile);
		
		
		LN[] list = LN.getAsList(tree);
		
		double maxlen = 0;
		LN maxTip = null;
		int maxNameLen = 0;
		int ntips = 0;
		
		final int seqlen;
		
		if( args.length > 1 ) {
			seqlen = Integer.parseInt(args[1]);
		} else {
			if( MORPH ) {
				seqlen = 381;
			} else {
				seqlen = 1000;
			}
		}
		
		
		
		for( LN n : list ) {
			if( n.data.seq == null ) {
				n.data.seq = new byte[seqlen];
			}
			
			if( n.data.isTip && n.back != null ) {
				maxNameLen = Math.max(maxNameLen, n.data.getTipName().length());
				ntips++;
				
				double plen = LN.shortestPath(n);
				if( plen > maxlen ) {
					plen = maxlen;
					maxTip = n;
				}
			}
		}
		
		// take the (inner) node next to the innermost tip as startnode.
		LN startnode = maxTip.back;
		assert( !startnode.data.isTip );
		
		if( MORPH ) {
			randSeqBIN(startnode.data.seq);
		} else {
			randSeqDNA(startnode.data.seq);
		}
		evolveRec( startnode );
		evolveRec( startnode.next );
		evolveRec( startnode.next.next );
		
		System.out.printf( "%d %d\n", ntips, seqlen );
		
		for( LN n : list ) {
			if( n.data.isTip && n.back != null ) {
				System.out.printf( "%s%s\n", MultipleAlignment.padSpaceRight(n.data.getTipName(), maxNameLen + 1 ), toString(n.data.seq));
			}
		}
	}

	
	private static Object toString(byte[] seq) {
		StringBuilder sb = new StringBuilder(seq.length);
		for( byte c : seq ) {
			if( !isValidState(c) ) {
				throw new RuntimeException( "fuckage in generated seq: illegal byte: " + (int)c );
			}
			
			sb.append((char) c );
		}
		
		return sb.toString();
	}

	private static boolean isValidState(byte c) {
		switch(c) {
		case 'A':
		case 'C':
		case 'G':
		case 'T':
		case '1':
		case '0':
			return true;
			
		default:
			return false;
		}
	}

	static void evolveRec(LN ancnode) {
		if( ancnode.back == null ) {
			return;
		}
		
		byte[] ancseq = ancnode.data.seq;
		
		LN cnode = ancnode.back;
		
		if( cnode.data.seq == null ) {
			cnode.data.seq = new byte[ancseq.length];
		}
		byte[] cseq = cnode.data.seq;
		
		assert( cnode.data.serial != ancnode.data.serial );
		assert( cseq.length == ancseq.length);

		//final float CPROP = 0.1f;
		double CPROP = ancnode.backLen * SCALE;
	//	System.out.printf( "CPROP: %f\n", CPROP );
		
		for( int i = 0; i < cseq.length; i++ ) {
			
			cseq[i] = ancseq[i];
			
			while( CPROP > 1.0 ) {
				cseq[i] = substitute(cseq[i]);
				CPROP -= 1.0;
			}
			
			final float r = rnd.nextFloat();
			
			if( r <= CPROP ) {
				cseq[i] = substitute( ancseq[i]);
			}
			
		}
		
		evolveRec(cnode.next);
		evolveRec(cnode.next.next);
		
		
	}

	private static byte substitute(byte b) {
		float r = rnd.nextFloat();
		
		switch( b ) {
		case 'A':
			//if the original base is "A"
			if( r <=0.750) return 'G';
			else if(( r <= 0.875)) return 'C';
			else return 'T';
			
		case 'G':
			if( r <=0.750) return 'A';
			else if(( r <= 0.875)) return 'C';
			else return 'T';
			
		case 'C':
			//if the original base is "C"
			if(r<=0.125) return 'A';
			else if((r<=0.250)) return 'G';
			else return 'T';
			
		case 'T':
			if(r<=0.125) return 'A';
			else if((r<=0.250)) return 'G';
			else return 'C';
			
		case '0':
			return '1';
			
		case '1':
			return '0';
			
		default:
			throw new RuntimeException( "unknown character in sequence: " + (char)b );

		}
	}

	private static void randSeqDNA(byte[] seq) {
		for( int i = 0; i < seq.length; i++ ) {
			float randnum= rnd.nextFloat();
			
			if(randnum<=0.37) {
				seq[i]='A';
			} else if(/*(randnum>0.37) && */ (randnum<=0.49)) {
				seq[i]='G';
			} else if(/*(randnum>0.49) && */ (randnum<=0.73)) {
				seq[i]='C';
			} else {
				seq[i]='T';
			}
		}
	
	}
	
	private static void randSeqBIN(byte[] seq) {
		// not necessary. just fill in '0'
		Arrays.fill(seq, (byte)'0');
	}

}
