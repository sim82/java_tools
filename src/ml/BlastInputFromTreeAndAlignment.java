package ml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class BlastInputFromTreeAndAlignment {
	public static void main(String[] args) {
		File treefile = new File( args[0] );
		File mafile = new File( args[1] );
		
		File outdir_query = new File( "query");
		File outdir_ma = new File( "blast");
		
		outdir_query.mkdir();
		outdir_ma.mkdir();
		
		
		LN tree = LN.parseTree(treefile);
		MultipleAlignment ma = MultipleAlignment.loadPhylip(mafile);
		
		LN[] list = LN.getAsList(tree);
		
		Set<String> tipSet = LN.getTipSet(list);
		
		File outfafile = new File( outdir_ma, mafile.getName());
		
		int qi = 0;
		try {
			PrintWriter faw = new PrintWriter( new FileWriter( outfafile ));
			for( String taxon : ma.names ) {
				if( tipSet.contains(taxon)) {
					faw.printf( "> %s\n", taxon);
					faw.println( FindMinSupport.removeGaps(ma.getSequence(taxon)));
				} else {
					String qip = FindMinSupport.padchar(""+qi, '0', 5);
					
					File qfile = new File( outdir_query, qip);
					
					PrintWriter qw = new PrintWriter( new FileWriter(qfile));
					qw.printf( "> %s\n", taxon );
					qw.println( FindMinSupport.removeGaps(ma.getSequence(taxon)));
					qw.close();
					qi++;
					
				}
			}
			faw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out." );
		}
		
		
	}
}
