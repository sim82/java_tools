package ml;

import java.io.File;

public class SupportVsDepth {
    public static void main(String[] args) {
	File treeFile = new File(args[0]);

	LN n = TreeParser.parse(treeFile);

	LN[][] branches = LN.getAllBranchList3(n);

	for (LN[] br : branches) {
	    double nd = LN.getShortestPathBranchToTipND(br);

	    System.out.printf("%f %f\n", nd, br[0].backSupport);

	}

    }
}
