package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

class MutableInt {
	
	public int i = 0;
}

public class GigaPingNIO {
	public static void main(String[] args) throws IOException, InterruptedException {
		final int MTU = 1500;
		
		final DatagramChannel txc = DatagramChannel.open();
		txc.socket().bind(null);
		final SocketAddress txsockaddr = txc.socket().getLocalSocketAddress();
		
		final DatagramChannel rxc = DatagramChannel.open();
		rxc.socket().bind(null);
		final int rxport = rxc.socket().getLocalPort();
		final SocketAddress rxsockaddr = rxc.socket().getLocalSocketAddress();
		
		
		System.out.printf( "rx port: %d\n", rxport );
	
		
		final byte[] addr = {(byte) 127, (byte) 0, 0, 1};

		
		final MutableInt ack = new MutableInt();
		final MutableInt canExit = new MutableInt();
		
		
		final Thread reader = new Thread() {
			
			@Override
			public void run() {
				
				java.nio.ByteBuffer rxb = ByteBuffer.allocateDirect(MTU);
				
				int lastser = -1;
				int nrec = 0;
				int nloss = 0;

				
				long time = System.currentTimeMillis();

				long rxbytes = 0;
				try {
					rxc.connect(txsockaddr);
					
					
					while( !isInterrupted() ) {
					
							rxb.rewind();
							int rxsize = rxc.read(rxb);
							
							
							rxb.rewind();
							int ser = rxb.asIntBuffer().get(0);
					
							if( true ) {
								if( lastser >= 0 && ser > lastser + 1 ) {
									//System.out.printf( "loss: %d -> %d\n", lastser, ser );
									ack.i = lastser + 1;
								} 
								
							}
							
							if( ser == lastser + 1 ) {
								nrec++;
								rxbytes+=rxsize;
								lastser = ser;
							}
							if( lastser == 999999 ) {
								canExit.i = 1;
							}
	//						System.out.printf( "received packet of length: %d from %s\n", rxb.position(), rxaddr.toString() );
							//DatagramPacket retp = new DatagramPacket(rxb, rxb.length, InetAddress.getByAddress(addr), 21844 );
							//rxsock.send( retp );
							
							
	//						if( i % 1000 == 0 ) {
	//							System.out.printf( "received %d\n", i );
	//						}			
						
					}
				} catch( ClosedByInterruptException e ) {
					System.out.printf( "reader: interrupted. bye ...\n" );
				
				} catch (IOException e) {
				
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException( "bailing out." );
				}
				long dt = System.currentTimeMillis() - time;
				System.out.printf( "%d bytes in %d ms: %.2f Mb/s\n", rxbytes, dt, rxbytes / (dt * 1000.0) );
				System.out.printf( "nrec: %d (%d)\n", nrec, lastser );
			}
		};
		
		
		//byte[] buf = {0,1,2,5,8,16,32,64};
		
		
		
		//byte[] addr = {(byte) 127, (byte) 0, 0, 1};
		
		
		Thread writer = new Thread() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//java.nio.ByteBuffer txb = MappedByteBuffer.allocate(MTU);
				java.nio.ByteBuffer txb = java.nio.ByteBuffer.allocateDirect(MTU);
				int i = 0;
				long time = System.currentTimeMillis();

				long txbytes = 0;
				long nj = 0;
				while( canExit.i == 0 ) {
					txb.rewind();
					
					txb.asIntBuffer().put(0, i);
					txb.rewind();
					try {
						txbytes += txc.send(txb, rxsockaddr);
					
						
//						for( int j = 0; j < 1000; j++ ) {
//							nj++;
//						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i++;
					
//					if( i % 100 == 0 ) {
//						try {
//							sleep(1);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
					
					if( i % 10000 == 0 ) {
						System.out.printf( "tx -> rx %d\n", i );
					}
					
					if( ack.i != -1 ) {
						i = ack.i;
						ack.i = -1;
					}
				}
				long dt = System.currentTimeMillis() - time;
				System.out.printf( "%d bytes in %d ms: %.2f Mb/s\n", txbytes, dt, txbytes / (dt * 1000.0) );
			}
		};
				

		reader.start();		
		writer.start();
		
		writer.join();
		
		reader.interrupt();
		reader.join();
	}
}
