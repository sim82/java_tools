package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;

public class ClassifierOutputFilterByTree {
    public static void main(String[] args) throws IOException {
	File cofile = new File(args[0]);
	File treefile = new File(args[1]);

	LN n = TreeParser.parse(treefile);
	Set<String> ts = LN.getTipSet(LN.getAsList(n));

	BufferedReader br = new BufferedReader(new FileReader(cofile));

	String line;
	while ((line = br.readLine()) != null) {
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.hasMoreTokens() && ts.contains(st.nextToken())) {
		System.out.println(line);
	    }
	}

    }
}
