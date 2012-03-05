package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GigaRx {
    public static void main(String[] args) throws IOException,
	    InterruptedException {
	final DatagramSocket rxsock = new DatagramSocket(21845);
	// sock.bind(new InetSocketAddress());

	int i = 0;

	while (true) {
	    byte[] rxb = new byte[2048];
	    DatagramPacket rxp = new DatagramPacket(rxb, rxb.length);
	    try {
		rxsock.receive(rxp);

		byte[] bb = rxp.getData();
		int serial = bb[0] + (bb[1] << 8) + (((int) bb[2]) << 16)
			+ (((int) bb[3]) << 24);

		System.out
			.printf("%d received packet of length: %d from %s (%d) (%d %d %d %d)\n",
				i, rxp.getLength(),
				rxp.getAddress().toString(), serial, bb[0],
				bb[1], bb[2], bb[3]);

		i++;
		if (i % 1000 == 0) {
		    System.out.printf("received %d\n", i);
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException("bailing out.");
	    }
	}
    }
}
