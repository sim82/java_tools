/*
 * Copyright (C) 2010 Simon A. Berger
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.management.RuntimeErrorException;

import ml.ClassifierTwoCladesBSWeight.Res;

public class EpaBinning {
	
	
	static class Bin {
		Set<String> bns;
		Bin( LN tr ) {
			bns = new HashSet<String>( Arrays.asList(LN.getAllBranchNameList(tr)) );
		}
		
		Bin( LN[] list, String[] split, boolean addConnecting ) {
			bns = findSubtreeByTipSet(list, split, addConnecting);
		}
		public Bin(Set<String> branchLabelSet) {
			this.bns = branchLabelSet;
		}

		void print() {
			for( String b: bns ) {
				System.out.printf( "%s ", b );
			}
			System.out.printf( "\n" );
			
		}
		
		public void remove(Bin b) {
			bns.removeAll(b.bns);
			
		}
		
		private static Set<String> findSubtreeByTipSet( final LN[] list, String[] split, boolean addConnecting ) {
			Set<String> brLabels = new HashSet<String>();
			
			

			Map<String,LN> tipIndex = new HashMap<String,LN>();
			Map<Integer,LN> nodeIndex = new HashMap<Integer,LN>();
			
			// build indices: tipname => LN and node-serialnumber => LN
			for( LN ln : list ) {
				if( ln.data.isTip ) {
					tipIndex.put(ln.data.getTipName(), ln);
				}

				if( !nodeIndex.containsKey(ln.data.serial)) {
					nodeIndex.put(ln.data.serial, ln);
				}
				
			}

			
			
			if( split.length == 1 ) {
				// trivial case, won't work with the algorithm below

				//return findBranchBySplitTrivial( n, split[0] );
				if( !addConnecting ) {
					throw new RuntimeException( "branches with one taxon are only allowd in 'add-connecting' mode.");
				} else {
					brLabels.add(LN.getTowardsTree(tipIndex.get(split[0])).backLabel);
					return brLabels;
				}
			}

						//
			// this algorithm is adapted from LN.findBranchBySplit. Look there for a (rubbish) description.
			// basically it just floods the tree from the tips inward...
			//
			
			Set<Integer>markedNodes = new HashSet<Integer>();
			Set<Integer>openNodes = new HashSet<Integer>(markedNodes);

			for( String name : split ) {
				LN tip = tipIndex.get(name);

				if( tip == null ) {
					throw new RuntimeException("tip '" + name + "' not found in tip index" );
				}

				markedNodes.add( tip.data.serial );

				tip = LN.getTowardsTree(tip);

				openNodes.add( tip.back.data.serial );
//				System.out.printf( "add to open: %s\n", tip.backLabel ); 
			}

			while(true) {

				int lastNode = -1;
				LN lastLN = null;

				// if this is too slow, we could keep track of the mark-count
				// and use a priority queue:
				
				for( int next : openNodes ) {
					LN oln = nodeIndex.get(next);

					int markedCount = 0;
					
					LN[] toMarked = new LN[3];
					for( int i = 0; i < 3; i++, oln = oln.next ) {
						if( markedNodes.contains(oln.back.data.serial)) {
							toMarked [markedCount]= oln;
							markedCount++;
						}
					}


					if( markedCount == 2 ) {
						lastNode = next;
						lastLN = oln;
						
//						// add the branches pointing towards the two marked neighbors to the brLabel set.
						brLabels.add( toMarked[0].backLabel );
						brLabels.add( toMarked[1].backLabel );
						break;
					}
				}
				
				// proof of correctness (tm): ;)
				// termination: the loop will always terminate because we are constantly removing nodes from the open-list (and each node can be added to the open-list only once)
				// if the tip-names in 'split' are inconsistent (=the nodes in split are from two unconnected sub-trees), the following error will be triggered before it terminates
				
				if( lastNode < 0 ) {
					for( String s : split ) {
						System.out.printf( "%s ", s );
					}
					System.out.println();
					throw new RuntimeException("no open node with two marked neighbors found. looks like a bad split" );
				}

				if( openNodes.size() > 1 ) {
					// we still have at least one open node for the next iteration
					// remove current from open list-list add it to marked list
					openNodes.remove(lastNode);
					markedNodes.add(lastNode);
					
					// put all unmarked neighbors on open-list
					for( int i = 0; i < 3; i++, lastLN = lastLN.next ) {
//						brLabels.add( lastLN.backLabel );
//						
//						System.out.printf( "add: %s\n", lastLN.backLabel );
						if( !markedNodes.contains(lastLN.back.data.serial)) {
							
							openNodes.add(lastLN.back.data.serial);
						}
					}
				} else {
					// the last node remaining on the open-list is the root of the 
					// subtree.
					
					if( addConnecting ) {
						// the last node on the open list is the root of the subtree.
						// 
						LN n1 = nodeIndex.get(openNodes.iterator().next());
						for( int i = 0; i < 3; i++, n1 = n1.next ) {
							brLabels.add( n1.backLabel );
						}
					}
					return brLabels;
				}
			}

			
			//throw new RuntimeException( "split finder finished but openNodes.size != 1 !?" );
						


		}

		
		
	}
	
	private static Set<String> getBranchLabelSet( LN[] bl ) {
		Set<String> s = new HashSet<String>();
		
		for( LN n : bl ) {
			s.add( n.backLabel );
		}
		
		return s;
	}
	static ArrayList<String>binNames = new ArrayList<String>();
	static String[][] readBindef( File f ) {
		ArrayList<String[]> rs = new ArrayList<String[]>();
		ArrayList<String>cur = null;
		binNames.clear();
		try {
			BufferedReader br = new BufferedReader( new FileReader(f));
			String line;
			while( (line = br.readLine()) != null ) {
				StringTokenizer st = new StringTokenizer(line);
				while( st.hasMoreTokens() ) {
					
					String s = st.nextToken();
					
					if( s.startsWith("@")) {
						binNames.add( s );
						if( cur != null ) {
							rs.add( cur.toArray(new String[cur.size()]));
						}
						cur = new ArrayList<String>();
					} else {
						if( cur == null ) {
							throw new RuntimeException( "error in bindef: missing bin identifier (@<bin-name>) before the first taxon name" );
						}
						cur.add( s );
					}
					
				}
			}
			rs.add( cur.toArray(new String[cur.size()]));
			
			
			return rs.toArray( new String[rs.size()][]);	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	static class Res {
		String name;
		double[] wgh;
		
		Res( String name, int numBins ) {
			this.name = name;
			wgh = new double[numBins];
		}
	}
	public static void main(String[] args) {
		boolean includeConnecting;
		
		//String[] s1 = {"Graphis_cinerea", "Graphis_angustata", "Graphis_chrysocarpa", "Graphis_pseudocinerea", "Graphis_illinata", "Graphis_vestitoides", "Graphis_rhizocola", "Graphis_ruiziana"};
//		String[] s1 = {"Graphis_cinerea", "Graphis_angustata", "Graphis_chrysocarpa", "Graphis_pseudocinerea", "Graphis_illinata", "Graphis_vestitoides", "Graphis_rhizocola", "Graphis_ruiziana"};
//		String[] s2 = {"Graphis_tenella", "Graphis_caesiella", "Graphis_dupaxana", "Graphis_scripta", "Graphis_japonica", "Graphis_furcata", "Graphis_anfractuosa", "Graphis_librata"};
		
//		String[][] sets = {s1, s2};
	
		
		if( args.length < 2 ) {
			throw new RuntimeException( "missing args:\n<classifier output> <original labelled tree> (-og <out group>|-bd <bin-def file>)" );
		}
		File classFile = new File( args[0] );
		File treeFile = new File( args[1] );
		
		ClassifierOutput cf = ClassifierOutput.read( classFile );
		LN tr = TreeParser.parse(treeFile);
		
		
		final String outgroup;
		final String[][] sets;
		boolean addConnecting = false;
		if( args.length >= 4 && args[2].equals("-og")) {
			outgroup = args[3];
			sets = null;
			
		} else if( args.length >= 4 && (args[2].equals( "-bd" ) || args[2].equals( "-bx" ))) {
			sets = readBindef(new File( args[3]));
			outgroup = null;
			
			addConnecting = args[2].equals("-bd");
			
		} else {
			throw new RuntimeException( "missing bin specification (either -og <out group> or -bd <bin-def file>)");
		}
		 
		final boolean printBins =  args.length >= 5 && (args[4].equals("-pb" ) || args[4].equals("-px" ));
		final boolean dieYoung = printBins && args[4].equals("-px" );
		
		
		Bin[] bins;
		
		
		
		if( outgroup != null ) {
			bins = new Bin[3];	
			
			LN[] sp = LN.findBranchByTip(tr, outgroup);
			
			LN t1 = sp[0].back.next;
			LN t2 = sp[0].back.next.next;
			
			bins[0] = new Bin(getBranchLabelSet( LN.getAsList(t1.back, false) ));
			bins[1] = new Bin(getBranchLabelSet( LN.getAsList(t2.back, false) ));
			
			
			//
			
		} else {
			LN[] list = LN.getAsList(tr);
			bins = new Bin[sets.length + 1];
			
			for( int i = 0; i < sets.length; i++ ) {
			
				bins[i] = new Bin(list, sets[i], addConnecting);
			}
			
		}
		
		
		
		// create the 'outgroup' bin
		Bin bo = bins[bins.length-1] = new Bin(tr);
		for( int i = 0; i < bins.length - 1; i++ ) {
			Bin b = bins[i];
			if( printBins ) {
				System.out.printf( "%s: ", binNames.get(i) );
				b.print();
			}

			bo.remove(b);
		}

		if( printBins ) {
			System.out.printf( "trash: " );
			bo.print();
		}
		
		if( dieYoung ) {
			return;
		}
		
		Map<String,Res> orm = new TreeMap<String, Res>();
	
		
		for( ClassifierOutput.Res res : cf.reslist ) {
//			String seq = res.seq + "_" + res.support;
			String seq = res.seq;
			Res or = orm.get(res.seq);
			
			if( or == null ) {
				or = new Res( res.seq, bins.length );
				orm.put( res.seq, or );
			}
			
			for( int i = 0; i < bins.length; i++ ) {
				Bin b = bins[i];
				
				if( b.bns.contains(res.branch)) {
					or.wgh[i] += res.support;
				}
			}
			
		}
		
		Res[] orl = orm.values().toArray(new Res[orm.size()]);
		Arrays.sort(orl, new Comparator<Res>() {

			@Override
			public int compare(Res o1,Res o2) {
				// TODO Auto-generated method stub
				
				final double w1, w2;
				if( o1.wgh.length == 3 && o2.wgh.length == 3 ) {
					w1 = o1.wgh[0] - o1.wgh[1];
					w2 = o2.wgh[0] - o2.wgh[1];
				} else {
					w1 = o1.wgh[0];
					w2 = o2.wgh[0];
				}
				if( w1 < w2 ) {
					return 1;
				} else if( w1 >= w2 ){
					return -1;
				} else {
					return o1.name.compareTo(o2.name);
				}
				
			}
		});
		
		for( Res res : orl ) {
			System.out.printf( "%s", res.name );
			for( int i = 0; i < bins.length; i++ ) {
				System.out.printf( "\t%f", res.wgh[i] );
			}
			System.out.println();
		}
		
	}
}
