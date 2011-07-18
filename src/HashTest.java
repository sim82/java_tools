import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;

class HashTable {
    String[] strings;
    // boolean[] ht;

    BitSet ht;
    int M;

    int nE;
    int threshold;
    float loadFactor = 0.95f;

    int colProbe;
    int PROBE = 1;
    int nhops = 0;
    int nsearch = 0;
    int nihops = 0;
    int ninserts = 0;

    public HashTable() {
	M = 16;
	threshold = (int) (M * loadFactor);
	nE = 0;
	strings = new String[M];
	// ht = new boolean[M];
	ht = new BitSet(M);
    }

    int ith(int ix) {

	return (int) (0.5 * ix + 0.5 * ix * ix);
    }

    int index(int h, int i) {
	return (h + ith(i)) & (M - 1);
    }

    void insert(String s) {
	int hash = s.hashCode();
	// System.out.printf( "hash: %s %d\n", s, hash );
	int n = 0;

	if (nE >= threshold) {
	    grow();
	}
	ninserts++;
	while (n < M) {

	    // int x = hash & (M-1);

	    // int x = (hash + ith(n)) & (M-1);
	    int x = index(hash, n);
	    // if( !ht[x] ) {
	    // ht[x] = true;
	    nihops++;
	    if (!ht.get(x)) {
		ht.set(x);
		strings[x] = s;
		// System.out.printf( "insert: %d\n", x );
		break;
	    }
	    // System.out.printf( "insert: collide: %d\n", x );
	    colProbe++;
	    n += 1;
	    // hash+=probe + PROBE;
	    // probe*=2;

	}
	nE++;
	if (n >= M) {
	    throw new RuntimeException("hash table full");
	}
    }

    int search(String s) {
	int hash = s.hashCode();
	// System.out.printf( "hash: %s %d\n", s, hash );
	int n = 0;
	nsearch++;

	while (n < M) {
	    // int x = hash & (M-1);
	    int x = index(hash, n);
	    // System.out.printf( "entry: %d %s\n", x, strings[x] );
	    // if( !ht[x] ) {
	    nhops++;

	    if (!ht.get(x)) {
		// System.out.printf( "empty: %d\n", x );
		break;
	    } else if (strings[x].equals(s)) {

		return x;
	    }
	    n += 1;
	    // hash+=probe + PROBE;
	    // probe*=2;
	}

	return -1;
    }

    private void grow() {
	String[] oldStrings = strings;
	// boolean[] oldHt = ht;
	// int oldM = M;
	System.out.printf("grow: %d %d %d %f\n", M, nihops, ninserts,
		(float) nihops / ninserts);
	nihops = 0;
	ninserts = 0;

	M *= 2;
	nE = 0;
	colProbe = 0;
	threshold = (int) (M * loadFactor);
	strings = new String[M];
	// ht = new boolean[M];
	ht = new BitSet(M);

	for (int i = 0; i < oldStrings.length; i++) {
	    if (oldStrings[i] != null) {
		insert(oldStrings[i]);
	    }
	}

    }

}

public class HashTest {
    static String[] strings;
    // static int[] ht;
    static Random rnd = new Random();

    static int N = 10 * 1024 * 1024;
    // static int N = (int) (4194304 * 0.74) * 2;
    // static int M = 1024 * 1024 * 2;
    static int L = 20;

    public static void main(String[] args) {

	strings = new String[N];

	for (int i = 0; i < N; i++) {
	    strings[i] = rndString();

	}
	System.out.printf("begin\n");

	boolean mine = true;
	if (mine) {

	    long time4 = System.currentTimeMillis();

	    HashTable ht = new HashTable();

	    for (int i = 0; i < N; i++) {
		ht.insert(strings[i]);
	    }

	    System.out.printf("time: %d\n", System.currentTimeMillis() - time4);

	    System.out.printf("col: %d\n", ht.colProbe);

	    long time1 = System.currentTimeMillis();

	    for (int j = 0; j < 10; j++) {
		for (int i = 0; i < N; i++) {
		    int idx = ht.search(strings[i]);
		    if (idx == -1) {
			System.out.printf("%s: %d %d %s\n", strings[i], i, idx,
				idx != -1 ? "true" : "false");

		    }

		}
	    }
	    System.out.printf("time: %d\n", System.currentTimeMillis() - time1);
	    System.out.printf("hops: %d %d %f\n", ht.nhops, ht.nsearch,
		    (float) ht.nhops / ht.nsearch);

	}
	if (!mine) {
	    long time3 = System.currentTimeMillis();
	    HashSet<String> hs = new HashSet<String>();

	    for (int i = 0; i < N; i++) {
		hs.add(strings[i]);
	    }

	    System.out.printf("time: %d\n", System.currentTimeMillis() - time3);

	    long time2 = System.currentTimeMillis();

	    for (int j = 0; j < 10; j++) {
		for (int i = 0; i < N; i++) {

		    if (!hs.contains(strings[i])) {
			System.out.printf("%s: %d\n", strings[i], i);

		    }

		}
	    }
	    System.out.printf("time: %d\n", System.currentTimeMillis() - time2);
	}

    }

    static String rndString() {
	char[] tmp = new char[L];
	for (int i = 0; i < L; i++) {
	    tmp[i] = (char) ('a' + rnd.nextInt((int) ('z' - 'a') + 1));
	}

	return new String(tmp);
    }

}
