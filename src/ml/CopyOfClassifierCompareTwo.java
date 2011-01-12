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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import ml.ClassifierOutput.Res;

public class CopyOfClassifierCompareTwo {
	public static void main(String[] args) {
		
		
		if( args.length >= 5 ) {
			File reftreeFile = new File(args[0] );
			
			File class1File = new File( args[1] );
			File olt1File = new File( args[2] );
			
			File class2File = new File( args[3] );
			File olt2File = new File( args[4] );
			
			boolean reftreeRemove = args.length >= 6 && args[5].equals("--remove" );  
					
			
	
			ClassifierOutput class1Out = ClassifierOutput.read(class1File);
			ClassifierOutput class2Out = ClassifierOutput.read(class2File);
			
//			ArrayList<Res> res1list = class1Out.reslist;
//			ArrayList<Res> res2list = class2Out.reslist;
			
			Res[] res1list = class1Out.reslist.toArray(new Res[class1Out.reslist.size()]);
			Res[] res2list = class2Out.reslist.toArray(new Res[class2Out.reslist.size()]);
			
			Comparator<Res> resCmp = new Comparator<Res>() {
				@Override
				public int compare(Res o1, Res o2) {
					return o1.seq.compareTo(o2.seq);
				}
			};
			Arrays.sort(res1list, resCmp);
			Arrays.sort(res2list, resCmp);
			
			LN olt1 = TreeParser.parse(olt1File);
			LN olt2 = TreeParser.parse(olt2File);
			LN reftreeOrig = TreeParser.parse(reftreeFile);
					
			double diameter = ClassifierLTree.treeDiameter(reftreeOrig);
			
			if( res1list.length != res2list.length ) {
				throw new RuntimeException( "result lists have different size" );
			}
			
			double ndMean = 0.0;
			double bdMean = 0.0;
			double bdnMean = 0.0;
			double pdMean = 0.0;
			double ndnMean = 0.0;
			
			HashMap <UnorderedPair<UnorderedPair<Integer, Integer>, UnorderedPair<Integer, Integer>>, LN.PathLen> lpCache = new HashMap<UnorderedPair<UnorderedPair<Integer, Integer>, UnorderedPair<Integer, Integer>>, LN.PathLen>();
			HashMap <UnorderedPair<Integer, Integer>, Double> lpttCache = new HashMap<UnorderedPair<Integer, Integer>, Double>();
			HashMap <UnorderedPair<Integer, Integer>, double[]> slpttCache = new HashMap<UnorderedPair<Integer, Integer>, double[]>();
			Map <UnorderedPair<Integer, Integer>, LN[]> cbrCache = new HashMap<UnorderedPair<Integer, Integer>, LN[]>();
			HashMap <UnorderedPair<Integer, Integer>, Integer> lpttCacheND = new HashMap<UnorderedPair<Integer, Integer>, Integer>();
			
			
			HashMap <String,LN[]> olt1Name2Br = new HashMap<String, LN[]>();
			HashMap <String,LN[]> olt2Name2Br = new HashMap<String, LN[]>();
			
			for( int i = 0; i < res1list.length; i++ ) {
				Res res1 = res1list[i];
				Res res2 = res2list[i];
				
				if( !res1.seq.equals(res2.seq)) {
					throw new RuntimeException( "different sequences in classifier outputs" );
				}
				
				LN reftree;
				if( reftreeRemove ) {
					reftree = LN.removeTaxon(LN.deepClone(reftreeOrig), res1.seq)[0];
				} else {
					reftree = reftreeOrig;	
				}
				
				
				
				LN[] ibr1 = olt1Name2Br.get(res1.branch);
				if( ibr1 == null ) {
					ibr1 = LN.findBranchByName(olt1, res1.branch);
					olt1Name2Br.put( res1.branch, ibr1 );
				}
				
				
				LN[] ibr2 = olt2Name2Br.get( res2.branch );
				
				if( ibr2 == null ) {
					ibr2 = LN.findBranchByName(olt2, res2.branch);
					olt2Name2Br.put( res2.branch, ibr2 );
				}
				
				UnorderedPair<Integer, Integer> ibr1key = new UnorderedPair<Integer, Integer>(ibr1[0].data.serial, ibr1[1].data.serial); 
				UnorderedPair<Integer, Integer> ibr2key = new UnorderedPair<Integer, Integer>(ibr2[0].data.serial, ibr2[1].data.serial);
				
				
				
				LN[] ibr1ref = cbrCache.get( ibr1key );
				
				if( ibr1ref == null ) {
					ibr1ref = LN.findCorrespondingBranch(ibr1, reftree);
					cbrCache.put( ibr1key, ibr1ref );
				}
				
				
				LN[] ibr2ref = cbrCache.get( ibr2key );
				
				if( ibr2ref == null ) {
					ibr2ref = LN.findCorrespondingBranch(ibr2, reftree);
					cbrCache.put( ibr2key, ibr2ref);
				}
				
		//		int[] ndout = {-1};
		//		double bd = ClassifierLTree.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5, ndout);
				
				UnorderedPair<UnorderedPair<Integer, Integer>, UnorderedPair<Integer, Integer>> key = new UnorderedPair<UnorderedPair<Integer,Integer>, UnorderedPair<Integer,Integer>>(new UnorderedPair<Integer, Integer>(ibr1ref[0].data.serial,ibr1ref[1].data.serial), new UnorderedPair<Integer, Integer>(ibr2ref[0].data.serial,ibr2ref[1].data.serial)); 
				
				LN.PathLen pathlen = lpCache.get(key); 
				
				if( pathlen == null ) {
					pathlen = LN.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5 );
					lpCache.put(key, pathlen);
				} else {
					
					//System.out.printf( "<<<<<<<<< HIT\n" );
				}
				
				// 'longest path to tip (lptt)': the maximum possible branch dist returned by LN.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5 ).
				// The branch-dist normalized by the lptt was suggested by pavlos.
				// assume that the first placement (ibr1/ibr1ref) is the 'correct' one. Calculate the 'longest path to tip (lptt)' for this placement 
				UnorderedPair<Integer, Integer> brKey = new UnorderedPair<Integer, Integer>(ibr1ref[0].data.serial, ibr1ref[1].data.serial);
				final double lptt;
				final int lpttND; //= (int)LN.getLongestPathBranchToTipND(ibr1ref);
				if( lpttCache.containsKey(brKey)) {
					lptt = lpttCache.get(brKey);
					lpttND = lpttCacheND.get(brKey);
//					System.out.printf( ">>>>>>>>>>>>> HIT2\n");
				} else {
					lptt = LN.getLongestPathBranchToTip(ibr1ref);
					lpttND = (int)LN.getLongestPathBranchToTipND(ibr1ref);
					
					lpttCache.put(brKey, lptt );
					lpttCacheND.put(brKey, lpttND );
				}
				
				final double[] slptt;
				if( slpttCache.containsKey(brKey)) {
					slptt = slpttCache.get(brKey);
				} else {
					slptt = LN.getShortestLongestPathBranchToTip(ibr1ref);
					slpttCache.put( brKey, slptt );
			
				}
				
				
				
				//System.out.printf( "dist: %f %d (%f %d)\n", pathlen.bd, pathlen.nd, bd, ndout[0] );
	//			System.out.printf( "branches: %s %s\n", res1.branch, res2.branch );
				System.out.printf( "%s %s %s %f %d %f %f %f %f %f %d\n", res1.seq, res1.branch, res2.branch, pathlen.bd, pathlen.nd, pathlen.bd / lptt, lptt, slptt[0], slptt[1], pathlen.nd / (float)lpttND, lpttND );
				
				ndMean += pathlen.nd;
				bdMean += pathlen.bd;
				bdnMean += pathlen.bd / diameter;
				pdMean += pathlen.bd / lptt;
				ndnMean += pathlen.nd / ((double)lpttND);
			}
			
			if( res1list.length > 1 ) {
				double n = res1list.length; 
				System.out.printf( "mean: bd: %f nd: %f bdn: %f pd: %f ndn: %f\n", bdMean / n, ndMean / n, bdnMean / n, pdMean / n, ndnMean / n ); 
			}
		} else if( args.length == 4 ) {
			File reftreeFile = new File(args[0] );
			
			File class1File = new File( args[1] );
			File olt1File = new File( args[2] );
			
			File nn2File = new File( args[3] );
			  
					
			
			ClassifierOutput class1Out = ClassifierOutput.read(class1File);
			
			
			Map<String,String> nnMap = read2ColMap( nn2File );
			
			ArrayList<Res> res1list = class1Out.reslist;
			
			
			LN olt1 = TreeParser.parse(olt1File);
			
			LN reftreeOrig = TreeParser.parse(reftreeFile);
					
			
			
			
			double ndMean = 0.0;
			double bdMean = 0.0;
			
			for( int i = 0; i < res1list.size(); i++ ) {
				Res res1 = res1list.get(i);
				
				String nn = nnMap.get( res1.seq );
				
				if( nn == null ) {
					throw new RuntimeException( "different sequences in classifier outputs" );
				}
				
				LN reftree;
				
				reftree = reftreeOrig;	
				
				
				LN[] ibr1 = LN.findBranchByName(olt1, res1.branch);
				LN[] ibr2 = LN.findBranchByTip(reftree, nn);
				
				LN[] ibr1ref = LN.findCorrespondingBranch(ibr1, reftree);
				LN[] ibr2ref = LN.findCorrespondingBranch(ibr2, reftree);
				
		//		int[] ndout = {-1};
		//		double bd = ClassifierLTree.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5, ndout);
				
				LN.PathLen pathlen = LN.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5 );
				
				
				//System.out.printf( "dist: %f %d (%f %d)\n", pathlen.bd, pathlen.nd, bd, ndout[0] );
	//			System.out.printf( "branches: %s %s\n", res1.branch, res2.branch );
				System.out.printf( "%s %s %s %f %d\n", res1.seq, res1.branch, nn, pathlen.bd, pathlen.nd );
				
				ndMean += pathlen.nd;
				bdMean += pathlen.bd;
			}
			
			if( res1list.size() > 1 ) {
				double n = res1list.size(); 
				System.out.printf( "mean: %f %f\n", bdMean / n, ndMean / n ); 
			}
		}
	}

	private static Map<String,String> read2ColMap(File nn2File) {
		
		
		HashMap<String,String>map = new HashMap<String, String>();
		
		
		try {
			BufferedReader r = new BufferedReader( new FileReader(nn2File) );
			
			String line;
			
			while( (line = r.readLine()) != null ) {
				StringTokenizer ts = new StringTokenizer(line);
				
				if( ts.countTokens() < 2 ) {
					continue;
				}
				
				String key = ts.nextToken();
				String value = ts.nextToken();
				
				map.put(key, value);
			}
		} catch( IOException x ) {
			x.printStackTrace();
			throw new RuntimeException( "bailing out");
		}
		
		return map;
	}
}
