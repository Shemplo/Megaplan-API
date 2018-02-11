package ru.shemplo.megaplan.updater.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import ru.shemplo.megaplan.updater.MegaplanAPIManager;

public abstract class AbstractScene implements AppSceneInstance {

	protected static List <String> styles = new ArrayList <> ();
	
	public static boolean addCommonStyle (String styleFile) {
		String style = ClassLoader.getSystemResource (styleFile).toExternalForm ();
		if (style == null) { return false; }
		
		for (String string: styles) {
			if (string.equals (style)) {
				return true;
			}
		}
		
		styles.add (style);
		return true;
	}
	
	// -===| INSTANCE |====- //
	
	protected Scene scene = MegaplanAPIManager.getStageScene ();
	
}
