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
		
		public Res( String seq, String branch, double support) {
			this.seq = seq;
			this.branch = branch;
			this.support = support;
		}
		public Res( String seq, String branch, String support) {
			this.seq = seq;
			this.branch = branch;
			
			if( support != null ) {
				this.support = Double.parseDouble(support);
			} else {
				this.support = Double.NaN;
			}
		}
	}

	final public ArrayList<Res> reslist = new ArrayList<Res>();
	
	public static ClassifierOutput read( File classfile ) {
		ClassifierOutput co;
		
		try {
			co = new ClassifierOutput();
			
			BufferedReader r = new BufferedReader( new FileReader( classfile ));
			String line;
			
			while( (line = r.readLine()) != null ) {
				StringTokenizer ts = new StringTokenizer(line);
				
				try {
					Res res = new Res( ts.nextToken(), ts.nextToken(), ts.nextToken());
					co.reslist.add( res );
				} catch( NoSuchElementException x ) {
					x.printStackTrace();
					throw new RuntimeException( "failed to parse line from classfile: " + line );
				}
			}
		} catch( IOException x ) {
			x.printStackTrace();
			throw new RuntimeException( "bailing out");
		}
		return co;
			
	}
}
