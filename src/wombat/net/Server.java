package wombat.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import wombat.pack2.RxData;
import wombat.pack2.TxData;
import wombat.pack2.TxExec;

class ClientHandler implements Runnable {

	Socket csock;
	int serial;
	private File tmpDir;
	
	ClientHandler( Socket csock, int serial, File tmpDir ) {
		this.csock = csock;
		this.serial = serial;
		this.tmpDir = tmpDir;
	}
	
	@Override
	public void run() {

		try {
			InputStream cis = csock.getInputStream();
			
			OutputStream cos = csock.getOutputStream();
			cos.write( "rdy!".getBytes() );
			
			
			ObjectInputStream ois = new ObjectInputStream(cis);
			
			try {
				Object ro = ois.readObject();
				if( ro instanceof Payload ) {
					Payload pl = (Payload)ro;
					pl.print();
					pl = null;
					
					ro = null;
					
					
					pl = new Payload();
					pl.bla = "Hello client";
					pl.blai = 1234;
					pl.big = new byte[1024 * 1024 * 100];
					ObjectOutputStream oos = new ObjectOutputStream(cos);
					oos.writeObject(pl);
				} else if( ro instanceof TxData ) {
					RxData rx = TxExec.execute( (TxData)ro, tmpDir, false);
					
					ObjectOutputStream oos = new ObjectOutputStream( cos );
					oos.writeObject(rx);
					
									
				}
				
				
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch( IOException x ) {
			x.printStackTrace();
			System.out.printf( "client handler dying\n" );
		}
		System.out.printf( "client handle exit\n" );
	}
	
	
}

public class Server {
	public static void main(String[] args) {
		final int sport = 1234;
		
		
		try {
			ServerSocket ssock = new ServerSocket(sport);
	
			
			File tmpDir = new File( "./ws_tmp" );
			if( !tmpDir.isDirectory() ) {
				tmpDir.mkdir();
			}
			int serial = 1;
			while( true ) {
				
				
				Socket csock = ssock.accept();
				
				
				ClientHandler ch = new ClientHandler( csock, serial, tmpDir );
	
				Thread ct = new Thread( ch );
				System.out.printf( "forking off client handler...\n" );
				ct.start();
				serial++;
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
}
