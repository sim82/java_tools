package wombat.pack;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.management.RuntimeErrorException;

import sun.security.provider.MD5;

import wombat.StreamConsumerThread;
import wombat.pack.Rx.RxFile;


class TeeStreamConsumer extends Thread {
	InputStream is;
	private BufferedOutputStream os;
	public TeeStreamConsumer( InputStream is, FileOutputStream os ) {
		this.is = is;
		this.os = new BufferedOutputStream(os);
	}
	
	public void run() {
		int c;
		
		try {
			while( !isInterrupted() && (c = is.read()) != -1 ) {
				System.out.print((char) c);
				os.write((char)c);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


public class Tx implements Serializable {
	final static long MAX_SIZE = 1024 * 1024 * 20;
	static class FileMapping implements Serializable {
		/**
		 * 
		 */
		String origName;
		String tmpName;
		int ci;
		byte[] storedFile;
		boolean isExecutable;
		boolean isGz; 
		byte[] md5sum;
		
		public FileMapping( String origName, String tmpName, int ci ) {
			this.origName = origName;
			this.tmpName = tmpName;
			this.ci = ci;
		}
		
		public void store() {
			File f = new File(origName);
			if( !f.canRead() || !f.isFile() ) {
				throw new RuntimeException( "cannot store file " + origName	);
			}
			
			isExecutable = f.canExecute();
			isGz = true;
			md5sum = new byte[16];
			
			storedFile = Tx.readFile(f, isGz,  md5sum );
			
			String md5string = md5ToString( md5sum );
			System.out.printf( "file stored: '%s' => '%s' %s\n", origName, tmpName, md5string );
		}

		
	}
	
	String[] es;
	String[] esNew;
	ArrayList<FileMapping> fms = new ArrayList<FileMapping>();
	
	
	public Tx( String localDir, String commandline ) {
		StringTokenizer st = new StringTokenizer(commandline);
		
		
		ArrayList<String> esd = new ArrayList<String>();
		while( st.hasMoreTokens() ) {
			String token = st.nextToken();	
			int ci = esd.size(); 
			esd.add(token);
			
			
			
			final String name;
			if( token.startsWith("%r%") ) {
				name = token.substring(3);
			} else if( token.startsWith( "/" ) || token.startsWith("./") ){
				
				File probe = new File( token );
				if( probe.isFile() && probe.canWrite() ) {
					name = token;
				} else {
					name = null;
				}
			} else { 
				name = null;
			}
			
			if( name != null ) {
				int cim = fms.size();
				String tmpName = "./tmp_" + cim;
				
				FileMapping fm = new FileMapping( name, tmpName, ci );
				fms.add(fm);
			}
		}
		
		
		es = esd.toArray( new String[esd.size()] );
		esNew = es.clone();
		
		for( FileMapping fm : fms ) {
			esNew[fm.ci] = fm.tmpName;
			fm.store();
		}
		
		String cmdNew = "";
		for( String e : esNew ) {
			if( cmdNew.length() > 0 ) {
				cmdNew += " ";
			}
			
			cmdNew += e;
			
		}
		
		System.out.printf( "command transformed:\n'%s'\n'%s'\n", commandline, cmdNew );
	}
	
	



	public static byte[] readFile(File f, boolean isGz, byte[] md5sum ) {
		long size = f.length();
		
		if( size > MAX_SIZE ) {
			throw new RuntimeException( "file too large for storage: " + f );
		}
		
		MessageDigest digest;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException( "could not get MD5 digest" );
		}
		if( digest.getDigestLength() != md5sum.length ) {
			throw new RuntimeException( "wrong length buffer supplied for md5 sum");
		}
		
		
		if( !isGz ) {
			byte[] buf = new byte[(int) size];
			try {
				FileInputStream is = new FileInputStream(f);
				int rs = is.read(buf);
				is.close();
				
				
				if( rs != size ) {
					throw new RuntimeException("read returned wrong size. bailing out.\n" );
				}
				digest.update(buf);
				
				System.arraycopy(digest.digest(), 0, md5sum, 0, md5sum.length);
				return buf;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				throw new RuntimeException("io exception while trying to store file. bailing out");
			}
			
		
		} else {
			try {
				FileInputStream is = new FileInputStream(f);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos);
				
				final int BUFFSIZE = 4096;
				byte[] buf = new byte[BUFFSIZE];
				
				int nb;
				
				while( (nb = is.read(buf)) > 0 ) {
					digest.update(buf, 0, nb);
					zos.write(buf, 0, nb);
				}
				zos.close();
				is.close();
				
				System.arraycopy(digest.digest(), 0, md5sum, 0, md5sum.length);
				
				return bos.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				throw new RuntimeException("io exception while trying to store file. bailing out");
			}
		}
		
		
	}


	Rx execute( File tmpRoot, boolean installHook ) {
		Random rnd = new Random();
		
		
		File tmpDir = null;
		int i = 0;
		while( tmpDir == null ) {
			tmpDir = new File( tmpRoot, "pack_" + Math.abs(rnd.nextInt()) );
			
			if( tmpDir.isFile() || tmpDir.isDirectory() ) {
				tmpDir = null;
				
			} else {
				boolean ret = tmpDir.mkdir();
				
				if( !ret ) {
					throw new RuntimeException( "mkdir returned false. bailing out. " + tmpDir );
				}
			}
			
			if( i > 10 ) {
				throw new RuntimeException( "failed to create tmp dir after 10th attempt. bailing out." );
			}
			i++;
		}
		
		
		Set<String>rxExclude = new HashSet<String>();
		
		for( FileMapping fm : fms ) {
			File tmpFile = new File( tmpDir, fm.tmpName );
			if( tmpFile.isFile() || tmpFile.isDirectory() ) {
				throw new RuntimeException( "temp file we want to create already exists!? bailing out." );
			}
			
			if( fm.storedFile == null ) {
				throw new RuntimeException( "WTF!? storedFile in FileMapping is null. bailing out.");
			}
	
			byte[] md5sum = writeFile( tmpFile, fm.storedFile, fm.isExecutable, fm.isGz );
			
			if( !Arrays.equals(fm.md5sum, md5sum) ) {
				System.out.printf( "bad md5sum for stored file '%s' => '%s'.\n%s vs. %s\n", fm.origName, fm.tmpName, md5ToString(md5sum), md5ToString(fm.md5sum) );
			}
			
			try {
				rxExclude.add(tmpFile.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
				
				throw new RuntimeException("WTF!? tmpFile.getCanonicalPath failed. bailing out.");
			}
		}
		
		
		
		String[] esNewMod = new String[3];
		esNewMod[0] = "/bin/bash";
		esNewMod[1] = "-c";
		esNewMod[2] = join( esNew );
		
		
		ProcessBuilder pb = new ProcessBuilder( esNewMod );
		pb.directory(tmpDir);
		
		try {
			final boolean[] removeHook = {true}; // oh, well. strange problems call for strange solutions ...
			final Process process = pb.start();
			
			final Thread shutdownHandler;
			if( installHook ) {
				shutdownHandler = new Thread() {
					public void run() {
						// if the shutdown is already in progress, prevent the removal of the shutdown hook later.
						removeHook[0] = false;
						process.destroy();
					}
				};
				Runtime.getRuntime().addShutdownHook(shutdownHandler);
			} else {
				shutdownHandler = null;
			}
			
			
			StreamConsumerThread isc = new StreamConsumerThread(process.getInputStream(), new File(tmpDir, "out.txt"));
			StreamConsumerThread esc = new StreamConsumerThread(process.getErrorStream(), new File(tmpDir, "err.txt"));
			isc.start();
			esc.start();
			
			try {
				
				
				int ret = process.waitFor();
				System.out.printf( "process returned: %d\n", ret );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				
				System.out.printf( "interrupt exception\n" );
				process.destroy();
			} finally {
				isc.interrupt();
				esc.interrupt();
				
				try {
					process.getInputStream().close();
					
				} catch (IOException e) {
				
				}
				try {
					process.getOutputStream().close();
				} catch (IOException e) {
				
				}
				try {
					process.getErrorStream().close();
				} catch (IOException e) {
				
				}
				
//				System.out.printf( "waiting for reader threads: %d\n", serial );
//				try {
//					isc.join();
//					esc.join();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}

				
				//System.out.printf( "what happened?\n" );
				
				

			}
			
			if( shutdownHandler != null && removeHook[0] ) {
				Runtime.getRuntime().removeShutdownHook(shutdownHandler);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		Rx rx = new Rx(tmpDir, rxExclude);
		
		deleteTmpdir( tmpDir );
		
		
		return rx;
	}
	
	
		
	
	private void deleteTmpdir(File tmpDir) {
		for( File tdf : tmpDir.listFiles() ) {
			if( tdf.isFile() && tdf.canRead() ) {
				//String name = tdf.getName();
				tdf.delete();
				
			}
		}
		tmpDir.delete();
	}


//	public static void main(String[] args) {
//		Pack p = new Pack( ".", "%r%/mnt/nufa1/berger/mlali_pe/raxml-hpc/raxmlHPC -f A -m GTRGAMMA -t %r%/mnt/nufa1/berger/tree//redtree/RAxML_bipartitions.1604.BEST.WITH_0009 -s %r%/mnt/nufa1/berger/tree//dist_subseq_alignments/1604_0009_200_60 -n 1604_0009_200_60_A");
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( new GZIPOutputStream(new FileOutputStream("/tmp/pack"))));
//			oos.writeObject(p);
//			oos.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private String join(String[] l) {
		String out = null;
		
		for( String s : l ) {
			if( out == null ) {
				out = s;
			} else {
				out += " " + s;
			}
		}
		return out;
	}


	static byte[] writeFile(File tmpFile, byte[] storedFile, boolean isExecutable, boolean isGzip ) {
		try {
			MessageDigest digest;
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new RuntimeException( "could not get MD5 digest" );
			}
			
			FileOutputStream os = new FileOutputStream(tmpFile);
			if( !isGzip ) {
				digest.update(storedFile);
				os.write(storedFile);
			} else {
				GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(storedFile));
				
				final int BUFSIZE = 4096;
				byte[] buf = new byte[BUFSIZE];
				
				int nb;
				
				
				while( (nb = is.read(buf)) > 0 ) {
					digest.update(buf, 0, nb);
					os.write(buf, 0, nb);
				}
				
			}
			os.close();
			if( isExecutable ) {
				boolean ret = tmpFile.setExecutable(true);
				if( !ret ) {
					throw new IOException( "could not set executable permissions.");
				}
			}
			
			return digest.digest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("bailing out.");
		}
	}


	public static void main(String[] args) throws IOException {
		final String batchfile;
		if( args.length > 0 ) {
			batchfile = args[0];
		} else {
			batchfile = "/space/tmp/run.wb";
		}
		
		BufferedReader br = new BufferedReader(new FileReader(batchfile));
		
		String line;
		int serial = 1;
		
		final File txDir = new File( "./tx" );
		txDir.mkdir();
		
		ExecutorService exec = Executors.newFixedThreadPool(2);
		//ExecutorService exec = Executors.newSingleThreadExecutor();
		
		
		while( ( line = br.readLine()) != null ) {
			if( line.length() > 0 && !line.startsWith("#") ) {
				System.out.printf( "start task: '%s'\n", line );
				
				final String cmd = line;
				final int mySerial = serial;
				
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						Tx p = new Tx( ".", cmd );
						try {
							File txFile = new File( txDir, "tx_" + padnum(mySerial, 5));
							//ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( new GZIPOutputStream(new FileOutputStream(txFile))));
							ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream(txFile)));
							oos.writeObject(p);
							oos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		
					}
				};
		
				
				exec.execute(r);
				serial++;
			}
			
		}
		
		br.close();
		
		exec.shutdown();
		try {
			exec.awaitTermination(1000 * 365, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static String padnum( int num, int nd ) {
		String ns = "" + num;
		
		while( ns.length() < nd ) {
			ns = "0" + ns;
		}
		
		return ns;
	}
	
	public static String md5ToString(byte[] md5sum) {
		String out = "";
		
		for( byte b : md5sum ) {
			out += toHex(0xFF & b);
		}
		
		return out;
	}
	static String toHex( int v ) {
		String out = "";
		
		
		while( v > 0 ) {
			out = toHex1( v & 0xF ) + out;
			v >>= 4;
		}
		
		return out;
	}
	
	static char toHex1( int i ) {
		if( i >= 0 && i <= 9 ) {
			return (char) ('0' + i);
		} else if( i <= 15 ) {
			return (char) ('a' + i - 10);
		} else {
			return 'X';
		}
	}
}
