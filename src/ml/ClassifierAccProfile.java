package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

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
		
		
		int N_THREADS = 3;
		ThreadPoolExecutor tpe = new ThreadPoolExecutor( N_THREADS, N_THREADS, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(N_THREADS * 3));
		tpe.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		for( final File classFile : classFiles ) {
			
			tpe.execute( new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
			
					double avgs[] = new double[maxLen];
					int coverage[] = new int[maxLen];
					
					CSVFile cf = CSVFile.load( classFile, "sssiidddi" );
					
					
					String[] names = cf.getString(0);
					int[] nds = cf.getInteger(4);
					int[] starts = cf.getInteger(8);
					
					int ntaxa = 0;
					for( int i = 0; i < names.length; i++ ) {
						if( starts[i] < 0 ) {
							continue;
						}
						ntaxa++;
						
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
			});
			
			
		}
		
	}
	
}
