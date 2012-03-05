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
package wombat.pack2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RxInfo {
    public static void main(String[] args) throws IOException,
	    ClassNotFoundException {
	File rxFile = new File(args[0]);

	ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
		new FileInputStream(rxFile)));

	RxData rx = (RxData) ois.readObject();
	ois.close();

	for (RxData.RxFile rxf : rx.rxfs) {
	    System.out.printf("%s %d\n", rxf.localName, rxf.data.length);
	}
	System.out.printf("pre time: %.2f s\n",
		(rx.timeStart - rx.timeFirst) / 1000.0);
	System.out.printf("exec time: %.2f s\n",
		(rx.timeEnd - rx.timeStart) / 1000.0);

    }
}
