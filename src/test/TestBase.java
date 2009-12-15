package test;

public class TestBase {
	
	
	
	public static void main(String[] args) {
		String bla;
		if( args.length > 2 ) {
			bla = null;
		} else {
			bla = "abc";
		}
		
		
		bla.length();
		if( bla != null ) {
			bla.length();
		}
	}
}
