/*
 * 
 */
package mongobatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


class ClientConnection {
	Socket sock;
	StringBuffer buf;
	public int id;
}

class ResponderThread extends Thread {
	ArrayList<ClientConnection>clientConnections = new ArrayList<ClientConnection>();
	
	
	public void run() {
		while(true) {
			synchronized (clientConnections) {
				poll();
			}
			
			
			for( ClientConnection conn : clientConnections ) {
				int bsize = conn.buf.length();
				if( bsize > 0 ) {
//					System.out.printf( "%d recv: %s\n", conn.id, conn.buf.substring(0, bsize));
//					conn.buf.delete(0, bsize);

					System.out.printf( "%d recv %d\n", conn.id, bsize );
					parse( conn.buf );

					PrintStream ps;
					try {
						ps = new PrintStream( conn.sock.getOutputStream() );
						ps.print(">jojojojojo<");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			
			try {
				sleep(1000);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException( "bailing out" );
			}
		}
	}
	
	private void parse(StringBuffer buf) {
		int ptr = 0;
		
		int laststart = -1;
		int laststop = -1;
		int oobcount = 0;
		while( ptr < buf.length()) {
			char c = buf.charAt(ptr);
			
			if( c == '>' ) {
				if( laststart != -1 ) {
					System.out.printf( "bad start character\n" );
					
				}
				laststart = ptr;
			} else if( c == '<' ) {
				if( laststart < 0 ) {
					System.out.printf( "bad stop character\n" );
				}
				
				laststop = ptr;
				System.out.printf( "msg: '%s'\n", buf.substring(laststart, laststop + 1) );
				laststart = -1;
				
			} else {
				if( laststart < 0 ) {
					oobcount++;
				}
			}
			
			
			ptr++;
		}
		
		System.out.printf( "oob data: %d\n", oobcount );
		buf.delete( 0, laststop + 1);
	}

	void poll() {
		Iterator<ClientConnection> iter = clientConnections.iterator();
		
		while( iter.hasNext()) {
			ClientConnection conn = iter.next();
			
			Socket cs = conn.sock;
			
			if( !cs.isConnected() ) {
				System.out.printf( "client disconnected" );
				iter.remove();
				continue;
			}
			
			try {
				InputStream is = cs.getInputStream();
				
				int b;
				
				while( (b = is.available()) > 0 ) {
				
				
					byte[] recbuf = new byte[b];
					is.read( recbuf );
//					if( b != breal ) {
//						throw new RuntimeException( "b != breal: " + b + " " + breal );
//					}
					
					//System.out.printf( "read: '%s'\n", new String(recbuf) );
					
					conn.buf.append(new String(recbuf));
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void addClient( Socket client ) {
		synchronized (clientConnections) {
			ClientConnection conn = new ClientConnection();
			conn.sock = client;
			conn.buf = new StringBuffer();
			conn.id = clientConnections.size();
			clientConnections.add(conn);
		}
	}
	
}

public class Server {
	
	
	public static void main(String[] args) {
		try {
			ServerSocket sock = new ServerSocket();
			sock.bind( new InetSocketAddress( (InetAddress)null, 40666 ));
			
			int port = sock.getLocalPort();
			
			System.out.printf( "port: %d\n", port );
			
			
			ResponderThread rt = new ResponderThread();
			rt.start();
			
			while(true) {
				
				Socket cs = sock.accept();
				System.out.printf( "client connect\n" );
				rt.addClient(cs);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
