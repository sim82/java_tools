package ml;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class ColumnOccMap {
    Map<String, BitSet> map = new HashMap<String, BitSet>();

    void add(MultipleAlignment ma) {

	String[] datass = ma.data;
	String[] names = ma.names;

	for (int i = 0; i < names.length; i++) {
	    String name = names[i];
	    String data = datass[i];
	    if (map.containsKey(name)) {
		continue;
	    }

	    int dl = data.length();
	    BitSet bs = new BitSet(dl);
	    for (int j = 0; j < dl; j++) {
		bs.set(j, !isgap(data.charAt(j)));
	    }
	    map.put(name, bs);
	}

    }

    void add(LargePhylip lp) {

	int size = -1;

	for (int i = 0; i < lp.size(); i++) {
	    LargePhylip.Rec rec = lp.getRecord(i);

	    String name = rec.getName();
	    if (map.containsKey(name)) {
		continue;
	    }

	    String data = rec.getData();
	    int dl = data.length();

	    if (size < 0) {
		size = dl;
	    } else {
		if (size != dl) {
		    throw new RuntimeException("wrong seq len");
		}

	    }

	    BitSet bs = new BitSet(dl);
	    for (int j = 0; j < dl; j++) {
		bs.set(j, !isgap(data.charAt(j)));
	    }
	    map.put(name, bs);
	}

    }

    static boolean isgap(char c) {
	return c == '-' || c == 'N';
    }
}
