package ru.shemplo.megaplan.updater.gui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.shemplo.exception.AuthorizationException;
import ru.shemplo.megaplan.network.APIConnection_dep;
import ru.shemplo.megaplan.updater.MegaplanAPIManager;

public class LoginScene extends AbstractScene {
	
	private TextField loginField, hostField;
	private PasswordField passwordField;
	private Label progressLog;
	private Button authButton;
	private BorderPane root;
	private HBox authForm;
	
	@Override
	public void onSceneOpen () {
		this.root = (BorderPane) scene.lookup ("#main_root_pane");
		root.requestFocus ();
		
		this.authForm = (HBox) scene.lookup ("#client_form");
		authForm.applyCss ();
		
		this.loginField = (TextField) scene.lookup ("#client_login");
		loginField.applyCss ();
		
		this.passwordField = (PasswordField) scene.lookup ("#client_password");
		passwordField.applyCss ();
		
		this.hostField = (TextField) scene.lookup ("#client_host");
		hostField.applyCss ();
		
		this.authButton = (Button) scene.lookup ("#client_auth");
		authButton.setOnAction (ae -> authorize ());
		authButton.setMinWidth (48);
		authButton.applyCss ();
		
		this.progressLog = (Label) scene.lookup ("#progress_log");
		progressLog.applyCss ();
		
		root.applyCss ();
		root.autosize ();
		
		scene.getWindow ().setHeight (root.getHeight () + 32);
		scene.getWindow ().setWidth (root.getWidth ());
		
		if (System.getProperty ("megaplan.api.login") != null) {
			passwordField.setText (System.getProperty ("megaplan.api.password"));
			loginField.setText (System.getProperty ("megaplan.api.login"));
			hostField.setText (System.getProperty ("megaplan.api.host"));
			authorize ();
		}
	}
	
	private void authorize () {
		changeState (false, "Connecting to Megaplan...");
		
		Runnable task = () -> {
			try {
				String password = passwordField.getText ().trim (),
						login = loginField.getText ().trim (),
						host = hostField.getText ().trim ();
				//password = APIConnection.hashMD5 (password);
				APIConnection_dep.authorize (login, password, host);
				if (APIConnection_dep.isAuthorized ()) {
					Platform.runLater ( // Turn back to main thread
							() -> changeState (true, "Authorized"));
					MegaplanAPIManager.switchScenes (AppScene.MAIN_SCENE);
					return;
				}
				
				Platform.runLater ( // Turn back to main thread
						() -> changeState (true, "Unexpected error (try again)"));
			} catch (AuthorizationException ae) {
				Platform.runLater ( // Turn back to main thread
						() -> changeState (true, ae.getMessage ()));
			}
		};
		
		Thread t = new Thread (task);
		t.setDaemon (true);
		t.start ();
	}
	
	private void changeState (boolean active, String text) {
		authForm.setDisable (!active);
		progressLog.setText (text);
		root.requestFocus ();
	}

}
