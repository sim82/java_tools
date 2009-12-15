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
package wombat.pack2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import wombat.MyRunnable;
import wombat.RunnableSet;
import wombat.StreamConsumerThread;

public class TxExec {
	static RxData execute( TxData tx, File tmpRoot, boolean installHook ) {
		Random rnd = new Random();
		long firstTime = System.currentTimeMillis();
		
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
		
		for( TxData.FileMapping fm : tx.fms ) {
			File tmpFile = new File( tmpDir, fm.tmpName );
			if( tmpFile.isFile() || tmpFile.isDirectory() ) {
				throw new RuntimeException( "temp file we want to create already exists!? bailing out." );
			}
			
			if( fm.storedFile == null ) {
				throw new RuntimeException( "WTF!? storedFile in FileMapping is null. bailing out.");
			}
	
			byte[] md5sum = Tx.writeFile( tmpFile, fm.storedFile, fm.isExecutable, fm.isGz );
			
			if( !Arrays.equals(fm.md5sum, md5sum) ) {
				System.out.printf( "bad md5sum for stored file '%s' => '%s'.\n%s vs. %s\n", fm.origName, fm.tmpName, Tx.md5ToString(md5sum), Tx.md5ToString(fm.md5sum) );
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
		esNewMod[2] = join( tx.esNew );
		
		
		ProcessBuilder pb = new ProcessBuilder( esNewMod );
		pb.directory(tmpDir);
		
		long startTime = System.currentTimeMillis();
		
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
		
		long endTime = System.currentTimeMillis();
		
		RxData rx = Rx.RxNew(tmpDir, rxExclude);
		
		rx.timeFirst = firstTime;
		rx.timeStart = startTime;
		rx.timeEnd = endTime;
		
		deleteTmpdir( tmpDir );
		
		
		return rx;
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		
		final File f;
		
		if( args.length > 0 ) {
			f = new File( args[0]);
		} else {
			f = new File( "/space/tmp/pack_1");
		}
		
		final File tmpDir;
		if( args.length > 1 ) {
			tmpDir = new File( args[1] );
		} else {
			tmpDir = new File( "/space/pack_tmp"); 
		}
		
		File rxFile = new File( f.getPath() + ".rx" );
		execute(f, tmpDir, rxFile, true );
	}

	public static void execute( File txFile, File tmpDir, File rxFile, boolean installHook ) throws FileNotFoundException, IOException, ClassNotFoundException {
		//ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream(new GZIPInputStream( new FileInputStream(txFile))));
		ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream(new FileInputStream(txFile)));
		
		
		TxData pack = (TxData) ois.readObject();
		ois.close();
		
		RxData rx = TxExec.execute( pack, tmpDir, installHook);
		
		ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream(new FileOutputStream(rxFile)));
		oos.writeObject(rx);
		oos.close();	
	}
	
	public static Runnable newRunnable(final String line, final int serial, RunnableSet rs) {
		return new MyRunnable() {
			
			private long startTime;

			@Override
			public void run() {
				
				StringTokenizer st = new StringTokenizer(line);
				
				if( st.countTokens() != 3 ) {
					System.out.printf( "at the moment we expect a tx runner commandline to consist of 3 token: ", line );
					return;
				}
				
				String first = st.nextToken();
				
				if( !first.equals("%tx2%" )) {
					System.out.println( "baaad commandline for the creation of a Tx Runnable: " + line );
					return;
				}
					
				String scratchDirName = st.nextToken();
				String txFileName = st.nextToken();
				
				File tmpDir = new File( scratchDirName );
				File txFile = new File( txFileName );
				File rxFile = new File( txFile.getPath() + ".rx" );
				
				startTime = System.currentTimeMillis();
				
				try {
					execute(txFile, tmpDir, rxFile, false );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.printf( "some exception occured during tx execution for commandline: " + line );
					return;
				} 
			}

			@Override
			public String getInfo(boolean b) {
				return "tx2 executable";
			}

			@Override
			public int getSerial() {
				return serial;
			}

			@Override
			public long startTime() {
				return startTime;
			}
		};
	}



	private static void deleteTmpdir(File tmpDir) {
		for( File tdf : tmpDir.listFiles() ) {
			if( tdf.isFile() && tdf.canRead() ) {
				//String name = tdf.getName();
				tdf.delete();
				
			}
		}
		tmpDir.delete();
	}



	private static String join(String[] l) {
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
}
	