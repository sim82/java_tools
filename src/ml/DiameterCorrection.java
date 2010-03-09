package ml;

import java.io.File;

public class DiameterCorrection {
	public static void main(String[] args) {
		File treeFile = new File( args[0] );
		
		LN n = TreeParser.parse(treeFile);
		
		
		
		LN[] list = LN.getAsList(n);

		double longestPath = 0.0;
		double longestPathCorr = 0.0;


		for( LN node : list ) {
			if( node.data.isTip && node.back != null ) {
			

				longestPath = Math.max( longestPath, LN.longestPath(node));
				longestPathCorr = Math.max(longestPathCorr, LN.longestPathCorrected(node));
			}
		}


		//System.out.printf( "cnt: %d\n", cnt );

		System.out.printf( "factor: %f -> %f = %f\n", longestPath, longestPathCorr, longestPath / longestPathCorr );
	}
}
