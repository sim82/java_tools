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



public class ParsimonyAlignVista {
	
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
	
	final int[] _score;// = new int[W*H];
	final int[] _scoreL;// = new int[W*H];
	
	final int[] dbscore;// = new int[W*H];
	final int[] dbscoreL;// = new int[W*H];
	
	
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
	
	
	static boolean PRINT_ALIGNMENT = false;
	
	
	byte dir_safe( int a, int b ) {
		return dir[addr(a, b)];
		
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
		
		return b + a * H;
		
	}

	// address for 'sequence space' coordinate (correct for trailing gap row/column)
	int saddr( int a, int b ) {
		return addr(a + 1, b + 1);
	}
	
	
	static int[] score_cached;
	static int[] scoreL_cached;
	static byte[] dir_cached;
	
	public ParsimonyAlignVista( int[] seqA, int[] seqB, int[] scoreA ) {
		this.seqA = seqA;
		this.seqB = seqB;
		this.scoreA = scoreA;
		
		lenA = seqA.length;
		lenB = seqB.length;
		
		W = lenA + 1;
		H = lenB + 1;

		int size;
		
		size = W * H;
		int strip = lenA - lenB + 1;
		
		// array allocation seemed to cause quite some overhead.
		// get rid of allocation and (more importantly) unnecessary initialization
		// of the score arrays.
		if( true ) {
				
			if( score_cached == null || score_cached.length < strip ) {
				score_cached = new int[strip];
				scoreL_cached = new int[strip];
			} 
			if( dir_cached == null || dir_cached.length < size ) {
				dir_cached = new byte[size];
				
			} else {
				//Arrays.fill(score_cached, 0, W*H, 0);
				//Arrays.fill(scoreL_cached, 0, W*H, 0);
				Arrays.fill(dir_cached, 0, size, (byte)0);
			}
			
			
			_score = score_cached;
			_scoreL = scoreL_cached;
		
//			dbscore = new int[size];			
//			dbscoreL = new int[size];
			dbscore = null;
			dbscoreL = null;
			
			dir = dir_cached;
		} else {
			_score = new int[size];
			_scoreL = new int[size];
			dir = new byte[size];
		}
	}
	
