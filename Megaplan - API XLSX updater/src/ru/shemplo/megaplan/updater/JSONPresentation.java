package ru.shemplo.megaplan.updater;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONPresentation {

	public static String present (JSONObject object) {
		StringBuilder sb = new StringBuilder ();
		String [] names = JSONObject.getNames (object);
		
		for (int i = 0; i < names.length; i ++) {
			Object tmp = object.get (names [i]);
			sb.append (names [i]);
			sb.append (": ");
			
			if (tmp instanceof JSONObject) {
				sb.append ("\n");
				sb.append (presentLevel ((JSONObject) tmp, ""));
			} else if (tmp instanceof JSONArray) {
				sb.append ("\n");
				sb.append (presentLevel ((JSONArray) tmp, ""));
			} else if (tmp != null) {
				sb.append (tmp.toString ());
			}
			
			sb.append ("\n");
		}
		
		return sb.toString ();
	}
	
	private static String presentLevel (JSONObject object, String prefix) {
		StringBuilder sb = new StringBuilder ();
		sb.append (prefix);
		sb.append ("{\n");
		
		String [] names = JSONObject.getNames (object);
		for (int i = 0; i < names.length; i ++) {
			Object tmp = object.get (names [i]);
			sb.append (prefix);
			sb.append ("  ");
			sb.append (names [i]);
			sb.append (": ");
			
			if (tmp instanceof JSONObject) {
				sb.append ("\n");
				sb.append (presentLevel ((JSONObject) tmp, prefix + "  "));
			} else if (tmp instanceof JSONArray) {
				sb.append ("\n");
				sb.append (presentLevel ((JSONArray) tmp, prefix + "  "));
			} else if (tmp != null) {
				sb.append (tmp.toString ());
			}
			
			sb.append ("\n");
		}
		
		sb.append (prefix);
		sb.append ("}\n");
		return sb.toString ();
	}
	
	private static String presentLevel (JSONArray array, String prefix) {
		StringBuilder sb = new StringBuilder ();
		sb.append (prefix);
		sb.append ("[\n");
		
		for (int i = 0; i < array.length (); i ++) {
			Object tmp = array.get (i);
			
			if (tmp instanceof JSONObject) {
				sb.append (presentLevel ((JSONObject) tmp, prefix + "  "));
			} else if (tmp instanceof JSONArray) {
				sb.append (presentLevel ((JSONArray) tmp, prefix + "  "));
			} else if (tmp != null) {
				sb.append (tmp.toString ());
			}
			
			sb.append ("\n");
		}
		
		sb.append (prefix);
		sb.append ("]\n");
		return sb.toString ();
	}
	
}
