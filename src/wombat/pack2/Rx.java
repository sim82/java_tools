package wombat.pack2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;


public class Rx {

	/**
	 * 
	 */

	public static RxData RxNew(File tmpDir, Set<String> rxExclude) {
	
		RxData rx = new RxData();
		
		ArrayList<RxData.RxFile> tmp = new ArrayList<RxData.RxFile>();
		for( File tdf : tmpDir.listFiles() ) {
			if( tdf.isFile() && tdf.canRead() ) {
				//String name = tdf.getName();
				
				try {
					if( !rxExclude.contains(tdf.getCanonicalPath())) {
					//	System.out.printf( "candidate for rx: %s\n", tdf );
						
						RxData.RxFile rxf = new RxData.RxFile();
						rxf.localName = tdf.getName();
						
						rxf.isGzip = true;
						rxf.md5sum = new byte[16];
						rxf.data = Tx.readFile(tdf, rxf.isGzip, rxf.md5sum);
					
						tmp.add(rxf);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("WTF!? tmpFile.getCanonicalPath failed. bailing out.");
				}
			}
		}
		
		rx.rxfs = tmp.toArray(new RxData.RxFile[tmp.size()]);
		
		return rx;
	}

	
	public static void unpack( RxData rx, File outDir ) {
		for( RxData.RxFile rxf : rx.rxfs ) {
			File outFile = new File( outDir, rxf.localName );
			byte[] md5sum = Tx.writeFile( outFile, rxf.data, false, rxf.isGzip );
			
			if( !Arrays.equals(md5sum, rxf.md5sum )) {
				System.out.printf( "bad md5sum for stored file '%s'.\n%s vs. %s\n", rxf.localName, Tx.md5ToString(md5sum), Tx.md5ToString(rxf.md5sum) );
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		final File in;
		if( args.length > 0 ) {
			in = new File( args[0] );
		} else {
			in = null;
		}
		
		if( in == null || in.isDirectory() ) {
			File inDir = (in != null) ? in : new File( "./" );
			
			File[] files = inDir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".rx");
				}
			});
			
			//ExecutorService exec = Executors.newFixedThreadPool(2);
			ExecutorService exec = Executors.newSingleThreadExecutor();
			
			for( final File file : files ) {
				exec.execute( new Runnable() {
					
					@Override
					public void run() {
						rxExtract(file, new File("./"));
					}
				});
				
			}
			
			exec.shutdown();
			exec.awaitTermination(365 * 1000, TimeUnit.DAYS);
		} else {
			assert( in.isFile() );
			File rxFile = in;
			
			
			rxExtract(rxFile, new File("./"));
		}
	}


	private static void rxExtract(File rxFile, File outDir) {
		try {
			ObjectInputStream ois;
			if( false ) {
				ois = new ObjectInputStream( new BufferedInputStream( new GZIPInputStream( new FileInputStream(rxFile))));
			} else {
				ois = new ObjectInputStream( new BufferedInputStream( new FileInputStream(rxFile)));
			}
			RxData rx = (RxData) ois.readObject();
			
			unpack( rx, outDir);
			System.out.printf( "unpacked %s. rt: %d %d\n", rxFile.getName(), (rx.timeStart - rx.timeFirst), (rx.timeEnd - rx.timeStart) / 1000 );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
