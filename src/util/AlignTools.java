package util;

public class AlignTools {
    static boolean isSingleBase(int s) {
	// this was a quick from the parsimony aligner, so it may look a bit
	// awkward...
	switch (s) {
	case 'A':
	case 'C':
	case 'G':
	case 'T':
	case 'U':
	case 'a':
	case 'c':
	case 'g':
	case 't':
	case 'u':
	    return true;
	default:
	    return false;
	}

    }

    public static float alignError(String r, String t) {
	if (r.length() != t.length()) {
	    throw new RuntimeException("strings not of same length: "
		    + r.length() + " " + t.length());
	}

	int len = r.length();

	int ct1 = 0;
	int ct2 = 0;

	int right = 0;
	for (int i = 0; i < len; i++) {
	    boolean ris = isSingleBase(r.charAt(i));
	    boolean tis = isSingleBase(t.charAt(i));

	    if (ris) {
		ct1++;
	    }

	    if (tis) {
		ct2++;
	    }

	    if (ct1 == ct2 && (ris || tis)) {
		right++;
	    }
	}

	if (ct1 != ct2) {
	    throw new RuntimeException("non-gap content not of same length");
	}
	return right / ((float) ct1);
    }
}
