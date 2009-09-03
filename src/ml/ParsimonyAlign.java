package ml;

import java.util.ArrayList;
import java.util.Arrays;

import javax.management.RuntimeErrorException;

public class ParsimonyAlign {
	enum Dir {
		D,
		U,
		L,
		X
	};
	
	static String sA = "abcd<babcdab";
	static String sB = "abbbbdab";
	static int[] seqA = stringToSymbol( sA );
	static int[] seqB = stringToSymbol( sB );
	
	static final int LenA = seqA.length;
	static final int LenB = seqB.length;
	
	static final int W = LenA + 1;
	static final int H = LenB + 1;
	
	static int[] score = new int[W*H];
	static Dir[] dir = new Dir[W*H];
	
	
	
	
	
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
	
		//ArrayList<Integer> ret = new ArrayList<Integer>();
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
		dir[0] = Dir.X;
		for( int ib = 0; ib < LenB; ib++ ) {
			//score[saddr(-1, ib)] = score[saddr(-1, ib-1)];
			score[saddr(-1, ib)] = LARGE_VALUE;
			dir[saddr(-1, ib)] = Dir.U;
		}
			
		for( int ia = 0; ia < LenA; ia++ ) {
			score[saddr(ia, -1)] = score[saddr(ia-1, -1)];
			dir[saddr(ia, -1)] = Dir.L;
		}
		
		for( int ib = 0; ib < LenB; ib++ ) {
			for( int ia = 0; ia < LenA; ia++ ) {
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				int sd;
				{
					sd = score[saddr(ia-1, ib-1)];
					if( (ca & cb) == 0 ) {
						sd += 1;
					}
				}
	
				int sl;
				{
					sl = score[saddr(ia-1, ib)];
					if( (15 & cb) == 0 ) {
						sl += 1;
					}
	
					
				}
				int su;
				{
					su = score[saddr(ia, ib-1)];
					if( (ca & 15) == 0 ) {
						su += 1;
					}
				}
				su = LARGE_VALUE;
				
				if( sd <= sl && sd <= su ) {
					score[saddr(ia,ib)] = sd;
					dir[saddr(ia,ib)] = Dir.D;
				} else {
					if( sl <= su ) {
						score[saddr(ia,ib)] = sl;
						dir[saddr(ia,ib)] = Dir.L;
					} else {
						score[saddr(ia,ib)] = su;
						dir[saddr(ia,ib)] = Dir.U;
					}
				}
				
			}			
			//score[saddr(i,i)] = ;
		}
		
		System.out.printf( "score: %d\n", score[saddr(LenA-1, LenB-1)] );
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%s\t", dir[addr(ia,ib)] );
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
		
		int bb = LenB-1;
		int ba = LenA-1;
		StringBuffer bsa = new StringBuffer();
		StringBuffer bsb = new StringBuffer();
		
		while( ba >= 0 || bb >= 0 ) {
			switch( dir[saddr(ba, bb)]) {
			case D:
				bsa.append(Integer.toHexString(seqA[ba]));
				bsb.append(Integer.toHexString(seqB[bb]));
				ba--;
				bb--;
				break;
				
			case L:
				bsa.append(Integer.toHexString(seqA[ba]));
				bsb.append("-");
				ba--;
				break;
				
			case U:
				bsa.append("-");
				bsb.append(Integer.toHexString(seqA[bb]));
				bb--;
				break;
				
			default:
				throw new RuntimeException( "traceback should be finished by now ..." );
			}
			
		}
		
		System.out.printf( "%s\n%s\n", bsa.reverse(), bsb.reverse() );
	} 
}
