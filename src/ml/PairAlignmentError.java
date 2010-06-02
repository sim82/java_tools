package ml;

import java.io.File;

import util.AlignTools;

public class PairAlignmentError {

	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(new File(args[0]));
		
		float cerr = 0.0f;
		int n = 0;
		
		for( int i = 0; i < ma.names.length; i++ ) {
			String name = ma.names[i];
			String oname = "x" + name;
			if( name.charAt(0) != 'x' && ma.nameMap.containsKey( oname )) {
				String seq1 = ma.data[i];
				String seq2 = ma.getSequence(oname);
				System.out.printf( "seq1: %s\n", seq1 );
				System.out.printf( "seq2: %s\n", seq2 );
				float err = AlignTools.alignError(seq1,	seq2);
				
				System.out.printf( "pw error: %f\n", err );
				cerr += err;
				n++;
			}
			
		}
		
		System.out.printf( "overal error: %f\n", cerr / n );
	}
	
}
