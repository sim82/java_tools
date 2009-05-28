package ml;

import java.io.File;

public class AlignmentChopper {
	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(new File( args[0] ));
		
		int len = ma.seqLen;
		int maxLen = len / 2;
		
		for( String name : ma.names ) {
			char[] seq = ma.getSequence(name).toCharArray();
			
			char[] newseq = new char[seq.length];
			int n = 0;
			for( int i = 0; i < seq.length; i++ ) {
				char c = seq[i];
				if( !isGapCharacter(c) ) {
					if( n > maxLen ) {
						c = '-';
					}
					
					n++;
				}
				
				newseq[i] = c;
			}
			
			ma.replaceSequence(name, new String(newseq));
		}
		
		ma.writePhylip(System.out);
		
	}
	
	
	private static boolean isGapCharacter(char c) {
		return c == '-' || c == 'N' || c == '?' || c == 'O' || c == 'X';
	}
}
