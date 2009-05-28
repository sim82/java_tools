package ml;

import java.io.File;
import java.util.HashSet;

public class FindMostInnerNode {
	public static void main(String[] args) {
		File infile = new File(args[0]);
		
		final TreeParser tp = new TreeParser(infile);
		
		
		
		final LN n = tp.parse();
		LN[] list = LN.getAsList(n);
		
	
		double maxd = -1.0;
		LN maxln = null;
		
		for( LN ln : list ) {
			if( ln.back != null && ln.data.isTip) {
				double lp = LN.shortestPath(ln);
				//System.out.printf( "%d %f\n", ln.data.serial, lp );
				
				if( lp > maxd ) {
					maxd = lp;
					maxln = ln;
				}
			}
		}
		
		System.out.printf( "max: %f %s\n", maxd, maxln.data.getTipName() );
		
//		LN[] nodeList = LN.getAsList(n);
//		int i = 0;
//		for( LN node : nodeList ) {
//			if( !node.data.isTip ) {
//				continue;
//			}
//			
//			if( node.data.getTipSerial() == maxln.data.getTipSerial()) {
//				System.out.printf( "idx: %d\n", i );
//				break;
//			}
//			
//			i++;
//		}
		
	}
}
