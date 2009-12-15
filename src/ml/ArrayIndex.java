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
