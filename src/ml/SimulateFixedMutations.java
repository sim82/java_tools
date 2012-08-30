package ml;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;



public class SimulateFixedMutations {

	
	static class Mutation {
		public char src;
		public char dst;
		
		public int site;
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
			System.out.printf( "%s\n", n.backLabel );
			treeLength += n.backLen;
			
		}
		
		final int numMutations = 10;
		double[] ew = exponentialWalk(numMutations);
		
		// non-generic programming is soooo stupid.
		double ewLength = sum( ew );
		double scale = treeLength / ewLength;
		for( int i = 0; i < ew.length; ++i ) {
			ew[i] *= scale;
		}
		
		// drop mutations onto linearized edges
		
		int edgeIdx = 0;
		double inEdge = 0;
		for( double e : ew ) {
			
			double remaining = e;
			
			while( remaining > linearEdges.get(edgeIdx).backLen - inEdge ) {
				remaining -= linearEdges.get(edgeIdx).backLen - inEdge;
				
				edgeIdx++;
				inEdge = 0;
			}
			
			inEdge += remaining;
			
			System.out.printf( "mutation %f put on edge: %s\n", remaining, linearEdges.get(edgeIdx).backLabel );
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
 