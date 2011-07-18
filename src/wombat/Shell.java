package wombat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;



public class Shell {
	Process pr;
	StreamConsumerThread ec;
	BufferedReader stdout;
	OutputStream stdin;
	
	Shell() {
		try {
			pr = Runtime.getRuntime().exec( "sh" );
			
			ec = new StreamConsumerThread(pr.getErrorStream(), null );
			
			stdout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			stdin = pr.getOutputStream();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out." );
		}
	}
	
	
	ArrayList<String> exec( String cmd ) {
		ArrayList<String> ret = new ArrayList<String>();
		
		
		
		try {
			System.currentTimeMillis();
			
			stdin.write(cmd.getBytes());
			stdin.write('\n');
			
			stdin.write( "echo @end\n".getBytes() );
			stdin.flush();
			String line;
			while( (line = stdout.readLine()) != null ) {
				//System.out.printf( "out: '%s' %d\n", line, System.currentTimeMillis() - time1 );
				
				if( line.equals("@end")) {
					break;
				} else {
					ret.add(line);
				} 
			}
			//System.out.printf( "done: %d\n", System.currentTimeMillis() - time1 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out." );
		}
		
		
		
		return ret;
	}
	void exit() {
		
		try {
			exec( "exit" );
			System.out.printf( "exit returned.\n" );
			pr.waitFor();
			ec.stopPlease();
			ec.join();
			
			System.out.printf( "joined.\n" );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		//int[] arr = new int[1024 * 1024 * 256];
		
		Shell sh = new Shell();
		ArrayList<String> out = sh.exec( "ulimit -t 2; /home/sim/a.out" );
		
		for( String l : out ) {
			System.out.printf( "out: '%s'\n", l );
		}
        out = sh.exec( "ls ~/" );
		
		for( String l : out ) {
			System.out.printf( "out2: '%s'\n", l );
		}
		sh.exit();
		
	}


	
}