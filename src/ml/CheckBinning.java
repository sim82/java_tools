package ml;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.management.RuntimeErrorException;

public class CheckBinning {
	public static void main(String[] args) {
		File treeFile = new File( args[0] );
		File classFile = new File( args[1] );
		File rnFile = new File( args[2] );
		File rtFile = new File( args[3] );
		
		Map<String, String[]> splitMap = FileUtils.parseSplits(rnFile);
		
		ClassifierOutput cf = ClassifierOutput.read( classFile );
		
		LN t = TreeParser.parse(treeFile);
		LN rt = LN.getNonTipNode( TreeParser.parse(rtFile));
		
		
		LN[][] branches = LN.getAllBranchList(t);
		
		
		Set<String> hs = new TreeSet<String>();
		for( LN[] branch : branches ) {
			hs.add( branch[0].backLabel );
		}
		
//		for( String s : hs ) {
//			System.out.printf( "%s ", s );
//		}
//		
//		System.out.printf( "\n" );
		
		int nSame = 0;
		int nSplits = 0;
		int nExcl = 0;
		final boolean VERBOSE = false;
		
		for( ClassifierOutput.Res res : cf.reslist ) {
			String[] sp = splitMap.get( res.seq );
			
			String brOrig = LN.findBranchBySplit(t, sp)[0].backLabel;
			
		
			
			Set<String> done = new HashSet<String>();
		
			LN rtp = LN.getNonTipNode(LN.removeTaxon(LN.deepClone(rt), res.seq)[0]);
			//LN[][] rtpBranches = LN.getBranchList(rtp);
//			Set<String> ts1 = LN.getTipSet(LN.getAsList(rt));
//			Set<String> ts2 = LN.getTipSet(LN.getAsList(rtp));
//			
//			System.out.printf( "sizes: %d %d\n", ts1.size(), ts2.size() );
			
			System.out.printf( " (%s %s) ", brOrig, res.branch ); 
			
			for( LN[] branch : branches ) {
				
				
				
				
				
				
				
				String cbr = branch[0].backLabel;
				
				
				if( done.contains( cbr )) {
					continue;
				}
				
				done.add( cbr );
				if( cbr.equals(brOrig) || cbr.equals(res.branch)) {
					LN[] brOrigRt = LN.findCorrespondingBranch(LN.findBranchByName(t, brOrig), rtp );
					LN[] brRt = LN.findCorrespondingBranch(LN.findBranchByName(t, res.branch), rtp );
					
					
				//	System.out.printf( "exclude: (%s %s) %f %f\n", brOrig, res.branch, brOrigRt[0].backSupport, brRt[0].backSupport );
					continue;
				}
				
				
				
				LN[] rtBranch = LN.findCorrespondingBranch(branch, rtp);
				double support = rtBranch[0].backSupport;
				if( support < 75 ) {
					continue;
				}
				
				
				
				
				
				LN.checkLinkSymmetry(rtBranch[0]);
				
				
				if( VERBOSE ) {
					System.out.printf( "support: %f\n", support );	
				}
				
				Set<String> nl1 = ClassifierTwoClades.getBranchLabelSet( LN.getAsList(branch[0], false) );
				Set<String> nl2 = ClassifierTwoClades.getBranchLabelSet( LN.getAsList(branch[1], false) );
		
				final boolean same;
				
				if( nl1.contains(brOrig ) ) { 
					same = nl1.contains(res.branch);
				} else if( nl2.contains(brOrig )) {
					same = nl2.contains(res.branch);
				} else {
					throw new RuntimeException( "none of the split trees contains the original branch");
				}
				
				nSplits++;
				if( same ) {
					nSame++;
				}
				if( VERBOSE ) {
					System.out.printf( "same %s (%s %s): %s\n", cbr, brOrig, res.branch, same );
				}
				
				
			}
			
		}
		System.out.printf( "same: %d of %d %f%% %d\n", nSame, nSplits, nSame / (float)nSplits * 100.0, nExcl ); 
		
		
	}
}
