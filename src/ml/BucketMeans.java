package ml;

import java.util.Arrays;

public class BucketMeans {
	
	float min;
	float max;
	int nbins;
	float[] bins;
	int[] counts;
	
	BucketMeans( float min, float max, int nbins ) {
		bins = new float[nbins];
		counts = new int[nbins];
		Arrays.fill(counts, 1); // pseudocount to prevent possible div-by-zero
		
		this.min = min;
		this.max = max;
		this.nbins = nbins;
	}
	
	void add( float k, float v ) {
		int bin = Histogram.bin(k, min, max, nbins);
		bin = Math.min( nbins - 1, Math.max(0, bin)); // hope my dodgy interger maths don't spoil the results ...
		
		bins[bin] += v;
		counts[bin]++;
	}
	
	
	float[] getValues() {
		float[] out = bins.clone();
		
		for( int i = 0; i < out.length; i++ ) {
			out[i] /= counts[i];
		}
		
		return out;
	}
	
	void print() {
		float[] out = getValues();
		
		for( int i = 0; i < out.length; i++ ) {
			int bmin = (int) (min + ((max-min) / (float)nbins) * i);
			System.out.printf( "%d %f\n", bmin, out[i] );
		}
	}
}