	int align() {
			
		for( int ia = 0; ia <= lenA - lenB - 1; ia++ ) {
			//_set_scoreL( ia, -1, gapCost(ia+1));
		//	_set_score( ia, -1, gapCost(ia+1));
			
			
//			scoreL[saddr(ia, -1)] = gapCost(ia+1);
//			score[saddr(ia, -1)] = gapCost(ia+1);
//			
			dir[saddr(ia, -1)] = STAY_L;
		}
	
//		score[saddr(-1,-1)] = 0;
//		scoreL[saddr(-1,-1)] = 0;
		
		//_set_score(-1,-1, 0);
		//_set_scoreL(-1,-1, 0);
		
		
		init_ib(0, -1);
		set_score_1_1(0);
		set_scoreL_0_0(0);
		
		
		for( int ia = 0; ia < lenA; ia++ ) {
			int bstart = Math.max(0, ia - (lenA - lenB));
			int bend = Math.min(lenB, ia + 1); 
			
			init_ib(bstart, gapCost(ia));
//			if( bstart == 0 ) {
//				set_score_1_1(gapCost(ia));
//				//set_scoreL_1_1(gapCost(ia));
//			} 
			for( int ib = bstart; ib < bend; inc_ib(++ib) ) {
			
				//_set_ib(ib);
				
				int ca = seqA[ia];
				int cb = seqB[ib];
				
				
				
				// calculate match score ('go diagonal')
				//int sd = score[saddr(ia-1, ib-1)];
				int sd = get_score_1_1();//(ia-1, ib-1);
				
				
				// penalize based on parsimony
				if( (ca & cb) == 0 ) {
					sd += 1;
				}
				
				int sl;
				if( ia > ib ) {
				//	int saddr_1_0 = saddr(ia-1, ib);
					// calculate gap score. Either open or extend gap
					
					
					final int scoreExt;
					
					// only allow to jump to gap-extent matrix if we can have a gap of
					// length greater than one before hitting the left diagonal.
					if( ia > ib + 1 ) { 
						//scoreExt = scoreL[saddr_1_0] + GAP_EXTEND;
						scoreExt = get_scoreL_1_0()/*(ia-1, ib)*/ + GAP_EXTEND;
						
					} else {
						scoreExt = LARGE_VALUE;
						
					}
					//final int scoreOpen = score[saddr_1_0] + GAP_OPEN + GAP_EXTEND;
					
					final int scoreOpen = get_score_1_0() /*(ia-1, ib)*/ + GAP_OPEN + GAP_EXTEND;
					
					
					if( /*ia > ib + 1 && */ scoreExt < scoreOpen ) {
						// gap gets extended. Set 'stay' flag in the gap-extension matrix,
						// so the traceback knows to stay in the extension state.
						sl = scoreExt;
						dir[saddr(ia, ib)] |= STAY_L;
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
				set_scoreL_0_0(sl);//(ia, ib, sl);
				
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
					set_score_0_0(sl);//(ia, ib, sl);
				} else {
					set_score_0_0(sd);//(ia, ib, sd);
					// match positions. Set 'stay' flag in the main-matrix (means 'go diagonal')
					dir[saddr(ia, ib)] |= STAY;
				}
			}			
		}
		
		tbStartA = lenA-1;
		tbStartB = lenB-1;
		
		g_ia = tbStartA;
		g_ib = tbStartB;
		//g_bstart = 0;
		return get_score_0_0();
	}

	

	int g_ia = -1;
	int g_ib = -1;
	//int g_bstart = -1;
//	private int score_0_0 = -1;
	private int score_1_1 = -1;
	private int score_1_0 = -1;
	
//	private int scoreL_0_0 = -1;
	private int scoreL_1_0 = -1;
	
	private void init_ib(int ib, int g) {
		//g_ib = ib;
	//	g_bstart = ib;
		g_ib = ib;
		if( ib == 0 ) {
			score_1_1 = g;
		} else {
			score_1_1 = _score[g_ib - 1];
		}
		//score_1_1 = LARGE_VALUE;
		score_1_0 = _score[g_ib];
		scoreL_1_0 = _scoreL[g_ib];
	}

	private void inc_ib( int ib ) {
		g_ib = ib;
		score_1_1 = score_1_0;
		score_1_0 = _score[g_ib];
		scoreL_1_0 = _scoreL[g_ib];
		
	}
	
//	private void init_ia() {
////		g_ia = 0;
////		score_1_0 = LARGE_VALUE;
////		score_1_1 = LARGE_VALUE;
////		scoreL_1_0 = LARGE_VALUE;
//	}
//	
//	private void inc_ia() {
////		g_ia++;
////		score_1_0 = LARGE_VALUE;
////		scoreL_1_0 = LARGE_VALUE;
////		score_1_1 = LARGE_VALUE;
//	}


	
	private void set_score_0_0(int sl) {
//		_set_score(g_ia, g_ib + g_bstart, sl);
		_score[g_ib] = sl;
		
//		score_0_0 = sl;
	}

	private void set_score_1_1(int sl) {
//		_set_score(g_ia - 1, g_ib + g_bstart- 1, sl);
		score_1_1 = sl;
	}
	
	
	private void set_scoreL_0_0(int sl) {
//		_set_scoreL(g_ia, g_ib + g_bstart, sl);
		_scoreL[g_ib] = sl;
		
//		scoreL_0_0 = sl;
	}

	private int get_score_1_0() {
////		int r = _get_score(g_ia - 1, g_ib + g_bstart);
//
//		assert( r == score_1_0 );
		//return r;
		return score_1_0;
	}

	private int get_scoreL_1_0() {
//		int r = _get_scoreL(g_ia - 1, g_ib + g_bstart);
//		assert( r == scoreL_1_0 );
		//return r;
		return scoreL_1_0;
	}

	private int get_score_1_1() {
//		int r = _get_score(g_ia - 1, g_ib + g_bstart - 1);
//		assert( r == score_1_1 );
//		//return r;
		return score_1_1;
	}

	private int get_score_0_0() {
//		int r = _get_score(g_ia, g_ib + g_bstart);
//		assert( r == score_0_0 );
		//return r;
//		return score_0_0;
		return _score[g_ib];
	}
	
//	private void _set_score(int ia, int ib, int sl) {
//		dbscore[saddr(ia, ib)] = sl;
//	}
//
//	private void _set_scoreL(int ia, int ib, int sl) {
//		dbscoreL[saddr(ia, ib)] = sl;
//	}
//
//	private int _get_scoreL(int ia, int ib) {
//		return dbscoreL[saddr(ia, ib)];
//	}
//
//	private int _get_score(int ia, int ib) {
//		return dbscore[saddr(ia, ib)];
//	}




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
			
//			for( int ib = 0; ib < h; ib++ ) {
//				for( int ia = 0; ia < w; ia++ ) {
//					System.out.printf( "%d\t", score_safe(ia,ib) );
//				}
//				System.out.println();
//			}
//			System.out.println();
//			
//			
//			for( int ib = 0; ib < h; ib++ ) {
//				for( int ia = 0; ia < w; ia++ ) {
//					System.out.printf( "%d\t", scoreL_safe(ia,ib) );
//				}
//				System.out.println();
//			}
//			System.out.println();
			throw new RuntimeException( "bailing out" );
		}
	}
	
	
	public static void main(String[] args) throws IOException {

		int[] seqA;  
		int[] seqB;
		
		int[] scoreA;
		
		BufferedReader r = new BufferedReader(new FileReader("/scratch/pars.txt.PNAS100"));
		
		
		PrintWriter w = new PrintWriter(new File( "/scratch/pars.out.XP" ));
		
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
			
			
			
			ParsimonyAlignVista pa = new ParsimonyAlignVista(seqA, seqB, scoreA);
			int score = pa.align();
			//int score = pa.alignFreeshift();
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
