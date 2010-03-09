package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GigaRx {
	public static void main(String[] args) throws IOException, InterruptedException {
		final int MTU = 1500;
		
		
		final DatagramSocket rxsock = new DatagramSocket(21845);
		//sock.bind(new InetSocketAddress());
		
		
		
		int i = 0;
		
		while( true ) {
			byte[] rxb = new byte[2048];
			DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
			try {
				rxsock.receive(rxp);
				
				System.out.printf( "received packet of length: %d from %s\n", rxp.getLength(), rxp.getAddress().toString() );
		
				i++;
				if( i % 1000 == 0 ) {
					System.out.printf( "received %d\n", i );
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException( "bailing out." );
			}
		}
	}
}
