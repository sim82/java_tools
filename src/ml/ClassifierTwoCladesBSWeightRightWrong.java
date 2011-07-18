package ml;


public class ClassifierTwoCladesBSWeightRightWrong {
//	static class Res {
//		String name;
//		double wghr;
//		double wghw;
//		double wghog;
//		
//		Res( String name, double wgh1, double wgh2, double wghog ) {
//			this.name = name;
//			this.wghr = wghr;
//			this.wghw = wghw;
//			this.wghog = wghog;
//		}
//	}
//	
//	public static void main(String[] args) {
//		File treeFile = new File( args[0] );
//		File classFile = new File( args[1] );
//			
//		String outgroup = args[2];
//		File rnFile = new File( args[3] );
//		
//		Map<String, String[]> splitMap = ClassifierLTree.parseSplits(rnFile);
//		
//		LN n = TreeParser.parse(treeFile);
//		
//		LN[] sp = LN.findBranchByTip(n, outgroup);
//		
//		LN t1 = sp[0].back.next;
//		LN t2 = sp[0].back.next.next;
//		
//		Set<String> nl1 = getBranchLabelSet( LN.getAsList(t1.back, false) );
//		Set<String> nl2 = getBranchLabelSet( LN.getAsList(t2.back, false) );
//		
//		Set<String> tl1 = LN.getTipSet( LN.getAsList(t1.back, false) );
//		Set<String> tl2 = LN.getTipSet( LN.getAsList(t2.back, false) );
//		
//		
//		
//		if( false ) {
//			String tl1s = "";
//			String tl2s = "";
//			for( String s : tl1 ) {
//				tl1s += " " + s;
//			}
//			for( String s : tl2 ) {
//				tl2s += " " + s;
//			}
//			System.out.printf( "set1: %s\n", tl1s );
//			System.out.printf( "set2: %s\n", tl2s );
//		}
////		System.out.println( "b1:");
////		print( nl1 );
////		
//		//LN.getBranchList(n)
//		
//		String ogbrname = sp[0].backLabel;
//		
//		ClassifierOutput cf = ClassifierOutput.read(classFile);
//		
//		ArrayList<String> os1 = new ArrayList<String>();
//		ArrayList<String> os2 = new ArrayList<String>();
//		ArrayList<String> osog = new ArrayList<String>();
//		
//		Map<String,Res> orm = new TreeMap<String, Res>();
//		
//		for( ClassifierOutput.Res res : cf.reslist ) {
//
//			String rb = LN.findBranchBySplit(n, splitMap.get(res.seq))[0].backLabel;
//			
//			
//			
////			String seq = res.seq + "_" + res.support;
//			String seq = res.seq;
//			Res or = orm.get(res.seq);
//			
//			if( or == null ) {
//				or = new Res( res.seq, 0, 0, 0 );
//				orm.put( res.seq, or );
//			}
//			
//			
//			if( (nl1.contains(res.branch) && nl1.contains(rb)) || (nl2.contains(res.branch) && nl2.contains(rb))) {
//				or.wghr += res.support;
//			} else if((nl1.contains(res.branch) && nl2.contains(rb)) || (nl2.contains(res.branch) && nl1.contains(rb))){
//				or.wghw += res.support;
//			} else {
//				or.wghog += res.support;
//			}
//		}
//		
//		
////		for( Res res : orm.values() ) {
////			System.out.printf( "%s\t%f\t%f\t%f\n", res.name, res.wgh1, res.wgh2, res.wghog );
////		}
//		
//		
//		ml.ClassifierTwoCladesBSWeightRightWrong.Res[] orl = orm.values().toArray(new Res[orm.size()]);
//		Arrays.sort(orl, new Comparator<Res>() {
//
//			@Override
//			public int compare(ml.ClassifierTwoCladesBSWeightRightWrong.Res o1,
//					ml.ClassifierTwoCladesBSWeightRightWrong.Res o2) {
//				// TODO Auto-generated method stub
//				
//				int w1 = (int)o1.wghr - (int)o1.wghw - (int)o1.wghog * 10;
//				int w2 = (int)o2.wghr - (int)o2.wghw - (int)o2.wghog * 10;
//				
//				if( w1 < w2 ) {
//					return 1;
//				} else if( w1 > w2 ){
//					return 0;
//				} else {
//					return o1.name.compareTo(o2.name);
//				}
//				
//			}
//		});
//		
//		for( Res res : orl ) {
//			System.out.printf( "%s\t%f\t%f\t%f\n", res.name, res.wghr, res.wghw, res.wghog );
//		}
//	}
//	
//	private static void print(Iterable<String> nl1) {
//		for( String s : nl1 ) {
//			System.out.println( s );
//		}
//		
//	}
//
//	static Set<String> getBranchLabelSet( LN[] bl ) {
//		Set<String> s = new HashSet<String>();
//		
//		for( LN n : bl ) {
//			s.add( n.backLabel );
//		}
//		
//		return s;
//	}
}
