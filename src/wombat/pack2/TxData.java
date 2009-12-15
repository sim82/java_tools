package wombat.pack2;

import java.io.Serializable;



@SuppressWarnings("serial")
public class TxData implements Serializable {
	static class FileMapping implements Serializable {
		/**
		 * 
		 */
		String origName;
		String tmpName;
		
		byte[] storedFile;
		boolean isExecutable;
		boolean isGz; 
		byte[] md5sum;
		
		public FileMapping( String origName, String tmpName ) {
			this.origName = origName;
			this.tmpName = tmpName;
		}
		
		

		
	}
	
	String[] es;
	String[] esNew;
	FileMapping[] fms;
}
