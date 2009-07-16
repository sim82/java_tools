package ml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubseqGen {
	
	final static int N_RND_SEQS = 100;
	
	public static void main(String[] args) {
		if( false )
		{
			int[] x = drawNWithoutReplacement(1, 1);
			System.out.printf( "size: %d\n", x.length );
			for( int y : x ) {
				System.out.printf( "%d\n", y );
				
			}
			System.exit(0);
		}
		
		
		File treeFile = new File( args[0] );
		File alignFile = new File( args[1] );
		
		
		MultipleAlignment ma = MultipleAlignment.loadPhylip(alignFile);
		LN tree = TreeParser.parse(treeFile);
		
		PrintStream rns = null;
		
		PrintStream sms = null;
		
		try {
			if( true ) {
				
				rns = new PrintStream(new FileOutputStream(alignFile.getName() + "_real_neighbors.txt"));
			}
			
			if( true ) {
				sms = new PrintStream(new FileOutputStream(alignFile.getName() + "_smap.txt"));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException( "bailing out" );
		}
		int starti;
		if( args.length > 2 ) {
			starti = Integer.parseInt(args[2]);
		} else {
			starti = 0;
		}
		
		for( int i = starti;; i++ ) {
			LN clonedTree = LN.deepClone(tree);
			
			ReductionResult res = createNThReducedTree(clonedTree, i);
			
			if( res == null ) {
				break;
			}
			
			System.out.printf( "red tree %d\n", i );
			

			if( rns != null ) {
				final LN[] ll = LN.getAsList(res.nl, false);
				final LN[] lr = LN.getAsList(res.nr, false);

				final Set<String> sl = LN.getTipSet(ll);
				final Set<String> sr = LN.getTipSet(lr);

				final Set<String> smallset = (sl.size() <= sr.size()) ? sl : sr;

				String[] ssl = new String[smallset.size()];
				ssl = smallset.toArray(ssl);
				Arrays.sort(ssl);
							
				
				rns.printf( "%s\t%s\t%s\t%s\n", FindMinSupport.padchar("" + i, 0, 4 ), res.taxon, res.taxonNeighbor != null ? res.taxonNeighbor : "*NONE*", FindMinSupport.commaSeparatedList(ssl));
			}
		
			if( sms != null ) {
				String seq = ma.getSequence(res.taxon);
				
				
				final int[] nm = FindMinSupport.getNonGapCharacterMap(seq);
					
				sms.print( res.taxon + "\t" );
				for( int j = 0; j < nm.length; j++ ) {
					sms.printf( "%d ", nm[j] );
				}
				sms.println();
			}
			
			if( true ) {
				try {
					final File outfile = new File(alignFile.getName() + "_" + FindMinSupport.padchar("" + i, 0, 4) + ".tree");
	
					final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
					TreePrinter.printRaw(res.nl, ps);
					ps.close();
				} catch (final FileNotFoundException ex) {
					Logger.getLogger(FindMinSupport.class.getName()).log(Level.SEVERE, null, ex);
				}
			
			}			
			
				
			if( true ) {
				MultipleAlignment clonedMa = ma.deepClone( N_RND_SEQS );
				randomSubseqs( res.taxon, clonedMa, N_RND_SEQS, 250 );
				
				String alignName = alignFile.getName() + "_" + FindMinSupport.padchar("" + i, 0, 4);
	
				
				clonedMa.writePhylip(new File( alignName + ".gz"));
			}
		}
		
		if( rns != null ) {
			rns.close();
		}
		
		if( sms != null ) {
			sms.close();
		}
	}
	
	
	private static void randomSubseqs(String taxon, MultipleAlignment ma, int n, int seqLen ) {
		
		String seq = ma.getSequence(taxon);
		
		
		final int[] nm = FindMinSupport.getNonGapCharacterMap(seq);
		
		if( nm.length < seqLen ) {
			seqLen = nm.length;
		}
		
		n = Math.min( n, nm.length - seqLen + 1);
		int[] rnd = drawNWithoutReplacement(n, nm.length - seqLen + 1);
		
		for( int r : rnd ) {
			char[] newseq = new char[seq.length()];
			Arrays.fill(newseq, '-');
			
			for( int i = 0; i < seqLen; i++ ) {
				int nmi = nm[r+i];
				newseq[nmi] = seq.charAt(nmi);  
			}
			
			String newname = taxon + "@" + r;
			ma.append(newname, new String(newseq));
		}
		
		
	}


	private static int[] drawNWithoutReplacement(int n, int m) {
		if( n > m ) {
			throw new RuntimeException( "trying to draw " + n + " from " + m + " elements. How?");
		}
		
		
		int[] narray = new int[n];
		int[] marray = new int[m];
		
		for( int i = 0; i < m; i++ ) {
			marray[i] = i;
		}
		
		Random rnd = new Random();
	
		for( int i = 0; i < n; i++ ) {
			int mdraw = rnd.nextInt( m - i );
			narray[i] = marray[mdraw];
			
			marray[mdraw] = marray[m-i-1];
		}
		
		return narray;
	}


	static class ReductionResult {

		String taxon;
		String taxonNeighbor;

		LN nl;
		LN nr;
	}
	
	public static ReductionResult createNThReducedTree(LN n, int num) {
		final LN[] nodelist = LN.getAsList(n);

		//System.out.printf("nodes: %d\n", nodelist.length);

		int nTT = 0;
		int i = 0;

        //if( true ) {
		

        for (final LN node : nodelist) {
        	if( node.back != null && node.data.isTip ) {
        		if( num == i ) {
        			final ReductionResult ret = new ReductionResult();
					
        			LN tt = node.back;
        			
        			ret.taxon = node.data.getTipName();
					ret.taxonNeighbor = null;
					ret.nl = tt.next.back;
					ret.nr = tt.next.next.back;

                    // remove the current node (and the branch toward the tip) by retwiddling of the other two nodes
                    tt.next.back.back = tt.next.next.back;
                    tt.next.next.back.back = tt.next.back;

                    return ret;
        		}
        		
        		i++;
        	}
        }
        	
        			
		
		return null;
	}
	
}
