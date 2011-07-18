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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Histogram<T> {
    SortedMap<T, Integer> buckets = new TreeMap<T, Integer>();

    /*
     * there is most likely some bad integer math going on in the bin functions.
     * can't be bothered to check at the moment.
     */
    public static int bin(float v, float min, float max, int nbins) {
	assert (max > min && v >= min && v <= max);
	float range = max - min;
	float vn = (v - min) / range;

	return (int) (vn * nbins);

    }

    public static int bin(double v, double min, double max, int nbins) {
	assert (max > min && v >= min && v <= max);
	double range = max - min;
	double vn = (v - min) / range;

	return (int) (vn * nbins);

    }

    public void add(T e) {

	if (buckets.containsKey(e)) {
	    int cnt = buckets.get(e);
	    buckets.put(e, cnt + 1);
	} else {
	    buckets.put(e, 1);
	}
    }

    public void print() {
	for (Map.Entry<T, Integer> e : buckets.entrySet()) {
	    System.out.printf("%s: %d\n", e.getKey().toString(), e.getValue());
	}
    }

    public void printPoser() {
	int maxBucket = 0;
	int indent = 0;
	for (Map.Entry<T, Integer> e : buckets.entrySet()) {
	    // System.out.printf( "%s: %d\n", e.getKey().toString(),
	    // e.getValue() );
	    maxBucket = Math.max(maxBucket, e.getValue());

	    String s = "" + e.getKey().toString() + ": " + e.getValue();
	    indent = Math.max(indent, s.length());
	}

	if (maxBucket == 0) {
	    System.out.printf("nothing to plot\n");
	    return;
	}

	final int cols = 80;

	for (Map.Entry<T, Integer> e : buckets.entrySet()) {
	    String name = e.getKey().toString();
	    int num = e.getValue();
	    float f = num / (float) maxBucket;
	    assert (f >= 0.0 && f <= 1.0);

	    String s = "" + name + ": " + num;
	    s += times(' ', indent - s.length());

	    String bar = times('=', Math.round(f * cols));

	    System.out.printf("%s |%s\n", s, bar);
	}
    }

    public void printPoser(double min, double max, int nbins) {
	int maxBucket = 0;
	int minBinId = Integer.MAX_VALUE;
	int maxBinId = 0;
	int indent = 4 + 1 + 4 + 1;

	// HashMap<T, String> namemap = new HashMap<T, String>();
	for (Map.Entry<T, Integer> e : buckets.entrySet()) {
	    // System.out.printf( "%s: %d\n", e.getKey().toString(),
	    // e.getValue() );
	    int bin = ((Integer) e.getKey()).intValue();
	    // double range = max - min;
	    // double tl = min + ((range / nbins) * bin);
	    // double th = tl + (range / nbins);
	    //
	    maxBinId = Math.max(bin, maxBinId);
	    minBinId = Math.min(bin, minBinId);
	    // String name = e.getKey().toString();

	    // String name = "" + tl + "-" + th;

	    maxBucket = Math.max(maxBucket, e.getValue());

	    // String s = "" + e.getKey().toString() + ": " + e.getValue();
	    // indent = Math.max( indent, name.length());
	    // namemap.put( e.getKey(), name );
	}

	if (maxBucket == 0) {
	    System.out.printf("nothing to plot\n");
	    return;
	}

	final int cols = 80;

	for (int i = minBinId; i <= maxBinId; i++) {

	    int bin = i;
	    double range = max - min;
	    double tl = min + ((range / nbins) * bin);
	    double th = tl + (range / nbins);

	    // String name = e.getKey().toString();

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);

	    pw.printf("%.2f-%.2f", tl, th);
	    String s = sw.toString();
	    // System.out.printf( "s: '%s'\n", s );
	    // String name = "" + tl + "-" + th;
	    // String s = name;

	    String bar = "";
	    if (buckets.containsKey(i)) {
		int num = buckets.get(i);
		float f = num / (float) maxBucket;
		assert (f >= 0.0 && f <= 1.0);

		bar = times('=', Math.round(f * cols));
	    }
	    s += times(' ', indent - s.length());

	    System.out.printf("%s |%s\n", s, bar);
	}
    }

    private String times(char c, int num) {
	char[] buf = new char[num];
	Arrays.fill(buf, c);
	return new String(buf);
	// StringBuffer s = new StringBuffer(num);
	// for( int i = 0; i < num; i++ ) {
	// s.append(c);
	// }
	// return s.toString();
    }
}