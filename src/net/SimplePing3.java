package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class SimplePing3 {
	public static void main(String[] args) throws IOException, InterruptedException {
		final DatagramSocket sock = new DatagramSocket(12350);
		//sock.bind(new InetSocketAddress());
		
		
		final byte[] txb = new byte[1024];
		
		class Guard implements Runnable {
			long lastRecv = System.currentTimeMillis();

			@Override
			public void run() {
				while( !Thread.interrupted() ) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if( System.currentTimeMillis() - lastRecv > 10000 ) {
						lastRecv = System.currentTimeMillis();
						
						if( addr != null ) {
							DatagramPacket txp = new DatagramPacket(txb, txb.length / 2, addr, port );
							try {
								sock.send(txp);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							System.out.printf( "rescue packet sent\n" );
						
						}
						
					}
				}
				
				
			}

			
			InetAddress addr = null;
			int port = 0;
			public void setAddr(InetAddress address, int i) {
				addr = address;
				port = i;
				
			}
			
			public void ping() {
				lastRecv = System.currentTimeMillis();
			
			}
		}

		
		Guard guard = new Guard();
		Thread t = new Thread(guard);
		t.start();
		
		while( true ) {
			byte[] rxb = new byte[2048];
			DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
			
			try {
				
				
				
				sock.receive(rxp);
				
				guard.setAddr( rxp.getAddress(), 12340 );
				
				System.out.printf( "received packet of length: %d from %s\n", rxp.getLength(), rxp.getAddress().toString() );
				
				
				for( int i = 0; i < 100000; i++ ) {
					txb[0] = (byte)i;
					DatagramPacket txp = new DatagramPacket(txb, txb.length, rxp.getAddress(), 12340 );
					sock.send(txp);
				}
				DatagramPacket txp = new DatagramPacket(txb, txb.length / 2, rxp.getAddress(), 12340 );
				sock.send(txp);
				guard.ping();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException( "bailing out." );
			}
		}
//		t.interrupt();
	}
}
