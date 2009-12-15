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

import java.util.HashMap;

public class ArrayIndex {
	String[] a;
	HashMap<String,Integer> idx = new HashMap<String, Integer>();
	boolean doubleKeys = false;
	
	ArrayIndex( String[] a ) {
		this.a = a;
		
		for( int i = 0; i < a.length; i++ ) {
			doubleKeys |= idx.containsKey(a[i]);
			idx.put( a[i], i );
		}
	}
	
	int getIdx( String key ) {
		return idx.get(key);
	}
}
