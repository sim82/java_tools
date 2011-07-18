package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class SimplePing {
	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket sock = new DatagramSocket(21844);
		//sock.bind(new InetSocketAddress());
		
		
		//byte[] buf = {0,1,2,5,8,16,32,64};
		byte[] buf = new byte[8*8];
		
		
		//byte[] addr = {(byte) 192, (byte) 168, 1, 1};
		byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(addr), 21845 );
		
		while( true ) {
			sock.send(p);
			Thread.sleep(1000);
		}
	}
}
