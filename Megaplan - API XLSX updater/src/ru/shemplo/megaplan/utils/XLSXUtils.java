package ru.shemplo.megaplan.utils;


public class XLSXUtils {

	public static String numToString (int number) {
		String result = "";
		number ++;
		
		while (number != 0) {
			int rest = (number - 1) % 26;
			result = ((char) ('a' + rest)) + result;
			number = (number - rest) / 26;
		}
		
		return result.toUpperCase ();
	}
	
}
