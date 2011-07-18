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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;



public class ParsimonyAlign {
	
	static String sA = "abcda<bbcdab";
	static String sB = "abcbbdab";
	
//	static String sA = "ba<bbbcbcd";
//	static String sB = "abcd";
	
	final int[] seqA;// = stringToSymbol( sA );
	final int[] seqB;// = stringToSymbol( sB );
	
	final int[] scoreA;// = fill( new int[seqA.length], 1 );
	
	final int lenA;// = seqA.length;
	final int lenB;// = seqB.length;
	
	final int W;// = LenA + 1;
	final int H;// = LenB + 1;
	
	final int[] score;// = new int[W*H];
	final int[] scoreL;// = new int[W*H];
	final byte[] dir;// = new byte[W*H];

	// initialized after call of 'traceback'
	int[] seqBOut = null;
	
	// traceback start position. Set after calling align*
	int tbStartA = -1;
	int tbStartB = -1;
	
	
	// stay flag in the main-matrix
	final static byte STAY = 0x1;
	
	// stay flag in the gap-matrix
	final static byte STAY_L = 0x2;
	
	
	// debug bits that can be set in the dir matrix without affecting the alignment algorithm
	final static byte BT_TOUCH = 0x4;
	final static byte BT_DIAG = 0x8;
	
	static int GAP_OPEN = 3;
	static int GAP_EXTEND = 1;
	
	static boolean COMPRESS_MATRIX = !true;
	static boolean PRINT_ALIGNMENT = false;
	
	int score_safe( int a, int b ) {
		if( validPos( a, b )) {
			return score[addr(a, b)];
		} else {
			return -1;
		}
	}
	
	int scoreL_safe( int a, int b ) {
		if( validPos( a, b )) {
			return scoreL[addr(a, b)];
		} else {
			return -1;
		}
	}
	
	byte dir_safe( int a, int b ) {
		if( validPos( a, b )) {
			return dir[addr(a, b)];
		} else {
			return -1;
		}
	}
	
	private boolean validPos(int a, int b) {
		return (!COMPRESS_MATRIX) || (a >= b || a <= b + (W-H));
	}



	static int gapCost( int l ) {
		if( l > 0 ) {
			return l * GAP_EXTEND + GAP_OPEN;
		} else {
			return 0;
		}
	}
	
	private static int[] fill(int[] js, int i) {
		Arrays.fill( js, i );
		return js;
	}

	final static int LARGE_VALUE = 100000;
	private static final boolean IGNORE_SCORE_A = true;
	private static final boolean DIR_VERBOSE = false;
	
