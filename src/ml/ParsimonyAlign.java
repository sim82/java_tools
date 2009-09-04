package ml;

import java.util.ArrayList;
import java.util.Arrays;

import javax.management.RuntimeErrorException;

public class ParsimonyAlign {
	enum Dir {
		D,
		U,
		L,
		E,
		X
	};
	
//	static String sA = "abcda<bbcdab";
//	static String sB = "abcbbdab";
	
	static String sA = "bbbbabcd";
	static String sB = "abcd";
	static int[] seqA = stringToSymbol( sA );
	static int[] seqB = stringToSymbol( sB );
	
	static final int LenA = seqA.length;
	static final int LenB = seqB.length;
	
	static final int W = LenA + 1;
	static final int H = LenB + 1;
	
	static int[] score = new int[W*H];
	static int[] scoreL = new int[W*H];
	static Dir[] dir = new Dir[W*H];
	static Dir[] dirL = new Dir[W*H];
	
	
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
			score[saddr(-1, ib)] = gapCost(ib+1);
			scoreL[saddr(-1, ib)] = gapCost(ib+1);
			dir[saddr(-1, ib)] = Dir.U;
		}
			
		for( int ia = 0; ia < LenA; ia++ ) {
//			scoreL[saddr(ia, -1)] = scoreL[saddr(ia-1, -1)] + 1;
//			score[saddr(ia, -1)] = scoreL[saddr(ia, -1)] + 1;
			scoreL[saddr(ia, -1)] = gapCost(ia+1);
			score[saddr(ia, -1)] = gapCost(ia+1);
			
			
			dir[saddr(ia, -1)] = Dir.L;
		}
		
		for( int ib = 0; ib < LenB; ib++ ) {
			for( int ia = 0; ia < LenA; ia++ ) {
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				int sd;
				int sdl;
				//int sd2;
				{
					sd = score[saddr(ia-1, ib-1)];
					//sd2 = scoreL[saddr(ia-1, ib-1)];
					if( (ca & cb) == 0 ) {
						sd += 1;
						//sd2 += 1;
					}
					sdl = sd;
				}
	
				int sl;
				int sll;
				boolean open;
				{
					//sll = scoreL[saddr(ia-1, ib)] + 1;
					
					scoreL[saddr(ia, ib)] = Math.min(scoreL[saddr(ia-1, ib)] + GAP_EXTEND, score[saddr(ia-1, ib)] + GAP_OPEN + GAP_EXTEND);
					//scoreL[saddr(ia, ib)] = scoreL[saddr(ia-1, ib)] + 1;
					
					sl = scoreL[saddr(ia, ib)];
					open = sl == score[saddr(ia-1, ib)] + GAP_OPEN + GAP_EXTEND;
					
					if( open ) {
						dirL[saddr(ia, ib)] = Dir.E;
					} else {
						dirL[saddr(ia, ib)] = Dir.L;
					}
//					if( sl1 < sl2 ) {
//						sl = sl1;
//					} else {
//						sl = sl2;
//					}
//					sll = sl;
//					if( sl <= sl2 ) {
//						sld = Dir.L;
//						sl = sl;
//					} else {
//						sld = Dir.L;
//						sl = sl2;
//					}
//					if( (15 & cb) == 0 ) {
//						sl += 1;
//					}
	
					
				}
				int su;
				{
					su = score[saddr(ia, ib-1)];
					if( (ca & 15) == 0 ) {
						su += 1;
					}
				}
				su = LARGE_VALUE;
				
//				if( sd <= sl && sd <= su ) {
//					score[saddr(ia,ib)] = sd;
//					scoreL[saddr(ia,ib)] = sl2;
//					dir[saddr(ia,ib)] = Dir.D;
//				} else {
//					if( sl <= su ) {
//						score[saddr(ia,ib)] = sl;
//						scoreL[saddr(ia,ib)] = sl2;
//						dir[saddr(ia,ib)] = sld;
//					} else {
//						score[saddr(ia,ib)] = su;
//						dir[saddr(ia,ib)] = Dir.U;
//					}
//				}
				
				if( sl <= sd ) {
					score[saddr(ia,ib)] = sl;
					Dir d = open ? Dir.L : Dir.E;
					dir[saddr(ia,ib)] = Dir.E;
					
				} else {
					score[saddr(ia,ib)] = sd;
					
					dir[saddr(ia,ib)] = Dir.D;
					
				}
				
				//scoreL[saddr(ia,ib)] = sll;
				
				
			}			
			//score[saddr(i,i)] = ;
		}
		
		System.out.printf( "score: %d\n", score[saddr(LenA-1, LenB-1)] );
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%s ", dir[addr(ia,ib)] );
			}
			System.out.println();
		}
		System.out.println();
		
		for( int ib = 0; ib < H; ib++ ) {
			for( int ia = 0; ia < W; ia++ ) {
				System.out.printf( "%s ", dirL[addr(ia,ib)] );
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
		boolean inL = false;
		while( ba >= 0 || bb >= 0 ) {
			
			if( !inL ) {
				switch( dir[saddr(ba, bb)]) {
				case D:
					bsa.append(Integer.toHexString(seqA[ba]));
					bsb.append(Integer.toHexString(seqB[bb]));
					ba--;
					bb--;
					break;
					
				case E:
				case L:
//					bsa.append(Integer.toHexString(seqA[ba]));
//					bsb.append("-");
//					ba--;
					inL = true;
					break;
					
				case U:
					bsa.append("-");
					bsb.append(Integer.toHexString(seqA[bb]));
					bb--;
					break;
					
				
				default:
					throw new RuntimeException( "traceback should be finished by now ..." );
				}
			} else {
				bsa.append(Integer.toHexString(seqA[ba]));
				bsb.append("-");
				
				inL = dirL[saddr(ba, bb)] == Dir.L;
//				if( inL ) {
//					System.out.printf( "stay\n" );
//				} else {
//					System.out.printf( "leave\n" );
//				}
				ba--;
			}
		}
		
		System.out.printf( "%s\n%s\n", bsa.reverse(), bsb.reverse() );
	} 
}
