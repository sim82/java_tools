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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import wombat.StreamConsumerThread;


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


public class Tx {
	final static long MAX_SIZE = 1024 * 1024 * 20;
	public static void store( TxData.FileMapping fm ) {
		File f = new File(fm.origName);
		if( !f.canRead() || !f.isFile() ) {
			throw new RuntimeException( "cannot store file " + fm.origName	);
		}
		
		fm.isExecutable = f.canExecute();
		fm.isGz = true;
		fm.md5sum = new byte[16];
		
		fm.storedFile = Tx.readFile(f, fm.isGz,  fm.md5sum );
		
		String md5string = md5ToString( fm.md5sum );
		System.out.printf( "file stored: '%s' => '%s' %s\n", fm.origName, fm.tmpName, md5string );
	}
	
	public static TxData TxNew( String localDir, String commandline ) {
		StringTokenizer st = new StringTokenizer(commandline);

		ArrayList<TxData.FileMapping> fmsd = new ArrayList<TxData.FileMapping>();

		
		ArrayList<String> esd = new ArrayList<String>();
		Map<String,Integer>currentMappings = new HashMap<String, Integer>();
		Map<Integer,Integer>posToFm = new HashMap<Integer,Integer>();
		
		while( st.hasMoreTokens() ) {
			String token = st.nextToken();	
			int ci = esd.size(); 
			esd.add(token);
			
			
			
			final String name;
			if( token.startsWith("%r%") ) {
				name = token.substring(3);
			} else if( token.startsWith( "/" ) || token.startsWith("./") ){
				
				File probe = new File( token );
				// FIXME: review this when there is more time: do we really want to check for WRITE access?
				if( probe.isFile() && probe.canWrite() ) {
					name = token;
				} else {
					if( token.startsWith( "/" ) ) {
						System.out.printf( "WARNING: command contains reference to global file that could not be stored:.\n%s\n", token );
					}
					
					name = null;
				}
			} else { 
				name = null;
			}
			
			if( name != null ) {
				
				final String cname;
				try {
					cname = new File(name).getCanonicalPath();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					throw new RuntimeException( "bailing out." );
				}
				
				
				if( !currentMappings.containsKey(cname)) {
					int cim = fmsd.size();
					String tmpName = "./tmp_" + cim;
					
					TxData.FileMapping fm = new TxData.FileMapping( cname, tmpName );
					posToFm.put( ci, cim );
					currentMappings.put( cname, cim );
					fmsd.add(fm);
				} else {
					posToFm.put( ci, currentMappings.get( cname ));
				}
				
			}
		}
		
		TxData tx = new TxData();
		
		tx.es = esd.toArray( new String[esd.size()] );
		tx.esNew = tx.es.clone();
		
		tx.fms = fmsd.toArray( new TxData.FileMapping[fmsd.size()] );
		
		for( TxData.FileMapping fm : tx.fms ) {
			store(fm);
		}
		
		for( Map.Entry<Integer,Integer> e : posToFm.entrySet() ) {
			tx.esNew[e.getKey()] = tx.fms[e.getValue()].tmpName;
		}
		
		String cmdNew = "";
		for( String e : tx.esNew ) {
			if( cmdNew.length() > 0 ) {
				cmdNew += " ";
			}
			
			cmdNew += e;
			
		}
		
		System.out.printf( "command transformed:\n'%s'\n'%s'\n", commandline, cmdNew );
		
		return tx;
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
				//System.out.printf( "start task: '%s'\n", line );
				
				final String cmd = line;
				final int mySerial = serial;
				
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						TxData p = TxNew( ".", cmd );
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
