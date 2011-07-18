package ml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClassifierTwoClades {
	public static void main(String[] args) {
		File treeFile = new File( args[0] );
		File classFile = new File( args[1] );
			
		String outgroup = args[2];
		
		
		LN n = TreeParser.parse(treeFile);
		
		LN[] sp = LN.findBranchByTip(n, outgroup);
		
		LN t1 = sp[0].back.next;
		LN t2 = sp[0].back.next.next;
		
		Set<String> nl1 = getBranchLabelSet( LN.getAsList(t1.back, false) );
		Set<String> nl2 = getBranchLabelSet( LN.getAsList(t2.back, false) );
		
//		System.out.println( "b1:");
//		print( nl1 );
//		
		//LN.getBranchList(n)
		
		ClassifierOutput cf = ClassifierOutput.read(classFile);
		
		ArrayList<String> os1 = new ArrayList<String>();
		ArrayList<String> os2 = new ArrayList<String>();
		ArrayList<String> osog = new ArrayList<String>();
		
		
		for( ClassifierOutput.Res res : cf.reslist ) {
//			String seq = res.seq + "_" + res.support;
			String seq = res.seq;
			if( nl1.contains(res.branch)) {
				os1.add(seq);
			} else if( nl2.contains(res.branch)) {
				os2.add(seq);
			} else {
				osog.add(seq);
			}
		}
		
		System.out.println( "set1:" );
		for( String s : os1 ) {
			System.out.println( s );
		}
		System.out.println( "set2:" );
		for( String s : os2 ) {
			System.out.println( s );
		}
		System.out.println( "og:" );
		for( String s : osog ) {
			System.out.println( s );
		}
		
	}
	
	static void print(Iterable<String> nl1) {
		for( String s : nl1 ) {
			System.out.println( s );
		}
		
	}

	static Set<String> getBranchLabelSet( LN[] bl ) {
		Set<String> s = new HashSet<String>();
		
		for( LN n : bl ) {
			s.add( n.backLabel );
		}
		
		return s;
	}
}
