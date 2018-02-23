package ru.shemplo.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import ru.shemplo.exception.UserProfileException;

public class UserProfile {
	
	private final Map <String, String> values;
	public final long ID;
	
	public UserProfile (long id) throws UserProfileException {
		this.values = new HashMap <> ();
		this.ID = id;
	}
	
	public UserProfile (long id, List <Pair <String, String>> properties) throws UserProfileException {
		this.values = new HashMap <> ();
		if (properties == null || properties.size () == 0) {
			String message = "(NPE) given properties array is null";
			throw new UserProfileException (message);
		}
		
		for (int i = 0; i < properties.size (); i ++) {
			Pair <String, String> property = properties.get (i);
			values.put (property.f, property.s);
		}
		
		this.ID = id;
	}
	
	@SafeVarargs
	public UserProfile (long id, Pair <String, String>... properties) throws UserProfileException {
		this.values = new HashMap <> ();
		if (properties == null || properties.length == 0) {
			String message = "(NPE) given properties array is null";
			throw new UserProfileException (message);
		}
		
		for (int i = 0; i < properties.length; i ++) {
			Pair <String, String> property = properties [i];
			values.put (property.f, property.s);
		}
		
		this.ID = id;
	}
	
	@Override
	public String toString () {
		return ID + " " + values.toString ();
	}
	
	public String getValue (String key) throws UserProfileException {
		if (key == null) {
			String message = "(NPE) given property key is null";
			throw new UserProfileException (message);
		}
		
		return values.get (key);
	}
	
	public void setNewValue (String key, String value) throws UserProfileException {
		if (key == null) {
			String message = "(NPE) given property key is null";
			throw new UserProfileException (message);
		}
		
		this.values.put (key, value);
	}
	
	public List <String> findDifferences (UserProfile profile) throws UserProfileException {
		if (profile == null) {
			String message = "(NPE) given profile is null";
			throw new UserProfileException (message);
		}
		
		List <String> keys = new ArrayList <> ();
		for (String key : values.keySet ()) {
			if (!profile.values.containsKey (key)
					|| !profile.values.get (key).equals (values.get (key))) {
				keys.add (key); // In this filed is difference;
			}
		}
		
		return keys;
	}
	
	public static UserProfile makeFromJSON (JSONObject object) throws UserProfileException {
		if (object == null) {
			String message = "(NPE) given JSON object is null";
			throw new UserProfileException (message);
		}
		
		String [] keys = JSONObject.getNames (object);
		UserProfile profile = new UserProfile (object.getLong ("Id"));
		
		for (String key : keys) {
			Object value = object.get (key);
			if ((value instanceof String)) {
				if (((String) value).length () == 0) { continue; }
			}
			
			if (value != null) {
				profile.values.put (key, value.toString ());
			}
		}
		
		return profile;
	}
	
	public static String convertDate (String date) {
		String [] bParts = date.split ("\\."); // Converting to Y-m-d
		if (bParts.length < 3) { return ""; }
		
		return bParts [2] + "-" + bParts [1] + "-" + bParts [0];
	}
	
}
