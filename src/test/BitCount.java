package test;

import java.util.Random;

public class BitCount {
	static byte[] bc16 = new byte[256*256];
	static byte[] bc8 = new byte[256];
	
	public static void main(String[] args) {
		for( int i = 0; i < bc16.length; i++ ) {
			bc16[i] = (byte) Long.bitCount(i);
		}
		
		for( int i = 0; i < bc8.length; i++ ) {
			bc8[i] = (byte) Long.bitCount(i);
		}
		Random rnd = new Random();
		System.out.printf( "%d %d\n", Integer.bitCount(0xFFFFFFFF), 0xffffffff);
		for( int i = 0; i < 1000000; i++ ) {
			int v = (int) (rnd.nextInt());
			int bc = bitcount16(v);
			int bcr = Integer.bitCount(v);
			
			if( bc != bcr ) {
				System.out.printf( "%d %d %d\n", v, bc, bcr );
			}
		}
	
		final int N = 1000000000;
		final int C = Integer.parseInt(args[0]);
		final int[] R = {4234234,52215,6643636,23423235};;
		rnd.setSeed(1234);
		long time1 = System.currentTimeMillis();
	
		int ret1 = 0;
		for( int i = 0; i < N; i++ ) {
			ret1 += bitcount8(R[i%4]/*rnd.nextInt()*/);
		}

		rnd.setSeed(1234);
		long time2 = System.currentTimeMillis();
		
		int ret2 = 0;
		for( int i = 0; i < N; i++ ) {
			ret2 += Integer.bitCount(R[i%4] /*rnd.nextInt()*/);
		}
		
		long time3 = System.currentTimeMillis();
		
		System.out.printf( "%d %d, %d %d\n", ret1, ret2, time2 - time1, time3 - time2 );
//		System.out.printf( "%d %d\n", bitcount16(1), bitcount16(9));
	}
	
	
	static int bitcount16( int v ) {
		return bc16[v & 0xffff] + bc16[(v>>16) & 0xffff];
	}
	
	static int bitcount8( int v ) {
		return bc8[v & 0xff] + bc8[(v>>8) & 0xff] + bc8[(v>>16) & 0xff] + bc8[(v>>24) & 0xff];
	}
}
