package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class GigaTx {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		final int MTU = 1500;
		
		
		final DatagramSocket txsock = new DatagramSocket(21844);
		
		
		//byte[] buf = {0,1,2,5,8,16,32,64};
		
		
		byte[] buf = new byte[MTU];
		
		
		byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		//byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(addr), 21845 );
		
		int i = 0;
		
		long time = System.currentTimeMillis();
		
		while( i < 1000000 ) {
			txsock.send(p);
			//Thread.sleep(1);
			i++;
			
			if( i % 1000 == 0 ) {
				//System.out.printf( "sent %d\n", i );
				Thread.sleep(6);
			}
		}
		
		long dt = System.currentTimeMillis() - time;
		
		System.out.printf( "%d bytes in %d ms: %.2f Mb/s\n", i * MTU, dt, (i * MTU) / (dt * 1000.0) );
	}
}
