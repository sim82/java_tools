package ml;

import java.io.File;
import java.util.Random;

import util.StringTools;

/**
 * Reads a multiple alignment file. Outputs multiple alignment containing
 * randomly distributed sub sequences of the sequences in the input alignment.
 * @author sim
 *
 */
public class SampleDistSubseq {
	public static void main(String[] args) {
		File inFile = new File( args[0] );
		
		
		MultipleAlignment ma = MultipleAlignment.loadPhylip(inFile);
		final int meanLen = 200;
		final int sdLen = 60;
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
		
		MultipleAlignment outMa = new MultipleAlignment(ma.seqLen, outNames, outData );
		outMa.writePhylip(System.out);
		
		
	}
}
