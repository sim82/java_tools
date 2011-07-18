package ml;

public class NormalizePhylip {
    public static void main(String[] args) {
	MultipleAlignment ma = MultipleAlignment.loadPhylip(System.in);

	char[] tmp = new char[ma.seqLen];

	for (int i = 0; i < ma.data.length; i++) {
	    String s = ma.data[i];
	    int len = s.length();
	    for (int j = 0; j < len; j++) {
		char c = s.charAt(j);

		switch (c) {
		case 'U':
		    c = 'T';
		    break;

		case 'u':
		    c = 't';
		    break;
		}

		tmp[j] = c;

	    }

	    ma.data[i] = new String(tmp);
	}

	ma.writePhylip(System.out);
    }
}
