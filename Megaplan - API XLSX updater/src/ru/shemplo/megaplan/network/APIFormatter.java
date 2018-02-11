package ru.shemplo.megaplan.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ru.shemplo.support.UserProfile;

public class APIFormatter {

	private static final Map <String, Function <String, String>> rules;
	static {
		rules = new HashMap <> ();
		
		rules.put ("Birthday", d -> UserProfile.convertDate (d));
	}
	
	public static String format (String key, String value) {
		if (APIFormatter.rules.containsKey (key)) {
			return APIFormatter.rules.get (key).apply (value);
		}
		
		return value;
	}
	
}
