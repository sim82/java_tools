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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class CSVFile {
	enum ColType {
		STRING,
		INTEGER,
		DOUBLE
	}
	
	ColType[] colTypes;
	Object[] colArrays;
	int nlines;
	
	public CSVFile( ColType[] colTypes ) {
		this.colTypes = colTypes.clone();
		colArrays = new Object[colTypes.length];
	}

	static ColType[] ctaFromString( String s ) {
		ColType[] cta = new ColType[s.length()];
		
		for( int i = 0; i < s.length(); i++ ) {
			char c = s.charAt(i);
			
			switch( c ) {
			case 'S':
			case 's':
				cta[i] = ColType.STRING;
				break;
				
			case 'I':
			case 'i':
				cta[i] = ColType.INTEGER;
				break;
				
			case 'D':
			case 'd':
				cta[i] = ColType.DOUBLE;
				break;
				
			default:
				throw new RuntimeException( "cannot parse column specifier: " + s );
					
			}
		}
		
		return cta;
	}
	
	static CSVFile load( File file, String colTypes ) {
		return load( file, ctaFromString(colTypes));
	}
	
	static CSVFile load( File file, ColType[] colTypes ) {
//		if( colTypes != null || colTypes != null ) {
//			throw new RuntimeException( "'CSVFile.load' called twice" );
//		}
		
		try {
			CSVFile cf = new CSVFile(colTypes);
			
			BufferedReader r = new BufferedReader(new FileReader(file));
			
			
			int nlines = 0;
			while( r.readLine() != null ) {
				nlines++;
			}
			
			r.close();
						
			cf.colArrays = new Object[colTypes.length];
			for( int i = 0; i < colTypes.length; i++ ) {
				switch( colTypes[i] ) {
				case STRING:
					cf.colArrays[i] = new String[nlines];
					break;
					
				case INTEGER:
					cf.colArrays[i] = new int[nlines];
					break;
					
				case DOUBLE:
					cf.colArrays[i] = new double[nlines];
					break;
				}
			}


			r = new BufferedReader(new FileReader(file));

			int cline = 0;
			String line = null;
			while( (line = r.readLine()) != null ) {
				StringTokenizer ts = new StringTokenizer(line);
				
				for( int i = 0; i < colTypes.length; i++ ) {
					String token = ts.nextToken();
					
					switch( colTypes[i] ) {
					case STRING:
						String[] sa = (String[]) cf.colArrays[i];
						sa[cline] = token;
						break;
						
					case INTEGER:
						int[] ia = (int[]) cf.colArrays[i];
						ia[cline] = Integer.parseInt(token);
						break;
						
					case DOUBLE:
						double[] da = (double[]) cf.colArrays[i];
						da[cline] = Double.parseDouble(token);
						break;
					}
				}
				
				cline++;
			}
			
			cf.nlines = cline;
			r.close();
			
			return cf;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException( "bailing out" );
		}
	}
	
	
	void print() {
		if( colTypes == null || colTypes == null ) {
			throw new RuntimeException( "CSVFile not initialized" );
		}
	
		for( int i = 0; i < nlines; i++ ) {
			for( int j = 0; j < colTypes.length; j++ ) {
				
				
				switch( colTypes[j] ) {
				case STRING:
					String[] sa = (String[]) colArrays[j];
					System.out.print( sa[i] );
					
					break;
					
				case INTEGER:
					int[] ia = (int[]) colArrays[j];
					System.out.print( ia[i] );
					break;
					
				case DOUBLE:
					double[] da = (double[]) colArrays[j];
					System.out.print( da[i] );
					break;
				}
				
				if( j < colTypes.length - 1 ) {
					System.out.print( "\t" );
				} else {
					System.out.println();
				}
			}
		}
		
	}
	public static void main(String[] args) {
		CSVFile cf = CSVFile.load( new File( "/mnt/nufa1/berger/rob_new/subseq/ext/RAxML_classification.new.phylip.syn_0002" ), "sssiidddi" );
		cf.print();
	}
	
	String[] getString( int col ) {
		if( colTypes[col] != ColType.STRING) {
			throw new RuntimeException( "column " + col + " is not of type STRING but" + colTypes[col] );
		} else {
			return (String[]) colArrays[col];
		}
	}
	
	int[] getInteger( int col ) {
		if( colTypes[col] != ColType.INTEGER) {
			throw new RuntimeException( "column " + col + " is not of type INTEGER but" + colTypes[col] );
		} else {
			return (int[]) colArrays[col];
		}
	}
	
	double[] getDouble( int col ) {
		if( colTypes[col] != ColType.DOUBLE) {
			throw new RuntimeException( "column " + col + " is not of type DOUBLE but" + colTypes[col] );
		} else {
			return (double[]) colArrays[col];
		}
	}
	
}
