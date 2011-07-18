package wombat.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import wombat.pack2.Rx;
import wombat.pack2.RxData;
import wombat.pack2.Tx;
import wombat.pack2.TxData;

public class Client {
	public static void main(String[] args) {
		if( args.length < 2 ) {
			throw new RuntimeException( "missing arguments" );
		}
		final int sport = 1234;
		String sname = args[0];
		String cmd = args[1];
		TxData p = Tx.TxNew( ".", cmd );
		
		try {
			Socket csock = new Socket( sname, sport );
			
			InputStream cis = csock.getInputStream();
			byte rcv[] = new byte[4];
			cis.read(rcv, 0, 4);
			if( !(Arrays.equals(rcv, (new String("rdy!")).getBytes() ))) {
				throw new RuntimeException( "wrong server greeting: '" + (new String(rcv)) +"'" );
			}
			System.out.printf( "got server greeting\n" );
			OutputStream cos = csock.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( cos );
			
			
			
			System.out.printf( "cl writing\n" );

			oos.writeObject(p);
			System.out.printf( "cl written\n" );
			ObjectInputStream ois = new ObjectInputStream( cis );
			Object ro = ois.readObject();
			
			if( ro instanceof RxData ) {
//				ObjectOutputStream foos = new ObjectOutputStream( new BufferedOutputStream(new FileOutputStream(rxFile)));
//				oos.writeObject(rx);
//				oos.close();
				
				Rx.unpack((RxData) ro, new File( "./"));
			}
			
		} catch(IOException x){
			x.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
