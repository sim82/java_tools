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

public class ColtBlasTest {

    long seed;

    double randum() {
	long sum, mult0, mult1, seed0, seed1, seed2, newseed0, newseed1, newseed2;
	double res;

	mult0 = 1549;
	seed0 = seed & 4095;
	sum = mult0 * seed0;
	newseed0 = sum & 4095;
	sum >>>= 12;
	seed1 = (seed >>> 12) & 4095;
	mult1 = 406;
	sum += mult0 * seed1 + mult1 * seed0;
	newseed1 = sum & 4095;
	sum >>>= 12;
	seed2 = (seed >>> 24) & 255;
	sum += mult0 * seed2 + mult1 * seed1;
	newseed2 = sum & 255;

	seed = newseed2 << 24 | newseed1 << 12 | newseed0;
	res = 0.00390625 * (newseed2 + 0.000244140625 * (newseed1 + 0.000244140625 * newseed0));

	return res;
    }

    public static void main(String[] args) {
	(new ColtBlasTest()).run();

    }

    private int AADR(int i, int j, int matrixSize) {
	return i * matrixSize + j;
    }

    private void run() {
	seed = 12345;

	for (int matrixSize = 16; matrixSize <= 1024; matrixSize *= 2) {

	    double[] a = new double[matrixSize * matrixSize];
	    double[] b = new double[matrixSize * matrixSize];
	    double[] c = new double[matrixSize * matrixSize];

	    for (int i = 0; i < matrixSize; i++) {
		for (int j = 0; j < matrixSize; j++) {
		    a[AADR(i, j, matrixSize)] = randum();
		    b[AADR(i, j, matrixSize)] = randum();
		    c[AADR(i, j, matrixSize)] = 0.0;
		}
	    }

	    // System.out.printf("start: %d\n", matrixSize);
	    long time1 = System.currentTimeMillis();

	    // for (int i = 0; i < matrixSize; i++) {
	    // for (int k = 0; k < matrixSize; k++) {
	    // for (int j = 0; j < matrixSize; j++) {
	    //
	    // c[AADR(i, j, matrixSize)] += a[AADR(i, k, matrixSize)]
	    // * b[AADR(k, j, matrixSize)];
	    // }
	    // }
	    // }

	    for (int i = 0; i < matrixSize; i++) {
		for (int k = 0; k < matrixSize; k++) {
		    int ik = AADR(i, k, matrixSize);
		    for (int j = 0; j < matrixSize; j += 8) {

			int ij = AADR(i, j, matrixSize);
			int kj = AADR(k, j, matrixSize);
			c[ij] += a[ik] * b[kj];
			c[ij + 1] += a[ik] * b[kj + 1];
			c[ij + 2] += a[ik] * b[kj + 2];
			c[ij + 3] += a[ik] * b[kj + 3];
			c[ij + 4] += a[ik] * b[kj + 4];
			c[ij + 5] += a[ik] * b[kj + 5];
			c[ij + 6] += a[ik] * b[kj + 6];
			c[ij + 7] += a[ik] * b[kj + 7];
		    }
		}
	    }

	    // System.out.printf("done: %d\n", System.currentTimeMillis() -
	    // time1);

	    double looptime = (System.currentTimeMillis() - time1) / 1000.0;
	    double checkSum = 0.0;

	    for (int i = 0; i < matrixSize; i++) {
		for (int j = 0; j < matrixSize; j++) {
		    checkSum += c[AADR(i, j, matrixSize)];
		}
	    }
	    System.out.printf("Size %d Loop time %f Check Sum %f\n",
		    matrixSize, looptime, checkSum);
	    // System.out.printf("cs: %f\n", checkSum);
	}
    }

}
