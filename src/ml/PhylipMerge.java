package ml;

import java.io.File;

public class PhylipMerge {
    public static void main(String[] args) {
	MultipleAlignment ma1 = MultipleAlignment.loadPhylip(new File(args[0]));
	MultipleAlignment ma2 = MultipleAlignment.loadPhylip(new File(args[1]));

	MultipleAlignment outMa = new MultipleAlignment(
		ma1.nTaxon + ma2.nTaxon, ma1.seqLen);
	int outPtr = 0;

	for (int i = 0; i < ma1.nTaxon; i++) {
	    outMa.names[outPtr] = ma1.names[i];
	    outMa.data[outPtr] = ma1.data[i];
	    outPtr++;
	}

	for (int i = 0; i < ma2.nTaxon; i++) {
	    outMa.names[outPtr] = ma2.names[i];
	    outMa.data[outPtr] = ma2.data[i];
	    outPtr++;
	}

	outMa.writePhylip(System.out);
    }
}