	// address for 'matrix space' coordinate
	int addr( int a, int b ) {
		if( a < 0 || b < 0 ) {
			throw new RuntimeException( "a or b out of range" ); 
		}
		if( COMPRESS_MATRIX ) {
//			if( a < b || a > b + (W - H)) {
//				throw new RuntimeException( "bad maths exception " + a + " " + b + " " + W + " " + H );
//			}
			
			return b * (W - H + 1) + (a - b);
		} else {
			return b * W + a;
		}
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
	int saddr( int a, int b ) {
		return addr(a + 1, b + 1);
	}
	
	
	static int[] score_cached;
	static int[] scoreL_cached;
	static byte[] dir_cached;
	
	public ParsimonyAlign( int[] seqA, int[] seqB, int[] scoreA ) {
		this.seqA = seqA;
		this.seqB = seqB;
		this.scoreA = scoreA;
		
		lenA = seqA.length;
		lenB = seqB.length;
		
		W = lenA + 1;
		H = lenB + 1;

		int size;
		if( COMPRESS_MATRIX ) {
			size = (W - H + 1) * H;
		} else {
			size = W * H;
		}
		// array allocation seemed to cause quite some overhead.
		// get rid of allocation and (more importantly) unnecessary initialization
		// of the score arrays.
		if( true ) {
				
			if( score_cached == null || score_cached.length < size ) {
				score_cached = new int[size];
				scoreL_cached = new int[size];
				dir_cached = new byte[size];
			} else {
				//Arrays.fill(score_cached, 0, W*H, 0);
				//Arrays.fill(scoreL_cached, 0, W*H, 0);
				Arrays.fill(dir_cached, 0, size, (byte)0);
			}
			
			
			score = score_cached;
			scoreL = scoreL_cached;
			dir = dir_cached;
		} else {
			score = new int[size];
			scoreL = new int[size];
			dir = new byte[size];
		}
	}
	
	int align() {
			
		for( int ia = 0; ia <= lenA - lenB - 1; ia++ ) {
			scoreL[saddr(ia, -1)] = gapCost(ia+1);
			score[saddr(ia, -1)] = gapCost(ia+1);
			
			dir[saddr(ia, -1)] = STAY_L;
		}
	
		score[saddr(-1,-1)] = 0;
		scoreL[saddr(-1,-1)] = 0;
		
		for( int ib = 0; ib < lenB; ib++ ) {
			for( int ia = ib; ia <= ib + lenA - lenB; ia++ ) {
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				int saddr_0_0 = saddr(ia, ib);
				
				// calculate match score ('go diagonal')
				int sd = score[saddr(ia-1, ib-1)];
				
				if( !IGNORE_SCORE_A ) {
					sd += scoreA[ia];
				}
				
				// penalize based on parsimony
				if( (ca & cb) == 0 ) {
					sd += 1;
				}
				
				int sl;
				if( ia > ib ) {
					int saddr_1_0 = saddr(ia-1, ib);
					// calculate gap score. Either open or extend gap
					
					
					final int scoreExt;
					
					// only allow to jump to gap-extent matrix if we can have a gap of
					// length greater than one before hitting the left diagonal.
					if( ia > ib + 1 ) { 
						scoreExt = scoreL[saddr_1_0] + GAP_EXTEND;
					} else {
						scoreExt = LARGE_VALUE;
					}
					final int scoreOpen = score[saddr_1_0] + GAP_OPEN + GAP_EXTEND;
					
					if( /*ia > ib + 1 && */ scoreExt < scoreOpen ) {
						// gap gets extended. Set 'stay' flag in the gap-extension matrix,
						// so the traceback knows to stay in the extension state.
						sl = scoreExt;
						dir[saddr_0_0] |= STAY_L;
					} else {
						// gap gets openend. No stay flag means 'go left and switch to main-matrix'
						sl = scoreOpen;
					}
				} else {
					sl = LARGE_VALUE;
				}
			
				
				if( !IGNORE_SCORE_A ) {
					
					sl += scoreA[ia];
				}
				scoreL[saddr_0_0] = sl;
				
				// the ia > ib is there to prevent getting into a situation where we would have
				// to open a gap in seqA later on. (== getting below the left diagonal 
				// of the matrix (we can never 'go up' in a one sided alignment, so we get 'trapped'
				// under the diagonal))
				// TODO: check if a 'below the diagonal' incident could ever happen if everything
				// else is correct ... 
				// Mission Accomplished! TODO: could we optimize away filling the matrixes below the diagonal? (YES, WE CAN!)
				
				
				// left diagonal:
				// +----------
				// |\.........
				// |.\........
				// |..\.......
				// |...\......
				
				
				// choose between match or gap
				if( sl < sd && ia > ib ) {
					// open gap. No stay flag means 'go left and switch to gap-matrix'.
					score[saddr_0_0] = sl;
				} else {
					score[saddr_0_0] = sd;
					// match positions. Set 'stay' flag in the main-matrix (means 'go diagonal')
					dir[saddr_0_0] |= STAY;
				}
			}			
		}
		
		tbStartA = lenA-1;
		tbStartB = lenB-1;
				
		return score[saddr(lenA-1, lenB-1)];
	}

	int alignFreeshift() {
		
		for( int ia = 0; ia <= lenA - lenB - 1; ia++ ) {
			scoreL[saddr(ia, -1)] = 0;
			score[saddr(ia, -1)] = 0;
			
			dir[saddr(ia, -1)] = STAY_L;
		}
	
		score[saddr(-1,-1)] = 0;
		scoreL[saddr(-1,-1)] = 0;
		
		for( int ib = 0; ib < lenB; ib++ ) {
			for( int ia = ib; ia <= ib + lenA - lenB; ia++ ) {
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				int saddr_0_0 = saddr(ia, ib);
				
				// calculate match score ('go diagonal')
				int sd = score[saddr(ia-1, ib-1)];
				
				if( !IGNORE_SCORE_A ) {
					sd += scoreA[ia];
				}
				
				// penalize based on parsimony
				if( (ca & cb) == 0 ) {
					sd += 1;
				}
				
				int sl;
				if( ia > ib ) {
					int saddr_1_0 = saddr(ia-1, ib);
					// calculate gap score. Either open or extend gap
					
					
					final int scoreExt;
					
					// only allow to jump to gap-extent matrix if we can have a gap of
					// length greater than one before hitting the left diagonal.
					if( ia > ib + 1 ) { 
						scoreExt = scoreL[saddr_1_0] + GAP_EXTEND;
					} else {
						scoreExt = LARGE_VALUE;
					}
					final int scoreOpen = score[saddr_1_0] + GAP_OPEN + GAP_EXTEND;
					
					if( /*ia > ib + 1 && */ scoreExt < scoreOpen ) {
						// gap gets extended. Set 'stay' flag in the gap-extension matrix,
						// so the traceback knows to stay in the extension state.
						sl = scoreExt;
						dir[saddr_0_0] |= STAY_L;
					} else {
						// gap gets openend. No stay flag means 'go left and switch to main-matrix'
						sl = scoreOpen;
					}
				} else {
					sl = LARGE_VALUE;
				}
			
				
				if( !IGNORE_SCORE_A ) {
					
					sl += scoreA[ia];
				}
				scoreL[saddr_0_0] = sl;
				
				// the ia > ib is there to prevent getting into a situation where we would have
				// to open a gap in seqA later on. (== getting below the left diagonal 
				// of the matrix (we can never 'go up' in a one sided alignment, so we get 'trapped'
				// under the diagonal))
				// TODO: check if a 'below the diagonal' incident could ever happen if everything
				// else is correct ... 
				// Mission Accomplished! TODO: could we optimize away filling the matrixes below the diagonal? (YES, WE CAN!)
				
				
				// left diagonal:
				// +----------
				// |\.........
				// |.\........
				// |..\.......
				// |...\......
				
				
				// choose between match or gap
				if( sl < sd && ia > ib ) {
					// open gap. No stay flag means 'go left and switch to gap-matrix'.
					score[saddr_0_0] = sl;
				} else {
					score[saddr_0_0] = sd;
					// match positions. Set 'stay' flag in the main-matrix (means 'go diagonal')
					dir[saddr_0_0] |= STAY;
				}
			}			
		}
		
		// find (freeshift) traceback start position: best score in last row
		
		
		tbStartB = lenB-1;
		
		int best = LARGE_VALUE;
		
		for( int a = lenA-1; a >= lenB - 1; a-- ) {
			final int s = score[saddr(a, tbStartB)];
			if( s < best ) {
				best = s;
				tbStartA = a;
			}
		}		
		return score[saddr(tbStartA,tbStartB)];
	}

	
	private void traceback() {
		assert( tbStartB == lenB-1 );
		
		StringBuffer bsa = new StringBuffer();
		StringBuffer bsb = new StringBuffer();
		
		seqBOut = new int[seqA.length];
		int seqBIdx = 0;
		
		// output pregaps
		if( tbStartA < lenA - 1 ) {
			for( int ba = lenA - 1; ba > tbStartA; ba-- ) {
				bsa.append(Integer.toHexString(seqA[ba]));
				bsb.append("-");
				seqBOut[seqBIdx++] = 15;
			}
		}
		
		int ba = tbStartA;
		int bb = tbStartB;
		
		
		// traceback
		boolean inL = false;
		boolean btError = false;
		
		
		try {
			while( (ba >= 0 || bb >= 0)) {
//				if( ba < bb ) {
//					System.out.printf( "ba < bb @ %d %d\n", ba, bb );
////					btError = true;
////					break;
//				}
				if( DIR_VERBOSE ) {
					dir[saddr(ba, bb)] |= BT_TOUCH;
				}
			
				if( !inL ) {
					
					// we are currently not in a gap. see if we should match ('go diagonal') or open a gap ('jump to gap-matrix')
					if( (dir[saddr(ba, bb)] & STAY) != 0 ) 
					{
						// go diagonal
						bsa.append(Integer.toHexString(seqA[ba]));
						bsb.append(Integer.toHexString(seqB[bb]));
						seqBOut[seqBIdx++] =  seqB[bb];
						
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
					seqBOut[seqBIdx++] = 15;
					
					// check if we should stay in the gap-matrix
					inL = (dir[saddr(ba, bb)] & STAY_L) != 0;
					ba--;
				}
			}
		} catch( RuntimeException x ) {
			x.printStackTrace();
			btError = true;
		}
		System.out.printf( "%s\n%s\n", bsa.reverse(), bsb.reverse() );
		
		//btError = true;
		if( btError ) {
			int h = 40;
			int w = 40;
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					System.out.printf( "%d ", dir_safe(ia,ib) & STAY);
				}
				System.out.println();
			}
			System.out.println();
			
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					System.out.printf( "%d ", dir_safe(ia,ib) & STAY_L);
				}
				System.out.println();
			}
			System.out.println();
			
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					
					
