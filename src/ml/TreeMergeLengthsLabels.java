package ml;

import java.io.File;


public class TreeMergeLengthsLabels {
	public static void main(String[] args) {
		File lenFile = new File( args[0] );
		File lableFile = new File( args[1] );
		
		
		LN lenTree = TreeParser.parse(lenFile);
		LN lableTree = TreeParser.parse(lableFile);
		
		LN[][] lenBrl = LN.getAllBranchList3(lenTree);
		
		for( LN[] br : lenBrl ) {
			String[] split = LN.getSmallerSplitSet(br);
			
			try {
				LN[] lableBranch = LN.findBranchBySplit(lableTree, split);
				
//				System.out.printf( "lable: %s\n", lableBranch[0].backLabel );
				assert( br[0].backLen == br[1].backLen);
				lableBranch[0].backLen = br[0].backLen;
				lableBranch[1].backLen = br[1].backLen;
			} catch( RuntimeException x ) {
				System.out.printf( "LN.findBranchBySplit threw a RuntimeException.\nProbably the two trees have different topologies. Re-throwing the exception now.\n" );
				//x.printStackTrace();
				throw x;
				
			}
		}
		
		
		TreePrinter.printRaw(lableTree, System.out);
		
		
	}
}
