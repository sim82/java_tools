package wombat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;



public class MyRunnable implements Runnable {
	final private ProcessBuilder pb;
	private Process process;
	private StreamConsumerThread isc;
	private StreamConsumerThread esc;
	
	final int serial;
	private final RunnableSet rs;
	private long startTime;
	
	public MyRunnable( ProcessBuilder pb, int serial, RunnableSet rs ) {
		this.pb = pb;
		this.serial = serial;
		this.rs = rs;
	}
	
	public int getSerial() {
		return serial;
	}
	
	public String getCommand() {
		String cmd = null;
		for( String s : pb.command() ) {
			if( cmd == null ) {
				cmd = s;
			} else {
				cmd += " " + s;
			}
		}
		
		return cmd;
	}
	
	public void killProcess() {
//		isc.stopPlease();
//		esc.stopPlease();
		
//		process.destroy();
	}
	
	@Override
	public void run() {
		System.out.printf( "running: %d\n", serial );
		startTime = System.currentTimeMillis();
		
		try {
			process = pb.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String pserial = "task_" + padchar( "" + serial, 0, 5 );
		
		isc = new StreamConsumerThread(process.getInputStream(), new File(pserial + ".out"));
		esc = new StreamConsumerThread(process.getErrorStream(), new File(pserial + ".err"));
		
		String fc = getCommand();
		isc.addComment( "stdout of task:\n" + fc + "\n" );
		esc.addComment( "stderr of task:\n" + fc + "\n" );
		
		
		isc.start();
		esc.start();
		rs.add(this);
		try {
			int ret = process.waitFor();
//			System.out.printf( "wait for returned: %d\n", ret );
			
//			isc.stopPlease();
//			esc.stopPlease();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.printf( "got interrupt. killing process of task %d\n", serial );
//			killProcess();
			process.destroy();
			
		//	process.
		}
		
		isc.interrupt();
		esc.interrupt();
		
		try {
			process.getInputStream().close();
			
		} catch (IOException e) {
		
		}
		try {
			process.getOutputStream().close();
		} catch (IOException e) {
		
		}
		try {
			process.getErrorStream().close();
		} catch (IOException e) {
		
		}
		
		
//		System.out.printf( "waiting for reader threads: %d\n", serial );
//		try {
//			isc.join();
//			esc.join();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		
		//System.out.printf( "what happened?\n" );
		rs.remove(this);
		
		System.out.printf( "MyRunner exiting: %d\n", serial ); 
		process = null;
	} 
	static String padchar(String input_string, int with_digit, int to_len) {
		while (input_string.length() < to_len) {
			input_string = with_digit + input_string;
		}
		return input_string;
	}

	public long startTime() {
		return startTime;
	}
}
