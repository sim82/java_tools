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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BlastFile {
    ArrayList<String> seqNames = new ArrayList<String>();
    Map<String, Double> bitscoreMap = new HashMap<String, Double>();
    private String queryName;

    public BlastFile(File file) {

	try {
	    BufferedReader r = new BufferedReader(new FileReader(file));

	    while (true) {
		String line = r.readLine();

		if (line == null) {
		    throw new RuntimeException(
			    "eof while looking for start of bitscore section");
		}

		if (line.startsWith("Query=")) {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();
		    this.queryName = st.nextToken();
		} else if (line
			.startsWith("Sequences producing significant alignments:")) {
		    r.readLine();
		    break;
		}
	    }

	    while (true) {
		String line = r.readLine();

		if (line == null) {
		    throw new RuntimeException(
			    "eof while still in bitscore section");
		}

		if (line.length() == 0) {
		    // System.out.printf( "end of score section\n" );
		    break;
		}

		StringTokenizer st = new StringTokenizer(line);
		String name = st.nextToken();
		String bitscoreS = st.nextToken();

		double bitscore = Double.parseDouble(bitscoreS);

		bitscoreMap.put(name, bitscore);
		seqNames.add(name);
	    }

	    r.close();

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();

	    throw new RuntimeException("bailing out");
	}

    }

    ArrayList<String> getSeqNames() {
	return seqNames;
    }

    double getBitscore(String seqName) {
	Double s = bitscoreMap.get(seqName);

	if (s != null) {
	    return s.doubleValue();
	} else {
	    throw new RuntimeException("seqName not in blastfile: '" + seqName
		    + "'");
	    // return s.POSITIVE_INFINITY;
	}

    }

    String getQueryName() {
	return queryName;
    }

}
