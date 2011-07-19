/*
 * Copyright (C) 2011 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */


package ml;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ml.LN.TipCollectVisitor;

import org.forester.archaeopteryx.MainFrame;
import org.forester.io.writers.PhylogenyWriter;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.BranchColor;
import org.forester.phylogeny.data.BranchData;

class PerfTimer {
    final long start;

    PerfTimer() {
	start = System.currentTimeMillis();
    }

    void print() {

	System.err.printf("elapsed: %d\n", System.currentTimeMillis() - start);
    }

}

class ConvertToForester {
    final HashSet<UnorderedPair<LN, LN>>[] branchFound;

    ConvertToForester(HashSet<UnorderedPair<LN, LN>>[] branchFound) {
	this.branchFound = branchFound;
    }

    void colorBlend(int[] c1, int[] c2) {
	for (int i = 0; i < c1.length; i++) {
	    c1[i] += c2[i];
	    c1[i] = Math.min(c1[i], 255);
	}

    }
    int nfound = 0;
    PhylogenyNode trav(LN n, boolean back) {
	PhylogenyNode fn = new PhylogenyNode();

	if (!back) {

	    final int[] color;
	    if (branchFound.length == 1) {
		// pairwise color-scheme
		final int[] x = { 255, 255, 255 }; // java initializer lists
						   // syntax is stupid!
		color = x;
		boolean found = branchFound[0].contains(new UnorderedPair<LN, LN>(n, n.back));
		if (!found) {
		    color[1] = 0;
		    color[2] = 0;

		    if( n.data.isTip ) {
			System.out.printf( "tip: %s %s %s\n", found ? "true" : "false", n.data.toString(), n.back.data.toString() );
			throw new RuntimeException( "consistency check failed while creating forester phylogeny: trivial split not marked.");
			
		    }
		    
		} else {
		    nfound++;
		}
		

		
	    } else if (branchFound.length <= 3) {
		// rgb scheme for 1-to-[2,3] comparison

		int[][] colors = { { 255, 0, 0 }, { 0, 255, 0 }, { 0, 0, 255 } };

		if (!n.data.isTip) {
		    final int[] x = { 32, 32, 32 };
		    color = x;

		    UnorderedPair<LN, LN> key = new UnorderedPair<LN, LN>(
			    n, n.back);
		    for (int i = 0; i < branchFound.length; i++) {
			if (branchFound[i].contains(key)) {
			    colorBlend(color, colors[i]);
			}

		    }
		} else {
		    final int[] x = { 96, 96, 96 };
		    color = x;

		}
	    } else {
		throw new RuntimeException(
			"no color-scheme for more than 3 tree comparison. bailing out.");

	    }

	    BranchData bd = new BranchData();
	    bd.setBranchColor(new BranchColor(new Color(color[0], color[1],
		    color[2])));

	    if (n.data.isTip) {
		fn.setName(n.data.getTipName());
	    }

	    fn.setBranchData(bd);
	    fn.setDistanceToParent(n.backLen);

	}

	if (back) {
	    assert (n.back != null);
	    fn.addAsChild(trav(n.back, false));
	}
	if (!n.data.isTip) {

	    fn.addAsChild(trav(n.next.back, false));
	    fn.addAsChild(trav(n.next.next.back, false));
	}
	return fn;
    }

};

public class VisualTreeDiffNT {


    public static void main(String[] args) {
	

	
	
	if (args.length >= 2) {
	    boolean showArch = false;
	    ArrayList<File> files = new ArrayList<File>();
	    
	    //treeFiles = new File[args.length];
	    for (int i = 0; i < args.length; i++) {
		if( args[i].equals("-v")) {
		    showArch = true;
		} else {
		    File f = new File(args[i]); 
		    
		    if (!f.canRead() || !f.isFile()) {
			throw new RuntimeException(
			    "illegal parameter: not a file or not readable: "
				    + args[i]);
		    
		    }
		    files.add(f);
		}
	    }
	    VisualTreeDiffNT xxx = new VisualTreeDiffNT();
		
	    xxx.runDiff(files.toArray( new File[files.size()]), showArch);
	} else {
	    try {
		String curDir = System.getProperty("user.dir");

		final JFileChooser fc = new JFileChooser(curDir);

		fc.setPreferredSize(new Dimension(800, 600));
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(new File(curDir));

		ArrayList<File> files = new ArrayList<File>();

		while (files.size() < 2) {
		    int returnVal = fc.showOpenDialog(null);

		    if (returnVal != JFileChooser.APPROVE_OPTION) {
			throw new RuntimeException(
				"file chooser cancelled with less than 2 files already selected. bailing out.");
		    }

		    File[] f1 = fc.getSelectedFiles();
		    files.addAll(Arrays.asList(f1));
		}

		File[] treeFiles = files.toArray(new File[files.size()]);

		
		VisualTreeDiffNT xxx = new VisualTreeDiffNT();
		xxx.runDiff(treeFiles, true);

	    } catch (RuntimeException x) {
		x.printStackTrace();

		JOptionPane.showMessageDialog(null, x.getMessage(),
			"Runtime error", JOptionPane.ERROR_MESSAGE);
	    }

	}
    }

