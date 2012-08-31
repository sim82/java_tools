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

	
	static class Mutation {
		//public char src;
		Mutation( char t1, char t2, char t3, char t4, int site ) {
			char[] xxx = {t1, t2, t3, t4};
			dst = xxx;
			this.site = site;
		}
		
		public char[] dst; // the four destination states for mutation starting from A, C, G, T
		
		public int site;

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
	
	static class RootedBifurcation {
		
		public RootedBifurcation left = null;
		public RootedBifurcation right = null;
		public double leftLength = -1;
		public double rightLength = -1;
		
		
	}
	
	
	static RootedBifurcation toRooted( LN n ) {
		if( n.data.isTip ) {
			return null;		
		} else {
			RootedBifurcation me = new RootedBifurcation();
			me.left = toRooted(n.next.back);
			me.leftLength = n.next.backLen;
			
			me.right = toRooted(n.next.next.back);
			me.rightLength = n.next.next.backLen;
			
			return me;
		}
	}
	
	static Random r = new Random();
	static final double expLambda = 1.0;
	static double randExp2() {
		return -1.0 / (expLambda * Math.log(1.0 - r.nextDouble()));
	
	}
	
	static double randExp() {
		return -Math.log(r.nextDouble()) / expLambda;
	
	}
	
	public static void main( String[] args ) {
		File treeFile = new File( args[0] );
		
		TreeParser tp = new TreeParser(treeFile);
		tp.setKeepRooted(true);
		LN tree = tp.parse();
		
		RootedBifurcation rb = toRooted( tree );
		
		final int numSites = 10;
		
		
		for( int i = 0; i < 10; ++i ) {
			System.out.printf( "%f\n", randExp() );
		
		}
//		Deque<LN> rbStack = new ArrayDeque<LN>();
//		rbStack.addLast(tree.next);
//		rbStack.addLast(tree.next.next);
//		
		ArrayList<LN> linearEdges = new ArrayList<LN>();

		linearize(tree.next, linearEdges);
		linearize(tree.next.next, linearEdges);
		
		
//		linearize( rbStack, linearEdges );
		double treeLength = 0;
		for( LN n : linearEdges ) {
			System.out.printf( "%s %f\n", n.backLabel, n.backLen );
			treeLength += n.backLen;
			
		}
		
		final int numMutations = 10;
		Mutation[] mutations = new Mutation[numMutations];
		randomMutations( mutations, numSites );
		
		double[] expw = exponentialWalk(numMutations);
		
		// non-generic programming is soooo stupid.
		double ewLength = sum( expw );
		
		// overhang: artificially elongate the random walk, so the last mutation does not exactly hit the end
		// of the last edge, but slightly before it (otherwise it could be put past the end of the last edge 
		// because of rounding errors, which would crash the code below)
		final double overhang = 1.01;
		
		double scale = treeLength / (ewLength * overhang);
		double ewsum = 0;
		for( int i = 0; i < expw.length; ++i ) {
			expw[i] *= scale;
			
			System.out.printf( "ew: %f\n", expw[i] );
			ewsum += expw[i];
		}
		System.out.printf( "sum: %f\n", ewsum );
		
		// drop mutations onto linearized edges
		
		int edgeIdx = 0;
		double inEdge = 0;
		
		
		
		@SuppressWarnings("unchecked")
		ArrayList<Mutation>[] mutationsByEdge = (ArrayList<Mutation>[]) new ArrayList[linearEdges.size()];
		
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
				System.out.printf( "nex edge: %d %f\n", edgeIdx, edgeLen );
				edgeToMutations.put(linearEdges.get(edgeIdx), new ArrayList<Mutation>());
						
			}
			
			inEdge += remaining;
			
			System.out.printf( "mutation %d put on edge: %f %s\n", i, inEdge, linearEdges.get(edgeIdx).backLabel );
			
			edgeToMutations.get(edgeIdx).add(mutations[i]);
		}
		
		
		String startSeq = "";
		for( int i = 0; i < numSites; ++i ) {
			startSeq += randomState();
			
		}
		
		simulate( tree, edgeToMutations, startSeq );
		
	}
	private static void simulate(LN n, Map<LN, ArrayList<Mutation>> edgeToMutations, String startSeq) {
		assert( !n.data.isTip );
		
		String leftSeq = runMutations( startSeq, edgeToMutations.get(n.next));
		
	}

	
	
	private static String runMutations(String startSeq,	ArrayList<Mutation> mutations) {
		char[] curSeq = startSeq.toCharArray();
		for( Mutation m : mutations ) {
			curSeq[m.site] = m.apply( curSeq[m.site]);
		}
		return new String(curSeq);
	}

	static Random rnd = new Random();
	private static char randomState() {
		char[] s = {'A','C','G','T'};
		return s[rnd.nextInt(s.length)];
	}
	private static void randomMutations(Mutation[] mutations, int numSites) {
		for( int i = 0; i < mutations.length; ++i ) {
			mutations[i] = new Mutation(randomState(), randomState(), randomState(), randomState(), rnd.nextInt(numSites));
		}
	}

	static double sum( double[] v ) {
		double sum = 0;
		for( double d : v ) {
			sum += d;
		}
		
		return sum;
			
	}
	
	static double[] exponentialWalk(int numMutations) {
		
		double[] ew = new double[numMutations];
		for( int i = 0; i < numMutations; ++i ) {
			ew[i] = randExp();
		}
		
		return ew;
	}

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
 