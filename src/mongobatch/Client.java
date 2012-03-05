/*
 * 
 */
package mongobatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws InterruptedException {
	int serverport = Integer.parseInt((args[0]));

	try {
	    Socket sock = new Socket("localhost", serverport);

	    OutputStream os = sock.getOutputStream();

	    PrintStream ps = new PrintStream(os);

	    ps.print(">hello<");

	    Thread.sleep(500);

	    ps.print(">bla bla<");

	    byte[] bigbuf = new byte[1024 * 1024];
	    ps.write(bigbuf);
	    ps.print(">bla bla2<");

	    InputStream is = sock.getInputStream();
	    int c;
	    StringBuffer recbuf = new StringBuffer();
	    while ((c = is.read()) >= 0) {
		recbuf.append((char) c);

		if (c == '<') {
		    break;
		}
	    }
	    System.out.printf("server said: '%s'\n", recbuf.toString());

	    Thread.sleep(5000);
	    ps.print(">bye<");
	    sock.close();

	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
}