    void runDiff(File[] treeFiles, boolean showArch) {
	File t1_name = treeFiles[0];

	LN t1 = TreeParser.parse(t1_name);
	if( t1 == null ) {
	    throw new RuntimeException( "could not parse newick file: " + t1_name );
	}
	
	PerfTimer timer1;
	LN.CollectBranchSplitSets cbss1;

	cbss1 = new LN.CollectBranchSplitSets(t1);
	cbss1.convertToSmaller();

	
	timer1 = new PerfTimer();

	
	final int numQTrees = treeFiles.length - 1;
	
	
	final Map<BitSet, UnorderedPair<LN, LN>> t1_hash = cbss1.splits;

	timer1.print();
	
	final HashSet<UnorderedPair<LN,LN>>[] branchFound = new HashSet[numQTrees];

	for (int qTree = 0; qTree < treeFiles.length - 1; qTree++) {
	    PerfTimer timer = new PerfTimer();

	    File t2_name = treeFiles[qTree + 1];
	    LN t2 = TreeParser.parse(t2_name);

	    if( t2 == null ) {
		throw new RuntimeException( "could not parse newick file: " + t2_name );
	    }
	    
	    LN.CollectBranchSplitSets cbss_q = new LN.CollectBranchSplitSets(t2);
	    cbss_q.convertToSmaller();
	    timer.print();

	    if( cbss1.tcv.tips.size() != cbss_q.tcv.tips.size() ) {
		throw new RuntimeException("inconsitent tip sets in trees (the trees can not be compared).\nthe trees have different sizes: "+ cbss1.tcv.tips.size() + " vs " + cbss_q.tcv.tips.size());
	    }
	    
	    for (int i = 0; i < cbss1.tcv.tips.size(); i++) {
		if (!cbss1.tcv.tips.get(i).data.getTipName().equals(
			cbss_q.tcv.tips.get(i).data.getTipName())) {
		    throw new RuntimeException("inconsitent tip sets in trees (the trees can not be compared).\nfirst inconsitent taxon: " + cbss1.tcv.tips.get(i).data.getTipName() + " vs " + cbss_q.tcv.tips.get(i).data.getTipName());
		}
	    }

	    branchFound[qTree] = new HashSet<UnorderedPair<LN, LN>>();



	    //LN[][] t2_br = LN.getAllBranchList3(t2);
	    //
	    // System.err.printf( "br: %d %d\n", t1_br.length, t2_br.length );
	    //
	    // assert( t1_br.length == t2_br.length );

	    //BitSet[] t2_splits = new BitSet[t2_br.length];

	    BitSet[] t2_splits = cbss_q.splits.keySet().toArray(new BitSet[cbss_q.splits.keySet().size()]);

	    

	    if (false) {
//		int nFound = 0;
//		for (int i = 0; i < t1_splits.length; i++) {
//		    boolean found = false;
//
//		    // if( t1_splits[i].cardinality() > 1 ) {
//		    for (int j = 0; j < t2_splits.length; j++) {
//			if (t1_splits[i].equals(t2_splits[j])) {
//			    nFound++;
//			    found = true;
//			    break;
//
//			}
//		    }
//
//		    if (found) {
//			branchFound[qTree].add(new UnorderedPair<ANode, ANode>(
//				t1_br[i][0].data, t1_br[i][1].data));
//
//		    }
//		}
	    } else {
		int nfound = 0;
		int ntips = 0;
		
		for (int j = 0; j < t2_splits.length; j++) {
		    UnorderedPair<LN, LN> br_j = t1_hash.get(t2_splits[j]);
		    
		    
		    if( t2_splits[j].cardinality() == 1 && br_j == null ) {
			throw new RuntimeException( "consitency check failed: did not find trivial split in other tree." );
		    }
//		    
//		    if( t2_splits[j].cardinality() == 1 ) {
//			ntips++;
//			System.out.printf( "found tip: %s %s\n", br_j.get1(), br_j.get2() ); 
//		    }
		    
		    if (br_j != null) {
			branchFound[qTree].add(br_j);
			nfound++;

		    }
		}

		System.err.printf("nfound: %d %d %d\n", nfound, ntips, branchFound[qTree].size());
	    }
	    timer.print();
	}



	Phylogeny phy = new Phylogeny();
	ConvertToForester ctf = new ConvertToForester(branchFound);
	PhylogenyNode fn = ctf.trav(t1, true);
	
	phy.setRoot(fn);
	phy.setRooted(false);

	if (!showArch) {
	    try {
		Writer w = new OutputStreamWriter(System.out);

		PhylogenyWriter.createPhylogenyWriter().toPhyloXML(w, phy, 0);
		w.flush();

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();

	    }
	} else {
	    System.err
		    .printf("Tree visualization using Forester/Archeopteryx (http://www.phylosoft.org/forester)\n");
	    
	    
	    Phylogeny[] phys = {phy};
	    Object xxx = new Object();
	    
	    
	    
	    final File tmp;	    
	    try {
		tmp = File.createTempFile("arc", ".txt");
		ClassLoader cl = getClass().getClassLoader();
		    	
		InputStream is = cl.getResourceAsStream("_aptx_configuration_file.txt");
		FileOutputStream os = new FileOutputStream(tmp);
		byte[] buf = new byte[4096];
		while(true ) {
		    int read = is.read(buf);
		    os.write(buf,0,read);
		    
		    if( read != buf.length ) {
			break;
		    }
		}
		os.close();
		tmp.deleteOnExit();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException( "error while creating temp file" );
 	    }
	    MainFrame x = org.forester.archaeopteryx.Archaeopteryx.createApplication(phys, tmp.getPath(), "" );
	    
	   
	    
	    // archStarted = true;
	}

    }

    static BitSet splitToBitset(String[] splitSet, String[] refOrder) {
	// Arrays.sort( splitSet );

	BitSet bs = new BitSet(refOrder.length);

	for (int i = 0, j = 0; i < refOrder.length && j < splitSet.length; ++i) {
	    if (refOrder[i].equals(splitSet[j])) {
		bs.set(i);
		j++;
	    }
	}

	return bs;
    }
}
