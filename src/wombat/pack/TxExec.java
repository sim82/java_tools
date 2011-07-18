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
package wombat.pack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

import wombat.RunnableSet;

public class TxExec {
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
		txExec(f, tmpDir, rxFile, true );
	}

	public static void txExec( File txFile, File tmpDir, File rxFile, boolean installHook ) throws FileNotFoundException, IOException, ClassNotFoundException {
		//ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream(new GZIPInputStream( new FileInputStream(txFile))));
		ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream(new FileInputStream(txFile)));
		
		
		Tx pack = (Tx) ois.readObject();
		ois.close();
		
		Rx rx = pack.execute(tmpDir, installHook);
		
		ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream(new FileOutputStream(rxFile)));
		oos.writeObject(rx);
		oos.close();	
	}
	
	public static Runnable newRunnable(final String line, final int serial, RunnableSet rs) {
		return new Runnable() {
			
			@Override
			public void run() {
				StringTokenizer st = new StringTokenizer(line);
				
				if( st.countTokens() != 3 ) {
					System.out.printf( "at the moment we expect a tx runner commandline to consist of 3 token: ", line );
					return;
				}
				
				String first = st.nextToken();
				
				if( !first.equals("%tx%" )) {
					System.out.printf( "baaad commandline for the creation of a Tx Runnable: " + line );
					return;
				}
					
				String scratchDirName = st.nextToken();
				String txFileName = st.nextToken();
				
				File tmpDir = new File( scratchDirName );
				File txFile = new File( txFileName );
				File rxFile = new File( txFile.getPath() + ".rx" );
				
				try {
					txExec(txFile, tmpDir, rxFile, false );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.printf( "some exception occured during tx execution for commandline: " + line );
					return;
				} 
			}
		};
	}
}
	