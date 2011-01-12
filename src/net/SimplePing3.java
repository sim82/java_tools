package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class SimplePing3 {
	public static void main(String[] args) throws IOException, InterruptedException {
		final DatagramSocket sock = new DatagramSocket(12350);
		//sock.bind(new InetSocketAddress());
		
		
		byte[] txb = new byte[1024];
		
		while( true ) {
			byte[] rxb = new byte[2048];
			DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
			
			
			try {
				sock.receive(rxp);
				
				System.out.printf( "received packet of length: %d from %s\n", rxp.getLength(), rxp.getAddress().toString() );
				
				
				for( int i = 0; i < 1000; i++ ) {
					DatagramPacket txp = new DatagramPacket(txb, txb.length, rxp.getAddress(), 12340 );
					sock.send(txp);
				}
				DatagramPacket txp = new DatagramPacket(txb, txb.length / 2, rxp.getAddress(), 12340 );
				sock.send(txp);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException( "bailing out." );
			}
		}
		
	}
}
