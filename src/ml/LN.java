/*
 * Copyright (C) 2009 Simon A. Berger
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class LN {

    public ANode data;
    public LN next;
    public LN back;
    double backLen;
    String backLabel;
    int backId;
    public double backSupport;

    public LN(ANode data) {
	this.data = data;
    }

    public static LN create() {
	ANode data = new ANode();

	LN n = new LN(data);
	n.next = new LN(data);
	n.next.next = new LN(data);
	n.next.next.next = n;
	return n;
    }

    public static LN[] getAsList(LN n) {
	return getAsList(n, true);
    }

    public static LN[] getAsList(LN n, boolean back) {
	int nNodes = countNodes(n, back);
	LN[] list = new LN[nNodes];

	int xpos = insertDFS(n, list, 0, back);

	if (xpos != nNodes) {
	    throw new RuntimeException("xpos != nNodes");
	}

	return list;
    }

    public static LN[] getAsList2(LN n, boolean back) {
	LN[] list = new LN[1024 * 10];

	int xpos = insertDFS(n, list, 0, back);

	return Arrays.copyOf(list, xpos);
    }

    public static String[] getAllBranchNameList(LN n) {
	LN[][] brl = getAllBranchList(n);

	String[] nl = new String[brl.length];
	for (int i = 0; i < nl.length; i++) {
	    nl[i] = brl[i][0].backLabel;
	}

	return nl;
    }

    // public static String[] getBranchNameList( LN n ) {
    // LN[][] brl = getBranchList(n);
    //
    // String[] nl = new String[brl.length];
    // for( int i = 0; i < nl.length; i++ ) {
    // nl[i] = brl[i][0].backLabel;
    // }
    //
    // return nl;
    // }

    public static LN[][] getBranchList(LN n) {
	if (n.data.isTip) {
	    throw new RuntimeException("we don't like tips!");
	}

	ArrayList<LN[]> list = new ArrayList<LN[]>();

	getBranchListForward(n, list);
	getBranchListForward(n.next, list);
	getBranchListForward(n.next.next, list);

	return list.toArray(new LN[list.size()][]);
    }

    private static void getBranchListForward(LN n, ArrayList<LN[]> list) {
	if (!n.data.isTip) {
	    LN[] b = { n, n.back };

	    branchSanityCheck(b);

	    list.add(b);

	    getBranchListForward(n.next.back, list);
	    getBranchListForward(n.next.next.back, list);
	}
    }

    public static LN[][] getAllBranchList(LN n) {
	if (n.data.isTip) {
	    throw new RuntimeException("we don't like tips!");
	}

	ArrayList<LN[]> list = new ArrayList<LN[]>();

	getAllBranchListForward(n, list);
	getAllBranchListForward(n.next, list);
	getAllBranchListForward(n.next.next, list);

	return list.toArray(new LN[list.size()][]);
    }

    private static void getAllBranchListForward(LN n, ArrayList<LN[]> list) {
	LN[] b = { n, n.back };
	branchSanityCheck(b);
	list.add(b);

	if (!n.data.isTip) {

	    getAllBranchListForward(n.next.back, list);
	    getAllBranchListForward(n.next.next.back, list);
	}
    }

    public static LN[][] getAllBranchList2(LN n) {
	// this is exactly the same as getAllBranchList. It is here
	// only to add quirkyness

	if (n.data.isTip) {
	    throw new RuntimeException("we don't like tips!");
	}

	ArrayList<LN[]> list = new ArrayList<LN[]>();

	getAllBranchListForward(n, list);
	getAllBranchListForward(n.next, list);
	getAllBranchListForward(n.next.next, list);

	return list.toArray(new LN[list.size()][]);
    }

    public static LN[][] getAllBranchList3(LN n) {
	// this is the only version of this function which actually works
	// correctly.
	// the other versions return most branches more than once. They seem to
	// return _all_ branches, so most code that used them so far should have
	// worked (e.g. adding all branch names to a set)

	if (n.data.isTip) {
	    throw new RuntimeException("we don't like tips!");
	}

	ArrayList<LN[]> list = new ArrayList<LN[]>();

	getAllBranchList3Forward(n.back, list);
	getAllBranchList3Forward(n.next.back, list);
	getAllBranchList3Forward(n.next.next.back, list);

	return list.toArray(new LN[list.size()][]);
    }

    static void getAllBranchList3Forward(LN n, ArrayList<LN[]> list) {

	LN[] b = { n.back, n };
	branchSanityCheck(b);
	list.add(b);

	if (!n.data.isTip) {
	    getAllBranchList3Forward(n.next.back, list);
	    getAllBranchList3Forward(n.next.next.back, list);
	}

    }

    public interface BranchVisitor {

	public void visit(LN[] br);
    }

    public static class BranchIndexUpdate implements BranchVisitor {
	int idx = 0;

	@Override
	public void visit(LN[] br) {
	    br[0].backId = br[1].backId = idx++;
	}
    }

    public static void visitBranchesPreorder(LN n, BranchVisitor v,
	    boolean backward) {

	if (backward) {
	    if (n.data.isTip) {
		throw new RuntimeException(
			"we don't like tips as start node for traversal!");
	    }

	    visitBranchesPreorder(n.back, v, false);
	} else {
	    LN[] br = { n.back, n };
	    v.visit(br);
	}

	if (!n.data.isTip) {
	    visitBranchesPreorder(n.next.back, v, false);
	    visitBranchesPreorder(n.next.next.back, v, false);
	}
    }

    public interface NodeVisitor {
	public void visit(LN n);

    }

    public static class TipIndexUpdate implements NodeVisitor {
	int idx = 0;

	@Override
	public void visit(LN n) {
	    if (n.data.isTip) {
		n.data.setTipSerial(idx++);
		System.out.printf("tip serial: %d\n", n.data.getTipSerial());
	    }
	}
    }

    public static class TipIndexUpdateAlphabetic implements NodeVisitor {
	public TreeMap<String, ANode> map = new TreeMap<String, ANode>();

	@Override
	public void visit(LN n) {
	    if (n.data.isTip) {
		map.put(n.data.getTipName(), n.data);
	    }
	}

	public void commit() {
	    int idx = 0;

	    String prev = "";

	    for (Entry<String, ANode> p : map.entrySet()) {
		if (p.getKey().equals(prev)) {
		    throw new RuntimeException(
			    "duplicate tip name in tree: '%s" + p.getKey()
				    + "'");
		}

		prev = p.getKey();
		p.getValue().setTipSerial(idx++);

		System.out.printf("update: %s %d\n", p.getValue().getTipName(),
			p.getValue().getTipSerial());
	    }
	}
    }

    public static class TipCollectVisitor implements NodeVisitor {

	public ArrayList<LN> tips = new ArrayList<LN>();

	@Override
	public void visit(LN n) {
	    if (n.data.isTip) {
		tips.add(n);
	    }
	}
    }

    // this should be typically called with the 'tips' array from a
    // TipCollectVisitor
    // parameter 'tips' will be modified.
    public static void tipIndexUpdateAlphabetic(ArrayList<LN> tips) {
	Comparator<LN> comp = new Comparator<LN>() {

	    @Override
	    public int compare(LN o1, LN o2) {
		return o1.data.getTipName().compareTo(o2.data.getTipName());
	    }

	};

	Collections.sort(tips, comp);
	int idx = 0;
	String prev = "";
	for (LN t : tips) {
	    if (t.data.getTipName().equals(prev)) {
		throw new RuntimeException("duplicate tip name in tree: '%s"
			+ t.data.getTipName() + "'");
	    }

	    prev = t.data.getTipName();

	    t.data.setTipSerial(idx++);
	    // System.out.printf( "serial: %s %d\n", t.data.getTipName(),
	    // t.data.getTipSerial() );
	}
    }

    public static void visitNodesPreorder(LN n, NodeVisitor v, boolean backwards) {
	v.visit(n);

	if (!n.data.isTip) {
	    if (backwards) {
		visitNodesPreorder(n.back, v, false);
	    }

	    visitNodesPreorder(n.next.back, v, false);
	    visitNodesPreorder(n.next.next.back, v, false);
	} else {
	    if (backwards) {

		throw new RuntimeException(
			"BAD: tip as starting node for traversal.");
	    }
	}
    }

    public static class VisitNodesBottomUp {
	//
	// This contraption is supposed to do kind of a breadth-first flooding
	// of a tree.
	// When the flooding is started at the tips, it will do a bottom-up
	// traversal.
	// A node will be visited after two of it's neighbor nodes have been
	// visited.
	//
	// WARNING: the AData.serial numbers have to be unique (which _should_
	// be enforced by AData, anyway...)!
	// The traversal order is implicitly defined by the order of the
	// AData.serial values.
	//
	// TODO: It is currently only tested for the case that the flooding
	// starts at _all_ tips.
	// It might throw or fail to terminate in any other case.
	// TODO: Maybe use it to re-implement findBranchBySplit. It's basically
	// the same.
	//

	// Set<ANode> done;
	static class Pre {
	    public LN pre;
	    public Object res;

	    Pre(LN pre, Object res) {
		this.pre = pre;
		this.res = res;
	    }

	}

	public interface Visitor {
	    // the Pre objects are either null (for tip nodes), or contain the
	    // previously
	    // visited neigbor nodes and the Objects returned by their 'visit'
	    // calls.
	    Object visit(LN towardsRest, Pre pre1, Pre pre2);

	}

	Map<Integer, Pre[]> open = new HashMap<Integer, Pre[]>();

	Visitor v = null;

	// LN findCont( LN n ) {
	// int ctDone = 0;
	//
	// LN undone = null;
	// LN nnext = n;
	// do {
	// if( done.contains(nnext.back.data)) {
	// ctDone++;
	// } else {
	// undone = nnext;
	// }
	// nnext = n.next;
	// } while( nnext != n );
	//
	// if( ctDone == 2) {
	// return undone;
	// } else {
	// return null;
	// }
	//
	// }
	//
	void addOpen(LN n, LN pre, Object preRes) {
	    assert (pre != null);

	    Pre[] ent = open.get(n.data.serial);

	    if (ent != null) {
		if (ent[0] == null) {
		    throw new RuntimeException("ent[0] == null");
		}
		if (ent[1] != null) {
		    System.err.printf("already open: %s %s %s %s %s\n",
			    ent[0].pre, ent[1].pre, pre, pre.back.next.back,
			    pre.back.next.next.back);
		    // throw new RuntimeException( "ent[1] != null" );
		    return;
		}

		ent[1] = new Pre(pre, preRes);

	    } else {
		Pre[] x = { new Pre(pre, preRes), null };
		open.put(n.data.serial, x);
	    }
	}

	VisitNodesBottomUp(Collection<LN> tips, Visitor v) {
	    this.v = v;

	    for (LN t : tips) {
		t = LN.getTowardsTree(t);

		if (t.back.data.isTip) {
		    throw new RuntimeException(
			    "tip neighboring tip. me no likee. bailing out.");
		}

		// done.add( t.data );
		Object res = v.visit(t, null, null);

		addOpen(t.back, t, res);
		// addOpen( t.back );

	    }

	}

	void run() {

	    while (!open.isEmpty()) {

		Iterator<Entry<Integer, Pre[]>> iter = open.entrySet()
			.iterator();
		while (iter.hasNext()) {
		    Entry<Integer, Pre[]> n = iter.next();

		    Pre[] pre = n.getValue();

		    // if( open.size() == 1 ) {
		    // System.out.printf( "pre: %s %s\n", pre[0], pre[1] );
		    // }

		    if (pre[0] != null && pre[1] != null) {
			final LN towardsRest;

			if (pre[0].pre.back.next != pre[1].pre.back) {
			    towardsRest = pre[0].pre.back.next;
			    // addOpen(pre[0].pre.back.next.back,
			    // pre[0].pre.back.next, res);
			} else if (pre[1].pre.back.next != pre[0].pre.back) {
			    towardsRest = pre[1].pre.back.next;
			    // addOpen(pre[1].pre.back.next.back,
			    // pre[1].pre.back.next, res);
			} else {
			    throw new RuntimeException(
				    "quirk: pre-nodes do not point back to same node");
			}

			if (n.getKey() != towardsRest.data.serial) {
			    throw new RuntimeException(
				    "quirk: n.getKey() != towardsRest.data");
			}

			Object res = v.visit(towardsRest, pre[0], pre[1]);
			iter.remove();

			if (pre[0].pre.back.data.serial != n.getKey()
				|| pre[1].pre.back.data.serial != n.getKey()) {
			    throw new RuntimeException(
				    "pre[0].back.data != n.getKey() || pre[1].back.data != n.getKey()");
			}

			if (!open.isEmpty()) {
			    addOpen(towardsRest.back, towardsRest, res);

			}
			break;
		    }

		}

	    }
	}

    }

    static class CollectBranchSplitSets {
	public final Map<BitSet, UnorderedPair<LN, LN>> splits = new HashMap<BitSet, UnorderedPair<LN, LN>>();
	public final int numTips;
	public final TipCollectVisitor tcv = new TipCollectVisitor();

	public CollectBranchSplitSets(LN t1) {

	    // PerfTimer timer1 = new PerfTimer();

	    //
	    // collect tip nodes from t1
	    //

	    LN.visitNodesPreorder(t1, tcv, true);

	    //
	    // update the tip serial number (stored in the ANode objects
	    // associated with the tips)
	    // to the lexicographic order of the tip names.
	    //
	    LN.tipIndexUpdateAlphabetic(tcv.tips);

	    numTips = tcv.tips.size();

	    //
	    // collect the split-sets (=bit vectors) for all nodes using a
	    // bottom-up node visitor.
	    // If the n-th bit in a split set is true this means that the n-th
	    // tip (according to the
	    // lexicographic ordering as mentionend above) is present in the
	    // subtree below that node.
	    //
	    // Store one of the split-sets in a map indexed by branches
	    // (represented by UnorderedSet<LN,LN>).
	    // These are not necessarily the _smaller_ split sets (but they can
	    // be converted to the smaller split
	    // by flipping the bits, if necessary, see method convertToSmaller)
	    //
	    VisitNodesBottomUp.Visitor v = new VisitNodesBottomUp.Visitor() {

		@Override
		public Object visit(LN n, VisitNodesBottomUp.Pre pre1,
			VisitNodesBottomUp.Pre pre2) {

		    if (n.back == null) {
			throw new RuntimeException("n.back == null");
		    }
		    final LN[] br = { n, n.back };
		    final BitSet bs;

		    if (n.data.isTip) {
			// System.out.printf( "visit tip: %s\n",
			// n.data.getTipName() );

			bs = new BitSet(numTips);
			bs.set(n.data.getTipSerial());
		    } else {
			bs = (BitSet) ((BitSet) pre1.res).clone();
			bs.or((BitSet) pre2.res);

			// System.out.printf( "visit inner: %d, %d %d\n",
			// n.data.serial, pre1.pre.data.serial,
			// pre2.pre.data.serial );
		    }

		    splits.put(bs, new UnorderedPair<LN, LN>(br));
		    return bs;
		}
	    };

	    VisitNodesBottomUp vnbu = new VisitNodesBottomUp(tcv.tips, v);
	    vnbu.run();

	}

	void convertToSmaller() {
	    Iterator<Entry<BitSet, UnorderedPair<LN, LN>>> esi = splits.entrySet().iterator();
	    
	    HashMap<BitSet, UnorderedPair<LN, LN>> tmp = new HashMap<BitSet, UnorderedPair<LN,LN>>();
	    
	    while( esi.hasNext() ) {
		Entry<BitSet, UnorderedPair<LN, LN>> v = esi.next();
		if (v.getKey().cardinality() > numTips / 2) {
		    esi.remove();
		    
		    v.getKey().flip(0, numTips);
		    tmp.put(v.getKey(), v.getValue());
		    
		}
	    }
	    
	    splits.putAll(tmp);
	}
	// timer1.print();

    }

    public static void branchSanityCheck(LN[] b) {
	if (((b[0].backLabel != null && b[1].backLabel != null) && !b[0].backLabel
		.equals(b[1].backLabel))
		|| ((b[0].backLabel == null || b[1].backLabel == null) && b[0].backLabel != b[1].backLabel)) {
	    throw new RuntimeException(
		    "! b[0].backLabel.equals(b[1].backLabel): "
			    + b[0].backLabel + " " + b[1].backLabel);
	}
	if (b[0].backLen != b[1].backLen) {
	    throw new RuntimeException("b[0].backLen != b[1].backLen");
	}
	if (b[0].backSupport != b[1].backSupport) {
	    throw new RuntimeException("b[0].backSupport != b[1].backSupport");
	}

    }

    private static int countNodes(LN n) {
	return countNodes(n, true);
    }

    private static int countNodes(LN n, boolean back) {
	// if (n.data.isTip) {
	// return 1;
	// } else {
	// return 1 + countNodes(n.next.back) + countNodes(n.next.next.back);
	// }

	if (n == null) {
	    return 0;
	} else {
	    if (back) {
		return 1 + countNodes(n.back, false)
			+ countNodes(n.next.back, false)
			+ countNodes(n.next.next.back, false);
	    } else {
		return 1 + countNodes(n.next.back, false)
			+ countNodes(n.next.next.back, false);
	    }
	}

    }

    static int insertDFS(LN n, LN[] list, int pos) {
	// if (n.data.isTip) {
	// list[pos] = n;
	// return pos + 1;
	// } else {
	//
	// pos = insertDFS(n.next.back, list, pos);
	// pos = insertDFS(n.next.next.back, list, pos);
	// list[pos] = n;
	// return pos + 1;
	// }

	return insertDFS(n, list, pos, true);
    }

    private static int insertDFS(LN n, LN[] list, int pos, boolean back) {
	if (n != null) {
	    if (back) {
		pos = insertDFS(n.back, list, pos, false);
	    }
	    pos = insertDFS(n.next.back, list, pos, false);
	    pos = insertDFS(n.next.next.back, list, pos, false);
	    list[pos] = n;
	    return pos + 1;
	} else {
	    return pos;
	}

    }

    static int insertDFS(LN n, ArrayList<LN> list, int pos, boolean back) {
	if (n != null) {
	    if (back) {
		pos = insertDFS(n.back, list, pos, false);
	    }
	    pos = insertDFS(n.next.back, list, pos, false);
	    pos = insertDFS(n.next.next.back, list, pos, false);
	    list.add(n);
	    return pos + 1;
	} else {
	    return pos;
	}

    }

    public static LN getTowardsTree(LN ln) {
	if (!ln.data.isTip) {
	    throw new RuntimeException(
		    "this method must only be called for tip LNs");
	}

	for (int i = 0; i < 3; i++, ln = ln.next) {
	    if (ln.back != null) {
		return ln;
	    }
	}

	throw new RuntimeException(
		"this seems to be a completely unlinked LN (all LNs in ring have no back pointer");
    }

    static LN getTowardsNonTip(LN n) {
	for (int i = 0; i < 3; i++, n = n.next) {
	    if (!n.back.data.isTip) {
		break;
	    }
	}

	return n;

    }

    static LN getTowardsTip(LN n) {
	LN start = n;
	LN cur = n.next;

	while (cur != start) {
	    if (cur.back.data.isTip) {
		break;
	    }
	    cur = cur.next;

	}

	return cur;
    }

    static ArrayList<String> getTipList(LN[] list) {
	ArrayList<String> set = new ArrayList<String>();

	for (LN n : list) {
	    if (n.data.isTip) {
		set.add(n.data.getTipName());
	    }
	}

	return set;
    }

    static Set<String> getTipSet(LN[] list) {
	HashSet<String> set = new HashSet<String>();

	for (LN n : list) {
	    if (n.data.isTip) {
		set.add(n.data.getTipName());
	    }
	}

	return set;
    }

    static LN[] findBranchBySplitTrivial(LN n, String tipName) {
	LN[] list = getAsList(n);

	for (LN ln : list) {
	    if (ln.data.isTip && ln.data.getTipName().equals(tipName)) {
		LN[] ret = { ln, ln.back };
		return ret;
	    }
	}
	//
	// for( LN ln : list ) {
	// if( ln.data.isTip ) {
	// System.out.print( ln.data.getTipName() + " " );
	// }
	// }

	throw new RuntimeException("could not find node for tip '" + tipName
		+ "'");

    }

    static LN[] findBranchBySplit(LN n, String[] split) {
	if (split.length == 1) {
	    // trivial case, won't work with the algorithm below

	    return findBranchBySplitTrivial(n, split[0]);
	}

	LN[] list = getAsList(n);

	Map<String, LN> tipIndex = new HashMap<String, LN>();
	Map<Integer, LN> nodeIndex = new HashMap<Integer, LN>();

	// build indices: tipname => LN and node-serialnumber => LN
	for (LN ln : list) {
	    if (ln.data.isTip) {
		tipIndex.put(ln.data.getTipName(), ln);
	    }

	    if (!nodeIndex.containsKey(n.data.serial)) {
		nodeIndex.put(ln.data.serial, ln);
	    }
	}

	//
	// ad-hoc algorithm for finding the separating branch, given one
	// split-set:
	// flood the tree from the tips of the split-set:
	//
	// nodes get 'marked' if they have two marked
	// neighbors (ancestors). At the beginning only the leafs from one
	// split-set are marked.
	// The recursion stops at the branch that separates the two split-sets
	// (which can never have two
	// marked ancestors (at least this is my theory...))
	// MEMO TO SELF: this description is rubbish
	//
	Set<Integer> markedNodes = new HashSet<Integer>();
	Set<Integer> openNodes = new HashSet<Integer>(markedNodes);

	for (String name : split) {
	    LN tip = tipIndex.get(name);

	    if (tip == null) {
		throw new RuntimeException("tip '" + name
			+ "' not found in tip index");
	    }

	    markedNodes.add(tip.data.serial);

	    tip = LN.getTowardsTree(tip);

	    openNodes.add(tip.back.data.serial);
	}

	while (openNodes.size() > 1) {

	    int lastNode = -1;
	    LN lastLN = null;

	    // if this is too slow, we could keep track of the mark-count
	    // and use a priority queue:

	    for (int next : openNodes) {
		LN oln = nodeIndex.get(next);

		int markedCount = 0;
		for (int i = 0; i < 3; i++, oln = oln.next) {
		    if (markedNodes.contains(oln.back.data.serial)) {
			markedCount++;
		    }
		}

		if (markedCount == 2) {
		    lastNode = next;
		    lastLN = oln;
		    break;
		}
	    }

	    if (lastNode < 0) {
		for (String s : split) {
		    System.out.printf("%s ", s);
		}
		System.out.println();
		throw new RuntimeException(
			"no open node with two marked neighbors found. looks like a bad split");
	    }

	    openNodes.remove(lastNode);
	    markedNodes.add(lastNode);

	    for (int i = 0; i < 3; i++, lastLN = lastLN.next) {
		if (!markedNodes.contains(lastLN.back.data.serial)) {
		    openNodes.add(lastLN.back.data.serial);
		}
	    }

	}

	if (openNodes.size() != 1) {
	    throw new RuntimeException(
		    "split finder finished but openNodes.size != 1 !?");
	}

	// the last node on the open list is on one side of the separating
	LN n1 = nodeIndex.get(openNodes.iterator().next());

	// the neighbor that is not in the 'marked' set is the other half of the
	// separating branch

	for (int i = 0; i < 3; i++, n1 = n1.next) {
	    if (!markedNodes.contains(n1.back.data.serial)) {
		// NOTE: ret[0] always points towards the subtree identified by
		// the split set

		LN[] ret = { n1, n1.back };
		return ret;
	    }
	}

	throw new RuntimeException(
		"the node next to the split does not have an unmarked neighbor");

    }

    public static String[] getSmallerSplitSet(LN[] branch) {
	LN[] ll = LN.getAsList(branch[0], false);
	LN[] lr = LN.getAsList(branch[1], false);

	Set<String> sl = LN.getTipSet(ll);
	Set<String> sr = LN.getTipSet(lr);

	Set<String> smallset = (sl.size() <= sr.size()) ? sl : sr;

	// System.out.printf( "M00Clon8: %s %s %s\n", sl.contains("M00Clon8"),
	// sr.contains("M00Clon8"), smallset.contains("M00Clon8"));
	// System.out.printf( "ts: %d %d %d %d\n", ll.length, lr.length,
	// sl.size(), sr.size() );

	// System.exit(0);
	String[] split = smallset.toArray(new String[smallset.size()]);
	Arrays.sort(split);

	return split;
    }

    public static String[] getSmallerSplitSet2(LN[] branch) {
	LN[] ll = LN.getAsList2(branch[0], false);
	LN[] lr = LN.getAsList2(branch[1], false);

	ArrayList<String> sl = LN.getTipList(ll);
	ArrayList<String> sr = LN.getTipList(lr);

	ArrayList<String> smallset = (sl.size() <= sr.size()) ? sl : sr;

	// System.out.printf( "M00Clon8: %s %s %s\n", sl.contains("M00Clon8"),
	// sr.contains("M00Clon8"), smallset.contains("M00Clon8"));
	// System.out.printf( "ts: %d %d %d %d\n", ll.length, lr.length,
	// sl.size(), sr.size() );

	// System.exit(0);
	String[] split = smallset.toArray(new String[smallset.size()]);
	Arrays.sort(split);

	return split;
    }

    public static LN[] removeTaxon(LN n, String taxon) {
	return removeTaxon(n, taxon, true);
    }

    public static LN[] removeTaxon(LN n, String taxon, boolean safe) {
	if (n.data.isTip(taxon)) {
	    // keep it simple
	    throw new RuntimeException(
		    "ooops! the tip to be removed is used as pseudo root. bailing out");
	}

	LN[] nodelist = getAsList(n);

	for (LN node : nodelist) {
	    if (node.data.isTip(taxon)) {
		node = getTowardsTree(node);

		if (safe) {
		    // check if we would remove the pseudo root
		    if (n == node.back || n == node.back.next
			    || n == node.back.next.next) {
			throw new RuntimeException(
				"ooops! other node in the removed branch is used as pseudo root. bailing out");
		    }
		}

		// removing one branch means to join the two adjacent branches.
		// use the sum of the two branch length as approximation for the
		// length of the joined branch.
		double newLen = node.back.next.back.backLen
			+ node.back.next.next.back.backLen;
		node.back.next.back.backLen = newLen;
		node.back.next.next.back.backLen = newLen;

		double newSupport = Math.min(node.back.next.back.backSupport,
			node.back.next.next.back.backSupport);
		// System.out.printf( "remove: %f %f %f\n",
		// node.back.next.back.backSupport,
		// node.back.next.next.back.backSupport, newSupport );
		node.back.next.back.backSupport = newSupport;
		node.back.next.next.back.backSupport = newSupport;

		node.back.next.back.back = node.back.next.next.back;
		node.back.next.next.back.back = node.back.next.back;

		LN[] ret = { node.back.next.back, node.back.next.next.back };

		// scratch links in old node to prevent dead links from orphans.
		// (node and node.back should be unreachable now, but there may
		// still be
		// references to them)
		node.back.next.back = null;
		node.back.next.next.back = null;

		return ret;
	    }
	}

	throw new RuntimeException("could not find tip with name '" + taxon
		+ "'");
    }

    public static LN[] removeNode(LN d1, LN d2) {
	if (d1.back.data.serial != d2.back.data.serial) {
	    throw new RuntimeException(
		    "nodes do not qualify for remove operation: they do not link back to the same node");
	}

	// scratch links in old node to prevent dead links from orphans.
	d1.back.back = null;
	d2.back.back = null;

	// link the two input nodes directly
	double newLen = d1.backLen + d2.backLen;
	d1.back = d2;
	d1.backLen = newLen;
	d2.back = d1;
	d2.backLen = newLen;

	LN[] ret = { d1, d2 };

	return ret;
    }

    public static double shortestPathNd(LN n) {
	if (!n.data.isTip) {
	    throw new RuntimeException("this method is only for tips");
	}

	n = LN.getTowardsTree(n);

	return 1.0 + shortestPathNdRec(n.back);
    }

    private static double shortestPathNdRec(LN n) {
	if (n.data.isTip) {
	    return 0.0;
	} else {
	    double len1 = shortestPathNdRec(n.next.back) + 1.0;
	    double len2 = shortestPathNdRec(n.next.next.back) + 1.0;

	    return Math.min(len1, len2);

	}
    }

    public static double shortestPath(LN n) {
	if (!n.data.isTip) {
	    throw new RuntimeException("this method is only for tips");
	}

	n = LN.getTowardsTree(n);

	return n.backLen + shortestPathRec(n.back);
    }

    public static double shortestPathRec(LN n) {
	if (n.data.isTip) {
	    return 0.0;
	} else {
	    double len1 = shortestPathRec(n.next.back) + n.next.backLen;
	    double len2 = shortestPathRec(n.next.next.back)
		    + n.next.next.backLen;

	    return Math.min(len1, len2);

	}
    }

    public static double longestPath(LN n) {
	if (!n.data.isTip) {
	    throw new RuntimeException("this method is only for tips");
	}

	n = LN.getTowardsTree(n);

	return n.backLen + longestPathRec(n.back);
    }

    public static double longestPathRec(LN n) {
	if (n.data.isTip) {
	    return 0.0;
	} else {
	    double len1 = longestPathRec(n.next.back) + n.next.backLen;
	    double len2 = longestPathRec(n.next.next.back)
		    + n.next.next.backLen;

	    return Math.max(len1, len2);

	}
    }

    public static ArrayList<String> trace = new ArrayList<String>();

    public static double longestPathCorrected(LN n) {
	if (!n.data.isTip) {
	    throw new RuntimeException("this method is only for tips");
	}
	trace.clear();
	trace.add(n.data.getTipName());

	n = LN.getTowardsTree(n);

	// return (n.backLen/2) + longestPathCorrectedRec( n.back );
	return (n.backLen / 2) + longestPathCorrectedRec(n.back);
    }

    public static double longestPathCorrectedRec(LN n) {

	if (n.data.isTip) {
	    trace.add(n.data.getTipName());
	    // ouch! I hope this works
	    // System.out.printf( "xxx: %f\n", -(n.backLen / 2) );
	    return -(n.backLen / 2);
	} else {
	    // hahaha, this 'trace' stuff is hillarious

	    double len1 = longestPathCorrectedRec(n.next.back) + n.next.backLen;
	    String lt1 = trace.remove(trace.size() - 1);
	    double len2 = longestPathCorrectedRec(n.next.next.back)
		    + n.next.next.backLen;
	    String lt2 = trace.remove(trace.size() - 1);
	    if (len1 > len2) {
		trace.add(lt1);
	    } else {
		trace.add(lt2);
	    }
	    return Math.max(len1, len2);

	}
    }

    public static LN deepCloneDirected(LN n, LN prev) {
	assert (n.data == n.next.data && n.data == n.next.next.data);

	ANode data = new ANode(n.data);

	LN nn = new LN(data);
	nn.next = new LN(data);
	nn.next.next = new LN(data);
	nn.next.next.next = nn;

	// if we are at the pseudo root (prev == null), also clone n.back
	// in the other case, let it point to prev, which is the previously
	// cloned portion of the tree.
	if (prev == null) {
	    nn.back = (n.back != null) ? deepCloneDirected(n.back, nn) : null;
	} else {
	    nn.back = prev;
	}

	nn.next.back = (n.next.back != null) ? deepCloneDirected(n.next.back,
		nn.next) : null;
	nn.next.next.back = (n.next.next.back != null) ? deepCloneDirected(
		n.next.next.back, nn.next.next) : null;

	nn.backLabel = n.backLabel;
	nn.backLen = n.backLen;
	nn.backSupport = n.backSupport;

	nn.next.backLabel = n.next.backLabel;
	nn.next.backLen = n.next.backLen;
	nn.next.backSupport = n.next.backSupport;

	nn.next.next.backLabel = n.next.next.backLabel;
	nn.next.next.backLen = n.next.next.backLen;
	nn.next.next.backSupport = n.next.next.backSupport;

	return nn;
    }

    public static LN deepClone(LN n) {
	return deepCloneDirected(n, null);
    }

    public static boolean checkLinkSymmetry(LN n) {
	LN[] list = getAsList(n);

	boolean consistent = true;

	for (int i = 0; i < list.length && consistent; i++) {
	    consistent = consistent
		    && (list[i].back == null || (list[i].back.back != null && list[i] == list[i].back.back));
	}

	return consistent;
    }

    static boolean cmpLNList(LN[] l1, LN[] l2) {

	if (l1.length != l2.length) {
	    return false;
	} else {
	    boolean equal = true;

	    for (int i = 0; i < l1.length && equal; i++) {
		equal = equal && l1[i].backLabel.equals(l2[i].backLabel)
			&& l1[i].backLen == l2[i].backLen
			&& l1[i].backSupport == l2[i].backSupport
			&& l1[i].data.contentEquals(l2[i].data);
	    }

	    return equal;
	}
    }

    static boolean cmpLNListObjectIdentity(LN[] l1, LN[] l2) {
	if (l1.length != l2.length) {
	    return false;
	} else {
	    boolean equal = true;

	    for (int i = 0; i < l1.length && equal; i++) {
		equal = equal && l1[i] == l2[i];

	    }

	    return equal;
	}
    }

    public static boolean hasOutgoingBranchLabel(LN node, String name) {
	// acually start at the node next to us (because 'node' is most likely
	// the ingoing node in the
	// current tree traversal, so it is likely that its lable has already
	// been compared)

	node = node.next;

	for (int i = 0; i < 3; i++, node = node.next) {
	    if (node.backLabel != null && node.backLabel.equals(name)) {
		return true;
	    }
	}

	return false;
    }

    public static LN[] findBranchByTip(LN n, String name) {
	LN[] list = LN.getAsList(n);

	for (LN l : list) {
	    if (l.data.isTip(name)) {
		l = LN.getTowardsTree(l);

		LN[] ret = { l, l.back };

		return ret;
	    }
	}

	throw new RuntimeException("tip not found: '" + name + "'");
    }

    public static LN parseTree(File file) {
	TreeParser tp = new TreeParser(file);
	LN tree = tp.parse();

	if (tree != null) {
	    return tree;
	} else {
	    throw new RuntimeException("tp.parse returned null");
	}
    }

    public static LN[] findCorrespondingBranch(LN[] ref, LN other) {
	// LN[] reflist = LN.getAsList(ref[0]);
	// LN[] otherlist = LN.getAsList(other);
	// System.out.printf( "size: %d %d\n", reflist.length, otherlist.length
	// );
	//
	// Set<String> refts = LN.getTipSet(reflist);
	// Set<String> otherts = LN.getTipSet(otherlist);
	//
	// System.out.printf( "%s %s\n", refts.containsAll(otherts),
	// otherts.containsAll(refts));
	// otherts.removeAll(refts);
	// for( String s : otherts ) {
	// System.out.printf( "%s\n", s );
	// }

	LN.countNodes(ref[0]);
	LN.countNodes(other);

	// System.out.printf( "counts: %d %d\n", nn1, nn2 );

	LN[] ll = LN.getAsList(ref[0], false);
	LN[] lr = LN.getAsList(ref[1], false);

	Set<String> sl = LN.getTipSet(ll);
	Set<String> sr = LN.getTipSet(lr);

	String[] ssl = sl.toArray(new String[sl.size()]);
	String[] ssr = sr.toArray(new String[sr.size()]);

	LN[] bl = LN.findBranchBySplit(other, ssl);
	LN[] br = LN.findBranchBySplit(other, ssr);

	// System.out.printf( "(%d %d) (%d %d)\n", bl[0].data.serial,
	// bl[1].data.serial, br[0].data.serial, br[1].data.serial );

	// check if both split-sets return the same branch of the other tree
	if (bl[1].data.serial != br[0].data.serial
		|| br[1].data.serial != bl[0].data.serial) {
	    throw new RuntimeException(
		    "reftree and other tree do not seem to have the same topology");
	}

	LN[] ret = { bl[0], br[0] };
	return ret;
    }

    static class PathLen {
	PathLen(double bd, int nd) {
	    this.bd = bd;
	    this.nd = nd;
	}

	double bd; // branch dist: sum of branch lengths
	int nd; // node dist: (in case of branch-branch distances it is
		// interpreted as number of nodes separating the two branches)
    }

    public static LN[] findBranchByName(LN n, String branch) {
	LN[] list = getAsList(n);

	for (LN node : list) {
	    if (node.backLabel.equals(branch)) {
		assert (node.back.backLabel.equals(branch));
		LN[] ret = { node, node.back };

		return ret;
	    }
	}

	throw new RuntimeException("could not find named branch '" + branch
		+ "'");
    }

    public static boolean belongsToBranch(LN n, LN[] b) {
	return (n.data.serial == b[0].data.serial)
		|| (n.data.serial == b[1].data.serial);
    }

    public static boolean branchEquals(LN[] b1, LN[] b2) {
	return (b1[0].data.serial == b2[0].data.serial && b1[1].data.serial == b2[1].data.serial)
		|| (b1[0].data.serial == b2[1].data.serial && b1[1].data.serial == b2[0].data.serial);
    }

    private static PathLen getPathLenToBranch(LN n, LN[] b) {
	if (n == null) {
	    return null;
	}

	// System.out.printf( "bbd: %d (%d %d)\n", n.data.serial,
	// b[0].data.serial, b[1].data.serial );

	if (belongsToBranch(n, b)) {
	    return new PathLen(0.0, 0);
	} else {
	    {
		PathLen len = getPathLenToBranch(n.next.back, b);
		if (len != null) {
		    len.nd++;
		    len.bd += n.next.backLen;
		    return len;
		}
	    }
	    {
		PathLen len = getPathLenToBranch(n.next.next.back, b);

		if (len != null) {
		    len.nd++;
		    len.bd += n.next.next.backLen;

		    return len;
		}
	    }

	    return null;

	}
    }

    static PathLen getPathLenBranchToBranch(LN n[], LN[] b, double seScale) {
	assert (n[0].backLen == n[1].backLen);
	assert (b[0].backLen == b[1].backLen);

	if (branchEquals(n, b)) {
	    return new PathLen(0.0, 0);
	}

	PathLen len1 = getPathLenToBranch(n[0], b);
	PathLen len2 = getPathLenToBranch(n[1], b);

	PathLen len;

	if (len1 != null && len2 != null) {
	    throw new RuntimeException(
		    "tree anomaly: two branches connected with more than one path !?");
	}
	if (len1 != null) {
	    len = len1;
	} else if (len2 != null) {
	    len = len2;
	} else {
	    return null;
	}

	len.nd += 1;
	len.bd += (n[0].backLen + b[0].backLen) * seScale;
	//
	return len;
    }

    private static void shortestLongestPathToTip(LN n, double[] l) {
	if (n.data.isTip) {
	    l[0] = l[1] = -(n.backLen / 2.0);
	} else {
	    double[] l1 = new double[2];
	    double[] l2 = new double[2];
	    shortestLongestPathToTip(n.next.back, l1);
	    shortestLongestPathToTip(n.next.next.back, l2);

	    l1[0] += n.backLen;
	    l1[1] += n.backLen;
	    l2[0] += n.backLen;
	    l2[1] += n.backLen;

	    l[0] = Math.min(l1[0], l2[0]);
	    l[1] = Math.max(l1[1], l2[1]);

	}
    }

    static double[] getShortestLongestPathBranchToTip(LN b[]) {
	double[] l1 = new double[2];
	double[] l2 = new double[2];

	shortestLongestPathToTip(b[0], l1);
	shortestLongestPathToTip(b[1], l2);
	l1[0] += b[0].backLen / 2.0;
	l1[1] += b[0].backLen / 2.0;

	l2[0] += b[1].backLen / 2.0;
	l2[1] += b[1].backLen / 2.0;

	// double l1 = longestPathToTip( b[0] ) + (b[0].backLen/2.0);
	// double l2 = longestPathToTip( b[1] ) + (b[1].backLen/2.0);

	double l[] = new double[2];

	l[0] = Math.min(l1[0], l2[0]);
	l[1] = Math.max(l1[1], l2[1]);

	return l;
    }

    private static double longestPathToTip(LN n) {
	if (n.data.isTip) {
	    return -(n.backLen / 2.0);
	} else {
	    return Math.max(n.backLen + longestPathToTip(n.next.back),
		    n.backLen + longestPathToTip(n.next.next.back));
	}

    }

    static double getLongestPathBranchToTip(LN b[]) {
	double l1 = longestPathToTip(b[0]) + (b[0].backLen / 2.0);
	double l2 = longestPathToTip(b[1]) + (b[1].backLen / 2.0);

	return Math.max(l1, l2);
    }

    private static double longestPathToTipND(LN n) {
	if (n.data.isTip) {
	    return 0;
	} else {
	    return Math.max(1 + longestPathToTipND(n.next.back),
		    1 + longestPathToTipND(n.next.next.back));
	}

    }

    static double getLongestPathBranchToTipND(LN b[]) {
	double l1 = longestPathToTipND(b[0]);
	double l2 = longestPathToTipND(b[1]);

	return Math.max(l1, l2);
    }

    static double shortestPathToTipND(LN n) {
	if (n.data.isTip) {
	    return 0;
	} else {
	    return Math.min(1 + shortestPathToTipND(n.next.back),
		    1 + shortestPathToTipND(n.next.next.back));
	}

    }

    static double getShortestPathBranchToTipND(LN b[]) {
	return Math.min(shortestPathToTipND(b[0]), shortestPathToTipND(b[1]));
    }

    // static double getShortestPathBranchToTip( LN b[] ) {
    // double l1 = longestPathToTip( b[0] ) + (b[0].backLen/2.0);
    // double l2 = longestPathToTip( b[1] ) + (b[1].backLen/2.0);
    //
    // return Math.max( l1, l2 );
    // }

    public static LN insertBranch(LN[] br, double len) {
	LN nl = LN.create();

	double bl = br[0].backLen / 2;

	br[0].back = nl.next;
	nl.next.back = br[0];
	br[0].backLen = nl.next.backLen = bl;

	br[1].back = nl.next.next;
	nl.next.next.back = br[1];
	br[1].backLen = nl.next.next.backLen = bl;

	LN nt = LN.create();

	nt.back = nl;
	nl.back = nt;

	nt.backLen = nl.backLen = len;

	return nt;
    }

    static LN getNonTipNode(LN n) {
	return LN.getNonTipNode(n, true);
    }

    static LN getNonTipNode(LN n, boolean back) {
	if (n == null) {
	    return null;
	} else if (!n.data.isTip && !n.back.data.isTip) {
	    return n;
	} else {
	    LN good = null;

	    if (back) {
		good = getNonTipNode(n.back, false);

		if (good != null) {
		    return good;
		}
	    }

	    good = getNonTipNode(n.next.back, false);
	    if (good != null) {
		return good;
	    }
	    good = getNonTipNode(n.next.next.back, false);

	    return good;

	}

    }

    static LN moveBranch(LN n, LN[] dest) {
	if (n.data.isTip) {
	    throw new RuntimeException("cannot move tip");
	}

	{
	    // remove n:
	    // re-link the two neighbor nodes

	    LN n1 = n.next.back;
	    LN n2 = n.next.next.back;
	    n1.back = n2;
	    n2.back = n1;

	    double newLen = n1.backLen + n2.backLen;
	    n1.backLen = newLen;
	    n2.backLen = newLen;

	}

	assert (dest[0].back == dest[1] && dest[1].back == dest[0]);

	{
	    // add n:
	    // link with new neighbors

	    LN n1 = dest[0];
	    LN n2 = dest[1];

	    double newLen = n1.backLen / 2;

	    n.next.back = n1;
	    n.next.next.back = n2;

	    n1.back = n.next;
	    n2.back = n.next.next;

	    n.next.backLen = n.next.next.backLen = n1.backLen = n2.backLen = newLen;

	}

	return n;
    }

    static LN[] removeBranch(LN n) {
	if (n.data.isTip) {
	    throw new RuntimeException("cannot remove tip");
	}
	LN n1 = n.next.back;
	LN n2 = n.next.next.back;
	n1.back = n2;
	n2.back = n1;

	double newLen = n1.backLen + n2.backLen;
	n1.backLen = newLen;
	n2.backLen = newLen;

	n.next.back = n.next.next.back = null;

	LN[] newbranch = { n1, n2 };
	return newbranch;
    }

    static void insertBranch(LN n, LN[] dest) {

	LN n1 = dest[0];
	LN n2 = dest[1];

	if (n1.back != n2 || n2.back != n1) {
	    throw new RuntimeException("bad destination branch");
	}

	double newLen = n1.backLen / 2;

	n.next.back = n1;
	n.next.next.back = n2;

	n1.back = n.next;
	n2.back = n.next.next;

	n.next.backLen = n.next.next.backLen = n1.backLen = n2.backLen = newLen;

    }
}
