package ml;

import java.io.File;
import java.util.Arrays;

public class MergePhylipFasta {
	public static void main(String[] args) {
		MultipleAlignment p = MultipleAlignment.loadPhylip(new File(args[0]));
		FastaFile f = FastaFile.parse(new File(args[1]));
		
		
		MultipleAlignment ma = new MultipleAlignment(p.nTaxon + f.entries.size(), p.seqLen );
		
		System.arraycopy(p.names, 0, ma.names, 0, p.names.length);
		System.arraycopy(p.data, 0, ma.data, 0, p.data.length );
		
		int next = p.nTaxon;
		
		for( FastaFile.Entry e : f.entries ) {
			ma.names[next] = e.name;
			ma.data[next] = pad( e.data, p.seqLen );
			next++;
		}
		
		ma.writePhylip(System.out);
		
	}

	private static String pad(String data, int seqLen) {
		if( data.length() > seqLen ) {
			System.out.printf( "%d %d\n", seqLen, data.length());
			throw new RuntimeException( "fa file contains longer sequence than phy file");
			
		}
		
		StringBuffer s = new StringBuffer(seqLen);
		s.append(data);
		
		while( s.length() < seqLen ) {
			s.append("-");
		}
		return s.toString();
	}
}
