package ml;
import java.awt.LinearGradientPaint;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.management.RuntimeErrorException;



public class SimulateFixedMutations {

	/**
	 * Mutation class. It describes the target state (dependent on the start state) and the site of the mutation.
	 * @author sim
	 *
	 */
	static class Mutation {
		//public char src;
		Mutation( char t1, char t2, char t3, char t4, int site ) {
			char[] xxx = {t1, t2, t3, t4};
			dst = xxx;
			this.site = site;
		}
		
		public char[] dst; // the four destination states for mutation starting from A, C, G, T
		
		public int site;

		/**
		 * Apply this mutation to state c
		 * @param c the starting state
		 * @return target state
		 */
		public char apply(char c) {
			final int idx;
			
			switch( c ) {
				case 'A': idx = 0; break;
				case 'C': idx = 1; break;
				case 'G': idx = 2; break;
				case 'T': idx = 3; break;
				default: throw new RuntimeException( "bad starting state in Mutation.apply: " + c );
			}
			
			return dst[idx];
			
		}
	}
	
//	static class RootedBifurcation {
//		
//		public RootedBifurcation left = null;
//		public RootedBifurcation right = null;
//		public double leftLength = -1;
//		public double rightLength = -1;
//		
//		
//	}
//	
//	
//	static RootedBifurcation toRooted( LN n ) {
//		if( n.data.isTip ) {
//			return null;		
//		} else {
//			RootedBifurcation me = new RootedBifurcation();
//			me.left = toRooted(n.next.back);
//			me.leftLength = n.next.backLen;
//			
//			me.right = toRooted(n.next.next.back);
//			me.rightLength = n.next.next.backLen;
//			
//			return me;
//		}
//	}
	
	static Random r = new Random();
	static final double expLambda = 1.0;
//	static double randExp2() {
//		return -1.0 / (expLambda * Math.log(1.0 - r.nextDouble()));
//	
//	}

	/**
	 * @return Exponentially distributed random value
	 */
	static double randExp() {
		return -Math.log(r.nextDouble()) / expLambda;
	}
	
	public static void main( String[] args ) {
		
		//
		// read (rooted) input tree
		//
		final TreeParser tp;
		if( false ) {
			File treeFile = new File( args[0] );
		
			tp = new TreeParser(treeFile);
			
		} else {
			String tree = "((a:1[E11],b:1[E12]):1[E1],((c:1[E221],d:1[E222]):1[E22],e:1[E21]):1[E2]);";
			
			tp = new TreeParser(tree);
		
		}
		
		tp.setKeepRooted(true);
		LN tree = tp.parse();
		
		
//		RootedBifurcation rb = toRooted( tree );
		
		final int numSites = 10;

		//
		// generate (linear) list of edges
		//
		ArrayList<LN> linearEdges = new ArrayList<LN>();

		linearize(tree.next, linearEdges);
		linearize(tree.next.next, linearEdges);
		
		
		//
		// calculate tree length
		// 
		double treeLength = 0;
		for( LN n : linearEdges ) {
			System.out.printf( "%s %f\n", n.backLabel, n.backLen );
			treeLength += n.backLen;
			
		}
		
		
		//
		// create random mutations (testing only)
		//
		final int numMutations = 10;
		Mutation[] mutations = new Mutation[numMutations];
		randomMutations( mutations, numSites );
		
		
		//
		// create exponential random walk (random walk may be a stupid name in this setting)
		//
		double[] expw = exponentialWalk(numMutations);
		
		// non-generic programming is soooo stupid.
		double ewLength = sum( expw );
		
		// overhang: artificially elongate the random walk, so the last mutation does not exactly hit the end
		// of the last edge, but slightly before it (otherwise it could be put past the end of the last edge 
		// because of rounding errors, which would crash the code below)
		final double overhang = 1.01;

		
		//
		// scale the mutations to the tree length
		//
		double scale = treeLength / (ewLength * overhang);
		double ewsum = 0;
		for( int i = 0; i < expw.length; ++i ) {
			expw[i] *= scale;
			
//			System.out.printf( "ew: %f\n", expw[i] );
			ewsum += expw[i];
		}
		System.out.printf( "sum: %f\n", ewsum );
		
		// drop mutations onto linearized edges
		
		int edgeIdx = 0;
		double inEdge = 0;
		
		
		
//		@SuppressWarnings("unchecked")
//		ArrayList<Mutation>[] mutationsByEdge = (ArrayList<Mutation>[]) new ArrayList[linearEdges.size()];
		
		
		//
		// assign the Mutation objects to the edges, as layen out in the 'linearized' list
		//
		Map<LN, ArrayList<Mutation>> edgeToMutations = new HashMap<LN,ArrayList<Mutation>>();
		edgeToMutations.put(linearEdges.get(0), new ArrayList<Mutation>());
		for( int i = 0; i < numMutations; ++i ) {
			
			double e = expw[i];
			
			double remaining = e;
			
			double edgeLen = linearEdges.get(edgeIdx).backLen;
			
			// distribute remaining distance onto edges
			while( remaining > edgeLen - inEdge ) {
				remaining -= edgeLen - inEdge;
				
				edgeIdx++;
				inEdge = 0;
				edgeLen = linearEdges.get(edgeIdx).backLen;
				System.out.printf( "next edge: %d %f\n", edgeIdx, edgeLen );
				edgeToMutations.put(linearEdges.get(edgeIdx), new ArrayList<Mutation>());
						
			}
			
			inEdge += remaining;
			
			System.out.printf( "mutation %d put on edge: %f %s\n", i, inEdge, linearEdges.get(edgeIdx).backLabel );
			
			edgeToMutations.get(linearEdges.get(edgeIdx)).add(mutations[i]);
		}
		
		
		//
		// create random staring sequence
		//
		String startSeq = "";
		for( int i = 0; i < numSites; ++i ) {
			startSeq += randomState();
			
		}
		
		//
		// recursively run the simulation
		//
		simulate( tree, edgeToMutations, startSeq );
		
	}
	
