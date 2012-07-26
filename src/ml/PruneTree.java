package ml;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class PruneTree {
    public static void main(String[] args) {
	final File treeFile = new File(args[0]);
	final File aliFile = new File(args[1]);

	LN t = TreeParser.parse(treeFile);
	MultipleAlignment ali = MultipleAlignment.loadPhylip(aliFile);

	LN[] lnList = LN.getAsList(t);
	Set<String> pruneNames = LN.getTipSet(lnList);

	pruneNames.removeAll(Arrays.asList(ali.names));

	for (String pn : pruneNames) {
	    LN[] newt = LN.removeTaxon(t, pn, false); // using the "unsafe"
						      // version, which allows
						      // to prune the
						      // pseudo-root
						      // (parameter t) if
						      // necessary.

	    assert (newt != null && newt[0] != null);

	    if (!newt[0].data.isTip) {
		t = newt[0];
	    } else if (!newt[1].data.isTip) {
		t = newt[1];

	    } else {
		throw new RuntimeException(
			"oops. got a branch with two tips... either the tree is about to disappear, or the tip pruning code is buggy...");
	    }
	}
	TreePrinter.printRaw(t, System.out);

    }
}
