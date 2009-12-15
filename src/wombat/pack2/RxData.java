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
