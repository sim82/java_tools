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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;



public class TreeParserMulti {

	//String input;

	// input as char array
	private byte[] inputA;

	// pointer to next char in input string
	private int ptr = 0;

	private int nLeafs = 0;
	private int nInnerNodes = 0;

	public static LN parse( File f ) {
		TreeParserMulti tp = new TreeParserMulti(f);
		return tp.parse();
	}
	static boolean QUIET = false;
//	public TreeParser(String input) {
//	//	this.input = input;
//		this.inputA = input.toCharArray();
//		ptr = 0;
//	}

	public TreeParserMulti(File f) {

		this.inputA = readFile(f);
      //  this.input = new String(inputA);
        ptr = 0;
	}

	private static byte[] readFile(File f) {

		try {

            //BufferedReader r = new BufferedReader(new FileReader(f));
			
			
            InputStream r;
            long len;
            if( f.getName().endsWith(".gz")) {
            	
            	// this is a very ugly way to get the size of the uncompressed file, but it's not worth
            	// thinking about a better solution atm. There should be a call for this in GZIPStream)
            	GZIPInputStream gzis = new GZIPInputStream( new FileInputStream(f) );
            	
            	len = 0;
            	
            	long skipped;
            	while( (skipped = gzis.skip(1024 * 1024)) != 0 ) {
            		len+=skipped;
            	}
            	
            	
            	gzis.close();
            	
            	r = new GZIPInputStream( new FileInputStream(f) );
            } else {
            	r = new FileInputStream(f);
            	len = f.length();
            }
//			String line = null;
//
//            String cont = "";
//            int i = 0;
//            while( ( line = r.readLine()) != null ) {
//                cont += line;
//                System.out.printf("line %d\n", i);
//                i++;
//            }
//            return cont;
             
            byte[] data = new byte[(int)len];

            r.read(data);
            r.close();
            return data;

		} catch (FileNotFoundException ex) {

			Logger.getLogger(TreeParserMulti.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (IOException ex) {
			Logger.getLogger(TreeParserMulti.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public void printLocation() {
		if( QUIET ) {
			return;
		}
		
		int pos1 = Math.max(0, ptr - 40);
		int pos2 = Math.min(inputA.length, ptr + 40);

		System.out.println(substring(pos1, pos2));

		for (int i = pos1; i < ptr; i++) {
			System.out.print(" ");
		}
		System.out.println("^");
	}

	private void skipWhitespace() {
		while ( ptr < inputA.length && Character.isWhitespace(inputA[ptr])) {// || inputA[ptr] == '\n' || inputA[ptr] == '\r') ) {
           // System.out.printf( "skip ws: %d\n", (int) inputA[ptr]);
			ptr++;
		}

	}

	/**
	 * Call this after object construction to parse the complete tree,
	 * @return pseudo root of the tree
	 */
	
	public LN parse() {
		nLeafs = 0;
        nInnerNodes = 0;

        skipWhitespace();

        if( ptr >= inputA.length ) {
            // seems like we hit the end of the file
            return null;
        }
		// expect at least one node
		LN node = parseNode();

		// expect terminating ';'
		if (ptr >= inputA.length) {
			throw new RuntimeException("parse error. parse: end of input. missing ';'");
		}

		// branch length might be present for historical reasons 
		if( inputA[ptr] == ':' ) {
			parseBranchLength();
		}
		
		if( node.back == null ) {
			
//			System.out.printf( "here:\n" );
			LN nx = node.next;
			LN ny = nx;
			while( ny.next != node ) {
				ny = ny.next;
			}
			ny.next = nx;
			node = nx;
        }
		
		
		
		if (inputA[ptr] != ';') {
			printLocation();
			throw new RuntimeException("parse error. parse expects ';'");
		}
        // consume terminating ;
        ptr++;
		return node;
	}

    private String parseBranchLabel() {
        if( inputA[ptr] == '[' ) {
            int lstart = ptr;
            ptr++;
            

            int lend = findNext(ptr, ']' );

            ptr = lend+1;

            // labels have the form [blablabla], so the label content starts at lstart + 1

            if( lend - (lstart+1) <= 0 ) {
                printLocation();
                throw new RuntimeException("bad branch label: " + substring(lstart, ptr) );
            }

            return substring(lstart + 1, lend);
            

        } else {
            return null;
        }


    }

	private LN parseNode() {
		skipWhitespace();

		// lookahead: determine node type
		if (inputA[ptr] == '(') {
			return parseInnerNode();
		} else {
			return parseLeaf();
		}
	}

	private double parseBranchLength() {
		skipWhitespace();

		// expect + consume ':'
		if (inputA[ptr] != ':') {
			//throw new RuntimeException("parse error: parseBranchLength expects ':' at " + ptr);
			return -1;
		} else {
	
			ptr++;
	
			skipWhitespace();
	
			int lend = findFloat(ptr);
			if (lend == ptr) {
				throw new RuntimeException("missing float number at " + ptr);
			}
	
			double l = Double.parseDouble(substring(ptr, lend));
			ptr = lend;
	
			return l;
		}
	}
    private String substring( int from, int to ) {
        return new String( Arrays.copyOfRange( inputA, from, to));
    }
    
    public static LN createLN( int num ) {
		ANode data = new ANode();
		
		LN n = new LN(data);
		LN nx = n;
		for( int i = 1; i < num; ++i ) {
			nx.next = new LN(data);
			nx = nx.next;
		}
		nx.next = n;
		return n;
	}
	private LN parseInnerNode() {
		skipWhitespace();


		// expect + consume '('
		if (inputA[ptr] != '(') {
			throw new RuntimeException("parse error: parseInnerNode expects '(' at " + ptr);
		}
		ptr++;

		ArrayList<LN> subtrees = new ArrayList<LN>();

		
		// parse first node + branch length
		{
			LN nl = parseNode();
			double l1 = parseBranchLength();
			String label1 = parseBranchLabel();
			
			nl.backLen = l1;
			nl.backLabel = label1;
			subtrees.add(nl);
			
		}
    	
		nInnerNodes++;


		double support = -1;
		String nodeLabel = null;
		
        while( true ) {
	
			skipWhitespace();
			
			if (inputA[ptr] == ')') {
				// 'normal' inner node: two childs
				ptr++;
	
	        	// the stuff between the closing ')' and the ':' of the branch length (or a ',' or the terminating ';')
	        	// is interpreted as node-label. If the node label corresponds to a float value
	        	// it is interpreted as branch support (or node support as a rooted-trees-only-please biologist would say)
	            int lend = findEndOfBranch(ptr);
	        	
	        	nodeLabel = substring(ptr, lend);
	        	ptr = lend;
	            	
	            	
	        	boolean isDigit = nodeLabel.length() != 0;
	        	for( int i = 0; i < nodeLabel.length(); i++ ) {
	        		isDigit &= Character.isDigit(nodeLabel.charAt(i));
	        		
	        		if( i == 0 ) {
	        			isDigit &= (nodeLabel.charAt(i) != '0');
	        		}
	        	}
	        	
	        	if( isDigit ) {
	        		try {
	        			support = Double.parseDouble(nodeLabel);
	        		} catch( NumberFormatException e ) {
	        			printLocation();
	        			throw e;
	        		}
	        	} else {
	        		
	        		support = -1;
	        	}
	        	
	
//				LN n = LN.create();
//	            n.data.setSupport(support);
//	            n.data.setNodeLabel(nodeLabel);
//	            
//				twiddle( nl, n.next, l1, label1, nl.data.getSupport() );
//				twiddle( nr, n.next.next, l2, label2, nr.data.getSupport() );
//	
	
	        	break;
			} else if( inputA[ptr] == ',') {
				// second comma found: three child nodes == pseudo root
				ptr++;
	
				LN nx = parseNode();
	
				double l3 = parseBranchLength();
	            String label3 = parseBranchLabel();
	         //   System.out.printf( "l3: %s\n", nx.data.getTipName() );
				skipWhitespace();
	
				subtrees.add( nx );
				nx.backLen = l3;
				nx.backLabel = label3;
//				LN n = LN.create();
//	
//				twiddle( nl, n.next, l1, label1, nl.data.getSupport() );
//				twiddle( nr, n.next.next, l2, label2, nr.data.getSupport() );
//				twiddle( nx, n, l3, label3, nx.data.getSupport() );
//				
	//			System.out.printf( "root: %f %f %f\n", nl.data.getSupport(), nr.data.getSupport(), nx.data.getSupport() );
	//			System.exit(0);
				
			} else {
				printLocation();
				throw new RuntimeException("parse error: parseInnerNode expects ')'or ',' at " + ptr);
			}
        }
        
        
        LN n = createLN( subtrees.size() + 1);
        
        LN nx = n.next;
        
        
        
        for( int i = 0; i < subtrees.size(); ++i ) {
        	nx.back = subtrees.get(i);
            nx.backLen = nx.back.backLen;
            nx.backLabel = nx.back.backLabel;
            nx = nx.next;
        }
        
//        System.out.printf( "num: %d\n", subtrees.size());
        return n;

	}
	
	/**
	 * create an edge (=double link) between the two nodes and set branch length
	 * 
	 * @param n1
	 * @param n2
	 * @param branchLen
	 */
	private void twiddle( LN n1, LN n2, double branchLen, String branchLabel, double support ) {
		if( n1.back != null ) {
			printLocation();
			throw new RuntimeException( "n1.back != null" );
		}

		if( n2.back != null ) {
			printLocation();
			throw new RuntimeException( "n2.back != null" );
		}

		n1.back = n2;
		n2.back = n1;

        n1.backLen = branchLen;
        n2.backLen = branchLen;
        n1.backLabel = branchLabel;
        n2.backLabel = branchLabel;
        n1.backSupport = support;
        n2.backSupport = support;

	}


	private LN parseLeaf() {


		skipWhitespace();

		// a leaf consists just of a data string. use the ':' as terminator for now (this is not correct, as there doesn't have to be a branch length (parsr will crash on tree with only one leaf...));
		//int end = findNext(ptr, ':');
		int end = findEndOfBranch(ptr);
		String ld = substring(ptr, end);

		ptr = end;


	//	System.out.printf("leaf: %s\n", ld);
		LN n = LN.create();
		n.data.setTipName(ld);
		n.data.setTipSerial(nLeafs);
		//n.data = ld;
		n.data.isTip = true; // fake

		nLeafs++;
		return n;
	}

	private int findEndOfBranch(int pos) {
		char[] termchars = { 
			':',
			',',
			')',
			';'
		};
		
		try {
            while (!isOneOf(inputA[pos], termchars)) {
                pos++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printLocation();
            throw new RuntimeException( "reached end of input while looking for end of branch label" );
        }

		return pos;
	}

	private boolean isOneOf(byte c, char[] chars) {
		for( char tc : chars ) {
			if( c == tc ) {
				return true;
			}
		}
		return false;
	}

	private int findNext(int pos, char c) {

        try {
            while (inputA[pos] != c) {
                pos++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printLocation();
            throw new RuntimeException( "reached end of input while looking for character " + c );
        }

		return pos;
	}

	private boolean isFloatChar(byte c) {
		return Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-';
	}

	private int findFloat(int pos) {
		while (isFloatChar(inputA[pos])) {
			pos++;
		}

		return pos;
	}

	

	public int getNTips() {
		return nLeafs;
	}

	
	public int getNInnerNodes() {
		return nInnerNodes;
	}

	public static void main(String[] args) {
		TreeParserMulti.QUIET = !true;
		try {
			LN t = TreeParserMulti.parse(new File( args[0] ));
			//System.out.printf( "good\n" );
			
			printPhyxml(t, new PrintWriter( System.out ), true );
		} catch( RuntimeException e ) {
			if( !TreeParserMulti.QUIET ) {
				e.printStackTrace();
			}
			System.out.printf( "bad\n" );
		}
	}

	
	static void indent( PrintWriter w, int n) {
		for( int i = 0; i < n; ++i ) {
			w.print(' ');
		}
	}
	static void printPhyxmlClade( LN n, PrintWriter w, int indent ) {
		indent( w, indent );
		w.println( "<clade>" );
		if( n.data == null ) {
			throw new RuntimeException();
		}
		
		if( n.data.isTip ) {
			indent( w, indent + 2 );
			w.printf( "<name>%s</name>\n", n.data.getTipName() );
			indent( w, indent + 2 );
			w.printf( "<branch_length>%f</branch_length>\n", n.backLen );
		} else {
			indent( w, indent + 2 );
			w.printf( "<branch_length>%f</branch_length>\n", n.backLen );
			
			LN nx = n.next;
			
			while( nx != n ) {
				printPhyxmlClade( nx.back, w, indent + 2 );
				nx = nx.next;
			}
		}
		indent( w, indent );
		w.println( "</clade>" );
	}
	static void printPhyxml( LN n, PrintWriter w, boolean back ) {
		w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<phyloxml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd\" xmlns=\"http://www.phyloxml.org\">\n<phylogeny rooted=\"false\">\n");
		indent(w,2);
		w.println( "<clade>" );
		if( back ) {
			printPhyxmlClade( n.back, w, 2 );
		}
		
		LN nx = n.next;
		
		while( nx != n ) {
			printPhyxmlClade( nx.back, w, 2 );
			nx = nx.next;
		}
		
		indent( w, 2 );
		w.println( "</clade>" );
		w.println("</phylogeny>\n</phyloxml>\n");
		w.flush();
	}
}