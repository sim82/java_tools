package ml;

import java.io.File;

public class MeanBranchSupport {
	public static void main(String[] args) {
		File infile = new File( args[0] );
		
		final TreeParser tp = new TreeParser(infile);
		LN n = tp.parse();
		
		//LN[] nodelist = LN.getAsList(n);
		
		LN[][] bl = LN.getBranchList(n);
		
		double sum = 0.0;
		double num = 0.0;
		
		for( LN[] br : bl ) {
			sum += br[0].backSupport;
			num += 1.0;
		}
		
		
		System.out.printf( "mean bs support: %f\n", sum / num );
	}
}
