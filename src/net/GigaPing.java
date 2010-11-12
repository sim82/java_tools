package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GigaPing {
	public static void main(String[] args) throws IOException, InterruptedException {
		final int MTU = 1500;
		
		
		final DatagramSocket txsock = new DatagramSocket(21844);
		final DatagramSocket rxsock = new DatagramSocket(21845);
		//sock.bind(new InetSocketAddress());
		
		byte[] buf = new byte[MTU];
		final byte[] abuf = new byte[4];
		
		final byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		class MyReader implements Runnable {
			
			@Override
			public void run() {
				int i = 0;
				
				while( true ) {
					byte[] rxb = new byte[2048];
					DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
					try {
						rxsock.receive(rxp);
						
						//System.out.printf( "received packet of length: %d from %s\n", rxp.getLength(), rxp.getAddress().toString() );
						byte[] bb = rxp.getData();
						int serial = bb[0] + (bb[1] << 8) + (((int)bb[2]) << 16) + (((int)bb[3]) << 24);
						
						
//						System.out.printf( "%d received packet of length: %d from %s:%d (%d) (%d %d %d %d)\n", i, rxp.getLength(), rxp.getAddress().toString(), rxp.getPort(), serial, bb[0], bb[1], bb[2], bb[3] );
				
						
						DatagramPacket retp = new DatagramPacket(abuf, 4, rxp.getAddress(), 21844 );
						rxsock.send( retp );
						
						i++;
//						if( i % 1000 == 0 ) {
//							System.out.printf( "received %d\n", i );
//						}			
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException( "bailing out." );
					}
				}
			}
		};
		
		Thread rt = new Thread( new MyReader() );
		rt.start();
//		Thread rt2 = new Thread( new MyReader() );
//		rt2.start();
		
		rt.join();
		
		//byte[] buf = {0,1,2,5,8,16,32,64};
		
		
		
		//byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(addr), 21845 );
		
		int i = 0;
		byte[] rxb = new byte[2048];
		
		long time = System.currentTimeMillis();
		while( i < 1000000 ) {
			txsock.send(p);
			DatagramPacket rxp = new DatagramPacket(rxb, rxb.length );
			txsock.receive(rxp);
			//Thread.sleep(1);
			i++;
			
			if( i % 10000 == 0 ) {
				System.out.printf( "tx -> rx %d\n", i );
			}
		}
		
		long dt = System.currentTimeMillis() - time;
		
		System.out.printf( "%d bytes in %d ms: %.2f Mb/s\n", i * MTU, dt, (i * MTU) / (dt * 1000.0) );
		
		Double.toString(123);
	}
}
