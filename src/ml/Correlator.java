package ml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class Correlator {
	public static void main(String[] args) throws NumberFormatException, IOException {
	
		File infile = new File( args[0] );
		
		File xdir = null;
		BufferedWriter[] xfiles = null;
		if( args.length > 1 ) {
			xdir = new File( args[1] );
			xdir.mkdir();
			assert( xdir.isDirectory() );
		}
		
		BufferedReader r = new BufferedReader(new FileReader(infile));
		String line = null;
		
		double[] sumx = null;
		double sumy = 0.0;

		double[] sumxsqr = null;
		double sumysqr = 0.0;
		int n = 0;

		double[] sumxy = null;
		int ncol = 0;
		
		double[] xtmp = null;
		double[] xtmp2 = null;
		
		boolean multi = !true;
		
		int nlines = 0;
		
		while( (line = r.readLine()) != null) {
			
			
			StringTokenizer st = new StringTokenizer(line);
			
			if( sumx == null ) {
				
				ncol = st.countTokens() - 2; 

				assert( ncol > 0 );
				
				
				if( multi ) {
					xtmp2 = new double[ncol];
					ncol = ncol * ncol;
					
				}
				
				sumx = new double[ncol];
				sumxsqr = new double[ncol];
				sumxy = new double[ncol];
				
				xtmp = new double[ncol];
				
				if( xdir != null && !multi) {
					xfiles = new BufferedWriter[ncol];
					
					for( int i = 0; i < xfiles.length; i++ ) {
						xfiles[i] = new BufferedWriter(new FileWriter(new File( xdir, FindMinSupport.padchar("" + i, 0, 4))));
					}
				}
			}
			
			
			st.nextToken();
			
			
			if( !multi ) {
				for( int i = 0; i < ncol; i++ ) {
					xtmp[i] = Double.parseDouble(st.nextToken()) / 100.0;
				}
			} else {
				for( int i = 0; i < xtmp2.length; i++ ) {
					xtmp2[i] = Double.parseDouble(st.nextToken()) / 100.0;
				} 
				int l = 0;
				for( int i = 0; i < xtmp2.length; i++ ) {
					for( int j = 0; j < xtmp2.length; j++ ) {
						xtmp[l] = xtmp2[i] * xtmp2[j];
						l++;
					}
				}
				
				
			}
			double y = Double.parseDouble( st.nextToken() );
			
			if( xfiles != null ) {
				for( int i = 0; i < ncol; i++ ) {
					xfiles[i].write( "" + (int)xtmp[i] + " " + y  + "\n");
				}
			}
			
			for( int i = 0; i < ncol; i++ ) {
				double x = xtmp[i];
				sumx[i] += x;
				sumxsqr[i] += x * x;
				sumxy[i] += x * y;
			}
			n += 1;
			assert( !st.hasMoreElements() );
			sumy += y;
			sumysqr += y * y;
		
			
//			if( nlines % 10000 == 0 ) {
//				System.out.printf( "%d\n", nlines );
//			}
//			
//			nlines++;
			if( n == 100000 ) {
				break;
			}
		}

//		#  list_squared = list.map {|item| item*item }  
//		#   n = list.size  
//		#   
//		#   #Calculate the std deviation  
//		#   right = (Float (sum(list)**2))/n  
//		#   
//		#   ((Float (sum(list_squared)) - right) / (n-1)) ** 0.5  
//		
		
		
		double[] cor = new double[ncol];
		for( int i = 0; i < ncol; i++ ) {
			double left = n * sumxy[i] - sumx[i] * sumy;
			double right = Math.sqrt((n * sumxsqr[i] - sumx[i]*sumx[i]) * (n * sumysqr - sumy*sumy) );
			cor[i] = left / right;
			//
		}
		
		double mean = 0;
		double sdcor;
		{
			double right = 0.0;
			double sum = 0.0;
			double sumsqr = 0.0;
			for( int i = 0; i < ncol; i++ ) {
				sum += cor[i]; 
				sumsqr += cor[i] * cor[i];
			}
			mean = sum / ncol;
			sdcor = Math.sqrt((sumsqr - (sum*sum)/ncol) / (ncol - 1));
		}
		System.out.printf( "sd: %f\n", sdcor );
		System.out.printf( "mean: %f\n", mean );
		
		if( !multi ) {
			for( int i = 0 ; i < ncol; i++ ) {
				System.out.printf( "%d: %f %f\n", i, cor[i], (cor[i]-mean) / sdcor );
			}
		} else {
			int l = 0;
			for( int i = 0; i < xtmp2.length; i++ ) {
				for( int j = 0; j < xtmp2.length; j++ ) {
					System.out.printf( "%d %d: %f %f\n", i, j, cor[l], (cor[l]-mean) / sdcor );
					l++;
				}
			}
		}
		if( xfiles != null ) {
			for( BufferedWriter s : xfiles ) {
				s.close();
			}
		}
	}
}
