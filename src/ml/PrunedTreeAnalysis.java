package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PrunedTreeAnalysis {
	public static void main(String[] args) {
		final File reftreefile = new File( args[0] );
		final File prunesplitfile = new File( args[1] );
		
		final File oltfile;
		final File classfile;
		
		if( false ) {
			oltfile = new File( args[2] );
			classfile = new File( args[3] );
		} else {
			String runName = args[2];
			oltfile = new File( "RAxML_originalLabelledTree." + runName );
			classfile = new File( "RAxML_classification." + runName );
		}
		
		LN reftree = LN.parseTree( reftreefile );
		LN olt = LN.parseTree( oltfile );
		ClassifierOutput co = ClassifierOutput.read( classfile );
		
		String[] prunesplit = readSplit( prunesplitfile );
		
		LN reftreepruned;
		LN[] oip_reftree;
		{
			LN tmp = LN.deepClone(reftree);
			
			LN[] psbranch = LN.findBranchBySplit(tmp, prunesplit);
			LN removenode = psbranch[1];
			
			
			// remove the complete subtree ( = remove node psbranch[1] )
			oip_reftree = LN.removeNode(removenode.next.back, removenode.next.next.back);
			
			// use a node as pseudo root that will still be part of the tree after the remove (= one from the newly created branch).
			reftreepruned = oip_reftree[0];
			
			//System.out.printf( "%s\n", reftreepruned == null );
		}

//		// original insertion position on the olt
//		LN[] oip = LN.findCorrespondingBranch( oip_reftree, olt );
		
		for( ClassifierOutput.Res cr : co.reslist ) {
			
			// classifier insert position
			LN[] cip = ClassifierLTree.findBranchByName(olt, cr.branch);
			LN[] cip_reftree = LN.findCorrespondingBranch(cip, reftreepruned);
			
			int[] fuck = new int[1]; // some things (like 'multiple return values') are soo painful in java ...
            double lenOT = ClassifierLTree.getPathLenBranchToBranch(oip_reftree, cip_reftree, 0.5, fuck);
            double ndOT = fuck[0];
            
            System.out.printf( "%s %f %f\n", cr.seq, ndOT, lenOT );
		}
			
	}

	public static String[] readSplit(File prunesplitfile) {
		ArrayList<String> l;
		
		try {
			l = new ArrayList<String>();
			
			BufferedReader r = new BufferedReader( new FileReader( prunesplitfile ));
			
			String line;
			while( (line = r.readLine()) != null ) {
				l.add(line);
			}
		} catch( IOException x ) {
			x.printStackTrace();
			throw new RuntimeException( "bailing out");
		}
		
		return l.toArray(new String[l.size()]);
	}
}
