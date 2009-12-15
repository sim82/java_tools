package ml;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Histogram<T> {
	SortedMap<T,Integer>	buckets = new TreeMap<T,Integer>();
	
	/*
	 * there is most likely some bad integer math going on in the bin functions.
	 * can't be bothered to check at the moment.
	 */
	public static int bin( float v, float min, float max, int nbins ) {
		assert( max > min && v >= min && v <= max );
		float range = max - min;
		float vn = (v - min) / range; 
		
		return (int) (vn * nbins);
		
	}
	
	public static int bin( double v, double min, double max, int nbins ) {
		assert( max > min && v >= min && v <= max );
		double range = max - min;
		double vn = (v - min) / range; 
		
		return (int) (vn * nbins);
		
	}
	
	public void add( T e ) {
		
		if( buckets.containsKey(e)) {
			int cnt = buckets.get(e);
			buckets.put(e, cnt+1);
		} else {
			buckets.put(e, 1);
		}
	}
	
	
	public void print() {
		for( Map.Entry<T, Integer> e : buckets.entrySet() ) {
			System.out.printf( "%s: %d\n", e.getKey().toString(), e.getValue() );
		}
	}
	
	public void printPoser() {
		int maxBucket = 0;
		int indent = 0;
		for( Map.Entry<T, Integer> e : buckets.entrySet() ) {
			//System.out.printf( "%s: %d\n", e.getKey().toString(), e.getValue() );
			maxBucket = Math.max(maxBucket, e.getValue());
			
			String s = "" + e.getKey().toString() + ": " + e.getValue();
			indent = Math.max( indent, s.length());
		}
		
		if( maxBucket == 0 ) {
			System.out.printf( "nothing to plot\n" );
			return;
		}
		
		final int cols = 80;
		
		for( Map.Entry<T, Integer> e : buckets.entrySet() ) {
			String name = e.getKey().toString();
			int num = e.getValue();
			float f = num / (float)maxBucket;
			assert( f >= 0.0 && f <= 1.0 );
			
			String s = "" + name + ": " + num;
			s += times( ' ', indent - s.length());
			
			String bar = times('=', Math.round(f * cols));
			
			System.out.printf( "%s |%s\n", s, bar );
		}
	}

	private String times(char c, int num) {
		char[] buf = new char[num];
		Arrays.fill( buf, c);
		return new String(buf);
//		StringBuffer s = new StringBuffer(num);
//		for( int i = 0; i < num; i++ ) {
//			s.append(c);
//		}
//		return s.toString();
	}
}