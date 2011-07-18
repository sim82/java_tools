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
package ml.oldtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ml.CSVFile;
import ml.ClassifierLTree;
import ml.ClassifierLTree.TaxonSMap;

public class ClassifierAccProfile {

	public static void main(String[] args) {
		File smapFile = new File(args[0]);
		//File classFile = new File(args[1]);
		
		final ClassifierLTree.TaxonSMap smap = new ClassifierLTree.TaxonSMap(smapFile); 
		
		final int maxLen = smap.maxLen();
		
		Iterable<File> classFiles;
		if( args.length >= 2 ) {
			ArrayList<File> cfs = new ArrayList<File>();
			for( int i = 1; i < args.length; i++ ) {
				cfs.add(new File( args[i] ));
			}
			
			classFiles = cfs;
		} else {
			throw new RuntimeException( "missing arguments" );
		}
		
		
		int N_THREADS = 1;
		ThreadPoolExecutor tpe = new ThreadPoolExecutor( N_THREADS, N_THREADS, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(N_THREADS * 3));
		tpe.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		for( final File classFile : classFiles ) {
			
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
			
					double avgs[] = new double[maxLen];
					int coverage[] = new int[maxLen];
					
					CSVFile cf = CSVFile.load( classFile, "sssiidddi" );
					
					
					String[] names = cf.getString(0);
					int[] nds = cf.getInteger(4);
					int[] starts = cf.getInteger(8);
					
					for( int i = 0; i < names.length; i++ ) {
						if( starts[i] < 0 ) {
							continue;
						}
						String seq = names[i];
						int seqSubIdx = -1; 
			            
			            int atIdx = seq.indexOf( '@' );
			            if( atIdx > 0 ) {
			            	String seqOrig = seq;
			            	seq = seq.substring(0, atIdx);
			            	seqSubIdx = Integer.parseInt(seqOrig.substring( atIdx + 1 ));
			            } else {
			            	throw new RuntimeException( "taxon name is expected to contain '@': " + names[i] );
			            }
						
						int[] tsmap = smap.getMap(seq);
						int astart = tsmap[seqSubIdx];
						
						//System.out.printf( "%d %d\n", starts[i], tsmap.length );
						int aend = tsmap[seqSubIdx+249];
						
						for( int j = astart; j <= aend; j++ ) {
							
							avgs[j] += nds[i];
							coverage[j]++;
						}
					}
					
					
				
					PrintStream ps;
					try {
						ps = new PrintStream( new File( "prof." + classFile.getName() + ".txt"));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException( "bailing out" );
					}
					
					for( int i = 0; i < avgs.length; i++ ) {
						if( coverage[i] > 0 ) {
							ps.printf( "%d\t%f\n", i, avgs[i] / coverage[i] );
						} else {
						//	ps.printf( "%d\t%f\n", i, -1.0 );
						}
					}
					ps.close();
				}
			};
			
			//tpe.execute( r );
			r.run();
			
		}
		
	}
	
}
