package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;


public class BurstAlign {
	
	static float[] a;
	static float[] b;
	
	static int addr( int i, int j ) {
		
		return i + j * (a.length + 1);
	}
	
	static float align() throws IOException {
		float mat[] = new float[(a.length + 1) * (b.length + 1)];
		
		Arrays.fill( mat, Float.POSITIVE_INFINITY);
		
		mat[0] = 0.0f;
		
		PrintWriter fw = new PrintWriter(new FileWriter( "/tmp/xxx.txt"));
		float x = 0;
		for( int i = 0; i < a.length; i++ ) {
			float ai = a[i];
			for( int j = 0; j < b.length; j++ ) {
				float bj = b[j];
				
				float sm = mat[addr( i, j )];
				float si = mat[addr( i + 1, j )];
				float sd = mat[addr( i, j + 1 )];
				
				float cost = Math.abs(ai - bj);
				
				x = mat[addr( i + 1, j + 1)] = cost + Math.min( sm, Math.min(si, sd));
				fw.printf( "x: %f\n", x );
			}
			
		}
		fw.close();
		return x;
//		return mat[addr( a.length, b.length)];
	}
	public static void main(String[] args) throws IOException {
		float[] x = new float[100];
		float[] y = new float[100];
		
		new Random();
		float d = 0;
		for( int i = 0; i < 100; i++ ) {
			x[i] = (float)Math.sin( (i / 100.0) * 8 * 3.14159 + 3.14159);
			y[i] = (float)Math.sin( (i / 100.0) * 8 * 3.14159);
//			
			System.out.printf( "%f %f\n", x[i], y[i] );
			d += Math.abs( x[i] - y[i] );
		}
		
		System.out.printf( "%f\n", d );
		a = x;
		b = y;
		float cost = align();
		
		System.out.printf( "cost: %f\n", cost );
		
	}
}
