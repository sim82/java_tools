package ml;

import java.io.File;
import java.util.ArrayList;

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
				
				
					
				double lpc = LN.longestPathCorrected(node);
				if( longestPathCorr < lpc ) {
					new ArrayList<String>(LN.trace);
				}
				longestPathCorr = Math.max(longestPathCorr, lpc);
				
				
			}
		}


		//System.out.printf( "cnt: %d\n", cnt );

//		for( String s : longestTrace ) {
//			System.out.printf( "%s\n", s ) ;
//			
//		}
		System.out.printf( "factor: %f -> %f = %f\n", longestPath, longestPathCorr, longestPath / longestPathCorr );
	}
}
