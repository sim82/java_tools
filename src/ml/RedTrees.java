/*
 * Copyright (C) 2009 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */
package ml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

class Result {
	LN n;
	LN nl;
	LN nr;
}

public class RedTrees {
	public static void main(String[] args) throws FileNotFoundException {
		File infile = new File( args[0] );
		
		File rnfile = new File( ".", infile.getName() + ".rn" );
		
		PrintWriter rfw = new PrintWriter(rnfile);
		
		
		for( int i = 0; ; i++ ) {
			final TreeParser tp = new TreeParser(infile);
			
	
	
			final LN n = tp.parse();
			if( n.data.isTip ) {
				throw new RuntimeException( "pseudo root is a tip. Meh no like." );
			}
			
			Result res = createNThReducedTree(n, i);
			
			if( res == null ) {
				break;
			}
			
			
			try {
				File outfile = new File( ".", infile.getName() + "_" + FindMinSupport.padchar("" + i, 0, 4 ) );
				
				final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
				TreePrinter.printRaw(res.nr, ps);
				ps.close();
			} catch( IOException x ) {
				x.printStackTrace();
				throw new RuntimeException( "bailing out." );
			}
			
			
			// write real-neighbor file entry
			LN[] sbr = {res.nl, res.nr};
			String[] ssl = LN.getSmallerSplitSet(sbr);
						
			
            rfw.println( FindMinSupport.padchar("" + i, 0, 4) + "\t" + res.n.data.getTipName() + "\t" + "*NONE*" + "\t" + FindMinSupport.commaSeparatedList(ssl));
        
			
		}
		
		rfw.close();
	}

	private static Result createNThReducedTree(LN n, int num) {

		LN[] nodeList = LN.getAsList(n);
		int i = 0;
		for( LN node : nodeList ) {
			if( !node.data.isTip ) {
				continue;
			}
			
			if( i == num ) {
				LN tt = node.back;
				
				Result res = new Result();
				res.n = node;
				res.nl = tt.next.back;
				res.nr = tt.next.next.back;
				
	            // remove the current node (and the branch toward the tip) by retwiddling of the other two nodes
				tt.next.back.back = tt.next.next.back;
                tt.next.next.back.back = tt.next.back;
                
                return res;
			} 
			
			i++;
		}
		
		
		return null;
		
	}
}
