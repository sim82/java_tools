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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ml.ClassifierOutput.Res;

public class ClassifierCompareTwo {
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
			
			ArrayList<Res> res1list = class1Out.reslist;
			ArrayList<Res> res2list = class2Out.reslist;
			
			LN olt1 = TreeParser.parse(olt1File);
			LN olt2 = TreeParser.parse(olt2File);
			LN reftreeOrig = TreeParser.parse(reftreeFile);
					
			
			if( res1list.size() != res2list.size() ) {
				throw new RuntimeException( "result lists have different size" );
			}
			
			double ndMean = 0.0;
			double bdMean = 0.0;
			
			for( int i = 0; i < res1list.size(); i++ ) {
				Res res1 = res1list.get(i);
				Res res2 = res2list.get(i);
				
				if( !res1.seq.equals(res2.seq)) {
					throw new RuntimeException( "different sequences in classifier outputs" );
				}
				
				LN reftree;
				if( reftreeRemove ) {
					reftree = LN.removeTaxon(LN.deepClone(reftreeOrig), res1.seq)[0];
				} else {
					reftree = reftreeOrig;	
				}
				
				LN[] ibr1 = LN.findBranchByName(olt1, res1.branch);
				LN[] ibr2 = LN.findBranchByName(olt2, res2.branch);
				
				LN[] ibr1ref = LN.findCorrespondingBranch(ibr1, reftree);
				LN[] ibr2ref = LN.findCorrespondingBranch(ibr2, reftree);
				
		//		int[] ndout = {-1};
		//		double bd = ClassifierLTree.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5, ndout);
				
				LN.PathLen pathlen = LN.getPathLenBranchToBranch(ibr1ref, ibr2ref, 0.5 );
				
				
				//System.out.printf( "dist: %f %d (%f %d)\n", pathlen.bd, pathlen.nd, bd, ndout[0] );
	//			System.out.printf( "branches: %s %s\n", res1.branch, res2.branch );
				System.out.printf( "%s %s %s %f %d\n", res1.seq, res1.branch, res2.branch, pathlen.bd, pathlen.nd );
				
				ndMean += pathlen.nd;
				bdMean += pathlen.bd;
			}
			
			if( res1list.size() > 1 ) {
				double n = res1list.size(); 
				System.out.printf( "mean: %f %f\n", bdMean / n, ndMean / n ); 
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
