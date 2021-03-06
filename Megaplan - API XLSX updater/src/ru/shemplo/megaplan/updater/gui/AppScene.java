package ru.shemplo.megaplan.updater.gui;

public enum AppScene {

	LOADING_SCENE ("loading_scene.fxml", null),
	LOGIN_SCENE ("login_scene.fxml", new LoginScene ()),
	MAIN_SCENE ("main_scene.fxml", new MainScene ());
	
	public final AppSceneInstance INSTANCE;
	public final String FILE;
	
	private AppScene (String file, final AppSceneInstance instance) {
		this.INSTANCE = instance;
		this.FILE = file;
	}
	
}
