package ml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import wombat.StreamConsumerThread;

import com.sun.imageio.plugins.common.InputStreamAdapter;



public class ExecTest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
	
		
		if( true ) {
			return;
		}
		
		int[] arr = new int[1024 * 1024 * 256];
		
		System.gc();
		System.gc();
		System.gc();
		//Thread.currentThread().sleep(5000);
		
		
		Process pr = Runtime.getRuntime().exec( "sh" );
		
		
		long time1 = System.currentTimeMillis();
		
		OutputStream os = pr.getOutputStream();
		OutputStreamWriter op = new OutputStreamWriter(os);
		op.write("ls /home/sim/test\n");
		op.flush();
		
		op.write("echo bla\n");
		op.flush();
		
		op.write("exit\n");
		op.flush();
		
		BufferedReader ir = new BufferedReader( new InputStreamReader(pr.getInputStream()));
		StreamConsumerThread ec = new StreamConsumerThread(pr.getErrorStream(), null);
		ec.start();
		
		String line;
		while( (line = ir.readLine()) != null ) {
			System.out.printf( "out: '%s' %d\n", line, System.currentTimeMillis() - time1 );
		}
		
		pr.waitFor();
		ec.join();
		long time2 = System.currentTimeMillis();
		
		System.out.printf( "time: %d %d\n", time2 - time1, arr[1] );
		
	}
}
