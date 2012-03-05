package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class LargePhylip {
    FileChannel fc;
    long fileSize;
    MappedByteBuffer mbuf;
    Rec[] recs;

    int nTaxa;
    int seqLen;
    int maxNameLen;
    long headerSize = -1;

    static class Rec {
	// NOTE: the two ByteBuffers are actually slices from the mmapped phylip
	// file (=only pointers)
	ByteBuffer name;
	int nameLen;

	ByteBuffer data;
	int dataLen;

	public void print() {
	    // TODO Auto-generated method stub
	    System.out.printf("len: %d %d\n", nameLen, dataLen);

	}

	public String getName() {
	    byte[] ba = new byte[nameLen];
	    name.rewind();
	    name.get(ba);
	    return new String(ba);
	}

	public String getData() {
	    byte[] ba = new byte[dataLen];
	    data.rewind();
	    data.get(ba);
	    return new String(ba);
	}
    }

    public LargePhylip(File file, boolean create) {
	if (!create) {
	    try {
		RandomAccessFile fis = new RandomAccessFile(file, "r");
		fc = fis.getChannel();
		fileSize = file.length();

		mbuf = fc.map(MapMode.READ_ONLY, 0, fileSize);
		// System.out.printf( "mbuf size: %d\n", mbuf.capacity() );
		long ptr = 0;
		mbuf.position(0);

		boolean haveHeader = false;
		int curtax = 0;
		while (ptr < fileSize) {

		    // save start position of current line
		    long spos = mbuf.position();

		    // seek to line end or EOF
		    while (ptr < fileSize && mbuf.get() != '\n') {
			ptr++;
		    }

		    // ptr currently points to the newline (or the first
		    // position past the EOF).
		    // in the non-EOF case this is one position before the
		    // current mbuf.position()
		    long epos = ptr;
		    mbuf.position((int) spos);
		    ByteBuffer line = mbuf.slice();

		    // calc line lenght excluding the newline
		    long lineLen = epos - spos;
		    // byte[] l = new byte[(int) lineLen];

		    // line.get(l);

		    // System.out.printf( "line: '%s'\n", new String(l) );

		    // advance ptr past newline (=beginning of next line)
		    ptr++;

		    if (ptr < fileSize - 1) {
			mbuf.position((int) ptr);
		    }

		    // interpret line
		    // header and data lines look basically the same ...
		    Rec rec = interpret(line, lineLen);
		    // rec.print();
		    if (!haveHeader) {

			String name = rec.getName();
			String data = rec.getData();

			int ntaxa = Integer.parseInt(name);
			int seqlen = Integer.parseInt(data);

			// System.out.printf( "header: %d %d\n", ntaxa, seqlen
			// );
			haveHeader = true;

			recs = new Rec[ntaxa];

			nTaxa = ntaxa;
			seqLen = seqlen;
		    } else {
			if (lineLen != 0) {
			    recs[curtax] = rec;
			    curtax++;

			    maxNameLen = Math.max(rec.nameLen, maxNameLen);
			} else {
			    System.out.printf("skip empty line\n");
			}
		    }
		}

	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {

	}
    }

    Rec getRecord(int n) {

	return recs[n];
    }

    int size() {
	return recs.length;
    }

    long dataOffset(int n) {
	return headerSize + n * (maxNameLen + 1 + seqLen + 1);
    }

    public LargePhylip(File file, int nTaxa, int seqLen, int maxNameLen) {
	this.nTaxa = nTaxa;
	this.seqLen = seqLen;
	this.maxNameLen = maxNameLen;

	if (file.exists()) {
	    throw new RuntimeException("file exists. chicken out.");
	}

	String header = nTaxa + " " + seqLen + "\n";

	headerSize = header.length();
	fileSize = headerSize + nTaxa * (maxNameLen + 1 + seqLen + 1); // data

	RandomAccessFile fis;
	try {

	    // calculate file size

	    System.out.printf("file size: %d\n", fileSize);
	    fis = new RandomAccessFile(file, "rw");
	    fis.setLength(fileSize);

	    fc = fis.getChannel();
	    mbuf = fc.map(MapMode.READ_WRITE, 0, fileSize);

	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	mbuf.position(0);
	mbuf.put(strToByteArray(header));

	recs = new Rec[nTaxa];
	for (int i = 0; i < nTaxa; i++) {
	    Rec r = new Rec();

	    long ofs = dataOffset(i);
	    mbuf.position((int) ofs);
	    r.name = mbuf.slice();
	    r.name.limit(maxNameLen + 1);
	    mbuf.position((int) ofs + maxNameLen + 1);

	    r.data = mbuf.slice();
	    r.data.limit(seqLen + 1);

	    recs[i] = r;
	}
    }

    private byte[] strToByteArray(String header) {

	byte[] ba = new byte[header.length()];
	for (int i = 0; i < ba.length; i++) {
	    ba[i] = (byte) header.charAt(i);
	}

	return ba;
    }

    int lenOfInt(int i) {
	return ("" + i).length();
    }

    private static boolean isSpace(byte c) {
	// System.out.printf( "isSpace: '%c'\n", c );
	return c == ' ' || c == '\t';
    }

    private Rec interpret(ByteBuffer line, long lineLen) {
	Rec r = new Rec();
	r.name = line.slice();

	int nnspace = 0;
	while (line.hasRemaining() && !isSpace(line.get())) {
	    nnspace++;
	}
	if (nnspace == 0) {
	    throw new RuntimeException("missing first field");
	}
	// System.out.printf( "nnspace: %d\n", nnspace );
	r.nameLen = line.position() - 1;
	r.name.limit(r.nameLen);

	while (line.hasRemaining() && isSpace(line.get())) {

	}

	line.position(line.position() - 1);

	r.data = line.slice();
	r.dataLen = (int) (lineLen - line.position());
	r.data.limit(r.dataLen);
	return r;
    }

    public static void main(String[] args) {
	LargePhylip lph = new LargePhylip(new File(args[0]), false);
	LargePhylip lph2 = new LargePhylip(new File(args[1]), false);

	int maxNameLen = Math.max(lph.maxNameLen, lph2.maxNameLen);

	System.out.printf("ret %d\n", lph.maxNameLen);
	LargePhylip lphw = new LargePhylip(new File("/scratch/big.phy"),
		lph.nTaxa + lph2.nTaxa, lph.seqLen, maxNameLen);

	int outptr = 0;
	for (int i = 0; i < lph.nTaxa; i++) {
	    lphw.copy(outptr, lph.recs[i]);
	    outptr++;
	}

	for (int i = 0; i < lph2.nTaxa; i++) {
	    lphw.copy(outptr, lph.recs[i]);
	    outptr++;
	}
	System.out.printf("1st copy done\n");
	System.gc();

	// lph.close();
	// lph2.close();
	// lph = null;
	// lph2 = null;
	// System.out.printf( "freed\n" );
	// System.gc();
	//
	// try {
	// Thread.currentThread().sleep(100000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }

	LargePhylip lphw2 = new LargePhylip(new File("/scratch/big2.phy"),
		lphw.nTaxa + lphw.nTaxa, lph.seqLen, maxNameLen);
	outptr = 0;
	for (int i = 0; i < lphw.nTaxa; i++) {
	    lphw2.copy(outptr, lphw.recs[i]);
	    outptr++;
	}

	for (int i = 0; i < lphw.nTaxa; i++) {
	    lphw2.copy(outptr, lphw.recs[i]);
	    outptr++;
	}

	// lph.close();
	// lph2.close();

	// lph = null;
	// lph2 = null;

	LargePhylip lphw3 = new LargePhylip(new File("/scratch/big3.phy"),
		lphw2.nTaxa + lphw2.nTaxa, lph.seqLen, maxNameLen);
	outptr = 0;
	for (int i = 0; i < lphw2.nTaxa; i++) {
	    lphw3.copy(outptr, lphw2.recs[i]);
	    outptr++;
	}

	for (int i = 0; i < lphw2.nTaxa; i++) {
	    lphw3.copy(outptr, lphw2.recs[i]);
	    outptr++;
	}

	LargePhylip lphw4 = new LargePhylip(new File("/scratch/big4.phy"),
		lphw3.nTaxa + lphw3.nTaxa, lph.seqLen, maxNameLen);
	outptr = 0;
	for (int i = 0; i < lphw3.nTaxa; i++) {
	    lphw4.copy(outptr, lphw3.recs[i]);
	    outptr++;
	}

	for (int i = 0; i < lphw3.nTaxa; i++) {
	    lphw4.copy(outptr, lphw3.recs[i]);
	    outptr++;
	}
	System.out.printf("done\n");
	try {
	    Thread.sleep(100000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    void close() {
	mbuf = null;
	try {
	    fc.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    void copy(int n, Rec rec) {
	Rec orec = recs[n];

	rec.name.rewind();
	orec.name.rewind();
	orec.name.put(rec.name);
	for (int i = 0; i < orec.name.limit() - rec.name.limit(); i++) {
	    orec.name.put((byte) ' ');
	}

	orec.nameLen = rec.nameLen;

	rec.data.rewind();
	orec.data.rewind();
	orec.data.put(rec.data);
	if (orec.data.limit() > rec.data.limit()) {
	    orec.data.put((byte) '\n');
	}
    }
}
