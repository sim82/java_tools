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

import java.io.PrintStream;

/**
 *
 * @author sim
 */
public class TreePrinter {
	static boolean doIndent;

	static void print( LN tree, boolean indent ) {
		doIndent = indent;

		print( tree, 0 );
	}

	private static void print( LN node, int level ) {

		if( doIndent ) {
			for (int i = 0; i < level; i++) {
				System.out.print(" ");
			}
		}

		if( node.data.isTip ) {
			System.out.printf("leaf: %s\n", node.data.getTipName());
		} else {
			System.out.printf("inner node: %s\n", node.data);
			print(node.next.back, level + 1);
			print(node.next.next.back, level + 1);

			if( level == 0 ) {
				print( node.back, level + 1 );
			}
		}
	}

    static void printRaw( LN node, PrintStream s, boolean root) {
        if( node.data.isTip ) {
			//s.printf("%s:%G", node.data.getTipName(), node.backLen);
            s.printf("%s:%8.20f", node.data.getTipName(), node.backLen);
		} else {
			s.print("(");
			printRaw(node.next.back, s ,false);
			s.print(",");
            printRaw(node.next.next.back, s ,false);

			if( root ) {
                s.print(",");
				printRaw( node.back, s ,false);
                s.printf(");" );
			} else {
                //s.printf("):%G", node.backLen );
                s.printf("):%8.20f", node.backLen );
            }
		}
    }
    static void printRaw( LN node, PrintStream s ) {
        //node = LN.getTowardsTree(node);
    	if( node.data.isTip ) {
    		if( node.back != null ) {
    			node = node.back;
    		} else if( node.next.back != null ) {
    			node = node.next.back;
    		} else if( node.next.next.back != null ) {
    			node = node.next.next.back;
    		} else {
    			throw new RuntimeException( "can not print single unlinked node");
    		}
    		
    		if( node.data.isTip ) {
    			throw new RuntimeException( "could not find non-tip node for writing the three (this is a braindead limitation of this tree printer!)");
    		}
    	}
    	printRaw( node, s, true );
    }
}