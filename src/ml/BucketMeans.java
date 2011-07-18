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

package ml;

import java.util.Arrays;

public class BucketMeans {
	
	float min;
	float max;
	int nbins;
	float[] bins;
	int[] counts;
	
	public BucketMeans( float min, float max, int nbins ) {
		bins = new float[nbins];
		counts = new int[nbins];
		Arrays.fill(counts, 1); // pseudocount to prevent possible div-by-zero
		
		this.min = min;
		this.max = max;
		this.nbins = nbins;
	}
	
	public void add( float k, float v ) {
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
	
	public void print() {
		float[] out = getValues();
		
		for( int i = 0; i < out.length; i++ ) {
			int bmin = (int) (min + ((max-min) / (float)nbins) * i);
			System.out.printf( "%d %f\n", bmin, out[i] );
		}
	}
}
