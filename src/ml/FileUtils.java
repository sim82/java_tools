package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    static Map<String, String[]> parseSplits(File rnFile) {
	try {
	    BufferedReader r = new BufferedReader(new FileReader(rnFile));

	    Map<String, String[]> map = new HashMap<String, String[]>();

	    String line;

	    while ((line = r.readLine()) != null) {

		try {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();
		    String k = st.nextToken();
		    st.nextToken(); // skip the 'real neighbor'
		    String v = st.nextToken();

		    //
		    // parse the 'split list' (comma separated list of tips
		    // names)
		    //

		    StringTokenizer st2 = new StringTokenizer(v, ",");
		    int num = st2.countTokens();
		    if (num < 1) {
			throw new RuntimeException(
				"could not parse split list from real neighbor file");
		    }

		    String[] split = new String[num];

		    // System.out.printf( "split: %s\n", k );
		    for (int i = 0; i < num; i++) {
			split[i] = st2.nextToken();
			// System.out.printf( " '%s'", split[i] );
		    }
		    // split[num] = k;
		    // System.out.println();

		    map.put(k, split);
		} catch (NoSuchElementException x) {

		    System.out.printf("bad line in tsv file: " + line);
		    x.printStackTrace();
		    throw new RuntimeException("bailing out");
		}
	    }

	    r.close();

	    // for( Map.Entry<String,String> e : map.entrySet() ) {
	    // System.out.printf( "rnm: '%s' => '%s'\n", e.getKey(),
	    // e.getValue() );
	    //
	    // }

	    return map;

	} catch (IOException ex) {
	    Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE,
		    null, ex);

	    throw new RuntimeException("bailing out");
	}
    }
}
