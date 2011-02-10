package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class SimplePing2 {
	public static void main(String[] args) throws IOException, InterruptedException {
		final DatagramSocket sock = new DatagramSocket(21845);
		//sock.bind(new InetSocketAddress());
		
		Runnable reader = new Runnable() {
			
			@Override
			public void run() {
				while( true ) {
					byte[] rxb = new byte[2048];
					DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
					try {
						sock.receive(rxp);
						
						System.out.printf( "received packet of length: %d from %s\n", rxp.getLength(), rxp.getAddress().toString() );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException( "bailing out." );
					}
				}
			}
		};
		
		
		//byte[] buf = {0,1,2,5,8,16,32,64};
		byte[] buf = new byte[8*8];
		
		
		byte[] addr = {(byte) 192, (byte) 168, 1, 1};
		//byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(addr), 21845 );
		
		while( true ) {
			sock.send(p);
			Thread.sleep(1000);
		}
	}
}
