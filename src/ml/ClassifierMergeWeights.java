package ml;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

class PlacementResultsLHWeight {
    static class QS implements Comparable<QS> {
	String name;

	class Placement {
	    String branchName;
	    float lhWeight;

	    Placement( String branchName, float lhw ) {
		this.branchName = branchName;
		this.lhWeight = lhw;
	    }
	}

	ArrayList<Placement> placements = new ArrayList<PlacementResultsLHWeight.QS.Placement>();


	QS( String name ) {
	    this.name = name;
	}

	void add( String branchName, float lhw ) {
	    placements.add( new Placement(branchName, lhw));

	}

	String getName() {
	    return name;
	}

	Placement getPlacement( int index ) {
	    return placements.get(index);
	}



	@Override
	public int compareTo(QS o) {
	    return name.compareTo(o.name);
	}

    }



    Map<String,QS> qsMap = new HashMap<String, PlacementResultsLHWeight.QS>();
    ArrayList<QS> qsList = new ArrayList<PlacementResultsLHWeight.QS>();

    PlacementResultsLHWeight( BufferedReader r ) {
	String line;

	try {

	    while( (line = r.readLine()) != null ) {
		StringTokenizer st = new StringTokenizer(line);
		if( st.countTokens() != 4 ) {
		    throw new RuntimeException( "bad line in classification file (not 4 token): '" + line + "'" );
		}
		String qsName = st.nextToken();

		QS qs = qsMap.get(qsName);
		if( qs == null ) {
		    qs = new QS( qsName );
		    qsMap.put( qsName, qs );
		    qsList.add( qs );
		    //		            fireIntervalAdded(this, qsList.size() - 1, qsList.size() - 1);
		}

		String branchName = st.nextToken();
		float lhw = Float.parseFloat(st.nextToken());

		qs.add( branchName, lhw );
		Collections.sort(qsList);

	    }


	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}


    }



    public QS getQS( int index ) {
	return qsList.get(index);
    }


}



public class ClassifierMergeWeights {
    static void indent( PrintWriter w, int n) {
	for( int i = 0; i < n; ++i ) {
	    w.print(' ');
	}
    }
    static Map<String,Color> colorMap;

    static String removePrefix( String s, String p ) {
	if( s.startsWith(p)) {
	    return s.substring(p.length());
	} else {
	    return s;
	}
    }
    static void printPhyxmlClade( LN n, PrintWriter w, int indent ) {
	indent( w, indent );
	w.println( "<clade>" );
	if( n.data == null ) {
	    throw new RuntimeException();
	}

	if( n.data.isTip ) {
	    indent( w, indent + 2 );

	    String name = removePrefix(n.data.getTipName(), "QUERY___");
	    w.printf( "<name>%s</name>\n", name );

	    indent( w, indent + 2 );
	    w.printf( "<branch_length>%f</branch_length>\n", n.backLen );

	    Color col = colorMap.get( n.data.getTipName());

	    if( col != null ) {
		indent( w, indent + 2 );
		w.println("<color>");
		indent( w, indent + 4 );
		w.printf("<red>%d</red>\n", col.getRed() );
		indent( w, indent + 4 );
		w.printf("<green>%d</green>\n", col.getGreen() );
		indent( w, indent + 4 );
		w.printf("<blue>%d</blue>\n", col.getBlue() );
		indent( w, indent + 2 );
		w.println("</color>" );

	    }
	} else {
	    indent( w, indent + 2 );
	    w.printf( "<branch_length>%f</branch_length>\n", n.backLen );

	    LN nx = n.next;

	    while( nx != n ) {
		printPhyxmlClade( nx.back, w, indent + 2 );
		nx = nx.next;
	    }
	}
	indent( w, indent );
	w.println( "</clade>" );
    }
    static void printPhyxml( LN n, PrintWriter w, boolean back ) {
	w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<phyloxml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd\" xmlns=\"http://www.phyloxml.org\">\n<phylogeny rooted=\"false\">\n");
	indent(w,2);
	w.println( "<clade>" );
	if( back ) {
	    printPhyxmlClade( n.back, w, 2 );
	}

	LN nx = n.next;

	while( nx != n ) {
	    printPhyxmlClade( nx.back, w, 2 );
	    nx = nx.next;
	}

	indent( w, 2 );
	w.println( "</clade>" );
	w.println("</phylogeny>\n</phyloxml>\n");
	w.flush();
    }


    static int clampColor( float c ) {
	return Math.max( 0, Math.min( (int)c, 255 ));
    }

    static Color interpolate( Color c1, Color c2, float v ) {
	int dr = c2.getRed() - c1.getRed();
	int dg = c2.getGreen() - c1.getGreen();
	int db = c2.getBlue() - c1.getBlue();


	int r = clampColor(c1.getRed() + v * dr);
	int g = clampColor(c1.getGreen() + v * dg);
	int b = clampColor(c1.getBlue() + v * db);
	//System.err.printf( "%d %d %d\n", r, g, b );
	return new Color( r, g, b );
    }

    public static void main(String[] args) throws FileNotFoundException {
	LN tree = TreeParserMulti.parse(new File( args[0] ));

	PlacementResultsLHWeight res = new PlacementResultsLHWeight( new BufferedReader(new FileReader( args[1] )));

	colorMap = new HashMap<String, Color>();

	for( PlacementResultsLHWeight.QS qs : res.qsList ) {
	    Color c = interpolate(Color.RED, Color.BLUE, qs.placements.get(0).lhWeight );
	    colorMap.put("QUERY___" + qs.name, c);
	}

	printPhyxml(tree, new PrintWriter(System.out), true);
    }
}
