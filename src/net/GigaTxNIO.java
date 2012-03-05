package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class GigaTxNIO {
    public static void main(String[] args) throws IOException,
	    InterruptedException {
	final int MTU = 1500;

	final SocketAddress txsendtoaddr = new InetSocketAddress("127.0.0.1",
		21845);
	final DatagramChannel txc = DatagramChannel.open();
	txc.socket().bind(null);
	Thread writer = new Thread() {

	    @Override
	    public void run() {
		// TODO Auto-generated method stub
		// java.nio.ByteBuffer txb = MappedByteBuffer.allocate(MTU);
		java.nio.ByteBuffer txb = java.nio.ByteBuffer
			.allocateDirect(MTU);
		int i = 0;
		long time = System.currentTimeMillis();

		long txbytes = 0;
		while (i < 100000000) {
		    txb.rewind();

		    txb.asIntBuffer().put(0, i);
		    txb.rewind();
		    try {
			txbytes += txc.send(txb, txsendtoaddr);

			//
			// for( int j = 0; j < 100000; j++ ) {
			// nj++;
			// }
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    i++;

		    if (i % 10000 == 0) {
			System.out.printf("tx -> rx %d\n", i);
		    }

		    // if( ack.i != -1 ) {
		    // i = ack.i;
		    // ack.i = -1;
		    // }
		}
		long dt = System.currentTimeMillis() - time;
		System.out.printf("%d bytes in %d ms: %.2f Mb/s\n", txbytes,
			dt, txbytes / (dt * 1000.0));
	    }
	};

	writer.start();

	writer.join();
    }
}