	/**
	 * Recursively simulate the tip sequences 
	 * @param n the 'root' node (n.next and n.next.next point toward the two subtrees)
	 * @param edgeToMutations Map from edge -> mutation list (edges are identified by their LN object facing towards the root
	 * @param startSeq starting sequence for simulation
	 */
	private static void simulate(LN n, Map<LN, ArrayList<Mutation>> edgeToMutations, String startSeq) {
		if( n.data.isTip ) {
		    System.out.printf( "%s %s\n", n.data.getTipName(), startSeq );
		} else {
			
			String leftSeq = runMutations( startSeq, edgeToMutations.get(n.next));
			simulate( n.next.back, edgeToMutations, leftSeq );
			
			String rightSeq = runMutations( startSeq, edgeToMutations.get(n.next.next));
			simulate( n.next.next.back, edgeToMutations, rightSeq );
		}
	}

	
	/**
	 * Apply a list of Mutation objects to a given starting sequence.
	 * 
	 * @param startSeq The starting sequence
	 * @param mutations the list of Mutation objects.
	 * @return The mutated sequence
	 */
	private static String runMutations(String startSeq, ArrayList<Mutation> mutations) {
		char[] curSeq = startSeq.toCharArray();
		for( Mutation m : mutations ) {
			curSeq[m.site] = m.apply( curSeq[m.site]);
		}
		return new String(curSeq);
	}
	/**
	 * 
	 * @return Uniformly distributed DNA character (ACGT)
	 */
	private static char randomState() {
		char[] s = {'A','C','G','T'};
		return s[r.nextInt(s.length)];
	}
	/**
	 * Generate a list of random Mutation objects
	 * 
	 * @param mutations Output list.
	 * @param numSites Maximum site number for the generated Mutation objects.
	 */
	private static void randomMutations(Mutation[] mutations, int numSites) {
		for( int i = 0; i < mutations.length; ++i ) {
			mutations[i] = new Mutation(randomState(), randomState(), randomState(), randomState(), r.nextInt(numSites));
		}
	}

	static double sum( double[] v ) {
		double sum = 0;
		for( double d : v ) {
			sum += d;
		}
		
		return sum;
			
	}
	
	/**
	 * Generate sequence of exponentially distributed variables
	 * 
	 * @param numValues Number of random values to generate 
	 * @return the random values
	 */
	static double[] exponentialWalk(int numValues) {
		
		double[] ew = new double[numValues];
		for( int i = 0; i < numValues; ++i ) {
			ew[i] = randExp();
		}
		
		return ew;
	}

	/**
	 * Put tree edges in a 'linear order'
	 * 
	 * @param start Start Node for (rooted) traversal
	 * @param linearEdges Output list of edges (the LN of each edge facing towards the root)
	 *        according to a pre-order DFS
	 */
	static void linearize( LN start, ArrayList<LN> linearEdges ) {
		
		// thid should be pretty much just a pre-order DFS
		linearEdges.add( start );
		
		
		if( !start.back.data.isTip ) {
			linearize( start.back.next, linearEdges);
			linearize( start.back.next.next, linearEdges);
		}
		
	}
	
	
//	static void linearize( Deque<LN> rbStack, ArrayList<LN> linearEdges ) {
//		// FIXME: this can be done without the explicit stack (don't want to think about it now). Maybe we can use it in case we need BDS later.
//		
//		if( rbStack.isEmpty() ) {
//			return;
//		}
//		
//		LN n = rbStack.getLast();
//		rbStack.removeLast();
//		linearEdges.add(n);
//		
//		if( !n.back.data.isTip ) {
//			rbStack.addLast(n.back.next);
//			rbStack.addLast(n.back.next.next);
//			
//		}
//		
//		linearize(rbStack, linearEdges);
//	}
}
 