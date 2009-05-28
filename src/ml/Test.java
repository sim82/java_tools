package ml;

import java.io.File;

public class Test {
	public static void main(String[] args) {
		LN tree = LN.parseTree(new File("/space/raxml/VINCENT/RAxML_bipartitions.1604.BEST.WITH"));
		
		
		LN[] list = LN.getAsList(tree);
		
		for( LN n : list ) {
			
			if( n.data.isTip("M00Clon8") ) {
				n = LN.getTowardsTree(n);
				
				System.out.printf( "%f %f %f\n", n.backSupport, n.back.next.backSupport, n.back.next.next.backSupport );
				break;
			}
		}
	}
}
