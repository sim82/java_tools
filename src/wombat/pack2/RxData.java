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

import java.io.Serializable;

@SuppressWarnings("serial")
public class RxData implements Serializable {
	public static class RxFile implements Serializable {
		public String localName;
		public byte[] md5sum;
		public boolean isGzip;
		public byte[] data;
		
		
	}
	
	
	public RxFile[] rxfs;

	public long timeFirst;
	public long timeStart;
	public long timeEnd;
}
