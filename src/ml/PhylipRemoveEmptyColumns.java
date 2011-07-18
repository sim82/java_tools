package ml;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.BitSet;

import ml.LargePhylip.Rec;

public class PhylipRemoveEmptyColumns {
	private static boolean isgap(byte c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}
	
	public static void main(String[] args) {
		File f = new File( args[0] );
		
		LargePhylip lp = new LargePhylip(f, false);
		
		BitSet bs = new BitSet(lp.seqLen);
		bs.set(0, lp.seqLen, false);
		BitSet bsc = new BitSet(lp.seqLen);
		
		int maxNameLen = 0;
		
		for( int i = 0; i < lp.nTaxa; i++ ) {
			Rec rec = lp.getRecord(i);
			
			maxNameLen = Math.max( maxNameLen, rec.nameLen );
			
			ByteBuffer bb = rec.data;
			
			bsc.set(0, lp.seqLen, false);
			
//			System.out.printf( "%d %d\n", bb.limit(), bsc.);
			if( bb.limit() != lp.seqLen) {
				throw new RuntimeException("bb.limit() != lp.seqLen");
			}

			
			for( int j = 0; j < rec.dataLen; j++ ) {
				bsc.set( j, !isgap(bb.get(j)));
			}
			
			bs.or(bsc);
		}
		
		int[] map = new int[bs.cardinality()];
		for( int i = bs.nextSetBit(0), j = 0; i != -1; j++) {
			map[j] = i;
			i = bs.nextSetBit(i+1);
			
		}
		
		System.out.printf( "%d %d\n", lp.nTaxa, map.length );
		for( int i = 0; i < lp.nTaxa; i++ ) {
			Rec rec = lp.getRecord(i);
			
			for( int j = 0; j < /*maxNameLen + 1*/ rec.nameLen + 1; j++ ) {
				
				if( j < rec.nameLen ) {
					System.out.write( (char)rec.name.get(j));
				} else {
					System.out.write( ' ' );
				}
			}
//			FileOutputStream fsdfds;
			for( int j = 0; j < map.length; j++ ) {
				System.out.write( removeStupidCharacters( (char)rec.data.get(map[j] )));
				
			}
			System.out.println();
			
		}
		
//		while( true ) {}
		
	}

	private static char removeStupidCharacters(char b) {
		if( b == 'N' || b == 'n' || b == '?' ) {
			return '-';
		} else {
			return b;
		}
		
	}
}
