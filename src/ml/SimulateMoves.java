package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class SimulateMoves {
    interface Visitor {
	void visit(LN n);

    }

    public static void main(String[] args) {

	File f = new File(args[0]);

	// File f = new File(
	// "/space/raxml/VINCENT/RAxML_bipartitions.150.BEST.WITH" );
	// File f = new File( "/home/sim/t8.tree" );

	LN t = TreeParser.parse(f);

	LN[][] allBranches = LN.getAllBranchList3(t);

	Random r = new Random();

	LN[] rb = allBranches[r.nextInt(allBranches.length)];

	new ArrayList<LN[]>();

	LN[] l0 = LN.getAsList(rb[0], false);
	LN[] l1 = LN.getAsList(rb[1], false);

	final LN n;
	final Set<String> tips;

	if (l0.length < l1.length) {
	    n = rb[1];
	    tips = LN.getTipSet(l0);

	} else {
	    n = rb[0];
	    tips = LN.getTipSet(l1);
	}

	System.out.print("move subtree:");
	for (String s : tips) {
	    System.out.printf(" %s", s);
	}
	System.out.println();

	// LN.getAllBranchList3Forward(n, ibranches);

	LN[] nbr = LN.removeBranch(n);
	// TreePrinter.printRaw(nbr[1], System.out );
	int depth = 5;

	Visitor writer = new Visitor() {

	    @Override
	    public void visit(LN n) {
		String filename = "/tmp/lsr_" + n.next.back.data.serial + "-"
			+ n.next.next.back.data.serial;
		System.out.printf("writing: %s\n", filename);
		PrintStream os;
		try {
		    os = new PrintStream(new FileOutputStream(filename));
		    TreePrinter.printRaw(n, os);

		    os.close();
		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	};

	System.out.printf("%s %s %s %s\n", nbr[0].next.back,
		nbr[0].next.next.back, nbr[1].next.back, nbr[1].next.next.back);

	if (nbr[0].next.back != null) {
	    insertRec(n, nbr[0].next, depth, writer);
	}

	if (nbr[0].next.next.back != null) {
	    insertRec(n, nbr[0].next.next, depth, writer);
	}

	if (nbr[1].next.back != null) {
	    insertRec(n, nbr[1].next, depth, writer);
	}

	if (nbr[1].next.next.back != null) {
	    insertRec(n, nbr[1].next.next, depth, writer);
	}

    }

    private static void insertRec(LN n, LN sbr, int depth, Visitor v) {
	if (depth < 1) {
	    return;
	}

	LN[] br = { sbr, sbr.back };

	LN.insertBranch(n, br);

	v.visit(n);

	LN.removeBranch(n);

	if (sbr.back.next.back != null) {
	    insertRec(n, sbr.back.next, depth - 1, v);
	}

	if (sbr.back.next.next.back != null) {
	    insertRec(n, sbr.back.next.next, depth - 1, v);
	}
    }

}
