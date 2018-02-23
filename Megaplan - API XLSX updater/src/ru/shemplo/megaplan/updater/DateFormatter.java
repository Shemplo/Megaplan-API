package ru.shemplo.megaplan.updater;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.shemplo.megaplan.utils.FormatterUtils;

public enum DateFormatter {

	CLASSIC        ("23.02.2018", FormatterUtils.F_CLASSIC),
	CLASSIC_BEND   ("23/02/2018", FormatterUtils.F_CLASSIC),
	CLASSIC_DAHSED ("23-02-2018", FormatterUtils.F_CLASSIC);
	
	private static final Set <String> DATE_FIELDS;
	static {
		DATE_FIELDS = new HashSet <> ();
		DATE_FIELDS.addAll (Arrays.asList (
			"Birthday",
			"TimeCreated",
			"DataRozhdeniya",
			"DataVidachi",
			"SrokDeystviya",
			"DataVidachiP",
			"SrokDeystviyaP",
			"DataVEzda",
			"SrokPrebivaniyaDo",
			"NaUcheteS",
			"NaUchetePo",
			"DataVidachiMigr",
			"DataVidachiR",
			"DeystvuetRS",
			"SrokDeystviyaR",
			"DataZaklyucheniyaDogovora",
			"DataPrekrashchDogovora"
		));
	}
	
	public static boolean isDateField (String name) {
		if (name == null) { return false; }
		
		name = name.replace (MegaplanAPIManager.FIELDS_PREFIX, "");
		return DATE_FIELDS.contains (name);
	}
	
	public static ObservableList <String> getList () {
		ObservableList <String> list = FXCollections.<String> observableArrayList ();
		for (DateFormatter formatter : values ()) {
			list.add (formatter.VIEW);
		}
		
		return list;
	}
	
	public static DateFormatter getFormatter (String template) {
		for (DateFormatter formatter : values ()) {
			if (formatter.VIEW.equals (template)) {
				return formatter;
			}
		}
		
		return null;
	}
	
	public final Function <String, String> F;
	public final String VIEW;
	
	private DateFormatter (String example, Function <String, String> formatter) {
		this.VIEW = example;
		this.F = formatter;
	}
	
}
