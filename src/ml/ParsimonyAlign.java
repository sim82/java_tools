package ml;

import java.util.ArrayList;
import java.util.Arrays;

import javax.management.RuntimeErrorException;

public class ParsimonyAlign {
	
	static String sA = "abcda<bbcdab";
	static String sB = "abcbbdab";
	
//	static String sA = "ba<bbbcbcd";
//	static String sB = "abcd";
	static int[] seqA = stringToSymbol( sA );
	static int[] seqB = stringToSymbol( sB );
	
	static final int LenA = seqA.length;
	static final int LenB = seqB.length;
	
	static final int W = LenA + 1;
	static final int H = LenB + 1;
	
	static int[] score = new int[W*H];
	static int[] scoreL = new int[W*H];
	
	// stay flag in the main-matrix
	final static byte STAY = 0x1;
	
	// stay flag in the gap-matrix
	final static byte STAY_L = 0x2;
	
	static byte[] dir = new byte[W*H];
	
	static int GAP_OPEN = 2;
	static int GAP_EXTEND = 1;
	
	
	static int gapCost( int l ) {
		if( l > 0 ) {
			return l * GAP_EXTEND + GAP_OPEN;
		} else {
			return 0;
		}
	}
	
	final static int LARGE_VALUE = 100000;
	
	// address for 'matrix space' coordinate
	static int addr( int a, int b ) {
		if( a < 0 || b < 0 ) {
			throw new RuntimeException( "a or b out of range" ); 
		}
		return b * W + a;
	}
	private static int[] stringToSymbol(String string ) {
		int[] ret = new int[1024];
	
		int inc;
		int i = 0;
		for( char c : string.toCharArray() ) {
			int r = 1;
			inc = 1;
			switch( c ) {
			case 'd':
				r *= 2;
			case 'c':
				r *= 2;
			case 'b':
				r *= 2;
			case 'a':
				break;
				
			case '<':
				r = 0;
				inc = -1;
				break;
				
			default:
				r = 15;
				break;
				//throw new RuntimeException( "unknown char in input string" );
			}
			 
			ret[i] |= r;
			i += inc;
		}
	
		
		return Arrays.copyOfRange( ret, 0, i );
	}
	// address for 'sequence space' coordinate (correct for trailing gap row/column)
	static int saddr( int a, int b ) {
		return addr(a + 1, b + 1);
	}
	
	public static void main(String[] args) {
		for( int ib = 0; ib < LenB; ib++ ) {
			score[saddr(-1, ib)] = gapCost(ib+1);
			scoreL[saddr(-1, ib)] = gapCost(ib+1);
			
			dir[saddr(-1, ib)] = STAY_L;
		}
			
		for( int ia = 0; ia < LenA; ia++ ) {
			scoreL[saddr(ia, -1)] = gapCost(ia+1);
			score[saddr(ia, -1)] = gapCost(ia+1);
			
			dir[saddr(ia, -1)] = STAY_L;
		}
	
		for( int ib = 0; ib < LenB; ib++ ) {
			for( int ia = 0; ia < LenA; ia++ ) {
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				// calculate match score ('go diagonal')
				int sd = score[saddr(ia-1, ib-1)];
				
				// penalize based on parsimony
				if( (ca & cb) == 0 ) {
					sd += 1;
				}
				
				
				// calculate gap score. Either open or extend gap
				int scoreExt = scoreL[saddr(ia-1, ib)] + GAP_EXTEND;
				int scoreOpen = score[saddr(ia-1, ib)] + GAP_OPEN + GAP_EXTEND;
				
				int sl;
				
				if( scoreExt < scoreOpen ) {
					// gap gets extended. Set 'stay' flag in the gap-extension matrix,
					// so the traceback knows to stay in the extension state.
					sl = scoreExt;
					dir[saddr(ia, ib)] |= STAY_L;
				} else {
					// gap gets openend. No stay flag means 'go left and switch to main-matrix'
					sl = scoreOpen;
				}
				
				scoreL[saddr(ia, ib)] = sl;
				
				
				// choose between match or gap
				if( sl < sd ) {
					// open gap. No stay flag means 'go left and switch to gap-matrix'.
					score[saddr(ia,ib)] = sl;
				} else {
					score[saddr(ia,ib)] = sd;
					
					// match positions. Set 'stay' flag in the main-matrix (means 'go diagonal')
					dir[saddr(ia,ib)] |= STAY;
				}
			}			
		}
		
		System.out.printf( "score: %d\n", score[saddr(LenA-1, LenB-1)] );
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%d ", (int)dir[addr(ia,ib)]);
			}
			System.out.println();
		}
		System.out.println();
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%d\t", score[addr(ia,ib)] );
			}
			System.out.println();
		}
		System.out.println();
		
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%d\t", scoreL[addr(ia,ib)] );
			}
			System.out.println();
		}
		System.out.println();
		
		int bb = LenB-1;
		int ba = LenA-1;
		StringBuffer bsa = new StringBuffer();
		StringBuffer bsb = new StringBuffer();
		
		// traceback
		boolean inL = false;
		while( ba >= 0 || bb >= 0 ) {
			
			if( !inL ) {
				// we are currently not in a gap. see if we should match ('go diagonal') or open a gap ('jump to gap-matrix')
				if( (dir[saddr(ba, bb)] & STAY) != 0 ) 
				{
					// go diagonal
					bsa.append(Integer.toHexString(seqA[ba]));
					bsb.append(Integer.toHexString(seqB[bb]));
					ba--;
					bb--;
				} else { 
					// open gap. keep position and switch to gap-matrix
					inL = true;
				}					
			} else {
				// we are in a gap
				
				// output gap
				bsa.append(Integer.toHexString(seqA[ba]));
				bsb.append("-");
				
				// check if we should stay in the gap-matrix
				inL = (dir[saddr(ba, bb)] & STAY_L) != 0;
				ba--;
			}
		}
		
		System.out.printf( "%s\n%s\n", bsa.reverse(), bsb.reverse() );
	} 
}
