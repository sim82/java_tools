package wombat.pack;

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
import java.util.zip.GZIPInputStream;

public class Rx implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7656276938725569281L;

	static class RxFile implements Serializable {
		String localName;
		byte[] md5sum;
		boolean isGzip;
		byte[] data;
	}
	
	
	final RxFile[] rxfs;
	public Rx(File tmpDir, Set<String> rxExclude) {
	
		ArrayList<RxFile> tmp = new ArrayList<RxFile>();
		for( File tdf : tmpDir.listFiles() ) {
			if( tdf.isFile() && tdf.canRead() ) {
				//String name = tdf.getName();
				
				try {
					if( !rxExclude.contains(tdf.getCanonicalPath())) {
					//	System.out.printf( "candidate for rx: %s\n", tdf );
						
						RxFile rxf = new RxFile();
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
		
		rxfs = tmp.toArray(new RxFile[tmp.size()]);
	}

	
	public void unpack( File outDir ) {
		for( RxFile rxf : rxfs ) {
			File outFile = new File( outDir, rxf.localName );
			byte[] md5sum = Tx.writeFile( outFile, rxf.data, false, rxf.isGzip );
			
			if( !Arrays.equals(md5sum, rxf.md5sum )) {
				System.out.printf( "bad md5sum for stored file '%s'.\n%s vs. %s\n", rxf.localName, Tx.md5ToString(md5sum), Tx.md5ToString(rxf.md5sum) );
			}
		}
	}
	
	public static void main(String[] args) {
		
		if( args.length == 0 ) {
			File[] files = new File( "./" ).listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".rx");
				}
			});
			
			
			for( File file : files ) {
				rxExtract(file, new File("./"));
			}
		} else {
			File rxFile = new File( args[0] );
			
			
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
			Rx rx = (Rx) ois.readObject();
			
			rx.unpack(outDir);
			
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
