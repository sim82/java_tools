package ml;

public class PhylipCutRight {
	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(System.in);
		int ncut = Integer.parseInt(args[0]);
		
		ma.seqLen -= ncut;
		
		for( int i = 0; i < ma.data.length; i++ ) {
			ma.data[i] = ma.data[i].substring(0, ma.data[i].length() - ncut );
		}
		
		ma.writePhylip(System.out);
	}
}
