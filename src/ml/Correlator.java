package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Correlator {
	public static void main(String[] args) throws NumberFormatException, IOException {
	
		File infile = new File( args[0] );
		
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
		
		while( (line = r.readLine()) != null) {
			
			
			StringTokenizer st = new StringTokenizer(line);
			
			if( sumx == null ) {
				ncol = st.countTokens() - 2; 
				
				assert( ncol > 0 );
				
				sumx = new double[ncol];
				sumxsqr = new double[ncol];
				sumxy = new double[ncol];
				
				xtmp = new double[ncol];
				
				
			}
			
			
			st.nextToken();
			
			
			
			for( int i = 0; i < ncol; i++ ) {
				xtmp[i] = Double.parseDouble(st.nextToken());
			}
			
			double y = Double.parseDouble( st.nextToken() );
			
			for( int i = 0; i < ncol; i++ ) {
				double x = xtmp[i] / 100.0;
				sumx[i] += x;
				sumxsqr[i] += x * x;
				sumxy[i] += x * y;
			}
			n += 1;
			assert( !st.hasMoreElements() );
			sumy += y;
			sumysqr += y * y;
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
		
		double sdcor;
		{
			double right = 0.0;
			double sum = 0.0;
			double sumsqr = 0.0;
			for( int i = 0; i < ncol; i++ ) {
				sum += cor[i]; 
				sumsqr += cor[i] * cor[i];
			}
			
			sdcor = Math.sqrt((sumsqr - (sum*sum)/ncol) / (ncol - 1));
		}
		System.out.printf( "sd: %f\n", sdcor );
		for( int i = 0 ; i < ncol; i++ ) {
			System.out.printf( "%d: %f %f\n", i, cor[i], cor[i] / sdcor );
		}
		
		
	}
}
