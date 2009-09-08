package ml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GZStreamAdaptor {
	public static InputStream open( File file ) throws FileNotFoundException, IOException {
		String name = file.getName();
		
		if( name.endsWith(".gz" )) {
			return new GZIPInputStream(new FileInputStream(file));
		} else {
			return new FileInputStream(file);
		}
		
	}
}
