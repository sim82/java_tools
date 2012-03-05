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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class ClassifierOutput {
    static class Res {
	final public String seq;
	final public String branch;
	final public double support;

	public Res(String seq, String branch, double support) {
	    this.seq = seq;
	    this.branch = branch;
	    this.support = support;
	}

	public Res(String seq, String branch, String support) {
	    this.seq = seq;
	    this.branch = branch;

	    if (support != null) {
		this.support = Double.parseDouble(support);
	    } else {
		this.support = Double.NaN;
	    }
	}
    }

    final public ArrayList<Res> reslist = new ArrayList<Res>();

    public static ClassifierOutput read(File classfile) {
	ClassifierOutput co;

	try {
	    co = new ClassifierOutput();

	    BufferedReader r = new BufferedReader(new FileReader(classfile));
	    String line;

	    while ((line = r.readLine()) != null) {
		StringTokenizer ts = new StringTokenizer(line);

		try {
		    Res res = new Res(ts.nextToken(), ts.nextToken(),
			    ts.nextToken());
		    co.reslist.add(res);
		} catch (NoSuchElementException x) {
		    x.printStackTrace();
		    throw new RuntimeException(
			    "failed to parse line from classfile: " + line);
		}
	    }
	} catch (IOException x) {
	    x.printStackTrace();
	    throw new RuntimeException("bailing out");
	}
	return co;

    }
}
