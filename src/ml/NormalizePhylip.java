package ml;

public class NormalizePhylip {
	public static void main(String[] args) {
		MultipleAlignment ma = MultipleAlignment.loadPhylip(System.in);
		ma.writePhylip(System.out);
	}
}
