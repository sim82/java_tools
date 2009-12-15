package test;

import java.util.Random;

import ml.Histogram;

public class GaussRnd {
	public static void main(String[] args) {
		Histogram<Integer> h = new Histogram<Integer>();
		
		double[] out = {0,0};
		Random r = new Random();
		
		long time = System.currentTimeMillis();
		for( int i = 0; i < 10000000; i++ ) {
			if( true ) {
//				out[0] = r.nextGaussian();
//				out[1] = r.nextGaussian();
				nextGaussian(out, r);
			} else {
				gauss(out, r);
			}
			
			//System.out.printf( "%f\n", out[0] );
			h.add( Histogram.bin( out[0], -4, 4, 41));
		}
		
		h.printPoser();
		System.out.printf( "time: %d\n", System.currentTimeMillis() - time );
//		h.print();
	}
	
	
	static void gauss2( double[] in, double[] out ) {
		
		
		double r =  Math.sqrt( -Math.log(in[0]));
		
		double i2p =  (2 * Math.PI * in[1]);
		out[0] =  (r * Math.cos(i2p));
		out[1] =  (r * Math.sin(i2p));
		
	}
	
	static void gauss( double[] out, Random rnd ) {
		
		final double v1 = rnd.nextDouble();
		final double v2 = rnd.nextDouble();
		
		
		final double r =  Math.sqrt( -Math.log(v1));
		
		double i2p =  (2 * Math.PI * v2);
		out[0] =  (r * Math.cos(i2p));
		out[1] =  (r * Math.sin(i2p));
		
	}
	
	public static void nextGaussian( double[] out, Random rnd ) {
        // 
        double v1, v2, s;
	    do {
            v1 = 2 * rnd.nextDouble() - 1; // between -1 and 1
        	v2 = 2 * rnd.nextDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2;
	    } while (s >= 1 || s == 0);
	    
	    
	    double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
	    out[0] = v1 * multiplier;
	    out[1] = v2 * multiplier;
	    
    }
}
