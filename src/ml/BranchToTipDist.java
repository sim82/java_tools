package ml;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;



public class BranchToTipDist {
	public static void main(String[] args) {
		File classfile = new File("/space/newPNAS/RAxML_classification.PNAS");
		File oltfile = new File("/space/newPNAS/RAxML_originalLabelledTree.PNAS");
		
		
		ClassifierOutput co = ClassifierOutput.read(classfile);
		
		
		SortedSet<String> branchSet = new TreeSet<String>();
		
		LN olt = LN.parseTree(oltfile);
		
		for( ClassifierOutput.Res res : co.reslist ) {
			branchSet.add(res.branch);
			
			LN[] branch = LN.findBranchByName(olt, res.branch);
			int d1 = ndToNearestTip(branch[0]);
			int d2 = ndToNearestTip(branch[1]);
			
			int mind = Math.min(d1, d2);
			System.out.printf( "%s %s\t%d\t%d\n", res.seq, res.branch, (int)res.support, mind );
		}
		if( true ) {
			throw new RuntimeException( "exit" );
		}
		
		Histogram<Integer> hist = new Histogram<Integer>();
		
		for( String branchname : branchSet ) {
			LN[] branch = LN.findBranchByName(olt, branchname);
			
			int d1 = ndToNearestTip(branch[0]);
			int d2 = ndToNearestTip(branch[1]);
			
			int mind = Math.min(d1, d2);
			System.out.printf( "%s\t%d\n", branchname, mind );
			hist.add(mind);
		}
		hist.print();
		hist.printPoser();
	}
	
	
	public static int ndToNearestTip( LN n ) {
		if( n.data.isTip ) {
			return 0;
		} else {
			int d1 = ndToNearestTip(n.next.back);
			int d2 = ndToNearestTip(n.next.next.back);
			
			return Math.min( d1, d2 ) + 1;
		}
	}
}
