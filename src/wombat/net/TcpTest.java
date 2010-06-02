package wombat.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import util.StringTools;



class Payload implements Serializable {
	String bla = null;
	int blai = 0;
	byte big[];
	
	void print() { 
		System.out.printf( "payload %s %d %d\n", bla, blai, big.length );
	}
	
}


public class TcpTest {
	
	
	public static void main(String[] args) {
		
		final String sname;
		
		if( args.length > 1 ) {
			sname = args[1];
		} else {
			sname = "localhost";
		}
		
		final int mode;
		
		if( args.length > 0 ) {
			mode = Integer.parseInt(args[0]);
		} else {
			mode = 3;
		}
		
		final int sport = 1234;
		
		Runnable sr = new Runnable() {
			
			@Override
			public void run() {
				try {
					ServerSocket ssock = new ServerSocket(sport);
					
					Socket csock = ssock.accept();
					
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
						}
						
						ro = null;
						
						
						Payload pl = new Payload();
						pl.bla = "Hello client";
						pl.blai = 1234;
						pl.big = new byte[1024 * 1024 * 100];
						ObjectOutputStream oos = new ObjectOutputStream(cos);
						oos.writeObject(pl);
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		};
		
		
		
		Runnable cr = new Runnable() {

			@Override
			public void run() {
				try {
					Socket csock = new Socket( sname, sport );
					
					InputStream cis = csock.getInputStream();
					byte rcv[] = new byte[1024];
					cis.read(rcv, 0, 4);
					
					System.out.printf( "read: %s\n", new String(rcv) );
					{
						Payload pl = new Payload();
						pl.bla = "Hello server!";
						pl.blai = 666;
						pl.big = new byte[1024 * 1024 * 100];
						
						OutputStream cos = csock.getOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(cos);
						oos.writeObject(pl);
					}
					ObjectInputStream ois = new ObjectInputStream(cis);
					try {
						Object ro = ois.readObject();
						if( ro instanceof Payload ) {
							Payload pl = (Payload)ro;
							pl.print();
							pl = null;
						}
						
						ro = null;
						
						
					
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		};
		
		if( (mode & 1) != 0 ) {
			(new Thread(sr)).start();
		} 
		
		if( (mode & 2) != 0 ) {
			(new Thread(cr)).start();
		}
		
	}
}
