package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

public class GigaRxLossy {
    public static void main(String[] args) throws IOException,
	    InterruptedException {
	final int MTU = 1500;

	final SocketAddress rxaddr = new InetSocketAddress(21845);

	final DatagramChannel rxc = DatagramChannel.open();

	rxc.socket().bind(rxaddr);

	final SocketAddress txsendtoaddr = new InetSocketAddress("127.0.0.1",
		rxc.socket().getLocalPort());

	boolean haveTx = false;
	final DatagramChannel txc;
	if (haveTx) {
	    txc = DatagramChannel.open();
	    txc.socket().bind(null);
	} else {
	    txc = null;
	}

	final Thread reader = new Thread() {

	    @Override
	    public void run() {

		java.nio.ByteBuffer rxb = ByteBuffer.allocateDirect(MTU);

		int firstser = -1;
		int lastser = -1;
		int nrec = 0;
		long time = System.currentTimeMillis();

		long rxbytes = 0;
		try {
		    // rxc.connect(rxaddr);

		    while (!isInterrupted()) {

			rxb.rewind();
			rxc.receive(rxb);
			int rxsize = rxb.position();

			rxb.rewind();
			int ser = rxb.asIntBuffer().get(0);

			if (firstser == -1) {
			    firstser = ser;
			}

			lastser = ser;
			nrec++;
			rxbytes += rxsize;

		    }
		} catch (ClosedByInterruptException e) {
		    System.out.printf("reader: interrupted. bye ...\n");

		} catch (IOException e) {

		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    throw new RuntimeException("bailing out.");
		}
		long dt = System.currentTimeMillis() - time;
		System.out.printf("%d bytes in %d ms: %.2f Mb/s\n", rxbytes,
			dt, rxbytes / (dt * 1000.0));
		int serrange = (lastser - firstser) + 1;
		System.out.printf("nrec: %d of %d (%.2f%%)\n", nrec, serrange,
			nrec / (float) serrange * 100.0);
	    }
	};

	reader.start();

	if (haveTx) {
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
		    while (i < 1000000) {
			txb.rewind();

			txb.asIntBuffer().put(0, i);
			txb.rewind();
			try {
			    txbytes += txc.send(txb, txsendtoaddr);

			    for (int j = 0; j < 100000; j++) {
			    }
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
		    System.out.printf("%d bytes in %d ms: %.2f Mb/s\n",
			    txbytes, dt, txbytes / (dt * 1000.0));
		}
	    };

	    writer.start();

	    writer.join();
	} else {
	    Thread.sleep(10000);
	}
	reader.interrupt();
	reader.join();
    }
}
