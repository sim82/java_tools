package wombat.pack2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RxInfo {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		File rxFile = new File( args[0] );
		
		ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream(new FileInputStream(rxFile)));
		
		
		RxData rx = (RxData) ois.readObject();
		ois.close();
		
		
		for( RxData.RxFile rxf : rx.rxfs ) {
			System.out.printf( "%s %d\n", rxf.localName, rxf.data.length );
		}
		
	}
}
