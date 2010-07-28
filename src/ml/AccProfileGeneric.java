package ml;

import java.io.File;
import java.util.BitSet;

public class AccProfileGeneric {
	public static void main(String[] args) {
		
		final boolean format2;
		int nextarg = 0;
		if( args[nextarg].equals( "-f2" )) {
			format2 = true;
			nextarg++;
		} else {
			format2 = false;
		}
		File clsFile = new File(args[nextarg]);
		nextarg++;
		
		ColumnOccMap occmap = new ColumnOccMap();
		for( ; nextarg < args.length; nextarg++ ) {
			String maName = args[nextarg];
			File maf = new File(maName);
			
			if( maf.isFile() && maf.canRead() ) {
//				MultipleAlignment ma = MultipleAlignment.loadPhylip(maf);
//				occmap.add(ma);
				
				LargePhylip lp = new LargePhylip(maf, false);
				occmap.add(lp);
			}
		}
		
		
		
		final String[] qss;
		final int[] dists;
		if( !format2 ) {
			CSVFile cf = CSVFile.load(clsFile, "SSSSIDD");
			qss = cf.getString(2);
			dists = cf.getInteger(4);
		} else {
			CSVFile cf = CSVFile.load(clsFile, "SSSIIDDD");
			qss = cf.getString(0);
			dists = cf.getInteger(4);
		}
		
		
		
		int[] coverage = null;
		int[] cdist = null;
		
		for( int i = 0; i < qss.length; i++ ) {
			BitSet bs = occmap.map.get(qss[i]);
			
			if( coverage == null ) {
				coverage = new int[bs.size()];
				cdist = new int[bs.size()];
			}
			
			
			for( int j = 0; j < coverage.length; j++ ) {
				
				if( bs.get(j) ) {
					
					coverage[j]++;
					cdist[j] += dists[i]; 
				}
			}
		}
		
		for( int i = 0; i < coverage.length; i++ ) {
			//coverage[i] = Math.max( coverage[i], 1 ); // add pseudocount of 1
			if( coverage[i] > 0 ) {
				System.out.printf( "%d %f\n", coverage[i], (float)cdist[i] / coverage[i] );
			} else {
				System.out.printf( "0 %f\n", Double.POSITIVE_INFINITY );
			}
		}
		//System.out.printf( "done %d\n", occmap.map.size() );
	}
}
