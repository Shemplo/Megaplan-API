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
		rules.put ("TimeCreated", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataRozhdeniya", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataVidachi", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldSrokDeystviya", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataVidachiP", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldSrokDeystviyaP", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataVEzda", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldSrokPrebivaniyaDo", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldNaUcheteS", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldNaUchetePo", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataVidachiMigr", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataVidachiR", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDeystvuetRS", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldSrokDeystviyaR", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataZaklyucheniyaDogovora", d -> UserProfile.convertDate (d));
		rules.put ("Category183CustomFieldDataPrekrashchDogovora", d -> UserProfile.convertDate (d));
		rules.put ("", d -> UserProfile.convertDate (d));
	}
	
	public static String format (String key, String value) {
		if (APIFormatter.rules.containsKey (key)) {
			return APIFormatter.rules.get (key).apply (value);
		}
		
		return value;
	}
	
}
