package ru.shemplo.megaplan.utils;

import java.util.function.Function;

public class FormatterUtils {

	public static final Function <String, String> F_CLASSIC = a -> {
		String day = a.substring (0, 2),
				month = a.substring (3, 5),
				year = a.substring (6);
		return year + "-" + month + "-" + day;
	};
	
}
