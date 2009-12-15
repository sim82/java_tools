package util;

public class StringTools {
	public static String padchar(String input_string, int with_digit, int to_len) {
		while (input_string.length() < to_len) {
			input_string = with_digit + input_string;
		}
		return input_string;
	}
}
