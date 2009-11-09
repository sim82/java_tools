package wombat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ml.FindMinSupport;


class RunnableSet  {
	Map<Integer, MyRunnable> currentlyRunning = new HashMap<Integer, MyRunnable>();
	
	void add( MyRunnable r ) {
		synchronized (currentlyRunning) {
			currentlyRunning.put(r.getSerial(), r);
		}
	}
	
	void remove( MyRunnable r ) {
		synchronized (currentlyRunning) {
			if( currentlyRunning.remove(r.getSerial()) == null ) {
				System.out.printf( "strange: remove called by non member\n" );
			} 
		}
	}

	void killAll() {
		synchronized (currentlyRunning) {
			for( MyRunnable r: currentlyRunning.values()) {
				System.out.printf( "killing %d\n", r.getSerial() );
				r.killProcess();
			}
		}
	}
}


class StreamConsumerThread extends Thread {
	InputStream is;
	PrintStream outStream;
	boolean doClose = false;
	
	public StreamConsumerThread( InputStream is, File outfile ) {
		this.is = is;
		
		if( outfile != null ) {
			try {
				outStream = new PrintStream(new BufferedOutputStream( new FileOutputStream(outfile)));
				doClose = true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// fall back, kind of ...
				outStream = System.out;
			}
		} else {
			outStream = System.out;
		}
	}
	
	@Override
	public void run() {
		int c = -666;
		
		try {
			
			while( !isInterrupted() &&  (c = is.read()) != -1 ) {
				outStream.print((char)c);
			}
			System.out.printf( "read loop ended: %d\n", c );
			if( doClose ) {
				outStream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.printf( "StreamConsumerThread: bye... %d %s\n", c, isInterrupted() );
	}
	
	public void stopPlease() {
		interrupt();
	}
	
	public void addComment( String comment ) {
		if( outStream != null ) {
			outStream.print( comment );
		}
	}
}

class MyRunnable implements Runnable {
	final private ProcessBuilder pb;
	private Process process;
	private StreamConsumerThread isc;
	private StreamConsumerThread esc;
	
	final int serial;
	private final RunnableSet rs;
	
	public MyRunnable( ProcessBuilder pb, int serial, RunnableSet rs ) {
		this.pb = pb;
		this.serial = serial;
		this.rs = rs;
	}
	
	public int getSerial() {
		return serial;
	}
	
	public void killProcess() {
		isc.stopPlease();
		esc.stopPlease();
		
		process.destroy();
	}
	
	@Override
	public void run() {
		System.out.printf( "running: %d\n", serial );
		try {
			process = pb.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String pserial = "task_" + padchar( "" + serial, 0, 5 );
		
		isc = new StreamConsumerThread(new BufferedInputStream(process.getInputStream()), new File(pserial + ".out"));
		esc = new StreamConsumerThread(new BufferedInputStream(process.getErrorStream()), new File(pserial + ".err"));
		isc.start();
		esc.start();
		//rs.add(this);
		try {
			int ret = process.waitFor();
			System.out.printf( "wait for returned: %d\n", ret );
			
//			isc.stopPlease();
//			esc.stopPlease();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.printf( "got interrupt. killing process of task %d\n", serial );
			killProcess();
			
		}
		//System.out.printf( "what happened?\n" );
		//rs.remove(this);
	} 
	static String padchar(String input_string, int with_digit, int to_len) {
		while (input_string.length() < to_len) {
			input_string = with_digit + input_string;
		}
		return input_string;
	}
}

public class Wombat {
	private ThreadPoolExecutor tpe;
	final private File batchfile;
	//RunnableSet rs = new RunnableSet();
	final Thread cfgThread;
	
	public Wombat(File batchfile, final int N_THREADS ) {
		this.batchfile = batchfile;
		
		tpe = new ThreadPoolExecutor( N_THREADS, N_THREADS, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(N_THREADS * 3));
		tpe.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		
		cfgThread = new Thread() {
			
			public void run() {
				BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
				String line;
				try {
					while( !isInterrupted() && (line = stdin.readLine()) != null ) {
						System.out.printf( "read line '%s'\n", line );
						
						if( line.equals("exit")) {
							System.exit(0);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.printf( "cfg thread finished\n" );
			}
		};
		
		cfgThread.start();
	}

	private void start() {
		
		Runtime.getRuntime().addShutdownHook( new Thread() {
			public void run() {
				synchronized (this) {
					System.out.printf( "enter exit handler\n" );
			
					cfgThread.interrupt();
					
					System.out.printf( "shutdown NOW!\n" );
					tpe.shutdownNow();
					
				//	rs.killAll();
				//	System.out.printf( "kill all\n" );
					
					try {
						System.out.printf( "waiting ...\n" );
						tpe.awaitTermination(1, TimeUnit.DAYS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.printf( "all threads/processes should be terminated now...\n" );
				}
			}
		});
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(batchfile));
	
			String line;
			int serial = 1;
			
			while( ( line = br.readLine()) != null ) {
				if( line.length() > 0 && !line.startsWith("#") ) {
					System.out.printf( "start task: '%s'\n", line );
					
					final ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", line );
					MyRunnable mr = new MyRunnable(pb, serial, null);
					
					tpe.execute(mr);
					serial++;
				}
				
			}
			
			System.out.printf( "done. waiting for shutdown...\n" );
			tpe.shutdown();
			try {
			
				tpe.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.printf( "exit!\n" );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		
		File batchfile;
		
		if( args.length > 0 ) {
			batchfile = new File( args[0] );
		} else {
			batchfile = new File( "/home/sim/test.wb");
		}
		
		final int N_THREADS;
		if( args.length > 1 ) {
			N_THREADS = Integer.parseInt(args[1]);
		} else {
			N_THREADS = 2;
		}
		
		Wombat wb = new Wombat( batchfile, N_THREADS );
		wb.start();
		
		
		
		System.exit(0);
		
		final ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "/home/sim/busy");
		pb.environment().put( "BLA", "XTC");
		
		MyRunnable mr = new MyRunnable(pb, 666, null);
		Thread t = new Thread( mr );
		
		t.start();
		System.out.printf( "go to sleep\n" );
		long time = System.currentTimeMillis();
		long wakeetime = time + 10000;
		
		while( (time = System.currentTimeMillis()) < wakeetime ) {
			Thread.currentThread().sleep(wakeetime - time);
			System.out.printf( "yaaawn\n" );
		}
		
		System.out.printf( "wakeee up!\n" );
		mr.killProcess();
		
		t.join();
	
		
	}

	
}