					if( DIR_VERBOSE && ia == ib ) {
						dir[saddr(ia-1, ib-1)] |= BT_DIAG;
					}
					
					System.out.printf( "%x ", dir_safe(ia,ib) & (BT_TOUCH|BT_DIAG));
				}
				System.out.println();
			}
			System.out.println();
		
			
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					System.out.printf( "%x ", (int)dir_safe(ia,ib));
				}
				System.out.println();
			}
			System.out.println();
			
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					System.out.printf( "%d\t", score_safe(ia,ib) );
				}
				System.out.println();
			}
			System.out.println();
			
			
			for( int ib = 0; ib < h; ib++ ) {
				for( int ia = 0; ia < w; ia++ ) {
					System.out.printf( "%d\t", scoreL_safe(ia,ib) );
				}
				System.out.println();
			}
			System.out.println();
			throw new RuntimeException( "bailing out" );
		}
	}
	
	
	public static void main(String[] args) throws IOException {

		if( false ) {
			final int[] seqA;  
			final int[] seqB;
			
			final int[] scoreA;
			
			
			if( false ) {
				seqA  = stringToSymbol( sA );
				seqB = stringToSymbol( sB );
				
				scoreA = fill( new int[seqA.length], 1 );
			} else {
				BufferedReader r = new BufferedReader(new FileReader("/tmp/pars.txt"));	
				
				String lineB = r.readLine(); 
				
				seqB = readIntVec( lineB, true );
				seqA = readIntVec( r.readLine(), false );
				scoreA = readIntVec( r.readLine(), false );
				//scoreA = fill( new int[seqA.length], 0 );
				
				
				int[] seqBRaw = readIntVec( lineB, false );
				for( int i = 0; i < seqBRaw.length; i++ ) {
					System.out.printf( "%x", seqBRaw[i] );
				}
				System.out.println();
			}
			
			scoreA[6] = 4;
			
			ParsimonyAlign pa = new ParsimonyAlign(seqA, seqB, scoreA);
			pa.align();
		} else {

			int[] seqA;  
			int[] seqB;
			
			int[] scoreA;
			
			BufferedReader r = new BufferedReader(new FileReader("/scratch/pars.txt.PNAS100"));
			
			
			PrintWriter w = new PrintWriter(new File( "/scratch/pars.out.new" ));
			
			new Random( 123456 );
			
			long time1 = System.currentTimeMillis();
			long time2 = time1;
			int count = 0;
			
			while( true ) {
				
				String lineB = r.readLine(); 
			
				if( lineB == null ) {
					break;
				}
				
				String lineA = r.readLine();
				String lineScoreA = r.readLine();
				
				String lineInfo = r.readLine();
			
//				if( rnd.nextFloat() < 0.99 ) {
//					continue;
//				}
				
				String info = lineInfo;
			//	System.out.printf( "info: %s\n", info );
				
//				if( !lineInfo.equals("336 I2 18429")) {
//					continue;
//				}
				seqB = readIntVec( lineB, true );
				seqA = readIntVec( lineA, false );
				//scoreA = readIntVec( r.readLine(), false );
				scoreA = fill( new int[seqA.length], 0 );
				
				
				
				ParsimonyAlign pa = new ParsimonyAlign(seqA, seqB, scoreA);
				//int score = pa.align();
				int score = pa.alignFreeshift();
				if( PRINT_ALIGNMENT ) {
					pa.traceback();
					
					int[] seqBRaw = readIntVec( lineB, false );
					for( int i = 0; i < seqBRaw.length; i++ ) {
						System.out.printf( "%x", seqBRaw[i] );
					}
					System.out.println();
				}
				
				//int pscore = parsimonyScore( seqA, readIntVec(lineB, false), readIntVec(lineScoreA, false) );
				int pscore = -1;
				if( pa.getSeqBOut() != null ) {
					pscore = parsimonyScore( seqA, pa.getSeqBOut(), readIntVec(lineScoreA, false) );
					
					
									

				
					System.out.printf( "pscore: %d %s\n", pscore, info );
				}
				
				w.printf( "%s %d %d\n", info, score, pscore );
				w.flush();

				count++;
				
				if( PRINT_ALIGNMENT && count > 10 ) {
					break;
				}
				
				if( count % 1000 == 0 ) {
					System.out.printf( "time: %d\n", System.currentTimeMillis() - time2 );
					time2 = System.currentTimeMillis();
					
				}
			}
			
			
			w.close();
			
			System.out.printf( "time: %d\n", System.currentTimeMillis() - time1 );
		}
	}

	
	private int[] getSeqBOut() {
		return seqBOut;
	}

	private static int parsimonyScore(int[] seqA, int[] seqB, int[] scoreA) {
		int score = 0;
		
		for( int i = 0; i < seqA.length; i++ ) {
			score += scoreA[i];
			
			if( (seqA[i] & seqB[i]) == 0 ) {
				score++;
			}
		}
		
		return score;
	}

	private static int[] readIntVec(String line, boolean killGaps) {
		int[] tmp = new int[4096];
		
		StringTokenizer ts = new StringTokenizer(line);
		
		int i;
		for( i = 0; ts.hasMoreTokens(); i++ ) {
			tmp[i] = Integer.parseInt(ts.nextToken());
			if( killGaps && tmp[i] == 15 ) {
				i--;
				continue;
			}
		}
		
		return Arrays.copyOfRange(tmp, 0, i);
	}
}
