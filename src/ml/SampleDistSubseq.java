package ml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import util.StringTools;

/**
 * Reads a multiple alignment file. Outputs multiple alignment containing
 * randomly distributed sub sequences of the sequences in the input alignment.
 * @author sim
 *
 */
public class SampleDistSubseq {
	static Random rnd = new Random(12234);
	
	static interface error_model {
		String mutate( String in );
	
	}
	
	static class no_error implements error_model {
		public String mutate( String in ) {
			return in;
		}
	}
	
	static class balzer2010 implements error_model {
		// my interpretation of the (Balzer et al. 2010) "model" of 454 read errors.
		// I think this is correct rather than the N(n, 0.03494 + n * 0.06856) used by grinder,
		// which Balzer et al. only give as an estimation for homopolymers of lengts of 6 and longer.
		// For shorter ones they report exact values.
		static double mean( int hp_len ) {
			double[] fixed = { 0.1239, 1.0193, 2.0006, 2.9934, 3.9962, 4.9550 };
			
			if( hp_len < fixed.length ) {
				return fixed[hp_len];
			} else {
				return hp_len;
			}
		}
		
		
		static double stddev( int hp_len ) {
			double[] fixed = {0.0737, 0.1227, 0.1585, 0.2188, 0.3168, 0.3863};
			if( hp_len < fixed.length ) {
				return fixed[hp_len];
			} else {
				return 0.03494 + hp_len * 0.06856;
			}
			
		
		}
		
		
		public String mutate( String in ) {
			int last_change = 0;
			
			StringBuilder sb = new StringBuilder((int) (in.length() * 1.2));
			char last_char = in.charAt(0);
			for( int i = 1; i <= in.length(); i++ ) {
				char c;
				if( i < in.length()) {
					c = in.charAt(i);
				} else {
					c = 0;
				}
				
				if( c != last_char ) {
					int stretch_len = i - last_change;
					
					int hp_len = stretch_len; // this is not really clear: what is the 'homopolymer length' in the paper? (what does length 0 mean?)
					//double new_len = mean(hp_len) + rnd.nextDouble() * stddev(hp_len);
					
					double new_len = mean(hp_len) + rnd.nextGaussian() * stddev(hp_len);
					int new_len_int = (int)Math.round(new_len);
					
					for( int j = 0; j < new_len_int; j++ ) {
						sb.append(last_char);
					}
					
			//		new_len = mean
					last_change = i;
					
				}
				last_char = c;
			}
			
			return sb.toString();
		}
		
		
	}
	
	public static void main(String[] args) {
		File inFile = new File( args[0] );
		
		
		MultipleAlignment ma = MultipleAlignment.loadPhylip(inFile);
		final int meanLen;
		if( args.length > 1 ) {
			meanLen = Integer.parseInt(args[1]);
		} else {
			meanLen = 200;
		}	
		final int sdLen;
		if( args.length > 2 ) {
			sdLen = Integer.parseInt(args[2]);
		} else {
			sdLen = 60;
		}
		
		
		HashSet<String> argset = new HashSet<String>(Arrays.asList(args));
		
		boolean balzer = argset.contains("-balzer" );
		
		final error_model em;
		
		
		
		if( balzer ) {
			System.err.printf( "Balzer et al. 2010 454 error model\n" );
			em = new balzer2010();
		} else {
			em = new no_error();
			
		}
		//error_model em = new no_error();
		
		final boolean writeFasta;
		final boolean fastaKeepGaps;
		if( args.length > 3 ) {
			if( args[3].equals("-fasta")) {
				writeFasta = true;
				fastaKeepGaps = false;
			} else if( args[3].equals("-fastag")) {
				writeFasta = true;
				fastaKeepGaps = true;
			} else {
				writeFasta = false;
				fastaKeepGaps = false;
			
			}
		} else {
			writeFasta = false;
			fastaKeepGaps = false;
		}
		
		
			
		

		
		final int nSamples = 20;
		final int minLen = 20;
		
		String[] outNames = new String[nSamples * ma.nTaxon];
		String[] outData = new String[nSamples * ma.nTaxon];
		int outPtr = 0;
		
		Random rand = new Random(12345);
		
		for( int i = 0; i < ma.nTaxon; i++ ) {
			String name = ma.names[i];
			String data = ma.data[i];
			
			
			for( int j = 0; j < nSamples; j++ ) {
				double gr = rand.nextGaussian();
				final int len = Math.max( minLen, (int) Math.round(gr * sdLen + meanLen)); 
			
				outNames[outPtr] = name;
				if( nSamples > 1 ) {
					outNames[outPtr] += "_" + FindMinSupport.padchar( "" + j, 0, 2 );
				}
				outData[outPtr] = FindMinSupport.createRandomSubseqClamped(data, len);
				
				outPtr++;
			}
		}
		
		if (!writeFasta) {
			MultipleAlignment outMa = new MultipleAlignment(ma.seqLen, outNames, outData );
			outMa.writePhylip(System.out);
		} else {
			// this is all pretty stupid, but hey, this is java which was invented for inefficient shit ...
			
			for( int i = 0; i < outNames.length; i++ ) {
				StringBuilder sb = new StringBuilder(ma.seqLen);	
				System.out.println( ">" + outNames[i] );
				
				for( int j = 0; j < outData[i].length(); j++ ) {
					char c = outData[i].charAt(j);
					if( fastaKeepGaps || !FindMinSupport.isGapCharacter(c)) {
						//System.out.write( c );
						sb.append(c);
					}
				}
				
				System.out.println( em.mutate(sb.toString()) );
				//System.out.write( '\n' );
			}
		
		}
		
	}
}
